/**
 * Copyright 2012 Internet2
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
 * @author mchyzer
 * $Id: GrouperReportException.java,v 1.1 2008-12-11 05:49:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;


/**
 * exception thrown from grouper report
 */
@SuppressWarnings("serial")
public class GrouperReportException extends RuntimeException {

  /** current result of grouper report */
  private String result;
  
  /**
   * current result of grouper report
   * @return the result
   */
  public String getResult() {
    return this.result;
  }

  /**
   * current result of grouper report
   * @param result1
   */
  public void setResult(String result1) {
    this.result = result1;
  }

  /**
   * 
   */
  public GrouperReportException() {
  }

  /**
   * @param message
   */
  public GrouperReportException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public GrouperReportException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public GrouperReportException(String message, Throwable cause) {
    super(message, cause);
  }

}
