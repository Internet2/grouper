package edu.internet2.middleware.grouper.ws.samples.rest.member;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import edu.internet2.middleware.subject.Subject;

/**
 * @author mchyzer
 */
public class WsSampleAddDeleteMemberRestLite implements WsSampleRest {

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsSampleAddDeleteMemberRestLite.class);

  /**
   * add a member with the api
   * @throws Exception
   */
  public static void addMemberApi() throws Exception {
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(SubjectFinder.findById("GrouperSystem", true));
      
      Group group = GroupFinder.findByName(grouperSession, "aStem:aGroup1", true);
      
      Subject me = SubjectFinder.findById("10021368", true);
      
      group.addMember(me, false);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * add a member with the api
   * @throws Exception
   */
  public static void deleteMemberApi() throws Exception {
    GrouperSession grouperSession = null;
    try {
      grouperSession = GrouperSession.start(SubjectFinder.findById("GrouperSystem", true));
      
      Group group = GroupFinder.findByName(grouperSession, "aStem:aGroup1", true);
      
      Subject me = SubjectFinder.findById("10021368", true);
      
      if (group.hasImmediateMember(me)) {
        group.deleteMember(me);
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * add member lite web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  @SuppressWarnings("deprecation")
  public static void addMemberLite(WsSampleRestType wsSampleRestType) {

    try {
      HttpClient httpClient = new HttpClient();
      
      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      PutMethod method = new PutMethod(
          RestClientSettings.URL + "/" + wsSampleRestType.getWsLiteResponseContentType().name()
            + "/" + RestClientSettings.VERSION  
            + "/groups/aStem%3AaGroup1/members/10021368");

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(RestClientSettings.USER, 
          RestClientSettings.PASS);
      
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

      //convert to object (from xhtml, xml, json, etc)
      WsAddMemberLiteResult wsAddMemberLiteResult = (WsAddMemberLiteResult)wsSampleRestType
        .getWsLiteResponseContentType().parseString(response);
      
      String resultMessage = wsAddMemberLiteResult.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      LOG.error("Add Server version: " + wsAddMemberLiteResult.getResponseMetadata().getServerVersion()
          + ", result code: " + resultCode
          + ", result message: " + resultMessage );

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * delete member lite web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  @SuppressWarnings("deprecation")
  public static void deleteMemberLite(WsSampleRestType wsSampleRestType) {

    try {
      HttpClient httpClient = new HttpClient();
      
      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      DeleteMethod method = new DeleteMethod(
          RestClientSettings.URL + "/" + wsSampleRestType.getWsLiteResponseContentType().name()
            + "/" + RestClientSettings.VERSION  
            + "/groups/aStem%3AaGroup1/members/10021368");

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(RestClientSettings.USER, 
          RestClientSettings.PASS);
      
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

      //convert to object (from xhtml, xml, json, etc)
      WsDeleteMemberLiteResult wsDeleteMemberLiteResult = (WsDeleteMemberLiteResult)wsSampleRestType
        .getWsLiteResponseContentType().parseString(response);
      
      String resultMessage = wsDeleteMemberLiteResult.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      LOG.error("Delete: Server version: " + wsDeleteMemberLiteResult.getResponseMetadata().getServerVersion()
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
    for (int i=0;i<1000000;i++) {
      if (i%100 == 99) {
        try {
          System.gc();
          Thread.sleep(2000);
          LOG.error("Done " + i);
          LOG.error("Memory: " + (Runtime.getRuntime().totalMemory() -
      Runtime.getRuntime().freeMemory()));
          Thread.sleep(2000);
        } catch (InterruptedException ie) {
          //
        }
      }
      try {
        //addMemberLite(WsSampleRestType.xml);
        //deleteMemberLite(WsSampleRestType.xml);
        
        addMemberApi();
        deleteMemberApi();
        
      } catch (Exception e) {
        LOG.error("Failure " + i, e);
      }
    }
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
    //allow all
    return true;
  }
}
