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

package edu.internet2.middleware.ldappc.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.subject.Subject;

/**
 * This provides utility methods for interacting with a LDAP directory.
 */
public final class LdapDisplayUtil
{
    /**
     * Prevent instantiation of LdapDisplayUtil.
     */
    private LdapDisplayUtil()
    {
    }

    /**
     * Given a subject, construct a printable representation of its attributes.
     * 
     * @param subject
     *            the subject to display.
     * 
     * @return the subject's attributes as a long text string.
     */
    static String getAttributesAsString(Subject subject)
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
                    String key = (String) it2.next();
                    System.out.println("                     Attribute key/value " + key + "   " + attributes.get(key));
                    attributesString.append("\n[" + key + "] " + attributes.get(key));
                }
            }
        }
        else
        {
            attributesString.append("Subject is null.");
        }
        System.out.println("DEBUG in LdapDisplayUtil.getAttributesAsString: " + attributesString);

        return attributesString.toString();
    }
}
