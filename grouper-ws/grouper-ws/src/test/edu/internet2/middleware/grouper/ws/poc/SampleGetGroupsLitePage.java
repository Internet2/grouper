/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.poc;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
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
      // grouper http client
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      String url = RestClientSettings.URL + "/" + WsSampleRestType.json.getWsLiteResponseContentType().name()
            + "/v2_3_0"
            + "/subjects/test.subject.0/groups";

      //method.addParameter("wsLiteObjectType", "WsRestGetGroupsLiteRequest");
      //method.addParameter("pageSize", "2");
      //method.addParameter("pageNumber", "1");
      
      // assign the URL and method
      grouperHttpClient.assignUrl(url).assignGrouperHttpMethod(GrouperHttpMethod.post);
      // assign user and pass
      grouperHttpClient.assignUser(RestClientSettings.USER)
          .assignPassword(RestClientSettings.PASS);
      
      grouperHttpClient.addBodyParameter("wsLiteObjectType", "WsRestGetGroupsLiteRequest");
      grouperHttpClient.addBodyParameter("pageSize", "2");
      grouperHttpClient.addBodyParameter("pageNumber", "1");

      // execute
      grouperHttpClient.executeRequest();
      

      //check if success
      String successString = grouperHttpClient.getResponseHeadersLower().get("x-grouper-success");

      if (StringUtils.isBlank(successString)) {
        throw new RuntimeException("Web service did not even respond!");
      }
      boolean success = "T".equals(successString);

      // check result code
      String resultCode = grouperHttpClient.getResponseHeadersLower().get("x-grouper-resultcode");
      
      String response = grouperHttpClient.getResponseBody();

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
