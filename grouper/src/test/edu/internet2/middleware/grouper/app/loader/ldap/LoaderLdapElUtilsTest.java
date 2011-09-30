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
    TestRunner.run(new LoaderLdapElUtilsTest("testConvertGroupNameAttributes"));
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
  
  
  
}
