/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.tools.cloudstorage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of {@link GcsInputChannel} than attempts to load the data into memory
 * before it is actually needed to avoid blocking the calling thread.
 *
 */
final class PrefetchingGcsInputChannelImpl implements GcsInputChannel {

  private static final long serialVersionUID = 5119437751884637172L;

  private static final Logger log =
      Logger.getLogger(PrefetchingGcsInputChannelImpl.class.getName());

  private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

  private transient Object lock = new Object();
  private transient RawGcsService raw;
  private final GcsFilename filename;
  private final int blockSizeBytes;

  private boolean closed = false;
  private transient boolean eofHit = false;

  /**
   * If the user only reads part of the contents of the buffer and then they serialize the reader,
   * the buffer is discarded and the next request will be issued from pos. (the offset in the file
   * actually read to.)
   */
  private long readPosition;
  private long length = -1;
  private transient long fetchPosition;
  private transient Future<GcsFileMetadata> pendingFetch = null;
  private transient ByteBuffer current = EMPTY_BUFFER;
  private transient ByteBuffer next;

  private final RetryParams retryParams;
  private final Map<String, String> headers;


  PrefetchingGcsInputChannelImpl(RawGcsService raw, GcsFilename filename, int blockSizeBytes,
      long startPosition, RetryParams retryParams, Map<String, String> headers) {
    this.raw = checkNotNull(raw, "Null raw");
    this.filename = checkNotNull(filename, "Null filename");
    checkArgument(
        blockSizeBytes >= 1024, "Block size must be at least 1kb. Was: " + blockSizeBytes);
    this.blockSizeBytes = blockSizeBytes;
    this.retryParams = retryParams;
    this.headers = headers;
    checkArgument(startPosition >= 0, "Start position cannot be negitive");
    this.readPosition = startPosition;
    this.fetchPosition = startPosition;
    requestBlock();
  }

  private void readObject(ObjectInputStream aInputStream)
      throws ClassNotFoundException, IOException {
    aInputStream.defaultReadObject();
    lock = new Object();
    raw = GcsServiceFactory.createRawGcsService(headers);
    fetchPosition = readPosition;
    current = EMPTY_BUFFER;
    eofHit = length != -1 && readPosition >= length;
  }

  /**
   * Allocates a new next buffer and pending fetch.
   */
  private void requestBlock() {
    next = ByteBuffer.allocate(blockSizeBytes);
    long requestTimeout = retryParams.getRequestTimeoutMillisForCurrentAttempt();
    pendingFetch = raw.readObjectAsync(next, filename, fetchPosition, requestTimeout);
  }

  @Override
  public String toString() {
    return "PrefetchingGcsInputChannelImpl [filename=" + filename + ", blockSizeBytes="
        + blockSizeBytes + ", closed=" + closed + ", eofHit=" + eofHit + ", length=" + length
        + ", fetchPosition=" + fetchPosition + ", pendingFetch=" + pendingFetch + ", retryParams="
        + retryParams + "]";
  }

  @Override
  public boolean isOpen() {
    synchronized (lock) {
      return !closed;
    }
  }

  @Override
  public void close() {
    synchronized (lock) {
      closed = true;
    }
  }

  private void waitForFetchWithRetry() throws IOException {
    try {
      RetryHelper.runWithRetries(new Callable<Void>() {
        @Override public Void call() throws IOException, InterruptedException {
          waitForFetch();
          return null;
        }}, retryParams, GcsServiceImpl.exceptionHandler);
    } catch (RetryInterruptedException e) {
      closed = true;
      throw new ClosedByInterruptException();
    } catch (NonRetriableException e) {
      Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
      throw e;
    }
  }

  private void waitForFetch() throws IOException, InterruptedException {
    Preconditions.checkState(pendingFetch != null, "%s: no fetch pending", this);
    Preconditions.checkState(!current.hasRemaining(), "%s: current has remaining", this);
    try {
      GcsFileMetadata gcsFileMetadata = pendingFetch.get();
      flipToNextBlockAndPrefetch(gcsFileMetadata.getLength());
    } catch (ExecutionException e) {
      if (e.getCause() instanceof BadRangeException) {
        eofHit = true;
        current = EMPTY_BUFFER;
        next = null;
        pendingFetch = null;
      } else if (e.getCause() instanceof FileNotFoundException) {
        FileNotFoundException toThrow = new FileNotFoundException(e.getMessage());
        toThrow.initCause(e);
        throw toThrow;
      } else if (e.getCause() instanceof IOException) {
        log.log(Level.WARNING, this + ": IOException fetching block", e);
        requestBlock();
        throw new IOException(this + ": Prefetch failed, prefetching again", e.getCause());
      } else {
        throw new RuntimeException(this + ": Unknown cause of ExecutionException", e.getCause());
      }
    }
  }

  private void flipToNextBlockAndPrefetch(long contentLength) {
    Preconditions.checkState(next != null, "%s: no next", this);
    current = next;
    current.flip();
    fetchPosition += blockSizeBytes;
    if (length == -1) {
      length = contentLength;
    } else {
      if (contentLength != length) {
        eofHit = true;
        next = null;
        pendingFetch = null;
        throw new RuntimeException("Contents of file: " + filename + " changed while being read.");
      }
    }
    if (fetchPosition >= contentLength) {
      eofHit = true;
      next = null;
      pendingFetch = null;
    } else {
      requestBlock();
    }
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    synchronized (lock) {
      if (closed) {
        throw new ClosedChannelException();
      }
      if (eofHit && !current.hasRemaining()) {
        return -1;
      }
      Preconditions.checkArgument(dst.remaining() > 0, "Requested to read data into a full buffer");
      if (!current.hasRemaining()) {
        if (pendingFetch == null) {
          requestBlock();
        }
        waitForFetchWithRetry();
        if (eofHit && !current.hasRemaining()) {
          return -1;
        }
      }
      Preconditions.checkState(current.hasRemaining(), "%s: no remaining after wait", this);
      int toRead = dst.remaining();
      if (current.remaining() <= toRead) {
        dst.put(current);
        if (pendingFetch != null && pendingFetch.isDone()) {
          waitForFetchWithRetry();
        }
        readPosition += toRead - dst.remaining();
        return toRead - dst.remaining();
      } else {
        int oldLimit = current.limit();
        current.limit(current.position() + toRead);
        dst.put(current);
        current.limit(oldLimit);
        readPosition += toRead;
        return toRead;
      }
    }
  }
}
