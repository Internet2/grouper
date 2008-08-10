/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package edu.internet2.middleware.ldappcTest.configuration;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.directory.SearchControls;

import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.LdappcConfigurationException;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappcTest.BaseTestCase;
import edu.internet2.middleware.ldappcTest.DisplayTest;

/**
 * This set of tests validates the
 * {@link edu.internet2.middleware.ldappc.ConfigManager}.
 */
public class ConfigManagerTest extends BaseTestCase
{
    /**
     * Relative configuration resource path
     */
    public static String RELATIVE_RESOURCE_PATH = "edu/internet2/middleware/ldappcTest/configuration/data/";

    /**
     * Valid configuration file resource with all elements and attributes
     */
    public static String VALID_ALL_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
            + "ldappcValidAll.xml";

    /**
     * Valid configuration file resource with all elements and no optional
     * attributes
     */
    public static String VALID_NO_OPTIONAL_ATTRIBUTES_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
            + "ldappcValidNoOptionalAttributes.xml";

    /**
     * Valid configuration file resource with a minimal Grouper group
     * configuration
     */
    public static String VALID_GROUPER_GROUP_MINIMAL_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
            + "ldappcValidGrouperGroupMinimal.xml";

    /**
     * Valid configuration file resource with a minimal Grouper membership
     * configuration
     */
    public static String VALID_GROUPER_MEMBERSHIP_MINIMAL_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
            + "ldappcValidGrouperMembershipMinimal.xml";

    /**
     * Valid configuration file resource with the minimal Signet configuration
     */
    public static String VALID_SIGNET_MINIMAL_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
            + "ldappcValidSignetMinimal.xml";

    /**
     * Invalid configuration file resource that is missing the ldappc element
     */
    public static String INVALID_NO_LDAPPC_ELEMENT_CONFIG_FILE_RESOURCE = RELATIVE_RESOURCE_PATH
            + "ldappcInvalidNoLdappcElement.xml";

    /**
     * Class constructor
     * 
     * @param name
     *            Name of the test case.
     */
    public ConfigManagerTest(String name)
    {
        super(name);
    }

    /**
     * Set up the fixture.
     */
    protected void setUp()
    {
        DisplayTest.showRunClass(getClass().getName());
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown()
    {
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[])
    {
        BaseTestCase.runTestRunner(ConfigManagerTest.class);
    }

    /**
     * Test reading in a valid configuration file
     */
    public void testValidAllFile()
    {
        DisplayTest
                .showRunTitle("testValidAllFile",
                        "Process a Valid Configuration File with all elements and attributes");

        try
        {
            //
            // Get the configuration manager and validate all of the entries
            //
            ConfigManager cm = ConfigManager.load(ConfigManager.getSystemResourceURL(VALID_ALL_CONFIG_FILE_RESOURCE, true).toString());
//            ConfigManager cm = ConfigManager.load(ConfigManager.getSystemResourceURL(VALID_ALL_CONFIG_FILE_RESOURCE, true).to);

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
            assertEquals(cm.getGroupDnStructure(), "flat");
            assertEquals(cm.getGroupDnRoot(), "ou=root");
            assertEquals(cm.getGroupDnObjectClass(), "gLdapObjectClass");
            assertEquals(cm.getGroupDnRdnAttribute(), "gLdapRdnAttribute");
            assertEquals(cm.getGroupDnGrouperAttribute(), "id");

            // validate group members dn list
            assertEquals(cm.isGroupMembersDnListed(), true);
            assertEquals(cm.getGroupMembersDnListObjectClass(),
                    "gmdlObjectClass");
            assertEquals(cm.getGroupMembersDnListAttribute(), "gmdlAttribute");
            assertEquals(cm.getGroupMembersDnListEmptyValue(), "gmdlListEmpty");

            // validate group members name list
            assertEquals(cm.isGroupMembersNameListed(), true);
            assertEquals(cm.getGroupMembersNameListObjectClass(),
                    "gmnlObjectClass");
            assertEquals(cm.getGroupMembersNameListAttribute(), "gmnlAttribute");
            assertEquals(cm.getGroupMembersNameListEmptyValue(),
                    "gmnlListEmpty");

            Map groupMbrsNameMap = cm.getGroupMembersNameListNamingAttributes();
            assertEquals(groupMbrsNameMap.size(), 2);
            assertEquals(groupMbrsNameMap.get("source1"), "subjectAttribute1");
            assertEquals(groupMbrsNameMap.get("source2"), "subjectAttribute2");

            // validate group attribute mapping
            assertEquals(cm.getGroupAttributeMappingObjectClass(),
                    "gamObjectClass");

            Map groupAttributeMap = cm.getGroupAttributeMapping();
            assertEquals(groupAttributeMap.size(), 4);
            assertEquals(groupAttributeMap.get("gamGroupAttribute1"),
                    "gamLdapAttribute1");
            assertEquals(groupAttributeMap.get("gamGroupAttribute2"),
                    "gamLdapAttribute2");
            assertEquals(groupAttributeMap.get("gamGroupAttribute3"),
                    "gamLDAPAttribute1");
            assertEquals(groupAttributeMap.get("gamGroupAttribute4"),
                    "gamLdapAttribute4");

            assertEquals(
                    cm
                            .getGroupAttributeMappingLdapEmptyValue((String) groupAttributeMap
                                    .get("gamGroupAttribute1")),
                    cm
                            .getGroupAttributeMappingLdapEmptyValue((String) groupAttributeMap

                            .get("gamGroupAttribute3")));

            HashSet values = new HashSet();
            values.add("gamLdapEmpty1");
            values.add("gamLdapEmpty3");
            assertEquals(
                    true,
                    values
                            .contains(cm
                                    .getGroupAttributeMappingLdapEmptyValue((String) groupAttributeMap
                                            .get("gamGroupAttribute1"))));

            assertEquals(
                    cm
                            .getGroupAttributeMappingLdapEmptyValue((String) groupAttributeMap
                                    .get("gamGroupAttribute2")), "");
            assertEquals(
                    cm
                            .getGroupAttributeMappingLdapEmptyValue((String) groupAttributeMap
                                    .get("gamGroupAttribute4")),
                    "gamLdapEmpty4");

            // validate member groups list
            assertEquals(cm.isMemberGroupsListed(), true);
            assertEquals(cm.getMemberGroupsListObjectClass(), "mglObjectClass");
            assertEquals(cm.getMemberGroupsListAttribute(), "mglListAttribute");
            assertEquals(cm.getMemberGroupsNamingAttribute(),
                    "mglNamingAttribute");
            assertEquals(cm.getMemberGroupsListEmptyValue(), "mglListEmpty");

            // validate permissions listing
            assertEquals(cm.getPermissionsListingStoredAs(), "string");
            assertEquals(cm.getPermissionsListingStringObjectClass(),
                    "stringObjectClass");
            assertEquals(cm.getPermissionsListingStringAttribute(),
                    "stringAttribute");
            assertEquals(cm.getPermissionsListingStringPrefix(), "stringPrefix");
            assertEquals(cm.getPermissionsListingStringEmptyValue(),
                    "stringEmptyValue");

            // validate permissions subsystem queries
            Set permissionSubsystems = cm.getPermissionsSubsystemQueries();
            assertEquals(permissionSubsystems.size(), 3);
            assertEquals(permissionSubsystems.contains("subsystem1"), true);
            assertEquals(permissionSubsystems.contains("subsystem2"), true);
            assertEquals(permissionSubsystems.contains("subsystem3"), true);

            // validate permissions function queries
            Set permissionFunctions = cm.getPermissionsFunctionQueries();
            assertEquals(permissionFunctions.size(), 2);
            assertEquals(permissionFunctions.contains("function1"), true);
            assertEquals(permissionFunctions.contains("function2"), true);

            // validate source subject identifiers
            Map srcSubjAttrs = cm.getSourceSubjectNamingAttributes();
            assertEquals(srcSubjAttrs.size(), 2);
            assertEquals(cm.getSourceSubjectNamingAttribute("source1"),
                    "subjectAttr1");
            assertEquals(cm.getSourceSubjectNamingAttribute("source2"),
                    "subjectAttr2");

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

            // validate ldap initial context parameters
            Hashtable data = cm.getLdapContextParameters();
            assertEquals((String) data.get(Context.INITIAL_CONTEXT_FACTORY),
                    "initContextFactory");
            assertEquals((String) data.get(Context.PROVIDER_URL), "providerUrl");
            assertEquals((String) data.get(Context.SECURITY_AUTHENTICATION),
                    "securityAuthentication");
            assertEquals((String) data.get(Context.SECURITY_PRINCIPAL),
                    "securityPrincipal");
            assertEquals((String) data.get(Context.SECURITY_CREDENTIALS),
                    "securityCredentials");

        }
        catch(Exception e)
        {
            e.printStackTrace();
            fail("Test failed : " + e.getMessage());
        }
    }

    /**
     * Test reading in a valid configuration file without any optional
     * attributes
     */
    public void testValidNoOptionalAttributesFile()
    {
        DisplayTest
                .showRunTitle("testValidNoOptionalAttributesFile",
                        "Process a Valid Configuration File without optional attributes");

        try
        {
            //
            // Get the configuration manager and validate all of the entries
            //
            ConfigManager cm = ConfigManager.load(ConfigManager.getSystemResourceURL(VALID_NO_OPTIONAL_ATTRIBUTES_CONFIG_FILE_RESOURCE, true).toString());
//            ConfigManager cm = ConfigManager.load(VALID_NO_OPTIONAL_ATTRIBUTES_CONFIG_FILE_RESOURCE);

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
            assertEquals(cm.getGroupDnStructure(), "flat");
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

            Map groupAttributeMap = cm.getGroupAttributeMapping();
            assertEquals(groupAttributeMap.size(), 2);
            assertEquals(groupAttributeMap.get("gamGroupAttribute1"),
                    "gamLdapAttribute1");
            assertEquals(groupAttributeMap.get("gamGroupAttribute2"),
                    "gamLdapAttribute2");

            assertEquals(
                    cm
                            .getGroupAttributeMappingLdapEmptyValue((String) groupAttributeMap
                                    .get("gamGroupAttribute1")), null);
            assertEquals(
                    cm
                            .getGroupAttributeMappingLdapEmptyValue((String) groupAttributeMap
                                    .get("gamGroupAttribute2")), null);

            // validate member groups list
            assertEquals(cm.isMemberGroupsListed(), true);
            assertEquals(cm.getMemberGroupsListObjectClass(), null);
            assertEquals(cm.getMemberGroupsListAttribute(), "mglListAttribute");
            assertEquals(cm.getMemberGroupsNamingAttribute(),
                    "mglNamingAttribute");
            assertEquals(cm.getMemberGroupsListEmptyValue(), null);

            // validate permissions listing
            assertEquals(cm.getPermissionsListingStoredAs(), "string");
            assertEquals(cm.getPermissionsListingStringObjectClass(), null);
            assertEquals(cm.getPermissionsListingStringAttribute(),
                    "stringAttribute");
            assertEquals(cm.getPermissionsListingStringPrefix(), "stringPrefix");
            assertEquals(cm.getPermissionsListingStringEmptyValue(), null);

            // validate permissions subsystem queries
            Set permissionSubsystems = cm.getPermissionsSubsystemQueries();
            assertEquals(permissionSubsystems.size(), 3);
            assertEquals(permissionSubsystems.contains("subsystem1"), true);
            assertEquals(permissionSubsystems.contains("subsystem2"), true);
            assertEquals(permissionSubsystems.contains("subsystem3"), true);

            // validate permissions function queries
            Set permissionFunctions = cm.getPermissionsFunctionQueries();
            assertEquals(permissionFunctions.size(), 2);
            assertEquals(permissionFunctions.contains("function1"), true);
            assertEquals(permissionFunctions.contains("function2"), true);

            // validate source subject identifiers
            Map srcSubjAttrs = cm.getSourceSubjectNamingAttributes();
            assertEquals(srcSubjAttrs.size(), 2);
            assertEquals(cm.getSourceSubjectNamingAttribute("source1"),
                    "subjectAttr1");
            assertEquals(cm.getSourceSubjectNamingAttribute("source2"),
                    "subjectAttr2");

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

            // validate ldap initial context parameters
            Hashtable data = cm.getLdapContextParameters();
            assertEquals((String) data.get(Context.INITIAL_CONTEXT_FACTORY),
                    "initContextFactory");
            assertEquals((String) data.get(Context.PROVIDER_URL), "providerUrl");
            assertEquals((String) data.get(Context.SECURITY_AUTHENTICATION),
                    "securityAuthentication");
            assertEquals((String) data.get(Context.SECURITY_PRINCIPAL),
                    "securityPrincipal");
            assertEquals((String) data.get(Context.SECURITY_CREDENTIALS),
                    "securityCredentials");

        }
        catch(Exception e)
        {
            fail("Test failed : " + e.getMessage());
        }
    }

    /**
     * Test reading in a valid configuration file with a minimal Grouper group
     * configuration
     */
    public void testValidGrouperGroupMinimal()
    {
        DisplayTest
                .showRunTitle("testValidGrouperGroupMinimal",
                        "Process a Valid Configuration File with minimal Grouper group elements");

        try
        {
            //
            // Get the configuration manager and validate all of the entries
            //
            ConfigManager cm = ConfigManager.load(ConfigManager.getSystemResourceURL(VALID_GROUPER_GROUP_MINIMAL_CONFIG_FILE_RESOURCE, true).toString());
//            ConfigManager cm = ConfigManager.load(VALID_GROUPER_GROUP_MINIMAL_CONFIG_FILE_RESOURCE);

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
            assertEquals(cm.getGroupDnStructure(), "flat");
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
            assertEquals(cm.getSourceSubjectNamingAttribute("source1"),
                    "subjectAttr1");
            assertEquals(cm.getSourceSubjectNamingAttribute("source2"),
                    "subjectAttr2");

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

            // validate ldap initial context parameters
            Hashtable data = cm.getLdapContextParameters();
            assertEquals((String) data.get(Context.INITIAL_CONTEXT_FACTORY),
                    "initContextFactory");
            assertEquals((String) data.get(Context.PROVIDER_URL), "providerUrl");
            assertEquals((String) data.get(Context.SECURITY_AUTHENTICATION),
                    "securityAuthentication");
            assertEquals((String) data.get(Context.SECURITY_PRINCIPAL),
                    "securityPrincipal");
            assertEquals((String) data.get(Context.SECURITY_CREDENTIALS),
                    "securityCredentials");

        }
        catch(Exception e)
        {
            fail("Test failed : " + e.getMessage());
        }
    }

    /**
     * Test reading in a valid configuration file with the minimal Signet
     * configuration
     */
    public void testValidSignetMinimal()
    {
        DisplayTest
                .showRunTitle("testValidSignetMinimal",
                        "Process a Valid Configuration File with a minimal Signet configuration");

        try
        {
            //
            // Get the configuration manager and validate all of the entries
            //
            ConfigManager cm = ConfigManager.load(ConfigManager.getSystemResourceURL(VALID_SIGNET_MINIMAL_CONFIG_FILE_RESOURCE, true).toString());
//            ConfigManager cm = ConfigManager.load(VALID_SIGNET_MINIMAL_CONFIG_FILE_RESOURCE);

            //
            // Once loaded, validate all of the data
            //
            // validate permissions listing
            assertEquals(cm.getPermissionsListingStoredAs(), "string");
            assertEquals(cm.getPermissionsListingStringObjectClass(), null);
            assertEquals(cm.getPermissionsListingStringAttribute(),
                    "stringAttribute");
            assertEquals(cm.getPermissionsListingStringPrefix(), "stringPrefix");

            // validate permissions subsystem queries
            Set permissionSubsystems = cm.getPermissionsSubsystemQueries();
            assertEquals(permissionSubsystems.size(), 0);

            // validate permissions function queries
            Set permissionFunctions = cm.getPermissionsFunctionQueries();
            assertEquals(permissionFunctions.size(), 0);

            // validate source subject identifiers
            Map srcSubjAttrs = cm.getSourceSubjectNamingAttributes();
            assertEquals(srcSubjAttrs.size(), 2);
            assertEquals(cm.getSourceSubjectNamingAttribute("source1"),
                    "subjectAttr1");
            assertEquals(cm.getSourceSubjectNamingAttribute("source2"),
                    "subjectAttr2");

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

            // validate ldap initial context parameters
            Hashtable data = cm.getLdapContextParameters();
            assertEquals((String) data.get(Context.INITIAL_CONTEXT_FACTORY),
                    "initContextFactory");
            assertEquals((String) data.get(Context.PROVIDER_URL), "providerUrl");
            assertEquals((String) data.get(Context.SECURITY_AUTHENTICATION),
                    "securityAuthentication");
            assertEquals((String) data.get(Context.SECURITY_PRINCIPAL),
                    "securityPrincipal");
            assertEquals((String) data.get(Context.SECURITY_CREDENTIALS),
                    "securityCredentials");

        }
        catch(Exception e)
        {
            fail("Test failed : " + e.getMessage());
        }
    }

    /**
     * Test reading in a valid configuration file with minimal Grouper
     * membership configuration
     */
    public void testValidGrouperMembershipMinimalFile()
    {
        DisplayTest
                .showRunTitle(
                        "testValidGrouperMembershipMinimalFile",
                        "Process a Valid Configuration File with minimal Grouper membership configuration");

        try
        {
            //
            // Get the configuration manager and validate all of the entries
            //
            ConfigManager cm = ConfigManager.load(ConfigManager.getSystemResourceURL(VALID_GROUPER_MEMBERSHIP_MINIMAL_CONFIG_FILE_RESOURCE, true).toString());
//            ConfigManager cm = ConfigManager.load(VALID_GROUPER_MEMBERSHIP_MINIMAL_CONFIG_FILE_RESOURCE);

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
            assertEquals(cm.getMemberGroupsNamingAttribute(),
                    "mglNamingAttribute");

            // validate source subject identifiers
            Map srcSubjAttrs = cm.getSourceSubjectNamingAttributes();
            assertEquals(srcSubjAttrs.size(), 2);
            assertEquals(cm.getSourceSubjectNamingAttribute("source1"),
                    "subjectAttr1");
            assertEquals(cm.getSourceSubjectNamingAttribute("source2"),
                    "subjectAttr2");

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

            // validate ldap initial context parameters
            Hashtable data = cm.getLdapContextParameters();
            assertEquals((String) data.get(Context.INITIAL_CONTEXT_FACTORY),
                    "initContextFactory");
            assertEquals((String) data.get(Context.PROVIDER_URL), "providerUrl");
            assertEquals((String) data.get(Context.SECURITY_AUTHENTICATION),
                    "securityAuthentication");
            assertEquals((String) data.get(Context.SECURITY_PRINCIPAL),
                    "securityPrincipal");
            assertEquals((String) data.get(Context.SECURITY_CREDENTIALS),
                    "securityCredentials");

        }
        catch(Exception e)
        {
            fail("Test failed : " + e.getMessage());
        }
    }

    /**
     * Test reading in an invalid configuration file that is missing an ldappc
     * element. This test is inactive to avoid an error message in the test case
     * results output that might mislead the user into thinking that a test case
     * has failed, when this test should create an error message in the log.
     * This test was made and verified to work prior to the initial release of
     * the code.
     */
    public void inactiveTestInvalidMissingLdappcElementFile()
    {
        DisplayTest
                .showRunTitle("testInvalidMissingLdappcElementFile",
                        "Process an invalid Configuration File that is missing the ldappc element");

        try
        {
            //
            // Get the configuration manager and validate all of the entries
            //
            ConfigManager cm = ConfigManager.load(ConfigManager.getSystemResourceURL(INVALID_NO_LDAPPC_ELEMENT_CONFIG_FILE_RESOURCE, true).toString());
//            ConfigManager cm = ConfigManager.load(INVALID_NO_LDAPPC_ELEMENT_CONFIG_FILE_RESOURCE);
            fail("Test failed : File without ldappc element parsed without error.");
        }
        catch(LdappcConfigurationException lce)
        {
            assertTrue(lce.getCause() == null);
        }
        catch(Exception e)
        {
            fail("Test failed : Unexpected exception " + e.getClass().getName()
                    + " :: " + e.getMessage());
        }
    }
}
