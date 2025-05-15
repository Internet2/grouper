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
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;


/**
 * run a manual web service
 */
public class WsParamsRestPoc {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {


    // grouper http client
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    
    String url = RestClientSettings.URL + "/json/" + RestClientSettings.VERSION + "/groups/test%3Atest1/members?wsLiteObjectType=WsRestGetMembersLiteRequest&subjectAttributeNames=PENNNAME";
    
    // assign the URL and method
    grouperHttpClient.assignUrl(url).assignGrouperHttpMethod(GrouperHttpMethod.get);
    // assign user and pass
    grouperHttpClient.assignUser(RestClientSettings.USER)
        .assignPassword(RestClientSettings.PASS);
    

    grouperHttpClient.executeRequest();
    
    String result = grouperHttpClient.getResponseBody();

    System.out.println(result);
  }

}
