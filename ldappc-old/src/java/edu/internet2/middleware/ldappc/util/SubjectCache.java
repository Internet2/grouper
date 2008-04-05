/*
 * Copyright (c) 2007 Kathryn Huxtable
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: SubjectCache.java,v 1.3 2008-04-05 04:30:45 khuxtable Exp $
 */
package edu.internet2.middleware.ldappc.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.EntryNotFoundException;
import edu.internet2.middleware.ldappc.MultipleEntriesFoundException;
import edu.internet2.middleware.ldappc.ProvisionerConfiguration;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Kathryn Huxtable
 * 
 */
public class SubjectCache
{
    private static final int                     DEFAULT_HASH_ESTIMATE = 25000;
    private Map<String, Hashtable<String, Name>> subjectDNTables       = new HashMap<String, Hashtable<String, Name>>();
    private Map<String, Hashtable<Name, String>> subjectIDTables       = new HashMap<String, Hashtable<Name, String>>();
    int                                          subjectDNLookups;
    int                                          subjectDNTableHits;
    int                                          subjectIDLookups;
    int                                          subjectIDTableHits;

    /**
     * @return the subjectDNLookups
     */
    public int getSubjectDNLookups()
    {
        return subjectDNLookups;
    }

    /**
     * @return the subjectDNTableHits
     */
    public int getSubjectDNTableHits()
    {
        return subjectDNTableHits;
    }

    /**
     * @return the subjectIDLookups
     */
    public int getSubjectIDLookups()
    {
        return subjectIDLookups;
    }

    /**
     * @return the subjectIDTableHits
     */
    public int getSubjectIDTableHits()
    {
        return subjectIDTableHits;
    }

    public void init(ProvisionerConfiguration configuration)
    {
        //
        // Initialize the hash tables mapping between RDN and subject ID.
        // Use the estimate in the config file if present
        //
        Map<String, Integer> estimates = configuration
                .getSourceSubjectHashEstimates();
        for (String source : configuration.getSourceSubjectLdapFilters().keySet())
        {
            int estimate = DEFAULT_HASH_ESTIMATE;
            if (estimates.get(source) != null)
            {
                estimate = estimates.get(source);
            }

            if (subjectDNTables.get(source) == null)
            {
                subjectDNTables.put(source, new Hashtable<String, Name>(
                        estimate));
            }
            else
            {
                subjectDNTables.get(source).clear();
            }

            if (subjectIDTables.get(source) == null)
            {
                subjectIDTables.put(source, new Hashtable<Name, String>(
                        estimate));
            }
            else
            {
                subjectIDTables.get(source).clear();
            }
        }
    }

    /**
     * Returns subject data string without attributes
     * 
     * @param subject
     *            Subject
     * @return subject data string
     */
    public String getSubjectData(Subject subject)
    {
        return getSubjectData(subject, false);
    }

    /**
     * Returns subject data string
     * 
     * @param subject
     *            Subject
     * @param attributes
     *            include attribute values
     * @return subject data string
     */
    public String getSubjectData(Subject subject, boolean attributes)
    {
        String subjData = "null";
        if (subject != null)
        {
            subjData = "[ NAME = " + subject.getName() + " ][ ID = "
                    + subject.getId() + " ]";
            if (attributes)
            {
                subjData += "[ ATTRIBUTES = " + subject.getAttributes() + " ]";
            }
        }
        return subjData;
    }

    /**
     * This returns the DN of the subject. This uses the query filter defined
     * for the <code>sourceId</code> in the configuration to search for the
     * <code>subject</code>.
     * 
     * @param ctx
     *            Ldap context
     * @param configuration
     *            Provisioner configuration
     * @param subject
     *            Subject
     * @return Subject's DN
     * @throws javax.naming.NamingException
     *             thrown if a Naming exception occurs
     * @throws EntryNotFoundException
     *             thrown if the entry could not be located
     * @throws MultipleEntriesFoundException
     *             thrown if the search found more than one entry
     */
    public Name findSubjectDn(LdapContext ctx,
            ProvisionerConfiguration configuration, Subject subject) throws NamingException, MultipleEntriesFoundException, EntryNotFoundException
    {
        //
        // Get the subject's source and source id
        //
        Source source = subject.getSource();
        if (source == null)
        {
            throw new EntryNotFoundException("Subject [ "
                    + getSubjectData(subject) + " ] has a null source");
        }

        String sourceId = source.getId();

        //
        // Get the source name attribute
        //
        String sourceNameAttr = configuration
                .getSourceSubjectNamingAttribute(sourceId);
        if (sourceNameAttr == null)
        {
            throw new EntryNotFoundException("Subject [ "
                    + getSubjectData(subject) + " ] source [ " + sourceId
                    + " ] does not identify a source subject naming attribute");
        }

        //
        // Get the subject's name attribute value
        //
        String sourceName = subject.getAttributeValue(sourceNameAttr);
        if (sourceName == null)
        {
            throw new EntryNotFoundException("Subject [ "
                    + getSubjectData(subject, true)
                    + " ] has no value for attribute [ " + sourceNameAttr
                    + " ]");
        }

        return findSubjectDn(ctx, configuration, sourceId, sourceName);
    }

    /**
     * This returns the DN of the identified subject. This uses the query filter
     * defined for the <code>sourceId</code> in the configuration to search
     * for the subject identified by <code>subjectIdentifier</code>. It will
     * return <code>null</code> if the subject is not found or multiple
     * entries match the search.
     * 
     * @param ldapCtx
     *            Ldap context
     * @param configuration
     *            Provisioner configuration
     * @param sourceId
     *            Source Id of the subject
     * @param subjectIdentifier
     *            Identifier string of the subject
     * @return Subject's DN
     * @throws javax.naming.NamingException
     *             thrown if a Naming exception occurs
     * @throws EntryNotFoundException
     *             thrown if the entry could not be located
     * @throws MultipleEntriesFoundException
     *             thrown if the search found more than one entry
     */
    public Name findSubjectDn(LdapContext ldapCtx,
            ProvisionerConfiguration configuration, String sourceId,
            String subjectIdentifier) throws NamingException, MultipleEntriesFoundException, EntryNotFoundException
    {
        //
        // Initialize error message suffix
        //
        String errorSuffix = "[ subject id = " + subjectIdentifier
                + " ][ source = " + sourceId + " ]";

        //
        // Get the LDAP search filter for the source
        //
        LdapSearchFilter filter = configuration
                .getSourceSubjectLdapFilter(sourceId);
        if (filter == null)
        {
            throw new EntryNotFoundException(
                    "Ldap search filter not found using " + errorSuffix);
        }

        //
        // Update the error suffix
        //
        errorSuffix += "[ filter = " + filter + " ]";

        //
        // Build all of pieces needed to search
        //
        NameParser parser = ldapCtx.getNameParser(LdapUtil.EMPTY_NAME);
        Name baseName = parser.parse(filter.getBase());

        subjectIDLookups++;
        Name subjectDn = subjectDNTables.get(sourceId).get(subjectIdentifier);
        if (subjectDn != null)
        {
            subjectIDTableHits++;
            return subjectDn;
        }

        String filterExpr = filter.getFilter();

        Object[] filterArgs = new Object[] { subjectIdentifier };

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(filter.getScope());

        //
        // As only 1 is wanted, if two are found the subjectName value
        // wasn't unique
        //
        searchControls.setCountLimit(2);

        //
        // Perform the search
        //
        NamingEnumeration namingEnum = ldapCtx.search(baseName, filterExpr,
                filterArgs, searchControls);

        //
        // If no entries where found, throw an exception
        //
        if (!namingEnum.hasMore())
        {
            throw new EntryNotFoundException("Subject not found using "
                    + errorSuffix);
        }

        //
        // Get the first result.
        //
        SearchResult searchResult = (SearchResult) namingEnum.next();

        //
        // After getting the first result, if there are more throw an exception
        // as the search result was not unique
        //
        if (namingEnum.hasMore())
        {
            throw new MultipleEntriesFoundException(
                    "Multiple entries found using " + errorSuffix);
        }

        //
        // If name is NOT relative, throw an exception
        //
        if (!searchResult.isRelative())
        {
            throw new EntryNotFoundException(
                    "Unable to resolve the reference found using "
                            + errorSuffix);
        }

        //
        // Build the subject's DN
        //
        subjectDn = parser.parse(searchResult.getName());
        subjectDn = subjectDn.addAll(0, baseName);

        subjectDNTables.get(sourceId).put(subjectIdentifier, subjectDn);
        subjectIDTables.get(sourceId).put(subjectDn, subjectIdentifier);

        return subjectDn;
    }
}
