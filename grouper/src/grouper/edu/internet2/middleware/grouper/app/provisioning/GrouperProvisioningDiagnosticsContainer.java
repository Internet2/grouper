package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsRequest;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2MembershipCache;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2SchemaAttribute;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.app.scim2Provisioning.ScimSettings;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ui.util.ProgressBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

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
    
    ProvisioningConfiguration provisionerConfiguration = this.getGrouperProvisioner().getControllerForProvisioningConfiguration();
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

      this.appendScimSchemaCrossCheck();

      this.appendSelectAllGroups();
      this.appendSelectAllEntities();
      this.appendSelectAllMemberships();
      
      this.appendSelectGroupFromGrouper();
      this.appendSelectGroupFromTarget();

      this.appendSelectEntityFromGrouper();
      this.appendSelectEntityFromTarget();

      this.appendScimSearchAttributeVerification();

      this.appendInsertGroupIntoTarget();
      this.appendInsertEntityIntoTarget();

      this.appendUpdateEntityIntoTarget();

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
        this.appendInsertGroupAttributesMembershipIntoTarget();
        this.appendDeleteGroupAttributesMembershipFromTarget();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
        this.appendInsertEntityAttributesMembershipIntoTarget();
        this.appendDeleteEntityAttributesMembershipFromTarget();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
        
        Boolean resultInsert = this.appendInsertMembershipObjectsIntoTarget();
        Boolean resultDelete = null;
        if (resultInsert != null) {
          resultDelete = this.appendDeleteMembershipObjectsFromTarget();
        }
        
        if (resultInsert != null && !resultInsert && resultDelete != null && resultDelete) {
          this.appendInsertMembershipObjectsIntoTarget();
          this.appendDeleteMembershipObjectsFromTarget();
        }
        
      }
      
      this.appendDeleteGroupFromTarget();
      this.appendDeleteEntityFromTarget();
    } catch (Exception e) {
      LOG.error("error in diagnostics", e);
      this.report.append("</pre><pre>").append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(e))).append("</pre>");
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
  
  private ProvisioningEntityWrapper provisioningEntityWrapper = null;

  private ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
  
  /**
   * select a group from grouper
   */
  public void appendSelectGroupFromGrouper() {

    this.report.append("<h4>Select group from Grouper</h4><pre>");
    
    String groupName = this.getGrouperProvisioningDiagnosticsSettings().getDiagnosticsGroupName();
    if (StringUtils.isBlank(groupName)) {
      this.report.append("<font color='orange'><b>Warning:</b></font> Group name for diagnostics is not set\n");
    } else {
    
      Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false);
      if (group == null) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Group '").append(GrouperUtil.xmlEscape(groupName)).append("' does not exist in Grouper. Going to create one.\n");
        group = new GroupSave(GrouperSession.staticGrouperSession()).assignName(groupName).assignSaveMode(SaveMode.INSERT).save();
        
      } else {
        this.report.append("<font color='gray'><b>Note:</b></font> Group: ").append(GrouperUtil.xmlEscape(group.toStringDb())).append(this.getCurrentDuration()).append("\n");
      }
      
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(group.getId());
      
      if (gcGrouperSyncGroup == null) {
        this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncGroup record does not exist in database. Going to create one.\n");
        gcGrouperSyncGroup = new GcGrouperSyncGroup();
        gcGrouperSyncGroup.setGrouperSync(gcGrouperSync);
        gcGrouperSyncGroup.setGroupId(group.getId());
        gcGrouperSyncGroup.setProvisionable(true);
        
        gcGrouperSync.getGcGrouperSyncGroupDao().internal_groupStore(gcGrouperSyncGroup);
      } else {
        this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncGroup: ").append(GrouperUtil.xmlEscape(gcGrouperSyncGroup.toString())).append(this.getCurrentDuration()).append("\n");
      }
      
      String subjectIdOrIdentifier = this.getGrouperProvisioningDiagnosticsSettings().getDiagnosticsSubjectIdOrIdentifier();
      if (StringUtils.isNotBlank(subjectIdOrIdentifier)) {
        Subject subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, false);
        if (subject != null && !group.hasMember(subject)) {
          group.addMember(subject);
        }
      }
        
      GrouperProvisioningAttributeValue provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(group, this.getGrouperProvisioner().getConfigId());
      
      if (provisioningAttributeValue == null) {
        GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
        attributeValue.setDirectAssignment(true);
        attributeValue.setDoProvision(this.getGrouperProvisioner().getConfigId());
        attributeValue.setTargetName(this.getGrouperProvisioner().getConfigId());
        
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, group);
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
        
        List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
        
        if (GrouperUtil.length(grouperTargetGroups) == 0) {
          this.report.append("<font color='gray'><b>Note:</b></font> Cannot find grouperTargetGroup object after translation, perhaps the group is not supposed to translate\n");
        } else {
          GrouperUtil.assertion(grouperTargetGroups.size() == 1, "Why is size not 1???? " + grouperTargetGroups.size());
          ProvisioningGroup grouperTargetGroup = grouperTargetGroups.get(0);
          this.provisioningGroupWrapper.setGrouperTargetGroup(grouperTargetGroup);
          this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningGroup (translated): ").append(GrouperUtil.xmlEscape(grouperTargetGroup.toString())).append("\n");
        
          this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(grouperTargetGroups, true, true, false, false);

          this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(grouperTargetGroups);

          this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningGroup (filtered, attributes manipulated, matchingId calculated): ").append(GrouperUtil.xmlEscape(grouperTargetGroup.toString())).append("\n");
          
          if (GrouperUtil.length(grouperTargetGroup.getMatchingIdAttributeNameToValues()) == 0) {
            
            boolean attributeInsertOrUpdate = false;
            
            for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
              if (matchingAttribute.isInsert() || matchingAttribute.isUpdate()) {
                attributeInsertOrUpdate = true;
              }
            }

            if (GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) == 0) {
              
              if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroups()) {
                if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isInsertGroups() || this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isUpdateGroups() || this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isDeleteGroups()) {
                  this.report.append("<font color='red'><b>Error:</b></font> Cannot find the group matching attribute/field\n");
                } else {
                  this.report.append("<font color='gray'><b>Note:</b></font> Cannot find the group matching attribute/field\n");
                }
              }
              
            } else {
              if (!attributeInsertOrUpdate) {
                if (gcGrouperSyncGroup != null && gcGrouperSyncGroup.getInTarget() != null && gcGrouperSyncGroup.getInTarget()) {
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
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(grouperTargetGroups, false, false, true);
          
          if (this.provisioningGroupWrapper.getErrorCode() != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Group is not valid! " + this.provisioningGroupWrapper.getErrorCode() + "\n");
          } else {
            this.report.append("<font color='green'><b>Success:</b></font> Group is valid\n");
          }
        }          
      }
      
    }
    this.report.append("</pre>\n");

  }
  
  /**
   * select an entity from grouper
   */
  public void appendSelectEntityFromGrouper() {

    this.report.append("<h4>Select entity from Grouper</h4><pre>");
    
    String subjectIdOrIdentifier = this.getGrouperProvisioningDiagnosticsSettings().getDiagnosticsSubjectIdOrIdentifier();
    if (StringUtils.isBlank(subjectIdOrIdentifier)) {
      this.report.append("<font color='orange'><b>Warning:</b></font> Subject id or identifier for diagnostics is not set\n");
    } else {
    
      Subject subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, false);

      if (subject == null) {

        this.report.append("<font color='orange'><b>Warning:</b></font> Subject '").append(GrouperUtil.xmlEscape(subjectIdOrIdentifier)).append("' is not resolvable\n");
        
      } else {

        Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, false);
        if (member == null) {
          this.report.append("<font color='orange'><b>Warning:</b></font> Subject '").append(GrouperUtil.xmlEscape(subjectIdOrIdentifier)).append("' is not in the grouper_members table\n");
        } else {
          
          this.report.append("<font color='gray'><b>Note:</b></font> Member: ").append(GrouperUtil.xmlEscape(member.toString())).append(this.getCurrentDuration()).append("\n");
  
          GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
          GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member.getId());
          if (gcGrouperSyncMember == null) {
            this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncMember record does not exist in database\n");
          } else {
            this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncMember: ").append(GrouperUtil.xmlEscape(gcGrouperSyncMember.toString())).append(this.getCurrentDuration()).append("\n");
          }
          
          List<ProvisioningEntity> grouperProvisioningEntities = this.grouperProvisioner.retrieveGrouperDao().retrieveMembers(false, GrouperUtil.toList(member.getId()));
          if (GrouperUtil.length(grouperProvisioningEntities) == 0) {
            grouperProvisioningEntities = this.grouperProvisioner.retrieveGrouperDao().retrieveMembersNonProvisionable(GrouperUtil.toList(member.getId()));
            if (GrouperUtil.length(grouperProvisioningEntities) > 0) {
              this.report.append("<font color='gray'><b>Note:</b></font> Could not find entity through the provisionable group membership cache (it may not be populated yet); retrieved the member directly for diagnostics\n");
            }
          }
          if (GrouperUtil.length(grouperProvisioningEntities) == 0) {
            this.report.append("<font color='orange'><b>Warning:</b></font> Cannot find ProvisioningEntity object, perhaps entity is not a member of any provisionable groups or in the list of entities to provision\n");
          } else {
            ProvisioningEntity grouperProvisioningEntity = grouperProvisioningEntities.get(0);
            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningEntity (unprocessed): ").append(GrouperUtil.xmlEscape(grouperProvisioningEntity.toString())).append(this.getCurrentDuration()).append("\n");
           
            this.provisioningEntityWrapper = new ProvisioningEntityWrapper();
            grouperProvisioningEntity.setProvisioningEntityWrapper(this.provisioningEntityWrapper);
            this.provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
            this.provisioningEntityWrapper.setGrouperProvisioningEntity(grouperProvisioningEntity);
            this.provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
            
            this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().put(provisioningEntityWrapper.getMemberId(), provisioningEntityWrapper);
            
            this.grouperProvisioner.retrieveGrouperProvisioningSyncIntegration().fullSyncMembersForInitialize();

            List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetEntities(GrouperUtil.toList(grouperProvisioningEntity), false, false);
            
            if (GrouperUtil.length(grouperTargetEntities) == 0) {
              this.report.append("<font color='gray'><b>Note:</b></font> Cannot find grouperTargetEntity object after translation, perhaps the entity is not supposed to translate\n");
            } else {
              GrouperUtil.assertion(grouperTargetEntities.size() == 1, "Why is size not 1???? " + grouperTargetEntities.size());
              ProvisioningEntity grouperTargetEntity = grouperTargetEntities.get(0);
              this.provisioningEntityWrapper.setGrouperTargetEntity(grouperTargetEntity);
              this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningEntity (translated): ").append(GrouperUtil.xmlEscape(grouperTargetEntity.toString())).append("\n");
            
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(grouperTargetEntities, true, true, false, false);

              this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(grouperTargetEntities);
  
              this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningEntity (filtered, attributes manipulated, matchingId calculated): ").append(GrouperUtil.xmlEscape(grouperTargetEntity.toString())).append("\n");
              
              if (GrouperUtil.length(grouperTargetEntity.getMatchingIdAttributeNameToValues()) == 0) {
                
                boolean attributeInsertOrUpdate = false;
                
                for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) {
                  if (matchingAttribute.isInsert() || matchingAttribute.isUpdate()) {
                    attributeInsertOrUpdate = true;
                  }
                }

                if (GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) == 0) {
                  
                  if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntities()) {
                    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isInsertEntities() || this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isUpdateEntities() || this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isDeleteEntities()) {
                      this.report.append("<font color='red'><b>Error:</b></font> Cannot find the entity matching attribute/field\n");
                    } else {
                      this.report.append("<font color='gray'><b>Note:</b></font> Cannot find the entity matching attribute/field\n");
                    }
                  }
                  
                } else {
                  if (!attributeInsertOrUpdate) {
                    if (gcGrouperSyncMember != null && gcGrouperSyncMember.getInTarget() != null && gcGrouperSyncMember.getInTarget()) {
                      this.report.append("<font color='red'><b>Error:</b></font> Grouper target entity matching id is blank and it is currently in target\n");
                    } else {
                      this.report.append("<font color='green'><b>Success:</b></font> Grouper target entity matching id is blank but it is not inserted or updated so it probably is not retrieved from target yet\n");
                    }
                  } else {
                    this.report.append("<font color='red'><b>Error:</b></font> Grouper target entity matching id is blank\n");
                  }
                }
                
              }
              
              // validate
              this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(grouperTargetEntities, false, false, true);
              
              if (this.provisioningEntityWrapper.getErrorCode() != null) {
                this.report.append("<font color='red'><b>Error:</b></font> Entity is not valid! " + this.provisioningEntityWrapper.getErrorCode() + "\n");
              } else {
                this.report.append("<font color='green'><b>Success:</b></font> Entity is valid\n");
              }
            }          
          }
        }
      }
      
    }
    this.report.append("</pre>\n");

  }
  
  /**
   * insert entity to group as a group attribute in target
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void appendInsertGroupAttributesMembershipIntoTarget() {
    this.report.append("<h4>Add entity to group (groupAttribute)</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipInsert()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to add entity to group in target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add entity to group in target because there's no specified group\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper != null && this.provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add entity to group in target since the group does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add entity to group in target since the group has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
    
    if (this.provisioningEntityWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add entity to group in target because there's no specified entity\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add entity to group in target since the entity has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
     
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      Set<MultiKey> groupUuidMemberUuids = new HashSet<MultiKey>();
      MultiKey groupIdMemberId = new MultiKey(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
      groupUuidMemberUuids.add(groupIdMemberId);
      List<ProvisioningMembership> grouperProvisioningMemberships = this.grouperProvisioner.retrieveGrouperDao().retrieveMemberships(false, null, null, groupUuidMemberUuids);
      
      if (GrouperUtil.length(grouperProvisioningMemberships) == 0) {
        // the lookup above reads the provisionable membership cache (grouper_sql_cache_*), which may
        // not be populated yet on a fresh setup. the group + entity are already resolved and the
        // Select-group step added the entity as a member, so build the membership directly from the
        // wrappers for diagnostics instead of skipping the test.
        ProvisioningMembership directProvisioningMembership = new ProvisioningMembership();
        directProvisioningMembership.setProvisioningGroupId(this.provisioningGroupWrapper.getGroupId());
        directProvisioningMembership.setProvisioningEntityId(this.provisioningEntityWrapper.getMemberId());
        grouperProvisioningMemberships = GrouperUtil.toList(directProvisioningMembership);
        this.report.append("<font color='gray'><b>Note:</b></font> Could not find membership through the provisionable membership cache (it may not be populated yet); built the membership directly for diagnostics\n");
      }
      {
        ProvisioningMembership grouperProvisioningMembership = grouperProvisioningMemberships.get(0);

        GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
        GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
        if (gcGrouperSyncMembership == null) {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership record does not exist in database\n");

        } else {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership: ").append(GrouperUtil.xmlEscape(gcGrouperSyncMembership.toString())).append(this.getCurrentDuration()).append("\n");
        }

        ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        grouperProvisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembershipWrapper.setGrouperProvisioningMembership(grouperProvisioningMembership);
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
        
        this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLink(GrouperUtil.toSet(provisioningEntityWrapper), true);
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().put(groupIdMemberId, provisioningMembershipWrapper);

        this.grouperProvisioner.retrieveGrouperProvisioningSyncIntegration().fullSyncMemberships();

        grouperProvisioningMembership.setProvisioningGroup(this.provisioningGroupWrapper.getGrouperProvisioningGroup());
        grouperProvisioningMembership.setProvisioningEntity(this.provisioningEntityWrapper.getGrouperProvisioningEntity());
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().add(this.provisioningGroupWrapper);

        this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetMemberships(GrouperUtil.toList(grouperProvisioningMembership), false);

        List<ProvisioningGroup> grouperTargetGroupsToUpdate = GrouperUtil.toList(this.provisioningGroupWrapper.getGrouperTargetGroup());
        String membershipAttributeName = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
        
        ProvisioningAttribute provisioningAttribute = this.provisioningGroupWrapper.getGrouperTargetGroup().retrieveProvisioningAttribute(membershipAttributeName);
        Collection<String> values = provisioningAttribute == null ? null : (Collection)provisioningAttribute.getValue();
        String value = values == null || values.size() == 0 ? null : values.iterator().next();
                
        if (values.size() == 0) {
          this.report.append("<font color='red'><b>Error:</b></font> No values to add after translation\n");
        } else if (values.size() > 1) {
          this.report.append("<font color='red'><b>Error:</b></font> Translation resulted in multiple values: " + values + "\n");
        } else if (this.provisioningGroupWrapper.getTargetProvisioningGroup().retrieveProvisioningAttribute(membershipAttributeName) != null && ((Collection)this.provisioningGroupWrapper.getTargetProvisioningGroup().retrieveProvisioningAttribute(membershipAttributeName).getValue()).contains(value)) {
          this.report.append("<font color='orange'><b>Warning:</b></font> Target already contains value: " + value + "\n");
        } else {
          grouperTargetGroupsToUpdate.get(0).addInternal_objectChange(
              new ProvisioningObjectChange(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), 
                  ProvisioningObjectChangeAction.insert, null, value)
              );
  
          this.grouperProvisioner.retrieveGrouperProvisioningCompare().removeGroupDefaultMembershipAttributeValueIfAnyAdded(grouperTargetGroupsToUpdate);
  
          for (ProvisioningObjectChange provisioningObjectChange : grouperTargetGroupsToUpdate.get(0).getInternal_objectChanges()) {
            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningObjectChange: attributeName=" + provisioningObjectChange.getAttributeName() + ", action=" + provisioningObjectChange.getProvisioningObjectChangeAction() + ", oldValue=" + provisioningObjectChange.getOldValue() + ", newValue=" + provisioningObjectChange.getNewValue() + "\n");
          }
          
          RuntimeException runtimeException = null;
          try {
            this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().updateGroups(new TargetDaoUpdateGroupsRequest(grouperTargetGroupsToUpdate));
          } catch (RuntimeException re) {
            runtimeException = re;
          } finally {
            try {
              this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsUpdateGroupsFull(grouperTargetGroupsToUpdate, true);
  
            } catch (RuntimeException e) {
              GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
            }
          }
  
          if (this.provisioningGroupWrapper.getGrouperTargetGroup().getException() != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Adding entity to group in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningGroupWrapper.getGrouperTargetGroup().getException())) + "\n");
            return;
          }
  
          if (runtimeException != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Adding entity to group in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
            return;
          }
          this.report.append("<font color='green'><b>Success:</b></font> No error adding entity to group in target\n");
          
          // check target
          TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToUpdate, true));
          
          List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
      
          if (GrouperUtil.length(targetGroups) == 0) {
            this.report.append("<font color='red'><b>Error:</b></font> Cannot find group from target after inserting membership!\n");
          } else if (GrouperUtil.length(targetGroups) > 1) {
            this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetGroups) + " groups after inserting membership, should be 1!\n");
          } else {
            if (targetGroups.get(0).retrieveProvisioningAttribute(membershipAttributeName) == null) {
              this.report.append("<font color='red'><b>Error:</b></font> Did not find membership in target after inserting: " + value + "\n");
            } else if (targetGroups.get(0).retrieveProvisioningAttribute(membershipAttributeName) != null && ((Collection)targetGroups.get(0).retrieveProvisioningAttribute(membershipAttributeName).getValue()).contains(value)) {
              this.report.append("<font color='green'><b>Success:</b></font> Found membership in target after inserting: " + value + "\n");
            } else {
              this.report.append("<font color='red'><b>Error:</b></font> Did not find membership in target after inserting: " + value + "\n");
            }
          }
          
          updateProvisioningGroupWrapperAfterTargetQuery(targetGroups);
          
          this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
        }
      }
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Adding entity to group").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));

    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
  }
  
  
  /**
   * @return null if not applicable, true if inserted, false if already existed
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Boolean appendInsertMembershipObjectsIntoTarget() {
    
    Boolean result = null;
    this.report.append("<h4>Add membership (membershipObjects)</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipInsert()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to add membership in target\n");
      this.report.append("</pre>\n");
      return null;
    }

    if (this.provisioningGroupWrapper == null || this.provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add membership in target since the group does not exist there\n");
      this.report.append("</pre>\n");
      return null;
    }
    
    if (this.provisioningEntityWrapper == null || this.provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add membership in target since the entity does not exist there\n");
      this.report.append("</pre>\n");
      return null;
    }

    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add membership in target since the group has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return null;
    }
    
    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add membership in target since the entity has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return null;
    }
    
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      Set<MultiKey> groupUuidMemberUuids = new HashSet<MultiKey>();
      MultiKey groupIdMemberId = new MultiKey(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
      groupUuidMemberUuids.add(groupIdMemberId);
      List<ProvisioningMembership> grouperProvisioningMemberships = this.grouperProvisioner.retrieveGrouperDao().retrieveMemberships(false, null, null, groupUuidMemberUuids);
      
      if (GrouperUtil.length(grouperProvisioningMemberships) == 0) {
        // the lookup above reads the provisionable membership cache (grouper_sql_cache_*), which may
        // not be populated yet on a fresh setup. the group + entity are already resolved and the
        // Select-group step added the entity as a member, so build the membership directly from the
        // wrappers for diagnostics instead of bailing.
        ProvisioningMembership directProvisioningMembership = new ProvisioningMembership();
        directProvisioningMembership.setProvisioningGroupId(this.provisioningGroupWrapper.getGroupId());
        directProvisioningMembership.setProvisioningEntityId(this.provisioningEntityWrapper.getMemberId());
        grouperProvisioningMemberships = GrouperUtil.toList(directProvisioningMembership);
        this.report.append("<font color='gray'><b>Note:</b></font> Could not find membership through the provisionable membership cache (it may not be populated yet); built the membership directly for diagnostics\n");
      } 
        
      ProvisioningMembership grouperProvisioningMembership = grouperProvisioningMemberships.get(0);

      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
      if (gcGrouperSyncMembership == null) {
        this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership record does not exist in database\n");

      } else {
        this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership: ").append(GrouperUtil.xmlEscape(gcGrouperSyncMembership.toString())).append(this.getCurrentDuration()).append("\n");
      }
      GrouperProvisioningLists grouperProvisioningLists = new GrouperProvisioningLists();
      grouperProvisioningLists.setProvisioningMemberships(grouperProvisioningMemberships);
      this.getGrouperProvisioner().retrieveGrouperDao().processWrappers(grouperProvisioningLists);
      
      this.getGrouperProvisioner().retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();
      
      this.grouperProvisioner.retrieveGrouperProvisioningSyncIntegration().fullSyncMemberships();
      
      GrouperProvisioningLists extraTargetData = this.getGrouperProvisioner().retrieveGrouperProvisioningLogic().retrieveExtraTargetData(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetProvisioningLists());
      if (extraTargetData != null) {
        this.grouperProvisioner.retrieveGrouperProvisioningLogic().processTargetDataEntities(extraTargetData.getProvisioningEntities(), false);
        this.grouperProvisioner.retrieveGrouperProvisioningLogic().processTargetDataGroups(extraTargetData.getProvisioningGroups(), false);
        this.grouperProvisioner.retrieveGrouperProvisioningLogic().processTargetDataMemberships(extraTargetData.getProvisioningMemberships(), false);
      }
      
      this.provisioningMembershipWrapper = grouperProvisioningMembership.getProvisioningMembershipWrapper();
      
      grouperProvisioningMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForRecalc());
      this.provisioningMembershipWrapper.getGrouperProvisioningMembership().setProvisioningGroup(this.provisioningGroupWrapper.getGrouperProvisioningGroup());
      List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetMemberships(GrouperUtil.toList(grouperProvisioningMembership), false);

              
      if (grouperProvisioningMembership.getProvisioningMembershipWrapper().getGrouperTargetMembership() == null) {
        this.report.append("<font color='red'><b>Error:</b></font> Could not translate\n");
        return result;
      }
      
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesMemberships(grouperTargetMemberships, true, true, false, false);
      
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false));

      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships(null);
      
      this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsSelectMembershipsFull(
          this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers(),
          this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers(),
          this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers());
      
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetMemberships(GrouperUtil.toSet(grouperProvisioningMembership.getProvisioningMembershipWrapper()));
      
      RuntimeException runtimeException = null;
      
      if (GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships()) > 0) {
        try {
          this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().insertMemberships(new TargetDaoInsertMembershipsRequest(grouperTargetMemberships));
        } catch (RuntimeException re) {
          runtimeException = re;
          result = null;
        } finally {
          try {
            this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsInsertMemberships(grouperTargetMemberships);

          } catch (RuntimeException e) {
            GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
          }
        }
        
        if (this.provisioningMembershipWrapper.getGrouperTargetMembership().getException() != null) {
          this.report.append("<font color='red'><b>Error:</b></font> Adding membership in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningMembershipWrapper.getGrouperTargetMembership().getException())) + "\n");
          result = null;
        }

        if (runtimeException != null) {
          this.report.append("<font color='red'><b>Error:</b></font> Adding membership in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
          result = null;
        }
        this.report.append("<font color='green'><b>Success:</b></font> No error adding membership in target\n");
        result = true;
        
      } else {
        this.report.append("<font color='gray'><b>Note:</b></font> Membership was already in target\n");
        result = false;
      }
      
      if (result == null) {
        return result;
      }
      
      if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsForMembership()) {
        // check target
        TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest = new TargetDaoRetrieveMembershipsRequest();
        targetDaoRetrieveMembershipsRequest.setTargetMemberships(GrouperUtil.toList(this.provisioningMembershipWrapper.getGrouperTargetMembership()));
        TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(
            targetDaoRetrieveMembershipsRequest);
        
        ProvisioningMembership targetProvisioningMembership = (targetDaoRetrieveMembershipsResponse == null || GrouperUtil.length(targetDaoRetrieveMembershipsResponse.getTargetMemberships()) == 0) 
            ? null : (ProvisioningMembership)targetDaoRetrieveMembershipsResponse.getTargetMemberships().get(0);

        if (targetProvisioningMembership == null) {
          this.report.append("<font color='red'><b>Error:</b></font> Cannot find membership from target after inserting membership!\n");
          return null;
        }
        this.report.append("<font color='green'><b>Success:</b></font> Found membership in target after inserting\n");
    
        updateProvisioningMembershipWrapperAfterTargetQuery(grouperTargetMemberships);
      } else {
        this.report.append("<font color='gray'><b>Note:</b></font> Cannot verify membership is in target since cannot retrieve individual memberships\n");
      }
      
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      return result;
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Adding membership to target").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));

    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
    
    return null;
  }
  
  /**
   * @return null if not applicable, true if deleted, false if didn't exist
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Boolean appendDeleteMembershipObjectsFromTarget() {
    
    Boolean result = null;
    this.report.append("<h4>Delete membership (membershipObjects)</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipDelete()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to delete membership in target\n");
      this.report.append("</pre>\n");
      return null;
    }

    if (this.provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add membership in target since the group does not exist there\n");
      this.report.append("</pre>\n");
      return null;
    }
    
    if (this.provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add membership in target since the entity does not exist there\n");
      this.report.append("</pre>\n");
      return null;
    }

    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add membership in target since the group has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return null;
    }
    
    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add membership in target since the entity has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return null;
    }
    
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();
     
      RuntimeException runtimeException = null;
      List<ProvisioningMembership> grouperTargetMemberships = GrouperUtil.toList(this.provisioningMembershipWrapper.getGrouperTargetMembership());
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().deleteMemberships(new TargetDaoDeleteMembershipsRequest(grouperTargetMemberships));
      } catch (RuntimeException re) {
        runtimeException = re;
        result = null;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsDeleteMemberships(grouperTargetMemberships);

        } catch (RuntimeException e) {
          GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
        }
      }
      
      if (this.provisioningMembershipWrapper.getGrouperTargetMembership().getException() != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Delete membership from target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningMembershipWrapper.getGrouperTargetMembership().getException())) + "\n");
        result = null;
      }

      if (runtimeException != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Delete membership in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
        result = null;
      }
      this.report.append("<font color='green'><b>Success:</b></font> No error deleting membership from target\n");
      result = true;
      
      if (result == null) {
        return result;
      }
      
      // check target
      TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest = new TargetDaoRetrieveMembershipsRequest();
      targetDaoRetrieveMembershipsRequest.setTargetMemberships(GrouperUtil.toList(this.provisioningMembershipWrapper.getGrouperTargetMembership()));
      TargetDaoRetrieveMembershipsResponse targetDaoRetrieveMembershipsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(
          targetDaoRetrieveMembershipsRequest);
      
      ProvisioningMembership targetProvisioningMembership = (targetDaoRetrieveMembershipsResponse == null || GrouperUtil.length(targetDaoRetrieveMembershipsResponse.getTargetMemberships()) == 0) 
          ? null : (ProvisioningMembership)targetDaoRetrieveMembershipsResponse.getTargetMemberships().get(0);
  
      if (targetProvisioningMembership != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Found membership in target after deleting membership!\n");
        return null;
      } 
      this.report.append("<font color='green'><b>Success:</b></font> Did not find membership in target after deleting\n");
      
      updateProvisioningMembershipWrapperAfterTargetQuery(grouperTargetMemberships);
      
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      return result;
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Deleting membership from target").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));

    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
    
    return null;
  }
  
  /**
   * remove entity from group as a group attribute in target
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void appendDeleteGroupAttributesMembershipFromTarget() {
    this.report.append("<h4>Remove entity from group (groupAttribute)</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipDelete()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to remove entity from group in target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot remove entity from group in target because there's no specified group\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper != null && this.provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot remove entity from group in target since the group does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot remove entity from group in target since the group has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
    
    if (this.provisioningEntityWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot remove entity from group in target because there's no specified entity\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot remove entity from group in target since the entity has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
     
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      Set<MultiKey> groupUuidMemberUuids = new HashSet<MultiKey>();
      MultiKey groupIdMemberId = new MultiKey(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
      groupUuidMemberUuids.add(groupIdMemberId);
      List<ProvisioningMembership> grouperProvisioningMemberships = this.grouperProvisioner.retrieveGrouperDao().retrieveMemberships(false, null, null, groupUuidMemberUuids);
      
      if (GrouperUtil.length(grouperProvisioningMemberships) == 0) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Cannot find ProvisioningMembership object.  Note that the entity must be a member of the group in Grouper.\n");
      } else {
        ProvisioningMembership grouperProvisioningMembership = grouperProvisioningMemberships.get(0);

        GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
        GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
        if (gcGrouperSyncMembership == null) {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership record does not exist in database\n");

        } else {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership: ").append(GrouperUtil.xmlEscape(gcGrouperSyncMembership.toString())).append(this.getCurrentDuration()).append("\n");
        }

        ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        grouperProvisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembershipWrapper.setGrouperProvisioningMembership(grouperProvisioningMembership);
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);

        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().put(groupIdMemberId, provisioningMembershipWrapper);

        this.grouperProvisioner.retrieveGrouperProvisioningSyncIntegration().fullSyncMemberships();

        grouperProvisioningMembership.setProvisioningGroup(this.provisioningGroupWrapper.getGrouperProvisioningGroup());
        grouperProvisioningMembership.setProvisioningEntity(this.provisioningEntityWrapper.getGrouperProvisioningEntity());
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().add(this.provisioningGroupWrapper);

        this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetMemberships(GrouperUtil.toList(grouperProvisioningMembership), false);

        List<ProvisioningGroup> grouperTargetGroupsToUpdate = GrouperUtil.toList(this.provisioningGroupWrapper.getGrouperTargetGroup());
        String membershipAttributeName = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
        
        ProvisioningAttribute provisioningAttribute = this.provisioningGroupWrapper.getGrouperTargetGroup().retrieveProvisioningAttribute(membershipAttributeName);
        Collection<String> values = provisioningAttribute == null ? null : (Collection)provisioningAttribute.getValue();
        String value = values == null || values.size() == 0 ? null : values.iterator().next();
                
        if (values.size() == 0) {
          this.report.append("<font color='red'><b>Error:</b></font> No values to remove after translation\n");
        } else if (values.size() > 1) {
          this.report.append("<font color='red'><b>Error:</b></font> Translation resulted in multiple values: " + values + "\n");
        } else if (this.provisioningGroupWrapper.getTargetProvisioningGroup().retrieveProvisioningAttribute(membershipAttributeName) == null || !((Collection)this.provisioningGroupWrapper.getTargetProvisioningGroup().retrieveProvisioningAttribute(membershipAttributeName).getValue()).contains(value)) {
          this.report.append("<font color='orange'><b>Warning:</b></font> Target does not contain value: " + value + "\n");
        } else {
          grouperTargetGroupsToUpdate.get(0).addInternal_objectChange(
              new ProvisioningObjectChange(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), 
                  ProvisioningObjectChangeAction.delete, value, null)
              );
  
          this.grouperProvisioner.retrieveGrouperProvisioningCompare().addGroupDefaultMembershipAttributeValueIfAllRemoved(grouperTargetGroupsToUpdate);
  
          for (ProvisioningObjectChange provisioningObjectChange : grouperTargetGroupsToUpdate.get(0).getInternal_objectChanges()) {
            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningObjectChange: attributeName=" + provisioningObjectChange.getAttributeName() + ", action=" + provisioningObjectChange.getProvisioningObjectChangeAction() + ", oldValue=" + provisioningObjectChange.getOldValue() + ", newValue=" + provisioningObjectChange.getNewValue() + "\n");
          }
          
          RuntimeException runtimeException = null;
          try {
            this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().updateGroups(new TargetDaoUpdateGroupsRequest(grouperTargetGroupsToUpdate));
          } catch (RuntimeException re) {
            runtimeException = re;
          } finally {
            try {
              this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsUpdateGroupsFull(grouperTargetGroupsToUpdate, true);
  
            } catch (RuntimeException e) {
              GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
            }
          }
  
          if (this.provisioningGroupWrapper.getGrouperTargetGroup().getException() != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Removing entity from group in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningGroupWrapper.getGrouperTargetGroup().getException())) + "\n");
            return;
          }
  
          if (runtimeException != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Removing entity from group in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
            return;
          }
          this.report.append("<font color='green'><b>Success:</b></font> No error removing entity from group in target\n");
          
          // check target
          TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToUpdate, true));
          
          List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
      
          if (GrouperUtil.length(targetGroups) == 0) {
            this.report.append("<font color='red'><b>Error:</b></font> Cannot find group from target after removing membership!\n");
          } else if (GrouperUtil.length(targetGroups) > 1) {
            this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetGroups) + " groups after removing membership, should be 1!\n");
          } else {
            if (targetGroups.get(0).retrieveProvisioningAttribute(membershipAttributeName) == null) {
              this.report.append("<font color='green'><b>Success:</b></font> Did not find membership in target after removing: " + value + "\n");
            } else if (targetGroups.get(0).retrieveProvisioningAttribute(membershipAttributeName) != null && ((Collection)targetGroups.get(0).retrieveProvisioningAttribute(membershipAttributeName).getValue()).contains(value)) {
              this.report.append("<font color='red'><b>Error:</b></font> Found membership in target after removing: " + value + "\n");
            } else {
              this.report.append("<font color='green'><b>Success:</b></font> Did not find membership in target after removing: " + value + "\n");
            }
          }
          
          updateProvisioningGroupWrapperAfterTargetQuery(targetGroups);
          
          this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
        }
      }
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Removing entity from group").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));

    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
  }
  

  /**
   * insert group into target
   */
  public void appendInsertGroupIntoTarget() {
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
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      this.provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc());
      
      List<ProvisioningGroup> grouperTargetGroupsToInsert = GrouperUtil.toList(this.provisioningGroupWrapper.getGrouperTargetGroup());
      
      // add object change entries
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForGroupsToInsert(grouperTargetGroupsToInsert);
      
      //lets create these
      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(grouperTargetGroupsToInsert));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsInsertGroups(grouperTargetGroupsToInsert, false);
          
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
      
      if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups()) {
        //retrieve so we have a copy
        TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
            this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToInsert, true));
        
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
        
        updateProvisioningGroupWrapperAfterTargetQuery(targetGroups);
      }
      
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Inserting group").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));
      
    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
          
  }
  
  public void updateProvisioningGroupWrapperAfterTargetQuery(List<ProvisioningGroup> targetGroups) {
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(targetGroups, false, true, false, false);

    // index
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(targetGroups);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups(targetGroups);
      
    this.provisioningGroupWrapper.setTargetProvisioningGroup(targetGroups.get(0));
    this.provisioningGroupWrapper.getProvisioningStateGroup().setCreate(false);
    
    GrouperUtil.setClear(this.provisioningGroupWrapper.getGrouperTargetGroup().getInternal_objectChanges());
  }
  
  public void updateProvisioningMembershipWrapperAfterTargetQuery(List<ProvisioningMembership> targetMemberships) {
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesMemberships(targetMemberships, false, true, false, false);

    // index
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships(targetMemberships);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships(targetMemberships);
      
    this.provisioningMembershipWrapper.setTargetProvisioningMembership(targetMemberships.get(0));
    this.provisioningMembershipWrapper.getProvisioningStateMembership().setCreate(false);
    
    GrouperUtil.setClear(this.provisioningMembershipWrapper.getGrouperTargetMembership().getInternal_objectChanges());
  }
  
  /**
   * delete group from target
   */
  public void appendDeleteGroupFromTarget() {
    this.report.append("<h4>Delete group from Target</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsGroupDelete()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to delete group from target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot delete group because there's no specified group\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper != null && this.provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot delete group from target since it does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot delete group from target since it has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
              
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      this.provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc());
      
      List<ProvisioningGroup> grouperTargetGroupsToDelete = GrouperUtil.toList(this.provisioningGroupWrapper.getGrouperTargetGroup());
      
      //lets delete
      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().deleteGroups(new TargetDaoDeleteGroupsRequest(grouperTargetGroupsToDelete));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsDeleteGroups(grouperTargetGroupsToDelete, false);
          
        } catch (RuntimeException e) {
          GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
        }
      }
      if (this.provisioningGroupWrapper.getGrouperTargetGroup().getException() != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Deleting group from target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningGroupWrapper.getGrouperTargetGroup().getException())) + "\n");
        return;
      }
  
      if (runtimeException != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Deleting group from target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> No error deleting group from target\n");
      
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
          this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToDelete, true));
      
      List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());

      if (GrouperUtil.length(targetGroups) > 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetGroups) + " groups in target after deleting, should be 0!\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> Did not find group in target after deleting\n");
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
        // delete the membership sync objects
        this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMembershipDao().membershipDeleteBySyncGroupId(this.provisioningGroupWrapper.getGcGrouperSyncGroup().getId(), false);  
      }
      
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Deleting group").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));
      
    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
          
  }
  
  /**
   * insert entity into target
   */
  public void appendInsertEntityIntoTarget() {
    this.report.append("<h4>Insert entity into Target</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsEntityInsert()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to insert entity into target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper == null || this.provisioningEntityWrapper.getGrouperProvisioningEntity() == null 
        || this.provisioningEntityWrapper.getGrouperTargetEntity() == null ) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert entity into target since it is not configured to be provisioned\n");
      this.report.append("</pre>\n");
      return;
    }
    if (this.provisioningEntityWrapper != null && this.provisioningEntityWrapper.getTargetProvisioningEntity() != null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert entity into target since it is already there\n");
      this.report.append("</pre>\n");
      return;
    }
    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot insert entity into target since it has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
              
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      this.provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc());

      List<ProvisioningEntity> grouperTargetEntitiesToInsert = GrouperUtil.toList(this.provisioningEntityWrapper.getGrouperTargetEntity());
      
      // add object change entries
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForEntitiesToInsert(grouperTargetEntitiesToInsert);
      
      //lets create these
      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().insertEntities(new TargetDaoInsertEntitiesRequest(grouperTargetEntitiesToInsert));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsInsertEntities(grouperTargetEntitiesToInsert, false);
          
        } catch (RuntimeException e) {
          GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
        }
      }
      if (this.provisioningEntityWrapper.getGrouperTargetEntity().getException() != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Inserting entity into target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningEntityWrapper.getGrouperTargetEntity().getException())) + "\n");
        return;
      }
  
      if (runtimeException != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Inserting entity into target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> No error inserting entity into target\n");
      
      if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
        //retrieve so we have a copy
        TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
            this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToInsert, true));
        
        List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
        
        if (GrouperUtil.length(targetEntities) == 0) {
          this.report.append("<font color='red'><b>Error:</b></font> Cannot find entity from target after inserting!\n");
          return;
        }
        if (GrouperUtil.length(targetEntities) > 1) {
          this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetEntities) + " entities after inserting, should be 1!\n");
          return;
        }
        this.report.append("<font color='green'><b>Success:</b></font> Found entity from target after inserting\n");
        
        updateProvisioningEntityWrapperAfterTargetQuery(targetEntities);
      }
      
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Inserting entity").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));
      
    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
          
  }
  
  public void updateProvisioningEntityWrapperAfterTargetQuery(List<ProvisioningEntity> targetEntities) {
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(targetEntities, false, true, false, false);

    // index
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(targetEntities);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities(targetEntities);

    this.provisioningEntityWrapper.setTargetProvisioningEntity(targetEntities.get(0));
    this.provisioningEntityWrapper.getProvisioningStateEntity().setCreate(false);
    
    GrouperUtil.setClear(this.provisioningEntityWrapper.getGrouperTargetEntity().getInternal_objectChanges());
  }
  
  /**
   * delete entity from target
   */
  public void appendDeleteEntityFromTarget() {
    this.report.append("<h4>Delete entity from Target</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsEntityDelete()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to delete entity from target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot delete entity because there's no specified entity\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper != null && this.provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot delete entity from target since it does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot delete entity from target since it has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
              
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      this.provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc());
      
      List<ProvisioningEntity> grouperTargetEntitiesToDelete = GrouperUtil.toList(this.provisioningEntityWrapper.getGrouperTargetEntity());
      
      //lets delete
      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().deleteEntities(new TargetDaoDeleteEntitiesRequest(grouperTargetEntitiesToDelete));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsDeleteEntities(grouperTargetEntitiesToDelete, false);
          
        } catch (RuntimeException e) {
          GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
        }
      }
      if (this.provisioningEntityWrapper.getGrouperTargetEntity().getException() != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Deleting entity from target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningEntityWrapper.getGrouperTargetEntity().getException())) + "\n");
        return;
      }
  
      if (runtimeException != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Deleting entity from target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> No error deleting entity from target\n");
      
      TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
          this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToDelete, true));
      
      List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());

      if (GrouperUtil.length(targetEntities) > 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetEntities) + " entities in target after deleting, should be 0!\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> Did not find entity in target after deleting\n");
        
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
        // delete the membership sync objects
        this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMembershipDao().membershipDeleteBySyncMemberId(this.provisioningEntityWrapper.getGcGrouperSyncMember().getId(), false);  
      }
      
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Deleting entity").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));
      
    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
          
  }
  
  /**
   * select a group from target
   */
  public void appendSelectGroupFromTarget() {
    this.report.append("<h4>Select group from Target</h4><pre>");
    
    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> No provisioningGroupWrapper means no group to select from target\n");
    } else {
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroup(), false)
          && !GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroups(), false)) {
        this.report.append("<font color='gray'><b>Note:</b></font> Target DAO cannot retrieve specific group(s)\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroups()) {
        this.report.append("<font color='gray'><b>Note:</b></font> Provisioning behavior is to not retrieve specific group(s)\n");
      } else if (this.provisioningGroupWrapper.getGrouperTargetGroup() == null) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Grouper target group is null\n");
      } else {

        try {

          TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest();
          targetDaoRetrieveGroupsRequest.setTargetGroups(GrouperUtil.toList(this.provisioningGroupWrapper.getGrouperTargetGroup()));
          targetDaoRetrieveGroupsRequest.setIncludeAllMembershipsIfApplicable(true);
          TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(
              targetDaoRetrieveGroupsRequest);

          if (targetDaoRetrieveGroupsResponse == null) {
            this.report.append("<font color='red'><b>Error:</b></font> TargetDaoRetrieveGroupResponse is null\n");
          } else if (GrouperUtil.length(targetDaoRetrieveGroupsResponse.getTargetGroups()) == 0) {
            this.report.append("<font color='gray'><b>Note:</b></font> group is not in target\n");
          } else {
            ProvisioningGroup targetGroup = targetDaoRetrieveGroupsResponse.getTargetGroups().get(0);
            this.provisioningGroupWrapper.setTargetProvisioningGroup(targetGroup);
            this.report.append("<font color='gray'><b>Note:</b></font> Target group (unprocessed): ")
              .append(GrouperUtil.xmlEscape(targetGroup.toString())).append(this.getCurrentDuration()).append("\n");
            
            List<ProvisioningGroup> targetGroupsForOne = GrouperUtil.toList(targetGroup);
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(targetGroupsForOne, false, true, false, false);

            this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(
                targetGroupsForOne);
            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups(
                targetGroupsForOne);

            this.report.append("<font color='gray'><b>Note:</b></font> Target group (filtered, attributes manipulated, matchingId calculated):\n  ")
              .append(GrouperUtil.xmlEscape(targetGroup.toString())).append("\n");

            if (GrouperUtil.length(targetGroup.getMatchingIdAttributeNameToValues()) == 0) {
              this.report.append("<font color='red'><b>Error:</b></font> Target group matching id is blank\n");
            }
            
            if (this.provisioningGroupWrapper.getGrouperTargetGroup().getProvisioningGroupWrapper() == null || 
                this.provisioningGroupWrapper.getGrouperTargetGroup().getProvisioningGroupWrapper() != targetGroup.getProvisioningGroupWrapper()) {
              this.report.append("<font color='red'><b>Error:</b></font> Matching id's do not match!\n");
            }
            
          }
          
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting specific group(s)").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));
          
        }
      }
    }
    this.report.append("</pre>\n");

  }
  
  /**
   * select an entity from target
   */
  public void appendSelectEntityFromTarget() {
    this.report.append("<h4>Select entity from Target</h4><pre>");
    
    if (this.provisioningEntityWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> No provisioningEntityWrapper means no entity to select from target\n");
    } else {
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveEntity(), false)
          && !GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveEntities(), false)) {
        this.report.append("<font color='gray'><b>Note:</b></font> Target DAO cannot retrieve specific entities(s)\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntities()) {
        this.report.append("<font color='gray'><b>Note:</b></font> Provisioning behavior is to not retrieve specific entities(s)\n");
      } else if (this.provisioningEntityWrapper.getGrouperTargetEntity() == null) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Grouper target entity is null\n");
      } else {

        try {
          TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest = new TargetDaoRetrieveEntitiesRequest();
          targetDaoRetrieveEntitiesRequest.setTargetEntities(GrouperUtil.toList(this.provisioningEntityWrapper.getGrouperTargetEntity()));
          targetDaoRetrieveEntitiesRequest.setIncludeAllMembershipsIfApplicable(true);
          TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(
              targetDaoRetrieveEntitiesRequest);

          if (targetDaoRetrieveEntitiesResponse == null) {
            this.report.append("<font color='red'><b>Error:</b></font> TargetDaoRetrieveEntityResponse is null\n");
          } else if (GrouperUtil.length(targetDaoRetrieveEntitiesResponse.getTargetEntities()) == 0) {
            
            this.report.append("<font color='gray'><b>Note:</b></font> entity is not in target\n");
          } else {
            ProvisioningEntity targetEntity = targetDaoRetrieveEntitiesResponse.getTargetEntities().get(0);
            this.provisioningEntityWrapper.setTargetProvisioningEntity(targetEntity);
            this.report.append("<font color='gray'><b>Note:</b></font> Target entity (unprocessed): ")
              .append(GrouperUtil.xmlEscape(targetEntity.toString())).append(this.getCurrentDuration()).append("\n");
            
            List<ProvisioningEntity> targetEntitiesForOne = GrouperUtil.toList(targetEntity);
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(
                targetEntitiesForOne, false, true, false, false);

            this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(
                targetEntitiesForOne);
            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities(
                targetEntitiesForOne);

            this.report.append("<font color='gray'><b>Note:</b></font> Target entity (filtered, attributes manipulated, matchingId calculated):\n  ")
              .append(GrouperUtil.xmlEscape(targetEntity.toString())).append("\n");

            if (GrouperUtil.length(targetEntity.getMatchingIdAttributeNameToValues()) == 0) {
              this.report.append("<font color='red'><b>Error:</b></font> Target entity matching id is blank\n");
            }
            
            if (this.provisioningEntityWrapper.getGrouperTargetEntity().getProvisioningEntityWrapper() == null || 
                this.provisioningEntityWrapper.getGrouperTargetEntity().getProvisioningEntityWrapper() != targetEntity.getProvisioningEntityWrapper()) {
              this.report.append("<font color='red'><b>Error:</b></font> Matching id's do not match!\n");
            }

          }
          
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting specific entity").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));
          
        }
      }
    }
    this.report.append("</pre>\n");

  }
  
  public void appendValidation() {
    
    this.report.append("<h4>Validation</h4><pre>");

    {
      List<String> errorsToDisplay = new ArrayList<String>();
      
      Map<String, String> validationErrorsToDisplay = new LinkedHashMap<String, String>();
      
      this.getGrouperProvisioner().getControllerForProvisioningConfiguration().validatePreSave(false, errorsToDisplay, validationErrorsToDisplay);
  
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
      List<ProvisioningValidationIssue> errors = this.getGrouperProvisioner().retrieveGrouperProvisioningConfigurationValidation().validate();
      if (errors.size() > 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Provisioner config validation rule violations: ")
          .append(errors.size()).append("\n");
        for (ProvisioningValidationIssue provisioningValidationIssue : errors) {
          String error = provisioningValidationIssue.getMessage();
          if (provisioningValidationIssue.isRuntimeError()) {
            this.report.append("<font color='red'><b>Fatal error:</b></font>");
          } else {
            this.report.append("<font color='red'><b>Error:</b></font>");
          }
          if (!StringUtils.isBlank(provisioningValidationIssue.getJqueryHandle())) {
            this.report.append(" in config item '" + provisioningValidationIssue.getJqueryHandle() + "': " + GrouperUtil.xmlEscape(error)).append("\n");
          } else {
            this.report.append(GrouperUtil.xmlEscape(error)).append("\n");
          }
        }
      } else {
        this.report.append("<font color='green'><b>Success:</b></font> Provisioner config satisfies validation rules\n");
      }
    }
    
    this.report.append("</pre>\n");
    
  }


  public void appendGeneralInfo() {
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
  public String getCurrentDuration() {
    return " (elapsed: " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - this.started) + ")";
  }
  
  public void appendSelectAllGroups() {
    this.report.append("<h4>All groups</h4>");
    this.report.append("<pre>");
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsGroupsAllSelect()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all groups\n");
    } else {
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllGroups(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all groups\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all groups\n");
      } else {

        try {
            
          this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

          TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveAllGroups(
              new TargetDaoRetrieveAllGroupsRequest(this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()));
          List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveAllGroupsResponse == null ? null : targetDaoRetrieveAllGroupsResponse.getTargetGroups());

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
          this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(targetGroups, false, true, false, false);

          this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(
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
              if (GrouperUtil.length(targetGroup.getMatchingIdAttributeNameToValues()) == 0) {
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
            Set<ProvisioningUpdatableAttributeAndValue> matchingIds = new HashSet<ProvisioningUpdatableAttributeAndValue>();
            Set<Object> firstTen = new HashSet<Object>();
            for (int i=0;i<GrouperUtil.length(targetGroups);i++) {
              ProvisioningGroup targetGroup = targetGroups.get(i);
              for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(targetGroup.getMatchingIdAttributeNameToValues())) {
                if (matchingIds.contains(provisioningUpdatableAttributeAndValue)) {
                  countWithDuplicateMatchingId++;
                  if (firstTen.size() <= 10) {
                    firstTen.add(provisioningUpdatableAttributeAndValue);
                  }
                } else {
                  matchingIds.add(provisioningUpdatableAttributeAndValue);
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
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));
          
        } finally {
          String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
          debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
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
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllEntities(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all entities\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all entities\n");
      } else {
  
        try {
            
          TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveAllEntities(
              new TargetDaoRetrieveAllEntitiesRequest(this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()));
          List<ProvisioningEntity> targetEntities = targetDaoRetrieveAllEntitiesResponse == null ? null : targetDaoRetrieveAllEntitiesResponse.getTargetEntities();
  
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
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(targetEntitiesForOne, false, true, false, false);
            
            this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(
                targetEntitiesForOne);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Entity ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetEntities)).append(" (filtered, attributes manipulated, matchingId calculated):\n  ").append(GrouperUtil.xmlEscape(targetEntity.toString())).append("\n");
            
          }
  
  
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting all entities").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));
          
        }
      }
    }
    
    this.report.append("</pre>");
  }

  public void appendSelectAllMemberships() {
    this.report.append("<h4>All memberships</h4>");
    this.report.append("<pre>");
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
        this.report.append("<font color='gray'><b>Note:</b></font> See groups section as well for memberships\n");
      } else if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
        this.report.append("<font color='gray'><b>Note:</b></font> See entity section as well for memberships\n");
      }
    }
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all memberships\n");
    } else if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      this.report.append("<font color='gray'><b>Note:</b></font> Membership type is: " + this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() + "\n");
    } else {
    
      if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all memberships\n");
      } else if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsWithEntity()
          || this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsWithGroup()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Memberships are retrieved with entities or groups\n");;
      } else {        
        try {
            
          TargetDaoRetrieveAllMembershipsResponse targetDaoRetrieveAllMembershipsResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveAllMemberships(new TargetDaoRetrieveAllMembershipsRequest());
          List<ProvisioningMembership> targetMemberships = targetDaoRetrieveAllMembershipsResponse == null ? null : targetDaoRetrieveAllMembershipsResponse.getTargetMemberships();
  
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
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesMemberships(targetMembershipsForOne, false, true, false, false);

            this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships(
                targetMembershipsForOne);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Membership ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetMemberships)).append(" (filtered, attributes manipulated, matchingId calculated):\n  ").append(GrouperUtil.xmlEscape(targetMembership.toString())).append("\n");
            
          }
  
  
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting all memberships").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));
          
        }
      }
    }
    
    this.report.append("</pre>");
  }

  /**
   * insert group to entity as an entity attribute in target
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void appendInsertEntityAttributesMembershipIntoTarget() {
    this.report.append("<h4>Add group to entity (entityAttribute)</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipInsert()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to add group to entity in target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add group to entity in target because there's no specified group\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add group to entity in target since the group has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
    
    if (this.provisioningEntityWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add group to entity in target because there's no specified entity\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper != null && this.provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add group to entity in target since the entity does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add group to entity in target since the entity has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
     
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      Set<MultiKey> groupUuidMemberUuids = new HashSet<MultiKey>();
      MultiKey groupIdMemberId = new MultiKey(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
      groupUuidMemberUuids.add(groupIdMemberId);
      List<ProvisioningMembership> grouperProvisioningMemberships = this.grouperProvisioner.retrieveGrouperDao().retrieveMemberships(false, null, null, groupUuidMemberUuids);
      
      if (GrouperUtil.length(grouperProvisioningMemberships) == 0) {
        // the lookup above reads the provisionable membership cache (grouper_sql_cache_*), which may
        // not be populated yet on a fresh setup. the group + entity are already resolved and the
        // Select-group step added the entity as a member, so build the membership directly from the
        // wrappers for diagnostics instead of skipping the test.
        ProvisioningMembership directProvisioningMembership = new ProvisioningMembership();
        directProvisioningMembership.setProvisioningGroupId(this.provisioningGroupWrapper.getGroupId());
        directProvisioningMembership.setProvisioningEntityId(this.provisioningEntityWrapper.getMemberId());
        grouperProvisioningMemberships = GrouperUtil.toList(directProvisioningMembership);
        this.report.append("<font color='gray'><b>Note:</b></font> Could not find membership through the provisionable membership cache (it may not be populated yet); built the membership directly for diagnostics\n");
      }
      {
        ProvisioningMembership grouperProvisioningMembership = grouperProvisioningMemberships.get(0);

        GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
        GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
        if (gcGrouperSyncMembership == null) {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership record does not exist in database\n");

        } else {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership: ").append(GrouperUtil.xmlEscape(gcGrouperSyncMembership.toString())).append(this.getCurrentDuration()).append("\n");
        }

        ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        grouperProvisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembershipWrapper.setGrouperProvisioningMembership(grouperProvisioningMembership);
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);

        this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLink(GrouperUtil.toSet(provisioningEntityWrapper), true);

        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().put(groupIdMemberId, provisioningMembershipWrapper);

        this.grouperProvisioner.retrieveGrouperProvisioningSyncIntegration().fullSyncMemberships();

        grouperProvisioningMembership.setProvisioningGroup(this.provisioningGroupWrapper.getGrouperProvisioningGroup());
        grouperProvisioningMembership.setProvisioningEntity(this.provisioningEntityWrapper.getGrouperProvisioningEntity());
        this.grouperProvisioner.retrieveGrouperProvisioningData().addAndIndexEntityWrapper(this.provisioningEntityWrapper);

        this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetMemberships(GrouperUtil.toList(grouperProvisioningMembership), false);

        List<ProvisioningEntity> grouperTargetEntitiesToUpdate = GrouperUtil.toList(this.provisioningEntityWrapper.getGrouperTargetEntity());
        String membershipAttributeName = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
        
        Collection<String> values = (Collection<String>)(Object)this.provisioningEntityWrapper.getGrouperTargetEntity().retrieveAttributeValueSet(membershipAttributeName);
        String value = values == null || values.size() == 0 ? null : values.iterator().next();
                
        if (values.size() == 0) {
          this.report.append("<font color='red'><b>Error:</b></font> No values to add after translation\n");
        } else if (values.size() > 1) {
          this.report.append("<font color='red'><b>Error:</b></font> Translation resulted in multiple values: " + values + "\n");
        } else if (this.provisioningEntityWrapper.getTargetProvisioningEntity().retrieveProvisioningAttribute(membershipAttributeName) != null && ((Collection)this.provisioningEntityWrapper.getTargetProvisioningEntity().retrieveProvisioningAttribute(membershipAttributeName).getValue()).contains(value)) {
          this.report.append("<font color='orange'><b>Warning:</b></font> Target already contains value: " + value + "\n");
        } else {
          grouperTargetEntitiesToUpdate.get(0).addInternal_objectChange(
              new ProvisioningObjectChange(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), 
                  ProvisioningObjectChangeAction.insert, null, value)
              );
    
          for (ProvisioningObjectChange provisioningObjectChange : grouperTargetEntitiesToUpdate.get(0).getInternal_objectChanges()) {
            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningObjectChange: attributeName=" + provisioningObjectChange.getAttributeName() + ", action=" + provisioningObjectChange.getProvisioningObjectChangeAction() + ", oldValue=" + provisioningObjectChange.getOldValue() + ", newValue=" + provisioningObjectChange.getNewValue() + "\n");
          }
          
          RuntimeException runtimeException = null;
          try {
            this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().updateEntities(new TargetDaoUpdateEntitiesRequest(grouperTargetEntitiesToUpdate));
          } catch (RuntimeException re) {
            runtimeException = re;
          } finally {
            try {
              this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsUpdateEntitiesFull(grouperTargetEntitiesToUpdate, true);
  
            } catch (RuntimeException e) {
              GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
            }
          }
  
          if (this.provisioningEntityWrapper.getGrouperTargetEntity().getException() != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Adding group to entity in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningEntityWrapper.getGrouperTargetEntity().getException())) + "\n");
            return;
          }
  
          if (runtimeException != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Adding group to entity in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
            return;
          }
          this.report.append("<font color='green'><b>Success:</b></font> No error adding group to entity in target\n");
          
          // check target
          TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToUpdate, true));
          
          List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
      
          if (GrouperUtil.length(targetEntities) == 0) {
            this.report.append("<font color='red'><b>Error:</b></font> Cannot find entity from target after inserting membership!\n");
          } else if (GrouperUtil.length(targetEntities) > 1) {
            this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetEntities) + " entitites after inserting membership, should be 1!\n");
          } else {
            if (targetEntities.get(0).retrieveProvisioningAttribute(membershipAttributeName) == null) {
              this.report.append("<font color='red'><b>Error:</b></font> Did not find membership in target after inserting: " + value + "\n");
            } else if (targetEntities.get(0).retrieveProvisioningAttribute(membershipAttributeName) != null && ((Collection)targetEntities.get(0).retrieveProvisioningAttribute(membershipAttributeName).getValue()).contains(value)) {
              this.report.append("<font color='green'><b>Success:</b></font> Found membership in target after inserting: " + value + "\n");
            } else {
              this.report.append("<font color='red'><b>Error:</b></font> Did not find membership in target after inserting: " + value + "\n");
            }
          }
          
          updateProvisioningEntityWrapperAfterTargetQuery(targetEntities);
          
          this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
        }
      }
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Adding group to entity").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));

    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
  }
  
  /**
   * remove group from entity as an entity attribute in target
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void appendDeleteEntityAttributesMembershipFromTarget() {
    this.report.append("<h4>Remove group from entity (entityAttribute)</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipDelete()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to remove group from entity in target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot remove group from entity in target because there's no specified group\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot remove group from entity in target since the group has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
    
    if (this.provisioningEntityWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot remove group from entity in target because there's no specified entity\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper != null && this.provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot remove group from entity in target since the entity does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot remove group from entity in target since the entity has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
     
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      Set<MultiKey> groupUuidMemberUuids = new HashSet<MultiKey>();
      MultiKey groupIdMemberId = new MultiKey(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
      groupUuidMemberUuids.add(groupIdMemberId);
      List<ProvisioningMembership> grouperProvisioningMemberships = this.grouperProvisioner.retrieveGrouperDao().retrieveMemberships(false, null, null, groupUuidMemberUuids);
      
      if (GrouperUtil.length(grouperProvisioningMemberships) == 0) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Cannot find ProvisioningMembership object.  Note that the entity must be a member of the group in Grouper.\n");
      } else {
        ProvisioningMembership grouperProvisioningMembership = grouperProvisioningMemberships.get(0);

        GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
        GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
        if (gcGrouperSyncMembership == null) {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership record does not exist in database\n");

        } else {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership: ").append(GrouperUtil.xmlEscape(gcGrouperSyncMembership.toString())).append(this.getCurrentDuration()).append("\n");
        }

        ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        grouperProvisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembershipWrapper.setGrouperProvisioningMembership(grouperProvisioningMembership);
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);

        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().put(groupIdMemberId, provisioningMembershipWrapper);

        this.grouperProvisioner.retrieveGrouperProvisioningSyncIntegration().fullSyncMemberships();

        grouperProvisioningMembership.setProvisioningGroup(this.provisioningGroupWrapper.getGrouperProvisioningGroup());
        grouperProvisioningMembership.setProvisioningEntity(this.provisioningEntityWrapper.getGrouperProvisioningEntity());
        this.grouperProvisioner.retrieveGrouperProvisioningData().addAndIndexEntityWrapper(this.provisioningEntityWrapper);

        this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetMemberships(GrouperUtil.toList(grouperProvisioningMembership), false);

        List<ProvisioningEntity> grouperTargetEntitiesToUpdate = GrouperUtil.toList(this.provisioningEntityWrapper.getGrouperTargetEntity());
        String membershipAttributeName = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
        
        ProvisioningAttribute provisioningAttribute = this.provisioningEntityWrapper.getGrouperTargetEntity().retrieveProvisioningAttribute(membershipAttributeName);
        Collection<String> values = provisioningAttribute == null ? null : (Collection)provisioningAttribute.getValue();
        String value = values == null || values.size() == 0 ? null : values.iterator().next();
                
        if (values.size() == 0) {
          this.report.append("<font color='red'><b>Error:</b></font> No values to remove after translation\n");
        } else if (values.size() > 1) {
          this.report.append("<font color='red'><b>Error:</b></font> Translation resulted in multiple values: " + values + "\n");
        } else if (this.provisioningEntityWrapper.getTargetProvisioningEntity().retrieveProvisioningAttribute(membershipAttributeName) == null || !((Collection)this.provisioningEntityWrapper.getTargetProvisioningEntity().retrieveProvisioningAttribute(membershipAttributeName).getValue()).contains(value)) {
          this.report.append("<font color='orange'><b>Warning:</b></font> Target does not contain value: " + value + "\n");
        } else {
          grouperTargetEntitiesToUpdate.get(0).addInternal_objectChange(
              new ProvisioningObjectChange(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), 
                  ProvisioningObjectChangeAction.delete, value, null)
              );
    
          for (ProvisioningObjectChange provisioningObjectChange : grouperTargetEntitiesToUpdate.get(0).getInternal_objectChanges()) {
            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningObjectChange: attributeName=" + provisioningObjectChange.getAttributeName() + ", action=" + provisioningObjectChange.getProvisioningObjectChangeAction() + ", oldValue=" + provisioningObjectChange.getOldValue() + ", newValue=" + provisioningObjectChange.getNewValue() + "\n");
          }
          
          RuntimeException runtimeException = null;
          try {
            this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().updateEntities(new TargetDaoUpdateEntitiesRequest(grouperTargetEntitiesToUpdate));
          } catch (RuntimeException re) {
            runtimeException = re;
          } finally {
            try {
              this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsUpdateEntitiesFull(grouperTargetEntitiesToUpdate, true);
  
            } catch (RuntimeException e) {
              GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
            }
          }
  
          if (this.provisioningEntityWrapper.getGrouperTargetEntity().getException() != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Removing group from entity in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningEntityWrapper.getGrouperTargetEntity().getException())) + "\n");
            return;
          }
  
          if (runtimeException != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Removing group from entity in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
            return;
          }
          this.report.append("<font color='green'><b>Success:</b></font> No error removing group from entity in target\n");
          
          // check target
          TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToUpdate, true));
          
          List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
      
          if (GrouperUtil.length(targetEntities) == 0) {
            this.report.append("<font color='red'><b>Error:</b></font> Cannot find entity from target after removing membership!\n");
          } else if (GrouperUtil.length(targetEntities) > 1) {
            this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetEntities) + " entities after removing membership, should be 1!\n");
          } else {
            if (targetEntities.get(0).retrieveProvisioningAttribute(membershipAttributeName) == null) {
              this.report.append("<font color='green'><b>Success:</b></font> Did not find membership in target after removing: " + value + "\n");
            } else if (targetEntities.get(0).retrieveProvisioningAttribute(membershipAttributeName) != null && ((Collection)targetEntities.get(0).retrieveProvisioningAttribute(membershipAttributeName).getValue()).contains(value)) {
              this.report.append("<font color='red'><b>Error:</b></font> Found membership in target after removing: " + value + "\n");
            } else {
              this.report.append("<font color='green'><b>Success:</b></font> Did not find membership in target after removing: " + value + "\n");
            }
          }
          
          updateProvisioningEntityWrapperAfterTargetQuery(targetEntities);
          
          this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
        }
      }
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Removing group from entity").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));

    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
  }
  
  /**
   * init the config of diagnostics from provisioner configuration
   */
  public void initFromConfiguration() {
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isDiagnosticsGroupsAllSelect());
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntitiesAllSelect(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isDiagnosticsEntitiesAllSelect());
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipsAllSelect(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isDiagnosticsMembershipsAllSelect());
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getDiagnosticsGroupName());

    // SCIM only: seed the strategy dropdowns with the currently configured values
    if (this.isScim()) {
      GrouperScim2ProvisionerConfiguration scimConfiguration = (GrouperScim2ProvisionerConfiguration) this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
      this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsScimEmailFilterStrategy(scimConfiguration.getScimEmailFilterStrategy());
      this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsScimNamePatchStrategy(scimConfiguration.getScimNamePatchStrategy());
      this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsScimEmailPatchStrategy(scimConfiguration.getScimEmailPatchStrategy());
    }

  }

  /**
   * SCIM only: is this a SCIM2 provisioner (used to gate SCIM-specific diagnostics UI/logic)
   * @return true if SCIM2 provisioner
   */
  public boolean isScim() {
    return this.grouperProvisioner != null
        && this.grouperProvisioner.retrieveGrouperProvisioningConfiguration() instanceof GrouperScim2ProvisionerConfiguration;
  }

  /**
   * SCIM only: verify that each configured search/match attribute actually resolves the object in
   * the target.  for the diagnostic entity and group (already retrieved from the target), look each
   * one up by each configured search attribute (id, userName/displayName, email, externalId, ...)
   * and report whether the target's filter returns the same object.  this confirms, e.g., that the
   * target really supports matching entities/groups by externalId before the provisioner relies on
   * it.  best-effort: never aborts the rest of diagnostics.
   */
  public void appendScimSearchAttributeVerification() {

    if (!this.isScim()) {
      return;
    }

    this.report.append("<h4>Search/match attribute verification (SCIM filter)</h4><pre>");

    try {
      GrouperScim2ProvisionerConfiguration scimConfiguration =
          (GrouperScim2ProvisionerConfiguration) this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
      ScimSettings scimSettings = new ScimSettings();
      scimSettings.loadFromScimProvisionerConfiguration(scimConfiguration);

      // ----- entity search attributes -----
      ProvisioningEntity targetEntity = this.provisioningEntityWrapper == null ? null
          : this.provisioningEntityWrapper.getTargetProvisioningEntity();
      if (targetEntity == null) {
        this.report.append("<font color='gray'><b>Note:</b></font> No diagnostic entity in the target to verify entity search attributes\n");
      } else {
        String targetEntityId = targetEntity.retrieveAttributeValueString("id");
        // the attributes the provisioner matches entities on: id and userName always, plus the
        // configured entity search attributes (e.g. emailValue, externalId)
        Set<String> entitySearchAttributeNames = new LinkedHashSet<String>();
        entitySearchAttributeNames.add("id");
        entitySearchAttributeNames.add("userName");
        for (GrouperProvisioningConfigurationAttribute searchAttribute : GrouperUtil.nonNull(scimConfiguration.getEntitySearchAttributes())) {
          entitySearchAttributeNames.add(searchAttribute.getName());
        }
        for (String attributeName : entitySearchAttributeNames) {
          this.verifyScimEntitySearchAttribute(scimConfiguration, scimSettings, targetEntity, targetEntityId, attributeName);
        }
      }

      // ----- group search attributes -----
      ProvisioningGroup targetGroup = this.provisioningGroupWrapper == null ? null
          : this.provisioningGroupWrapper.getTargetProvisioningGroup();
      if (targetGroup == null) {
        this.report.append("<font color='gray'><b>Note:</b></font> No diagnostic group in the target to verify group search attributes\n");
      } else {
        String targetGroupId = targetGroup.retrieveAttributeValueString("id");
        // the attributes the provisioner matches groups on: id and displayName always, plus the
        // configured group search attributes (e.g. externalId)
        Set<String> groupSearchAttributeNames = new LinkedHashSet<String>();
        groupSearchAttributeNames.add("id");
        groupSearchAttributeNames.add("displayName");
        for (GrouperProvisioningConfigurationAttribute searchAttribute : GrouperUtil.nonNull(scimConfiguration.getGroupSearchAttributes())) {
          groupSearchAttributeNames.add(searchAttribute.getName());
        }
        for (String attributeName : groupSearchAttributeNames) {
          this.verifyScimGroupSearchAttribute(scimConfiguration, scimSettings, targetGroup, targetGroupId, attributeName);
        }
      }

    } catch (Exception e) {
      LOG.error("error in scim search attribute verification diagnostics", e);
      this.report.append("<font color='gray'><b>Note:</b></font> Search attribute verification could not complete (")
        .append(GrouperUtil.xmlEscape(StringUtils.abbreviate(e.getMessage(), 200))).append("); skipping\n");
    } finally {
      this.report.append("</pre>\n");
    }
  }

  /**
   * verify one configured entity search attribute resolves the diagnostic entity in the target.
   * see {@link #appendScimSearchAttributeVerification()}.
   */
  private void verifyScimEntitySearchAttribute(GrouperScim2ProvisionerConfiguration scimConfiguration,
      ScimSettings scimSettings, ProvisioningEntity targetEntity, String targetEntityId, String attributeName) {

    String value = targetEntity.retrieveAttributeValueString(attributeName);
    if (StringUtils.isBlank(value)) {
      this.report.append("<font color='gray'><b>Skip:</b></font> entity attribute '").append(GrouperUtil.xmlEscape(attributeName))
        .append("' has no value on the target entity to look up by\n");
      return;
    }

    // emailValue is filtered via the configured email filter strategy, which retrieveScimUser keys off "email"
    String scimField = StringUtils.equals("emailValue", attributeName) ? "email" : attributeName;

    try {
      GrouperScim2User found = GrouperScim2ApiCommands.retrieveScimUser(
          scimConfiguration.getBearerTokenExternalSystemConfigId(), scimField, value,
          new GrouperScim2MembershipCache(), scimSettings);
      this.appendScimSearchResult("entity", attributeName, found == null ? null : found.getId(), targetEntityId);
    } catch (RuntimeException re) {
      this.report.append("<font color='orange'><b>Warning:</b></font> entity lookup by '").append(GrouperUtil.xmlEscape(attributeName))
        .append("' threw an error (").append(GrouperUtil.xmlEscape(StringUtils.abbreviate(re.getMessage(), 200))).append(")\n");
    }
  }

  /**
   * verify one configured group search attribute resolves the diagnostic group in the target.
   * see {@link #appendScimSearchAttributeVerification()}.
   */
  private void verifyScimGroupSearchAttribute(GrouperScim2ProvisionerConfiguration scimConfiguration,
      ScimSettings scimSettings, ProvisioningGroup targetGroup, String targetGroupId, String attributeName) {

    String value = targetGroup.retrieveAttributeValueString(attributeName);
    if (StringUtils.isBlank(value)) {
      this.report.append("<font color='gray'><b>Skip:</b></font> group attribute '").append(GrouperUtil.xmlEscape(attributeName))
        .append("' has no value on the target group to look up by\n");
      return;
    }

    try {
      GrouperScim2Group found = GrouperScim2ApiCommands.retrieveScimGroup(
          scimConfiguration.getBearerTokenExternalSystemConfigId(), attributeName, value,
          new GrouperScim2MembershipCache(), scimSettings);
      this.appendScimSearchResult("group", attributeName, found == null ? null : found.getId(), targetGroupId);
    } catch (RuntimeException re) {
      this.report.append("<font color='orange'><b>Warning:</b></font> group lookup by '").append(GrouperUtil.xmlEscape(attributeName))
        .append("' threw an error (").append(GrouperUtil.xmlEscape(StringUtils.abbreviate(re.getMessage(), 200))).append(")\n");
    }
  }

  /**
   * report the outcome of a single search-attribute lookup: the filter resolved the same object
   * (works), returned nothing (does not resolve), or returned a different object (not unique).
   */
  private void appendScimSearchResult(String objectType, String attributeName, String foundId, String expectedId) {
    if (foundId == null) {
      this.report.append("<font color='orange'><b>Does not resolve:</b></font> ").append(GrouperUtil.xmlEscape(objectType))
        .append(" attribute '").append(GrouperUtil.xmlEscape(attributeName))
        .append("' -- the target returned nothing for this filter (matching on it will not work)\n");
    } else if (StringUtils.equals(foundId, expectedId)) {
      this.report.append("<font color='green'><b>Works:</b></font> ").append(GrouperUtil.xmlEscape(objectType))
        .append(" attribute '").append(GrouperUtil.xmlEscape(attributeName)).append("' resolves the ").append(GrouperUtil.xmlEscape(objectType)).append("\n");
    } else {
      this.report.append("<font color='orange'><b>Warning:</b></font> ").append(GrouperUtil.xmlEscape(objectType))
        .append(" attribute '").append(GrouperUtil.xmlEscape(attributeName))
        .append("' resolved a DIFFERENT ").append(GrouperUtil.xmlEscape(objectType)).append(" (id '")
        .append(GrouperUtil.xmlEscape(foundId)).append("' != '").append(GrouperUtil.xmlEscape(expectedId)).append("') -- not unique?\n");
    }
  }

  /**
   * SCIM only: fetch the target's /Schemas and cross-check the provisioner's configured target
   * attributes against it -- flagging attributes mapped for write that the target says are read-only
   * (writes would be silently ignored, e.g. a derived email), attributes that are immutable but in
   * the update set, and required schema attributes that are not mapped.  degrades to a note if the
   * target does not expose /Schemas.
   */
  public void appendScimSchemaCrossCheck() {

    if (!this.isScim()) {
      return;
    }

    this.report.append("<h4>Target schema cross-check (SCIM /Schemas)</h4><pre>");

    // the whole cross-check is best-effort and must never abort the rest of diagnostics: any failure
    // (target does not expose /Schemas, unexpected payload, bug in the cross-check) is absorbed here
    // into a note, and the closing </pre> is always written in the finally
    try {

      GrouperScim2ProvisionerConfiguration scimConfiguration =
          (GrouperScim2ProvisionerConfiguration) this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();

      ScimSettings scimSettings = new ScimSettings();
      scimSettings.loadFromScimProvisionerConfiguration(scimConfiguration);
      List<GrouperScim2SchemaAttribute> schemaAttributes = GrouperScim2ApiCommands.retrieveScimSchemas(
          scimConfiguration.getBearerTokenExternalSystemConfigId(), scimSettings);

      if (GrouperUtil.length(schemaAttributes) == 0) {
        this.report.append("<font color='gray'><b>Note:</b></font> The target did not return any /Schemas attributes; skipping schema cross-check\n");
        return;
      }

      // provisioner attribute name -> the schema (flattened) attribute names it may map to, in
      // preference order.  most attributes match by name; the complex/derived ones need explicit
      // candidates (e.g. givenName lives under name.givenName; an email may be emails.value or email)
      Map<String, String[]> entityAliases = new HashMap<String, String[]>();
      entityAliases.put("givenName", new String[] {"name.givenName", "givenName"});
      entityAliases.put("familyName", new String[] {"name.familyName", "familyName"});
      entityAliases.put("formattedName", new String[] {"name.formatted", "name.formattedName", "formattedName"});
      entityAliases.put("emailValue", new String[] {"emails.value", "email"});
      entityAliases.put("emailValue2", new String[] {"emails.value", "email"});

      Map<String, String[]> groupAliases = new HashMap<String, String[]>();

      this.appendScimSchemaCrossCheckForResource("User",
          scimConfiguration.getTargetEntityAttributeNameToConfig(), schemaAttributes, entityAliases);
      this.appendScimSchemaCrossCheckForResource("Group",
          scimConfiguration.getTargetGroupAttributeNameToConfig(), schemaAttributes, groupAliases);

    } catch (Exception e) {
      LOG.error("error in scim schema cross-check diagnostics", e);
      this.report.append("<font color='gray'><b>Note:</b></font> Schema cross-check could not complete (")
        .append(GrouperUtil.xmlEscape(StringUtils.abbreviate(e.getMessage(), 200)))
        .append("); skipping\n");
    } finally {
      this.report.append("</pre>\n");
    }
  }

  /**
   * cross-check the configured target attributes for one SCIM resource (User or Group) against the
   * target schema.  see {@link #appendScimSchemaCrossCheck()}.
   * @param resourceName SCIM resource name, e.g. "User" or "Group"
   * @param targetAttributeNameToConfig configured target attributes for that resource
   * @param schemaAttributes all flattened schema attributes retrieved from the target
   * @param aliases provisioner-attribute-name -> candidate schema-attribute-names
   */
  private void appendScimSchemaCrossCheckForResource(String resourceName,
      Map<String, GrouperProvisioningConfigurationAttribute> targetAttributeNameToConfig,
      List<GrouperScim2SchemaAttribute> schemaAttributes, Map<String, String[]> aliases) {

    // schema attributes for this resource, keyed by flattened name
    Map<String, GrouperScim2SchemaAttribute> schemaByName = new HashMap<String, GrouperScim2SchemaAttribute>();
    for (GrouperScim2SchemaAttribute schemaAttribute : schemaAttributes) {
      if (StringUtils.equalsIgnoreCase(resourceName, schemaAttribute.getResourceName())) {
        schemaByName.put(schemaAttribute.getName(), schemaAttribute);
      }
    }

    if (schemaByName.size() == 0) {
      this.report.append("<font color='gray'><b>Note:</b></font> No '").append(GrouperUtil.xmlEscape(resourceName))
        .append("' resource in the target schema; skipping ").append(GrouperUtil.xmlEscape(resourceName)).append(" cross-check\n");
      return;
    }

    // track which schema attributes are mapped, so we can spot required-but-unmapped ones
    Set<String> mappedSchemaNames = new HashSet<String>();
    boolean anyIssue = false;

    for (GrouperProvisioningConfigurationAttribute targetAttribute : GrouperUtil.nonNull(targetAttributeNameToConfig).values()) {
      String targetAttributeName = targetAttribute.getName();

      // id is always target-assigned / read-only by nature; never flag it
      if (StringUtils.equals("id", targetAttributeName)) {
        continue;
      }

      // resolve the schema attribute: try each alias candidate, then the name itself
      String[] candidates = aliases.containsKey(targetAttributeName)
          ? aliases.get(targetAttributeName) : new String[] {targetAttributeName};
      GrouperScim2SchemaAttribute schemaAttribute = null;
      String matchedSchemaName = null;
      for (String candidate : candidates) {
        if (schemaByName.containsKey(candidate)) {
          schemaAttribute = schemaByName.get(candidate);
          matchedSchemaName = candidate;
          break;
        }
      }

      if (schemaAttribute == null) {
        this.report.append("<font color='gray'><b>Note:</b></font> ").append(GrouperUtil.xmlEscape(resourceName))
          .append(" attribute '").append(GrouperUtil.xmlEscape(targetAttributeName))
          .append("' has no match in the target schema (may be an extension or custom attribute)\n");
        continue;
      }

      mappedSchemaNames.add(matchedSchemaName);
      if (matchedSchemaName.contains(".")) {
        // a mapped sub-attribute also satisfies its complex parent (e.g. name via name.givenName)
        mappedSchemaNames.add(StringUtils.substringBefore(matchedSchemaName, "."));
      }

      boolean mappedForWrite = targetAttribute.isInsert() || targetAttribute.isUpdate();
      String mutability = schemaAttribute.getMutability();

      if (StringUtils.equalsIgnoreCase("readOnly", mutability) && mappedForWrite) {
        anyIssue = true;
        this.report.append("<font color='red'><b>Action needed:</b></font> ").append(GrouperUtil.xmlEscape(resourceName))
          .append(" attribute '").append(GrouperUtil.xmlEscape(targetAttributeName))
          .append("' is read-only in the target schema but is mapped for write; the target will silently ignore writes to it (unmap it)\n");
      } else if (StringUtils.equalsIgnoreCase("immutable", mutability) && targetAttribute.isUpdate()) {
        anyIssue = true;
        this.report.append("<font color='orange'><b>Warning:</b></font> ").append(GrouperUtil.xmlEscape(resourceName))
          .append(" attribute '").append(GrouperUtil.xmlEscape(targetAttributeName))
          .append("' is immutable in the target schema; it can be set on create but updates will be rejected\n");
      }
    }

    // required schema attributes that are not mapped
    for (GrouperScim2SchemaAttribute schemaAttribute : schemaAttributes) {
      if (!StringUtils.equalsIgnoreCase(resourceName, schemaAttribute.getResourceName()) || !schemaAttribute.isRequired()) {
        continue;
      }
      String schemaName = schemaAttribute.getName();
      if (mappedSchemaNames.contains(schemaName)) {
        continue;
      }
      // a required sub-attribute is covered if its complex parent is mapped
      if (schemaName.contains(".") && mappedSchemaNames.contains(StringUtils.substringBefore(schemaName, "."))) {
        continue;
      }
      anyIssue = true;
      this.report.append("<font color='orange'><b>Warning:</b></font> ").append(GrouperUtil.xmlEscape(resourceName))
        .append(" schema requires attribute '").append(GrouperUtil.xmlEscape(schemaName))
        .append("' but it is not mapped; creates may be rejected\n");
    }

    if (!anyIssue) {
      this.report.append("<font color='green'><b>Success:</b></font> ").append(GrouperUtil.xmlEscape(resourceName))
        .append(": all mapped attributes are consistent with the target schema\n");
    }
  }

  /**
   * SCIM only: discover which SCIM strategies the target accepts by trying each one (name patch,
   * email patch, and -- when applicable -- email filter), then compare the working strategies to
   * what is configured on the provisioner and report the result either way. This does not persist
   * any configuration; the in-memory strategy values are restored at the end of the run.
   */
  public void appendUpdateEntityIntoTarget() {

    if (!this.isScim()) {
      return;
    }

    this.report.append("<h4>Update entity in Target (SCIM PATCH) - strategy discovery</h4><pre>");

    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsEntityUpdate()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to update entity in target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper == null || this.provisioningEntityWrapper.getGrouperTargetEntity() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot update entity in target since it is not configured to be provisioned\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot update entity in target since it does not exist there (insert it first)\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot update entity in target since it has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }

    GrouperProvisioningDiagnosticsSettings settings = this.getGrouperProvisioningDiagnosticsSettings();

    String newGivenName = settings.getDiagnosticsScimGivenName();
    String newEmailValue = settings.getDiagnosticsScimEmailValue();

    if (StringUtils.isBlank(newGivenName) && StringUtils.isBlank(newEmailValue)) {
      this.report.append("<font color='gray'><b>Note:</b></font> No new given name or email value was specified, so there is nothing to test\n");
      this.report.append("</pre>\n");
      return;
    }

    GrouperScim2ProvisionerConfiguration scimConfiguration = (GrouperScim2ProvisionerConfiguration) this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();

    // remember the configured strategies: we mutate the in-memory config to try each candidate,
    // restore them in the finally block, and compare them against the strategies that work
    String configuredNamePatch = scimConfiguration.getScimNamePatchStrategy();
    String configuredEmailPatch = scimConfiguration.getScimEmailPatchStrategy();
    String configuredEmailFilter = scimConfiguration.getScimEmailFilterStrategy();

    ProvisioningEntity grouperTargetEntity = this.provisioningEntityWrapper.getGrouperTargetEntity();
    ProvisioningEntity targetProvisioningEntity = this.provisioningEntityWrapper.getTargetProvisioningEntity();

    // The SCIM create POST does not send the id (the server assigns its own). The server-assigned
    // id was picked up via the retrieve-after-insert (or select-from-target) and lives on
    // targetProvisioningEntity. The grouperTargetEntity still carries the stale Grouper-translated
    // id, so copy the real target id over before the PATCH; otherwise the PATCH URL points at a
    // non-existent resource and the target returns 404.
    String targetEntityId = targetProvisioningEntity == null ? null : targetProvisioningEntity.retrieveAttributeValueString("id");
    if (!StringUtils.isBlank(targetEntityId)) {
      String staleId = grouperTargetEntity.retrieveAttributeValueString("id");
      if (!StringUtils.equals(staleId, targetEntityId)) {
        this.report.append("<font color='gray'><b>Note:</b></font> Using server-assigned target id '")
          .append(GrouperUtil.xmlEscape(targetEntityId)).append("' for PATCH (was '")
          .append(GrouperUtil.xmlEscape(staleId)).append("')\n");
      }
      grouperTargetEntity.assignAttributeValue("id", targetEntityId);
    }

    // strategy discovery deliberately tries strategies the target may reject; those expected
    // failures stamp an error code (GcGrouperSyncErrorCode.ERR) onto the entity wrapper and the
    // gcGrouperSyncMember via processResultsUpdateEntitiesFull -> assignEntityError. that leftover
    // error would block later diagnostics steps (add entity to group, add membership) and pollute
    // the sync member's error fields. snapshot the pre-discovery error state and restore it in the
    // finally block so discovery is side-effect free.
    GcGrouperSyncMember gcGrouperSyncMemberForErrorRestore = this.provisioningEntityWrapper.getGcGrouperSyncMember();
    GcGrouperSyncErrorCode preDiscoveryWrapperErrorCode = this.provisioningEntityWrapper.getErrorCode();
    GcGrouperSyncErrorCode preDiscoveryMemberErrorCode = gcGrouperSyncMemberForErrorRestore == null ? null : gcGrouperSyncMemberForErrorRestore.getErrorCode();
    String preDiscoveryMemberErrorMessage = gcGrouperSyncMemberForErrorRestore == null ? null : gcGrouperSyncMemberForErrorRestore.getErrorMessage();
    Timestamp preDiscoveryMemberErrorTimestamp = gcGrouperSyncMemberForErrorRestore == null ? null : gcGrouperSyncMemberForErrorRestore.getErrorTimestamp();

    // remember the original target values so the test values can be reverted at the end -- we do
    // not want diagnostics to leave the throwaway test given name / email behind in the target
    String originalGivenName = targetProvisioningEntity == null ? null : targetProvisioningEntity.retrieveAttributeValueString("givenName");
    String originalEmailValue = targetProvisioningEntity == null ? null : targetProvisioningEntity.retrieveAttributeValueString("emailValue");

    List<String> workingNamePatchStrategies = null;
    List<String> workingEmailPatchStrategies = null;

    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStart();

      // name patch strategy discovery (uses the new given name)
      if (!StringUtils.isBlank(newGivenName)) {
        workingNamePatchStrategies = this.appendScimPatchStrategyDiscovery(scimConfiguration, grouperTargetEntity, targetProvisioningEntity,
            "namePatch", "Name patch strategy", "givenName", newGivenName,
            GrouperUtil.toList("nonqualified", "qualified", "nested"), configuredNamePatch);
      }

      // email patch strategy discovery (uses the new email value)
      if (!StringUtils.isBlank(newEmailValue)) {
        workingEmailPatchStrategies = this.appendScimPatchStrategyDiscovery(scimConfiguration, grouperTargetEntity, targetProvisioningEntity,
            "emailPatch", "Email patch strategy", "emailValue", newEmailValue,
            GrouperUtil.toList("pathEmails", "noPath", "pathEmailsQualified"), configuredEmailPatch);
      }

      // email filter strategy discovery (lookup by email) -- only meaningful when an email value
      // is available; if no strategy works the target does not support email filtering (e.g. GitHub).
      // this must run before the email revert below since it relies on the new email being in the target.
      if (!StringUtils.isBlank(newEmailValue)) {
        this.appendScimEmailFilterStrategyDiscovery(scimConfiguration, newEmailValue,
            GrouperUtil.toList("email", "emails.value", "emails[value]", "emails[typeWork and value]"), configuredEmailFilter);
      }

      // revert the test values back to the original values in the target so diagnostics is
      // side-effect free. only possible (and only necessary) when a strategy actually worked.
      if (!StringUtils.isBlank(newGivenName) && GrouperUtil.length(workingNamePatchStrategies) > 0) {
        this.revertScimAttribute(scimConfiguration, grouperTargetEntity, "namePatch", "givenName",
            newGivenName, originalGivenName, workingNamePatchStrategies.get(workingNamePatchStrategies.size() - 1));
      }
      if (!StringUtils.isBlank(newEmailValue) && GrouperUtil.length(workingEmailPatchStrategies) > 0) {
        this.revertScimAttribute(scimConfiguration, grouperTargetEntity, "emailPatch", "emailValue",
            newEmailValue, originalEmailValue, workingEmailPatchStrategies.get(workingEmailPatchStrategies.size() - 1));
      }

    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> SCIM strategy discovery").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getStackTrace(re)));

    } finally {
      // restore the configured strategies so later diagnostics steps and the cached config are unaffected
      scimConfiguration.setScimNamePatchStrategy(configuredNamePatch);
      scimConfiguration.setScimEmailPatchStrategy(configuredEmailPatch);
      scimConfiguration.setScimEmailFilterStrategy(configuredEmailFilter);

      // clear any leftover entity state from the last attempt
      GrouperUtil.setClear(grouperTargetEntity.getInternal_objectChanges());
      grouperTargetEntity.setException(null);

      // restore the pre-discovery error state so the expected probe failures don't block later
      // diagnostics steps (add entity to group, add membership) or pollute the sync member
      this.provisioningEntityWrapper.setErrorCode(preDiscoveryWrapperErrorCode);
      if (gcGrouperSyncMemberForErrorRestore != null) {
        gcGrouperSyncMemberForErrorRestore.setErrorCode(preDiscoveryMemberErrorCode);
        gcGrouperSyncMemberForErrorRestore.setErrorMessage(preDiscoveryMemberErrorMessage);
        gcGrouperSyncMemberForErrorRestore.setErrorTimestamp(preDiscoveryMemberErrorTimestamp);
      }

      String debugInfo = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "No debug info implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
  }

  /**
   * SCIM only: apply a candidate strategy for the given dimension onto the in-memory configuration.
   * @param scimConfiguration scim configuration
   * @param dimension one of "namePatch" or "emailPatch"
   * @param strategy candidate strategy value
   */
  private void applyScimStrategy(GrouperScim2ProvisionerConfiguration scimConfiguration, String dimension, String strategy) {
    if (StringUtils.equals(dimension, "namePatch")) {
      scimConfiguration.setScimNamePatchStrategy(strategy);
    } else if (StringUtils.equals(dimension, "emailPatch")) {
      scimConfiguration.setScimEmailPatchStrategy(strategy);
    }
  }

  /**
   * SCIM only: try each candidate patch strategy for a single attribute (PATCH), record which ones
   * the target accepts, and report how the working set compares to the configured strategy.
   * @param scimConfiguration scim configuration (mutated per candidate; caller restores it)
   * @param grouperTargetEntity entity to patch
   * @param targetProvisioningEntity current target state (for old values)
   * @param dimension one of "namePatch" or "emailPatch"
   * @param dimensionLabel human label for the report
   * @param attributeName attribute to change (e.g. givenName, emailValue)
   * @param newValue value to set
   * @param candidates strategy candidates to try
   * @param configuredStrategy what is configured on the provisioner
   * @return the candidate strategies that the target accepted (so the caller can revert the change)
   */
  private List<String> appendScimPatchStrategyDiscovery(
      GrouperScim2ProvisionerConfiguration scimConfiguration,
      ProvisioningEntity grouperTargetEntity, ProvisioningEntity targetProvisioningEntity,
      String dimension, String dimensionLabel, String attributeName, String newValue,
      List<String> candidates, String configuredStrategy) {

    this.report.append("\n<b>").append(GrouperUtil.xmlEscape(dimensionLabel)).append("</b> (configured: ")
      .append(GrouperUtil.xmlEscape(configuredStrategy)).append(")\n");

    List<String> workingStrategies = new ArrayList<String>();

    // capture the original target value once, before any candidate has changed it, so the caller
    // can revert back to it after all the discovery steps complete
    String oldValue = targetProvisioningEntity == null ? null : targetProvisioningEntity.retrieveAttributeValueString(attributeName);

    for (String candidate : candidates) {

      // build the PATCH using this candidate strategy
      this.applyScimStrategy(scimConfiguration, dimension, candidate);

      // reset entity state from any prior attempt
      GrouperUtil.setClear(grouperTargetEntity.getInternal_objectChanges());
      grouperTargetEntity.setException(null);
      grouperTargetEntity.setProvisioned(false);

      grouperTargetEntity.assignAttributeValue(attributeName, newValue);
      grouperTargetEntity.addInternal_objectChange(
          new ProvisioningObjectChange(attributeName, ProvisioningObjectChangeAction.update, oldValue, newValue));

      List<ProvisioningEntity> grouperTargetEntitiesToUpdate = GrouperUtil.toList(grouperTargetEntity);

      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().updateEntities(new TargetDaoUpdateEntitiesRequest(grouperTargetEntitiesToUpdate));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsUpdateEntitiesFull(grouperTargetEntitiesToUpdate, true);
        } catch (RuntimeException e) {
          // a failing candidate strategy is expected during discovery; ignore secondary errors
        }
      }

      // classify the outcome.  a strategy only truly "works" if the PATCH both returned success AND
      // the value actually changed in the target.  some SCIM servers return 2xx for a read-only or
      // derived attribute and silently ignore the change (e.g. CrowdStrike returns 200 for an email
      // patch but never applies it, since email is derived from userName).  trusting the status code
      // alone would wrongly report that strategy as working, so read the value back and compare.
      Exception failure = runtimeException != null ? runtimeException : grouperTargetEntity.getException();

      if (failure != null) {
        // the target rejected the PATCH outright (e.g. 400 for an unsupported path or immutable attr)
        String reason = failure.getMessage() == null ? "unknown" : failure.getMessage();
        this.report.append("<font color='gray'><b>Does not work:</b></font> ").append(GrouperUtil.xmlEscape(candidate))
          .append(" (").append(GrouperUtil.xmlEscape(StringUtils.abbreviate(reason, 200))).append(")\n");
      } else {

        // the PATCH returned success -- verify it actually took by reading the value back
        String readBackValue;
        boolean readBackFailed = false;
        try {
          readBackValue = this.readBackScimEntityAttributeValue(
              scimConfiguration, grouperTargetEntity.retrieveAttributeValueString("id"), attributeName);
        } catch (RuntimeException re) {
          readBackValue = null;
          readBackFailed = true;
        }

        if (readBackFailed) {
          // could not confirm one way or the other; treat the success status as working but say so
          workingStrategies.add(candidate);
          this.report.append("<font color='green'><b>Works:</b></font> ").append(GrouperUtil.xmlEscape(candidate))
            .append(" <font color='gray'>(could not read the value back to verify)</font>\n");
        } else if (StringUtils.equals(readBackValue, newValue)) {
          // success and the value stuck
          workingStrategies.add(candidate);
          this.report.append("<font color='green'><b>Works:</b></font> ").append(GrouperUtil.xmlEscape(candidate)).append("\n");
        } else {
          // success status but the value did not change -- the target accepted and silently ignored
          // the patch (the attribute is read-only / derived).  do NOT count this as a working strategy
          this.report.append("<font color='orange'><b>Accepted but ignored:</b></font> ").append(GrouperUtil.xmlEscape(candidate))
            .append(" (target returned success but ").append(GrouperUtil.xmlEscape(attributeName))
            .append(" is still '").append(GrouperUtil.xmlEscape(readBackValue))
            .append("', not '").append(GrouperUtil.xmlEscape(newValue)).append("' -- the attribute may be read-only)\n");
        }
      }
    }

    this.appendScimStrategyComparison(dimensionLabel, configuredStrategy, workingStrategies);

    return workingStrategies;
  }

  /**
   * SCIM only: re-fetch an entity from the target by id and read a single attribute value back, so
   * strategy discovery can tell a PATCH that actually changed the value from one the target accepted
   * with a 2xx but silently ignored (e.g. a read-only or derived attribute such as email).
   * @param scimConfiguration scim configuration
   * @param targetEntityId server-assigned id of the entity in the target
   * @param attributeName attribute to read (e.g. givenName, emailValue)
   * @return the current value in the target, or null if the entity or attribute was not found
   */
  private String readBackScimEntityAttributeValue(
      GrouperScim2ProvisionerConfiguration scimConfiguration, String targetEntityId, String attributeName) {

    if (StringUtils.isBlank(targetEntityId)) {
      return null;
    }

    ScimSettings scimSettings = new ScimSettings();
    scimSettings.loadFromScimProvisionerConfiguration(scimConfiguration);

    GrouperScim2User grouperScim2User = GrouperScim2ApiCommands.retrieveScimUser(
        scimConfiguration.getBearerTokenExternalSystemConfigId(), "id", targetEntityId,
        new GrouperScim2MembershipCache(), scimSettings);

    if (grouperScim2User == null) {
      return null;
    }

    return grouperScim2User.toProvisioningEntity().retrieveAttributeValueString(attributeName);
  }

  /**
   * SCIM only: revert a single attribute back to its original value in the target (PATCH), using a
   * strategy known to work, so diagnostics does not leave the test value behind.
   * @param scimConfiguration scim configuration (mutated to the revert strategy; caller restores it)
   * @param grouperTargetEntity entity to patch
   * @param dimension one of "namePatch" or "emailPatch"
   * @param attributeName attribute to change back (e.g. givenName, emailValue)
   * @param testValue the value that diagnostics put in the target
   * @param originalValue the value to restore
   * @param workingStrategy a strategy the target accepted during discovery
   */
  private void revertScimAttribute(
      GrouperScim2ProvisionerConfiguration scimConfiguration, ProvisioningEntity grouperTargetEntity,
      String dimension, String attributeName, String testValue, String originalValue, String workingStrategy) {

    // nothing was changed in the target if the test value equals what was already there
    if (StringUtils.equals(testValue, originalValue)) {
      return;
    }

    this.applyScimStrategy(scimConfiguration, dimension, workingStrategy);

    GrouperUtil.setClear(grouperTargetEntity.getInternal_objectChanges());
    grouperTargetEntity.setException(null);
    grouperTargetEntity.setProvisioned(false);

    grouperTargetEntity.assignAttributeValue(attributeName, originalValue);
    grouperTargetEntity.addInternal_objectChange(
        new ProvisioningObjectChange(attributeName, ProvisioningObjectChangeAction.update, testValue, originalValue));

    List<ProvisioningEntity> grouperTargetEntitiesToUpdate = GrouperUtil.toList(grouperTargetEntity);

    RuntimeException runtimeException = null;
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().updateEntities(new TargetDaoUpdateEntitiesRequest(grouperTargetEntitiesToUpdate));
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsUpdateEntitiesFull(grouperTargetEntitiesToUpdate, true);
      } catch (RuntimeException e) {
        // ignore secondary errors during revert
      }
    }

    if (runtimeException == null && grouperTargetEntity.getException() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Reverted ").append(GrouperUtil.xmlEscape(attributeName))
        .append(" back to its original value '").append(GrouperUtil.xmlEscape(originalValue)).append("'\n");
    } else {
      Exception failure = runtimeException != null ? runtimeException : grouperTargetEntity.getException();
      String reason = failure == null ? "unknown" : failure.getMessage();
      this.report.append("<font color='orange'><b>Warning:</b></font> Could not revert ").append(GrouperUtil.xmlEscape(attributeName))
        .append(" back to its original value (").append(GrouperUtil.xmlEscape(StringUtils.abbreviate(reason, 200))).append(")\n");
    }
  }

  /**
   * SCIM only: try each candidate email filter strategy by looking the entity up by email, record
   * which ones return the entity, and report how that compares to the configured filter strategy.
   * @param scimConfiguration scim configuration
   * @param emailValue email value to look up
   * @param candidates filter strategy candidates to try
   * @param configuredStrategy configured email filter strategy
   */
  private void appendScimEmailFilterStrategyDiscovery(
      GrouperScim2ProvisionerConfiguration scimConfiguration, String emailValue,
      List<String> candidates, String configuredStrategy) {

    this.report.append("\n<b>Email filter strategy</b> (configured: ").append(GrouperUtil.xmlEscape(configuredStrategy)).append(")\n");

    List<String> workingStrategies = new ArrayList<String>();

    for (String candidate : candidates) {

      GrouperScim2User found = null;
      RuntimeException runtimeException = null;
      try {
        ScimSettings scimSettings = new ScimSettings();
        scimSettings.loadFromScimProvisionerConfiguration(scimConfiguration);
        scimSettings.setScimEmailFilterStrategy(candidate);
        found = GrouperScim2ApiCommands.retrieveScimUser(scimConfiguration.getBearerTokenExternalSystemConfigId(),
            "email", emailValue, new GrouperScim2MembershipCache(), scimSettings);
      } catch (RuntimeException re) {
        runtimeException = re;
      }

      boolean works = runtimeException == null && found != null;

      if (works) {
        workingStrategies.add(candidate);
        this.report.append("<font color='green'><b>Works:</b></font> ").append(GrouperUtil.xmlEscape(candidate)).append("\n");
      } else {
        String reason = runtimeException != null ? StringUtils.abbreviate(runtimeException.getMessage(), 200) : "entity not found with this filter";
        this.report.append("<font color='gray'><b>Does not work:</b></font> ").append(GrouperUtil.xmlEscape(candidate))
          .append(" (").append(GrouperUtil.xmlEscape(reason)).append(")\n");
      }
    }

    if (workingStrategies.size() == 0) {
      this.report.append("<font color='gray'><b>Note:</b></font> No email filter strategy returned the entity; this target does not appear to support looking up entities by email (not applicable, e.g. GitHub). Configured is '")
        .append(GrouperUtil.xmlEscape(configuredStrategy)).append("'.\n");
      return;
    }

    this.appendScimStrategyComparison("Email filter strategy", configuredStrategy, workingStrategies);
  }

  /**
   * SCIM only: report whether the configured strategy is among the strategies that worked, and if
   * not, which ones the operator should switch to.
   * @param dimensionLabel human label
   * @param configuredStrategy configured value
   * @param workingStrategies strategies that worked
   */
  private void appendScimStrategyComparison(String dimensionLabel, String configuredStrategy, List<String> workingStrategies) {

    if (workingStrategies.size() == 0) {
      this.report.append("<font color='red'><b>Action needed:</b></font> ").append(GrouperUtil.xmlEscape(dimensionLabel))
        .append(": no strategy worked against the target. Configured is '").append(GrouperUtil.xmlEscape(configuredStrategy)).append("'.\n");
      return;
    }

    boolean configuredWorks = configuredStrategy != null && workingStrategies.contains(configuredStrategy);

    if (configuredWorks) {
      this.report.append("<font color='green'><b>Success:</b></font> ").append(GrouperUtil.xmlEscape(dimensionLabel))
        .append(": the configured strategy '").append(GrouperUtil.xmlEscape(configuredStrategy)).append("' works.\n");
    } else {
      this.report.append("<font color='red'><b>Action needed:</b></font> ").append(GrouperUtil.xmlEscape(dimensionLabel))
        .append(": the configured strategy '").append(GrouperUtil.xmlEscape(configuredStrategy))
        .append("' does NOT work, but the following do: ").append(GrouperUtil.xmlEscape(StringUtils.join(workingStrategies, ", ")))
        .append(". Update the provisioner configuration to one of these.\n");
    }
  }

}
