/**
 * 
 */
package edu.internet2.middleware.grouper.ws.samples.rest.attribute;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRestResultProblem;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefDeleteRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author vsachdeva
 *
 */
public class WsSampleAttributeDefDeleteRest implements WsSampleRest {

  /**
   * AttributeDefDelete web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  public static void attributeDefDelete(WsSampleRestType wsSampleRestType) {

    try {
      // grouper http client
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      String url = RestClientSettings.URL + "/" + RestClientSettings.VERSION
              + "/attributeDefs";

      // assign the URL and method
      grouperHttpClient.assignUrl(url).assignGrouperHttpMethod(GrouperHttpMethod.post);
      // assign user and pass
      grouperHttpClient.assignUser(RestClientSettings.USER)
          .assignPassword(RestClientSettings.PASS);

      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAttributeDefDeleteRequest attributeDefDeleteRequest = new WsRestAttributeDefDeleteRequest();

      // set the act as id
      WsSubjectLookup actAsSubject = new WsSubjectLookup("GrouperSystem", null, null);
      attributeDefDeleteRequest.setActAsSubjectLookup(actAsSubject);

      WsAttributeDefLookup wsAttributeDefLookup = new WsAttributeDefLookup("test:test1",
          null);
      attributeDefDeleteRequest
          .setWsAttributeDefLookups(new WsAttributeDefLookup[] { wsAttributeDefLookup });

      //get the xml / json / xhtml / paramString
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType()
          .writeString(attributeDefDeleteRequest);

      //make sure right content type is in request (e.g. application/xhtml+xml
      String contentType = wsSampleRestType.getWsLiteRequestContentType()
          .getContentType();

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

      Object result = wsSampleRestType
          .getWsLiteResponseContentType().parseString(response);

      //see if problem
      if (result instanceof WsRestResultProblem) {
        throw new RuntimeException(((WsRestResultProblem) result).getResultMetadata()
            .getResultMessage());
      }

      //convert to object (from xhtml, xml, json, etc)
      WsAttributeDefDeleteResults wsAttributeDefDeleteResults = (WsAttributeDefDeleteResults) result;

      String resultMessage = wsAttributeDefDeleteResults.getResultMetadata()
          .getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: successString: "
            + successString + ", resultCode: " + resultCode
            + ", " + resultMessage);
      }

      System.out.println("Server version: "
          + wsAttributeDefDeleteResults.getResponseMetadata().getServerVersion()
          + ", result code: " + resultCode
          + ", result message: " + resultMessage);

      for (WsAttributeDefDeleteResult wsAttributeDefDeleteResult : GrouperUtil.nonNull(
          wsAttributeDefDeleteResults.getResults(), WsAttributeDefDeleteResult.class)) {
        System.out.println("Result: "
            + wsAttributeDefDeleteResult.getResultMetadata().getResultCode()
            + " for " + wsAttributeDefDeleteResult.getWsAttributeDef().getName());
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    attributeDefDelete(WsSampleRestType.json);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  @Override
  public void executeSample(WsSampleRestType wsSampleRestType) {
    attributeDefDelete(wsSampleRestType);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#validType(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  @Override
  public boolean validType(WsSampleRestType wsSampleRestType) {
    //dont allow http params
    return !WsSampleRestType.http_json.equals(wsSampleRestType);
  }
}
