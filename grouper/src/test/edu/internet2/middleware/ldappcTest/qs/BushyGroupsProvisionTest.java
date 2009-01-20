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

package edu.internet2.middleware.ldappcTest.qs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.filter.ChildGroupFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.GrouperSessionControl;
import edu.internet2.middleware.ldappc.Ldappc;
import edu.internet2.middleware.ldappc.LdappcConfigurationException;
import edu.internet2.middleware.ldappc.ldap.OrganizationalUnit;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappcTest.BaseTestCase;
import edu.internet2.middleware.ldappcTest.DisplayTest;
import edu.internet2.middleware.ldappcTest.configuration.ConfigManagerTest;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Class for testing TODO FINISH
 */
public class BushyGroupsProvisionTest extends BaseTestCase {
    /**
     * Config file for provisioning bushy groups.
     */
    private static final String BUSHY_GROUPS_CONFIG = ConfigManagerTest.RELATIVE_RESOURCE_PATH
             + "ldappcBushyGroups.xml";

    /**
     * Grouper subject root DN
     */
    static final String           GROUPER_SUBJECT_ROOT_DN = "ou=uob,dc=example,dc=edu";
    /**
     * Stem delimiter
     */
    static final String           STEM_DELIMITER          = ":";

    /**
     * Grouper group root DN
     */
    private static Name           grouperGroupRootDn;

    /**
     * Holds the configuration instance.
     */
    private ConfigManager         configuration;

    /**
     * Holds the set of provisioned groups
     */
    private Set<Group>            provisionedGroups;

    /**
     * Holds the grouper session
     */
    private GrouperSessionControl sessionCtrl;

    /**
     * Holds the ldap context
     */
    private LdapContext           ldapContext;

    /**
     * Constructor
     */
    public BushyGroupsProvisionTest(String name) {
        super(name);
    }

    /**
     * Setup the fixture.
     */
    protected void setUp() {
        DisplayTest.showRunClass(getClass().getName());
        ConfigManager.cleanConfiguration();
        ConfigManager.loadSingleton(ConfigManager.getSystemResourceURL(BUSHY_GROUPS_CONFIG, true).toString());
        configuration = ConfigManager.getInstance();

        //
        // Get ldap context
        //
        try {
            ldapContext = LdapUtil.getLdapContext(ConfigManager.getInstance().getLdapContextParameters(), null);
        } catch (Exception e) {
            ErrorLog.fatal(getClass(), "Unable to get Ldap context :: " + e.getMessage());
        }

        //
        // Get the Name of the root ou
        //
        try {
            String rootDnString = configuration.getGroupDnRoot();
            if (rootDnString == null) {
                throw new LdappcConfigurationException("Group root DN is not defined.");
            }

            grouperGroupRootDn = ldapContext.getNameParser(LdapUtil.EMPTY_NAME).parse(rootDnString);
        } catch (NamingException e1) {
            ErrorLog.fatal(getClass(), "Unable to get Grouper root DN");
        }

        //
        // Verify basics about the directory
        //
        try {
            performVerification();
        } catch (Exception e) {
            fail("Unable to verify environment for testing :: " + e.getMessage());
        }

        //
        // Perform the provisioning
        //
        String[] args = { "-subject", "GrouperSystem", "-groups" };
        System.out.println("Calling Ldappc.main(args)");
        Ldappc.main(args);

        //
        // Build a grouper sessions
        //
        RegistryReset.internal_resetRegistryAndAddTestSubjects();
        sessionCtrl = new GrouperSessionControl();
        if (!sessionCtrl.startSession("GrouperSystem")) {
            fail("Failed to create Grouper session");
        }

        // 20090115 tz just one stem to pass the test
        StemHelper.addChildStem(StemHelper.findRootStem(SessionHelper.getRootSession()), "qsuob", "QS University of Bristol");

        //
        // Get the set of groups provisioned
        //
        try {
            provisionedGroups = getProvisionedGroups();
        } catch (Exception e) {
            ErrorLog.fatal(getClass(), "Unable to get provisioned group set :: " + e.getMessage());
        }

    }

    /**
     * Returns the set of provisioned groups. This ASSUMES the ldappc.xml file
     * uses a single stem query of "qsuob"
     */
    private Set getProvisionedGroups() throws Exception {
        Stem stem = StemFinder.findByName(sessionCtrl.getSession(), "qsuob");
        ChildGroupFilter filter = new ChildGroupFilter(stem);
        return GrouperQuery.createQuery(sessionCtrl.getSession(), filter).getGroups();
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown() {
        sessionCtrl.stopSession();
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) {
        BaseTestCase.runTestRunner(BushyGroupsProvisionTest.class);
    }

    /**
     * Test that groups have been provisioned
     */
    public void testProvisionedGroups() {
        DisplayTest.showRunTitle("Verifying the number of provisioned group entries");

        try {
            //
            // Get the set of provisioned groups from the directory
            //
            SearchControls cons = new SearchControls();
            cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> groupEntries = ldapContext.search(grouperGroupRootDn, "(objectClass=groupOfNames)", cons);
            Set<Name> ldapGroupNames = new HashSet<Name>();
            while (groupEntries.hasMore()) {
                SearchResult searchResult = (SearchResult) groupEntries.next();
                ldapGroupNames.add(new LdapName(searchResult.getNameInNamespace()));
            }

            //
            // Iterate through the groups building set of names
            //
            Set<Name> grouperGroupNames = new HashSet<Name>();
            for (Group group : provisionedGroups) {
                grouperGroupNames.add(buildGroupDn(group));
            }

            //
            // Verify the name sets are the same
            //
            if (!ldapGroupNames.containsAll(grouperGroupNames)) {
                grouperGroupNames.removeAll(ldapGroupNames);
                fail("Following names were not found in the directory: " + grouperGroupNames);
            } else if (!grouperGroupNames.containsAll(ldapGroupNames)) {
                ldapGroupNames.removeAll(grouperGroupNames);
                fail("Following names were not found in Grouper:" + ldapGroupNames);
            }
        } catch (NamingException ne) {
            fail("Unable to verify provisioned groups :: " + ne.getMessage());
        }

        // Avoid time for provisioning for each test.
        internalTestProvisionedGroupMappedAttributes();
        internalTestProvisionedGroupNameMembershipList();
    }

    /**
     * This builds the DN of the given bushy group.
     * 
     * @param group
     *            Group
     * @return DN for the associated LDAP entry
     * @throws NamingException
     *             thrown if a Naming error occurs.
     */
    private Name buildGroupDn(Group group) throws NamingException {
        //
        // Initialize return value
        //
        Name groupDn = null;

        groupDn = buildStemOuEntries(group);

        //
        // Get the group's rdn value
        //
        String rdnString = group.getExtension();

        //
        // Add the rdn to the group Dn and the rdnMods
        //
        String rdnAttr = ConfigManager.getInstance().getGroupDnRdnAttribute();
        groupDn = groupDn.add(rdnAttr + "=" + LdapUtil.makeLdapNameSafe(rdnString));

        return groupDn;
    }

    /**
     * This builds the group's parent OU DN.
     * 
     * @param group
     *            Group
     * @return OU DN under which the group entry must be created.
     * @throws javax.naming.NamingException
     *             thrown if a Naming exception occured.
     */
    protected Name buildStemOuEntries(Group group) throws NamingException {
        //
        // Initialize the stemDn to be the root DN. This stemDn
        // is updated for each element of the group's stem below
        //
        Name stemDn = (Name) grouperGroupRootDn.clone();

        //
        // Get the group's parent stem, and tokenize it's name to build
        // the ou's for the group.
        //
        Stem stem = group.getParentStem();
        StringTokenizer stemTokens = new StringTokenizer(stem.getName(), STEM_DELIMITER);
        while (stemTokens.hasMoreTokens()) {
            //
            // Get next stem token for the rdn value making sure it is Ldap name
            // safe
            //
            String rdnString = stemTokens.nextToken();
            String rdnValue = LdapUtil.makeLdapNameSafe(rdnString);

            //
            // Build the new name (keep adding on to previous)
            //
            stemDn = stemDn.add(OrganizationalUnit.Attribute.OU + "=" + rdnValue);
        }

        return stemDn;
    }

    /**
     * Test provisioned group mapped attributes
     */
    public void internalTestProvisionedGroupMappedAttributes() {
        DisplayTest.showRunTitle("Verifying provisioned group mapped attributes");

        try {
            //
            // Get the list of mapped attributes
            //
            Map<String, String> attributeMapping = configuration.getGroupAttributeMapping();

            //
            // Build the list of attributes to retrieve from directory
            //
            Collection ldapAttributes = attributeMapping.values();
            String[] ldapAttrArray = (String[]) ldapAttributes.toArray(new String[0]);

            //
            // For each group, make sure grouper attribute value matches the
            // ldap attribute value
            //
            for (Group group : provisionedGroups) {
                Name groupDn = buildGroupDn(group);

                Attributes attributes = ldapContext.getAttributes(groupDn, ldapAttrArray);

                //
                // Verify ldap attribute value is same as grouper attribute
                // value
                //
                for (String grouperAttr : attributeMapping.keySet()) {
                    String ldapAttr = attributeMapping.get(grouperAttr);

                    try {
                        String grouperAttrValue = group.getAttribute(grouperAttr);
                        Attribute attribute = attributes.get(ldapAttr);
                        if (attribute != null) {
                            assertEquals("To many ldap attribute values", 1, attribute.size());
                            assertEquals("Grouper and ldap attribute values don't match", grouperAttrValue, attribute.get());
                        } else {
                            fail("Ldap attribute not found");
                        }
                    } catch (AttributeNotFoundException anfe) {
                        // Do nothing; ignore
                    }
                }
            }

            //
            // Get the set of provisioned groups mapped attributes from the
            // directory
            //
            NamingEnumeration groupEntries = ldapContext.list(grouperGroupRootDn);
            HashSet ldapGroupNames = new HashSet();
            while (groupEntries.hasMore()) {
                NameClassPair nameClass = (NameClassPair) groupEntries.next();
                ldapGroupNames.add(nameClass.getName());
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            fail("Unable to verify provisioned groups mapped attributes :: " + ne.getMessage());
        }
    }

    /**
     * Test provisioned group name membership list
     */
    public void internalTestProvisionedGroupNameMembershipList() {
        DisplayTest.showRunTitle("Verifying provisioned group name membership list");

        // Get a HashMap containing a list of groups as keys with values of a
        // list of names of members.
        Map<Name, Set<String>> groupAndMembers = hasMemberSearch();

        for (Group group : provisionedGroups) {
            HashSet grouperMemberNames = new HashSet();

            Name groupDn = null;
            try {
                groupDn = buildGroupDn(group);
            } catch (NamingException e1) {
                fail("Unable to build group DN for group " + group.getName());
            }

            //
            // Get membership process it
            //
            for (Member member : (Set<Member>) group.getMembers()) {
                Subject subject = null;
                try {
                    subject = member.getSubject();
                    Source source = subject.getSource();
                    if ("qsuob".equals(source.getId())) {
                        // Check that the subject id is in the list of members
                        // for this group
                        if (!groupAndMembers.get(groupDn).contains(subject.getId())) {
                            fail("**********The subject id of " + subject.getId() + " was not found in " + groupAndMembers.get(groupDn));
                        }
                    }
                } catch (SubjectNotFoundException snfe) {
                    //
                    // If the subject was not found, log it and continue
                    //
                    fail(" Subject not found :: " + snfe.getMessage());
                    continue;
                }

                //
                // Catch all of the exceptions thrown as they are "warning" and
                // handle them in a common manner.
                //
                try {
                    //
                    // Get the subject source
                    //
                    Source source = subject.getSource();
                    if (source == null) {
                        fail("Source is null");
                    }

                    //
                    // Get the naming attribute for this source
                    //
                    String nameAttribute = configuration.getGroupMembersNameListNamingAttribute(source.getId());
                    if (nameAttribute != null) {
                        //
                        // Get the subject attribute value
                        //
                        String nameValue = subject.getAttributeValue(nameAttribute);
                        if (nameValue != null) {
                            grouperMemberNames.add(nameValue);
                        } else {
                            fail("Naming attribute [" + nameAttribute + "] is not defined.");
                        }
                    } else {
                        fail("No group members name list naming attribute defined for source id [" + source.getId() + "]");
                    }
                } catch (Exception e) {
                    //
                    // All of the exceptions thrown in this try are "warning"
                    // related so simply log them and continue on with
                    // processing.
                    //
                    fail("Failure in verifying group member list: " + e.getMessage());
                }
            }
            // FOR DEBUG, do a single group
            // break;
        }
    }

    /**
     * Test provisioned group DN membership list
     */

    /**
     * Test member's group membership list
     */

    /**
     * Test privilege subjects permission list
     */

    /**
     * Performs a very basic verification that the subjects.ldif file has been
     * loaded.
     */
    private void performVerification() throws Exception {
        //
        // Throw an exception based on msg length below
        //
        String msg = "";

        //
        // Verify that specific entries exists
        //
        String[] dn = { GROUPER_SUBJECT_ROOT_DN, grouperGroupRootDn.toString() };
        for (int i = 0; i < dn.length; i++) {
            try {
                ldapContext.lookup(dn[i]);
            } catch (Exception e) {
                msg += (msg.length() > 0 ? "\n" : "") + "Unable to locate dn: " + dn[i];
            }
        }

        //
        // If something wasn't found, thrown an exception
        //
        if (msg.length() > 0) {
            throw new Exception(msg);
        }
    }

    /**
     * Search LDAP for hasMember
     * 
     * @return map of groups as key with values of a string containing members
     */
    private Map<Name, Set<String>> hasMemberSearch() {
        Map<Name, Set<String>> groupAndMembers = new HashMap<Name, Set<String>>();
        // Specify the attributes to match. Ignore attribute name case.
        Attributes matchAttrs = new BasicAttributes(true);
        matchAttrs.put(new BasicAttribute("hasMember"));
        // Specify the attributes to return.
        String[] attributesToReturn = { "hasMember" };

        try {
            // Search for objects that have those matching attributes
            if (matchAttrs == null) {
                fail("In hasMemberSearch, matchAttrs is null.");
            }
            if (ldapContext == null) {
                fail("In hasMemberSearch, ldapContext is null.");
            }
            for (Group group : provisionedGroups) {
                Name groupDn = buildGroupDn(group);
                Attributes attributes = ldapContext.getAttributes(groupDn, attributesToReturn);

                Attribute attribute = attributes.get("hasMember");
                Set<String> members = new HashSet<String>();
                for (int i = 0; i < attribute.size(); i++) {
                    String value = (String) attribute.get(i).toString();
                    members.add(value);
                }
                groupAndMembers.put(groupDn, members);
            }
        } catch (NamingException ne) {
            fail("Could not get search attributes -- naming exception: " + ne.getMessage() + "    " + ne.toString());
        }
        return groupAndMembers;
    }
}
