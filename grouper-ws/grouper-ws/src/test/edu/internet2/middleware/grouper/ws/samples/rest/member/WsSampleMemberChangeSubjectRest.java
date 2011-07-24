package edu.internet2.middleware.grouper.ws.samples.rest.member;

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

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubject;
import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author mchyzer
 */
public class WsSampleMemberChangeSubjectRest implements WsSampleRest {

  /**
   * member change subject web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  @SuppressWarnings("deprecation")
  public static void memberChangeSubject(WsSampleRestType wsSampleRestType) {

    try {
      
      HttpClient httpClient = new HttpClient();
      
      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
      
      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      PostMethod method = new PostMethod(
          RestClientSettings.URL + "/" + RestClientSettings.VERSION  
            + "/members");

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
      WsRestMemberChangeSubjectRequest memberChangeSubject = new WsRestMemberChangeSubjectRequest();

      WsMemberChangeSubject[] wsMemberChangeSubjects = new WsMemberChangeSubject[2];
      
      memberChangeSubject.setWsMemberChangeSubjects(wsMemberChangeSubjects);

      // set the act as id
      wsMemberChangeSubjects[0] = new WsMemberChangeSubject();
      wsMemberChangeSubjects[0].setOldSubjectLookup(new WsSubjectLookup(null, null, "id.test.subject.0"));
      wsMemberChangeSubjects[0].setNewSubjectLookup(new WsSubjectLookup("test.subject.1", null, null));
      wsMemberChangeSubjects[1] = new WsMemberChangeSubject();
      wsMemberChangeSubjects[1].setOldSubjectLookup(new WsSubjectLookup(null, null, "id.test.subject.2"));
      wsMemberChangeSubjects[1].setNewSubjectLookup(new WsSubjectLookup("test.subject.3", null, null));
      wsMemberChangeSubjects[1].setDeleteOldMember("F");

      memberChangeSubject.setActAsSubjectLookup(new WsSubjectLookup("GrouperSystem", null, null));

      //get the xml / json / xhtml / paramString
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(memberChangeSubject);
      
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
      WsMemberChangeSubjectResults wsMemberChangeSubjectResults = (WsMemberChangeSubjectResults)wsSampleRestType
        .getWsLiteResponseContentType().parseString(response);
      
      String resultMessage = wsMemberChangeSubjectResults.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      System.out.println("Server version: " + wsMemberChangeSubjectResults.getResponseMetadata().getServerVersion()
          + ", result code: " + resultCode
          + ", result message: " + resultMessage );
      
      //lets make sure the old member was deleted
      try {
        GrouperDAOFactory.getFactory().getMember().findBySubject(SubjectFinder.findById("test.subject.0"));
        throw new RuntimeException("Should not find renamed member: test.subject.0!");
      } catch (MemberNotFoundException mnfe) {
        //good
      }
      
      //make sure old member was not deleted
      try {
        GrouperDAOFactory.getFactory().getMember().findBySubject(SubjectFinder.findById("test.subject.2"));
      } catch (MemberNotFoundException mnfe) {
        throw new RuntimeException("Should find renamed member: test.subject.2!");
      }

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
    
    memberChangeSubject(WsSampleRestType.xhtml);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public void executeSample(WsSampleRestType wsSampleRestType) {
    memberChangeSubject(wsSampleRestType);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#validType(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public boolean validType(WsSampleRestType wsSampleRestType) {
    //dont allow http params
    return !WsSampleRestType.http_xhtml.equals(wsSampleRestType);
  }
}
