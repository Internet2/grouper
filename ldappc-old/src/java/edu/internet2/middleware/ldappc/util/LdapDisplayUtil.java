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

package edu.internet2.middleware.ldappc.util;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;










import edu.internet2.middleware.subject.Subject;

/**
 * This provides utility methods for interacting with a LDAP directory.
 */
public class LdapDisplayUtil
{
    /**
     * 
     * @return LdapContext
     */
    static public String getAttributesAsString(Subject subject)
    {
        StringBuffer attributesString = new StringBuffer(128);

        if (subject != null)
        {
            attributesString.append("\nSubject Name: " + subject.getName());
            attributesString.append("\nSubject Type: " + subject.getType());
            attributesString.append("\nSubject Type: " + subject.getId());
            attributesString.append("\nSubject Type: " + subject.getSource());
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
                    attributesString.append("\n[" + key + "] " + attributes.get(key));
                }
            }
        }
        else
        {
            attributesString.append("Subject is null.");
        }
        System.out.println("DEBUG in LdapDisplayUtil.getAttributesAsString: " 
                + attributesString);
            
        return attributesString.toString();
    }
}
