package com.intellij.youtrack.util;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

/**
 * @author Konstantin Bulenkov
 */
public abstract class CancellableConnection implements Callable<Exception> {

  @Nullable
  @Override
  public final Exception call() {
    try {
      doTest();
      return null;
    }
    catch (Exception e) {
      return e;
    }
  }

  protected abstract void doTest() throws Exception;

  public abstract void cancel();
}

