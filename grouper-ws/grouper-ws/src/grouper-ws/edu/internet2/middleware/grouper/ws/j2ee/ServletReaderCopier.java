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
import java.io.OutputStreamWriter;
import java.io.Reader;

/**
 * copy bytes to a buffer
 */
public class ServletReaderCopier extends Reader {

  /**
   * reader
   */
  private Reader reader;

  /** keep a copy
   * 
   */
  private ByteArrayOutputStream copy;

  /**
   * bridge from chars to bytes
   */
  private OutputStreamWriter outputStreamWriter;
  
  /**
   * constructor
   * @param reader1
   */
  public ServletReaderCopier(Reader reader1) {
    this.reader = reader1;
    this.copy = new ByteArrayOutputStream(1024);
    this.outputStreamWriter = new OutputStreamWriter(this.copy);
  }

  /**
   * get the copy
   * @return bytes
   */
  public byte[] getCopy() {
    try {
      this.outputStreamWriter.flush();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return this.copy.toByteArray();
  }

  /**
   * @see java.io.Reader#close()
   */
  @Override
  public void close() throws IOException {
    this.reader.close();
  }

  /**
   * @see java.io.Reader#read(char[], int, int)
   */
  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    int length = this.reader.read(cbuf, off, len);
    if (length > 0) {
      this.outputStreamWriter.write(cbuf, off, length);
    }
    return length;
  }

}
