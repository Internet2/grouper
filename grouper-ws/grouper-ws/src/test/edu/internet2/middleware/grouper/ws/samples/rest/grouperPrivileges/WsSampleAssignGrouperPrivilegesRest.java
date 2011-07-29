package edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges;

import java.lang.reflect.Array;

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
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author mchyzer
 */
public class WsSampleAssignGrouperPrivilegesRest implements WsSampleRest {

  /**
   * get grouper privileges web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  @SuppressWarnings("deprecation")
  public static void assignGrouperPrivileges(WsSampleRestType wsSampleRestType) {

    try {
      
      HttpClient httpClient = new HttpClient();
      
      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      httpClient.getParams().setParameter("http.socket.timeout", new Integer(30000));

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      PostMethod method = new PostMethod(
          RestClientSettings.URL + "/" + RestClientSettings.VERSION  
            + "/grouperPrivileges");

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(RestClientSettings.USER, 
          RestClientSettings.PASS);
      
      //no keep alive so response if easier to indent for tests
      method.setRequestHeader("Connection", "close");
      
      //e.g. localhost and 8093
      httpClient.getState()
          .setCredentials(new AuthScope(RestClientSettings.HOST, RestClientSettings.PORT), defaultcreds);

      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAssignGrouperPrivilegesRequest wsRestAssignGrouperPrivilegesRequest 
        = new WsRestAssignGrouperPrivilegesRequest();
      
      wsRestAssignGrouperPrivilegesRequest.setPrivilegeType("access");
      wsRestAssignGrouperPrivilegesRequest.setPrivilegeNames(new String[]{"update", "read"});
      
      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");
      wsRestAssignGrouperPrivilegesRequest.setActAsSubjectLookup(actAsSubject);

      // just add, dont replace
      wsRestAssignGrouperPrivilegesRequest.setReplaceAllExisting("F");

      WsGroupLookup wsGroupLookup = WsGroupLookup.class.newInstance();
      wsGroupLookup.setGroupName("aStem:aGroup");
      wsRestAssignGrouperPrivilegesRequest.setWsGroupLookup(wsGroupLookup);
      
      //version, e.g. v1_3_000
      wsRestAssignGrouperPrivilegesRequest.setClientVersion(RestClientSettings.VERSION);

      // add two subjects to the group
      WsSubjectLookup[] subjectLookups = (WsSubjectLookup[]) Array.newInstance(WsSubjectLookup.class,
              2);
      subjectLookups[0] = WsSubjectLookup.class.newInstance();
      subjectLookups[0].setSubjectId("test.subject.0");
      subjectLookups[0].setSubjectSourceId("jdbc");
      subjectLookups[0].setSubjectIdentifier("");

      subjectLookups[1] = WsSubjectLookup.class.newInstance();
      subjectLookups[1].setSubjectId("");
      subjectLookups[1].setSubjectSourceId("");
      subjectLookups[1].setSubjectIdentifier("id.test.subject.1");

      wsRestAssignGrouperPrivilegesRequest.setWsSubjectLookups(subjectLookups);

      wsRestAssignGrouperPrivilegesRequest.setAllowed("T");

      //get the xml / json / xhtml / paramString
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(wsRestAssignGrouperPrivilegesRequest);
      
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

      //convert to object (from xhtml, xml, json, etc)
      WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = (WsAssignGrouperPrivilegesResults)wsSampleRestType
        .getWsLiteResponseContentType().parseString(response);
      
      String resultMessage = wsAssignGrouperPrivilegesResults.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      System.out.println(ToStringBuilder.reflectionToString(
          wsAssignGrouperPrivilegesResults, ToStringStyle.MULTI_LINE_STYLE));
      System.out.println(ToStringBuilder.reflectionToString(
          wsAssignGrouperPrivilegesResults.getResultMetadata(),
          ToStringStyle.MULTI_LINE_STYLE));

      if (wsAssignGrouperPrivilegesResults != null) {
        for (WsAssignGrouperPrivilegesResult wsAssignGrouperPrivilegesResult : wsAssignGrouperPrivilegesResults.getResults()) {
            System.out.println(ToStringBuilder.reflectionToString(
                wsAssignGrouperPrivilegesResult, ToStringStyle.MULTI_LINE_STYLE));
            System.out.println(ToStringBuilder.reflectionToString(
                wsAssignGrouperPrivilegesResult.getResultMetadata(),
                    ToStringStyle.MULTI_LINE_STYLE));
        }
      }

      if (!StringUtils.equals("T", 
          wsAssignGrouperPrivilegesResults.getResultMetadata().getSuccess())) {
        throw new RuntimeException("didnt get success! ");
      }

      System.out.println("Server version: " + wsAssignGrouperPrivilegesResults.getResponseMetadata().getServerVersion()
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
    RestClientSettings.resetData();
    assignGrouperPrivileges(WsSampleRestType.xhtml);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public void executeSample(WsSampleRestType wsSampleRestType) {
    assignGrouperPrivileges(wsSampleRestType);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#validType(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public boolean validType(WsSampleRestType wsSampleRestType) {
    //dont allow http params
    return !WsSampleRestType.http_xhtml.equals(wsSampleRestType);
  }
}
