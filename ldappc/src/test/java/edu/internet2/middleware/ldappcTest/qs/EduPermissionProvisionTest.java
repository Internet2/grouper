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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Name;
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

import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.Ldappc;
import edu.internet2.middleware.ldappc.ldap.EduPermission;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.ldappcTest.BaseTestCase;
import edu.internet2.middleware.ldappcTest.DisplayTest;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Privilege;
import edu.internet2.middleware.signet.PrivilegeImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.tree.TreeNode;
/**
 * Class for testing TODO FINISH
 */
public class EduPermissionProvisionTest extends BaseTestCase
{
    /**
     * Config file for provisioning eduPermission objects.
     */
    private static final String EDU_PERMISSION_CONFIG = "ldappc-eduPermission.xml";

    /**
     * Signet member root DN
     */
    static final String SIGNET_SUBJECT_ROOT_DN = "ou=kitn,dc=example,dc=edu";
    
    /**
     * Holds the ConfigManager instance.
     */
    ConfigManager configuration;
    
    /**
     * Holds the subject cache.
     */
    private SubjectCache subjectCache;

    /**
     * Holds the signet session
     */
    private Signet signet;
    
    /**
     * Holds the list of permissions provisioned
     */
    private Set<AssignmentImpl> signetAssignments;

    /**
     * Holds the ldap context
     */
    private LdapContext ldapContext;

    /**
     * Constructor
     */
    public EduPermissionProvisionTest(String name)
    {
        super(name);
    }

    /**
     * Setup the fixture.
     */
    protected void setUp()
    {
        DisplayTest.showRunClass(getClass().getName());
        ConfigManager.cleanConfiguration();
        ConfigManager.loadSingleton(ConfigManager.getSystemResourceURL(EDU_PERMISSION_CONFIG, true).toString());
        configuration = ConfigManager.getInstance();
        
        subjectCache = new SubjectCache();
        subjectCache.init(configuration);

        //
        // Perform the provisioning
        //
        String[] args = {"-subject", "GrouperSystem", "-permissions" };
        System.out.println("Calling Ldappc.main(args)");
        Ldappc.main(args);

        //
        // Get ldap context
        //
        try
        {
            ldapContext = LdapUtil.getLdapContext(configuration.getLdapContextParameters(), null);
        }
        catch(Exception e)
        {
            ErrorLog.fatal(getClass(), "Unable to get Ldap context :: "
                    + e.getMessage());
        }

        //
        // Build a grouper sessions
        //
        signet = new Signet();

        //
        // Get the set of groups provisioned
        //
        try
        {
            signetAssignments = getProvisionedPermissions();
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
    private Set<AssignmentImpl> getProvisionedPermissions() throws Exception
    {
        return signet.getPersistentDB().getAssignments("active");
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown()
    {
        ConfigManager.cleanConfiguration();

        // FIXME Do we need to close Signet? LDAP?
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[])
    {
        BaseTestCase.runTestRunner(EduPermissionProvisionTest.class);
    }

    /**
     * Test that groups have been provisioned
     */
    public void testProvisionedPermissions()
    {
        DisplayTest.showRunTitle("Verifying the number of provisioned eduPermission entries");

        try
        {
            //
            // Get the set of provisioned permissions from the directory.
            //
            SearchControls cons = new SearchControls();
            cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> groupEntries = ldapContext.search(SIGNET_SUBJECT_ROOT_DN, "(objectClass=eduPermission)", cons);
            Map<Name, Map<String, Set<String>>> ldapPermissions = new HashMap<Name, Map<String, Set<String>>>();
            while (groupEntries.hasMore()) {
                SearchResult result = groupEntries.next();
                Map<String, Set<String>> map = new HashMap<String, Set<String>>();
                String dn = result.getNameInNamespace();
                ldapPermissions.put(new LdapName(dn), map);

                Attributes attrs = result.getAttributes();
                NamingEnumeration<String> attrEnum = attrs.getIDs();
                while (attrEnum.hasMore()) {
                    String attrName = attrEnum.next().toLowerCase();
                    Set<String> set = new HashSet<String>();
                    map.put(attrName, set);

                    Attribute attr = attrs.get(attrName);
                    for (int i = 0; i < attr.size(); i++) {
                        Object value = attr.get(i);
                        if (!(value instanceof String)) {
                            fail("Attribute value for " + attrName + " is not a string");
                        }
                        set.add((String) value);
                    }
                }
            }

            //
            // Iterate through the groups building set of names
            //
            String rdnAttr = ConfigManager.getInstance().getGroupDnRdnAttribute();
            Map<Name, Map<String, Set<String>>> signetPermissions = new HashMap<Name, Map<String, Set<String>>>();
            for (AssignmentImpl assignment : signetAssignments) {
                SignetSubject privSubj = assignment.getGrantee();
                Name subjectDn = null;
                try
                {
                    subjectDn = subjectCache.findSubjectDn(ldapContext, configuration, privSubj.getSourceId(), privSubj.getId());
                }
                catch(Exception e)
                {
                    fail("Could not get DN for subject");
                }

                String safeFunctionId = LdapUtil.makeLdapNameSafe(assignment.getFunction().getId());
                for (Privilege privilege : (Set<Privilege>) PrivilegeImpl.getPrivileges(assignment)) {
                    Permission permission = privilege.getPermission();
                    String safeSubsystemId = LdapUtil.makeLdapNameSafe(permission.getSubsystem().getId());
                    String safePermissionId = LdapUtil.makeLdapNameSafe(permission.getId());
                    
                    Map<String, Set<String>> limitMap = buildLimitMap(privilege.getLimitValues());
    
                    String commonPrefix = EduPermission.Attribute.EDU_PERMISSION_SUBSYSTEM_ID + "=" + safeSubsystemId
                        + LdapUtil.MULTIVALUED_RDN_DELIMITER
                        + EduPermission.Attribute.EDU_PERMISSION_FUNCTION_ID + "=" + safeFunctionId
                        + LdapUtil.MULTIVALUED_RDN_DELIMITER
                        + EduPermission.Attribute.EDU_PERMISSION_ID + "=" + safePermissionId;

                    for (String limitId : limitMap.keySet()) {
                        String safeLimitId = LdapUtil.makeLdapNameSafe(limitId);
                        Name dn = (Name) subjectDn.clone();
                        dn.add(commonPrefix
                            + LdapUtil.MULTIVALUED_RDN_DELIMITER
                            + EduPermission.Attribute.EDU_PERMISSION_LIMIT_ID + "=" + safeLimitId);
                        Map<String, Set<String>> map = new HashMap<String, Set<String>>();

                        //
                        // Add the RDN attributes
                        //
                        map.put(EduPermission.Attribute.EDU_PERMISSION_SUBSYSTEM_ID.toLowerCase(), makeSet(permission.getSubsystem().getId()));
                        map.put(EduPermission.Attribute.EDU_PERMISSION_FUNCTION_ID.toLowerCase(), makeSet(safeFunctionId));
                        map.put(EduPermission.Attribute.EDU_PERMISSION_ID.toLowerCase(), makeSet(permission.getId()));
                        map.put(EduPermission.Attribute.EDU_PERMISSION_LIMIT_ID.toLowerCase(), makeSet(limitId));

                        //
                        // Add the scope values
                        //
                        TreeNode scope = privilege.getScope();
                        map.put(EduPermission.Attribute.EDU_PERMISSION_SCOPE_ID.toLowerCase(), makeSet(scope.getId()));
                        if (scope.getName() != null)
                        {
                            map.put(EduPermission.Attribute.EDU_PERMISSION_SCOPE_NAME.toLowerCase(), makeSet(scope.getName()));
                        }

                        //
                        // Add the limit values using the pre-built attribute
                        //
                        map.put(EduPermission.Attribute.EDU_PERMISSION_LIMIT.toLowerCase(), limitMap.get(limitId));
                        
                        Set<String> objectClass = new HashSet<String>();
                        objectClass.add("top");
                        objectClass.add(EduPermission.OBJECT_CLASS);
                        map.put(LdapUtil.OBJECT_CLASS_ATTRIBUTE.toLowerCase(), objectClass);
                        signetPermissions.put(dn, map);
                    }
                }
            }

            //
            // Verify the name sets are the same
            //
            if (!ldapPermissions.keySet().containsAll(signetPermissions.keySet()))
            {
                for (Name name : ldapPermissions.keySet()) {
                    signetPermissions.remove(name);
                }
                fail("Following names were not found in the directory: "
                        + signetPermissions.keySet());
            }
            else if (!signetPermissions.keySet().containsAll(ldapPermissions.keySet()))
            {
                for (Name name : signetPermissions.keySet()) {
                    ldapPermissions.remove(name);
                }
                fail("Following names were not found in Signet:"
                        + ldapPermissions.keySet());
            }

            DisplayTest.showRunTitle("Verifying provisioned permission attributes");

            // Verify that each permission is identical.
            for (Name name : ldapPermissions.keySet()) {
                comparePermissions(name.toString(), ldapPermissions.get(name), signetPermissions.get(name));
            }
        }
        catch(NamingException ne)
        {
            fail("Unable to verify provisioned groups :: " + ne.getMessage());
        }
    }

    /**
     * This builds a mapping from Limit ID to all of its values. This
     * transforms the set of LimitValues to a mapping where the key is the Limit
     * ID and the value is a Set whose values come from the associated LimitValues.
     * 
     * @param limitValues
     *            Set of LimitValues
     * @return Limit id to BasicAttribute map
     */
    private Map<String, Set<String>> buildLimitMap(Set<LimitValue> limitValues)
    {
        Map<String, Set<String>> limitMap = new HashMap<String, Set<String>>();

        if (limitValues != null)
        {
            for (LimitValue limitValue : limitValues) {
                //
                // Get the limit id and value
                //
                String id = limitValue.getLimit().getId();
                String value = limitValue.getValue();

                //
                // If the id is not already mapped, add it to the map
                //
                if (!limitMap.containsKey(id))
                {
                    limitMap.put(id, new HashSet<String>());
                }

                //
                // Add the value
                //
                limitMap.get(id).add(value);
            }
        }

        return limitMap;
    }
    
    /**
     * Create a single item set.
     */
    Set<String> makeSet(String value) {
        Set<String> set = new HashSet<String>();
        set.add(value);
        return set;
    }

    /**
     * Test provisioned group mapped attributes
     * @param dn TODO
     */
    public void comparePermissions(String dn, Map<String, Set<String>> ldapPermission, Map<String, Set<String>> signetPermission)
    {
        //
        // Verify the name sets are the same
        //
        if (!ldapPermission.keySet().containsAll(signetPermission.keySet()))
        {
            for (String attrName : ldapPermission.keySet()) {
                signetPermission.remove(attrName);
            }
            fail("Following attributes were not found in the directory entry " + dn + ": "
                    + signetPermission.keySet());
        }
        else if (!signetPermission.keySet().containsAll(ldapPermission.keySet()))
        {
            for (String attrName : signetPermission.keySet()) {
                ldapPermission.remove(attrName);
            }
            fail("Following names were not found in the Signet entry " + dn + ":"
                    + ldapPermission.keySet());
        }

        // Verify that each attribute is identical.
        for (String attrName : ldapPermission.keySet()) {
            compareAttributes(dn, attrName, ldapPermission.get(attrName), signetPermission.get(attrName));
        }
    }

    /**
     * Test provisioned group mapped attributes
     * @param dn TODO
     * @param attrName TODO
     */
    public void compareAttributes(String dn, String attrName, Set<String> ldapAttribute, Set<String> signetAttribute)
    {
        //
        // Verify the name sets are the same
        //
        if (!ldapAttribute.containsAll(signetAttribute))
        {
            signetAttribute.removeAll(ldapAttribute);
            fail("Following attribute values were not found in the directory entry " + dn + "(" + attrName + "): "
                    + signetAttribute);
        }
        else if (!signetAttribute.containsAll(ldapAttribute))
        {
            ldapAttribute.removeAll(signetAttribute);
            fail("Following names were not found in the Signet entry " + dn + "(" + attrName + "): "
                    + ldapAttribute);
        }
    }
}
