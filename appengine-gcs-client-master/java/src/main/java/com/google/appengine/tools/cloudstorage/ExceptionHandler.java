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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Exception handling used by {@link RetryHelper}.
 *
 * For internal use only. User code cannot safely depend on this class.
 *
 */
public final class ExceptionHandler implements Serializable {

  private static final long serialVersionUID = -2460707015779532919L;

  private static final ExceptionHandler DEFAULT_INSTANCE =
      new Builder().retryOn(Exception.class).abortOn(RuntimeException.class).build();

  private final ImmutableSet<Class<? extends Exception>> retriableExceptions;
  private final ImmutableSet<Class<? extends Exception>> nonRetriableExceptions;
  private final Set<RetryInfo> retryInfos = Sets.newHashSet();

  /**
   * ExceptionHandler builder.
   */
  public static class Builder {

    private final ImmutableSet.Builder<Class<? extends Exception>> retriableExceptions =
        new ImmutableSet.Builder<>();
    private final ImmutableSet.Builder<Class<? extends Exception>> nonRetriableExceptions =
        new ImmutableSet.Builder<>();

    /**
     * @param exceptions retry should continue when such exceptions are thrown
     * @return the Builder for chaining
     */
    @SafeVarargs
    public final Builder retryOn(Class<? extends Exception>... exceptions) {
      for (Class<? extends Exception> exception : exceptions) {
        retriableExceptions.add(checkNotNull(exception));
      }
      return this;
    }

    /**
     * @param exceptions retry should abort when such exceptions are thrown
     * @return the Builder for chaining
     */
    @SafeVarargs
    public final Builder abortOn(Class<? extends Exception>... exceptions) {
      for (Class<? extends Exception> exception : exceptions) {
        nonRetriableExceptions.add(checkNotNull(exception));
      }
      return this;
    }

    /**
     * Returns a new instance of ExceptionHandler
     */
    public ExceptionHandler build() {
      return new ExceptionHandler(this);
    }
  }

  @VisibleForTesting
  static final class RetryInfo implements Serializable {

    private static final long serialVersionUID = -4264634837841455974L;
    private final Class<? extends Exception> exception;
    private final boolean retry;
    private final Set<RetryInfo> children = Sets.newHashSet();

    RetryInfo(Class<? extends Exception> exception, boolean retry) {
      this.exception = checkNotNull(exception);
      this.retry = retry;
    }

    @Override
    public int hashCode() {
      return exception.hashCode();
    }

    @Override
    public boolean equals(Object other) {
      if (other == this) {
        return true;
      }
      if (!(other instanceof RetryInfo)) {
        return false;
      }
      return ((RetryInfo) other).exception.equals(exception);
    }
  }

  private ExceptionHandler(Builder builder) {
    retriableExceptions = builder.retriableExceptions.build();
    nonRetriableExceptions = builder.nonRetriableExceptions.build();
    Preconditions.checkArgument(
        Sets.intersection(retriableExceptions, nonRetriableExceptions).isEmpty(),
        "Same exception was found in both retriable and non-retriable sets");
    for (Class<? extends Exception> exception : retriableExceptions) {
      addToRetryInfos(retryInfos, new RetryInfo(exception, true));
    }
    for (Class<? extends Exception> exception : nonRetriableExceptions) {
      addToRetryInfos(retryInfos,  new RetryInfo(exception, false));
    }
  }

  private static void addToRetryInfos(Set<RetryInfo> retryInfos, RetryInfo retryInfo) {
    for (RetryInfo current : retryInfos) {
      if (current.exception.isAssignableFrom(retryInfo.exception)) {
        addToRetryInfos(current.children, retryInfo);
        return;
      }
      if (retryInfo.exception.isAssignableFrom(current.exception)) {
        retryInfo.children.add(current);
      }
    }
    retryInfos.removeAll(retryInfo.children);
    retryInfos.add(retryInfo);
  }


  private static RetryInfo findMostSpecificRetryInfo(Set<RetryInfo> retryInfos,
      Class<? extends Exception> exception) {
    for (RetryInfo current : retryInfos) {
      if (current.exception.isAssignableFrom(exception)) {
        RetryInfo  match = findMostSpecificRetryInfo(current.children, exception);
        return match == null ? current : match;
      }
    }
    return null;
  }

  private static Method getCallableMethod(Class<?> clazz) {
    try {
      return clazz.getDeclaredMethod("call");
    } catch (NoSuchMethodException e) {
      return getCallableMethod(clazz.getSuperclass());
    } catch (SecurityException e) {
      throw new RuntimeException("Unexpected exception", e);
    }
  }

  void verifyCaller(Callable<?> callable) {
    Method callMethod = getCallableMethod(callable.getClass());
    for (Class<?> exceptionOrError : callMethod.getExceptionTypes()) {
      Preconditions.checkArgument(Exception.class.isAssignableFrom(exceptionOrError),
          "Callable method exceptions must be dervied from Exception");
      @SuppressWarnings("unchecked") Class<? extends Exception> exception =
          (Class<? extends Exception>) exceptionOrError;
      Preconditions.checkArgument(findMostSpecificRetryInfo(retryInfos, exception) != null,
          "Declared exception '" + exception + "' is not covered by exception handler");
    }
  }

  public ImmutableSet<Class<? extends Exception>> getRetriableExceptions() {
    return retriableExceptions;
  }

  public ImmutableSet<Class<? extends Exception>> getNonRetriableExceptions() {
    return nonRetriableExceptions;
  }

  boolean shouldRetry(Exception ex) {
    RetryInfo retryInfo = findMostSpecificRetryInfo(retryInfos, ex.getClass());
    return retryInfo == null ? false : retryInfo.retry;
  }

  /**
   * Returns an instance which retry any checked exception and abort on any runtime exception.
   */
  public static ExceptionHandler getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }
}
