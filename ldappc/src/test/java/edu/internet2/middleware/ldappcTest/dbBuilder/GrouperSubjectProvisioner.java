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

package edu.internet2.middleware.ldappcTest.dbBuilder;

import java.util.Iterator;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.naming.NotContextException;
import javax.naming.ContextNotEmptyException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.ldap.LdapContext;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;


import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.subject.Subject;


/**
* This class provisions Grouper privileged subject data to the
* LDAP directory for testing purposes.
*/
public class GrouperSubjectProvisioner
{
   /**
    * Ldap Context
    */
   private LdapContext ldapCtx;

   /**
    * The root Dn for grouper, useds as a base for the test subjects
    * to be placed into the LDAP directory.
    */
   private String rootDn;

   /**
    * Constructs a <code>GrouperSubjectProvisioner</code> for the given
    * Ldap context.  
    *
    * The following is a sample of the type of ldif file data that corresponds to
    * the data that is provisioned to the LDAP directory by this class that 
    * accesses the Grouper database to retrieve subject data. 
    * 
    * //dn: uid=iado,ou=people,dc=uob,dc=ac,dc=uk
    * dn: uid=iado,ou=grouperTestSubjects,dc=my-domain,dc=com
    * objectClass: top
    * objectClass: person
    * objectClass: inetOrgPerson
    * cn: Ian Dolphin
    * uid: iado
    * sn: Dolphin
    * userPassword:: e1NTSEF9Y2ZuRW1vQnNDYjZlUXhSWVRSUHB3ZTlIdW9JUkgxWVU=
    * 
    * @param ldapCtx Ldap context
    */
   public GrouperSubjectProvisioner(LdapContext ldapCtx)
   {
       this.ldapCtx = ldapCtx;
       rootDn = ConfigManager.getInstance().getGroupDnRoot();
   }

   /**
    * Provisions permission data to the directory
    * @throws javax.naming.NamingException thrown if a Naming error occurs
    */
   public void provision(String testSubjectsName, String testSubjectsRdn) 
       throws NamingException
   {
       //
       // Get the list of  subjects
       //
       Set subjects = SubjectFinder.findAll("%");
       
       //
       // Create the DN that is used as a base for adding subjects.
       //
       String testSubjectsDn = testSubjectsRdn + "," + rootDn;
       //System.out.println("DEBUG, testSubjectsDn=" + testSubjectsDn);

       //
       // Process each Subject
       //
       Iterator subjectIterator = subjects.iterator();
       LdapContext ctx = LdapUtil.getLdapContext();        
       int numberOfSubjects = subjects.size();
       //System.out.println(" DEBUG Number of subjects = " + subjects.size() );
       int subjectCount = 0;
       if (numberOfSubjects > 0)
       {
           //
           // Create the base DN to which subjects will be added.
           //

           buildSubjectLdapBase(ctx, testSubjectsName, testSubjectsRdn, testSubjectsDn);

           //
           // Loop over subjects adding them to the LDAP directory
           //

           while(subjectIterator.hasNext())
           {
               subjectCount++;
               /*
                   // Can use the following to limit the number of subjects provisioned for testing. 
                   // TEMPORARILY limit to 5 subjects.
                   if (subjectCount > 50)
                   { 
                       break;
                   } 
               */
               //
               // Get the privileged subject
               //
               Subject subject = (Subject) subjectIterator.next();
    
               //System.out.println( "Id/TypeID/Name/SourceId: " + subject.getId() 
               //        + "   " + subject.getType().getName()
               //        + "    " + subject.getName()
               //        + "    " + subject.getSource().getId());

               // example of testSubjectsDn: "ou=testSubjects,ou=testgrouper,dc=my-domain,dc=com"; 

               // Write this subject to the LDAP directory
               buildSubjectLdapEntry(ctx, subject, testSubjectsDn);
           }
       }
       else
       {
           ErrorLog.warn(this.getClass(), 
                   "No subjects to provision from GrouperSubjectProvisioner");
       }
   }

   /**
     * This create an DN entry under which to place the test subjects used
     * for testing purposes.
     * It replaces all existing data under the testSubjectOu + rootDn entry.
     * 
     * @param ctx
     *         Ldap context
     * @param testSubjectsName
     *         Defines the name of the subject as an organizational unit
     * @param testSubjectsRdn
     *         Defines the RDN of the entry to be used as the base under which
     *         all test subject directory entries are placed.
     * @param testSubjectsDn
     *         Defines the DN of the entry to be used as the base under which
     *         all test subject directory entries are placed.
     * @throws javax.naming.NamingException
     *         thrown if a Naming exception occurs
     */
    protected void buildSubjectLdapBase(LdapContext ctx, String testSubjectsName, String testSubjectsRdn, String testSubjectsDn)
            throws NamingException
    {
        Attributes attributes = new BasicAttributes(true); 
        attributes.put(new BasicAttribute("objectclass", "top"));
        attributes.put(new BasicAttribute("objectclass", "organizationalUnit"));
        attributes.put(new BasicAttribute("ou", testSubjectsName));
      
        // Remove subcontext before recreating.

        try
        {
            DirContext ctxToPrune = (DirContext) ldapCtx.lookup(testSubjectsDn);
            LdapUtil.prune(ctxToPrune);
            destroySubcontext(testSubjectsDn);
        }
        catch (NamingException ne)
        {
            DebugLog.info( this.getClass(), "Could not prune directory: " + testSubjectsDn 
                    + "\n" + ne.getMessage() );
        } 

        try
        {
            ctx.createSubcontext(testSubjectsDn, attributes);
        }
        catch (NameAlreadyBoundException ne)
        {
            // Should not be possible to get here
            ErrorLog.fatal(getClass(), "Program Error, name already bound for: " + testSubjectsDn 
                    + "\n" + ne.getMessage() );
        }
        catch (NamingException ne)
        {
            ErrorLog.error(this.getClass(), 
                    "DEBUG in GrouperSubjectProvisioner.buildSubjectLdapBase, "
                    + "naming exception creating subcontext: " 
                    + testSubjectsDn + "\n" + ne.getMessage());
        }

        return;
    }

   /**
     * This populates the LDAP directory with subject data for testing purposes.
     * It replaces all existing data under the testSubjectOu + rootDn entry
     * from the .
     * 
     * @param ctx
     *            Ldap context
     * @param subject
     *            The subject to be provisioned to the LDAP directory. 
     * @param testSubjectsDn
     *            Defines the DN of the entry to be used as the root under which
     *            all other directory entries are placed, as set in the <group>
     *            element in the ldappc.xml file.
     * @throws javax.naming.NamingException
     *             thrown if a Naming exception occurs
     */
    protected void buildSubjectLdapEntry(LdapContext ctx, Subject subject, String testSubjectsDn)
            throws NamingException
    {
        //
        // Create an attribute list for creating the new subject entry.
        //
        Attributes attributes = new BasicAttributes(true);

        //
        // Define the RDN attribute name and set its value
        //
        final String rdnAttribute = "uid";
        String rdnValue = subject.getId();
        // add the RDN (uid) attribute
        attributes.put(new BasicAttribute(rdnAttribute, rdnValue));

        //
        // Make sure the rdnValue is LDAP safe
        //
        rdnValue = LdapUtil.makeLdapNameSafe(rdnValue);

        // Add the object classes
        Attribute attribute = new BasicAttribute("objectclass");
        attribute.add("top");
        attribute.add("person");
        attribute.add("inetOrgPerson");
        attributes.put(attribute);

        attributes.put(new BasicAttribute("cn", subject.getName()));
        attributes.put(new BasicAttribute("sn", subject.getName()));
        attributes.put(new BasicAttribute("userPassword",""));

        String subjectDn = rdnAttribute + "=" + rdnValue + "," + testSubjectsDn;
        //System.out.println("In GrouperSubjectProvisioner.buildSubjectLdapEntry() subjectDn =" + subjectDn);
        
        // remove the subcontext if it already exists.
        try
        {
            ctx.destroySubcontext(subjectDn);
        }
        catch (NamingException ne)
        {
            // Do nothing if it does not exist.
        }

        try
        {
            ctx.createSubcontext(subjectDn, attributes);
        }
        catch (NamingException ne)
        {
            ErrorLog.error(this.getClass(), "DEBUG in GrouperSubjectProvisioner, naming exception creating subcontext: " 
                    + subjectDn + "\n" + ne.getMessage());
        }

        return;
    }
       
    /**
     * Destroy a subcontext. 
     * Use LdapUtil.prune to remove subdirectories first. 
     * @param the subcontext to be removed
     */    
    private void destroySubcontext(String subcontext)
    {
        try
        {
            ldapCtx.destroySubcontext(subcontext);
        }
        catch (NameNotFoundException nnfe)
        {
            ErrorLog.error(getClass(), "Can not destroy subcontext, name not found: " +  subcontext + "\n" + nnfe.getMessage());
        }
        catch (NotContextException nce)
        {
            ErrorLog.error(getClass(), "Can not destroy subcontext, not a context: " +  subcontext + "\n" + nce.getMessage());
        }
        catch (ContextNotEmptyException cnee)
        {
            ErrorLog.error(getClass(), "Can not destroy subcontext, context not empty: " +  subcontext + "\n" + cnee.getMessage());
        }
        catch (NamingException ne)
        {
            ErrorLog.error(getClass(), "Can not destroy subcontext, naming exception: " +  subcontext + "\n" + ne.getMessage());
        }
    }

}


