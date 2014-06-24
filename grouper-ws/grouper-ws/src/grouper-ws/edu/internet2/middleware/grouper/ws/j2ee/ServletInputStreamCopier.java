/*******************************************************************************
 * Copyright 2014 Internet2
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.j2ee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/**
 * copy bytes to a buffer
 */
public class ServletInputStreamCopier extends ServletInputStream {

  /**
   * output stream
   */
  private InputStream inputStream;

  /** keep a copy
   * 
   */
  private ByteArrayOutputStream copy;

  /**
   * constructor
   * @param inputStream1
   */
  public ServletInputStreamCopier(InputStream inputStream1) {
    this.inputStream = inputStream1;
    this.copy = new ByteArrayOutputStream(1024);
  }

  /**
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException {
    int result = this.inputStream.read();
    this.copy.write(result);
    return result;
  }

  /**
   * get the copy
   * @return bytes
   */
  public byte[] getCopy() {
    return this.copy.toByteArray();
  }

}
