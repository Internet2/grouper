/*
 * @author mchyzer
 * $Id: RestConverterTest.java,v 1.5 2009-12-16 06:02:35 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembership;
import edu.internet2.middleware.grouper.ws.rest.WsRestClassLookup;


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
    TestRunner.run(new RestConverterTest("testMarshal2"));
  }
  
  /**
   * unmarshal a problem string
   */
  public void testMarshal3() {
    
    WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
    wsGetMembershipsResults.setWsMemberships(new WsMembership[0]);
    String string = WsRestRequestContentType.json.writeString(wsGetMembershipsResults);
    StringBuilder stringBuilder = new StringBuilder();
    
    //System.out.println(string);

    Object object = WsRestRequestContentType.json.parseString(string, stringBuilder);

    //System.out.println(object);
    
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
        
        System.out.println(theString + "\n\n");
        
        //see if there is an inputter
        WsRestRequestContentType wsLiteRequestContentType = WsRestRequestContentType
          .valueOfIgnoreCase(wsLiteResponseContentType.name(), true);
        
        //all responses should have a request at this point
        StringBuilder warnings = new StringBuilder();
        BeanGrandparent beanGrandparent2 = (BeanGrandparent)wsLiteRequestContentType.parseString(theString, warnings);
        
        //now convert to string again, and should be same
        stringWriter = new StringWriter();
        wsLiteResponseContentType.writeString(beanGrandparent2, stringWriter);
        String theString2 = stringWriter.toString();

        if (!StringUtils.equals(theString, theString2)) {
          System.out.println(theString2 + "\n\n");
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
