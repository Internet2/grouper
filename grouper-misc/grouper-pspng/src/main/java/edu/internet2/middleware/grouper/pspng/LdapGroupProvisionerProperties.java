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
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;

/**
 * Collects all the various properties and makes them available to the provisioner.
 *
 * @author Bert Bee-Lindgren
 */
public class LdapGroupProvisionerProperties extends LdapProvisionerProperties {

    private static final Logger LOG = LoggerFactory.getLogger(LdapGroupProvisionerProperties.class);
    private static final String PARAMETER_NAMESPACE = "changeLog.consumer.";

    /** 
     * What attribute of a group points to its members? 
     * (This will be 'member' when isActiveDirectory, and is required when !isActiveDirectory)
     */
    private String memberAttributeName;
    
    /**
     * What value is stored in a group's member attribute?
     *   Usually the DN of the member (for Active Directory or groupOfUniqueName)
     *   or the username of the member (for posixGroup)
     *   
     *   Here are the possible values:
     *     dn
     *     user.attribute which means ${userLdap.attributeValue}
     *     a JEXL expression that can refer to 
     *      user: the TargetSystemUser that was returned by userSearchFilter below
     *      subject: a string with the member's grouper-subjectId
     */
    private String memberAttributeValueFormat;
    protected  String memberAttributeValueFormat_defaultValue = "${ldapUser.dn}";
    
    /**
     * What attribute of an account points to its groups?
     * (This might be null if the directory server does not offer an 
     * overlay or virtual memberof attribute)
     */
    private String groupAttributeName;
    protected String groupAttributeName_defaultValue = null;
    
    /** 
     * LDIF used to create a new group. The DN of this LDIF should be relative
     * to the groupCreationBaseDn. 
     */
    private String groupCreationLdifTemplate;
    protected String groupCreationLdifTemplate_defaultValue = null;
    
    /**
     * Where in the directory server should groups be created
     */
    private String groupCreationBaseDn;
   
    /**
     * Should groups in the groupSearchBaseDn/allGroupSearchFilter be removed 
     * if they no longer exist in Grouper?
     */
    private boolean grouperIsAuthoritative;
    protected boolean grouperIsAuthoritative_defaultValue = false;
    
    /**
     * How to find the group objects. This is required.
     */
    private String groupSearchBaseDn;
    
    private String allGroupSearchFilter;
    protected String allGroupSearchFilter_defaultValue = null;
    
    private String singleGroupSearchFilter;
    protected String singleGroupSearchFilter_defaultValue = null;
    
    /** An array of attributes needed by the singleGroupSearchFilter. 
     * These are used to make sure the cached group LdapObjects have the necessary data
     * to run the singleGroupSearchFilter in memory after a bunch of 
     * groups are found via bulk searching.
     * 
     * TODO: Parse the filter to see what attributes it uses.
     * 
     * By default this will be cn, gidNumber, samAccountName
     */
    private String groupSearchAttributes[];
    protected String groupSearchAttributes_defaultValue = "cn,gidNumber,samAccountName,objectclass";

    private int ldapGroupCacheTime_secs;
    protected int ldapGroupCacheTime_secs_defaultValue = 600;
    
    private int ldapGroupCacheSize;
    protected int ldapGroupCacheSize_defaultValue = 10000;
    
    public LdapGroupProvisionerProperties(String provisionerName) {
      super(provisionerName);
      
      needsTargetSystemGroups_defaultValue=true;
      needsTargetSystemUsers_defaultValue=true;
    }
    
    @Override
    public void readConfiguration() {
      super.readConfiguration();
      final String qualifiedParameterNamespace = PARAMETER_NAMESPACE + provisionerName + ".";

      LOG.debug("Ldap Group Provisioner - Setting properties for {} consumer/provisioner.", provisionerName);

      if ( isActiveDirectory() )
        memberAttributeName =
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "memberAttributeName", "member");
      else
        memberAttributeName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "memberAttributeName");
      LOG.debug("Ldap Group Provisioner {} - Setting memberAttributeName to {}", provisionerName, memberAttributeName);

      memberAttributeValueFormat =
              GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "memberAttributeValueFormat" , memberAttributeValueFormat_defaultValue);
      LOG.debug("Ldap Group Provisioner {} - Setting memberAttributeValueFormat to {}", provisionerName, memberAttributeValueFormat);

      groupAttributeName =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "groupAttributeName" , 
              isActiveDirectory() ? "memberof" : groupAttributeName_defaultValue);
      LOG.debug("Ldap Group Provisioner {} - Setting groupAttributeName to {}", provisionerName, groupAttributeName);
      
      if ( StringUtils.isNotBlank(groupAttributeName) )
        addUserSearchAttribute(groupAttributeName);
      
      
      groupCreationLdifTemplate =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "groupCreationLdifTemplate" , groupCreationLdifTemplate_defaultValue);
      LOG.debug("Ldap Group Provisioner {} - Setting groupCreationLdifTemplate to {}", provisionerName, groupCreationLdifTemplate);

      groupSearchBaseDn =
          GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "groupSearchBaseDn");
      LOG.debug("Ldap Group Provisioner {} - Setting groupSearchBaseDn to {}", provisionerName, groupSearchBaseDn);

      groupCreationBaseDn =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "groupCreationBaseDn" , groupSearchBaseDn);
      LOG.debug("Ldap Group Provisioner {} - Setting groupCreationBaseDn to {}", provisionerName, groupCreationBaseDn);

      allGroupSearchFilter =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "allGroupSearchFilter" , 
              isActiveDirectory() ? "objectclass=group" : allGroupSearchFilter_defaultValue);
      LOG.debug("Ldap Group Provisioner {} - Setting allGroupSearchFilter to {}", provisionerName, allGroupSearchFilter);

      singleGroupSearchFilter =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "singleGroupSearchFilter" , singleGroupSearchFilter_defaultValue);
      LOG.debug("Ldap Group Provisioner {} - Setting singleGroupSearchFilter to {}", provisionerName, singleGroupSearchFilter);

      String groupSearchAttributeString =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "groupSearchAttributes" , groupSearchAttributes_defaultValue);
      groupSearchAttributes = groupSearchAttributeString.trim().split(" *, *");
      LOG.debug("Ldap Group Provisioner {} - Setting groupSearchAttributes to {}", provisionerName, groupSearchAttributes);
      
      ldapGroupCacheTime_secs =
          GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "ldapGroupCacheTime_secs", ldapGroupCacheTime_secs_defaultValue);
      LOG.debug("Ldap Group Provisioner {} - Setting ldapGroupCacheTime_secs to {}", provisionerName, ldapGroupCacheTime_secs);
  

      ldapGroupCacheSize =
              GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "ldapGroupCacheSize", ldapGroupCacheSize_defaultValue);
      LOG.debug("Ldap Group Provisioner {} - Setting ldapGroupCacheSize to {}", provisionerName, ldapGroupCacheSize);
    }

    
    public String getMemberAttributeName() {
      return memberAttributeName;
    }

    
    public String getMemberAttributeValueFormat() {
      return memberAttributeValueFormat;
    }

    
    public String getGroupAttributeName() {
      return groupAttributeName;
    }

    
    public String getGroupCreationLdifTemplate() {
      return groupCreationLdifTemplate;
    }

    
    public String getGroupCreationBaseDn() {
      return groupCreationBaseDn;
    }

    
    public String getGroupSearchBaseDn() {
      return groupSearchBaseDn;
    }

    
    public String getAllGroupSearchFilter() {
      return allGroupSearchFilter;
    }

    
    public String getSingleGroupSearchFilter() {
      return singleGroupSearchFilter;
    }

    public String[] getGroupSearchAttributes() {
      return groupSearchAttributes;
    }
    
    public void addGroupSearchAttribute(String attribute) {
      for ( String a : groupSearchAttributes )
        if ( a.equalsIgnoreCase(attribute))
          return;
      
      groupSearchAttributes = Arrays.copyOf(groupSearchAttributes, groupSearchAttributes.length+1);
      groupSearchAttributes[groupSearchAttributes.length-1] = attribute;
    }
    
    public int getLdapGroupCacheTime_secs() {
      return ldapGroupCacheTime_secs;
    }

    
    public int getLdapGroupCacheSize() {
      return ldapGroupCacheSize;
    }
    
    public void populateElMap(Map<String, Object> variableMap) {
      super.populateElMap(variableMap);
      variableMap.put("groupSearchBaseDn", getGroupSearchBaseDn());
      variableMap.put("groupCreationBaseDn", getGroupCreationBaseDn());
    }


}