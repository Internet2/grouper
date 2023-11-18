package edu.internet2.middleware.grouper.ws.samples.rest.audit;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;

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
     HttpClient httpClient = new HttpClient();
     
     DefaultHttpParams.getDefaultParams().setParameter(
         HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

     //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
     PostMethod method = new PostMethod(
         RestClientSettings.URL + "/" + RestClientSettings.VERSION  
           + "/audits");
     
     httpClient.getParams().setAuthenticationPreemptive(true);
     Credentials defaultcreds = new UsernamePasswordCredentials(RestClientSettings.USER, 
         RestClientSettings.PASS);
     
     //no keep alive so response if easier to indent for tests
     method.setRequestHeader("Connection", "close");
     
     //e.g. localhost and 8093
     httpClient.getState()
         .setCredentials(new AuthScope(RestClientSettings.HOST, RestClientSettings.PORT), defaultcreds);

     WsRestGetAuditEntriesLiteRequest getAuditEntriesLite = new WsRestGetAuditEntriesLiteRequest();
     
     getAuditEntriesLite.setAuditType("group");
     getAuditEntriesLite.setAuditActionId("addGroup");
     
     //get the xml / json / xhtml / paramString
     String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(getAuditEntriesLite);
     
     //make sure right content type is in request (e.g. application/xhtml+xml
     String contentType = wsSampleRestType.getWsLiteRequestContentType().getContentType();
     
     method.setRequestEntity(new StringRequestEntity(requestDocument, contentType, "UTF-8"));
     
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
