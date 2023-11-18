/*******************************************************************************
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.Credentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.auth.AuthScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.GetMethod;


/**
 * run a manual web service
 */
public class WsParamsRestPoc {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {

    HttpClient httpClient = new HttpClient();
    HttpMethod httpMethod = new GetMethod(RestClientSettings.URL + "/json/" + RestClientSettings.VERSION + "/groups/test%3Atest1/members?wsLiteObjectType=WsRestGetMembersLiteRequest&subjectAttributeNames=PENNNAME");
    Credentials defaultcreds = new UsernamePasswordCredentials(RestClientSettings.USER, 
        RestClientSettings.PASS);

    httpClient.getState()
      .setCredentials(new AuthScope(RestClientSettings.HOST, RestClientSettings.PORT), defaultcreds);
    httpClient.getParams().setAuthenticationPreemptive(true);
    httpClient.executeMethod(httpMethod);
    String result = httpMethod.getResponseBodyAsString();
    System.out.println(result);
  }

}
