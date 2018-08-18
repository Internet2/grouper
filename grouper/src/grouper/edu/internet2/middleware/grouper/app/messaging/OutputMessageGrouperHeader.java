/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.app.messaging;

public class OutputMessageGrouperHeader {
  
  private String messageVersion;
  private String timestampInput;
  private String timestampOutput;
  private String type;
  private String endpoint;
  private String messageInputUuid;
  private Integer httpResponseCode;
  private String httpHeaderXGrouperResultCode;
  private String httpHeaderXGrouperSuccess;
  private String httpHeaderXGrouperResultCode2;
  
  public String getMessageVersion() {
    return messageVersion;
  }
  
  public void setMessageVersion(String messageVersion) {
    this.messageVersion = messageVersion;
  }
  
  public String getTimestampInput() {
    return timestampInput;
  }
  
  public void setTimestampInput(String timestampInput) {
    this.timestampInput = timestampInput;
  }
  
  public String getTimestampOutput() {
    return timestampOutput;
  }
  
  public void setTimestampOutput(String timestampOutput) {
    this.timestampOutput = timestampOutput;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getEndpoint() {
    return endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
  
  public String getMessageInputUuid() {
    return messageInputUuid;
  }
  
  public void setMessageInputUuid(String messageInputUuid) {
    this.messageInputUuid = messageInputUuid;
  }
  
  public Integer getHttpResponseCode() {
    return httpResponseCode;
  }
  
  public void setHttpResponseCode(Integer httpResponseCode) {
    this.httpResponseCode = httpResponseCode;
  }
  
  public String getHttpHeaderXGrouperResultCode() {
    return httpHeaderXGrouperResultCode;
  }
  
  public void setHttpHeaderXGrouperResultCode(String httpHeaderXGrouperResultCode) {
    this.httpHeaderXGrouperResultCode = httpHeaderXGrouperResultCode;
  }
  
  public String getHttpHeaderXGrouperSuccess() {
    return httpHeaderXGrouperSuccess;
  }
  
  public void setHttpHeaderXGrouperSuccess(String httpHeaderXGrouperSuccess) {
    this.httpHeaderXGrouperSuccess = httpHeaderXGrouperSuccess;
  }
  
  public String getHttpHeaderXGrouperResultCode2() {
    return httpHeaderXGrouperResultCode2;
  }
  
  public void setHttpHeaderXGrouperResultCode2(String httpHeaderXGrouperResultCode2) {
    this.httpHeaderXGrouperResultCode2 = httpHeaderXGrouperResultCode2;
  }

}
