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
import java.util.Map;
import java.util.Vector;

import javax.naming.ContextNotEmptyException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.subject.Subject;

/**
* This class provisions Signet privileged subject data to the
* LDAP directory for testing purposes.
*/
public class SignetSubjectProvisioner
{
   /**
    * Ldap Context
    */
   private LdapContext ldapCtx;

   /**
    * The root Dn for grouper, used as a base for the test subjects
    * to be placed into the LDAP directory.
    */
   private String rootDn;

   /**
    * Constructs a <code>SignetSubjectProvisioner</code> for the given
    * Ldap context.  
    *
    * The following is a sample of the type of ldif file data that corresponds to
    * the data that is provisioned to the LDAP directory by this class that 
    * accesses the Signet database to retrieve subject data. 
    * 
    * //dn: uid=iado,ou=people,dc=uob,dc=ac,dc=uk
    * dn: uid=iado,ou=testSubjects,dc=my-domain,dc=com
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
   public SignetSubjectProvisioner(LdapContext ldapCtx)
   {
       this.ldapCtx = ldapCtx;
       rootDn = ConfigManager.getInstance().getGroupDnRoot();
   }

   /**
    * Provisions subject data to the directory
    * @throws javax.naming.NamingException thrown if a Naming error occurs
    */
   public String provision(String testSubjectsRdn, String subjectOuName) 
           throws NamingException, ObjectNotFoundException
   {
       String failureMessage = null;
       String sourceId = null;
       //
       // Get the list of privileged subjects
       //
       Signet signet = new Signet();
       // There are 3 types of Subject Sources in Signet:
       // 1. The built-in Signet Super-Subject Source (SIGNET_SOURCE_ID, above)
       // 2. The Signet Persistent Source (Signet's DB can be used as a Subject Source!)
       // 3. All other Subject Sources
       Vector<SignetSubject> privSubjs = new Vector<SignetSubject>(signet.getPersistentDB().getSubjects());
       
       //
       // Create the DN that is used as a base for adding subjects.
       //
       String testSubjectsDn = testSubjectsRdn + "," + rootDn;

       //
       // For each privileged subject, process the subjects
       //
       Iterator privSubjectIterator = privSubjs.iterator();
       LdapContext ctx = LdapUtil.getLdapContext();        
       int numberOfSubjects = privSubjs.size();

       if (numberOfSubjects > 0)
       {
           //
           // Create the base DN to which subjects will be added.
           //

           buildSubjectLdapBase(ctx, testSubjectsRdn, testSubjectsDn, subjectOuName);

           //
           // Loop over privileged subjects adding them to the LDAP directory
           //

           while(privSubjectIterator.hasNext())
           {
               //
               // Get the privileged subject
               //
               SignetSubject subject = (SignetSubject) privSubjectIterator.next();

               //
               // TEMPORARY FIX: Don't process the "signet" subject. 
               // TODO: Find a better way
               //
               if (subject != null && "Super_SignetSubject".equalsIgnoreCase(subject.getId())
                       && "application".equalsIgnoreCase(subject.getType().getName()))
               {
                   continue;
               }

               // example of testSubjectsDn: "ou=testSubjects,ou=testgrouper,dc=my-domain,dc=com"; 

               Subject subj = signet.getSubject(subject.getSourceId(), subject.getId());

               sourceId = subject.getSourceId();
               // Write this subject to the LDAP directory
               buildSubjectLdapEntry(ctx, subj, testSubjectsDn, sourceId);

               //
               // TODO: Reinstate the following code and figure out why it occurs.
               //       It is apparently a problem with the Signet API.
               //
               /*
               if (!"person".equals(subject.getType().getName()) )
               {
                   failureMessage = "\nTODO: Fix problem in SignetSubjectProvisioner got type: " 
                       + subject.getType().getName() 
                       + "  \ninstead of expected type of person.  Privileged subject indicates person," 
                       + "\nbut subject indicates group, database indicates person for both.";
               }
               */
           }
       }
       else
       {
           ErrorLog.warn(this.getClass(), 
                   "No privileged subjects to provision from SignetSubjectProvisioner");
       }
       return failureMessage;
   }

   /**
     * This create an DN entry under which to place the test subjects used
     * for testing purposes.
     * It replaces all existing data under the testSubjectOu + rootDn entry.
     * 
     * @param ctx
     *         Ldap context
     * @param testSubjectsRdn
     *         Defines the RDN of the entry to be used as the base under which
     *         all test subject directory entries are placed.
     * @param testSubjectsDn
     *         Defines the DN of the entry to be used as the base under which
     *         all test subject directory entries are placed.
     * @param subjectOuName
     *         Name of the OU under which the subject entries will be created
     * @throws javax.naming.NamingException
     *         thrown if a Naming exception occurs
     */
    protected void buildSubjectLdapBase(LdapContext ctx, String testSubjectsRdn, String testSubjectsDn,
            String subjectOuName)
            throws NamingException
    {
        Attributes attributes = new BasicAttributes(true); 
        attributes.put(new BasicAttribute("objectclass", "top"));
        attributes.put(new BasicAttribute("objectclass", "organizationalUnit"));
        attributes.put(new BasicAttribute("ou", subjectOuName));
      
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
            // Okay if already bound, just ignore.
        }
        catch (NamingException ne)
        {
            ErrorLog.error(this.getClass(), 
                    "Naming exception creating subcontext: " 
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
     *            Name of the DN under which the subject entries will be created
     * @param sourceId
     *            The source id
     * @throws javax.naming.NamingException
     *             thrown if a Naming exception occurs
     */
    protected void buildSubjectLdapEntry(LdapContext ctx, Subject subject, String testSubjectsDn,
            String sourceId)
            throws NamingException
    {
        // TODO: make sure this is a person

        //
        // Get the LDAP search filter for the source
        //
        ConfigManager configuration = ConfigManager.getInstance();
        LdapSearchFilter filter = configuration.getSourceSubjectLdapFilter(sourceId); 
        String sourceNameAttr = configuration.getSourceSubjectNamingAttribute(sourceId);
//        String subjectIdentifier = subject.getAttributeValue(sourceNameAttr);
        String subjectIdentifier = subject.getAttributeValue("subjectAuthId");

        //
        // Create an attribute list for creating the new subject entry.
        //
        Attributes attributes = new BasicAttributes(true);

        //
        // Define the RDN attribute name and set its value
        //
        final String rdnAttribute = "uid";
        //String rdnValue = subject.getId();
        String rdnValue = subjectIdentifier;
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
            ErrorLog.error(this.getClass(), "Naming exception creating subcontext: " 
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
