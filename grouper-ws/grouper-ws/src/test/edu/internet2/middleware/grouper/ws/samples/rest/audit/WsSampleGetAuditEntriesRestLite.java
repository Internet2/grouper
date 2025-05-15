package edu.internet2.middleware.grouper.ws.samples.rest.audit;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAuditEntriesResults;
import edu.internet2.middleware.grouper.ws.rest.WsRestResultProblem;
import edu.internet2.middleware.grouper.ws.rest.audit.WsRestGetAuditEntriesLiteRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * 
 * @author vsachdeva
 *
 */
public class WsSampleGetAuditEntriesRestLite implements WsSampleRest {
  
  /**
  *
  * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
  */
 public static void getAuditEntriesLite(WsSampleRestType wsSampleRestType) {
   
   try {
     // grouper http client
     GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

     //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
     String url = RestClientSettings.URL + "/" + RestClientSettings.VERSION  
           + "/audits";
     
     // assign the URL and method
     grouperHttpClient.assignUrl(url).assignGrouperHttpMethod(GrouperHttpMethod.post);
     // assign user and pass
     grouperHttpClient.assignUser(RestClientSettings.USER)
         .assignPassword(RestClientSettings.PASS);

     WsRestGetAuditEntriesLiteRequest getAuditEntriesLite = new WsRestGetAuditEntriesLiteRequest();
     
     getAuditEntriesLite.setAuditType("group");
     getAuditEntriesLite.setAuditActionId("addGroup");
     
     //get the xml / json / xhtml / paramString
     String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(getAuditEntriesLite);
     
     //make sure right content type is in request (e.g. application/xhtml+xml
     String contentType = wsSampleRestType.getWsLiteRequestContentType().getContentType();
     
     // assign body
     grouperHttpClient.assignBody(requestDocument);

     // content type
     grouperHttpClient.addHeader("Content-Type", contentType);
     
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

     Object resultObject = wsSampleRestType.getWsLiteResponseContentType().parseString(response);
   
     //see if problem
     if (resultObject instanceof WsRestResultProblem) {
       throw new RuntimeException(((WsRestResultProblem)resultObject).getResultMetadata().getResultMessage());
     }

     //convert to object (from xhtml, xml, json, etc)
     WsGetAuditEntriesResults wsGetAuditEntriesResults = (WsGetAuditEntriesResults)resultObject;
     
     String resultMessage = wsGetAuditEntriesResults.getResultMetadata().getResultMessage();

     // see if request worked or not
     if (!success) {
       throw new RuntimeException("Bad response from web service: resultCode: " + resultCode
           + ", " + resultMessage);
     }
     
     System.out.println("Server version: " + wsGetAuditEntriesResults.getResponseMetadata().getServerVersion()
         + ", result code: " + resultCode
         + ", result message: " + resultMessage );

   } catch (Exception e) {
     throw new RuntimeException(e);
   }
   
 }

  @Override
  public void executeSample(WsSampleRestType wsSampleRestType) {
    getAuditEntriesLite(wsSampleRestType);
  }

  @Override
  public boolean validType(WsSampleRestType wsSampleRestType) {
    return true;
  }
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    getAuditEntriesLite(WsSampleRestType.json);
  }

}
