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

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.ConfigManager;

/**
 * This provides utility methods for interacting with a LDAP directory.
 */
public class LdapUtil
{
    /**
     * Space character (i.e., ' ')
     */
    static final char SPACE = ' ';

    /**
     * LB Sign (i.e., '#')
     */
    static final char LB_SIGN = '#';

    /**
     * Comma (i.e., ',')
     */
    static final char COMMA = ',';

    /**
     * Plus Sign (i.e, '+')
     */
    static final char PLUS_SIGN = '+';

    /**
     * Double qoute (i.e., '"')
     */
    static final char DOUBLE_QUOTE = '"';

    /**
     * Backward slash (i.e., '\')
     */
    static final char BACKWARD_SLASH = '\\';

    /**
     * Less than (i.e., '<')
     */
    static final char LESS_THAN = '<';

    /**
     * Greater than (i.e., '>')
     */
    static final char GREATER_THAN = '>';

    /**
     * Semi-colon (i.e., ';')
     */
    static final char SEMI_COLON = ';';

    /**
     * Asterix (i.e., '*')
     */
    static final char ASTERIX = '*';

    /**
     * Left Parenthesis (i.e., '(')
     */
    static final char LEFT_PARENTHESIS = '(';

    /**
     * Right Parenthesis (i.e., ')')
     */
    static final char RIGHT_PARENTHESIS = ')';

    /**
     * Nul charcter
     */
    static final char NUL = '\u0000';

    /**
     * Object class attribute name
     */
    public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";

    /**
     * Empty name string for name parser
     */
    public static final String EMPTY_NAME = "";

    /**
     * Delimiter for multivalued RDNs
     */
    public static final String MULTIVALUED_RDN_DELIMITER = "+";

    /**
     * This deletes the subcontext of <code>context</code> identified by
     * <code>dn</code>. All subcontext of the context identified by
     * <code>dn</code> are deleted as well.
     * 
     * @param context
     *            Directory context
     * @param dn
     *            DN of the subcontext to delete
     * @return <code>true</code> if subcontext was found and deleted, and
     *         <code>false</code> if the subcontext was not found.
     * @throws NamingException
     *             thrown if a naming error occurs
     */
    static public boolean delete(DirContext context, Name dn)
            throws NamingException
    {
        //
        // Init return value
        //
        boolean success = true;

        //
        // Find the identified subcontext
        //
        DirContext subContext = null;
        try
        {
            subContext = (DirContext) context.lookup(dn);
        }
        catch(NamingException ne)
        {
            success = false;
        }

        //
        // If subContext found, then try to delete it
        //
        if (success)
        {
            //
            // Make sure the subcontext is empty
            //
            prune(subContext);

            //
            // Remove the subcontext
            //
            context.destroySubcontext(dn);
        }

        return success;
    }

    /**
     * This unbinds all descendent entries of given directory context.
     * 
     * @param context
     *            Directory context
     */
    static public void prune(DirContext context) throws NamingException
    {
        //
        // List each of the child context
        //
        NamingEnumeration childEnum = context.listBindings("");

        //
        // Prune each child and then unbind the child
        //
        while(childEnum.hasMore())
        {
            //
            // Get the child
            //
            Binding binding = (Binding) childEnum.next();
            DirContext child = (DirContext) binding.getObject();

            //
            // Prune the child and then remove the child
            // MUST be done in this order
            //
            prune(child);
            context.unbind(binding.getName());
        }
    }

    /**
     * This method converts '*','(',')' and the "null" character (i.e., 0x00)
     * to be a forward slash (i.e., \) and the two hex character value as 
     * defined in RFC2254. For example, the string "abc * efg" is converted 
     * to "abc \2a efg".
     * 
     * @param value
     *            String to make safe
     * @return Ldap filter value safe string
     */
    static public String makeLdapFilterValueSafe(String value)
    {
        StringBuffer safeBuf = new StringBuffer();
        if (value != null)
        {
            //
            // Get value as a char[]
            //
            char[] valueChars = value.toCharArray();

            for(int i = 0; i < valueChars.length; i++)
            {
                //
                // IMPORTANT
                // IMPORTANT: This switch takes advantage of "falling through"
                // IMPORTANT: behavior of switch.
                // IMPORTANT
                //
                switch (valueChars[i])
                {
                case ASTERIX:
                case LEFT_PARENTHESIS:
                case RIGHT_PARENTHESIS:
                case BACKWARD_SLASH:
                case NUL:
                    safeBuf.append(BACKWARD_SLASH);
                    if (valueChars[i] == 0)
                    {
                        //
                        // This makes sure '\u0000' is encoded as '\00' rather
                        // than '\0'
                        //
                        safeBuf.append("0");
                    }
                    safeBuf.append(Integer.toHexString(valueChars[i]));
                    break;
                default:
                    safeBuf.append(valueChars[i]);
                    break;
                }
            }
        }

        return (value == null ? value : safeBuf.toString());
    }

    /**
     * This method escapes all LDAP special characters in the <code>value</code>.
     * This way it is safe to use as part of an LDAP DN.
     * 
     * @param value
     *            String to make safe
     * @return LDAP name safe string
     */
    static public String makeLdapNameSafe(String value)
    {
        //
        // Must escape the following characters with a "\"
        // 1. A space or "#" character occurring at the beginning of the string
        // 2. A space character occurring at the end of the string
        // 3. One of the characters ",", "+", """, "\", "<", ">" or ";"
        //

        StringBuffer safeBuf = new StringBuffer();
        if (value != null)
        {

            //
            // Get value as a char[]
            //
            char[] valueChars = value.toCharArray();

            //
            // Determine index of beginning of trailing spaces
            //
            int trailingSpacesStart = valueChars.length;
            while(trailingSpacesStart > 0
                    && valueChars[trailingSpacesStart - 1] == SPACE)
            {
                trailingSpacesStart--;
            }

            //
            // Escape leading spaces and #
            //
            int index = 0;
            for(index = 0; index < trailingSpacesStart; index++)
            {
                if (valueChars[index] == SPACE || valueChars[index] == LB_SIGN)
                {
                    //
                    // Escape the space or # character
                    //
                    safeBuf.append(BACKWARD_SLASH);
                    safeBuf.append(valueChars[index]);
                }
                else
                {
                    //
                    // Hit non space or # character so break out of this loop
                    //
                    break;
                }
            }

            //
            // Escape ",", "+", """, "\", "<", ">" or ";"
            //
            for(; index < trailingSpacesStart; index++)
            {
                //
                // IMPORTANT
                // IMPORTANT: This switch takes advantage of "falling through"
                // IMPORTANT: behavior of switch.
                // IMPORTANT
                //
                switch (valueChars[index])
                {
                case COMMA:
                case PLUS_SIGN:
                case DOUBLE_QUOTE:
                case BACKWARD_SLASH:
                case LESS_THAN:
                case GREATER_THAN:
                case SEMI_COLON:
                    safeBuf.append(BACKWARD_SLASH);
                default:
                    safeBuf.append(valueChars[index]);
                    break;
                }
            }

            //
            // Escape trailing spaces
            //
            for(; index < valueChars.length; index++)
            {
                safeBuf.append(BACKWARD_SLASH);
                safeBuf.append(valueChars[index]);
            }
        }

        return (value == null ? value : safeBuf.toString());
    }

    /**
     * This creates an {@link javax.naming.ldap.LdapContext}. The environment
     * properties are defined by
     * {@link edu.internet2.middleware.ldappc.ConfigManager#getLdapContextParameters()}.
     * No connection request controls are used.
     * 
     * @return LdapContext
     * @throws javax.naming.NamingException
     *             if a naming exception is encountered
     */
    static public LdapContext getLdapContext() throws NamingException
    {
        return getLdapContext(ConfigManager.getInstance()
                .getLdapContextParameters(), null);
    }

    /**
     * This creates an {@link javax.naming.ldap.LdapContext} with the given
     * environment properties and connection request controls.
     * 
     * @param environment
     *            environment used to create the initial DirContext.
     *            <code>null</code> indicates an empty environment.
     * @param controls
     *            connection request controls for the initial context. If
     *            <code>null</code>, no connection request controls are used.
     * @return LdapContext
     * @throws javax.naming.NamingException
     *             if a naming exception is encountered
     */
    static public LdapContext getLdapContext(Hashtable environment,
            Control[] controls) throws NamingException
    {
        /*
        // 
        // DEBUG: Display the environment
        // 
        java.util.Enumeration envKeys = environment.keys();
        String key;
        System.out
                .println("DEBUG, Listing of Environmental variable for the LdapContext");
        while(envKeys.hasMoreElements())
        {
            key = (String) envKeys.nextElement();
            System.out.println("DEBUG, Key: " + key + " " + "value:"
                    + environment.get(key));
        }
        */
        return new InitialLdapContext(environment, controls);
    }

    /**
     * Converts a "{i}" ldap query filter parameter to "*" where i >= 0.
     * 
     * @param filter
     *            Ldap query filter
     * @param parameterIndex
     *            Index of the parameter to alter
     * @return A new ldap query filter with "{i}" replaced with "*", or an empty
     *         string if the filter is null.
     */
    static public String convertParameterToAsterisk(String filter,
            int parameterIndex)
    {
        String newFilter = "";
        if (filter != null)
        {
            String regExpr = "\\{\\s*?" + parameterIndex + "\\s*?\\}";
            String asterisk = "*";
            newFilter = filter.replaceAll(regExpr, asterisk);
        }
        return newFilter;
    }
}
