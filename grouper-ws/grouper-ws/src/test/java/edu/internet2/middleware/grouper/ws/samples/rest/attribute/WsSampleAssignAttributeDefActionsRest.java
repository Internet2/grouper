/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ******************************************************************************/
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

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefAssignActionResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRestResultProblem;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefActionsRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author vsachdeva
 * sample web service client to assign action to attribute def
 */
public class WsSampleAssignAttributeDefActionsRest implements WsSampleRest {

  /**
   * assign actions to attribute def web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  public static void assignAttributesDefActions(WsSampleRestType wsSampleRestType) {

    try {
      HttpClient httpClient = new HttpClient();

      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      String url = RestClientSettings.URL + "/" + RestClientSettings.VERSION
          + "/attributeDefActions";
      PostMethod method = new PostMethod(url);

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(RestClientSettings.USER,
          RestClientSettings.PASS);

      //no keep alive so response if easier to indent for tests
      method.setRequestHeader("Connection", "close");

      //e.g. localhost and 8093
      httpClient.getState().setCredentials(
          new AuthScope(RestClientSettings.HOST, RestClientSettings.PORT), defaultcreds);

      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAssignAttributeDefActionsRequest assignAttributesDefActions = new WsRestAssignAttributeDefActionsRequest();

      WsAttributeDefLookup wsAttributeDefLookup = new WsAttributeDefLookup(
          "test:testAttributeAssignDefNameDef", null);
      assignAttributesDefActions.setWsAttributeDefLookup(wsAttributeDefLookup);
      assignAttributesDefActions.setActions(new String[] { "read", "view" });
      assignAttributesDefActions.setAssign("T");
      assignAttributesDefActions.setReplaceAllExisting("T");

      //get the xml / json / xhtml / paramString
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType()
          .writeString(assignAttributesDefActions);

      //make sure right content type is in request (e.g. application/xhtml+xml
      String contentType = wsSampleRestType.getWsLiteRequestContentType()
          .getContentType();

      method.setRequestEntity(new StringRequestEntity(requestDocument, contentType,
          "UTF-8"));

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

      Object result = wsSampleRestType.getWsLiteResponseContentType().parseString(
          response);

      //see if problem
      if (result instanceof WsRestResultProblem) {
        throw new RuntimeException(((WsRestResultProblem) result).getResultMetadata()
            .getResultMessage());
      }

      //convert to object (from xhtml, xml, json, etc)
      WsAttributeDefAssignActionResults wsAttributeDefAssignActionResults = (WsAttributeDefAssignActionResults) result;

      String resultMessage = wsAttributeDefAssignActionResults.getResultMetadata()
          .getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: successString: "
            + successString + ", resultCode: " + resultCode
            + ", " + resultMessage);
      }

      System.out.println("Server version: "
          + wsAttributeDefAssignActionResults.getResponseMetadata().getServerVersion()
          + ", result code: " + resultCode
          + ", result message: " + resultMessage);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    assignAttributesDefActions(WsSampleRestType.json);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  @Override
  public void executeSample(WsSampleRestType wsSampleRestType) {
    assignAttributesDefActions(wsSampleRestType);
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
