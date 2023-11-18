/**
 */
package edu.internet2.middleware.grouper.util;

import java.io.InputStream;

/**
 * Receive the stream of an http response body.
 */
public interface GrouperHttpResponseBodyCallback {
  
  /**
   * Receiev the stream.
   * @param bodyInputStream is the body stream.
   */
  public void readBody(InputStream bodyInputStream);

}
