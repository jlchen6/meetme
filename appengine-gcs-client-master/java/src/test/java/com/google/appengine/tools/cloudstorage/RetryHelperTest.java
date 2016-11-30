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

import static java.util.concurrent.Executors.callable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tests for Retry helper.
 *
 */
@RunWith(JUnit4.class)
public class RetryHelperTest {

  @Test
  public void testTriesWithExceptionHandling() {
    assertNull(RetryHelper.getContext());
    RetryParams params =
        new RetryParams.Builder().initialRetryDelayMillis(0).retryMaxAttempts(3).build();
    ExceptionHandler handler = new ExceptionHandler.Builder()
        .retryOn(IOException.class).abortOn(RuntimeException.class).build();
    final AtomicInteger count = new AtomicInteger(3);
    try {
      RetryHelper.runWithRetries(new Callable<Void>() {
        @Override public Void call() throws IOException, NullPointerException {
          if (count.decrementAndGet() == 2) {
            assertEquals(1, RetryHelper.getContext().getAttemptNumber());
            throw new IOException("should be retried");
          }
          assertEquals(2, RetryHelper.getContext().getAttemptNumber());
          throw new NullPointerException("Boo!");
        }
      }, params, handler);
      fail("Exception should have been thrown");
    } catch (NonRetriableException ex) {
      assertEquals("Boo!", ex.getCause().getMessage());
      assertEquals(1, count.intValue());
    }
    assertNull(RetryHelper.getContext());

    @SuppressWarnings("serial") class E1 extends Exception {}
    @SuppressWarnings("serial") class E2 extends E1 {}
    @SuppressWarnings("serial") class E3 extends E1 {}
    @SuppressWarnings("serial") class E4 extends E2 {}

    params = new RetryParams.Builder().initialRetryDelayMillis(0).retryMaxAttempts(5).build();
    handler = new ExceptionHandler.Builder().retryOn(E1.class, E4.class).abortOn(E3.class).build();
    final Iterator<? extends E1> exceptions =
        Arrays.asList(new E1(), new E2(), new E4(), new E3()).iterator();
    try {
      RetryHelper.runWithRetries(new Callable<Void>() {
        @Override public Void call() throws E1 {
          E1 exception = exceptions.next();
          throw exception;
        }
      }, params, handler);
      fail("Exception should have been thrown");
    } catch (NonRetriableException ex) {
      assertTrue(ex.getCause() instanceof E3);
    }
    assertNull(RetryHelper.getContext());
  }

  @Test
  public void testTriesAtLeastMinTimes() {
    RetryParams params = new RetryParams.Builder().initialRetryDelayMillis(0)
        .totalRetryPeriodMillis(60000)
        .retryMinAttempts(5)
        .retryMaxAttempts(10)
        .build();
    final int timesToFail = 7;
    assertNull(RetryHelper.getContext());
    int attempted = RetryHelper.runWithRetries(new Callable<Integer>() {
      int timesCalled = 0;
      @Override public Integer call() throws IOException {
        timesCalled++;
        assertEquals(timesCalled, RetryHelper.getContext().getAttemptNumber());
        assertEquals(10, RetryHelper.getContext().getRetryParams().getRetryMaxAttempts());
        if (timesCalled <= timesToFail) {
          throw new IOException();
        } else {
          return timesCalled;
        }
      }
    }, params, ExceptionHandler.getDefaultInstance());
    assertEquals(timesToFail + 1, attempted);
    assertNull(RetryHelper.getContext());
  }

  @Test
  public void testTriesNoMoreThanMaxTimes() {
    final int maxAttempts = 10;
    RetryParams params = new RetryParams.Builder().initialRetryDelayMillis(0)
        .totalRetryPeriodMillis(60000)
        .retryMinAttempts(0)
        .retryMaxAttempts(maxAttempts)
        .build();
    final AtomicInteger timesCalled = new AtomicInteger(0);
    try {
      RetryHelper.runWithRetries(callable(new Runnable() {
        @Override public void run() {
          if (timesCalled.incrementAndGet() <= maxAttempts) {
            throw new RuntimeException();
          }
          fail("Body was executed too many times: " + timesCalled.get());
        }
      }), params, new ExceptionHandler.Builder().retryOn(RuntimeException.class).build());
      fail("Should not have succeeded, expected all attempts to fail and give up.");
    } catch (RetriesExhaustedException expected) {
      assertEquals(maxAttempts, timesCalled.get());
    }
  }

  private class FakeTicker extends Ticker {
    private final AtomicLong nanos = new AtomicLong();

    /** Advances the ticker value by {@code time} in {@code timeUnit}. */
    FakeTicker advance(long time, TimeUnit timeUnit) {
      return advance(timeUnit.toNanos(time));
    }

    /** Advances the ticker value by {@code nanoseconds}. */
    FakeTicker advance(long nanoseconds) {
      nanos.addAndGet(nanoseconds);
      return this;
    }

    @Override
    public long read() {
      return nanos.get();
    }
  }

  @Test
  public void testTriesNoMoreLongerThanTotalRetryPeriod() {
    final FakeTicker ticker = new FakeTicker();
    Stopwatch stopwatch = Stopwatch.createUnstarted(ticker);
    RetryParams params = new RetryParams.Builder().initialRetryDelayMillis(0)
        .totalRetryPeriodMillis(999)
        .retryMinAttempts(5)
        .retryMaxAttempts(10)
        .build();
    ExceptionHandler handler =
        new ExceptionHandler.Builder().retryOn(RuntimeException.class).build();
    final int sleepOnAttempt = 8;
    final AtomicInteger timesCalled = new AtomicInteger(0);
    try {
      RetryHelper.runWithRetries(callable(new Runnable() {
        @Override public void run() {
          timesCalled.incrementAndGet();
          if (timesCalled.get() == sleepOnAttempt) {
            ticker.advance(1000, TimeUnit.MILLISECONDS);
          }
          throw new RuntimeException();
        }
      }), params, handler, stopwatch);
      fail();
    } catch (RetriesExhaustedException e) {
      assertEquals(sleepOnAttempt, timesCalled.get());
    }
  }

  @Test
  public void testBackoffIsExponential() {
    RetryParams params = new RetryParams.Builder()
        .initialRetryDelayMillis(10)
        .maxRetryDelayMillis(10_000_000)
        .retryDelayBackoffFactor(2)
        .totalRetryPeriodMillis(60_000)
        .retryMinAttempts(0)
        .retryMaxAttempts(100)
        .build();
    long sleepDuration = RetryHelper.getSleepDuration(params, 1);
    assertTrue("" + sleepDuration, sleepDuration < 13 && sleepDuration >= 7);
    sleepDuration = RetryHelper.getSleepDuration(params, 2);
    assertTrue("" + sleepDuration, sleepDuration < 25 && sleepDuration >= 15);
    sleepDuration = RetryHelper.getSleepDuration(params, 3);
    assertTrue("" + sleepDuration, sleepDuration < 50 && sleepDuration >= 30);
    sleepDuration = RetryHelper.getSleepDuration(params, 4);
    assertTrue("" + sleepDuration, sleepDuration < 100 && sleepDuration >= 60);
    sleepDuration = RetryHelper.getSleepDuration(params, 5);
    assertTrue("" + sleepDuration, sleepDuration < 200 && sleepDuration >= 120);
    sleepDuration = RetryHelper.getSleepDuration(params, 6);
    assertTrue("" + sleepDuration, sleepDuration < 400 && sleepDuration >= 240);
    sleepDuration = RetryHelper.getSleepDuration(params, 7);
    assertTrue("" + sleepDuration, sleepDuration < 800 && sleepDuration >= 480);
    sleepDuration = RetryHelper.getSleepDuration(params, 8);
    assertTrue("" + sleepDuration, sleepDuration < 1600 && sleepDuration >= 960);
    sleepDuration = RetryHelper.getSleepDuration(params, 9);
    assertTrue("" + sleepDuration, sleepDuration < 3200 && sleepDuration >= 1920);
    sleepDuration = RetryHelper.getSleepDuration(params, 10);
    assertTrue("" + sleepDuration, sleepDuration < 6400 && sleepDuration >= 3840);
    sleepDuration = RetryHelper.getSleepDuration(params, 11);
    assertTrue("" + sleepDuration, sleepDuration < 12800 && sleepDuration >= 7680);
    sleepDuration = RetryHelper.getSleepDuration(params, 12);
    assertTrue("" + sleepDuration, sleepDuration < 25600 && sleepDuration >= 15360);
  }

  @Test
  public void testNestedUsage() {
    assertEquals((1 + 3) * 2, invokeNested(3, 2));
  }

  private int invokeNested(final int level, final int retries) {
    if (level < 0) {
      return 0;
    }
    return RetryHelper.runWithRetries(new Callable<Integer>() {
      @Override
      public Integer call() throws IOException {
        if (RetryHelper.getContext().getAttemptNumber() < retries) {
          throw new IOException();
        }
        assertEquals(retries, RetryHelper.getContext().getAttemptNumber());
        return invokeNested(level - 1, retries) + RetryHelper.getContext().getAttemptNumber();
      }
    });
  }

}
