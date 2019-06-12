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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * Collects all the various properties and makes them available to the provisioner.
 *
 * @author Bert Bee-Lindgren
 */
public class ProvisionerConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisionerConfiguration.class);
    public static final String PARAMETER_NAMESPACE = "changeLog.consumer.";
    protected final String provisionerName;

    /**
     * Should this provisioner be active, or should it simply report 
     * success when changelog entries occur and skip full-sync requests?
     */
    private boolean enabled;
    protected boolean enabled_defaultValue = true;
    
    /**
     * Should groups in the groupSearchBaseDn/allGroupSearchFilter be removed 
     * if they no longer exist in Grouper?
     */
    private boolean grouperIsAuthoritative;
    protected boolean grouperIsAuthoritative_defaultValue = false;

    private int sleepTimeAfterError_ms;
    protected int sleepTimeAfterError_ms_defaultValue = 1000;
    
    protected int dataCacheTime_secs;
    protected int dataCacheTime_secs_defaultValue = 12*3600;
    
    private int grouperGroupCacheSize, grouperSubjectCacheSize;
    protected int grouperGroupCacheSize_defaultValue = 10000;
    protected int grouperSubjectCacheSize_defaultValue = 10000;
    

    protected String groupSelectionExpression;

    // Coordinating between Full and Incremental Syncing of the same group. This is
    // to avoid noisy logs where two provisioning processes are trying to do the same thing
    //   coordinationTimeout_secs: How long to wait before proceeding without coordination
    //   coordinationUpdate_secs: How frequently to log that we're waiting for locks
    protected int coordinationTimeout_secs;
    protected int coordinationTimeout_secs_defaultValue= 300;
    protected int coordinationUpdateInterval_secs;
    protected int coordinationUpdateInterval_secs_defaultValue = 10;

    // Should changes that refer to g:gsa subjects be ignored
    // Most importantly, this means that group-nestings will be ignored because the
    // membership change would point to a g:gsa Subject
    protected boolean areChangesToInternalGrouperSubjectsIgnored;
    protected boolean areChangesToInternalGrouperSubjectsIgnored_defaultValue = true;

    // When to log messier details about missing subjects
    protected int missingSubjectsWarningThreshold_percentage;
    protected int missingSubjectsWarningThreshold_percentage_defaultValue = 100; // Never log

    // When to log about cache sizes
    protected boolean areCacheSizeWarningsEnabled;
    protected boolean areCacheSizeWarningsEnabled_defaultValue = true;

    protected int cacheFullnessWarningThreshold_percentage;
    protected int cacheFullnessWarningThreshold_percentage_defaultValue = 95;

    // How many times to retry full sync when each full sync is finding the group out of date
    protected int maxNumberOfTimesToRepeatedlyFullSyncGroup;
    protected int maxNumberOfTimesToRepeatedlyFullSyncGroup_defaultValue = 3;

    // How long to sleep when full syncs need to be retried
    protected int timeToSleepBetweenRepeatedFullSyncs_ms;
    protected int timeToSleepBetweenRepeatedFullSyncs_ms_defaultValue = 1000;

    /**
     * This expression says that the provisionerName has to be in a group or stem provision_to attribute
     * and NOT in neither a group or stem do_not_provision_to attribute
     */
    protected String groupSelectionExpression_defaultValue() {
      // GRP-1356: pspng should use the default configuration folder
      // = "${utils.containedWithin(provisionerName, stemAttributes['etc:pspng:provision_to'], groupAttributes['etc:pspng:provision_to']) " 
      //    + "&& !utils.containedWithin(provisionerName, stemAttributes['etc:pspng:do_not_provision_to'], groupAttributes['etc:pspng:do_not_provision_to'])}";
      String rootStem = GrouperConfig.retrieveConfig().propertyValueString(
          "grouper.rootStemForBuiltinObjects", "etc");
      return "${utils.containedWithin(provisionerName, stemAttributes['" + rootStem 
          + ":pspng:provision_to'], groupAttributes['" + rootStem + ":pspng:provision_to']) " 
          + "&& !utils.containedWithin(provisionerName, stemAttributes['" + rootStem 
          + ":pspng:do_not_provision_to'], groupAttributes['" + rootStem + ":pspng:do_not_provision_to'])}";
      
    }
    protected Collection<String> attributesUsedInGroupSelectionExpression;
    protected String attributesUsedInGroupSelectionExpression_defaultValue = "provision_to,do_not_provision_to";

    // attributesUsedInGroupSelectionExpression are referenced within the groupSelectionExpression
    // However, what are they compared to? By default, they are compared to the name of the provisioner
    // and we can use database filtering to find matching groups faster. However, if a different
    // selection expression is used, then we can't use database filtering, and we have to run the jexl
    // expression on each group/folder that refers to a selection-relevant attribute.
    //
    // This configuration item enables database filtering when looking for possible matching groups.
    // The groups are still run through the groupSelectionExpression, but this allows fewer
    // groups to be checked against that expression.
    protected boolean attributesUsedInGroupSelectionExpressionAreComparedToProvisionerName;
    protected boolean attributesUsedInGroupSelectionExpressionAreComparedToProvisionerName_defaultValue = true;
    
    // Does the provisioning process need User information from the target system or
    // can all the changes be implemented with Subject attributes from Grouper?
    private boolean needsTargetSystemUsers;
    protected boolean needsTargetSystemUsers_defaultValue = false;
    protected int targetSystemUserCacheSize;
    protected int targetSystemUserCacheSize_defaultValue = 10000;

    private int userSearch_batchSize;
    protected int userSearch_batchSize_defaultValue = 50;
    
    private boolean createMissingUsers;
    protected boolean createMissingUsers_defaultValue = false;
    
    // Does the provisioning process need Group information from the target system or
    // can all the changes be implemented with Group attributes from Grouper?
    private boolean needsTargetSystemGroups;
    protected boolean needsTargetSystemGroups_defaultValue = false;
    private int targetSystemGroupCacheSize;
    protected int targetSystemGroupCacheSize_defaultValue = 10000;


    private boolean supportsEmptyGroups;
    protected boolean supportsEmptyGroups_defaultValue = true;

    private int groupSearch_batchSize;
    protected int groupSearch_batchSize_defaultValue = 50;

    private String grouperMessagingSystemName;
    protected String grouperMessagingSystemName_defaultValue = GrouperBuiltinMessagingSystem.BUILTIN_NAME;

    private int numberOfDataFetchingWorkers;
    protected int numberOfDataFetchingWorkers_defaultValue = 1;

    public ProvisionerConfiguration(String provisionerName) {
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

        enabled =
            GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "enabled", enabled_defaultValue);
        LOG.debug("Provisioner {} - Setting enabled to {}", provisionerName, enabled);

        grouperIsAuthoritative =
            GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "grouperIsAuthoritative", grouperIsAuthoritative_defaultValue);
        LOG.debug("Provisioner {} - Setting grouperIsAuthoritative to {}", provisionerName, grouperIsAuthoritative);

        // This attribute used to have a poorly-named configuration property. Keeping compatibility with it.
        if (GrouperLoaderConfig.retrieveConfig().containsKey(qualifiedParameterNamespace + "grouperGroupCacheTime_secs") ) {
            dataCacheTime_secs =
                    GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "grouperGroupCacheTime_secs", dataCacheTime_secs_defaultValue);
        } else {
            dataCacheTime_secs =
                    GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "dataCacheTime_secs", dataCacheTime_secs_defaultValue);
        }

        LOG.debug("Provisioner {} - Setting dataCacheTime_secs to {}", provisionerName, dataCacheTime_secs);
    
        sleepTimeAfterError_ms =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "sleepTimeAfterError_ms", sleepTimeAfterError_ms_defaultValue);
        LOG.debug("Provisioner {} - Setting sleepTimeAfterError_ms to {}", provisionerName, sleepTimeAfterError_ms);
    
        grouperGroupCacheSize =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "grouperGroupCacheSize", grouperGroupCacheSize_defaultValue);
        LOG.debug("Provisioner {} - Setting grouperGroupCacheSize to {}", provisionerName, grouperGroupCacheSize);

        grouperSubjectCacheSize =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "grouperSubjectCacheSize", grouperSubjectCacheSize_defaultValue);
        LOG.debug("Provisioner {} - Setting grouperSubjectCacheSize to {}", provisionerName, grouperSubjectCacheSize);

        targetSystemGroupCacheSize =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "targetSystemGroupCacheSize", targetSystemGroupCacheSize_defaultValue);
        LOG.debug("Provisioner {} - Setting targetSystemGroupCacheSize to {}", provisionerName, targetSystemGroupCacheSize);

        targetSystemUserCacheSize =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "targetSystemUserCacheSize", targetSystemUserCacheSize_defaultValue);
        LOG.debug("Provisioner {} - Setting targetSystemUserCacheSize to {}", provisionerName, targetSystemUserCacheSize);

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
            GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "groupSelectionExpression", groupSelectionExpression_defaultValue());
        LOG.debug("Provisioner {} - Setting groupSelectionExpression to {}", provisionerName, groupSelectionExpression);

        // List of attributes used in GroupSelection expression
        String attributesUsedInGroupSelectionExpression_string =
            GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "attributesUsedInGroupSelectionExpression", attributesUsedInGroupSelectionExpression_defaultValue);

        String attributesUsed[] = attributesUsedInGroupSelectionExpression_string.split(" *, *");
        
        attributesUsedInGroupSelectionExpression = new ArrayList<String>();
        String rootStem = GrouperConfig.retrieveConfig().propertyValueString(
            "grouper.rootStemForBuiltinObjects", "etc");
        for ( String attribute: attributesUsed ) {
          if ( attribute.contains(":") ) {
            attributesUsedInGroupSelectionExpression.add(attribute);
          }
          else {
            attributesUsedInGroupSelectionExpression.add(String.format("%s:pspng:%s", rootStem, attribute));
          }
        }
        LOG.debug("Provisioner {} - Setting attributesUsedInGroupSelectionExpression to {}", provisionerName, attributesUsedInGroupSelectionExpression);
        
        attributesUsedInGroupSelectionExpressionAreComparedToProvisionerName =
            GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "attributesUsedInGroupSelectionExpressionAreComparedToProvisionerName", attributesUsedInGroupSelectionExpressionAreComparedToProvisionerName_defaultValue);
        LOG.debug("Provisioner {} - Setting attributesUsedInGroupSelectionExpressionAreComparedToProvisionerName to {}", provisionerName, attributesUsedInGroupSelectionExpressionAreComparedToProvisionerName);

        userSearch_batchSize =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "userSearch_batchSize", userSearch_batchSize_defaultValue);
        LOG.debug("Provisioner {} - Setting userSearch_batchSize to {}", provisionerName, userSearch_batchSize);

        groupSearch_batchSize =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "groupSearch_batchSize", groupSearch_batchSize_defaultValue);
        LOG.debug("Provisioner {} - Setting groupSearch_batchSize to {}", provisionerName, groupSearch_batchSize);

        coordinationTimeout_secs =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "coordinationTimeout_secs", coordinationTimeout_secs_defaultValue);
        LOG.debug("Provisioner {} - Setting coordinationTimeout_secs to {}", provisionerName, coordinationTimeout_secs);
        coordinationUpdateInterval_secs =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "coordinationUpdateInterval_secs", coordinationUpdateInterval_secs_defaultValue);
        LOG.debug("Provisioner {} - Setting coordinationUpdateInterval_secs to {}", provisionerName, coordinationUpdateInterval_secs);

        areChangesToInternalGrouperSubjectsIgnored =
                GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "areChangesToInternalGrouperSubjectsIgnored", areChangesToInternalGrouperSubjectsIgnored_defaultValue);
        LOG.debug("Provisioner {} - Setting areChangesToInternalGrouperSubjectsIgnored to {}", provisionerName, areChangesToInternalGrouperSubjectsIgnored);

        grouperMessagingSystemName =
                GrouperLoaderConfig.retrieveConfig().propertyValueString(qualifiedParameterNamespace + "grouperMessagingSystemName", grouperMessagingSystemName_defaultValue);
        LOG.debug("Provisioner {} - Setting grouperMessagingSystemName to {}", provisionerName, grouperMessagingSystemName);

        numberOfDataFetchingWorkers =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "numberOfDataFetchingWorkers", numberOfDataFetchingWorkers_defaultValue);
        LOG.debug("Provisioner {} - Setting numberOfDataFetchingWorkers to {}", provisionerName, numberOfDataFetchingWorkers);

        missingSubjectsWarningThreshold_percentage =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "missingSubjectsWarningThreshold_percentage", missingSubjectsWarningThreshold_percentage_defaultValue);
        LOG.debug("Provisioner {} - Setting missingSubjectsWarningThreshold_percentage to {}", provisionerName, missingSubjectsWarningThreshold_percentage);


        areCacheSizeWarningsEnabled =
                GrouperLoaderConfig.retrieveConfig().propertyValueBoolean(qualifiedParameterNamespace + "areCacheSizeWarningsEnabled", areCacheSizeWarningsEnabled_defaultValue);
        LOG.debug("Provisioner {} - Setting areCacheSizeWarningsEnabled to {}", provisionerName, areCacheSizeWarningsEnabled);


        cacheFullnessWarningThreshold_percentage =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "cacheFullnessWarningThreshold_percentage", cacheFullnessWarningThreshold_percentage_defaultValue);
        LOG.debug("Provisioner {} - Setting cacheFullnessWarningThreshold_percentage to {}", provisionerName, cacheFullnessWarningThreshold_percentage);

        maxNumberOfTimesToRepeatedlyFullSyncGroup =
            GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "maxNumberOfTimesToRepeatedlyFullSyncGroup", maxNumberOfTimesToRepeatedlyFullSyncGroup_defaultValue);
        LOG.debug("Provisioner {} - Setting maxNumberOfTimesToRepeatedlyFullSyncGroup to {}", provisionerName, maxNumberOfTimesToRepeatedlyFullSyncGroup);
        if ( maxNumberOfTimesToRepeatedlyFullSyncGroup<1 ) {
            LOG.warn("Provisioner {} - maxNumberOfTimesToRepeatedlyFullSyncGroup must be at least 1", provisionerName);
            maxNumberOfTimesToRepeatedlyFullSyncGroup = 1;
        }

        timeToSleepBetweenRepeatedFullSyncs_ms =
                GrouperLoaderConfig.retrieveConfig().propertyValueInt(qualifiedParameterNamespace + "timeToSleepBetweenRepeatedFullSyncs_ms", timeToSleepBetweenRepeatedFullSyncs_ms_defaultValue);
        LOG.debug("Provisioner {} - Setting timeToSleepBetweenRepeatedFullSyncs_ms to {}", provisionerName, timeToSleepBetweenRepeatedFullSyncs_ms);
    }


    public boolean isEnabled() { return enabled; }
    
    public int getDataCacheTime_secs() { return dataCacheTime_secs; }

    public int getGrouperGroupCacheSize() { return grouperGroupCacheSize; }

    public int getGrouperSubjectCacheSize() { return grouperSubjectCacheSize; }

    public int getTargetSystemUserCacheSize() {
        if (needsTargetSystemUsers) {
            return targetSystemUserCacheSize;
        }
        else {
            return 0;
        }
    }

    public int getTargetSystemGroupCacheSize() {
        if (needsTargetSystemGroups) {
            return targetSystemGroupCacheSize;
        } else {
            return 0;
        }
    }

    /**
     * The groupSelectionExpression is an arbitrary jexl expression. As such, it's hard
     * to know exactly what it is doing with Group and Folder attributes. This method
     * enables (fast) database filtering to occur when those Group and Folder attributes
     * are being compared to the name of the provisioner.
     * 
     * @return True if the Group-Selection expression compares the attributes to the provisioner name
     */
    public boolean areAttributesUsedInGroupSelectionExpressionComparedToProvisionerName() {
      return attributesUsedInGroupSelectionExpressionAreComparedToProvisionerName;
    }
    
    public boolean isCreatingMissingUsersEnabled() { return createMissingUsers; }
    
    public boolean areEmptyGroupsSupported() { return supportsEmptyGroups; }
    
    public int getUserSearch_batchSize() { return userSearch_batchSize; }
    
    public int getGroupSearch_batchSize() { return groupSearch_batchSize; }
    
    public boolean needsTargetSystemUsers() { return needsTargetSystemUsers; }

    public boolean needsTargetSystemGroups() { return needsTargetSystemGroups; }
    
    public String getGroupSelectionExpression() { return groupSelectionExpression; }
    
    public Collection<String> getAttributesUsedInGroupSelectionExpression() { return attributesUsedInGroupSelectionExpression; }

    public void populateElMap(Map<String, Object> variableMap) {
      variableMap.put("provisionerName", provisionerName);
    }

    public long getSleepTimeAfterError_ms() { return sleepTimeAfterError_ms; }
    
    public boolean isGrouperAuthoritative() { return grouperIsAuthoritative; }

    public int getCoordinationTimout_secs() {return coordinationTimeout_secs;}

    public int getCoordinationUpdateInterval_secs() {return coordinationUpdateInterval_secs;}

    public boolean areChangesToInternalGrouperSubjectsIgnored() {return areChangesToInternalGrouperSubjectsIgnored;}

    public String getGrouperMessagingSystemName() { return grouperMessagingSystemName; }

    public int getNumberOfDataFetchingWorkers() { return numberOfDataFetchingWorkers; }

    public int getMissingSubjectsWarningThreshold_percentage() { return missingSubjectsWarningThreshold_percentage; }

    public double getCacheFullnessWarningThreshold_percentage() { return cacheFullnessWarningThreshold_percentage; }

    public boolean areCacheSizeWarningsEnabled() { return areCacheSizeWarningsEnabled; }

    public int getMaxNumberOfTimesToRepeatedlyFullSyncGroup() { return maxNumberOfTimesToRepeatedlyFullSyncGroup; }

    public int getTimeToSleepBetweenRepeatedFullSyncs_ms() { return timeToSleepBetweenRepeatedFullSyncs_ms;}
}