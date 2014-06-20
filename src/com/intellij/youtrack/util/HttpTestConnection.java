package com.intellij.youtrack.util;

import org.apache.commons.httpclient.HttpMethod;

/**
 * @author Konstantin Bulenkov
 */
public abstract class HttpTestConnection<T extends HttpMethod> extends CancellableConnection {

  protected T myMethod;

  public HttpTestConnection(T method) {
    myMethod = method;
  }

  @Override
  protected void doTest() throws Exception {
    doTest(myMethod);
  }

  @Override
  public void cancel() {
    myMethod.abort();
  }

  protected abstract void doTest(T method) throws Exception;
}

