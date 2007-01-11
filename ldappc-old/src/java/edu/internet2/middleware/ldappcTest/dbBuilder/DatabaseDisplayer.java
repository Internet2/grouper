/*
    Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
    Copyright 2004-2006 The University Of Chicago
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
  
      http://www.apache.org/licenses/LICENSE-2.0
  
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOU WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package edu.internet2.middleware.ldappcTest.dbBuilder;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.grouper.SubjectFinder;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.GrouperSubjectRetriever;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.ResourceBundleUtil;
import edu.internet2.middleware.ldappcTest.AllJUnitTests;

import javax.naming.Binding;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;

import java.util.Set;
import java.util.Map;
import java.util.Iterator;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

/**
 * Class for displaying a grouper database.
 * This is design specifically to display the testing
 * database will display parts of other databases.
 *
 * This class should display database containing the
 * following type of structure:
 *
 * testStem1
 *     teststem2
 *         teststem3
 *             testgroup1
 *                 testmember11 
 *                 testmember12 
 *                 testmember13 
 *             testgroup2
 *                 testmember21 
 *                 testmember22 
 *                 testmember23 
 * 
 * @author Gil Singer 
 */
public class DatabaseDisplayer
{

    /**
     * Empty name
     */
    static final String EMPTY_NAME = "";

    /**
     * Maximum number of subject error messages to be displayed.
     */
    static final int MAX_SUBJECT_ERRORS = 1;

    /**
     * Maximum number of stems to be displayed.
     */
    static final int MAX_STEMS = 5;

    /**
     * Maximum number of groups to be displayed.
     */
    static final int MAX_GROUPS = 5;

    /**
     * Maximum number of members to be displayed.
     */
    static final int MAX_MEMBERS = 3;

    /**
     * Maximum number of persons to be displayed.
     */
    static final int MAX_PERSONS = 3;

    /**
     * Maximum number of subjects to be displayed.
     */
    static final int MAX_SUBJECTS = 20;

    /**
     * the grouper session
     */
    private GrouperSession grouperSession;

    /**
     * the grouper subject retriever
     */
    private GrouperSubjectRetriever grouperSubjectRetriever;

    /**
     * a subject
     */
    private Subject subject;

    /**
     * the root stem
     */
    private Stem rootStem;

    /**
     * LDAP context for provisioning
     */
    private LdapContext ldapContext;
    
    /**
     * Constructor
     */
    public DatabaseDisplayer() 
    {
        try
        { 
            ldapContext = LdapUtil.getLdapContext();
        }
        catch(NamingException ne)
        { 
            ErrorLog.error(this.getClass(), "Could not create inital directory context -- naming exception: "
                    + ne.getMessage());
        }
        
        grouperSubjectRetriever = new GrouperSubjectRetriever();
        subject = grouperSubjectRetriever.findSubjectById("GrouperSystem");
         
        try 
        {
            grouperSession = GrouperSession.start(subject);
            DebugLog.info("Started GrouperSession: " + grouperSession);
        }
        catch (SessionException se) 
        {
            ErrorLog.error(this.getClass(), "Failed to start GrouperSession for subjectId= " 
                   + "GrouperSystem" + ":    "  + se.getMessage());
        }
        catch (Exception e) 
        {
            ErrorLog.error(this.getClass(), "Failed to find GrouperSession: "  + e.getMessage());
        }
         
        // Find root stem.
        rootStem = StemFinder.findRootStem(grouperSession);

    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) 
    {
         DatabaseDisplayer displayer = new DatabaseDisplayer();
         displayer.display();
    }
    

    /**
     * Display selected database contents.
     */
    public void display()
    {
    
        System.out.println("*** Display of Display Limits ***");
        System.out.println(" ");
        
        System.out.println("Maximum number of stems to be displayed = "
                + MAX_STEMS);

        System.out.println("Maximum number of groups to be displayed = "
                + MAX_GROUPS);

        System.out.println("Maximum number of members to be displayed = "
                + MAX_MEMBERS);

        System.out.println("Maximum number of persons to be displayed = "
                + MAX_PERSONS);

        System.out.println("Maximum number of subjects to be displayed = "
                + MAX_SUBJECTS);

        System.out.println("Maximum number of subject error messages to be displayed = "
                + MAX_SUBJECT_ERRORS);

        System.out.println(" ");
        //
        // Display Stem and Group Structure
        //
        
        displayStructure();
                   
        //
        // Display Subjects
        //
 
        displaySubjects();

        //
        // Display Contexts
        //

        String DN_TEST_BASE = ResourceBundleUtil.getString(AllJUnitTests.TEST_CONTEXT_BASE);
        displayContexts(DN_TEST_BASE);

        //
        // Display Bindings
        //

        displayBindings();
        System.out.println(" ");
    }

    /**
     * Display stem structure starting at the root stem.
     * This method is for development purposes only. 
     */
    public void displayStructure() 
    {
        System.out.println(" ");
        System.out.println("*** Display of Stem and Group Structure ***");
        System.out.println(" ");
        displayStructure(rootStem);
    }

    /**
     * Display stem structure starting at the specified stem.
     * This method is for development purposes only. 
     */
    public void displayStructure(Stem stemBase) 
    {
        
        Set stems = stemBase.getChildStems(); 
        //GroupProcessor groupProcessor = new GroupProcessor(grouperSession);
       

        if (stems.size() > 0 )
        {
            Iterator it = stems.iterator();
            Stem stem = null;
            int stemCount = 0;
            while (it.hasNext())
            {
                stemCount++;
                if (stemCount > MAX_STEMS)
                {
                    int additional = stems.size() - MAX_STEMS;
                    System.out.println(additional + " additional stems are not shown.");
                    break;
                }
                stem = (Stem)it.next();
                    System.out.println("Stem: " + stem.getExtension()  + "  Name: " 
                    + stem.getName() );
                Set groups = stem.getChildGroups();
                Iterator itg = groups.iterator();
                Group group = null;
                int groupCount = 0;
                while (itg.hasNext())
                {
                    groupCount++;
                    if (groupCount > MAX_GROUPS)
                    {
                        int additional = groups.size() - MAX_GROUPS;
                        System.out.println(additional + " additional groups are not shown.");
                        break;
                    }
                    group = (Group)itg.next();
                    System.out.println("Group: " + group.getExtension()  + "  Name: " 
                            + group.getName() );
                    displayGroupStructure(group);
                }
                displayStructure(stem);
            }
        }

    }
    /**
     * Display group structure
     * This method is for development purposes only. 
     */
    public void displayGroupStructure(Group group) 
    {
        
        Set members = group.getMembers(); 
       
        //
        // Display the Members of the Group
        //
        
        if (members.size() > 0)
        {
            System.out.println("Group: " + group.getName() + " has " + members.size() + " members.");
            Iterator it = members.iterator();
            Member member = null;
            int subjectErrorCount = 0;
            int subjectMemberCount = 0;
            while (it.hasNext())
            {
                subjectMemberCount++;
                if (subjectMemberCount > MAX_MEMBERS)
                {
                    int additional = members.size() - MAX_MEMBERS;
                    System.out.println(additional + " additional members are not shown.");
                    break;
                }
                member = (Member)it.next();
                //
                // Get the Member subject
                //
                
                Subject subject = null;
                try
                {
                    subject = member.getSubject();
                    System.out.println("Member subject = " + subject.getName());
                }
                catch(SubjectNotFoundException snfe)
                {
                    subjectErrorCount++;
                    if (MAX_SUBJECT_ERRORS != 0)
                    {
                        if (subjectErrorCount == MAX_SUBJECT_ERRORS+1)
                        {
                            //ErrorLog.error(this.getClass(), 
                            //        "Limiting number of 'Can not get subject' messages to " + MAX_SUBJECT_ERRORS);
                            //System.out.println("Limiting number of 'Can not get subject' messages to " 
                            //        + MAX_SUBJECT_ERRORS);    
                        }
                        if (subjectErrorCount <= MAX_SUBJECT_ERRORS)
                        {
                            ErrorLog.error(this.getClass(), "Can not get subject for one or more members.");
                            System.out.println("Can not get subject for one or more members.");
                        }
                    }
                }
            }
        }
        else
        {
            System.out.println("Group: " + group.getName() + " has no members.");
        }
    }
    /**
     * Display Subjects
     */
    public void displaySubjects() 
    {  
        System.out.println(" ");
        System.out.println( "*** Display of Subject Data for GrouperSystem ***");
        System.out.println(" ");
 
        //GrouperSubjectRetriever subjectRetriever = new GrouperSubjectRetriever();
        //Subject subject = subjectRetriever.findSubjectById("GrouperSystem");
    
        Set subjects = SubjectFinder.findAll("GrouperSystem");

        for (Iterator it = subjects.iterator(); it.hasNext();)
        {
            Subject subject = (Subject)it.next();
            System.out.println( "Id/TypeID/Name: " + subject.getId() + "   " + subject.getType() +
                    "    " + subject.getName());
        }
        
        System.out.println(" ");
        System.out.println( "*** Display of Subject Data for GrouperAll Subject ***");
        System.out.println(" ");
        subject = SubjectFinder.findAllSubject();

        System.out.println( "Id/TypeID/Name/Source: " + subject.getId() + "   " + subject.getType()
                + "    " + subject.getName()
                + "    " + subject.getSource());
        
        subjects = SubjectFinder.findAll("%");
        System.out.println(" ");
        System.out.println( "*** Display of Subject Data for All Subjects ***");
        System.out.println(" ");
        int personCount = 0;
        int groupCount = 0;
        int subjectCount = 0;
        for (Iterator it = subjects.iterator(); it.hasNext();)
        {

            Subject subject = (Subject)it.next();
            String type = subject.getType().getName();
            if (type.equals("application"))
            {
                 System.out.println( "Id/TypeID/Name/SourceId: " + subject.getId() + "   " + subject.getType()
                         + "    " + subject.getName()
                         + "    " + subject.getSource().getId());
                 Map attributes = subject.getAttributes();
                 if (attributes.size() > 0)
                 {
                     Set entrySet = attributes.entrySet();
                     Iterator it2 = entrySet.iterator();
                     while (it2.hasNext())
                     {
                         String key = (String)it2.next();
                         System.out.println( "                     Attribute key/value " 
                                 + key + "   " + attributes.get(key) );
                     }
                 }
                 subjectCount++;
            }
            else if (type.equals("person"))
            {
                personCount++;
                subjectCount++;
            }
            if ((type.equals("person") && personCount <= MAX_PERSONS))
            {
                System.out.println( "Id/TypeID/Name/Source: " + subject.getId() + "   " + subject.getType()
                         + "    " + subject.getName()
                         + "    " + subject.getSource());
            }
            else if (type.equals("group"))
            {
                groupCount++;
                subjectCount++;
            }
            if ((type.equals("group") && groupCount <= MAX_GROUPS))
            {
                System.out.println( "Id/TypeID/Name/Source: " + subject.getId() + "   " + subject.getType()
                        + "    " + subject.getName()
                        + "    " + subject.getSource());
            }
            if (subjectCount > MAX_SUBJECTS)
            {
                int additional = subjectCount - MAX_SUBJECTS;
                if (additional > 0)
                {
                    System.out.println(additional + " additional subjects not displayed due to Max Subjects reached.");
                }
                break;
            }
        }
        if (groupCount > MAX_GROUPS)
        {
            int additional = groupCount - MAX_GROUPS;
            if (additional > 0)
            {
                System.out.println(additional + " additional group subjects not displayed");
            }
        }

        if (personCount > MAX_PERSONS)
        {
            int additional = personCount - MAX_PERSONS;
            if (additional > 0)
            {
                System.out.println(additional + " additional person subjects not displayed");
            }
        }

    }
    
    /**
     * Display bindings
     */
    public void displayBindings()
    {
        //
        // Get the bindings for base.
        //
        // e.g.: displayBindings("dc=my-domain,dc=com");
        displayBindings(ResourceBundleUtil.getString(AllJUnitTests.TEST_CONTEXT_BASE));

        //
        // Get bindings for root OU
        //
        displayBindings(ConfigManager.getInstance().getGroupDnRoot());
    }
    
    /**
     * Display bindings for DN
     * @param dn the name of the directory context to be displayed 
     */
    public void displayBindings(String dn)
    {
        System.out.println("*** Display of " + dn + " Bindings ***");

        //
        // Get the bindings for base.
        //

        try
        {
            DirContext contextToDisplay = (DirContext)LdapUtil.getLdapContext().lookup(dn);

            NamingEnumeration childEnum = contextToDisplay.listBindings("");
    
            //
            // Process each child
            //
            while(childEnum.hasMore())
            {
                Binding binding = (Binding) childEnum.next();
                System.out.println("Binding name = " + binding.getName());
            }
            
        }
        catch(NamingException ne)
        {
            ErrorLog.error(this.getClass(), "Could not display bindings: " + ne.getMessage());
            System.out.println("Could not display bindings: " + ne.getMessage());
        }
    }

    
    /**
     * Display Contexts
     * @param context the context to be displayed
     * @return true if success 
     * This method is for development purposes only. 
     */
    public boolean displayContexts(String context) 
    {
        System.out.println("*** Display of Contexts ***");
        boolean success = true;
 
        if (context == null)
        {
            ErrorLog.fatal(this.getClass(), "Trying to display a null context.");
            success = false;
        }
        else
        {
            try
            {
                NamingEnumeration list = ldapContext.list(context);
        
                while (list.hasMore()) 
                {
                    NameClassPair nc = (NameClassPair)list.next();
                    System.out.println(nc);
                }
    
            } 
            catch (NamingException e) 
            {
                ErrorLog.error(this.getClass(), "In displayContexts, destroy failed: " + e.getMessage());
                success = false;
            }
            finally 
            {
                try 
                {
                    if (null != ldapContext) ldapContext.close();
                } 
                catch (NamingException ne) 
                {
                    ErrorLog.error(this.getClass(), "Could not close context: " + ne.getMessage());
                    success = false;
                }
            }
        }
        return success;
    }
    
}
