/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeBatchEntry;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeBatchResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesBatchResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRestResultProblem;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesBatchRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author mchyzer
 */
public class WsSampleAssignAttributesBatchRest implements WsSampleRest {

  /**
   * attribute assignments batch web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  public static void assignAttributesBatch(WsSampleRestType wsSampleRestType) {

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
      
      WsRestAssignAttributesBatchRequest wsRestAssignAttributesBatchRequest = new WsRestAssignAttributesBatchRequest();

      // set the act as id
      WsSubjectLookup actAsSubject = WsSubjectLookup.class.newInstance();
      actAsSubject.setSubjectId("GrouperSystem");

      WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry1 = new WsAssignAttributeBatchEntry();
      
      {
        wsAssignAttributeBatchEntry1.setAttributeAssignOperation("assign_attr");
        wsAssignAttributeBatchEntry1.setAttributeAssignType("group");
        
        WsAttributeDefNameLookup wsAttributeDefNameLookup1 = new WsAttributeDefNameLookup();
        wsAttributeDefNameLookup1.setName("test:testAttributeAssignDefName");
        wsAssignAttributeBatchEntry1.setWsAttributeDefNameLookup(wsAttributeDefNameLookup1);

        WsGroupLookup wsGroupLookup = new WsGroupLookup();
        wsGroupLookup.setGroupName("test:groupTestAttrAssign");

        wsAssignAttributeBatchEntry1.setWsOwnerGroupLookup(wsGroupLookup);

      }
      
      WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry2 = new WsAssignAttributeBatchEntry();

      {
        wsAssignAttributeBatchEntry2.setAttributeAssignOperation("assign_attr");
        wsAssignAttributeBatchEntry2.setAttributeAssignType("group_asgn");
        
        WsAttributeDefNameLookup wsAttributeDefNameLookup2 = new WsAttributeDefNameLookup();
        wsAttributeDefNameLookup2.setName("test:testAttributeAssignAssignName");
        wsAssignAttributeBatchEntry2.setWsAttributeDefNameLookup(wsAttributeDefNameLookup2);
  
        WsAttributeAssignLookup wsAttributeAssignLookup = new WsAttributeAssignLookup();
        wsAttributeAssignLookup.setBatchIndex("0");
        wsAssignAttributeBatchEntry2.setWsOwnerAttributeAssignLookup(wsAttributeAssignLookup);
      }
      

      WsAssignAttributeBatchEntry[] wsAssignAttributeBatchEntries = new WsAssignAttributeBatchEntry[]{
          wsAssignAttributeBatchEntry1, wsAssignAttributeBatchEntry2};
      
      wsRestAssignAttributesBatchRequest.setWsAssignAttributeBatchEntries(wsAssignAttributeBatchEntries);
      
      
      //get the xml / json / xhtml / paramString
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(wsRestAssignAttributesBatchRequest);
      
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
      WsAssignAttributesBatchResults wsAssignAttributesBatchResults = (WsAssignAttributesBatchResults)result;

      System.out.println(ToStringBuilder.reflectionToString(
          wsAssignAttributesBatchResults));

      String resultMessage = wsAssignAttributesBatchResults.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: successString: " + successString + ", resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      WsAssignAttributeBatchResult[] wsAssignAttributeBatchResultsArray = wsAssignAttributesBatchResults
        .getWsAssignAttributeBatchResultArray();

      for (WsAssignAttributeBatchResult wsAssignAttributeBatchResult : wsAssignAttributeBatchResultsArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsAssignAttributeBatchResult));
      }
      
      WsGroup[] wsGroupsResultArray = wsAssignAttributesBatchResults.getWsGroups();

      for (WsGroup wsGroup : wsGroupsResultArray) {
        System.out.println(ToStringBuilder.reflectionToString(
            wsGroup));
      }

      System.out.println("Server version: " + wsAssignAttributesBatchResults.getResponseMetadata().getServerVersion()
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
    assignAttributesBatch(WsSampleRestType.xml);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public void executeSample(WsSampleRestType wsSampleRestType) {
    assignAttributesBatch(wsSampleRestType);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#validType(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public boolean validType(WsSampleRestType wsSampleRestType) {
    //dont allow http params
    return !WsSampleRestType.http_json.equals(wsSampleRestType);
  }
}
