package edu.internet2.middleware.grouper.ws.samples.rest;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.webservicesClient.util.ManualClientSettings;
import edu.internet2.middleware.grouper.ws.rest.WsRestResultProblem;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author mchyzer
 */
public class WsSampleAddMemberRest implements WsSampleRest {

  /**
   * add member simple web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  public static void addMemberLite(WsSampleRestType wsSampleRestType) {

    try {
      HttpClient httpClient = new HttpClient();
      
      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      PutMethod method = new PutMethod(
          RestClientSettings.URL + "/" + RestClientSettings.VERSION  
            + "/group/aStem%3AaGroup/members");

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(ManualClientSettings.USER, 
          ManualClientSettings.PASS);

      //no keep alive so response if easier to indent for tests
      method.setRequestHeader("Connection", "close");
      
      //e.g. localhost and 8093
      httpClient.getState()
          .setCredentials(new AuthScope(ManualClientSettings.HOST, ManualClientSettings.PORT), defaultcreds);

      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAddMemberRequest addMember = new WsRestAddMemberRequest();

      // set the act as id
      WsSubjectLookup actAsSubject = new WsSubjectLookup("GrouperSystem", null, null);
      addMember.setActAsSubjectLookup(actAsSubject);

      // just add, dont replace
      addMember.setReplaceAllExisting("F");

      // add two subjects to the group
      WsSubjectLookup[] subjectLookups = new WsSubjectLookup[2];
      subjectLookups[0] = new WsSubjectLookup("10021368", null, null);

      subjectLookups[1] = new WsSubjectLookup("10039438", null, null);

      addMember.setSubjectLookups(subjectLookups);
      
      //get the xml / json / xhtml / paramString
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(addMember);
      
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
      WsAddMemberResults wsAddMemberResults = (WsAddMemberResults)resultObject;
      
      String resultMessage = wsAddMemberResults.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      System.out.println("Server version: " + wsAddMemberResults.getResponseMetadata().getServerVersion()
          + ", result code: " + resultCode
          + ", result message: " + resultMessage );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * @param args
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    addMemberLite(WsSampleRestType.xhtml);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public void executeSample(WsSampleRestType wsSampleRestType) {
    addMemberLite(wsSampleRestType);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#validType(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public boolean validType(WsSampleRestType wsSampleRestType) {
    //dont allow http params
    return !WsSampleRestType.http_xhtml.equals(wsSampleRestType);
  }
}
