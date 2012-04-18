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
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
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
 */

package edu.internet2.middleware.ldappc.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.directory.SearchControls;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;
import edu.internet2.middleware.ldappc.exception.ConfigurationException;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter.OnNotFound;

/**
 * This set of tests validates the {@link edu.internet2.middleware.ldappc.ConfigManager}.
 */
public class ConfigManagerTest extends TestCase {

  /**
   * Relative configuration resource path
   */
  public static String RELATIVE_RESOURCE_PATH = "/test/edu/internet2/middleware/ldappc/configuration/data/";

  /**
   * Valid configuration file resource with all elements and attributes
   */
  public static String VALID_ALL_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcValidAll.xml";

  /**
   * Valid configuration file resource with all elements and no optional attributes
   */
  public static String VALID_NO_OPTIONAL_ATTRIBUTES_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcValidNoOptionalAttributes.xml";

  /**
   * Valid configuration file resource with a minimal Grouper group configuration
   */
  public static String VALID_GROUPER_GROUP_MINIMAL_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcValidGrouperGroupMinimal.xml";

  /**
   * Valid configuration file resource with a minimal Grouper membership configuration
   */
  public static String VALID_GROUPER_MEMBERSHIP_MINIMAL_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcValidGrouperMembershipMinimal.xml";

  /**
   * Invalid configuration file resource that is missing the ldappc element
   */
  public static String INVALID_NO_LDAPPC_ELEMENT_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcInvalidNoLdappcElement.xml";

  /**
   * Invalid configuration file with unnecessary g:gsa source-subject-identifier
   */
  public static String PROVISION_MEMBER_GROUPS_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcProvisionMemberGroups.xml";

  /**
   * Valid configuration file with property replacement
   */
  public static String PROPERTY_REPLACEMENT_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcPropertyReplacement.xml";

  /**
   * Valid properties file for property replacement
   */
  public static String PROPERTY_REPLACEMENT_PROPERTY_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcPropertyReplacement.properties";

  /**
   * Valid properties file for property replacement
   */
  public static String VALID_LDAPSEARCHFILTER_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcLdapSearchFilter.xml";

  /**
   * Empty properties file
   */
  public static String LDAPPC_EMPTY_PROPERTY_RESOURCE = RELATIVE_RESOURCE_PATH
      + "ldappcEmpty.properties";

  /**
   * Class constructor
   * 
   * @param name
   *          Name of the test case.
   */
  public ConfigManagerTest(String name) {
    super(name);
  }

  /**
   * Tear down the fixture.
   */
  protected void tearDown() {
  }

  /**
   * The main method for running the test.
   */
  public static void main(String args[]) {
    TestRunner.run(ConfigManagerTest.class);
  }

  /**
   * Test reading in a valid configuration file
   */
  public void testValidAllFile() {

    try {
      //
      // Get the configuration manager and validate all of the entries
      //
      String configPath = ConfigManager.getSystemResourceURL(
          VALID_ALL_CONFIG_FILE_RESOURCE, true).getPath();
      String propsPath = ConfigManager.getSystemResourceURL(
          LDAPPC_EMPTY_PROPERTY_RESOURCE, true).getPath();
      ConfigManager cm = new ConfigManager(configPath, propsPath);

      //
      // Once loaded, validate all of the data
      //

      // validate stem queries
      Set stemSet = cm.getGroupSubordinateStemQueries();
      assertEquals(stemSet.size(), 2);
      assertEquals(stemSet.contains("stem1"), true);
      assertEquals(stemSet.contains("stem2"), true);

      // validate attribute matching queries
      Map attrMap = cm.getGroupAttrMatchingQueries();
      assertEquals(attrMap.size(), 2);
      Set set = (Set) attrMap.get("attribute1");
      assertEquals(1, set.size());
      assertEquals(true, set.contains("attributeValue1"));
      set = (Set) attrMap.get("attribute2");
      assertEquals(1, set.size());
      assertEquals(true, set.contains("attributeValue2"));

      // validate group dn structure
      assertEquals(cm.getGroupDnStructure(), GroupDNStructure.flat);
      assertEquals(cm.getGroupDnRoot(), "ou=root");
      assertEquals(cm.getGroupDnObjectClass(), "gLdapObjectClass");
      assertEquals(cm.getGroupDnRdnAttribute(), "gLdapRdnAttribute");
      assertEquals(cm.getGroupDnGrouperAttribute(), "id");

      // validate group members dn list
      assertEquals(cm.isGroupMembersDnListed(), true);
      assertEquals(cm.getGroupMembersDnListObjectClass(), "gmdlObjectClass");
      assertEquals(cm.getGroupMembersDnListAttribute(), "gmdlAttribute");
      assertEquals(cm.getGroupMembersDnListEmptyValue(), "gmdlListEmpty");

      // validate group members name list
      assertEquals(cm.isGroupMembersNameListed(), true);
      assertEquals(cm.getGroupMembersNameListObjectClass(), "gmnlObjectClass");
      assertEquals(cm.getGroupMembersNameListAttribute(), "gmnlAttribute");
      assertEquals(cm.getGroupMembersNameListEmptyValue(), "gmnlListEmpty");

      Map groupMbrsNameMap = cm.getGroupMembersNameListNamingAttributes();
      assertEquals(groupMbrsNameMap.size(), 2);
      assertEquals(groupMbrsNameMap.get("source1"), "subjectAttribute1");
      assertEquals(groupMbrsNameMap.get("source2"), "subjectAttribute2");

      // validate group attribute mapping
      assertEquals(cm.getGroupAttributeMappingObjectClass(), new HashSet<String>(Arrays
          .asList(new String[] { "gamObjectClass" })));

      Map<String, List<String>> groupAttributeMap = cm.getGroupAttributeMapping();
      assertEquals(groupAttributeMap.size(), 4);
      assertEquals(Arrays.asList(new String[] { "gamLdapAttribute1" }), groupAttributeMap
          .get("gamGroupAttribute1"));
      assertEquals(Arrays.asList(new String[] { "gamLdapAttribute1" }), groupAttributeMap
          .get("gamGroupAttribute1"));
      assertEquals(Arrays.asList(new String[] { "gamLDAPAttribute1" }), groupAttributeMap
          .get("gamGroupAttribute3"));
      assertEquals(Arrays.asList(new String[] { "gamLdapAttribute4" }), groupAttributeMap
          .get("gamGroupAttribute4"));

      assertEquals(cm.getGroupAttributeMappingLdapEmptyValue(groupAttributeMap.get(
          "gamGroupAttribute1").get(0)), cm
          .getGroupAttributeMappingLdapEmptyValue(groupAttributeMap.get(
              "gamGroupAttribute3").get(0)));

      HashSet values = new HashSet();
      values.add("gamLdapEmpty1");
      values.add("gamLdapEmpty3");
      assertEquals(true, values.contains(cm
          .getGroupAttributeMappingLdapEmptyValue(groupAttributeMap.get(
              "gamGroupAttribute1").get(0))));

      assertEquals(cm.getGroupAttributeMappingLdapEmptyValue(groupAttributeMap.get(
          "gamGroupAttribute2").get(0)), "");
      assertEquals(cm.getGroupAttributeMappingLdapEmptyValue(groupAttributeMap.get(
          "gamGroupAttribute4").get(0)), "gamLdapEmpty4");

      // validate member groups list
      assertEquals(cm.isMemberGroupsListed(), true);
      assertEquals(cm.getMemberGroupsListObjectClass(), "mglObjectClass");
      assertEquals(cm.getMemberGroupsListAttribute(), "mglListAttribute");
      assertEquals(cm.getMemberGroupsNamingAttribute(), "mglNamingAttribute");
      // assertEquals(cm.getMemberGroupsListEmptyValue(), "mglListEmpty");

      // validate source subject identifiers
      Map srcSubjAttrs = cm.getSourceSubjectNamingAttributes();
      assertEquals(srcSubjAttrs.size(), 2);
      assertEquals(cm.getSourceSubjectNamingAttribute("source1"), "subjectAttr1");
      assertEquals(cm.getSourceSubjectNamingAttribute("source2"), "subjectAttr2");

      Map srcSubjFilters = cm.getSourceSubjectLdapFilters();
      assertEquals(srcSubjFilters.size(), 2);

      LdapSearchFilter filter = cm.getSourceSubjectLdapFilter("source1");
      assertEquals(filter.getBase(), "ldapSearchBase1");
      assertEquals(filter.getScope(), SearchControls.OBJECT_SCOPE);
      assertEquals(filter.getFilter(), "ldapSearchFilter1");

      filter = cm.getSourceSubjectLdapFilter("source2");
      assertEquals(filter.getBase(), "ldapSearchBase2");
      assertEquals(filter.getScope(), SearchControls.SUBTREE_SCOPE);
      assertEquals(filter.getFilter(), "ldapSearchFilter2");

    } catch (Exception e) {
      e.printStackTrace();
      fail("Test failed : " + e.getMessage());
    }
  }

  /**
   * Test reading in a valid configuration file without any optional attributes
   */
  public void testValidNoOptionalAttributesFile() {

    try {
      //
      // Get the configuration manager and validate all of the entries
      //
      String configPath = ConfigManager.getSystemResourceURL(
          VALID_NO_OPTIONAL_ATTRIBUTES_CONFIG_FILE_RESOURCE, true).getPath();
      String propsPath = ConfigManager.getSystemResourceURL(
          LDAPPC_EMPTY_PROPERTY_RESOURCE, true).getPath();
      ConfigManager cm = new ConfigManager(configPath, propsPath);

      //
      // Once loaded, validate all of the data
      //

      // validate stem queries
      Set stemSet = cm.getGroupSubordinateStemQueries();
      assertEquals(stemSet.size(), 2);
      assertEquals(stemSet.contains("stem1"), true);
      assertEquals(stemSet.contains("stem2"), true);

      // validate attribute matching queries
      Map attrMap = cm.getGroupAttrMatchingQueries();
      assertEquals(attrMap.size(), 2);
      Set set = (Set) attrMap.get("attribute1");
      assertEquals(1, set.size());
      assertEquals(true, set.contains("attributeValue1"));
      set = (Set) attrMap.get("attribute2");
      assertEquals(1, set.size());
      assertEquals(true, set.contains("attributeValue2"));

      // validate group dn structure
      assertEquals(cm.getGroupDnStructure(), GroupDNStructure.flat);
      assertEquals(cm.getGroupDnRoot(), "ou=root");
      assertEquals(cm.getGroupDnObjectClass(), "gLdapObjectClass");
      assertEquals(cm.getGroupDnRdnAttribute(), "gLdapRdnAttribute");
      assertEquals(cm.getGroupDnGrouperAttribute(), "id");

      // validate group members dn list
      assertEquals(cm.isGroupMembersDnListed(), true);
      assertEquals(cm.getGroupMembersDnListObjectClass(), null);
      assertEquals(cm.getGroupMembersDnListAttribute(), "gmdlAttribute");
      assertEquals(cm.getGroupMembersDnListEmptyValue(), null);

      // validate group members name list
      assertEquals(cm.isGroupMembersNameListed(), true);
      assertEquals(cm.getGroupMembersNameListObjectClass(), null);
      assertEquals(cm.getGroupMembersNameListAttribute(), "gmnlAttribute");
      assertEquals(cm.getGroupMembersNameListEmptyValue(), null);

      Map groupMbrsNameMap = cm.getGroupMembersNameListNamingAttributes();
      assertEquals(groupMbrsNameMap.size(), 2);
      assertEquals(groupMbrsNameMap.get("source1"), "subjectAttribute1");
      assertEquals(groupMbrsNameMap.get("source2"), "subjectAttribute2");

      // validate group attribute mapping
      assertEquals(cm.getGroupAttributeMappingObjectClass(), null);

      Map<String, List<String>> groupAttributeMap = cm.getGroupAttributeMapping();
      assertEquals(groupAttributeMap.size(), 2);
      assertEquals(Arrays.asList(new String[] { "gamLdapAttribute1" }), groupAttributeMap
          .get("gamGroupAttribute1"));
      assertEquals(Arrays.asList(new String[] { "gamLdapAttribute2" }), groupAttributeMap
          .get("gamGroupAttribute2"));

      assertEquals(cm.getGroupAttributeMappingLdapEmptyValue(groupAttributeMap.get(
          "gamGroupAttribute1").get(0)), null);
      assertEquals(cm.getGroupAttributeMappingLdapEmptyValue(groupAttributeMap.get(
          "gamGroupAttribute2").get(0)), null);

      // validate member groups list
      assertEquals(cm.isMemberGroupsListed(), true);
      assertEquals(cm.getMemberGroupsListObjectClass(), null);
      assertEquals(cm.getMemberGroupsListAttribute(), "mglListAttribute");
      assertEquals(cm.getMemberGroupsNamingAttribute(), "mglNamingAttribute");
      // assertEquals(cm.getMemberGroupsListEmptyValue(), null);

      // validate source subject identifiers
      Map srcSubjAttrs = cm.getSourceSubjectNamingAttributes();
      assertEquals(srcSubjAttrs.size(), 2);
      assertEquals(cm.getSourceSubjectNamingAttribute("source1"), "subjectAttr1");
      assertEquals(cm.getSourceSubjectNamingAttribute("source2"), "subjectAttr2");

      Map srcSubjFilters = cm.getSourceSubjectLdapFilters();
      assertEquals(srcSubjFilters.size(), 2);

      LdapSearchFilter filter = cm.getSourceSubjectLdapFilter("source1");
      assertEquals(filter.getBase(), "ldapSearchBase1");
      assertEquals(filter.getScope(), SearchControls.OBJECT_SCOPE);
      assertEquals(filter.getFilter(), "ldapSearchFilter1");

      filter = cm.getSourceSubjectLdapFilter("source2");
      assertEquals(filter.getBase(), "ldapSearchBase2");
      assertEquals(filter.getScope(), SearchControls.SUBTREE_SCOPE);
      assertEquals(filter.getFilter(), "ldapSearchFilter2");

    } catch (Exception e) {
      fail("Test failed : " + e.getMessage());
    }
  }

  /**
   * Test reading in a valid configuration file with a minimal Grouper group configuration
   */
  public void testValidGrouperGroupMinimal() {

    try {
      //
      // Get the configuration manager and validate all of the entries
      //
      String configPath = ConfigManager.getSystemResourceURL(
          VALID_GROUPER_GROUP_MINIMAL_CONFIG_FILE_RESOURCE, true).getPath();
      String propsPath = ConfigManager.getSystemResourceURL(
          LDAPPC_EMPTY_PROPERTY_RESOURCE, true).getPath();
      ConfigManager cm = new ConfigManager(configPath, propsPath);

      //
      // Once loaded, validate all of the data
      //

      // validate stem queries
      Set stemSet = cm.getGroupSubordinateStemQueries();
      assertEquals(stemSet.size(), 0);

      // validate attribute matching queries
      Map attrMap = cm.getGroupAttrMatchingQueries();
      assertEquals(attrMap.size(), 2);
      Set set = (Set) attrMap.get("attribute1");
      assertEquals(1, set.size());
      assertEquals(true, set.contains("attributeValue1"));
      set = (Set) attrMap.get("attribute2");
      assertEquals(1, set.size());
      assertEquals(true, set.contains("attributeValue2"));

      // validate group dn structure
      assertEquals(cm.getGroupDnStructure(), GroupDNStructure.flat);
      assertEquals(cm.getGroupDnRoot(), "ou=root");
      assertEquals(cm.getGroupDnObjectClass(), "gLdapObjectClass");
      assertEquals(cm.getGroupDnRdnAttribute(), "gLdapRdnAttribute");
      assertEquals(cm.getGroupDnGrouperAttribute(), "id");

      // validate group members dn list
      assertEquals(cm.isGroupMembersDnListed(), true);
      assertEquals(cm.getGroupMembersDnListObjectClass(), null);
      assertEquals(cm.getGroupMembersDnListAttribute(), "gmdlAttribute");

      // validate group members name list
      assertEquals(cm.isGroupMembersNameListed(), false);

      // validate group attribute mapping
      assertEquals(cm.getGroupAttributeMappingObjectClass(), null);

      Map groupAttributeMap = cm.getGroupAttributeMapping();
      assertEquals(groupAttributeMap.size(), 0);

      // validate source subject identifiers
      Map srcSubjAttrs = cm.getSourceSubjectNamingAttributes();
      assertEquals(srcSubjAttrs.size(), 2);
      assertEquals(cm.getSourceSubjectNamingAttribute("source1"), "subjectAttr1");
      assertEquals(cm.getSourceSubjectNamingAttribute("source2"), "subjectAttr2");

      Map srcSubjFilters = cm.getSourceSubjectLdapFilters();
      assertEquals(srcSubjFilters.size(), 2);

      LdapSearchFilter filter = cm.getSourceSubjectLdapFilter("source1");
      assertEquals(filter.getBase(), "ldapSearchBase1");
      assertEquals(filter.getScope(), SearchControls.OBJECT_SCOPE);
      assertEquals(filter.getFilter(), "ldapSearchFilter1");

      filter = cm.getSourceSubjectLdapFilter("source2");
      assertEquals(filter.getBase(), "ldapSearchBase2");
      assertEquals(filter.getScope(), SearchControls.SUBTREE_SCOPE);
      assertEquals(filter.getFilter(), "ldapSearchFilter2");

    } catch (Exception e) {
      fail("Test failed : " + e.getMessage());
    }
  }

  /**
   * Test reading in a valid configuration file with minimal Grouper membership
   * configuration
   */
  public void testValidGrouperMembershipMinimalFile() {

    try {
      //
      // Get the configuration manager and validate all of the entries
      //
      String configPath = ConfigManager.getSystemResourceURL(
          VALID_GROUPER_MEMBERSHIP_MINIMAL_CONFIG_FILE_RESOURCE, true).getPath();
      String propsPath = ConfigManager.getSystemResourceURL(
          LDAPPC_EMPTY_PROPERTY_RESOURCE, true).getPath();
      ConfigManager cm = new ConfigManager(configPath, propsPath);

      //
      // Once loaded, validate all of the data
      //

      // validate stem queries
      Set stemSet = cm.getGroupSubordinateStemQueries();
      assertEquals(stemSet.size(), 0);

      // validate attribute matching queries
      Map attrMap = cm.getGroupAttrMatchingQueries();
      assertEquals(attrMap.size(), 2);
      Set set = (Set) attrMap.get("attribute1");
      assertEquals(1, set.size());
      assertEquals(true, set.contains("attributeValue1"));
      set = (Set) attrMap.get("attribute2");
      assertEquals(1, set.size());
      assertEquals(true, set.contains("attributeValue2"));

      // validate group members dn list
      assertEquals(cm.isGroupMembersDnListed(), false);

      // validate group members name list
      assertEquals(cm.isGroupMembersNameListed(), false);

      // validate member groups list
      assertEquals(cm.isMemberGroupsListed(), true);
      assertEquals(cm.getMemberGroupsListObjectClass(), null);
      assertEquals(cm.getMemberGroupsListAttribute(), "mglListAttribute");
      assertEquals(cm.getMemberGroupsNamingAttribute(), "mglNamingAttribute");

      // validate source subject identifiers
      Map srcSubjAttrs = cm.getSourceSubjectNamingAttributes();
      assertEquals(srcSubjAttrs.size(), 2);
      assertEquals(cm.getSourceSubjectNamingAttribute("source1"), "subjectAttr1");
      assertEquals(cm.getSourceSubjectNamingAttribute("source2"), "subjectAttr2");

      Map srcSubjFilters = cm.getSourceSubjectLdapFilters();
      assertEquals(srcSubjFilters.size(), 2);

      LdapSearchFilter filter = cm.getSourceSubjectLdapFilter("source1");
      assertEquals(filter.getBase(), "ldapSearchBase1");
      assertEquals(filter.getScope(), SearchControls.OBJECT_SCOPE);
      assertEquals(filter.getFilter(), "ldapSearchFilter1");

      filter = cm.getSourceSubjectLdapFilter("source2");
      assertEquals(filter.getBase(), "ldapSearchBase2");
      assertEquals(filter.getScope(), SearchControls.SUBTREE_SCOPE);
      assertEquals(filter.getFilter(), "ldapSearchFilter2");

    } catch (Exception e) {
      fail("Test failed : " + e.getMessage());
    }
  }

  /**
   * Test reading in an invalid configuration file that is missing an ldappc element. This
   * test is inactive to avoid an error message in the test case results output that might
   * mislead the user into thinking that a test case has failed, when this test should
   * create an error message in the log. This test was made and verified to work prior to
   * the initial release of the code.
   */
  public void inactiveTestInvalidMissingLdappcElementFile() {

    try {
      //
      // Get the configuration manager and validate all of the entries
      //
      String configPath = ConfigManager.getSystemResourceURL(
          INVALID_NO_LDAPPC_ELEMENT_CONFIG_FILE_RESOURCE, true).getPath();
      String propsPath = ConfigManager.getSystemResourceURL(
          LDAPPC_EMPTY_PROPERTY_RESOURCE, true).getPath();
      new ConfigManager(configPath, propsPath);
      fail("Test failed : File without ldappc element parsed without error.");
    } catch (ConfigurationException lce) {
      assertTrue(lce.getCause() == null);
    } catch (Exception e) {
      fail("Test failed : Unexpected exception " + e.getClass().getName() + " :: "
          + e.getMessage());
    }
  }

  public void testProvisionMemberGroupsGGSASource() {

    try {
      String configPath = ConfigManager.getSystemResourceURL(
          PROVISION_MEMBER_GROUPS_CONFIG_FILE_RESOURCE, true).getPath();
      String propsPath = ConfigManager.getSystemResourceURL(
          LDAPPC_EMPTY_PROPERTY_RESOURCE, true).getPath();
      new ConfigManager(configPath, propsPath);
      fail("Should throw ConfigurationException.");
    } catch (ConfigurationException e) {
      // ok
    }
  }

  public void testPropertyReplacement() throws FileNotFoundException, IOException {

    String configPath = ConfigManager.getSystemResourceURL(
        PROPERTY_REPLACEMENT_CONFIG_FILE_RESOURCE, true).getPath();
    String propsPath = ConfigManager.getSystemResourceURL(
        PROPERTY_REPLACEMENT_PROPERTY_FILE_RESOURCE, true).getPath();

    Properties properties = new Properties();
    properties.load(new FileInputStream(new File(propsPath)));

    ConfigManager cm = new ConfigManager(configPath, propsPath);

    assertEquals(properties.get("groupDnRoot"), cm.getGroupDnRoot());
  }

  public void testLdapSearchFilter() throws FileNotFoundException, IOException {

    String configPath = ConfigManager.getSystemResourceURL(
        VALID_LDAPSEARCHFILTER_CONFIG_FILE_RESOURCE, true).getPath();
    String propsPath = ConfigManager.getSystemResourceURL(
        PROPERTY_REPLACEMENT_PROPERTY_FILE_RESOURCE, true).getPath();

    Properties properties = new Properties();
    properties.load(new FileInputStream(new File(propsPath)));

    ConfigManager cm = new ConfigManager(configPath, propsPath);

    assertTrue(cm.getSourceSubjectLdapFilters().get("source1").getMultipleResults());
    assertEquals(OnNotFound.fail, cm.getSourceSubjectLdapFilters().get("source1")
        .getOnNotFound());
  }

  public void testLdapSearchFilterDefaults() throws FileNotFoundException, IOException {

    String configPath = ConfigManager.getSystemResourceURL(
        PROPERTY_REPLACEMENT_CONFIG_FILE_RESOURCE, true).getPath();
    String propsPath = ConfigManager.getSystemResourceURL(
        PROPERTY_REPLACEMENT_PROPERTY_FILE_RESOURCE, true).getPath();

    Properties properties = new Properties();
    properties.load(new FileInputStream(new File(propsPath)));

    ConfigManager cm = new ConfigManager(configPath, propsPath);

    assertFalse(cm.getSourceSubjectLdapFilters().get("source1").getMultipleResults());
    assertEquals(OnNotFound.warn, cm.getSourceSubjectLdapFilters().get("source1")
        .getOnNotFound());
  }
}
