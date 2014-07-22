/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer
 * $Id: WsExample.java,v 1.2 2009-11-15 18:50:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.examples;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.util.GrouperClientXstreamUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAddMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestResultProblem;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.XStream;
import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.io.xml.CompactWriter;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.Credentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.Header;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpClient;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.HttpException;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.auth.AuthScope;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.PostMethod;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.methods.StringRequestEntity;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.DefaultHttpParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.HttpMethodParams;

/**
 *
 */
public class WsExample {

  /**
   * http client
   * @return the http client
   */
  private static HttpClient httpClient() {
    HttpClient httpClient = new HttpClient();
    
    DefaultHttpParams.getDefaultParams().setParameter(
        HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

    httpClient.getParams().setAuthenticationPreemptive(true);
//    httpClient.getParams().setSoTimeout(90000);
//    httpClient.getParams().setConnectionManagerTimeout(90000);

    Credentials defaultcreds = new UsernamePasswordCredentials("mchyzer", 
        "xxxxxx");

    //e.g. localhost and 8093
    httpClient.getState()
        .setCredentials(new AuthScope("localhost", 8092), defaultcreds);

    return httpClient;
  }

  /**
   * 
   * @return the method
   */
  private static PostMethod postMethod() {
    //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
    //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
    PostMethod method = new PostMethod(
        "http://localhost:8092/grouper-ws/servicesRest" + "/" + "v1_4_000"  
          + "/groups/aStem%3AaGroup/members");

    //no keep alive so response if easier to indent for tests
    method.setRequestHeader("Connection", "close");
    
    return method;
  }

  /**
   * 
   * @param xStream
   * @param object
   * @return the xml
   */
  private static String marshalObject(XStream xStream, Object object) {
    StringWriter stringWriter = new StringWriter();
    //dont indent
    xStream.marshal(object, new CompactWriter(stringWriter));

    String requestDocument = stringWriter.toString();
    return requestDocument;
  }
  
  /**
   * 
   * @param xStream
   * @param objectToMarshall
   * @return the post method
   * @throws UnsupportedEncodingException 
   * @throws HttpException 
   * @throws IOException 
   */
  private static PostMethod postMethod(XStream xStream, Object objectToMarshall) 
      throws UnsupportedEncodingException, HttpException, IOException {
    String contentType = "text/xml";
    
    HttpClient httpClient = httpClient();

    PostMethod method = postMethod();

    String requestDocument = marshalObject(xStream, objectToMarshall);
    
    method.setRequestEntity(new StringRequestEntity(requestDocument, contentType, "UTF-8"));
    
    httpClient.executeMethod(method);

    return method;

  }
  
  /**
   * 
   */
  public static class GrouperTestClientWs {
    
    /**
     * 
     */
    private XStream xStream;
    
    /**
     * 
     */
    private PostMethod method;
    
    /**
     * 
     */
    public GrouperTestClientWs() {
      this.xStream = GrouperClientXstreamUtils.retrieveXstream();
    }
    
    /** */
    private String response;
    
    /**
     * 
     */
    private boolean success = false;

    /**
     * 
     */
    private String resultCode = null;
    
    /**
     * 
     * @param toSend
     * @return the response object
     * @throws UnsupportedEncodingException
     * @throws HttpException
     * @throws IOException
     */
    public Object executeService(Object toSend) throws UnsupportedEncodingException, HttpException, IOException {
      //make sure right content type is in request (e.g. application/xhtml+xml
      this.method = postMethod(this.xStream, toSend);

      //make sure a request came back
      Header successHeader = this.method.getResponseHeader("X-Grouper-success");
      String successString = successHeader == null ? null : successHeader.getValue();
      if (GrouperClientUtils.isBlank(successString)) {
        throw new RuntimeException("Web service did not even respond!");
      }
      this.success = "T".equals(successString);
      this.resultCode = this.method.getResponseHeader("X-Grouper-resultCode").getValue();
      
      this.response = GrouperClientUtils.responseBodyAsString(this.method);

      Object resultObject = this.xStream.fromXML(this.response);
      
      //see if problem
      if (resultObject instanceof WsRestResultProblem) {
        throw new RuntimeException(((WsRestResultProblem)resultObject).getResultMetadata().getResultMessage());
      }

      return resultObject;
    }

    /**
     * if failure, handle it
     * @param resultMessage
     */
    public void handleFailure(String resultMessage) {
      // see if request worked or not
      if (!this.success) {
        throw new RuntimeException("Bad response from web service: resultCode: " + this.resultCode
            + ", " + resultMessage);
      }

    }
    
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    Map<String, String> argMap = GrouperClientUtils.argMap(args);
    Map<String, String> argMapNotUsed = new HashMap<String, String>(argMap);
    String groupName = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "groupName", true);
    String subjectIds = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "subjectIds", false);
    List<String> subjectIdsList = GrouperClientUtils.splitTrimToList(subjectIds, ","); 
    String subjectIdentifiers = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "subjectIdentifiers", false);
    List<String> subjectIdentifiersList = GrouperClientUtils.splitTrimToList(subjectIdentifiers, ",");
    String sourceIds = GrouperClientUtils.argMapString(argMap, argMapNotUsed, "sourceIds", false);
    List<String> sourceIdsList = GrouperClientUtils.splitTrimToList(sourceIds, ",");
    boolean replaceAllExisting = GrouperClientUtils.argMapBoolean(argMap, argMapNotUsed, "replaceAllExisting", false, false);
    String response = addMember(groupName, subjectIdsList, subjectIdentifiersList, sourceIdsList, replaceAllExisting);
    System.out.println(response);
  }

  /**
   * add member(s)
   * @param groupName
   * @param subjectIds
   * @param subjectIdentifiers
   * @param sourceIds
   * @param replaceAllExisting if replace all existing
   * @return the status
   */
  public static String addMember(String groupName, List<String> subjectIds, 
      List<String> subjectIdentifiers, List<String> sourceIds, boolean replaceAllExisting) {
    StringBuilder result = new StringBuilder();
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAddMemberRequest addMember = new WsRestAddMemberRequest();

      // set the act as id
      WsSubjectLookup actAsSubject = new WsSubjectLookup("GrouperSystem", null, null);
      addMember.setActAsSubjectLookup(actAsSubject);

      // just add, dont replace
      addMember.setReplaceAllExisting(replaceAllExisting ? "T" : "F");

      // add two subjects to the group
      int subjectIdLength = GrouperClientUtils.length(subjectIds);
      int subjectIdentifierLength = GrouperClientUtils.length(subjectIdentifiers);
      int sourceIdLength = GrouperClientUtils.length(sourceIds);
      
      if (subjectIdLength == 0 && subjectIdentifierLength == 0) {
        throw new RuntimeException("Cant pass no subject ids and no subject identifiers!");
      }
      if (subjectIdLength != 0 && subjectIdentifierLength != 0) {
        throw new RuntimeException("Cant pass subject ids and subject identifiers! (pass one of the other)");
      }
      
      if (sourceIdLength > 0 && sourceIdLength != subjectIdLength 
          && sourceIdLength != subjectIdentifierLength) {
        throw new RuntimeException("If source ids are passed in, you " +
        		"must pass the same number as subjectIds or subjectIdentifiers");
      }
      
      int subjectsLength = Math.max(subjectIdLength, subjectIdentifierLength);
      WsSubjectLookup[] subjectLookups = new WsSubjectLookup[subjectsLength];
      for (int i=0;i<subjectsLength;i++) {
        subjectLookups[i] = new WsSubjectLookup();
        if (subjectIdLength > 0) {
          subjectLookups[i].setSubjectId(subjectIds.get(i));
        }
        if (subjectIdentifierLength > 0) {
          subjectLookups[i].setSubjectIdentifier(subjectIdentifiers.get(i));
        }
        if (sourceIdLength > 0) {
          subjectLookups[i].setSubjectSourceId(sourceIds.get(i));
        }
      }
      addMember.setSubjectLookups(subjectLookups);
      
      GrouperTestClientWs grouperClientWs = new GrouperTestClientWs();
      
      //convert to object (from xhtml, xml, json, etc)
      WsAddMemberResults wsAddMemberResults = (WsAddMemberResults)grouperClientWs.executeService(addMember);
      
      String resultMessage = wsAddMemberResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(resultMessage);
      
      int index = 0;
      for (WsAddMemberResult wsAddMemberResult : wsAddMemberResults.getResults()) {

        result.append("Index " + index + ": success: " + wsAddMemberResult.getResultMetadata().getSuccess()
            + ": code: " + wsAddMemberResult.getResultMetadata().getResultCode() + ": " 
            + wsAddMemberResult.getWsSubject().getId() + "\n");
        index++;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result.toString();

  }
  
}
