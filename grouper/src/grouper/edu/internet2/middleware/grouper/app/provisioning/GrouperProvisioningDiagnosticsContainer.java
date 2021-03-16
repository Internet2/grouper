package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.ui.util.ProgressBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
    
    for (String propertyName : grouperLoaderConfig.propertyNames()) {
      if (propertyName.startsWith(configPrefix)) {
        String suffix = GrouperUtil.prefixOrSuffix(propertyName, configPrefix, false);
        String lowerKey = suffix.toLowerCase();
        boolean secret = lowerKey.contains("pass") || lowerKey.contains("secret") || lowerKey.contains("private");
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
    try {
      this.report = new StringBuilder();
      
      this.appendConfiguration();
      
      this.report.append("<h4>Provisioner</h4><pre>");
      GrouperProvisioningObjectLogType.appendProvisioner(grouperProvisioner, this.report, "Provisioner");
      this.report.append("</pre>\n<h4>Configuration analysis</h4><pre>");
      GrouperProvisioningObjectLogType.appendConfiguration(grouperProvisioner, this.report, "Configuration");
      this.report.append("</pre>\n<h4>Target Dao capabilities</h4><pre>");
      GrouperProvisioningObjectLogType.appendTargetDaoCapabilities(grouperProvisioner, this.report, "Target Dao capabilities");
      this.report.append("</pre>\n<h4>Provisioner behaviors</h4><pre>");
      GrouperProvisioningObjectLogType.appendTargetDaoBehaviors(grouperProvisioner, this.report, "Provisioner behaviors");
      this.report.append("</pre>\n");
      
      this.appendSelectAllGroups();
      this.appendSelectAllEntities();
      this.appendSelectAllMemberships();
      
    } finally {
      this.inDiagnostics = false;
    }
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
    if (!this.diagnosticsGroupsAllSelect) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all groups\n");
    } else {
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllGroups(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all groups\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all groups\n");
      } else {

        try {
            
          TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllGroups(new TargetDaoRetrieveAllGroupsRequest(this.diagnosticsMembershipsAllSelect));
          List<ProvisioningGroup> targetGroups = targetDaoRetrieveAllGroupsResponse == null ? null : targetDaoRetrieveAllGroupsResponse.getTargetGroups();
          this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().setProvisioningGroups(targetGroups);

          if (GrouperUtil.length(targetGroups) > 0) {
            this.report.append("<font color='green'><b>Success:</b></font> Selected " + GrouperUtil.length(targetGroups) + " groups")
              .append(this.getCurrentDuration()).append("\n");
          } else {
            this.report.append("<font color='orange'><b>Warning:</b></font> Selected " + GrouperUtil.length(targetGroups) + " groups")
              .append(this.getCurrentDuration()).append("\n");
          }
          
          for (int i=0;i<Math.min(10,GrouperUtil.length(targetGroups)); i++) {
            ProvisioningGroup targetGroup = targetGroups.get(i);

            this.report.append("<font color='gray'><b>Note:</b></font> Group ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetGroups)).append(" (unprocessed):\n  ").append(GrouperUtil.xmlEscape(targetGroup.toString())).append("\n");

            List<ProvisioningGroup> targetGroupsForOne = GrouperUtil.toList(targetGroup);
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(
                targetGroupsForOne, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(
                targetGroupsForOne);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
                targetGroupsForOne);

            this.report.append("<font color='gray'><b>Note:</b></font> Group ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetGroups)).append(" (filtered, attributes manipulated, matchingId calculated):\n  ").append(GrouperUtil.xmlEscape(targetGroup.toString())).append("\n");
            
          }


          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting all groups").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        }
      }
    }
    
    this.report.append("</pre>");
  }

  /**
   * if select all entities during diagnostics
   */
  private boolean diagnosticsEntitiesAllSelect;

  /**
   * if select all entities during diagnostics
   * @return
   */
  public boolean isDiagnosticsEntitiesAllSelect() {
    return diagnosticsEntitiesAllSelect;
  }


  /**
   * if select all entities during diagnostics
   * @param diagnosticsEntitiesAllSelect
   */
  public void setDiagnosticsEntitiesAllSelect(boolean diagnosticsEntitiesAllSelect) {
    this.diagnosticsEntitiesAllSelect = diagnosticsEntitiesAllSelect;
  }

  /**
   * if select all memberships during diagnostics
   */
  private boolean diagnosticsMembershipsAllSelect;



  /**
   * if select all memberships during diagnostics
   * @return
   */
  public boolean isDiagnosticsMembershipsAllSelect() {
    return diagnosticsMembershipsAllSelect;
  }


  /**
   * if select all memberships during diagnostics
   * @param diagnosticsMembershipsAllSelect
   */
  public void setDiagnosticsMembershipsAllSelect(boolean diagnosticsMembershipsAllSelect) {
    this.diagnosticsMembershipsAllSelect = diagnosticsMembershipsAllSelect;
  }


  /**
   * if select all groups during diagnostics
   */
  private boolean diagnosticsGroupsAllSelect;

  /**
   * if select all groups during diagnostics
   * @return
   */
  public boolean isDiagnosticsGroupsAllSelect() {
    return diagnosticsGroupsAllSelect;
  }


  /**
   * if select all groups during diagnostics
   * @param selectAllGroupsDuringDiagnostics
   */
  public void setDiagnosticsGroupsAllSelect(
      boolean selectAllGroupsDuringDiagnostics) {
    this.diagnosticsGroupsAllSelect = selectAllGroupsDuringDiagnostics;
  }

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


  public void appendSelectAllEntities() {
    this.report.append("<h4>All entities</h4>");
    this.report.append("<pre>");
    if (!this.diagnosticsEntitiesAllSelect) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all entities\n");
    } else {
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllEntities(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all entities\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all entities\n");
      } else {
  
        try {
            
          TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllEntities(new TargetDaoRetrieveAllEntitiesRequest(this.diagnosticsMembershipsAllSelect));
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
    if (!this.diagnosticsMembershipsAllSelect) {
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
  
  
  
}
