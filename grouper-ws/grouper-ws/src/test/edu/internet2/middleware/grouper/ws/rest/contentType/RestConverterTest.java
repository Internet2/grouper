/*
 * @author mchyzer
 * $Id: RestConverterTest.java,v 1.2 2009-11-17 02:55:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.rest.WsRestClassLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.soap.WsMembership;


/**
 * test the output converter
 */
public class RestConverterTest extends TestCase {

  /**
   * @param name
   */
  public RestConverterTest(String name) {
    super(name);
  }

  /**
   * run a test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RestConverterTest("testMarshal3"));
  }
  
  /**
   * unmarshal a problem string
   */
  public void testMarshal3() {
    
    WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
    wsGetMembershipsResults.setResults(new WsMembership[0]);
    String string = WsRestRequestContentType.json.writeString(wsGetMembershipsResults);
    StringBuilder stringBuilder = new StringBuilder();
    
    //System.out.println(string);

    Object object = WsRestRequestContentType.json.parseString(string, stringBuilder);

    //System.out.println(object);
    
    string = "{\"WsGetMembersResults\":{\"responseMetadata\":{\"serverVersion\":\"v1_5_000\",\"millis\":\"858\"},\"subjectAttributeNames\":{\"string\":[\"description\",\"loginid\",\"name\"],\"results\":{\"WsGetMembersResult\":{\"wsGroup\":{\"displayName\":\"a stem:a group\",\"description\":\"a group description\",\"extension\":\"aGroup\",\"name\":\"aStem:aGroup\",\"uuid\":\"ba4a6eea7aae4b899e48e77960105519\",\"displayExtension\":\"a group\"},\"wsSubjects\":{\"WsSubject\":[{\"attributeValues\":{\"string\":[\"GrouperSysAdmin\",{\"string\":\"EveryEntity\"}],\"sourceId\":\"g:isa\",\"success\":\"T\",\"name\":\"GrouperSysAdmin\",\"id\":\"GrouperSystem\",\"resultCode\":\"SUCCESS\"}},{\"attributeValues\":[{\"string\":[\"\",{\"string\":\"10021368\"}],\"sourceId\":\"jdbc\",\"success\":\"T\",\"name\":\"10021368\",\"id\":\"10021368\",\"resultCode\":\"SUCCESS\"},{\"string\":[\"\",{\"string\":\"10039438\"}],\"sourceId\":\"jdbc\",\"success\":\"T\",\"name\":\"10039438\",\"id\":\"10039438\",\"resultCode\":\"SUCCESS\"}],\"WsSubject\":{}}]},\"resultMetadata\":{\"resultCode\":\"SUCCESS\",\"success\":\"T\"}}},\"resultMetadata\":{\"resultMessage\":\"Success for: clientVersion: v1_5_000, wsGroupLookups: Array size: 1: [0]: WsGroupLookup[groupName=aStem:aGroup]\n\n, memberFilter: All, includeSubjectDetail: false, actAsSubject: null, fieldName: null, subjectAttributeNames: Array size: 3: [0]: description\n[1]: loginid\n[2]: name\n\n, paramNames: \n, params: null\",\"resultCode\":\"SUCCESS\",\"success\":\"T\"}}}}";
    
    object = WsRestRequestContentType.json.parseString(string, stringBuilder);
    System.out.println(object);
  }
  
  /**
   * test convert object map to xhtml
   * @param includeHeader
   * @throws IOException
   */
  public void testMarshal2() throws IOException {
    
    //register beans
    WsRestClassLookup.addAliasClass(BeanGrandparent.class);
    WsRestClassLookup.addAliasClass(BeanParent.class);
    WsRestClassLookup.addAliasClass(BeanChild.class);

    BeanGrandparent beanGrandparentOrig = WsXhtmlOutputConverterTest.generateGrandParent();
    
    //we know XML works, so always compare to that
    StringWriter xmlOrigWriter = new StringWriter();
    WsRestResponseContentType.xml.writeString(beanGrandparentOrig, xmlOrigWriter);
    String xmlOrig = xmlOrigWriter.toString();
    
    for (WsRestResponseContentType wsLiteResponseContentType : WsRestResponseContentType.values()) {
      
      try {
        
        BeanGrandparent beanGrandparent = WsXhtmlOutputConverterTest.generateGrandParent();
        StringWriter stringWriter = new StringWriter();
        wsLiteResponseContentType.writeString(beanGrandparent, stringWriter);
        String theString = stringWriter.toString();
        
        System.out.println(theString + "\n");
        
        //see if there is an inputter
        WsRestRequestContentType wsLiteRequestContentType = WsRestRequestContentType
          .valueOfIgnoreCase(wsLiteResponseContentType.name(), true);
        
        //all responses should hav a request at this point
        StringBuilder warnings = new StringBuilder();
        BeanGrandparent beanGrandparent2 = (BeanGrandparent)wsLiteRequestContentType.parseString(theString, warnings);
        
        //now convert to string again, and should be same
        stringWriter = new StringWriter();
        wsLiteResponseContentType.writeString(beanGrandparent2, stringWriter);
        String theString2 = stringWriter.toString();

        if (!StringUtils.equals(theString, theString2)) {
          System.out.println(theString2 + "\n");
        }
        
        assertEquals("Problem with content type: " + wsLiteResponseContentType, theString, theString2);
        
        //compare to orig
        stringWriter = new StringWriter();
        WsRestResponseContentType.xml.writeString(beanGrandparent2, stringWriter);
        String xmlString = stringWriter.toString();

        if (!StringUtils.equals(xmlOrig, xmlString)) {
          System.out.println(xmlOrig + "\n");
          System.out.println(xmlString + "\n");
        }
        
        assertEquals("Problem with content type: " + wsLiteResponseContentType, xmlOrig, xmlString);
      } catch (RuntimeException re) {
        throw new RuntimeException("Problem with content type: " + wsLiteResponseContentType, re);
      }
      
    }
    
  }
}
