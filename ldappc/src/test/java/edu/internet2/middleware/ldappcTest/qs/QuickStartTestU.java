/*
 Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
 Copyright 2004-2006 The University Of Chicago
 
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapContext;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SubjectNotFoundException;

import edu.internet2.middleware.ldappcTest.TestOptions;
import edu.internet2.middleware.ldappcTest.DisplayTest;

import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.Ldappc;
import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.GrouperSubjectRetriever;
import edu.internet2.middleware.ldappc.GrouperSessionControl;
import edu.internet2.middleware.ldappc.StemProcessor;
import edu.internet2.middleware.ldappc.GroupProcessor;
import edu.internet2.middleware.ldappc.InputOptions;
import edu.internet2.middleware.ldappc.LdappcProvisionControl;

import edu.internet2.middleware.grouper.AttributeNotFoundException;
import edu.internet2.middleware.grouper.GroupNameFilter;
import edu.internet2.middleware.grouper.GrouperQuery;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GroupAddException;
import edu.internet2.middleware.grouper.GroupDeleteException;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.MemberAddException;
import edu.internet2.middleware.grouper.MemberDeleteException;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.UnionFilter;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
/**
 * Class for testing TODO FINISH
 */
public class QuickStartTestU extends TestCase
{
    /**
     * Grouper group root DN
     */
    //static final String GROUPER_GROUP_ROOT_DN = "ou=testgrouper,dc=example,dc=edu";

    /**
     * Grouper subject root DN
     */
    static final String GROUPER_SUBJECT_ROOT_DN = "ou=uob,dc=example,dc=edu";

    /**
     * Signet member root DN
     */
    static final String SIGNET_SUBJECT_ROOT_DN = "ou=kitn,dc=example,dc=edu";

    /**
     * Grouper group root DN
     */
    private static String grouperGroupRootDn;

    /**
     * Holds the set of provisioned groups
     */
    private Set provisionedGroups;

    /**
     * Holds the grouper session
     */
    private GrouperSessionControl sessionCtrl;

    /**
     * Holds the ldap context
     */
    private LdapContext ldapContext;

    /**
     * Constructor
     */
    public QuickStartTestU(String name)
    {
        super(name);
    }

    /**
     * Setup the fixture.
     */
    protected void setUp()
    {
        DisplayTest.showRunClass(getClass().getName());

        //
        // Get ldap context
        //
        try
        {
            ldapContext = LdapUtil.getLdapContext();
        }
        catch(Exception e)
        {
            ErrorLog.fatal(getClass(), "Unable to get Ldap context :: "
                    + e.getMessage());
        }

        grouperGroupRootDn = ConfigManager.getInstance().getGroupDnRoot();

        //
        // Verify basics about the directory
        //
        try
        {
            performVerification();
        }
        catch(Exception e)
        {
            fail("Unable to verify environment for testing :: "
                    + e.getMessage());
        }

        //
        // Perform the provisioning
        //
        provisionAll();

        //
        // Build a grouper sessions
        //
        sessionCtrl = new GrouperSessionControl();
        if (!sessionCtrl.startSession("GrouperSystem"))
        {
            fail("Failed to create Grouper session");
        }

        //
        // Get the set of groups provisioned
        //
        try
        {
            provisionedGroups = getProvisionedGroups();
        }
        catch(Exception e)
        {
            ErrorLog.fatal(getClass(),
                    "Unable to get provisioned group set :: " + e.getMessage());
        }

    }

    /**
     * Returns the set of provisioned groups. This ASSUMES the ldappc.xml file
     * uses a single stem query of "qsuob"
     */
    private Set getProvisionedGroups() throws Exception
    {
        Stem stem = StemFinder.findByName(sessionCtrl.getSession(), "qsuob");
        GroupNameFilter filter = new GroupNameFilter("%", stem);
        return GrouperQuery.createQuery(sessionCtrl.getSession(), filter)
                .getGroups();
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown()
    {
        sessionCtrl.stopSession();
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(QuickStartTestU.class);
    }

    /**
     * Test that groups have been provisioned
     */
    public void testProvisionedGroups()
    {
        DisplayTest
                .showRunTitle("Verifying the number of provisioned group entries");

        try
        {
            //
            // Get the set of provisioned groups from the directory
            //
            NamingEnumeration groupEntries = ldapContext
                    .list(grouperGroupRootDn);
            HashSet ldapGroupNames = new HashSet();
            while(groupEntries.hasMore())
            {
                NameClassPair nameClass = (NameClassPair) groupEntries.next();
                ldapGroupNames.add(nameClass.getName());
            }

            //
            // Iterate through the groups building set of names
            //
            String rdnAttr = ConfigManager.getInstance()
                    .getGroupDnRdnAttribute();
            HashSet grouperGroupNames = new HashSet();
            Iterator groups = provisionedGroups.iterator();
            while(groups.hasNext())
            {
                /*
                grouperGroupNames.add(rdnAttr
                        + "="
                        + LdapUtil.makeLdapNameSafe(((Group) groups.next())
                                .getName()));
                 */
                Group group = (Group)groups.next();
                grouperGroupNames.add(rdnAttr + "="
                        + LdapUtil.makeLdapNameSafe(group.getName()));
            }

            //
            // Verify the name sets are the same
            //
            if (!ldapGroupNames.containsAll(grouperGroupNames))
            {
                grouperGroupNames.removeAll(ldapGroupNames);
                fail("Following names were not found in the directory: "
                        + grouperGroupNames);
            }
            else if (!grouperGroupNames.containsAll(ldapGroupNames))
            {
                ldapGroupNames.removeAll(grouperGroupNames);
                fail("Following names were not found in Grouper:"
                        + ldapGroupNames);
            }
        }
        catch(NamingException ne)
        {
            fail("Unable to verify provisioned groups :: " + ne.getMessage());
        }

        // Avoid time for provisioning for each test.
        internalTestProvisionedGroupMappedAttributes();
        internalTestProvisionedGroupNameMembershipList();
    }

    /**
     * Test provisioned group mapped attributes
     */
public void internalTestProvisionedGroupMappedAttributes()
    {
        DisplayTest
                .showRunTitle("Verifying provisioned group mapped attributes");

        try
        {
            //
            // Get the list of mapped attributes
            //
            ConfigManager cm = ConfigManager.getInstance();
            Map attributeMapping = cm.getGroupAttributeMapping();

            //
            // Build the list of attributes to retrieve from directory
            //
            Collection ldapAttributes = attributeMapping.values();
            String[] ldapAttrArray = (String[]) ldapAttributes
                    .toArray(new String[0]);

            //
            // Get the RDN attribute name
            //
            String rdnAttr = cm.getGroupDnRdnAttribute();

            //
            // For each group, make sure grouper attribute value matches the
            // ldap attribute value
            //
            Iterator groups = provisionedGroups.iterator();
            while(groups.hasNext())
            {
                //
                // Get the associated group entry
                //
                Group group = (Group) groups.next();
                String groupDn = rdnAttr + "="
                        + LdapUtil.makeLdapNameSafe(group.getName()) + "," +
                        grouperGroupRootDn;
                
                Attributes attributes = ldapContext.getAttributes(groupDn,
                        ldapAttrArray);

                //
                // Verify ldap attribute value is same as grouper attribute
                // value
                //
                Iterator keys = attributeMapping.keySet().iterator();
                while(keys.hasNext())
                {
                    String grouperAttr = (String) keys.next();
                    String ldapAttr = (String) attributeMapping
                            .get(grouperAttr);

                    try
                    {
                        String grouperAttrValue = group
                                .getAttribute(grouperAttr);
                        Attribute attribute = attributes.get(ldapAttr);
                        if (attribute != null)
                        {
                            assertEquals("To many ldap attribute values",1,attribute.size());
                            assertEquals("Grouper and ldap attribute values don't match",grouperAttrValue,attribute.get());
                        }
                        else
                        {
                            fail("Ldap attribute not found");
                        }
                    }
                    catch(AttributeNotFoundException anfe)
                    {
                        // Do nothing; ignore
                    }
                }
            }

                //
                // Get the set of provisioned groups mapped attributes from the
                // directory
                //
                NamingEnumeration groupEntries = ldapContext
                        .list(grouperGroupRootDn);
                HashSet ldapGroupNames = new HashSet();
                while(groupEntries.hasMore())
                {
                    NameClassPair nameClass = (NameClassPair) groupEntries
                            .next();
                    ldapGroupNames.add(nameClass.getName());
                }
        }
        catch(NamingException ne)
        {
            ne.printStackTrace();            
            fail("Unable to verify provisioned groups mapped attributes :: "
                    + ne.getMessage());
        }
    }

    /**
     * Test provisioned group name membership list
     */
    public void internalTestProvisionedGroupNameMembershipList()
    {
        DisplayTest
                .showRunTitle("Verifying provisioned group name membership list");

            ConfigManager cm = ConfigManager.getInstance();
            String rdnAttr = ConfigManager.getInstance().getGroupDnRdnAttribute();

            //
            // Get  the LDAP entry attribute containing the list of Members names which
            // belong to the Group. 
            // Explicitly, get the group-members-name-list element's
            // attribute, list-attribute (e.g. "hasMember").
            //
            String groupMembersNameListAttr = cm.getGroupMembersNameListAttribute();
            // Get a Map of source names to subject attribute names
            Map groupMembersNameListNamingAttributes = cm.getGroupMembersNameListNamingAttributes();

            // Get a HashMap containing a list of groups as keys with values of a list
            // of names of members.
            HashMap groupAndMembers = hasMemberSearch();
            Group group = null;            
            Iterator groups = provisionedGroups.iterator();

            while(groups.hasNext())
            {
                HashSet grouperMemberNames = new HashSet();
                //
                // Get the associated group entry
                //
                group = (Group) groups.next();
                String groupRdn = rdnAttr + "=" + LdapUtil.makeLdapNameSafe(group.getName());
                String groupDn = groupRdn + "," + grouperGroupRootDn;
                //
                // Get membership process it
                //
                Iterator members = group.getMembers().iterator();
                while(members.hasNext())
                {
                    //
                    // Get the member subject
                    //
                    Member member = (Member) members.next();
                    Subject subject = null;
                    try
                    {
                        subject = member.getSubject();
                        Source source = subject.getSource();
                        if ( "qsuob".equals(source.getId()) )
                        {
                            // Check that the subject id is in the list of members for this group
                            String listOfMembersInLdap = (String)groupAndMembers.get(groupRdn);
                            if (listOfMembersInLdap.indexOf(subject.getId()) == -1)
                            {
                                fail("**********The subject id of " + subject.getId() + " was not found in " 
                                        + listOfMembersInLdap );
                            }
                        }
                    }
                    catch(SubjectNotFoundException snfe)
                    {
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
                    try
                    {
                        //
                        // Get the subject source
                        //
                        Source source = subject.getSource();
                        if (source == null)
                        {
                            fail("Source is null");
                        }
        
                        //
                        // Get the naming attribute for this source
                        //
                        String nameAttribute = cm.getGroupMembersNameListNamingAttribute(
                                        source.getId());
                        if (nameAttribute != null)
                        {
                            //
                            // Get the subject attribute value
                            //
                            String nameValue = subject
                                    .getAttributeValue(nameAttribute);
                            if (nameValue != null)
                            {
                                grouperMemberNames.add(nameValue);
                            }
                            else
                            {
                                fail("Naming attribute ["
                                        + nameAttribute + "] is not defined.");
                            }
                        }
                        else
                        {
                            fail ("No group members name list naming attribute defined for source id ["
                                            + source.getId() + "]");
                        }
                    }
                    catch(Exception e)
                    {
                        //
                        // All of the exceptions thrown in this try are "warning"
                        // related so simply log them and continue on with
                        // processing.
                        //
                        fail("Failure in verifying group member list: " + e.getMessage());
                    }
                }
                // FOR DEBUG, do a single group
                //break;
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
     * Provisions groups, memberships, and permissions.
     */
    private void provisionAll()
    {
        String[] args = { "-subject", "GrouperSystem", "-groups",
                "-memberships", "-permissions" };
        System.out.println("Calling Ldappc.main(args)");
        Ldappc.main(args);
    }

    /**
     * Performs a very basic verification that the subjects.ldif file has been
     * loaded.
     */
    private void performVerification() throws Exception
    {
        //
        // Throw an exception based on msg length below
        //
        String msg = "";

        //
        // Verify that specific entries exists
        //
        String[] dn = { GROUPER_SUBJECT_ROOT_DN, grouperGroupRootDn,
                SIGNET_SUBJECT_ROOT_DN };
        for(int i = 0; i < dn.length; i++)
        {
            try
            {
                ldapContext.lookup(dn[i]);
            }
            catch(Exception e)
            {
                msg += (msg.length() > 0 ? "\n" : "") + "Unable to locate dn: "
                        + dn[i];
            }
        }

        //
        // If something wasn't found, thrown an exception
        //
        if (msg.length() > 0)
        {
            throw new Exception(msg);
        }
    }

    /**
     * Search kDAP for hasMember
     * @return map of groups as key with values of a string containing members 
     */
    private HashMap hasMemberSearch() 
    {
        HashMap groupAndMembers = new HashMap();
        HashSet members = new HashSet();
        // Specify the attributes to match
        Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case
        matchAttrs.put(new BasicAttribute("hasMember"));
        String[] attributesToReturn = {"hasMember"};

        try
        {
            // Search for objects that have those matching attributes
            if (matchAttrs == null)
            {
                fail("In hasMemberSearch, matchAttrs is null.");
            }
            if (ldapContext == null)
            {
                fail("In hasMemberSearch, ldapContext is null.");
            }
            //NamingEnumeration answer = ldapContext.search(groupDn, 
            NamingEnumeration answer = ldapContext.search(grouperGroupRootDn, 
                    matchAttrs, attributesToReturn);
            //e.g.: NamingEnumeration answer = ldapContext.search("dc=my-domain,dc=com", matchAttrs);
            if (answer != null)
            {
                if (!answer.hasMore())
                {
                    fail("Could not find any search match for " + grouperGroupRootDn + ".");
                }
            }
            else
            {
                fail("value returned from search is null.");
            }
        
            Attributes searchAttributes = null;
            while (answer.hasMore()) {
                SearchResult sr = (SearchResult)answer.next();
                //members.add(sr.getName());
                
                searchAttributes = sr.getAttributes();
                for (NamingEnumeration e = searchAttributes.getAll(); e.hasMore();)
                {
                    // Should only have one element.
                    String value = (String) e.next().toString();
                    groupAndMembers.put(sr.getName(), value);
                } 
            }
        }
        catch(NamingException ne)
        {
            fail("Could not get search attributes -- naming exception: "
                    + ne.getMessage() + "    " + ne.toString());
        }
        return groupAndMembers;
    }



}
