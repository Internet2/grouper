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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;

/**
 * Collects all the various properties and makes them available to the provisioner.
 *
 * @author Bert Bee-Lindgren
 */
public class LdapAttributeProvisionerConfiguration extends LdapProvisionerConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(LdapAttributeProvisionerConfiguration.class);
    private static final String PARAMETER_NAMESPACE = "changeLog.consumer.";

    /** 
     * What attribute of an account maintains values related to the
     * grouper groups the account is a member of. 
     */
    private String provisionedAttributeName;
    
    /**
     * What value is stored in a member's attribute?
     *   
     *   Here are the possible values:
     *   TODO
     */
    private String provisionedAttributeValueFormat;
    protected String provisionedAttributeValueFormat_defaultValue = "${group.name}";
    
    
    private String allProvisionedValuesPrefix;
    protected String allProvisionedValuesPrefix_defaultValue = null;
    
    /**
     * Wildcard attribute value can identify all users that attribute values
     * provisioned by this provisioner. This is used to find attribute values
     * no longer justified by the configuration, either by a deleted group or
     * by a previous configuration. 
     *  
     * This is only used if grouperIsAuthoritative=true.
     */
    private String wildcardValueForFindingAllProvisionedValues;
    
    public LdapAttributeProvisionerConfiguration(String provisionerName) {
      super(provisionerName);
      
      needsTargetSystemGroups_defaultValue = false;
      needsTargetSystemUsers_defaultValue = true;
    }
    
    @Override
    public void readConfiguration() {
      super.readConfiguration();
      final String qualifiedParameterNamespace = PARAMETER_NAMESPACE + provisionerName + ".";

      LOG.debug("Ldap Attribute Provisioner - Setting properties for {} consumer/provisioner.", provisionerName);

      provisionedAttributeName =
                GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(qualifiedParameterNamespace + "provisionedAttributeName");
      LOG.debug("Ldap Attribute Provisioner {} - Setting provisionedAttributeName to {}", provisionerName, provisionedAttributeName);

      provisionedAttributeValueFormat =
              GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "provisionedAttributeValueFormat" , provisionedAttributeValueFormat_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting provisionedAttributeValueFormat to {}", provisionerName, provisionedAttributeValueFormat);

      allProvisionedValuesPrefix =
          GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "allProvisionedValuesPrefix" , allProvisionedValuesPrefix_defaultValue);
      LOG.debug("Ldap Attribute Provisioner {} - Setting allProvisionedValuesPrefix to {}", provisionerName, allProvisionedValuesPrefix);
    }

    
    public String getProvisionedAttributeName() {
      return provisionedAttributeName;
    }

    
    public String getProvisionedAttributeValueFormat() {
      return provisionedAttributeValueFormat;
    }
    
    public String getAllProvisionedValuesPrefix() {
      return allProvisionedValuesPrefix;
    }
}