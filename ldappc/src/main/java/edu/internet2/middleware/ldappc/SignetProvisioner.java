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

package edu.internet2.middleware.ldappc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.ldappc.ldap.EduPermission;
import edu.internet2.middleware.ldappc.synchronize.EduPermissionSynchronizer;
import edu.internet2.middleware.ldappc.synchronize.PermissionSynchronizer;
import edu.internet2.middleware.ldappc.synchronize.StringPermissionSynchronizer;
import edu.internet2.middleware.ldappc.util.LdapSearchFilter;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.subject.Subject;

/**
 * This class provisions Signet data.
 */
public class SignetProvisioner extends Provisioner
{
    /**
     * Signet provisioner configuration
     */
    private SignetProvisionerConfiguration configuration;

    /**
     * Signet provisioner options
     */
    private SignetProvisionerOptions options;

    /**
     * Ldap Context
     */
    private LdapContext ldapCtx;

    /**
     * Constructs a <code>SignetProvisioner</code> with the given
     * configuration, options and Ldap context.
     * 
     * @param configuration
     *            SignetProvisionerConfiguration providing all configuration
     *            data.
     * @param options
     *            SignetProvisionerOptions providing values for the provisioning
     *            options.
     * @param ldapCtx
     *            Ldap context
     * @param subjectCache
     *            Subject cache to speed subject retrieval
     */
    public SignetProvisioner(SignetProvisionerConfiguration configuration,
            SignetProvisionerOptions options, LdapContext ldapCtx,
            SubjectCache subjectCache)
    {
        super(subjectCache);
        this.configuration = configuration;
        this.options = options;
        this.ldapCtx = ldapCtx;
    }

    /**
     * Provisions Permission data to the directory. This uses the options to
     * determine what Signet data to provision to the directory, and uses the
     * configuration to determine how the data is represented in the directory.
     * <p>
     * It is possible for exceptions to occur that prevent a portion of the data
     * from being provisioned, but do not have any effect on processing the
     * remaining data. If this occurs, a
     * {@link edu.internet2.middleware.ldappc.MultiErrorException} is thrown.
     * The MultiErrorException will hold all of the collected exceptions.
     * 
     * @throws javax.naming.NamingException
     *             thrown if a Naming error occurs
     * @throws MultiErrorException
     *             thrown if one or more exceptions occurred that did not need
     *             to stop all processing
     * @throws LdappcException
     *             thrown if an error occurs
     */
    public void provision() throws NamingException, MultiErrorException,
            LdappcException
    {
        //
        // Initialize a vector to hold all caught exceptions that should be
        // reported, but not immediately thrown
        //
        Vector caughtExceptions = new Vector();

        //
        // Build the set of all subjects with privileges
        //
        Set existingSubjectDns = buildSourceSubjectDnSet();

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
        // For each privileged subject, process the active permissions
        //
        for (SignetSubject privSubj : privSubjs) {
            if (privSubj == null)
            {
                //
                // Handle the exception
                //
                String errorData = getErrorData(privSubj, null, null);
                logThrowableWarning(null, errorData);

                //
                // Simply continue with the loop
                //
                continue;
            }

            //
            // Don't process the "signet" subject.
            //
            if (isSignetSubject(privSubj))
            {
                continue;
            }

            //
            // Try to get the subject's DN
            //
            Name subjectDn = null;
            try
            {
                subjectDn = subjectCache.findSubjectDn(ldapCtx, configuration, privSubj.getSourceId(), privSubj.getId());
            }
            catch(Exception e)
            {
                //
                // Log a warning and continue with the loop
                //
                String errorData = getErrorData(privSubj, privSubj, null);
                logThrowableWarning(e, errorData);
                continue;
            }

            //
            // Remove the subject DN from the set of existing subject DNs
            //
            existingSubjectDns.remove(subjectDn);

            //
            // get a synchronizer for the subjectDn and synchronize the Signet
            // permissions with
            // those in the directory
            //
            PermissionSynchronizer synchronizer = getSynchronizer(ldapCtx, subjectDn);
            try
            {
                synchronizer.synchronize(privSubj.getAssignmentsReceived());
            }
            catch(Exception e)
            {
                String errorData = getErrorData(privSubj, privSubj, subjectDn);
                logThrowableError(e, errorData);
                caughtExceptions.add(new LdappcException(errorData, e));
            }
        }

        //
        // Clear the memberships from any subject not processed above.
        //
        try
        {
            clearSubjectEntryPrivileges(ldapCtx, existingSubjectDns);
        }
        catch(Exception e)
        {
            logThrowableError(e);
            caughtExceptions.add(e);
        }

        //
        // If there were caughtExceptions throw a multiple error exception
        //
        if (caughtExceptions.size() > 0)
        {
            throw new MultiErrorException((Exception[]) caughtExceptions
                    .toArray(new Exception[0]));
        }
    }

    /**
     * Returns <code>true</code> if the subject is the Signet subject
     * 
     * @param subject
     *            Subject
     * @return <code>true</code> if subject is the Signet subject, and
     *         <code>false</code> otherwise
     */
    protected boolean isSignetSubject(Subject subject)
    {
        return (subject != null && "Super_SignetSubject".equalsIgnoreCase(subject.getId()) && "application"
                .equalsIgnoreCase(subject.getType().getName()));
    }

    /**
     * This returns the <code>PermissionsSynchronizer</code> to be used for
     * provisioning. The synchronizer chosen is determined by the configuration.
     * 
     * @param ctx
     *            Ldap context the synchronizer must use
     * @param subjectDn
     *            DN of the subject whose permissions are being provisioned
     * @return Permission synchronizer
     * @throws javax.naming.NamingException
     *             thrown if a Naming error occurs
     */
    protected PermissionSynchronizer getSynchronizer(LdapContext ctx,
            Name subjectDn) throws NamingException
    {
        //
        // Init the return value and build a synchronizer based on the
        // configuration
        //
        PermissionSynchronizer synchronizer = null;
        if (SignetProvisionerConfiguration.PERMISSIONS_LISTING_EDU_PERMISSION
                .equals(configuration.getPermissionsListingStoredAs()))
        {
            synchronizer = new EduPermissionSynchronizer(ctx, subjectDn,
                    configuration, options, subjectCache);
        }
        else
        {
            synchronizer = new StringPermissionSynchronizer(ctx, subjectDn,
                    configuration, options, subjectCache);
        }
        return synchronizer;
    }

    /**
     * Builds an error data string based on the objects provided. It is
     * <b>assumed</b> that all three objects are related.
     * 
     * @param privSubj
     *            Privileged Subject
     * @param subject
     *            Subject associated with <code>privSubj</code>
     * @param subjectDn
     *            DN of <code>subject</code>'s LDAP entry
     * @return data string
     */
    protected String getErrorData(SignetSubject privSubj, Subject subject,
            Name subjectDn)
    {
        String errorData = "PRIVILEGED SUBJECT";

        if (privSubj != null)
        {
            errorData += "[ NAME = " + privSubj.getName() + " ][ ID = "
                    + privSubj.getId() + " ]";
        }
        if (subject != null)
        {
            errorData += "[ SUBJECT " + subjectCache.getSubjectData(subject) + " ]";
        }
        if (subjectDn != null)
        {
            errorData += "[ SUBJECT DN = " + subjectDn + " ]";
        }

        return errorData;
    }

    /**
     * Builds the subject DN set. The subject DN set is the union of each set
     * created from each source id using the subject source identifier Ldap
     * filter provided by the <code>configuration</code>. Each entry that is
     * identified by the filter and that has either the privilege listing
     * attribute populated when permissions are provisioned as strings or has
     * eduPermission children when permission are provisioned as eduPermissions
     * is included in the subject DN set.
     * 
     * @return Set of subject DNs
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    protected Set buildSourceSubjectDnSet() throws NamingException
    {
        //
        // Init the map to return
        //
        HashSet subjectDns = new HashSet();

        //
        // Get the source to subject ldap filter mapping from the configuration
        //
        Map sourceFilterMap = configuration.getSourceSubjectLdapFilters();

        //
        // Iterate over the sourceFilterMap to build list of subjects for each
        // source
        //
        Iterator sources = sourceFilterMap.keySet().iterator();
        while(sources.hasNext())
        {
            //
            // Get the source id and associated filter
            //
            String source = (String) sources.next();
            LdapSearchFilter filter = (LdapSearchFilter) sourceFilterMap
                    .get(source);

            //
            // Add the subjectDns for this source
            //
            addSubjectDnSet(subjectDns, filter);
        }

        return subjectDns;
    }

    /**
     * Adds identified subject DNs the given set of subject DNs. The subject DN
     * set added is created by identifying all DNs satisfying the
     * LdapSearchFilter and having the either the permission listing attribute
     * populated when permissions are provisioned as strings or having
     * eduPermission children when permission are provisioned as eduPermissions.
     * 
     * @param subjectDns
     *            Set of subject DNs
     * @param filter
     *            Ldap search filter defined for a source
     * @throws NamingException
     *             thrown if a Naming error occurs.
     */
    private void addSubjectDnSet(Set subjectDns, LdapSearchFilter filter)
            throws NamingException
    {
        //
        // Determine if permissions are being provisioned as eduPermissions
        //
        boolean isEduPermission = SignetProvisionerConfiguration.PERMISSIONS_LISTING_EDU_PERMISSION
                .equals(configuration.getPermissionsListingStoredAs());

        //
        // Build the search control
        //
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(filter.getScope());
        searchControls.setCountLimit(0);

        //
        // Build the ldap query filter by replacing "{0}" from the ldap search
        // filter with "*" and adding the list attribute filter
        //
        String filterExpr = LdapUtil.convertParameterToAsterisk(filter
                .getFilter(), 0);

        //
        // If necessary, add a filter for the permission list attribute and
        // permission list object class if defined
        //
        if (!isEduPermission)
        {
            //
            // Get the attribute and object class
            //
            String listAttribute = configuration
                    .getPermissionsListingStringAttribute();
            if (listAttribute == null)
            {
                throw new LdappcConfigurationException(
                        "Permissions list attribute is null");
            }

            String listObjectClass = configuration
                    .getPermissionsListingStringObjectClass();

            //
            // Make the filterExpr an '&' expression with list attribute, and if
            // possible the list object class
            //
            filterExpr = filterExpr + "(" + listAttribute + "=*)";

            if (listObjectClass != null)
            {
                filterExpr = filterExpr + "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE
                        + "=" + listObjectClass + ")";
            }

            filterExpr = "(&" + filterExpr + ")";
        }

        //
        // Build the base DN
        //
        NameParser parser = ldapCtx.getNameParser(LdapUtil.EMPTY_NAME);
        Name baseDn = parser.parse(filter.getBase());

        //
        // perform the search
        //
        NamingEnumeration searchResults = ldapCtx.search(baseDn, filterExpr,
                searchControls);

        //
        // Init vars needed to process the search results when provisioning
        // permissions as eduPermissions
        //
        SearchControls eduPermissionControls = null;
        String eduPermissionFilter = null;
        if (isEduPermission)
        {
            //
            // Build the search control needed to determine if the subject has
            // eduPermission children
            //
            eduPermissionControls = new SearchControls();
            eduPermissionControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            eduPermissionControls.setCountLimit(1);

            //
            // Build the ldap query filter to determine if the subject has
            // eduPermission children
            //
            eduPermissionFilter = "(" + LdapUtil.OBJECT_CLASS_ATTRIBUTE + "="
                    + EduPermission.OBJECT_CLASS + ")";

        }

        //
        // Process the search results
        //
        while(searchResults.hasMore())
        {
            //
            // Get the search result
            //
            SearchResult searchResult = (SearchResult) searchResults.next();

            //
            // Build the DN for the search result
            //
            Name subjectDn = parser.parse(searchResult.getName());
            subjectDn = subjectDn.addAll(0, baseDn);

            //
            // if storing permissions as eduPermission, make sure this has
            // eduPermission children before adding it to the set
            //
            if (isEduPermission)
            {
                //
                // if the subject does not have any eduPermission children,
                // simply continue with the next subject Dn
                //
                NamingEnumeration eduPermissionResults = ldapCtx.search(
                        subjectDn, eduPermissionFilter, eduPermissionControls);
                if (!eduPermissionResults.hasMore())
                {
                    continue;
                }
            }

            //
            // Save the subject dn
            //
            subjectDns.add(subjectDn);
        }
    }

    /**
     * Clears the privileges from subject entries.
     * 
     * @param ctx
     *            Ldap context
     * @param subjectDnSet
     *            Set of subject DNs whose memberships are to be cleared
     * @throws NamingException
     *             thrown if a Naming error occurs
     */
    private void clearSubjectEntryPrivileges(LdapContext ctx, Set subjectDnSet)
            throws NamingException
    {
        //
        // Define an empty set that is used below
        //
        Set emptySet = new HashSet();

        //
        // Iterate over the subject DNs
        //
        Iterator subjectDns = subjectDnSet.iterator();
        while(subjectDns.hasNext())
        {
            Name subjectDn = (Name) subjectDns.next();
            try
            {
                //
                // Get a privilege synchronizer and synchronize with an empty
                // set.
                // (Doing it this way ensures that required attributes are
                // handled correctly).
                //
                PermissionSynchronizer synchronizer = getSynchronizer(ctx,
                        subjectDn);
                synchronizer.synchronize(emptySet);
            }
            catch(Exception e)
            {
                logThrowableError(e);
            }
        }
    }
}
