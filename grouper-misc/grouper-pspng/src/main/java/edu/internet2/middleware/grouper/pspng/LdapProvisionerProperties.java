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

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.props.BindConnectionInitializerPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.PoolConfigPropertySource;
import org.ldaptive.provider.unboundid.UnboundIDProvider;
import org.ldaptive.sasl.GssApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.ldap.GrouperLoaderLdapServer;

/**
 * Collects all the various properties and makes them available to the provisioner.
 *
 * @author Bert Bee-Lindgren
 */
public class LdapProvisionerProperties extends ProvisionerProperties {

    private static final Logger LOG = LoggerFactory.getLogger(LdapProvisionerProperties.class);
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
    
    private int ldapSearchResultPagingSize;
    protected int ldapSearchResultPagingSize_default_value = 100;
    
    private int ldapUserCacheTime_secs;
    protected int ldapUserCacheTime_secs_defaultValue = 600;
    
    private int ldapUserCacheSize;
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

    private String ldapPoolName;
    private Properties ldaptiveProperties = new Properties();
    private BlockingConnectionPool ldapPool;
    
    public LdapProvisionerProperties(String provisionerName) {
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
      LOG.debug("Ldap Attribute Provisioner {} - Setting userSearchAttributes to {}", provisionerName, userSearchAttributes);
      
      userCreationBaseDn =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "userCreationBaseDn" , userCreationBaseDn_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting userCreationBaseDn to {}", provisionerName, userCreationBaseDn);

      userCreationLdifTemplate =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "userCreationLdifTemplate" , userCreationLdifTemplate_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting userCreationLdifTemplate to {}", provisionerName, userCreationLdifTemplate);
      
      ouCreationLdifTemplate =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "ouCreationLdifTemplate" , ouCreationLdifTemplate_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting ouCreationLdifTemplate to {}", provisionerName, ouCreationLdifTemplate);
      
      ldapSearchResultPagingSize =
          GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "ldapSearchResultPagingSize", ldapSearchResultPagingSize_default_value);
      LOG.debug("Ldap Provisioner {} - Setting ldapSearchResultPagingSize to {}", provisionerName, ldapSearchResultPagingSize);
  
      ldapUserCacheTime_secs =
          GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "ldapUserCacheTime_secs", ldapUserCacheTime_secs_defaultValue);
      LOG.debug("Ldap Provisioner {} - Setting ldapUserCacheTime_secs to {}", provisionerName, ldapUserCacheTime_secs);
  
      ldapUserCacheSize =
          GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "ldapUserCacheSize", ldapUserCacheSize_defaultValue);
      LOG.debug("Ldap Provisioner {} - Setting ldapUserCacheSize to {}", provisionerName, ldapUserCacheSize);
    }

    public BlockingConnectionPool getLdapPool() {
      if ( ldapPool != null )
        return ldapPool;
      
      // We don't have a pool setup yet. Synchronize so we're sure we only make one pool.
      synchronized(this) {
        // Check if another thread has created our pool while we were waiting for the semaphore
        if ( ldapPool != null )
          return ldapPool;
        
       ldapPool = buildLdapConnectionPool();
      }

      return ldapPool;
    }


    private BlockingConnectionPool buildLdapConnectionPool() {
      BlockingConnectionPool result;

      String ldapPropertyPrefix = "ldap." + ldapPoolName.toLowerCase() + ".";
      
      for (String propName : GrouperLoaderConfig.retrieveConfig().propertyNames()) {
        if ( propName.toLowerCase().startsWith(ldapPropertyPrefix) ) {
          String propValue = GrouperLoaderConfig.getPropertyString(propName);
          
          // Get the part of the property after ldapPropertyPrefix 'ldap.person.'
          String propNameTail = propName.substring(ldapPropertyPrefix.length());
          ldaptiveProperties.put("org.ldaptive." + propNameTail, propValue);
        }
      }
      
      // Setup ldaptive ConnectionConfig
      ConnectionConfig connConfig = new ConnectionConfig();
      ConnectionConfigPropertySource ccpSource = new ConnectionConfigPropertySource(connConfig, ldaptiveProperties);
      ccpSource.initialize();

      //GrouperLoaderLdapServer grouperLoaderLdapProperties 
      //  = GrouperLoaderConfig.retrieveLdapProfile(ldapPoolName);
      
      /////////////
      // Binding
      BindConnectionInitializer binder = new BindConnectionInitializer();

      BindConnectionInitializerPropertySource bcip = new BindConnectionInitializerPropertySource(binder, ldaptiveProperties);
      bcip.initialize();

      // I'm not sure if SaslRealm and/or SaslAuthorizationId can be used independently
      // Therefore, we'll initialize gssApiConfig when either one of them is used.
      // And, then, we'll attach the gssApiConfig to the binder if there is a gssApiConfig
      GssApiConfig gssApiConfig = null;
      String val = (String) ldaptiveProperties.get("org.ldaptive.saslRealm");
      if (!StringUtils.isBlank(val)) {
        if ( gssApiConfig == null )
          gssApiConfig = new GssApiConfig();
        gssApiConfig.setRealm(val);
        
      }
      
      val = (String) ldaptiveProperties.get("org.ldaptive.saslAuthorizationId");
      if (!StringUtils.isBlank(val)) {
        if ( gssApiConfig == null )
          gssApiConfig = new GssApiConfig();
        gssApiConfig.setAuthorizationId(val);
      }

      // If there was a sasl/gssapi attribute, then save the gssApiConfig
      if ( gssApiConfig != null )
        binder.setBindSaslConfig(gssApiConfig);
      
      
      /////////////
      // PoolConfig
      
      PoolConfig ldapPoolConfig = new PoolConfig();
      PoolConfigPropertySource pcPropertySource = new PoolConfigPropertySource(ldapPoolConfig, ldaptiveProperties);

      DefaultConnectionFactory factory = new DefaultConnectionFactory(connConfig);
      factory.setProvider(new UnboundIDProvider());
      result = new BlockingConnectionPool(factory);
      
      result.initialize();
      return result;
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

    public Properties getLdaptiveProperties() {
      return ldaptiveProperties;
    }
    
    public int getLdapUserCacheTime_secs() {
      return ldapUserCacheTime_secs;
    }

    public int getLdapUserCacheSize() {
      return ldapUserCacheSize;
    }


    public int getLdapSearchResultPagingSize() {
      return ldapSearchResultPagingSize;
    }

    
    public boolean isActiveDirectory() {
      return isActiveDirectory;
    }


    public String getUserCreationBaseDn() {
      return userCreationBaseDn;
    }

    
    public String getUserCreationLdifTemplate() {
      return userCreationLdifTemplate;
    }

   
    public void populateElMap(Map<String, Object> variableMap) {
      super.populateElMap(variableMap);
      variableMap.put("userSearchBaseDn", getUserSearchBaseDn());
      variableMap.put("isActiveDirectory", isActiveDirectory());
    }

    
    public String getOuCreationLdifTemplate_defaultValue() {
      return ouCreationLdifTemplate_defaultValue;
    }


}