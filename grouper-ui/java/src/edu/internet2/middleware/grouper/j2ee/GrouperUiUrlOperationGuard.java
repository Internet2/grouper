package edu.internet2.middleware.grouper.j2ee;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * GRP-7503: when the UI loads a screen from the browser URL (page load, refresh, back/forward, a bookmark, a link in an
 * email or another site), grouperUi.js (guiProcessUrlForAjax) runs the operation in the URL as an ajax POST with the
 * CSRF token.  So a crafted link like UiV2Main.index?operation=UiV2Group.groupDeleteSubmit&amp;groupName=a:b would run the
 * action for whoever opens it.  guiProcessUrlForAjax marks those requests with the {@link #HEADER_NAME} header, and
 * GrouperUiRestServlet only lets them run operations in this allowlist of screens.  Buttons and forms call ajax() without
 * the header and are not affected.
 * 
 * If you add a screen that is linked to with guiV2link / handleGuiV2LinkClick / href="?operation=...", add its operation
 * here (GrouperUiUrlOperationGuardTest checks the JSPs, Java and text for this).  Operations in this list must only
 * display something, never change anything.  Sites can add their own with grouper-ui property
 * uiV2.urlOperationGuard.additionalAllowedOperations
 */
public class GrouperUiUrlOperationGuard {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUiUrlOperationGuard.class);

  /**
   * request header that grouperUi.js sends when the operation came from the browser URL (guiProcessUrlForAjax)
   */
  public static final String HEADER_NAME = "X-Grouper-Url-Operation";

  /**
   * mode of the guard, grouper-ui property uiV2.urlOperationGuard.mode
   */
  public static enum GrouperUiUrlOperationGuardMode {
    
    /** reject operations from the url that are not allowed */
    enforce,
    
    /** log operations from the url that are not allowed, but still run them */
    logOnly,
    
    /** do not check */
    off;
    
    /**
     * @param string
     * @return the mode, enforce if blank or not valid
     */
    public static GrouperUiUrlOperationGuardMode valueOfIgnoreCase(String string) {
      for (GrouperUiUrlOperationGuardMode mode : GrouperUiUrlOperationGuardMode.values()) {
        if (StringUtils.equalsIgnoreCase(mode.name(), StringUtils.trim(string))) {
          return mode;
        }
      }
      if (!StringUtils.isBlank(string)) {
        LOG.error("Invalid uiV2.urlOperationGuard.mode '" + string + "', using enforce");
      }
      return enforce;
    }
  }
  
  /**
   * operations (Class.method) that may run when they come from the browser URL.  These are screens only
   */
  private static final Set<String> allowedOperations = Collections.unmodifiableSet(new LinkedHashSet<String>(GrouperUtil.toList(
      "Logout.logout",
      "UiV2Admin.addDaemon",
      "UiV2Admin.daemonJobs",
      "UiV2Admin.editDaemon",
      "UiV2Admin.instrumentation",
      "UiV2Admin.jobHistoryChart",
      "UiV2Admin.subjectApiDiagnosticsSourceIdChanged",
      "UiV2Admin.viewLogs",
      "UiV2Admin.viewPerformanceChart",
      "UiV2AiAgent.aiAgent",
      "UiV2AiAgent.aiAgentUsageReport",
      "UiV2Attestation.allSettings",
      "UiV2Attestation.attestationOverall",
      "UiV2Attestation.editGroupAttestation",
      "UiV2Attestation.editStemAttestation",
      "UiV2Attestation.groupAttestation",
      "UiV2Attestation.stemAttestation",
      "UiV2Attestation.viewGroupAudits",
      "UiV2Attestation.viewGroupsInStem",
      "UiV2Attestation.viewSettingsInStem",
      "UiV2AttributeDef.assignEdit",
      "UiV2AttributeDef.assignmentMenuAddMetadataAssignment",
      "UiV2AttributeDef.assignmentMenuAddValue",
      "UiV2AttributeDef.attributeDefDelete",
      "UiV2AttributeDef.attributeDefEdit",
      "UiV2AttributeDef.attributeDefPrivileges",
      "UiV2AttributeDef.newAttributeDef",
      "UiV2AttributeDef.thisAttributeDefsPrivilegesInheritedFromFolders",
      "UiV2AttributeDef.viewAttributeDef",
      "UiV2AttributeDef.viewAttributeDefAssignedOwners",
      "UiV2AttributeDefAction.attributeDefActions",
      "UiV2AttributeDefAction.editAttributeDefAction",
      "UiV2AttributeDefAction.newAttributeDefAction",
      "UiV2AttributeDefAttributeAssignment.assignEdit",
      "UiV2AttributeDefAttributeAssignment.assignmentMenuAddMetadataAssignment",
      "UiV2AttributeDefAttributeAssignment.assignmentMenuAddValue",
      "UiV2AttributeDefAttributeAssignment.viewAttributeAssignments",
      "UiV2AttributeDefName.assignEdit",
      "UiV2AttributeDefName.assignmentMenuAddMetadataAssignment",
      "UiV2AttributeDefName.assignmentMenuAddValue",
      "UiV2AttributeDefName.deleteAttributeDefName",
      "UiV2AttributeDefName.editAttributeDefName",
      "UiV2AttributeDefName.editAttributeDefNameInheritance",
      "UiV2AttributeDefName.newAttributeDefName",
      "UiV2AttributeDefName.viewAttributeDefName",
      "UiV2AttributeDefName.viewAttributeDefNameAssignedOwners",
      "UiV2AuthenticationConfig.addWsTrustedJwt",
      "UiV2AuthenticationConfig.editWsTrustedJwtConfig",
      "UiV2AuthenticationConfig.index",
      "UiV2AuthenticationConfig.viewWsTrustedJwts",
      "UiV2ChangeLogMonitor.changeLogMonitor",
      "UiV2Configure.configurationFileAddConfig",
      "UiV2Configure.configurationFileImport",
      "UiV2Configure.configurationReview",
      "UiV2Configure.configure",
      "UiV2Configure.configureSelectFile",
      "UiV2Configure.ddlDeepCheck",
      "UiV2Configure.history",
      "UiV2Configure.index",
      "UiV2Configure.upgradeTasks",
      "UiV2CustomUi.customUiGroup",
      "UiV2CustomUiConfig.addCustomUiConfig",
      "UiV2CustomUiConfig.editCustomUiConfig",
      "UiV2CustomUiConfig.viewCustomUiConfigs",
      "UiV2CustomUiConfig.viewCustomUiConfigsForAll",
      "UiV2Deprovisioning.deprovisionUser",
      "UiV2Deprovisioning.deprovisioningAffiliationSubmit",
      "UiV2Deprovisioning.deprovisioningMain",
      "UiV2Deprovisioning.deprovisioningOnAttributeDef",
      "UiV2Deprovisioning.deprovisioningOnAttributeDefEdit",
      "UiV2Deprovisioning.deprovisioningOnAttributeDefReport",
      "UiV2Deprovisioning.deprovisioningOnFolder",
      "UiV2Deprovisioning.deprovisioningOnFolderEdit",
      "UiV2Deprovisioning.deprovisioningOnFolderReport",
      "UiV2Deprovisioning.deprovisioningOnGroup",
      "UiV2Deprovisioning.deprovisioningOnGroupEdit",
      "UiV2Deprovisioning.deprovisioningOnGroupReport",
      "UiV2Deprovisioning.viewRecentlyDeprovisionedUsers",
      "UiV2EntityDataFields.addDataFieldConfiguration",
      "UiV2EntityDataFields.addDataProviderChangeLogQueryConfig",
      "UiV2EntityDataFields.addDataProviderConfig",
      "UiV2EntityDataFields.addDataProviderQueryConfig",
      "UiV2EntityDataFields.addDataRowConfiguration",
      "UiV2EntityDataFields.addPrivacyRealmConfiguration",
      "UiV2EntityDataFields.editDataFieldConfig",
      "UiV2EntityDataFields.editDataProviderChangeLogQueryConfig",
      "UiV2EntityDataFields.editDataProviderConfig",
      "UiV2EntityDataFields.editDataProviderQueryConfig",
      "UiV2EntityDataFields.editDataRowConfig",
      "UiV2EntityDataFields.editPrivacyRealmConfig",
      "UiV2EntityDataFields.viewDataFieldAndRowDictionary",
      "UiV2EntityDataFields.viewDataProviders",
      "UiV2EntityDataFields.viewEntityDataFields",
      "UiV2EntityDataFields.viewEntityDataFieldsSummary",
      "UiV2EntityDataFields.viewEntityDataProviderChangeLogQueries",
      "UiV2EntityDataFields.viewEntityDataProviderQueries",
      "UiV2EntityDataFields.viewEntityDataRows",
      "UiV2EntityDataFields.viewPrivacyRealmConfigs",
      "UiV2ExternalEntities.inviteExternal",
      "UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister",
      "UiV2ExternalSystem.addExternalSystem",
      "UiV2ExternalSystem.editExternalSystemConfigDetails",
      "UiV2ExternalSystem.viewExternalSystemConfigDetails",
      "UiV2ExternalSystem.viewExternalSystems",
      "UiV2GlobalAttributeResolverConfig.addGlobalAttributeResolverConfig",
      "UiV2GlobalAttributeResolverConfig.editGlobalAttributeResolverConfig",
      "UiV2GlobalAttributeResolverConfig.viewGlobalAttributeResolverConfigs",
      "UiV2Group.addRuleOnGroup",
      "UiV2Group.editRuleOnGroup",
      "UiV2Group.groupCopy",
      "UiV2Group.groupDelete",
      "UiV2Group.groupEdit",
      "UiV2Group.groupEditComposite",
      "UiV2Group.groupMove",
      "UiV2Group.groupPrivileges",
      "UiV2Group.groupRemoveAllMembers",
      "UiV2Group.inheritedPrivilegesAssignedToThisGroupFromFolders",
      "UiV2Group.newGroup",
      "UiV2Group.thisGroupsAttributeDefPrivileges",
      "UiV2Group.thisGroupsGroupPrivileges",
      "UiV2Group.thisGroupsMemberships",
      "UiV2Group.thisGroupsPrivilegesInheritedFromFolders",
      "UiV2Group.thisGroupsStemPrivileges",
      "UiV2Group.viewAllLoaderManagedGroups",
      "UiV2Group.viewAudits",
      "UiV2Group.viewGroup",
      "UiV2Group.viewGroupMembers",
      "UiV2Group.viewGroupRules",
      "UiV2Group.viewGroupSummary",
      "UiV2Group.viewHistoryChart",
      "UiV2GroupAttributeAssignment.assignEdit",
      "UiV2GroupAttributeAssignment.assignmentMenuAddMetadataAssignment",
      "UiV2GroupAttributeAssignment.assignmentMenuAddValue",
      "UiV2GroupAttributeAssignment.viewAttributeAssignments",
      "UiV2GroupAttributeAssignment.viewMembershipAttributeAssignments",
      "UiV2GroupImport.groupExport",
      "UiV2GroupImport.groupImport",
      "UiV2GroupPermission.groupPermission",
      "UiV2GrouperLoader.editGrouperLoader",
      "UiV2GrouperLoader.importLoaderConfig",
      "UiV2GrouperLoader.loader",
      "UiV2GrouperLoader.loaderDiagnostics",
      "UiV2GrouperLoader.loaderOverall",
      "UiV2GrouperLoader.viewLogs",
      "UiV2GrouperObjectTypes.editObjectTypesOnFolder",
      "UiV2GrouperObjectTypes.editObjectTypesOnGroup",
      "UiV2GrouperObjectTypes.findAutoAssignTypes",
      "UiV2GrouperObjectTypes.viewObjectTypesOnFolder",
      "UiV2GrouperObjectTypes.viewObjectTypesOnGroup",
      "UiV2GrouperReport.reportOnFolderAdd",
      "UiV2GrouperReport.reportOnFolderEdit",
      "UiV2GrouperReport.reportOnGroupAdd",
      "UiV2GrouperReport.reportOnGroupEdit",
      "UiV2GrouperReport.viewAllReportConfigs",
      "UiV2GrouperReport.viewAllReportInstancesForFolder",
      "UiV2GrouperReport.viewAllReportInstancesForGroup",
      "UiV2GrouperReport.viewReportConfigsOnFolder",
      "UiV2GrouperReport.viewReportConfigsOnGroup",
      "UiV2GrouperReport.viewReportInstanceDetailsForFolder",
      "UiV2GrouperReport.viewReportInstanceDetailsForGroup",
      "UiV2GrouperWorkflow.editWorkflowConfig",
      "UiV2GrouperWorkflow.formAdd",
      "UiV2GrouperWorkflow.forms",
      "UiV2GrouperWorkflow.formsUserSubmitted",
      "UiV2GrouperWorkflow.formsWaitingForApproval",
      "UiV2GrouperWorkflow.showJoinGroupForm",
      "UiV2GrouperWorkflow.viewForms",
      "UiV2GrouperWorkflow.viewInstance",
      "UiV2GrouperWorkflow.viewInstances",
      "UiV2GrouperWorkflow.viewWorkflowConfigDetails",
      "UiV2GshTemplateConfig.addGshTemplate",
      "UiV2GshTemplateConfig.editGshTemplate",
      "UiV2GshTemplateConfig.viewGshTemplates",
      "UiV2LocalEntity.createNewWsJwtKey",
      "UiV2LocalEntity.editWsJwtKey",
      "UiV2LocalEntity.localEntityDelete",
      "UiV2LocalEntity.localEntityEdit",
      "UiV2LocalEntity.localEntityPrivileges",
      "UiV2LocalEntity.newLocalEntity",
      "UiV2LocalEntity.viewLocalEntityWSJwtKeys",
      "UiV2Main.globalInheritedPrivileges",
      "UiV2Main.help",
      "UiV2Main.indexMain",
      "UiV2Main.miscellaneous",
      "UiV2Main.myActivity",
      "UiV2Main.myFavorites",
      "UiV2Main.myPreferences",
      "UiV2Main.myServices",
      "UiV2Main.searchSubmit",
      "UiV2Mcp.addMcpRecipe",
      "UiV2Mcp.editMcpRecipe",
      "UiV2Mcp.mcpInfo",
      "UiV2Mcp.viewMcpRecipes",
      "UiV2Membership.editMembership",
      "UiV2Membership.traceAttributeDefPrivileges",
      "UiV2Membership.traceGroupPrivileges",
      "UiV2Membership.traceMembership",
      "UiV2Membership.traceStemPrivileges",
      "UiV2MembershipAttributeAssignment.assignEdit",
      "UiV2MembershipAttributeAssignment.assignmentMenuAddMetadataAssignment",
      "UiV2MembershipAttributeAssignment.assignmentMenuAddValue",
      "UiV2MembershipAttributeAssignment.viewAttributeAssignments",
      "UiV2MyGroups.myGroups",
      "UiV2MyGroups.myGroupsJoin",
      "UiV2MyGroups.myGroupsMemberships",
      "UiV2MyStems.myStems",
      "UiV2MyStems.myStemsContainingAttributesImanage",
      "UiV2MyStems.myStemsContainingGroupsImanage",
      "UiV2ProvisionerConfiguration.addProvisionerConfiguration",
      "UiV2ProvisionerConfiguration.diagnostics",
      "UiV2ProvisionerConfiguration.editProvisionerConfiguration",
      "UiV2ProvisionerConfiguration.groupsProvisionable",
      "UiV2ProvisionerConfiguration.viewAssignments",
      "UiV2ProvisionerConfiguration.viewProvisionerActivity",
      "UiV2ProvisionerConfiguration.viewProvisionerConfigDetails",
      "UiV2ProvisionerConfiguration.viewProvisionerConfigurations",
      "UiV2ProvisionerConfiguration.viewProvisionerErrors",
      "UiV2ProvisionerConfiguration.viewProvisionerJobDetails",
      "UiV2ProvisionerConfiguration.viewProvisionerJobs",
      "UiV2ProvisionerConfiguration.viewProvisionerLogs",
      "UiV2Provisioning.editProvisioningOnFolder",
      "UiV2Provisioning.editProvisioningOnGroup",
      "UiV2Provisioning.editProvisioningOnGroupMembership",
      "UiV2Provisioning.editProvisioningOnSubject",
      "UiV2Provisioning.editProvisioningOnSubjectMembership",
      "UiV2Provisioning.provisioningToOnFolder",
      "UiV2Provisioning.provisioningToOnGroup",
      "UiV2Provisioning.viewProvisioningConfigurationOnGroup",
      "UiV2Provisioning.viewProvisioningOnFolder",
      "UiV2Provisioning.viewProvisioningOnGroup",
      "UiV2Provisioning.viewProvisioningOnGroupMembership",
      "UiV2Provisioning.viewProvisioningOnSubject",
      "UiV2Provisioning.viewProvisioningOnSubjectMembership",
      "UiV2Provisioning.viewProvisioningTargetDetailsOnFolder",
      "UiV2Provisioning.viewProvisioningTargetDetailsOnGroup",
      "UiV2Provisioning.viewProvisioningTargetDetailsOnGroupMembership",
      "UiV2Provisioning.viewProvisioningTargetDetailsOnSubject",
      "UiV2Provisioning.viewProvisioningTargetDetailsOnSubjectMembership",
      "UiV2Provisioning.viewProvisioningTargetLogsOnGroup",
      "UiV2Public.postIndex",
      "UiV2Role.roleEditInheritance",
      "UiV2Rules.viewAllRules",
      "UiV2ScriptTester.testScript",
      "UiV2Service.viewService",
      "UiV2SqlSyncConfiguration.addSqlSyncConfiguration",
      "UiV2SqlSyncConfiguration.editSqlSyncConfig",
      "UiV2SqlSyncConfiguration.viewSqlSyncConfigurations",
      "UiV2Stem.addRuleOnStem",
      "UiV2Stem.editRuleOnStem",
      "UiV2Stem.groupMembershipsInFolder",
      "UiV2Stem.newStem",
      "UiV2Stem.privilegesInheritedToObjectsInFolder",
      "UiV2Stem.stemCopy",
      "UiV2Stem.stemDelete",
      "UiV2Stem.stemEdit",
      "UiV2Stem.stemMove",
      "UiV2Stem.stemPrivileges",
      "UiV2Stem.thisStemsPrivilegesInheritedFromFolders",
      "UiV2Stem.viewAudits",
      "UiV2Stem.viewHistoryChart",
      "UiV2Stem.viewStem",
      "UiV2Stem.viewStemRules",
      "UiV2StemAttributeAssignment.assignEdit",
      "UiV2StemAttributeAssignment.assignmentMenuAddMetadataAssignment",
      "UiV2StemAttributeAssignment.assignmentMenuAddValue",
      "UiV2StemAttributeAssignment.viewAttributeAssignments",
      "UiV2Subject.thisSubjectsAttributeDefPrivileges",
      "UiV2Subject.thisSubjectsGroupPrivileges",
      "UiV2Subject.thisSubjectsPrivilegesInheritedFromFolders",
      "UiV2Subject.thisSubjectsStemPrivileges",
      "UiV2Subject.viewAudits",
      "UiV2Subject.viewDataFieldAssignments",
      "UiV2Subject.viewSubject",
      "UiV2SubjectAttributeAssignment.assignEdit",
      "UiV2SubjectAttributeAssignment.assignmentMenuAddMetadataAssignment",
      "UiV2SubjectAttributeAssignment.assignmentMenuAddValue",
      "UiV2SubjectAttributeAssignment.viewAttributeAssignments",
      "UiV2SubjectPermission.subjectPermission",
      "UiV2SubjectResolution.searchSubjects",
      "UiV2SubjectResolution.subjectResolutionMain",
      "UiV2SubjectResolution.viewSubjectDeleteAudits",
      "UiV2SubjectResolution.viewUnresolvedSubjects",
      "UiV2SubjectSource.addSubjectSource",
      "UiV2SubjectSource.compareSubjectSources",
      "UiV2SubjectSource.editSubjectSource",
      "UiV2SubjectSource.viewSubjectSources",
      "UiV2Template.newTemplate",
      "UiV2Template.newTemplateSimplifiedUi",
      "UiV2UserLifecycle.addUserLifecycleActionConfiguration",
      "UiV2UserLifecycle.addUserLifecycleEventConfiguration",
      "UiV2UserLifecycle.addUserLifecyclePolicyConfiguration",
      "UiV2UserLifecycle.addUserLifecyclePolicyPartConfiguration",
      "UiV2UserLifecycle.editUserLifecycleActionConfig",
      "UiV2UserLifecycle.editUserLifecycleEventConfig",
      "UiV2UserLifecycle.editUserLifecyclePolicyConfig",
      "UiV2UserLifecycle.editUserLifecyclePolicyPartConfig",
      "UiV2UserLifecycle.viewUserLifecycleActions",
      "UiV2UserLifecycle.viewUserLifecycleEvents",
      "UiV2UserLifecycle.viewUserLifecyclePolicies",
      "UiV2UserLifecycle.viewUserLifecyclePolicyParts",
      "UiV2UserLifecycle.viewUserLifecycleSummary",
      "UiV2UserLifecycleEvents.viewUserLifecycleEvents",
      "UiV2Visualization.groupView",
      "UiV2Visualization.stemView",
      "UiV2Visualization.subjectView"
  )));

  /**
   * @return the built in allowed operations (not including the ones from config)
   */
  public static Set<String> allowedOperations() {
    return allowedOperations;
  }

  /**
   * @param request
   * @return true if grouperUi.js sent this request because the operation was in the browser URL
   */
  public static boolean isOperationFromUrl(HttpServletRequest request) {
    return StringUtils.equalsIgnoreCase("true", request.getHeader(HEADER_NAME));
  }

  /**
   * @return the mode from config
   */
  public static GrouperUiUrlOperationGuardMode mode() {
    return GrouperUiUrlOperationGuardMode.valueOfIgnoreCase(
        GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.urlOperationGuard.mode", "enforce"));
  }

  /**
   * @param classAndMethodName e.g. UiV2Group.viewGroup
   * @return true if the operation is a screen that may run from the browser URL
   */
  public static boolean isAllowedOperation(String classAndMethodName) {
    if (allowedOperations.contains(classAndMethodName)) {
      return true;
    }
    String additional = GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.urlOperationGuard.additionalAllowedOperations", null);
    if (!StringUtils.isBlank(additional)) {
      for (String operation : GrouperUtil.splitTrim(additional, ",")) {
        if (StringUtils.equals(operation, classAndMethodName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * see if this request may run this operation.  Requests without the header (buttons, forms) are always allowed
   * @param request
   * @param classAndMethodName e.g. UiV2Group.groupDeleteSubmit
   * @return false if the operation came from the browser URL, is not allowed, and the mode is enforce
   */
  public static boolean allowRequest(HttpServletRequest request, String classAndMethodName) {
    if (!isOperationFromUrl(request)) {
      return true;
    }
    GrouperUiUrlOperationGuardMode mode = mode();
    if (mode == GrouperUiUrlOperationGuardMode.off || isAllowedOperation(classAndMethodName)) {
      return true;
    }
    Subject loggedInSubject = null;
    try {
      loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn(true, null);
    } catch (RuntimeException re) {
      // just for logging
    }
    String subjectString = loggedInSubject == null ? "(none)" : loggedInSubject.getSourceId() + "::" + loggedInSubject.getId();
    if (mode == GrouperUiUrlOperationGuardMode.logOnly) {
      LOG.warn("Operation from the browser URL is not in the allowlist, allowing since uiV2.urlOperationGuard.mode is logOnly: '" 
          + classAndMethodName + "', subject: " + subjectString);
      return true;
    }
    LOG.warn("Rejected operation from the browser URL which is not in the allowlist: '" + classAndMethodName + "', subject: " 
        + subjectString + ".  If this is a screen, add it to uiV2.urlOperationGuard.additionalAllowedOperations");
    return false;
  }
}
