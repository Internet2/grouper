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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;

/**
 * Collects all the various properties and makes them available to the provisioner.
 *
 * @author Bert Bee-Lindgren
 */
public class ProvisionerProperties {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionerProperties.class);
    public static final String PARAMETER_NAMESPACE = "changeLog.consumer.";
    protected final String provisionerName;
    
    private int sleepTimeAfterError_ms;
    protected int sleepTimeAfterError_ms_defaultValue = 1000;
    
    private int grouperDataCacheTime_secs;
    protected int grouperGroupCacheTime_secs_defaultValue = 600;
    
    private int grouperGroupCacheSize, grouperSubjectCacheSize;
    protected int grouperGroupCacheSize_defaultValue = 10000;
    protected int grouperSubjectCacheSize_defaultValue = 10000;

    protected String groupSelectionExpression;

    // This expression says that the provisionerName has to be in a group or stem provision_to attribute
    // and NOT in neither a group or stem do_not_provision_to attribute

    protected String groupSelectionExpression_defaultValue
      = "${utils.containedWithin(provisionerName, stemAttributes['etc:attribute:userData:provision_to'], groupAttributes['etc:attribute:userData:provision_to']) " 
          + "&& !utils.containedWithin(provisionerName, stemAttributes['etc:attribute:userData:do_not_provision_to'], groupAttributes['etc:attribute:userData:do_not_provision_to'])}";

    
    // Does the provisioning process need User information from the target system or
    // can all the changes be implemented with Subject attributes from Grouper?
    private boolean needsTargetSystemUsers;
    protected boolean needsTargetSystemUsers_defaultValue = false;

    private int userSearch_batchSize;
    protected int userSearch_batchSize_defaultValue = 50;
    
    private boolean createMissingUsers;
    protected boolean createMissingUsers_defaultValue = false;
    
    // Does the provisioning process need Group information from the target system or
    // can all the changes be implemented with Group attributes from Grouper?
    private boolean needsTargetSystemGroups;
    protected boolean needsTargetSystemGroups_defaultValue = false;
    
    private boolean supportsEmptyGroups;
    protected boolean supportsEmptyGroups_defaultValue = true;

    private int groupSearch_batchSize;
    protected int groupSearch_batchSize_defaultValue = 50;


    public ProvisionerProperties(String provisionerName) {
      this.provisionerName = provisionerName;
    }
    
    /**
     * Populate the various settings from the configuration source(s). This is separated from our 
     * constructor so that our subclasses can override default values. In other words, a subclass
     * might construct us, change a _defaultValue ivar and then ask us to do 'readConfiguration'.
     */
    public void readConfiguration() {
        final String qualifiedParameterNamespace = PARAMETER_NAMESPACE + provisionerName + ".";

        LOG.debug("Ldap Group Provisioner - Setting properties for {} consumer/provisioner.", provisionerName);

        grouperDataCacheTime_secs =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "grouperGroupCacheTime_secs", grouperGroupCacheTime_secs_defaultValue);
        LOG.debug("Provisioner {} - Setting grouperGroupCacheTime_secs to {}", provisionerName, grouperDataCacheTime_secs);
    
        sleepTimeAfterError_ms =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "sleepTimeAfterError_ms", sleepTimeAfterError_ms_defaultValue);
        LOG.debug("Provisioner {} - Setting sleepTimeAfterError_ms to {}", provisionerName, sleepTimeAfterError_ms);
    
        grouperGroupCacheSize =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "grouperGroupCacheSize", grouperGroupCacheSize_defaultValue);
        LOG.debug("Provisioner {} - Setting grouperGroupCacheSize to {}", provisionerName, grouperGroupCacheSize);

        grouperSubjectCacheSize =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "grouperSubjectCacheSize", grouperSubjectCacheSize_defaultValue);
        LOG.debug("Provisioner {} - Setting grouperSubjectCacheSize to {}", provisionerName, grouperSubjectCacheSize);
        
        createMissingUsers =
            GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "createMissingUsers", createMissingUsers_defaultValue);
        LOG.debug("Provisioner {} - Setting createMissingUsers to {}", provisionerName, createMissingUsers);
        
        needsTargetSystemUsers =
            GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "needsTargetSystemUsers", needsTargetSystemUsers_defaultValue);
        LOG.debug("Provisioner {} - Setting needsTargetSystemUsers to {}", provisionerName, needsTargetSystemUsers);

        needsTargetSystemGroups =
            GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "needsTargetSystemGroups", needsTargetSystemGroups_defaultValue);
        LOG.debug("Provisioner {} - Setting needsTargetSystemGroups to {}", provisionerName, needsTargetSystemGroups);

        supportsEmptyGroups =
            GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "supportsEmptyGroups", supportsEmptyGroups_defaultValue);
        LOG.debug("Provisioner {} - Setting supportsEmptyGroups to {}", provisionerName, supportsEmptyGroups);

        groupSelectionExpression =
            GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "groupSelectionExpression", groupSelectionExpression_defaultValue);
        LOG.debug("Ldap Provisioner {} - Setting groupSelectionExpression to {}", provisionerName, groupSelectionExpression);

        userSearch_batchSize =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "userSearch_batchSize", userSearch_batchSize_defaultValue);
        LOG.debug("Ldap Provisioner {} - Setting userSearch_batchSize to {}", provisionerName, userSearch_batchSize);

        groupSearch_batchSize =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "groupSearch_batchSize", groupSearch_batchSize_defaultValue);
        LOG.debug("Ldap Provisioner {} - Setting groupSearch_batchSize to {}", provisionerName, groupSearch_batchSize);
    }


    
    public int getGrouperDataCacheTime_secs() {
      return grouperDataCacheTime_secs;
    }


    
    public int getGrouperGroupCacheSize() {
      return grouperGroupCacheSize;
    }

    public int getGrouperSubjectCacheSize() {
      return grouperSubjectCacheSize;
    }
    
    public boolean isCreatingMissingUsersEnabled() {
      return createMissingUsers;
    }
    
    public boolean areEmptyGroupsSupported() {
      return supportsEmptyGroups;
    }
    
    public int getUserSearch_batchSize() {
      return userSearch_batchSize;
    }
    
    public int getGroupSearch_batchSize() {
      return groupSearch_batchSize;
    }
    
    public boolean needsTargetSystemUsers() {
      return needsTargetSystemUsers;
    }

    public boolean needsTargetSystemGroups() {
      return needsTargetSystemGroups;
    }
    
    public String getGroupSelectionExpression() {
      return groupSelectionExpression;
    }

    public void populateElMap(Map<String, Object> variableMap) {
      variableMap.put("provisionerName", provisionerName);
    }

    public long getSleepTimeAfterError_ms() {
      return sleepTimeAfterError_ms;
    }
}