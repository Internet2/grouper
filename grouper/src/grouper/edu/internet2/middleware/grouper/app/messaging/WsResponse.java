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

public class WsResponse {
  
  private int httpStatusCode;
  private String resultCode;
  private String success;
  private String resultCode2;
  private String body;
  
  public int getHttpStatusCode() {
    return httpStatusCode;
  }
  
  public void setHttpStatusCode(int httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
  }
  
  public String getResultCode() {
    return resultCode;
  }
  
  public void setResultCode(String resultCode) {
    this.resultCode = resultCode;
  }
  
  public String getSuccess() {
    return success;
  }
  
  public void setSuccess(String success) {
    this.success = success;
  }
  
  public String getResultCode2() {
    return resultCode2;
  }
  
  public void setResultCode2(String resultCode2) {
    this.resultCode2 = resultCode2;
  }
  
  public String getBody() {
    return body;
  }
  
  public void setBody(String body) {
    this.body = body;
  }

}
