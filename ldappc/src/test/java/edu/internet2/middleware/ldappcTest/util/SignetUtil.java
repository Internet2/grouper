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

package edu.internet2.middleware.ldappcTest.util;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import edu.internet2.middleware.ldappc.LdappcConfigurationException;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Privilege;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;

/**
 * Class for examining Signet data
 *
 * @author Gil Singer 
 */
public class SignetUtil extends TestCase
{
    /**
     * a subject
     */
    private Subject subject;

    /**
     * Constructor
     */
    public SignetUtil(String name) 
    {
        super(name);
    }
    
    /**
     * This is a utility for displaying the data associated with
     * a Signet subject's privileges.
     */
    public static void displaySubjectPrivileges(String subjectTypeId, String subjectId) 
    {
        //
        // Find a PrivilegedSubject's permissions and display the
        // associated data. 
        //
        
        Signet signet = null;
        try
        {
            signet = new Signet();
        }
        catch (SignetRuntimeException sre)
        {
            LdappcConfigurationException ace = new LdappcConfigurationException(
                    "Failed to create Signet instance: Signet database may not be running.",
                    sre);
            throw(ace);
        }
        
        //
        // Get the PrivilegedSubject
        //
        SignetSubject privSubject = null;
        // example for uid lsaito, Saito, Lee      "person"       "SD00009"
        privSubject = signet.getSubject(subjectTypeId, subjectId);
        if (privSubject == null) { 
             ErrorLog.error(SignetUtil.class, "Could not find PrivilegedSubject.");
        } 

        Set privileges = privSubject.getPrivileges();
        Privilege privilege = null;
        Iterator it = privileges.iterator();
        while (it.hasNext())
        {
            privilege = (Privilege) it.next();
            Permission permission = privilege.getPermission();
            System.out.println("DEBUG in SignetProvisionerPermissionTest, permission.getId()="
                    + permission.getId());
            TreeNode scope = privilege.getScope();
            System.out.println("DEBUG in SignetProvisionerPermissionTest, scope.getName()="
                    + scope.getName());
            Set limitValues = privilege.getLimitValues();
            LimitValue limitValue = null;
            Limit limit = null;
            Iterator it2 = limitValues.iterator();
            while (it2.hasNext())
            {
                limitValue = (LimitValue)it2.next();
                limit = limitValue.getLimit(); 
                System.out.println("limitValue.getLimit().getName()="
                        + limit.getName());
                System.out.println("limitValue.getLimit().getId()="
                        + limit.getId());
                System.out.println("limitValue.getLimit().getSubsystem().getName()="
                        + limit.getSubsystem().getName());
                System.out.println("limitValue.getLimit().getSubsystem().getId()="
                        + limit.getSubsystem().getId());
                System.out.println("limitValue.getValue()="
                        + limitValue.getValue());
                ChoiceSet choiceSet = limitValue.getLimit().getChoiceSet();
                Set choices = choiceSet.getChoices();
                Choice choice = null;
                Iterator choiceIt = choices.iterator();
                while (choiceIt.hasNext())
                {
                    choice = (Choice)choiceIt.next();
                    System.out.println("choice.getDisplayValue=" + choice.getDisplayValue());
                    System.out.println("choice.getValue=" + choice.getValue());
                }
            }
        }
    }
}
