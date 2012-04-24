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
/*
 * @author mchyzer
 * $Id: WsXhtmlOutputConverterTest.java,v 1.2 2009-11-15 18:54:00 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.rest.WsRestClassLookup;


/**
 * test the output converter
 */
public class WsXhtmlOutputConverterTest extends TestCase {

  static {
    WsRestClassLookup.addAliasClass(BeanChild.class);
    WsRestClassLookup.addAliasClass(BeanParent.class);
    WsRestClassLookup.addAliasClass(BeanGrandparent.class);
  }
  /**
   * @param name
   */
  public WsXhtmlOutputConverterTest(String name) {
    super(name);
  }

  /**
   * run a test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsXhtmlOutputConverterTest("testMarshal2"));
  }
  
  /**
   * test convert to xml
   */
  public void testMarshal() {
    BeanChild beanChild = new BeanChild("va<l1", "val2", new String[]{"a"}, new int[]{1, 2});
    WsXhtmlOutputConverter wsXhtmlOutputConverter = new WsXhtmlOutputConverter(false, null);
    String xhtml = wsXhtmlOutputConverter.writeBean(beanChild);

    System.out.println(xhtml);
    assertEquals("<div title=\"BeanChild\"><p class=\"childField1\">va&lt;l1</p><p class=\"childField2\">val2</p><ul class=\"childStringArray\"><li>a</li></ul><ul class=\"childIntegerArray\"><li>1</li><li>2</li></ul></div>", xhtml);
    
    //test warnings
    xhtml = "<div title=\"BeanChild\" id=\"something\"><span>something</span><p class=\"childField1\">va&lt;l1</p><p class=\"childField2\">val2</p><ul class=\"childStringArray\"><li>a</li></ul><ul class=\"childIntegerArray\"><li>1</li><li>2</li></ul></div>";
    WsXhtmlInputConverter wsXhtmlInputConverter = new WsXhtmlInputConverter();
    
    beanChild = (BeanChild)wsXhtmlInputConverter.parseXhtmlString(xhtml);
    
    String warnings = wsXhtmlInputConverter.getWarnings();
    System.out.println(warnings);
    assertFalse(StringUtils.isBlank(warnings));
    assertTrue("should detect extraneous attribute", warnings.contains("not expecting attribute"));
    assertTrue("should detect extraneous element", warnings.contains("not expecting child element"));
  }
  
  /**
   * test convert object map to xhtml
   * @param includeHeader
   */
  public void testMarshal2() {
    marshal2helper(false);
    marshal2helper(true);
  }

  /**
   * test convert object map to xhtml
   * @param includeHeader
   */
  public void marshal2helper(boolean includeHeader) {
    BeanGrandparent beanGrandparent = generateGrandParent();
    
    WsXhtmlOutputConverter wsXhtmlOutputConverter = new WsXhtmlOutputConverter(includeHeader, "the title");
    String xhtml = wsXhtmlOutputConverter.writeBean(beanGrandparent);

    System.out.println(xhtml);
    
    WsXhtmlInputConverter wsXhtmlInputConverter = new WsXhtmlInputConverter();
    beanGrandparent = (BeanGrandparent)wsXhtmlInputConverter.parseXhtmlString(xhtml);
  
    //shouldnt be any warnings
    assertTrue(wsXhtmlInputConverter.getWarnings(), StringUtils.isBlank(wsXhtmlInputConverter.getWarnings()));

    WsXhtmlOutputConverter wsXhtmlOutputConverter2 = new WsXhtmlOutputConverter(includeHeader, "the title");
    String xhtml2 = wsXhtmlOutputConverter2.writeBean(beanGrandparent);
    
    assertEquals(xhtml, xhtml2);
  }

  /**
   * @return
   */
  public static BeanGrandparent generateGrandParent() {
    //note, xhtml doesnt do nulls vs empty strings, so dont worry about empty string in test
    BeanChild beanChild = new BeanChild("v\"a<l{1}", "val2", new String[]{"a"}, new int[]{1, 2});
    BeanParent beanParent = new BeanParent("qwe", "rtyu", new String[]{ "uio", "cv"}, 45, 
        null, beanChild, null, new BeanChild[]{beanChild});
    
    BeanGrandparent beanGrandparent = new BeanGrandparent("xv", null, 
        new String[]{null},beanParent, new BeanParent[]{beanParent, beanParent} );

    return beanGrandparent;
  }
  
}
