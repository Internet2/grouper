package edu.internet2.middleware.grouper.ws.samples.rest.attribute;

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
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author mchyzer
 */
public class WsSampleAssignAttributesWithValueRest implements WsSampleRest {

  /**
   * attribute assignments lite web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  public static void assignAttributesWithValue(WsSampleRestType wsSampleRestType) {

    try {
      HttpClient httpClient = new HttpClient();
      
      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
      String url = RestClientSettings.URL + "/" + RestClientSettings.VERSION  
        + "/attributeAssignments";
      PostMethod method = new PostMethod(
          url);

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
      WsRestAssignAttributesRequest assignAttributes = new WsRestAssignAttributesRequest();

      assignAttributes.setAttributeAssignType("group");

      WsGroupLookup wsGroupLookup = new WsGroupLookup("test:groupTestAttrAssign", null);
      assignAttributes.setWsOwnerGroupLookups(new WsGroupLookup[]{wsGroupLookup});
      assignAttributes.setAttributeAssignOperation("assign_attr");
      assignAttributes.setAttributeAssignValueOperation("add_value");
      WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
      wsAttributeAssignValue.setValueSystem("63");
      assignAttributes.setValues(new WsAttributeAssignValue[]{wsAttributeAssignValue});

      WsAttributeDefNameLookup wsAttributeDefNameLookup = new WsAttributeDefNameLookup();
      wsAttributeDefNameLookup.setUuid("");
      wsAttributeDefNameLookup.setName("test:testAttributeAssignDefName");
      assignAttributes.setWsAttributeDefNameLookups(new WsAttributeDefNameLookup[]{wsAttributeDefNameLookup});
      
      //get the xml / json / xhtml / paramString
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(assignAttributes);
      
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
      WsAssignAttributesResults wsAssignAttributesResults = (WsAssignAttributesResults)result;
      
      String resultMessage = wsAssignAttributesResults.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: successString: " + successString + ", resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      System.out.println("Server version: " + wsAssignAttributesResults.getResponseMetadata().getServerVersion()
          + ", result code: " + resultCode
          + ", result message: " + resultMessage );

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    assignAttributesWithValue(WsSampleRestType.xml);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public void executeSample(WsSampleRestType wsSampleRestType) {
    assignAttributesWithValue(wsSampleRestType);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#validType(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public boolean validType(WsSampleRestType wsSampleRestType) {
    //dont allow http params
    return !WsSampleRestType.http_xhtml.equals(wsSampleRestType);
  }
}
