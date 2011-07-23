package edu.internet2.middleware.grouper.ws.samples.rest.group;

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

import edu.internet2.middleware.grouper.ws.rest.WsRestResultProblem;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author mchyzer
 */
public class WsSampleGroupSaveRest100 implements WsSampleRest {

  /**
   * group save lite web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   * @param batchSize 
   */
  @SuppressWarnings("deprecation")
  public static void groupSave(WsSampleRestType wsSampleRestType, int batchSize) {

    try {
      HttpClient httpClient = new HttpClient();
      
      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      PostMethod method = new PostMethod(
          RestClientSettings.URL + "/" + RestClientSettings.VERSION  
            + "/groups");

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
      WsRestGroupSaveRequest groupSave = new WsRestGroupSaveRequest();
      
      long nanos = System.nanoTime();

      WsGroupToSave[] wsGroupToSaves = new WsGroupToSave[batchSize];
      for (int i=0;i<batchSize;i++) {
        WsGroupToSave wsGroupToSave = new WsGroupToSave();
        wsGroupToSave.setWsGroupLookup(new WsGroupLookup("aStem:whateverGroup_" + i + "_" + nanos, null));
        WsGroup wsGroup = new WsGroup();
        
        
        wsGroup.setDescription("desc_" + i + "_" + nanos);
        wsGroup.setDisplayExtension("disp" + i + "_" + nanos);
        wsGroup.setExtension("whateverGroup_" + i + "_" + nanos);
        wsGroup.setName("aStem:whateverGroup_" + i + "_" + nanos);
        wsGroupToSave.setWsGroup(wsGroup);
        wsGroupToSaves[i] = wsGroupToSave;
      }
      
      
      groupSave.setWsGroupToSaves(wsGroupToSaves);
      
      //get the xml / json / xhtml / paramString
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(groupSave);
      
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

      Object result = wsSampleRestType
        .getWsLiteResponseContentType().parseString(response);
      
      //see if problem
      if (result instanceof WsRestResultProblem) {
        throw new RuntimeException(((WsRestResultProblem)result).getResultMetadata().getResultMessage());
      }
      
      //convert to object (from xhtml, xml, json, etc)
      WsGroupSaveResults wsGroupSaveResults = (WsGroupSaveResults)result;
      
      String resultMessage = wsGroupSaveResults.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: successString: " + successString 
            + ", resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      System.out.println("Server version: " + wsGroupSaveResults.getResponseMetadata().getServerVersion()
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
    save100(5, 1);
    long nanoTime = System.nanoTime();
    save100(5, 20);
    //took 17.1 seconds against local mysql
    System.out.println("Took: " + ((System.nanoTime() - nanoTime)/1000000) + "ms");
  }

  /**
   * save 100 groups
   * @param loopSize 
   * @param batchSize 
   */
  public static void save100(int loopSize, int batchSize) {
    for (int i=0;i<loopSize;i++) {
      groupSave(WsSampleRestType.xml, batchSize);
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public void executeSample(WsSampleRestType wsSampleRestType) {
    groupSave(wsSampleRestType, 1);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#validType(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public boolean validType(WsSampleRestType wsSampleRestType) {
    //dont allow http params
    return !WsSampleRestType.http_xhtml.equals(wsSampleRestType);
  }
}
