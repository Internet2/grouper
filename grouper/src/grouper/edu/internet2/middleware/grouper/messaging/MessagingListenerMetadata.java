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
package edu.internet2.middleware.grouper.messaging;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;

/**
 * metadata about the messaging listener
 */
public class MessagingListenerMetadata {

  /** name of consumer in config file */
  private String consumerName;
  
  /**
   * name of consumer in config file
   * @return consumer name
   */
  public String getConsumerName() {
    return consumerName;
  }

  /**
   * name of consumer in config file
   * @param consumerName1
   */
  public void setConsumerName(String consumerName1) {
    this.consumerName = consumerName1;
  }

  /** log for job */
  private Hib3GrouperLoaderLog hib3GrouperLoaderLog;

  /** if there is an exception in a record, put it here */
  private Throwable recordException = null;
  
  
  /** the problem text of the problem */
  private String recordProblemText = null;
  
  /**
   * if it had problem
   */
  private boolean hadProblem = false;
  
  /**
   * if it had problem
   * @return if had problem
   */
  public boolean isHadProblem() {
    return this.hadProblem;
  }

  /**
   * if it had problem
   * @param hadProblem1
   */
  public void setHadProblem(boolean hadProblem1) {
    this.hadProblem = hadProblem1;
  }

  /**
   * 
   * @return the loader log
   */
  public Hib3GrouperLoaderLog getHib3GrouperLoaderLog() {
    return hib3GrouperLoaderLog;
  }

  /**
   * 
   * @param hib3GrouperLoaderLog1
   */
  public void setHib3GrouperLoaderLog(Hib3GrouperLoaderLog hib3GrouperLoaderLog1) {
    this.hib3GrouperLoaderLog = hib3GrouperLoaderLog1;
  }

  /**
   * if there is an exception in a record, put it here
   * @return exception
   */
  public Throwable getRecordException() {
    return this.recordException;
  }

  /**
   * if there is an exception in a record, put it here
   * @param recordException1
   */
  public void setRecordException(Throwable recordException1) {
    this.recordException = recordException1;
  }

  /**
   * id where record exception took place
   */
  private String recordExceptionId;
  
  /**
   * the index of the record where the exception took place
   * @return sequence
   */
  public String getRecordExceptionId() {
    return this.recordExceptionId;
  }

  /**
   * the index of the record where the exception took place
   * @param recordExceptionSequence1
   */
  public void setRecordExceptionId(String recordExceptionSequence1) {
    this.recordExceptionId = recordExceptionSequence1;
  }

  /**
   * the problem text of the problem
   * @return text
   */
  public String getRecordProblemText() {
    return this.recordProblemText;
  }

  /**
   * the problem text of the problem
   * @param recordProblemText1
   */
  public void setRecordProblemText(String recordProblemText1) {
    this.recordProblemText = recordProblemText1;
  }
  
  /**
   * register a problem with the run
   * @param throwable
   * @param errorMessage
   * @param problemMessageId the sequence with the problem
   */
  public void registerProblem(Throwable throwable, String errorMessage, String problemMessageId) {
    this.setRecordException(throwable);
    this.setRecordProblemText(errorMessage);
    this.setHadProblem(true);
    this.setRecordExceptionId(problemMessageId);
  }
  
}
