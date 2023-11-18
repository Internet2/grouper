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
package edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges;

import junit.framework.TestCase;
import junit.textui.TestRunner;

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

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

/**
 * @author mchyzer
 */
public class WsSampleGetAssignGrouperPrivilegesRestLiteTest extends TestCase implements WsSampleRest {

  @Override
  protected void setUp() throws Exception {
    RestClientSettings.resetData();
    

  }
  
  /**
   * 
   * @param wsSampleRestType
   * @param groupName 
   * @param subjectId 
   * @param privilegeType 
   * @param privilegeName 
   * @param expectedAllowed 
   * @param expectedResultCode 
   * @throws Exception 
   * @return the result
   */
  public WsGetGrouperPrivilegesLiteResult retrievePrivileges(WsSampleRestType wsSampleRestType, 
      String groupName, String subjectId, String privilegeType, String privilegeName, boolean expectedAllowed, 
      String expectedResultCode) throws Exception {

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
    WsRestGetGrouperPrivilegesLiteRequest wsRestGetGrouperPrivilegesLiteRequest 
      = new WsRestGetGrouperPrivilegesLiteRequest();

    // set the act as id
    wsRestGetGrouperPrivilegesLiteRequest.setActAsSubjectId("GrouperSystem");
    
    wsRestGetGrouperPrivilegesLiteRequest.setSubjectId(subjectId);
    wsRestGetGrouperPrivilegesLiteRequest.setGroupName(groupName);
    
    wsRestGetGrouperPrivilegesLiteRequest.setPrivilegeType(privilegeType);
    wsRestGetGrouperPrivilegesLiteRequest.setPrivilegeName(privilegeName);
    
    //get the xml / json / xhtml / paramString
    String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(wsRestGetGrouperPrivilegesLiteRequest);
    
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
    WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult = (WsGetGrouperPrivilegesLiteResult)wsSampleRestType
      .getWsLiteResponseContentType().parseString(response);
    
    String resultMessage = wsGetGrouperPrivilegesLiteResult.getResultMetadata().getResultMessage();

    // see if request worked or not
    assertTrue("Bad response from web service: resultCode: " + resultCode
          + ", " + resultMessage, success);
    
    assertEquals(expectedAllowed ? 1 : 0, GrouperUtil.length(wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()));
    
    assertEquals(expectedResultCode, resultCode);

    if (expectedAllowed) {
      
      WsGrouperPrivilegeResult wsGrouperPrivilegeResult = wsGetGrouperPrivilegesLiteResult.getPrivilegeResults()[0];
      
      assertEquals(groupName, wsGrouperPrivilegeResult.getWsGroup().getName());
      assertEquals(subjectId, wsGrouperPrivilegeResult.getWsSubject().getId());
      assertEquals(privilegeType, wsGrouperPrivilegeResult.getPrivilegeType());
      assertEquals(privilegeName, wsGrouperPrivilegeResult.getPrivilegeName());
      assertEquals("T", wsGrouperPrivilegeResult.getAllowed());

    }
    
    return wsGetGrouperPrivilegesLiteResult;
  }
  
  /**
   * 
   * @param wsSampleRestType
   * @param groupName 
   * @param subjectId 
   * @param privilegeType 
   * @param privilegeName 
   * @param allowed 
   * @param expectedResultCode 
   * @throws Exception 
   * @return the result
   */
  public WsAssignGrouperPrivilegesLiteResult assignPrivileges(WsSampleRestType wsSampleRestType, 
      String groupName, String subjectId, String privilegeType, String privilegeName, boolean allowed,
      String expectedResultCode) throws Exception {
    
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
    WsRestAssignGrouperPrivilegesLiteRequest wsRestAssignGrouperPrivilegesLiteRequest 
      = new WsRestAssignGrouperPrivilegesLiteRequest();

    // set the act as id
    wsRestAssignGrouperPrivilegesLiteRequest.setActAsSubjectId("GrouperSystem");
    
    wsRestAssignGrouperPrivilegesLiteRequest.setSubjectId(subjectId);
    wsRestAssignGrouperPrivilegesLiteRequest.setGroupName(groupName);
    
    wsRestAssignGrouperPrivilegesLiteRequest.setPrivilegeType(privilegeType);
    wsRestAssignGrouperPrivilegesLiteRequest.setPrivilegeName(privilegeName);
    
    wsRestAssignGrouperPrivilegesLiteRequest.setAllowed(allowed ? "T" : "F");
    
    //get the xml / json / xhtml / paramString
    String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(wsRestAssignGrouperPrivilegesLiteRequest);
    
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
    WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult = (WsAssignGrouperPrivilegesLiteResult)wsSampleRestType
      .getWsLiteResponseContentType().parseString(response);
    
    String resultMessage = wsAssignGrouperPrivilegesLiteResult.getResultMetadata().getResultMessage();

    // see if request worked or not
    assertTrue("Bad response from web service: resultCode: " + resultCode
          + ", " + resultMessage, success);
    
    assertEquals(groupName, wsAssignGrouperPrivilegesLiteResult.getWsGroup().getName());
    assertEquals(subjectId, wsAssignGrouperPrivilegesLiteResult.getWsSubject().getId());
    assertEquals(privilegeType.toLowerCase(), wsAssignGrouperPrivilegesLiteResult.getPrivilegeType().toLowerCase());
    assertEquals(privilegeName.toLowerCase(), wsAssignGrouperPrivilegesLiteResult.getPrivilegeName().toLowerCase());
    assertEquals(expectedResultCode, resultCode);
    
    return wsAssignGrouperPrivilegesLiteResult;
  }
  
  /**
   * get grouper privileges lite web service with REST
   * @param wsSampleRestType is the type of rest (xml, xhtml, etc)
   */
  @SuppressWarnings("deprecation")
  public void assignGetGrouperPrivilegesLite(WsSampleRestType wsSampleRestType) {

    try {

      String groupName = "aStem:aGroup";
      String subjectId = "test.subject.0";
      String privilegeType = "access";
      String privilegeName = "optin";
      retrievePrivileges(wsSampleRestType, groupName, subjectId, privilegeType, privilegeName, false, "SUCCESS_NOT_ALLOWED");
      
      assignPrivileges(wsSampleRestType, groupName, subjectId, privilegeType, privilegeName, true, "SUCCESS_ALLOWED");
      assignPrivileges(wsSampleRestType, groupName, subjectId, privilegeType, privilegeName, true, "SUCCESS_ALLOWED_ALREADY_EXISTED");

      retrievePrivileges(wsSampleRestType, groupName, subjectId, privilegeType, privilegeName, true, "SUCCESS_ALLOWED");
      
      assignPrivileges(wsSampleRestType, groupName, subjectId, privilegeType, privilegeName, false, "SUCCESS_NOT_ALLOWED");
      assignPrivileges(wsSampleRestType, groupName, subjectId, privilegeType, privilegeName, false, "SUCCESS_NOT_ALLOWED_DIDNT_EXIST");

      retrievePrivileges(wsSampleRestType, groupName, subjectId, privilegeType, privilegeName, false, "SUCCESS_NOT_ALLOWED");
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * 
   */
  public void testGetAssign() {
    assignGetGrouperPrivilegesLite(WsSampleRestType.xml);
  }
  
  /**
   * 
   */
  public WsSampleGetAssignGrouperPrivilegesRestLiteTest() {
    super();
  }

  /**
   * @param name
   */
  public WsSampleGetAssignGrouperPrivilegesRestLiteTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    TestRunner.run(new WsSampleGetAssignGrouperPrivilegesRestLiteTest("testGetAssign"));
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#executeSample(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public void executeSample(WsSampleRestType wsSampleRestType) {
    assignGetGrouperPrivilegesLite(wsSampleRestType);
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest#validType(edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType)
   */
  public boolean validType(WsSampleRestType wsSampleRestType) {
    return true;
  }
}
