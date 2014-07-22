/**
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
 */
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class LoaderLdapElUtilsTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LoaderLdapElUtilsTest("testConvertDnToSubpath"));
  }
  
  /**
   * @param name
   */
  public LoaderLdapElUtilsTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public static void testConvertDnToSpecificValue() {
    assertEquals("someapp", LoaderLdapElUtils.convertDnToSpecificValue("cn=someapp,ou=groups,dc=upenn,dc=edu"));
    
    Map<String, Object> envVars = new HashMap<String, Object>();
    envVars.put("subjectId", "cn=someapp,ou=groups,dc=upenn,dc=edu");
    
    assertEquals("someapp", LoaderLdapUtils.substituteEl("${loaderLdapElUtils.convertDnToSpecificValue(subjectId)}", envVars));
    
  }
  
  /**
   * 
   */
  public static void testConvertGroupNameAttributes() {
    
    Map<String, Object> envVars = new HashMap<String, Object>();
    
    Map<String, String> groupAttributes = new HashMap<String, String>();
    groupAttributes.put("cn", "test:testGroup");
    
    envVars.put("groupAttributes", groupAttributes);
    
    assertEquals("groups:test:testGroup", LoaderLdapUtils.substituteEl("groups:${groupAttributes['cn']}", envVars));
    
  }
  
  /**
   * 
   */
  public static void testConvertDnToSubpath() {
    assertEquals("a:b:c", LoaderLdapElUtils.convertDnToSubPath("cn=a:b:c,ou=groups,dc=upenn,dc=edu", "dc=upenn,dc=edu", "ou=groups"));
    assertEquals("groups:a:b:c", LoaderLdapElUtils.convertDnToSubPath("cn=a:b:c,ou=groups,dc=upenn,dc=edu", "dc=edu", "dc=upenn"));
    
  }
  
  
}
