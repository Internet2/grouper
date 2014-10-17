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
package edu.internet2.middleware.grouperAwsChangelog;


/**
 * message from sqs
 */
public class GrouperSqsMessage {

  /**
   * receipt id used for deleting
   */
  private String receiptHandle;
  
  /**
   * receipt id used for deleting
   * @return the receiptId
   */
  public String getReceiptHandle() {
    return this.receiptHandle;
  }
  
  /**
   * receipt id used for deleting
   * @param receiptId1 the receiptId to set
   */
  public void setReceiptHandle(String receiptId1) {
    this.receiptHandle = receiptId1;
  }
  
  /**
   * message body, might be a json string
   */
  private String messageBody;
  
  /**
   * message body, might be a json string
   * @return the messageBody
   */
  public String getMessageBody() {
    return this.messageBody;
  }
  
  /**
   * message body, might be a json string
   * @param messageBody1 the messageBody to set
   */
  public void setMessageBody(String messageBody1) {
    this.messageBody = messageBody1;
  }
  
}
