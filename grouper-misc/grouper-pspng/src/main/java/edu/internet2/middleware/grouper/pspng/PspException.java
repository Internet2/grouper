package edu.internet2.middleware.grouper.pspng;

/*******************************************************************************
 * Copyright 2015 Internet2
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

/**
 * This is the common caught-exception class used within PSP-NG. You can 
 * generally assume that some good information has been logged when a PspException
 * is thrown, so all you need to worry about is logging the local context surrounding
 * the call that caused the exception (and usually not the stack trace).
 * 
 * @author bert
 *
 */
public class PspException extends Exception {
  private static final long serialVersionUID = 1L;

  public PspException() {
    super();
  }

  public PspException(String messageFormat, Throwable cause, Object... messageArgs) {
    super(String.format(messageFormat, messageArgs), cause);
  }

  public PspException(String messageFormat, Object... messageArgs) {
    super(String.format(messageFormat, messageArgs));
  }

  public PspException(Throwable cause) {
    super(cause);
  }

}
