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
 * $Id: SubjectCache.java,v 1.4 2008-04-10 17:14:20 khuxtable Exp $
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
    private Map<String, Hashtable<String, Name>> subjectIdToDnTables   = new HashMap<String, Hashtable<String, Name>>();
    int                                          subjectIdLookups;
    int                                          subjectIdTableHits;

    /**
     * Return the count of subject ID lookups.
     * 
     * @return the subjectIDLookups
     */
    public int getSubjectIdLookups()
    {
        return subjectIdLookups;
    }

    /**
     * Return the count of subject ID table hits.
     * 
     * @return the subjectIDTableHits
     */
    public int getSubjectIdTableHits()
    {
        return subjectIdTableHits;
    }

    /**
     * Initialize the subject cache data structures.
     * 
     * @param configuration
     *            Configuration data from file.
     */
    public void init(ProvisionerConfiguration configuration)
    {
        //
        // Initialize the hash tables mapping between RDN and subject ID.
        // Use the estimate in the config file if present
        //
        Map<String, Integer> estimates = configuration.getSourceSubjectHashEstimates();
        for (String source : configuration.getSourceSubjectLdapFilters().keySet())
        {
            int estimate = DEFAULT_HASH_ESTIMATE;
            if (estimates.get(source) != null)
            {
                estimate = estimates.get(source);
            }

            if (subjectIdToDnTables.get(source) == null)
            {
                subjectIdToDnTables.put(source, new Hashtable<String, Name>(estimate));
            }
            else
            {
                subjectIdToDnTables.get(source).clear();
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
            subjData = "[ NAME = " + subject.getName() + " ][ ID = " + subject.getId() + " ]";
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
    public Name findSubjectDn(LdapContext ctx, ProvisionerConfiguration configuration,
            Subject subject) throws NamingException, MultipleEntriesFoundException, EntryNotFoundException
    {
        //
        // Get the subject's source and source id
        //
        Source source = subject.getSource();
        if (source == null)
        {
            throw new EntryNotFoundException("Subject [ " + getSubjectData(subject)
                    + " ] has a null source");
        }

        String sourceId = source.getId();

        //
        // Get the source name attribute
        //
        String sourceNameAttr = configuration.getSourceSubjectNamingAttribute(sourceId);
        if (sourceNameAttr == null)
        {
            throw new EntryNotFoundException("Subject [ " + getSubjectData(subject)
                    + " ] source [ " + sourceId
                    + " ] does not identify a source subject naming attribute");
        }

        //
        // Get the subject's name attribute value
        //
        String sourceName = subject.getAttributeValue(sourceNameAttr);
        if (sourceName == null)
        {
            throw new EntryNotFoundException("Subject [ " + getSubjectData(subject, true)
                    + " ] has no value for attribute [ " + sourceNameAttr + " ]");
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
    public Name findSubjectDn(LdapContext ldapCtx, ProvisionerConfiguration configuration,
            String sourceId, String subjectIdentifier) throws NamingException, MultipleEntriesFoundException, EntryNotFoundException
    {
        //
        // Initialize error message suffix
        //
        String errorSuffix = "[ subject id = " + subjectIdentifier + " ][ source = " + sourceId
                + " ]";

        //
        // Get the LDAP search filter for the source
        //
        LdapSearchFilter filter = configuration.getSourceSubjectLdapFilter(sourceId);
        if (filter == null)
        {
            throw new EntryNotFoundException("Ldap search filter not found using " + errorSuffix);
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

        subjectIdLookups++;
        Name subjectDn = subjectIdToDnTables.get(sourceId).get(subjectIdentifier);
        if (subjectDn != null)
        {
            subjectIdTableHits++;
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
        NamingEnumeration namingEnum = ldapCtx.search(baseName, filterExpr, filterArgs,
                searchControls);

        //
        // If no entries where found, throw an exception
        //
        if (!namingEnum.hasMore())
        {
            throw new EntryNotFoundException("Subject not found using " + errorSuffix);
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
            throw new MultipleEntriesFoundException("Multiple entries found using " + errorSuffix);
        }

        //
        // If name is NOT relative, throw an exception
        //
        if (!searchResult.isRelative())
        {
            throw new EntryNotFoundException("Unable to resolve the reference found using "
                    + errorSuffix);
        }

        //
        // Build the subject's DN
        //
        subjectDn = parser.parse(searchResult.getName());
        subjectDn = subjectDn.addAll(0, baseName);

        subjectIdToDnTables.get(sourceId).put(subjectIdentifier, subjectDn);

        return subjectDn;
    }
}
