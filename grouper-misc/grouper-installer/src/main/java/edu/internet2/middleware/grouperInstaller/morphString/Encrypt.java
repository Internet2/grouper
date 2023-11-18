/**
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
 */
/*
 * @author mchyzer $Id: Encrypt.java,v 1.3 2008-12-02 05:14:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouperInstaller.morphString;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

/**
 * from sh or bat file to encrypt a string
 */
public class Encrypt {

  /**
   * @param in stream to be used (e.g. System.in)
   *@param prompt The prompt to display to the user.
   *@return The password as entered by the user.
   * @throws IOException 
   */

  public static final char[] password(InputStream in, String prompt) throws IOException {
    MaskingThread maskingthread = new MaskingThread(prompt);

    Thread thread = new Thread(maskingthread);
    thread.start();

    char[] lineBuffer;
    char[] buf;

    buf = lineBuffer = new char[128];

    int room = buf.length;
    int offset = 0;
    int c;

    loop: while (true) {
      switch (c = in.read()) {
        case -1:
        case '\n':
          break loop;

        case '\r':
          int c2 = in.read();
          if ((c2 != '\n') && (c2 != -1)) {
            if (!(in instanceof PushbackInputStream)) {
              in = new PushbackInputStream(in);
            }
            ((PushbackInputStream) in).unread(c2);
          } else {
            break loop;
          }

        default:
          if (--room < 0) {
            buf = new char[offset + 128];
            room = buf.length - offset - 1;
            System.arraycopy(lineBuffer, 0, buf, 0, offset);
            Arrays.fill(lineBuffer, ' ');
            lineBuffer = buf;
          }
          buf[offset++] = (char) c;
          break;
      }
    }
    maskingthread.stopMasking();
    if (offset == 0) {
      return null;
    }
    char[] ret = new char[offset];
    System.arraycopy(buf, 0, ret, 0, offset);
    Arrays.fill(buf, ' ');
    return ret;
  }

  /**
   * thread to mask input
   */
  static class MaskingThread extends Thread {

    /** stop */
    private volatile boolean stop;

    /** echo char, this doesnt work correctly, so make a space so people dont notice...  
     * prints out too many */
    private char echochar = ' ';

    /**
     *@param prompt The prompt displayed to the user
     */
    public MaskingThread(String prompt) {
      System.out.print(prompt);
    }

    /**
     * Begin masking until asked to stop.
     */
    @Override
    public void run() {

      int priority = Thread.currentThread().getPriority();
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

      try {
        this.stop = true;
        while (this.stop) {
          System.out.print("\010" + this.echochar);
          try {
            // attempt masking at this rate
            Thread.sleep(1);
          } catch (InterruptedException iex) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      } finally { // restore the original priority
        Thread.currentThread().setPriority(priority);
      }
    }

    /**
     * Instruct the thread to stop masking.
     */
    public void stopMasking() {
      this.stop = false;
    }
  }
}
