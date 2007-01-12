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

import java.util.Set;

/**
 * This interface defines the common configuration functionality required by the
 * Signet provisioner.
 */
public interface SignetProvisionerConfiguration extends
        ProvisionerConfiguration
{
    /**
     * Indicates storing the permissions listing as a string
     */
    public static final String PERMISSIONS_LISTING_STRING = "string";

    /**
     * Indicates storing the permissions listing as eduPermission objects
     */
    public static final String PERMISSIONS_LISTING_EDU_PERMISSION = "eduPermission";

    /**
     * This get the storage method for permission listings.
     * 
     * @return Either {@link #PERMISSIONS_LISTING_EDU_PERMISSION} or
     *         {@link #PERMISSIONS_LISTING_STRING}.
     */
    public String getPermissionsListingStoredAs();

    /**
     * This gets the Subject LDAP entry object class for storing the permission
     * listing when the storage method is
     * {@link edu.internet2.middleware.ldappc.SignetProvisionerConfiguration#PERMISSIONS_LISTING_STRING}.
     * 
     * @return Subject LDAP entry object class for storing the permissions
     *         listing or <code>null</code> if not defined.
     */
    public String getPermissionsListingStringObjectClass();

    /**
     * This gets the Subject LDAP entry attribute for storing the permission
     * listing when the storage method is {@link #PERMISSIONS_LISTING_STRING}.
     * 
     * @return Subject LDAP entry attribute for storing the permissions listing.
     */
    public String getPermissionsListingStringAttribute();

    /**
     * This gets the prefix for creating the permission listing when the storage
     * method is {@link #PERMISSIONS_LISTING_STRING}.
     * 
     * @return Prefix to use when creating the permissions listing.
     */
    public String getPermissionsListingStringPrefix();

    /**
     * This gets the value to store in the permission listing string attribute
     * if there are no permissions found to store there.
     * 
     * @return String to place in the permissions listing string attribute if no
     *         permissions are found to store there, or <code>null</code> if
     *         not defined.
     */
    public String getPermissionsListingStringEmptyValue();

    /**
     * This method returns a possibly empty {@link java.util.Set} of the
     * subsystem IDs for creating permission subsystem queries.
     * 
     * @return Set of Permission subsystem ids.
     */
    public Set getPermissionsSubsystemQueries();

    /**
     * This method returns a possibly empty {@link java.util.Set} of the
     * function IDs for creating permission function queries.
     * 
     * @return Set of Permission function ids.
     */
    public Set getPermissionsFunctionQueries();
}