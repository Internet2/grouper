package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncIntegration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.ui.util.ProgressBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;

public class GrouperProvisioningDiagnosticsContainer {

  private long started = -1;
  
  /**
   * if in diagnostics
   */
  private boolean inDiagnostics;
  
  /**
   * 
   * @return true if in diagnostics execution
   */
  public boolean isInDiagnostics() {
    return this.inDiagnostics;
  }
  
  /**
   * uniquely identifies this diagnostics request as opposed to other diagnostics in other tabs
   */
  private String uniqueDiagnosticsId;
  
  /**
   * have a progress bean
   */
  private ProgressBean progressBean = new ProgressBean();
  
  private GrouperProvisioner grouperProvisioner;
  
  /**
   * have a progress bean
   * @return the progressBean
   */
  public ProgressBean getProgressBean() {
    return this.progressBean;
  }

  
  public String getUniqueDiagnosticsId() {
    return uniqueDiagnosticsId;
  }

  
  public void setUniqueDiagnosticsId(String uniqueDiagnosticsId) {
    this.uniqueDiagnosticsId = uniqueDiagnosticsId;
  }
  
  public String getReportFinal() {
    return this.report.toString();
  }


  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }


  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * report results
   */
  private StringBuilder report = new StringBuilder();

  /**
   * get report to append.  Assume the output is preformatted
   * @return report
   */
  public StringBuilder getReportInProgress() {
    return this.report;
  }
  
  /**
   * append configuration to diagnostics
   */
  public void appendConfiguration() {
    this.report.append("<h4>Configuration</h4>");
    
    Map<String, String> configuration = new TreeMap<String, String>();
    
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    
    String configPrefix = "provisioner." + this.getGrouperProvisioner().getConfigId() + ".";
    
    ProvisionerConfiguration provisionerConfiguration = this.getGrouperProvisioner().getProvisionerConfiguration();
    Map<String, GrouperConfigurationModuleAttribute> suffixToConfigAttribute = provisionerConfiguration.retrieveAttributes();
    for (String propertyName : grouperLoaderConfig.propertyNames()) {
      if (propertyName.startsWith(configPrefix)) {
        String suffix = GrouperUtil.prefixOrSuffix(propertyName, configPrefix, false);
        String lowerKey = suffix.toLowerCase();
        boolean secret = lowerKey.contains("pass") || lowerKey.contains("secret") || lowerKey.contains("private");
        
        GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = suffixToConfigAttribute.get(suffix);
        if (grouperConfigurationModuleAttribute != null) {
          secret = secret || GrouperConfigHibernate.isPassword(
              ConfigFileName.GROUPER_LOADER_PROPERTIES, grouperConfigurationModuleAttribute.getConfigItemMetadata(), 
                propertyName, grouperLoaderConfig.propertyValueString(propertyName), true, null);
        }
        
        configuration.put(propertyName, secret ? "****** (redacted)" : grouperLoaderConfig.propertyValueString(propertyName));
      }
    }

    this.report.append("<pre>");
    for (String propertyName : configuration.keySet()) {
      this.report.append(GrouperUtil.xmlEscape(propertyName + " = " + configuration.get(propertyName))).append("\n");
    }
    this.report.append("</pre>");
    
  }  
  /**
   * run diagnostics
   */
  public void runDiagnostics() {
    this.inDiagnostics = true;
    this.started = System.currentTimeMillis();
    
    Exception exception = null;
    
    try {
      this.report = new StringBuilder();
      
      this.appendConfiguration();

      this.appendExternalSystem();

      this.appendGeneralInfo();
      
      this.appendValidation();
      
      this.appendSelectAllGroups();
      this.appendSelectAllEntities();
      this.appendSelectAllMemberships();

      this.appendSelectGroupFromGrouper();
      this.appendSelectGroupFromTarget();

      this.appendInsertGroupIntoTarget();
      
    } catch (Exception e) {
      LOG.error("error in diagnostics", e);
      this.report.append("</pre><pre>").append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(e))).append("</pre>");
    } finally {
      this.inDiagnostics = false;

    
    }
    
    {
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
      gcGrouperSyncJob.setErrorMessage(exception == null ? null : GrouperUtil.getFullStackTrace(exception));
      gcGrouperSyncJob.setErrorTimestamp(exception == null ? null : nowTimestamp);
      gcGrouperSyncJob.setLastSyncTimestamp(nowTimestamp);
      if (this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().wasWorkDone()) {
        gcGrouperSyncJob.setLastTimeWorkWasDone(nowTimestamp);
      }
      gcGrouperSyncJob.setPercentComplete(100);

      // do this in the right spot, after assigning correct sync info about sync
      int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
  
      this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
    }

  }

  /**
   * override this to log the external system
   */
  protected void appendExternalSystem() {
    
  }

  private ProvisioningGroupWrapper provisioningGroupWrapper = null;
  
  /**
   * select a group from grouper
   */
  private void appendSelectGroupFromGrouper() {

    this.report.append("<h4>Select group from Grouper</h4><pre>");
    
    String groupName = this.getGrouperProvisioningDiagnosticsSettings().getDiagnosticsGroupName();
    if (StringUtils.isBlank(groupName)) {
      this.report.append("<font color='orange'><b>Warning:</b></font> Group name for diagnostics is not set\n");
    } else {
    
      Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false);
      if (group == null) {

        this.report.append("<font color='orange'><b>Warning:</b></font> Group '").append(GrouperUtil.xmlEscape(groupName)).append("' does not exist in Grouper\n");
        
      } else {

        this.report.append("<font color='gray'><b>Note:</b></font> Group: ").append(GrouperUtil.xmlEscape(group.toStringDb())).append(this.getCurrentDuration()).append("\n");

        GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
        GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(group.getId());
        if (gcGrouperSyncGroup == null) {
          this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncGroup record does not exist in database\n");
          
        } else {
          this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncGroup: ").append(GrouperUtil.xmlEscape(gcGrouperSyncGroup.toString())).append(this.getCurrentDuration()).append("\n");
        }
        
        List<ProvisioningGroup> grouperProvisioningGroups = this.grouperProvisioner.retrieveGrouperDao().retrieveGroups(false, GrouperUtil.toList(group.getId()));
        if (GrouperUtil.length(grouperProvisioningGroups) == 0) {
          this.report.append("<font color='orange'><b>Warning:</b></font> Cannot find ProvisioningGroup object, perhaps the group is not marked as provisionable\n");
        } else {
          GrouperUtil.assertion(grouperProvisioningGroups.size() == 1, "Why is size not 1???? " + grouperProvisioningGroups.size());
          
          ProvisioningGroup grouperProvisioningGroup = grouperProvisioningGroups.get(0);
          this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningGroup (unprocessed): ").append(GrouperUtil.xmlEscape(grouperProvisioningGroup.toString())).append(this.getCurrentDuration()).append("\n");
         
          this.provisioningGroupWrapper = new ProvisioningGroupWrapper();
          grouperProvisioningGroup.setProvisioningGroupWrapper(this.provisioningGroupWrapper);
          this.provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
          this.provisioningGroupWrapper.setGrouperProvisioningGroup(grouperProvisioningGroup);
          this.provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
          
          List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
          
          if (GrouperUtil.length(grouperTargetGroups) == 0) {
            this.report.append("<font color='gray'><b>Note:</b></font> Cannot find grouperTargetGroup object after translation, perhaps the group is not supposed to translate\n");
          } else {
            GrouperUtil.assertion(grouperTargetGroups.size() == 1, "Why is size not 1???? " + grouperTargetGroups.size());
            ProvisioningGroup grouperTargetGroup = grouperTargetGroups.get(0);
            this.provisioningGroupWrapper.setGrouperTargetGroup(grouperTargetGroup);
            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningGroup (translated): ").append(GrouperUtil.xmlEscape(grouperTargetGroup.toString())).append("\n");
          
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(grouperTargetGroups, null);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroups, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroups);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroups);

            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningGroup (filtered, attributes manipulated, matchingId calculated): ").append(GrouperUtil.xmlEscape(grouperTargetGroup.toString())).append("\n");
            
            if (GrouperUtil.isBlank(grouperTargetGroup.getMatchingId())) {
              
              GrouperProvisioningConfigurationAttribute matchingAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveGroupAttributeMatching();
              if (matchingAttribute == null) {
                this.report.append("<font color='red'><b>Error:</b></font> Cannot find the group matching attribute/field\n");
              } else {
                if (!matchingAttribute.isInsert() && !matchingAttribute.isUpdate()) {
                  if (gcGrouperSyncGroup != null && gcGrouperSyncGroup.isInTarget()) {
                    this.report.append("<font color='red'><b>Error:</b></font> Grouper target group matching id is blank and it is currently in target\n");
                  } else {
                    this.report.append("<font color='green'><b>Success:</b></font> Grouper target group matching id is blank but it is not inserted or updated so it probably is not retrieved from target yet\n");
                  }
                } else {
                  this.report.append("<font color='red'><b>Error:</b></font> Grouper target group matching id is blank\n");
                }
              }
              
            }
            
            // validate
            this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(grouperTargetGroups, false);
            
            if (this.provisioningGroupWrapper.getErrorCode() != null) {
              this.report.append("<font color='red'><b>Error:</b></font> Group is not valid! " + this.provisioningGroupWrapper.getErrorCode() + "\n");
            } else {
              this.report.append("<font color='green'><b>Success:</b></font> Group is valid\n");
            }
          }          
        }
      }
      
    }
    this.report.append("</pre>\n");

  }

  /**
   * insert group into target
   */
  private void appendInsertGroupIntoTarget() {
    this.report.append("<h4>Insert group into Target</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsGroupInsert()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to insert group into target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper == null || this.provisioningGroupWrapper.getGrouperProvisioningGroup() == null 
        || this.provisioningGroupWrapper.getGrouperTargetGroup() == null ) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert group into target since does not exist in Grouper\n");
      this.report.append("</pre>\n");
      return;
    }
    if (this.provisioningGroupWrapper != null && this.provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert group into target since it is already there\n");
      this.report.append("</pre>\n");
      return;
    }
    if (this.provisioningGroupWrapper != null && this.provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert group into target since it is already there\n");
      this.report.append("</pre>\n");
      return;
    }
    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot insert group into target since it has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
              
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStart();

      this.provisioningGroupWrapper.setRecalc(true);
      
      List<ProvisioningGroup> grouperTargetGroupsToInsert = GrouperUtil.toList(this.provisioningGroupWrapper.getGrouperTargetGroup());
      
      // add object change entries
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForGroupsToInsert(grouperTargetGroupsToInsert);
      
      //lets create these
      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(grouperTargetGroupsToInsert));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertGroups(grouperTargetGroupsToInsert, false);
          
        } catch (RuntimeException e) {
          GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
        }
      }
      if (this.provisioningGroupWrapper.getGrouperTargetGroup().getException() != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Inserting group into target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningGroupWrapper.getGrouperTargetGroup().getException())) + "\n");
        return;
      }
  
      if (runtimeException != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Inserting group into target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> No error inserting group into target\n");
      
      //retrieve so we have a copy
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
          this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToInsert, true));
      
      List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
  
      if (GrouperUtil.length(targetGroups) == 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Cannot find group from target after inserting!\n");
        return;
      }
      if (GrouperUtil.length(targetGroups) > 1) {
        this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetGroups) + " groups after inserting, should be 1!\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> Found group from target after inserting\n");

      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(targetGroups, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(targetGroups);
  
      // index
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(targetGroups);
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();

      this.provisioningGroupWrapper.setTargetProvisioningGroup(targetGroups.get(0));
      this.provisioningGroupWrapper.setCreate(false);
        
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Inserting group").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
      
    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "None implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
          
  }
  
  /**
   * select a group from target
   */
  private void appendSelectGroupFromTarget() {
    this.report.append("<h4>Select group from Target</h4><pre>");
    
    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> No provisioningGroupWrapper means no group to select from target\n");
    } else {
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroup(), false)
          && !GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroups(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve specific group(s)\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroups()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve specific group(s)\n");
      } else if (this.provisioningGroupWrapper.getGrouperTargetGroup() == null) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Grouper target group is null\n");
      } else {

        try {
            
          TargetDaoRetrieveGroupResponse targetDaoRetrieveGroupResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroup(
              new TargetDaoRetrieveGroupRequest(this.provisioningGroupWrapper.getGrouperTargetGroup(), 
                  this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()));

          if (targetDaoRetrieveGroupResponse == null) {
            this.report.append("<font color='red'><b>Error:</b></font> TargetDaoRetrieveGroupResponse is null\n");
          } else if (targetDaoRetrieveGroupResponse.getTargetGroup() == null) {
            this.report.append("<font color='gray'><b>Note:</b></font> group is not in target\n");
          } else {
            this.provisioningGroupWrapper.setTargetProvisioningGroup(targetDaoRetrieveGroupResponse.getTargetGroup());
            this.report.append("<font color='gray'><b>Note:</b></font> Target group (unprocessed): ")
              .append(GrouperUtil.xmlEscape(targetDaoRetrieveGroupResponse.getTargetGroup().toString())).append(this.getCurrentDuration()).append("\n");
            
            List<ProvisioningGroup> targetGroupsForOne = GrouperUtil.toList(targetDaoRetrieveGroupResponse.getTargetGroup());
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(
                targetGroupsForOne, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(
                targetGroupsForOne);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
                targetGroupsForOne);

            this.report.append("<font color='gray'><b>Note:</b></font> Target group (filtered, attributes manipulated, matchingId calculated):\n  ")
              .append(GrouperUtil.xmlEscape(targetDaoRetrieveGroupResponse.getTargetGroup().toString())).append("\n");

            if (GrouperUtil.isBlank(targetDaoRetrieveGroupResponse.getTargetGroup().getMatchingId())) {
              this.report.append("<font color='red'><b>Error:</b></font> Target group matching id is blank\n");
            }
            
            if (!GrouperUtil.equals(this.provisioningGroupWrapper.getGrouperTargetGroup().getMatchingId(), targetDaoRetrieveGroupResponse.getTargetGroup().getMatchingId())) {
              this.report.append("<font color='red'><b>Error:</b></font> Matching id's do not match!\n");
            }
            
          }
          
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting specific group(s)").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        }
      }
    }
    this.report.append("</pre>\n");

  }
  
  private void appendValidation() {
    
    this.report.append("<h4>Validation</h4><pre>");

    {
      List<String> errorsToDisplay = new ArrayList<String>();
      
      Map<String, String> validationErrorsToDisplay = new LinkedHashMap<String, String>();
      
      this.getGrouperProvisioner().getProvisionerConfiguration().validatePreSave(false, errorsToDisplay, validationErrorsToDisplay);
  
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Provisioner config JSON rule violations: ")
          .append(errorsToDisplay.size() + validationErrorsToDisplay.size()).append("\n");
        for (String errorToDisplay : errorsToDisplay) {
          this.report.append("<font color='red'><b>Error:</b></font> " + GrouperUtil.xmlEscape(errorToDisplay)).append("\n");
        }
        for (String validationKeyError : validationErrorsToDisplay.keySet()) {
          this.report.append("<font color='red'><b>Error:</b></font> in config item '" + validationKeyError + "': " + GrouperUtil.xmlEscape(validationErrorsToDisplay.get(validationKeyError))).append("\n");
        }
      } else {
        this.report.append("<font color='green'><b>Success:</b></font> Provisioner config satisfies configuration JSON rules\n");
      }
    }

    {
      List<MultiKey> errors = this.getGrouperProvisioner().retrieveGrouperProvisioningConfigurationValidation().validate();
      if (errors.size() > 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Provisioner config validation rule violations: ")
          .append(errors.size()).append("\n");
        for (MultiKey errorMultikey : errors) {
          String error = (String)errorMultikey.getKey(0);
          if (errorMultikey.size() > 1) {
            String validationKeyError = (String)errorMultikey.getKey(1);
            this.report.append("<font color='red'><b>Error:</b></font> in config item '" + validationKeyError + "': " + GrouperUtil.xmlEscape(error)).append("\n");
          } else {
            this.report.append("<font color='red'><b>Error:</b></font> " + GrouperUtil.xmlEscape(error)).append("\n");
          }
        }
      } else {
        this.report.append("<font color='green'><b>Success:</b></font> Provisioner config satisfies validation rules\n");
      }
    }
    
    this.report.append("</pre>\n");
    
  }


  private void appendGeneralInfo() {
    this.report.append("<h4>Provisioner</h4><pre>");
    GrouperProvisioningObjectLogType.appendProvisioner(grouperProvisioner, this.report, "Provisioner");
    this.report.append("</pre>\n<h4>Configuration analysis</h4><pre>");
    GrouperProvisioningObjectLogType.appendConfiguration(grouperProvisioner, this.report, "Configuration");
    this.report.append("</pre>\n<h4>Target Dao capabilities</h4><pre>");
    GrouperProvisioningObjectLogType.appendTargetDaoCapabilities(grouperProvisioner, this.report, "Target Dao capabilities");
    this.report.append("</pre>\n<h4>Provisioner behaviors</h4><pre>");
    GrouperProvisioningObjectLogType.appendTargetDaoBehaviors(grouperProvisioner, this.report, "Provisioner behaviors");
    this.report.append("</pre>\n");
    
  }


  /** 
   * get current duration
   * @return duration
   */
  private String getCurrentDuration() {
    return " (elapsed: " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - this.started) + ")";
  }
  
  public void appendSelectAllGroups() {
    this.report.append("<h4>All groups</h4>");
    this.report.append("<pre>");
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsGroupsAllSelect()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all groups\n");
    } else {
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllGroups(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all groups\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all groups\n");
      } else {

        try {
            
          this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStart();

          TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllGroups(
              new TargetDaoRetrieveAllGroupsRequest(this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()));
          List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveAllGroupsResponse == null ? null : targetDaoRetrieveAllGroupsResponse.getTargetGroups());
          this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().setProvisioningGroups(targetGroups);

          if (GrouperUtil.length(targetGroups) > 0) {
            this.report.append("<font color='green'><b>Success:</b></font> Selected " + GrouperUtil.length(targetGroups) + " groups")
              .append(this.getCurrentDuration()).append("\n");
          } else {
            this.report.append("<font color='orange'><b>Warning:</b></font> Selected " + GrouperUtil.length(targetGroups) + " groups")
              .append(this.getCurrentDuration()).append("\n");
          }

          for (int i=0;i<Math.min(10, GrouperUtil.length(targetGroups)); i++) {
            ProvisioningGroup targetGroup = targetGroups.get(i);

            this.report.append("<font color='gray'><b>Note:</b></font> Group ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetGroups)).append(" (unprocessed):\n  ").append(GrouperUtil.xmlEscape(targetGroup.toString())).append("\n");
          }
          this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(
              targetGroups, true, false, false);
          this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(
              targetGroups);
          this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
              targetGroups);
          for (int i=0;i<Math.min(10, GrouperUtil.length(targetGroups)); i++) {
            ProvisioningGroup targetGroup = targetGroups.get(i);

            this.report.append("<font color='gray'><b>Note:</b></font> Group ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetGroups)).append(" (filtered, attributes manipulated, matchingId calculated):\n  ").append(GrouperUtil.xmlEscape(targetGroup.toString())).append("\n");
          }

          {
            int countWithoutMatchingId = 0;
            for (int i=0;i<GrouperUtil.length(targetGroups);i++) {
              ProvisioningGroup targetGroup = targetGroups.get(i);
              if (GrouperUtil.isBlank(targetGroup.getMatchingId())) {
                countWithoutMatchingId++;
              }
            }
            if (countWithoutMatchingId == 0) {
              this.report.append("<font color='green'><b>Success:</b></font> All target groups have a matching id")
                .append(this.getCurrentDuration()).append("\n");
            } else {
              this.report.append("<font color='red'><b>Error:</b></font> " + countWithoutMatchingId + " target groups do not have a matching id")
                .append(this.getCurrentDuration()).append("\n");
            }
          }
          
          {
            int countWithDuplicateMatchingId = 0;
            Set<Object> matchingIds = new HashSet<Object>();
            Set<Object> firstTen = new HashSet<Object>();
            for (int i=0;i<GrouperUtil.length(targetGroups);i++) {
              ProvisioningGroup targetGroup = targetGroups.get(i);
              if (!GrouperUtil.isBlank(targetGroup.getMatchingId())) {
                if (matchingIds.contains(targetGroup.getMatchingId())) {
                  countWithDuplicateMatchingId++;
                  if (firstTen.size() <= 10) {
                    firstTen.add(targetGroup.getMatchingId());
                  }
                } else {
                  matchingIds.add(targetGroup.getMatchingId());
                }
              }
            }
            if (countWithDuplicateMatchingId == 0) {
              this.report.append("<font color='green'><b>Success:</b></font> All target groups have unique matching ids")
                .append(this.getCurrentDuration()).append("\n");
            } else {
              this.report.append("<font color='red'><b>Error:</b></font> " + countWithDuplicateMatchingId + " target groups have a duplicate matching id, e.g. " + GrouperUtil.toStringForLog(firstTen, 1000))
                .append(this.getCurrentDuration()).append("\n");
            }
          }
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting all groups").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        } finally {
          String debugInfo = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStop();
          debugInfo = StringUtils.defaultString(debugInfo, "None implemented for this DAO");
          this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
        }
      }
    }
    
    this.report.append("</pre>");
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningDiagnosticsContainer.class);

  /**
   * append this to log, and put a not before each line
   * this will escape html
   * @param string
   */
  public void appendReportLineIfNotBlank(String string) {
    if (!StringUtils.isBlank(string) && this.inDiagnostics) {
      int lineNumber = 0;
      for (String line : GrouperUtil.splitTrim(string, "\n")) {
        if (StringUtils.isBlank(line)) {
          continue;
        }
        if (!line.startsWith("<font color='")) {
          this.report.append("<font color='gray'><b>Note:</b></font> ").append(GrouperUtil.xmlEscape(StringUtils.abbreviate(line, 3000))).append("\n");
        } else {
          this.report.append(line).append("\n");
        }
        if (++lineNumber >= 50) {
          this.report.append("<font color='gray'><b>Note:</b></font> Only showing 50 lines\n");
          break;
        }
      }
    }
    
  }

  /**
   * settings for how diagnostics is going to go
   */
  private GrouperProvisioningDiagnosticsSettings grouperProvisioningDiagnosticsSettings = new GrouperProvisioningDiagnosticsSettings();
  
  /**
   * settings for how diagnostics is going to go
   * @return
   */
  public GrouperProvisioningDiagnosticsSettings getGrouperProvisioningDiagnosticsSettings() {
    return grouperProvisioningDiagnosticsSettings;
  }


  
  public void appendSelectAllEntities() {
    this.report.append("<h4>All entities</h4>");
    this.report.append("<pre>");
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsEntitiesAllSelect()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all entities\n");
    } else {
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllEntities(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all entities\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all entities\n");
      } else {
  
        try {
            
          TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllEntities(
              new TargetDaoRetrieveAllEntitiesRequest(this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()));
          List<ProvisioningEntity> targetEntities = targetDaoRetrieveAllEntitiesResponse == null ? null : targetDaoRetrieveAllEntitiesResponse.getTargetEntities();
          this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().setProvisioningEntities(targetEntities);
  
          if (GrouperUtil.length(targetEntities) > 0) {
            this.report.append("<font color='green'><b>Success:</b></font> Selected " + GrouperUtil.length(targetEntities) + " entities")
              .append(this.getCurrentDuration()).append("\n");
          } else {
            this.report.append("<font color='orange'><b>Warning:</b></font> Selected " + GrouperUtil.length(targetEntities) + " entities")
              .append(this.getCurrentDuration()).append("\n");
          }
          
          for (int i=0;i<Math.min(10,GrouperUtil.length(targetEntities)); i++) {
            ProvisioningEntity targetEntity = targetEntities.get(i);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Entity ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetEntities)).append(" (unprocessed):\n  ").append(GrouperUtil.xmlEscape(targetEntity.toString())).append("\n");
  
            List<ProvisioningEntity> targetEntitiesForOne = GrouperUtil.toList(targetEntity);
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(
                targetEntitiesForOne, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(
                targetEntitiesForOne);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(
                targetEntitiesForOne);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Entity ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetEntities)).append(" (filtered, attributes manipulated, matchingId calculated):\n  ").append(GrouperUtil.xmlEscape(targetEntity.toString())).append("\n");
            
          }
  
  
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting all entities").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        }
      }
    }
    
    this.report.append("</pre>");
  }

  public void appendSelectAllMemberships() {
    this.report.append("<h4>All memberships</h4>");
    this.report.append("<pre>");
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all memberships\n");
    } else if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      this.report.append("<font color='gray'><b>Note:</b></font> Membership type is: " + this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() + "\n");
    } else {
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllMemberships(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all memberships\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all memberships\n");
      } else {
  
        try {
            
          TargetDaoRetrieveAllMembershipsResponse targetDaoRetrieveAllMembershipsResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllMemberships(new TargetDaoRetrieveAllMembershipsRequest());
          List<ProvisioningMembership> targetMemberships = targetDaoRetrieveAllMembershipsResponse == null ? null : targetDaoRetrieveAllMembershipsResponse.getTargetMemberships();
          this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().setProvisioningMemberships(targetMemberships);
  
          if (GrouperUtil.length(targetMemberships) > 0) {
            this.report.append("<font color='green'><b>Success:</b></font> Selected " + GrouperUtil.length(targetMemberships) + " memberships")
              .append(this.getCurrentDuration()).append("\n");
          } else {
            this.report.append("<font color='orange'><b>Warning:</b></font> Selected " + GrouperUtil.length(targetMemberships) + " memberships")
              .append(this.getCurrentDuration()).append("\n");
          }
          
          for (int i=0;i<Math.min(10,GrouperUtil.length(targetMemberships)); i++) {
            ProvisioningMembership targetMembership = targetMemberships.get(i);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Membership ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetMemberships)).append(" (unprocessed):\n  ").append(GrouperUtil.xmlEscape(targetMembership.toString())).append("\n");
  
            List<ProvisioningMembership> targetMembershipsForOne = GrouperUtil.toList(targetMembership);
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(
                targetMembershipsForOne, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(
                targetMembershipsForOne);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(
                targetMembershipsForOne);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Membership ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetMemberships)).append(" (filtered, attributes manipulated, matchingId calculated):\n  ").append(GrouperUtil.xmlEscape(targetMembership.toString())).append("\n");
            
          }
  
  
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting all memberships").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        }
      }
    }
    
    this.report.append("</pre>");
  }

  /**
   * init the config of diagnostics from provisioner configuration
   */
  public void initFromConfiguration() {
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isDiagnosticsGroupsAllSelect());
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntitiesAllSelect(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isDiagnosticsEntitiesAllSelect());
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipsAllSelect(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isDiagnosticsMembershipsAllSelect());
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getDiagnosticsGroupName());
    
  }
  
  
  
}
