/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

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
    HttpMethod httpMethod = new GetMethod("http://localhost:8091/grouperWs/servicesRest/v1_4_000/groups/test%3Atest1/members?wsLiteObjectType=WsRestGetMembersLiteRequest&subjectAttributeNames=PENNNAME");
    Credentials defaultcreds = new UsernamePasswordCredentials("mchyzer", 
    "");

    httpClient.getState()
      .setCredentials(new AuthScope("localhost", 8091), defaultcreds);
    httpClient.getParams().setAuthenticationPreemptive(true);
    httpClient.executeMethod(httpMethod);
    String result = httpMethod.getResponseBodyAsString();
    System.out.println(result);
  }

}
