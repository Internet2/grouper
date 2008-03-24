/*
 * @author mchyzer
 * $Id: GrouperServiceUtilsTest.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.util;

import java.util.Map;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.ws.lite.group.WsLiteAddMemberSimpleRequest;
import edu.internet2.middleware.grouper.ws.lite.group.WsLiteGetMembersSimpleRequest;



/**
 *
 */
public class GrouperServiceUtilsTest extends TestCase {

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GrouperServiceUtilsTest("testQueryStringsAddMemberSimple"));
    //TestRunner.run(TestGroup0.class);
    //runPerfProblem();
  }

  /**
   * @param name
   */
  public GrouperServiceUtilsTest(String name) {
    super(name);
  }

  /**
   * create some query strings
   */
  public void testQueryStringsAddMemberSimple() {
    WsLiteAddMemberSimpleRequest wsLiteAddMemberSimpleRequest = 
      new WsLiteAddMemberSimpleRequest();
    wsLiteAddMemberSimpleRequest.setClientVersion("v1_3_000");
    wsLiteAddMemberSimpleRequest.setGroupName("aStem:aGroup");
    wsLiteAddMemberSimpleRequest.setSubjectId("10021368");
    wsLiteAddMemberSimpleRequest.setActAsSubjectId("GrouperSystem");
    String queryString = GrouperServiceUtils.marshalSimpleBeanToQueryString(wsLiteAddMemberSimpleRequest,
        true, false);
    System.out.println(queryString);
  }
  
  /**
   * make sure http params marshal correctly
   */
  public void testMarshalHttp() {
    WsLiteGetMembersSimpleRequest wsLiteGetMembersSimpleRequest 
      = new WsLiteGetMembersSimpleRequest();
    wsLiteGetMembersSimpleRequest.setActAsSubjectId("abc");
    wsLiteGetMembersSimpleRequest.setMemberFilter("123");
    
    String queryString = GrouperServiceUtils
      .marshalSimpleBeanToQueryString(wsLiteGetMembersSimpleRequest, true, true);
    //System.out.println(queryString);
    
    //create map that mimics a real request map
    Map<String, String> paramMap = GrouperServiceUtils.convertQueryStringToMap(
        queryString);
    
    wsLiteGetMembersSimpleRequest = (WsLiteGetMembersSimpleRequest)
      GrouperServiceUtils.marshalHttpParamsToObject(paramMap, null, null);
    
    assertEquals("abc", wsLiteGetMembersSimpleRequest.getActAsSubjectId());
    
    assertEquals("123", wsLiteGetMembersSimpleRequest.getMemberFilter());
    
  }
  
  
  /**
   * test format http
   */
  public void testFormatHttp() {
    
    String input = "POST /grouper-ws/services/GrouperService HTTP/1.1\n"
+ "Content-Type: application/soap+xml; charset=UTF-8; action=\"urn:addMember\"\n"
+ "User-Agent: Axis2\n"
+ "Authorization: Basic R3JvdXBlclN5c3RlbTpwYXNz\n"
+ "Host: localhost:8092\n"
+ "Transfer-Encoding: chunked\n"
+ "\n"
+ "3cc\n"
+ "<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><ns10:addMember xmlns:ns10=\"http://webservices.grouper.middleware.internet2.edu/xsd\"><ns10:clientVersion>v1_3_000</ns10:clientVersion><ns10:wsGroupLookup><ns2:groupName xmlns:ns2=\"http://group.ws.grouper.middleware.internet2.edu/xsd\">aStem:aGroup</ns2:groupName></ns10:wsGroupLookup><ns10:subjectLookups><ns4:subjectId xmlns:ns4=\"http://subject.ws.grouper.middleware.internet2.edu/xsd\">10021368</ns4:subjectId></ns10:subjectLookups><ns10:subjectLookups><ns4:subjectId xmlns:ns4=\"http://subject.ws.grouper.middleware.internet2.edu/xsd\">10039438</ns4:subjectId></ns10:subjectLookups><ns10:replaceAllExisting>F</ns10:replaceAllExisting><ns10:actAsSubjectLookup><ns4:subjectId xmlns:ns4=\"http://subject.ws.grouper.middleware.internet2.edu/xsd\">GrouperSystem</ns4:subjectId></ns10:actAsSubjectLookup></ns10:addMember></soapenv:Body></soapenv:Envelope>\n"
+ "0";
    String output = GrouperServiceUtils.formatHttp(input);
    //System.out.println(output);
    //should add a bunch of whitespace here
    assertTrue(output.length() > input.length() + 20);
  }
  
}
