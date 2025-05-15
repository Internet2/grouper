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
/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;


/**
 *
 */
public class SslPoc {

  /**
   * @param args
   * @throws Exception 
   */
  @SuppressWarnings("deprecation")
  public static void main(String[] args) throws Exception {

    // grouper http client
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    String url = "https://cosign-test-1.net.isc.upenn.edu/~jorj/file1.html";
    
    grouperHttpClient.assignUrl(url).assignGrouperHttpMethod(GrouperHttpMethod.get);
    grouperHttpClient.assignTrust(true);

    // execute
    grouperHttpClient.executeRequest();
    
    int resultCode = grouperHttpClient.getResponseCode();
    
    System.out.println("resultCode: " + resultCode);
  }

}
