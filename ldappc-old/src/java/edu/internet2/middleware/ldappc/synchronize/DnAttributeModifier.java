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

package edu.internet2.middleware.ldappc.synchronize;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

/**
 * This is an AttributeModifier for modifying LDAP attribute values that
 * are known to hold DN strings. This class currently assumes that the "no value" 
 * and the values it is initialized with via the attribute are valid DN strings.
 * No validation is currently done to enforce this.
 */
public class DnAttributeModifier extends AttributeModifier
{
    /**
     * Name parser to use for converting DN strings to Name objects
     */
    private NameParser parser;

    /**
     * Constructs a <code>DnAttributeModifier</code> for the attribute name without a "no value".
     * 
     * @param parser
     *            Name parser
     * @param attributeName
     *            Name of the attribute
     */
    public DnAttributeModifier(NameParser parser, String attributeName)
    {
        this(parser, attributeName, DEFAULT_NO_VALUE);
    }

    /**
     * Constructs a <code>DnAttributeModifier</code> for the attribute name
     * with the given "no value" value.
     * 
     * @param parser
     *            Name parser
     * @param attributeName
     *            Name of the attribute
     * @param noValue
     *            "no value" value (null if the attribute is not required).
     */
    public DnAttributeModifier(NameParser parser, String attributeName,
            String noValue)
    {
        super(attributeName, noValue);
        this.parser = parser;
    }

    /**
     * This method determines whether or not two DN strings are equal.
     * 
     * @param leftStr
     *            String value
     * @param rightStr
     *            String value
     * @return <code>true</code> if the two strings are equal, and
     *         <code>false</code> otherwise
     * @return NamingException thrown if an error occurs parsing the names
     */
    protected boolean isEqual(String leftStr, String rightStr)
            throws NamingException
    {
        //
        // Assume the strings are not equal
        //
        boolean equal = false;

        //
        // Determine equality
        //
        if (leftStr == null)
        {
            equal = (rightStr == null);
        }
        else if (rightStr == null)
        {
            equal = false;
        }
        else
        {
            // 
            // Convert rightStr and leftStr to Names, and compare
            //
            Name rightName = convertDnString(rightStr);
            Name leftName = convertDnString(leftStr);

            //
            // Determine if equal
            //
            equal = rightName.equals(leftName);
        }

        return equal;
    }

    /**
     * Converts DN string into a Name.
     * 
     * @param dnString
     *            non-null DN string
     * @return Name
     * @throws NamingException
     *             thrown if an error occurs parsing the DN string
     */
    protected Name convertDnString(String dnString) throws NamingException
    {
        // 
        // This method was defined so caching could be added here if necessary
        //
        return parser.parse(dnString);
    }
}
