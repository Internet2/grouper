package edu.internet2.middleware.grouper.pspng;


/*******************************************************************************
 * Copyright 2015 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang.BooleanUtils;
import org.ldaptive.LdapException;
import org.ldaptive.SearchRequest;
import org.ldaptive.pool.BlockingConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;

/**
 * Collects all the various properties and makes them available to the provisioner.
 *
 * @author Bert Bee-Lindgren
 */
public class LdapProvisionerConfiguration extends ProvisionerConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(LdapProvisionerConfiguration.class);
    private static final String PARAMETER_NAMESPACE = "changeLog.consumer.";

    /**
     * How to find member objects (users, accounts, etc), EL based on a subject.
     * 
     * If either userSearchBaseDn or userSearchFilter  is null (which is the default), 
     * then users will not be fetched. This is okay for ldap provisioning that only
     * needs subject attributes. For instance, posixGroup provisioning only needs the
     * usernames of the accounts, which might be the SubjectId or SubjectIdentifier.
     * 
     */
    private String userSearchBaseDn;
    protected String userSearchBaseDn_defaultValue = null;
    private String userSearchFilter;
    protected String userSearchFilter_defaultValue = null;
    
    // Where to create users if createMissingUsers is enabled
    private String userCreationBaseDn;
    protected String userCreationBaseDn_defaultValue = null;

    /** 
     * LDIF used to create a new user. The DN of this LDIF should be relative
     * to the userCreationBaseDn. 
     */
    private String userCreationLdifTemplate;
    protected String userCreationLdifTemplate_defaultValue = null;

    
    private String ouCreationLdifTemplate;
    protected String ouCreationLdifTemplate_defaultValue = "dn: ${dn}||ou: ${ou}||objectclass: organizationalunit";
    
    /** An array of attributes needed by the userSearchFilter. 
     * These are used to make sure the cached user LdapObjects have the necessary data
     * to run the userSearchFilter in memory after a bunch of users are found via bulk searching.
     * 
     * TODO: Parse the filter to see what attributes it uses.
     * 
     * By default this will be uid,mail,samAccountName
     */
    private String userSearchAttributes[];
    protected String userSearchAttributes_defaultValue = "cn,uid,uidNumber,mail,samAccountName,objectclass";
    
    protected int ldapUserCacheSize_defaultValue = 10000;

    private boolean isActiveDirectory;
    protected boolean isActiveDirectory_defaultValue = false;

    /**
     * How many ldap values should be changed (added or deleted) in each
     * operation. 0 ==> Don't break large operations up into smaller ones,
     * (Do all the changes in one ldap operation)
     */
    private int maxValuesToChangePerOperation;
    protected int maxValuesToChangePerOperation_defaultValue = 100;

    // LdapAttributes that hold DNs and, therefore, need their values to be dn-escaped
    private String attributesNeededingDnEscaping[];
    protected String attributesNeededingDnEscaping_defaultValue = "dn,distinguishedName,entrydn,uniqueMember,member";

    // Allow the null (empty) DN as an attribute value for attributes that hold DNs
    private boolean allowEmptyDnAttributeValues;
    protected boolean allowEmptyDnAttributeValues_defaultValue = false;

    private String ldapPoolName;
    private Properties ldaptiveProperties = new Properties();
    private BlockingConnectionPool ldapPool;
    
    public LdapProvisionerConfiguration(String provisionerName) {
      super(provisionerName);
    }
    
    @Override
    public void readConfiguration() {
      super.readConfiguration();
      final String qualifiedParameterNamespace = PARAMETER_NAMESPACE + provisionerName + ".";

      LOG.debug("Ldap Provisioner - Setting properties for {} consumer/provisioner.", provisionerName);

      isActiveDirectory = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "isActiveDirectory", isActiveDirectory_defaultValue);
      LOG.debug("Ldap Provisioner {} - Setting isActiveDirectory to {}", provisionerName, isActiveDirectory);

      ldapPoolName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "ldapPoolName");
      LOG.debug("Ldap Provisioner {} - Setting ldapPoolName to {}", provisionerName, ldapPoolName);
      
      maxValuesToChangePerOperation =
              GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "maxValuesToChangePerOperation", maxValuesToChangePerOperation_defaultValue);
      LOG.debug("Ldap Provisioner {} - Setting maxValuesToChangePerOperation to {}", provisionerName, maxValuesToChangePerOperation);

      userSearchBaseDn =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "userSearchBaseDn" , userSearchBaseDn_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting userSearchBaseDn to {}", provisionerName, userSearchBaseDn);

      userSearchFilter =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "userSearchFilter" , userSearchFilter_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting userSearchFilter to {}", provisionerName, userSearchFilter);

      String userSearchAttributeString =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "userSearchAttributes" , userSearchAttributes_defaultValue);
      userSearchAttributes = userSearchAttributeString.trim().split(" *, *");
      LOG.debug("Ldap Attribute Provisioner {} - Setting userSearchAttributes to {}", provisionerName, Arrays.toString(userSearchAttributes));
      
      userCreationBaseDn =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "userCreationBaseDn" , userCreationBaseDn_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting userCreationBaseDn to {}", provisionerName, userCreationBaseDn);

      userCreationLdifTemplate =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "userCreationLdifTemplate" , userCreationLdifTemplate_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting userCreationLdifTemplate to {}", provisionerName, userCreationLdifTemplate);
      
      ouCreationLdifTemplate =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "ouCreationLdifTemplate" , ouCreationLdifTemplate_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting ouCreationLdifTemplate to {}", provisionerName, ouCreationLdifTemplate);

      if (GrouperLoaderConfig.retrieveConfig().containsKey(qualifiedParameterNamespace + "ldapUserCacheTime_secs") ) {
        LOG.warn("Provisioner {}: The ldapUserCacheTime_secs property has been deprecated. Use the more general dataCacheTime_secs.", provisionerName);

        // If the general data-cache time is the default, set it to the ldap-user cache time
        if (getDataCacheTime_secs() == dataCacheTime_secs_defaultValue) {
          int ldapUserCacheTime_secs =
                  GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "ldapUserCacheTime_secs");
          LOG.warn("Ldap Provisioner {} - Setting dataCacheTime_secs from ldapUserCacheTime_secs: {}", provisionerName, ldapUserCacheTime_secs);
          dataCacheTime_secs = ldapUserCacheTime_secs;
        }
      }

      if (GrouperLoaderConfig.retrieveConfig().containsKey(qualifiedParameterNamespace + "ldapUserCacheSize") ) {
        LOG.warn("Provisioner {}: The ldapUserCacheSize property has been deprecated. Use the more general targetSystemUserCacheSize.", provisionerName);

        // If the general targetUserCacheSize is the default, set it to the ldap-user cache skze
        if (getTargetSystemUserCacheSize() == targetSystemUserCacheSize_defaultValue) {
          int ldapUserCacheSize =
                  GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "ldapUserCacheSize");
          LOG.warn("Ldap Provisioner {} - Setting targetSystemUserCacheSize from ldapUserCacheCacheSize: {}", provisionerName, ldapUserCacheSize);
          targetSystemUserCacheSize = ldapUserCacheSize;
        }
      }

      String attributesNeededingDnEscapingString =
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "attributesNeededingDnEscaping" , attributesNeededingDnEscaping_defaultValue);
      attributesNeededingDnEscaping = attributesNeededingDnEscapingString.trim().split(" *, *");

      allowEmptyDnAttributeValues = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "allowEmptyDnAttributeValues", allowEmptyDnAttributeValues_defaultValue);
      LOG.debug("Ldap Provisioner {} - Setting allowEmptyDnAttributeValues to {}", provisionerName, allowEmptyDnAttributeValues);

      // Lower-case the attributes
      for ( int i=0; i<attributesNeededingDnEscaping.length; i++ ) {
          attributesNeededingDnEscaping[i] = attributesNeededingDnEscaping[i].toLowerCase();
      }
      LOG.debug("Ldap Attribute Provisioner {} - Setting attributesNeededingDnEscaping to {}", provisionerName, Arrays.toString(attributesNeededingDnEscaping));
    }

    public int getMaxValuesToChangePerOperation() {
      return maxValuesToChangePerOperation;
    }
    

    
    public String getLdapPoolName() {
      return ldapPoolName;
    }

    public String getUserSearchBaseDn() {
      return userSearchBaseDn;
    }

    
    public String getUserSearchFilter() {
      return userSearchFilter;
    }
    
    public void addUserSearchAttribute(String attribute) {
      for ( String a : userSearchAttributes )
        if ( a.equalsIgnoreCase(attribute))
          return;

      userSearchAttributes = Arrays.copyOf(userSearchAttributes, userSearchAttributes.length+1);
      userSearchAttributes[userSearchAttributes.length-1] = attribute;
    }
    
    public String[] getUserSearchAttributes() {
      return userSearchAttributes;
    }


    public String getUserCreationBaseDn() {
      return userCreationBaseDn;
    }

    
    public String getUserCreationLdifTemplate() {
      return userCreationLdifTemplate;
    }

    public boolean isActiveDirectory() {
      return isActiveDirectory;
    }
   
    public void populateElMap(Map<String, Object> variableMap) {
      super.populateElMap(variableMap);
      variableMap.put("userSearchBaseDn", getUserSearchBaseDn());
    }

    
    public String getOuCreationLdifTemplate() {
      return ouCreationLdifTemplate;
    }

    public String[] getAttributesNeededingDnEscaping() { return attributesNeededingDnEscaping; }

    public boolean allowEmptyDnAttributeValues() {
      return allowEmptyDnAttributeValues;
    }
}
