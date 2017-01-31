/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.poc;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;


/**
 *
 */
public class SampleGetGroupsLitePage {

  /**
   * @param args
   */
  public static void main(String[] args) {

    try {
      HttpClient httpClient = new HttpClient();
      
      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      PostMethod method = new PostMethod(
          RestClientSettings.URL + "/" + WsSampleRestType.json.getWsLiteResponseContentType().name()
            + "/v2_3_0"
            + "/subjects/test.subject.0/groups");

      method.addParameter("wsLiteObjectType", "WsRestGetGroupsLiteRequest");
      method.addParameter("pageSize", "2");
      method.addParameter("pageNumber", "1");
      
      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(RestClientSettings.USER, RestClientSettings.PASS);
      
      //no keep alive so response if easier to indent for tests
      method.setRequestHeader("Connection", "close");
      
      //e.g. localhost and 8093
      httpClient.getState()
          .setCredentials(new AuthScope(RestClientSettings.HOST, RestClientSettings.PORT), defaultcreds);

      httpClient.executeMethod(method);

      //make sure a request came back
      Header successHeader = method.getResponseHeader("X-Grouper-success");
      String successString = successHeader == null ? null : successHeader.getValue();
      if (StringUtils.isBlank(successString)) {
        throw new RuntimeException("Web service did not even respond!");
      }
      boolean success = "T".equals(successString);
      String resultCode = method.getResponseHeader("X-Grouper-resultCode").getValue();
      
      String response = RestClientSettings.responseBodyAsString(method);

      System.out.println(response);
      
//      //convert to object (from xhtml, xml, json, etc)
//      WsDeleteMemberLiteResult wsDeleteMemberLiteResult = (WsDeleteMemberLiteResult)WsSampleRestType.xml
//        .getWsLiteResponseContentType().parseString(response);
//      
//      String resultMessage = wsDeleteMemberLiteResult.getResultMetadata().getResultMessage();
//
//      // see if request worked or not
//      if (!success) {
//        throw new RuntimeException("Bad response from web service: resultCode: " + resultCode
//            + ", " + resultMessage);
//      }
//      
//      System.out.println("Server version: " + wsDeleteMemberLiteResult.getResponseMetadata().getServerVersion()
//          + ", result code: " + resultCode
//          + ", result message: " + resultMessage );

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }

}
