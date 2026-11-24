/**
 * Grouper UI URL operation guard (security patch for Grouper 5.x, 6.x and 7.x releases before GRP-7503).
 *
 * Problem: on every UiV2 page load, grouperUi.js runs guiProcessUrlForAjax(location.href), which turns the
 * operation=Class.method and the other parameters of the browser URL into an ajax() POST that carries the
 * user's CSRF token.  So a crafted link such as
 *   UiV2Main.index?operation=UiV2Group.addMemberSubmit&groupName=etc:sysadmingroup&...
 * runs that action for whoever opens it (client side CSRF).
 *
 * Fix (client side, no Java): this file is loaded right after grouperUi.js and wraps four of its globals:
 *   guiV2link(url, options)     remembers which operation an in-page link is about to load (an attacker
 *                               can not call it, only Grouper's own pages and ajax responses do)
 *   guiProcessUrlForAjax(url)   marks the dispatch as "from the browser URL" unless it is the in-page
 *                               link that guiV2link just started
 *   processUrl()                (non UiV2 pages) always "from the browser URL"
 *   ajax(theUrl, options)       while a dispatch is "from the browser URL", only lets the request run if its
 *                               operation is a screen in the allowlist below; otherwise it is refused, the
 *                               user is sent to the home screen and shown an error
 *
 * The operation is taken from the final ajax URL (not from location), and anything that is not exactly
 * ../app/Class.method or ../public/Class.method (letters and digits only) is refused, so duplicate operation
 * params, fragments, encoded characters or dot segments can not slip an action past the check.
 *
 * The allowlist is the GRP-7503 server allowlist (GrouperUiUrlOperationGuard, 2026-11-06) minus the operations
 * whose handler acts in these releases (see the comment on the list, 2026-11-11).  Operations here only
 * display something.  Config (read by commonBottom.jsp, same keys as GRP-7503 so settings carry over when you
 * upgrade): grouper-ui uiV2.urlOperationGuard.mode (enforce default, logOnly, off) and
 * uiV2.urlOperationGuard.additionalAllowedOperations (comma separated Class.method screens).
 *
 * Plain ES5 on purpose (old browsers), ASCII only.
 */
(function () {

  // grouperUi.js must already be loaded; if the dispatcher is not there, there is nothing to guard
  if (typeof window.guiV2link != 'function' || typeof window.guiProcessUrlForAjax != 'function'
      || typeof window.ajax != 'function') {
    return;
  }

  // ---------------------------------------------------------------------------------------------
  // config
  // ---------------------------------------------------------------------------------------------

  /** Class.method, letters and digits only */
  var OPERATION_REGEX = /^[A-Za-z0-9]+\.[A-Za-z0-9]+$/;

  /** the only ajax URL shape a URL operation may have: ../app/Class.method or ../public/Class.method, then ? or # or end */
  var AJAX_URL_REGEX = /^\.\.\/(app|public)\/([A-Za-z0-9]+\.[A-Za-z0-9]+)(?:[?#]|$)/;

  /** how long after guiV2link the dispatch counts as that in-page link (History.js may fire statechange async) */
  var IN_PAGE_LINK_MILLIS = 10000;

  /** same text as GRP-7503 guiMiscUrlOperationNotAllowed (older releases do not have that key) */
  var NOT_ALLOWED_MESSAGE = 'This action cannot be run from a link, bookmark, or by refreshing the page.  '
      + 'Use the button or menu on the screen instead.';

  var config = window.grouperUrlOperationGuardConfig || {};

  // fail closed: anything but logOnly or off means enforce
  var mode = (config.mode === 'logOnly' || config.mode === 'off') ? config.mode : 'enforce';

  /**
   * operations (Class.method) that may run from the browser URL.  Screens only, never actions.
   *
   * Security patch 2026-11-11: an operation is only listed if its handler (and its helpers) only display,
   * whatever the request parameters, in the releases this file is for.  Exceptions by design: Logout.logout
   * ends the user's own session, and UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister is the
   * emailed invite link (it registers the logged in invitee for their own invites).  Left out here because
   * their handler acts in these releases (open them from the menus, which go through guiV2link):
   *   UiV2ProvisionerConfiguration.diagnostics: runs diagnostics (creates the test group, adds the test subject) when provisionerInitted=true
   *   UiV2Admin.daemonJobs: runs, disables, enables or failsafe-approves a daemon when action and jobName are given
   *   UiV2Provisioning.provisioningToOnGroup: saves the provisioning assignment at once when the target has no group metadata (4.26.0, 5.8.0-7.8.0)
   */
  var allowedOperationList = [
    'Logout.logout',
    'UiV2Admin.addDaemon',
    'UiV2Admin.editDaemon',
    'UiV2Admin.instrumentation',
    'UiV2Admin.jobHistoryChart',
    'UiV2Admin.subjectApiDiagnosticsSourceIdChanged',
    'UiV2Admin.viewLogs',
    'UiV2Admin.viewPerformanceChart',
    'UiV2AiAgent.aiAgent',
    'UiV2AiAgent.aiAgentUsageReport',
    'UiV2Attestation.allSettings',
    'UiV2Attestation.attestationOverall',
    'UiV2Attestation.editGroupAttestation',
    'UiV2Attestation.editStemAttestation',
    'UiV2Attestation.groupAttestation',
    'UiV2Attestation.stemAttestation',
    'UiV2Attestation.viewGroupAudits',
    'UiV2Attestation.viewGroupsInStem',
    'UiV2Attestation.viewSettingsInStem',
    'UiV2AttributeDef.assignEdit',
    'UiV2AttributeDef.assignmentMenuAddMetadataAssignment',
    'UiV2AttributeDef.assignmentMenuAddValue',
    'UiV2AttributeDef.attributeDefDelete',
    'UiV2AttributeDef.attributeDefEdit',
    'UiV2AttributeDef.attributeDefPrivileges',
    'UiV2AttributeDef.newAttributeDef',
    'UiV2AttributeDef.thisAttributeDefsPrivilegesInheritedFromFolders',
    'UiV2AttributeDef.viewAttributeDef',
    'UiV2AttributeDef.viewAttributeDefAssignedOwners',
    'UiV2AttributeDefAction.attributeDefActions',
    'UiV2AttributeDefAction.editAttributeDefAction',
    'UiV2AttributeDefAction.newAttributeDefAction',
    'UiV2AttributeDefAttributeAssignment.assignEdit',
    'UiV2AttributeDefAttributeAssignment.assignmentMenuAddMetadataAssignment',
    'UiV2AttributeDefAttributeAssignment.assignmentMenuAddValue',
    'UiV2AttributeDefAttributeAssignment.viewAttributeAssignments',
    'UiV2AttributeDefName.assignEdit',
    'UiV2AttributeDefName.assignmentMenuAddMetadataAssignment',
    'UiV2AttributeDefName.assignmentMenuAddValue',
    'UiV2AttributeDefName.deleteAttributeDefName',
    'UiV2AttributeDefName.editAttributeDefName',
    'UiV2AttributeDefName.editAttributeDefNameInheritance',
    'UiV2AttributeDefName.newAttributeDefName',
    'UiV2AttributeDefName.viewAttributeDefName',
    'UiV2AttributeDefName.viewAttributeDefNameAssignedOwners',
    'UiV2AuthenticationConfig.addWsTrustedJwt',
    'UiV2AuthenticationConfig.editWsTrustedJwtConfig',
    'UiV2AuthenticationConfig.index',
    'UiV2AuthenticationConfig.viewWsTrustedJwts',
    'UiV2Configure.configurationFileAddConfig',
    'UiV2Configure.configurationFileImport',
    'UiV2Configure.configurationReview',
    'UiV2Configure.configure',
    'UiV2Configure.configureSelectFile',
    'UiV2Configure.ddlDeepCheck',
    'UiV2Configure.history',
    'UiV2Configure.index',
    'UiV2Configure.upgradeTasks',
    'UiV2CustomComposite.addCustomComposite',
    'UiV2CustomComposite.addMembershipRequirement',
    'UiV2CustomComposite.editCustomComposite',
    'UiV2CustomComposite.editMembershipRequirement',
    'UiV2CustomComposite.index',
    'UiV2CustomUi.customUiGroup',
    'UiV2CustomUiConfig.addCustomUiConfig',
    'UiV2CustomUiConfig.editCustomUiConfig',
    'UiV2CustomUiConfig.viewCustomUiConfigs',
    'UiV2CustomUiConfig.viewCustomUiConfigsForAll',
    'UiV2Deprovisioning.deprovisionUser',
    'UiV2Deprovisioning.deprovisioningAffiliationSubmit',
    'UiV2Deprovisioning.deprovisioningMain',
    'UiV2Deprovisioning.deprovisioningOnAttributeDef',
    'UiV2Deprovisioning.deprovisioningOnAttributeDefEdit',
    'UiV2Deprovisioning.deprovisioningOnAttributeDefReport',
    'UiV2Deprovisioning.deprovisioningOnFolder',
    'UiV2Deprovisioning.deprovisioningOnFolderEdit',
    'UiV2Deprovisioning.deprovisioningOnFolderReport',
    'UiV2Deprovisioning.deprovisioningOnGroup',
    'UiV2Deprovisioning.deprovisioningOnGroupEdit',
    'UiV2Deprovisioning.deprovisioningOnGroupReport',
    'UiV2Deprovisioning.viewRecentlyDeprovisionedUsers',
    'UiV2EntityDataFields.addDataFieldConfiguration',
    'UiV2EntityDataFields.addDataProviderChangeLogQueryConfig',
    'UiV2EntityDataFields.addDataProviderConfig',
    'UiV2EntityDataFields.addDataProviderQueryConfig',
    'UiV2EntityDataFields.addDataRowConfiguration',
    'UiV2EntityDataFields.addPrivacyRealmConfiguration',
    'UiV2EntityDataFields.editDataFieldConfig',
    'UiV2EntityDataFields.editDataProviderChangeLogQueryConfig',
    'UiV2EntityDataFields.editDataProviderConfig',
    'UiV2EntityDataFields.editDataProviderQueryConfig',
    'UiV2EntityDataFields.editDataRowConfig',
    'UiV2EntityDataFields.editPrivacyRealmConfig',
    'UiV2EntityDataFields.viewDataFieldAndRowDictionary',
    'UiV2EntityDataFields.viewDataProviders',
    'UiV2EntityDataFields.viewEntityDataFields',
    'UiV2EntityDataFields.viewEntityDataFieldsSummary',
    'UiV2EntityDataFields.viewEntityDataProviderChangeLogQueries',
    'UiV2EntityDataFields.viewEntityDataProviderQueries',
    'UiV2EntityDataFields.viewEntityDataRows',
    'UiV2EntityDataFields.viewPrivacyRealmConfigs',
    'UiV2ExternalEntities.inviteExternal',
    'UiV2ExternalSubjectSelfRegister.externalSubjectSelfRegister',
    'UiV2ExternalSystem.addExternalSystem',
    'UiV2ExternalSystem.editExternalSystemConfigDetails',
    'UiV2ExternalSystem.viewExternalSystemConfigDetails',
    'UiV2ExternalSystem.viewExternalSystems',
    'UiV2GlobalAttributeResolverConfig.addGlobalAttributeResolverConfig',
    'UiV2GlobalAttributeResolverConfig.editGlobalAttributeResolverConfig',
    'UiV2GlobalAttributeResolverConfig.viewGlobalAttributeResolverConfigs',
    'UiV2Group.addRuleOnGroup',
    'UiV2Group.editRuleOnGroup',
    'UiV2Group.groupCopy',
    'UiV2Group.groupDelete',
    'UiV2Group.groupEdit',
    'UiV2Group.groupEditComposite',
    'UiV2Group.groupMove',
    'UiV2Group.groupPrivileges',
    'UiV2Group.groupRemoveAllMembers',
    'UiV2Group.inheritedPrivilegesAssignedToThisGroupFromFolders',
    'UiV2Group.newGroup',
    'UiV2Group.thisGroupsAttributeDefPrivileges',
    'UiV2Group.thisGroupsGroupPrivileges',
    'UiV2Group.thisGroupsMemberships',
    'UiV2Group.thisGroupsPrivilegesInheritedFromFolders',
    'UiV2Group.thisGroupsStemPrivileges',
    'UiV2Group.viewAllLoaderManagedGroups',
    'UiV2Group.viewAudits',
    'UiV2Group.viewGroup',
    'UiV2Group.viewGroupMembers',
    'UiV2Group.viewGroupRules',
    'UiV2Group.viewGroupSummary',
    'UiV2Group.viewHistoryChart',
    'UiV2GroupAttributeAssignment.assignEdit',
    'UiV2GroupAttributeAssignment.assignmentMenuAddMetadataAssignment',
    'UiV2GroupAttributeAssignment.assignmentMenuAddValue',
    'UiV2GroupAttributeAssignment.viewAttributeAssignments',
    'UiV2GroupAttributeAssignment.viewMembershipAttributeAssignments',
    'UiV2GroupImport.groupExport',
    'UiV2GroupImport.groupImport',
    'UiV2GroupPermission.groupPermission',
    'UiV2GrouperLoader.editGrouperLoader',
    'UiV2GrouperLoader.importLoaderConfig',
    'UiV2GrouperLoader.loader',
    'UiV2GrouperLoader.loaderDiagnostics',
    'UiV2GrouperLoader.loaderOverall',
    'UiV2GrouperLoader.viewLogs',
    'UiV2GrouperObjectTypes.editObjectTypesOnFolder',
    'UiV2GrouperObjectTypes.editObjectTypesOnGroup',
    'UiV2GrouperObjectTypes.findAutoAssignTypes',
    'UiV2GrouperObjectTypes.viewObjectTypesOnFolder',
    'UiV2GrouperObjectTypes.viewObjectTypesOnGroup',
    'UiV2GrouperReport.reportOnFolderAdd',
    'UiV2GrouperReport.reportOnFolderEdit',
    'UiV2GrouperReport.reportOnGroupAdd',
    'UiV2GrouperReport.reportOnGroupEdit',
    'UiV2GrouperReport.viewAllReportConfigs',
    'UiV2GrouperReport.viewAllReportInstancesForFolder',
    'UiV2GrouperReport.viewAllReportInstancesForGroup',
    'UiV2GrouperReport.viewReportConfigsOnFolder',
    'UiV2GrouperReport.viewReportConfigsOnGroup',
    'UiV2GrouperReport.viewReportInstanceDetailsForFolder',
    'UiV2GrouperReport.viewReportInstanceDetailsForGroup',
    'UiV2GrouperWorkflow.editWorkflowConfig',
    'UiV2GrouperWorkflow.formAdd',
    'UiV2GrouperWorkflow.forms',
    'UiV2GrouperWorkflow.formsUserSubmitted',
    'UiV2GrouperWorkflow.formsWaitingForApproval',
    'UiV2GrouperWorkflow.showJoinGroupForm',
    'UiV2GrouperWorkflow.viewForms',
    'UiV2GrouperWorkflow.viewInstance',
    'UiV2GrouperWorkflow.viewInstances',
    'UiV2GrouperWorkflow.viewWorkflowConfigDetails',
    'UiV2GshTemplateConfig.addGshTemplate',
    'UiV2GshTemplateConfig.editGshTemplate',
    'UiV2GshTemplateConfig.viewGshTemplates',
    'UiV2LocalEntity.createNewWsJwtKey',
    'UiV2LocalEntity.editWsJwtKey',
    'UiV2LocalEntity.localEntityDelete',
    'UiV2LocalEntity.localEntityEdit',
    'UiV2LocalEntity.localEntityPrivileges',
    'UiV2LocalEntity.newLocalEntity',
    'UiV2LocalEntity.viewLocalEntityWSJwtKeys',
    'UiV2Main.globalInheritedPrivileges',
    'UiV2Main.help',
    'UiV2Main.indexMain',
    'UiV2Main.miscellaneous',
    'UiV2Main.myActivity',
    'UiV2Main.myFavorites',
    'UiV2Main.myPreferences',
    'UiV2Main.myServices',
    'UiV2Main.searchSubmit',
    'UiV2Mcp.addMcpRecipe',
    'UiV2Mcp.editMcpRecipe',
    'UiV2Mcp.mcpInfo',
    'UiV2Mcp.viewMcpRecipes',
    'UiV2Membership.editMembership',
    'UiV2Membership.traceAttributeDefPrivileges',
    'UiV2Membership.traceGroupPrivileges',
    'UiV2Membership.traceMembership',
    'UiV2Membership.traceStemPrivileges',
    'UiV2MembershipAttributeAssignment.assignEdit',
    'UiV2MembershipAttributeAssignment.assignmentMenuAddMetadataAssignment',
    'UiV2MembershipAttributeAssignment.assignmentMenuAddValue',
    'UiV2MembershipAttributeAssignment.viewAttributeAssignments',
    'UiV2MyGroups.myGroups',
    'UiV2MyGroups.myGroupsJoin',
    'UiV2MyGroups.myGroupsMemberships',
    'UiV2MyStems.myStems',
    'UiV2MyStems.myStemsContainingAttributesImanage',
    'UiV2MyStems.myStemsContainingGroupsImanage',
    'UiV2ProvisionerConfiguration.addProvisionerConfiguration',
    'UiV2ProvisionerConfiguration.editProvisionerConfiguration',
    'UiV2ProvisionerConfiguration.groupsProvisionable',
    'UiV2ProvisionerConfiguration.viewAssignments',
    'UiV2ProvisionerConfiguration.viewProvisionerActivity',
    'UiV2ProvisionerConfiguration.viewProvisionerConfigDetails',
    'UiV2ProvisionerConfiguration.viewProvisionerConfigurations',
    'UiV2ProvisionerConfiguration.viewProvisionerErrors',
    'UiV2ProvisionerConfiguration.viewProvisionerJobDetails',
    'UiV2ProvisionerConfiguration.viewProvisionerJobs',
    'UiV2ProvisionerConfiguration.viewProvisionerLogs',
    'UiV2Provisioning.editProvisioningOnFolder',
    'UiV2Provisioning.editProvisioningOnGroup',
    'UiV2Provisioning.editProvisioningOnGroupMembership',
    'UiV2Provisioning.editProvisioningOnSubject',
    'UiV2Provisioning.editProvisioningOnSubjectMembership',
    'UiV2Provisioning.provisioningToOnFolder',
    'UiV2Provisioning.viewProvisioningConfigurationOnGroup',
    'UiV2Provisioning.viewProvisioningOnFolder',
    'UiV2Provisioning.viewProvisioningOnGroup',
    'UiV2Provisioning.viewProvisioningOnGroupMembership',
    'UiV2Provisioning.viewProvisioningOnSubject',
    'UiV2Provisioning.viewProvisioningOnSubjectMembership',
    'UiV2Provisioning.viewProvisioningTargetDetailsOnFolder',
    'UiV2Provisioning.viewProvisioningTargetDetailsOnGroup',
    'UiV2Provisioning.viewProvisioningTargetDetailsOnGroupMembership',
    'UiV2Provisioning.viewProvisioningTargetDetailsOnSubject',
    'UiV2Provisioning.viewProvisioningTargetDetailsOnSubjectMembership',
    'UiV2Provisioning.viewProvisioningTargetLogsOnGroup',
    'UiV2Public.postIndex',
    'UiV2Role.roleEditInheritance',
    'UiV2Rules.viewAllRules',
    'UiV2ScriptTester.testScript',
    'UiV2Service.viewService',
    'UiV2SqlSyncConfiguration.addSqlSyncConfiguration',
    'UiV2SqlSyncConfiguration.editSqlSyncConfig',
    'UiV2SqlSyncConfiguration.viewSqlSyncConfigurations',
    'UiV2Stem.addRuleOnStem',
    'UiV2Stem.editRuleOnStem',
    'UiV2Stem.groupMembershipsInFolder',
    'UiV2Stem.newStem',
    'UiV2Stem.privilegesInheritedToObjectsInFolder',
    'UiV2Stem.stemCopy',
    'UiV2Stem.stemDelete',
    'UiV2Stem.stemEdit',
    'UiV2Stem.stemMove',
    'UiV2Stem.stemPrivileges',
    'UiV2Stem.thisStemsPrivilegesInheritedFromFolders',
    'UiV2Stem.viewAudits',
    'UiV2Stem.viewHistoryChart',
    'UiV2Stem.viewStem',
    'UiV2Stem.viewStemRules',
    'UiV2StemAttributeAssignment.assignEdit',
    'UiV2StemAttributeAssignment.assignmentMenuAddMetadataAssignment',
    'UiV2StemAttributeAssignment.assignmentMenuAddValue',
    'UiV2StemAttributeAssignment.viewAttributeAssignments',
    'UiV2Subject.thisSubjectsAttributeDefPrivileges',
    'UiV2Subject.thisSubjectsGroupPrivileges',
    'UiV2Subject.thisSubjectsPrivilegesInheritedFromFolders',
    'UiV2Subject.thisSubjectsStemPrivileges',
    'UiV2Subject.viewAudits',
    'UiV2Subject.viewDataFieldAssignments',
    'UiV2Subject.viewSubject',
    'UiV2SubjectAttributeAssignment.assignEdit',
    'UiV2SubjectAttributeAssignment.assignmentMenuAddMetadataAssignment',
    'UiV2SubjectAttributeAssignment.assignmentMenuAddValue',
    'UiV2SubjectAttributeAssignment.viewAttributeAssignments',
    'UiV2SubjectPermission.subjectPermission',
    'UiV2SubjectResolution.searchSubjects',
    'UiV2SubjectResolution.subjectResolutionMain',
    'UiV2SubjectResolution.viewSubjectDeleteAudits',
    'UiV2SubjectResolution.viewUnresolvedSubjects',
    'UiV2SubjectSource.addSubjectSource',
    'UiV2SubjectSource.compareSubjectSources',
    'UiV2SubjectSource.editSubjectSource',
    'UiV2SubjectSource.viewSubjectSources',
    'UiV2Template.newTemplate',
    'UiV2Template.newTemplateSimplifiedUi',
    'UiV2UserLifecycle.addUserLifecycleActionConfiguration',
    'UiV2UserLifecycle.addUserLifecycleEventConfiguration',
    'UiV2UserLifecycle.addUserLifecyclePolicyConfiguration',
    'UiV2UserLifecycle.addUserLifecyclePolicyPartConfiguration',
    'UiV2UserLifecycle.editUserLifecycleActionConfig',
    'UiV2UserLifecycle.editUserLifecycleEventConfig',
    'UiV2UserLifecycle.editUserLifecyclePolicyConfig',
    'UiV2UserLifecycle.editUserLifecyclePolicyPartConfig',
    'UiV2UserLifecycle.viewUserLifecycleActions',
    'UiV2UserLifecycle.viewUserLifecycleEvents',
    'UiV2UserLifecycle.viewUserLifecyclePolicies',
    'UiV2UserLifecycle.viewUserLifecyclePolicyParts',
    'UiV2UserLifecycle.viewUserLifecycleSummary',
    'UiV2UserLifecycleEvents.viewUserLifecycleEvents',
    'UiV2Visualization.groupView',
    'UiV2Visualization.stemView',
    'UiV2Visualization.subjectView'
  ];

  var allowedOperations = {};
  var i;
  for (i = 0; i < allowedOperationList.length; i++) {
    allowedOperations[allowedOperationList[i]] = true;
  }

  // site additions, filtered again here in case the page put something odd in the config
  var additional = config.additionalAllowedOperations;
  if (additional && additional.length) {
    for (i = 0; i < additional.length; i++) {
      if (typeof additional[i] == 'string' && OPERATION_REGEX.test(additional[i])) {
        allowedOperations[additional[i]] = true;
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // state
  // ---------------------------------------------------------------------------------------------

  /** set by guiV2link: {operation: 'UiV2Group.viewGroup', time: 123}, consumed by the next guiProcessUrlForAjax */
  var pendingInPageLink = null;

  /** set while guiProcessUrlForAjax or processUrl runs: {linkOperation: op or null}, consumed by ajax */
  var urlDispatch = null;

  function now() {
    return new Date().getTime();
  }

  /**
   * first operation=... value of a guiV2link url like operation=UiV2Group.viewGroup&groupId=abc
   * @return the operation or null
   */
  function firstOperationInLinkUrl(url) {
    if (typeof url != 'string') {
      return null;
    }
    var questionIndex = url.indexOf('?');
    var args = (questionIndex == -1 ? url : url.substring(questionIndex + 1)).split('&');
    for (var j = 0; j < args.length; j++) {
      if (args[j].indexOf('operation=') == 0) {
        var operation = args[j].substring('operation='.length);
        return OPERATION_REGEX.test(operation) ? operation : null;
      }
    }
    return null;
  }

  /**
   * operation of an ajax url, same normalization as ajax() in grouperUi.js
   * @return the operation or null if the url is not exactly ../app/Class.method or ../public/Class.method
   */
  function operationOfAjaxUrl(theUrl) {
    var url = String(theUrl);
    if (url.indexOf('../app/') != 0 && url.indexOf('../public/') != 0) {
      url = '../app/' + url;
    }
    var match = AJAX_URL_REGEX.exec(url);
    return match ? match[2] : null;
  }

  /**
   * refuse an operation from the browser URL: go to the home screen and show an error
   */
  function refuse(description) {
    if (window.console && window.console.warn) {
      window.console.warn('Grouper refused an operation from the browser URL that is not a screen: ' + description
          + '.  If it is a screen, add it to grouper-ui uiV2.urlOperationGuard.additionalAllowedOperations');
    }
    var shown = false;
    var showMessage = function () {
      if (shown) {
        return;
      }
      shown = true;
      if (typeof window.guiMessageError == 'function') {
        window.guiMessageError(NOT_ALLOWED_MESSAGE);
      } else {
        alert(NOT_ALLOWED_MESSAGE);
      }
    };
    // the home screen ajax empties #messaging when it starts, so show the message after it is done
    $(document).one('ajaxStop', function () {
      setTimeout(showMessage, 0);
    });
    setTimeout(function () {
      if (!$.active) {
        showMessage();
      }
    }, 3000);
    // only logged in screens have a home screen (public pages just show the message)
    if (location.href.indexOf('/grouperUi/app/') != -1) {
      window.guiV2link('operation=UiV2Main.indexMain');
    }
  }

  // ---------------------------------------------------------------------------------------------
  // wrappers
  // ---------------------------------------------------------------------------------------------

  var originalGuiV2link = window.guiV2link;
  var originalGuiProcessUrlForAjax = window.guiProcessUrlForAjax;
  var originalProcessUrl = window.processUrl;
  var originalAjax = window.ajax;

  /** in-page link: remember its operation so the dispatch it starts is not treated as from the URL */
  window.guiV2link = function (url, options) {
    try {
      var operation = firstOperationInLinkUrl(url);
      pendingInPageLink = operation == null ? null : {operation: operation, time: now()};
    } catch (e) {
      pendingInPageLink = null;
    }
    return originalGuiV2link.apply(this, arguments);
  };

  /** page load, refresh, back/forward, or the dispatch of an in-page link */
  window.guiProcessUrlForAjax = function (url) {
    var link = pendingInPageLink;
    pendingInPageLink = null;
    if (link != null && now() - link.time > IN_PAGE_LINK_MILLIS) {
      link = null;
    }
    var previous = urlDispatch;
    urlDispatch = {linkOperation: link == null ? null : link.operation};
    try {
      return originalGuiProcessUrlForAjax.apply(this, arguments);
    } finally {
      urlDispatch = previous;
    }
  };

  /** non UiV2 pages build the ajax url from the browser URL too */
  if (typeof originalProcessUrl == 'function') {
    window.processUrl = function () {
      var previous = urlDispatch;
      urlDispatch = {linkOperation: null};
      try {
        return originalProcessUrl.apply(this, arguments);
      } finally {
        urlDispatch = previous;
      }
    };
  }

  /** the request itself: buttons and forms (no URL dispatch running) are never checked */
  window.ajax = function (theUrl, options) {
    if (urlDispatch == null || mode === 'off') {
      return originalAjax.apply(this, arguments);
    }
    // one ajax per dispatch
    var dispatch = urlDispatch;
    urlDispatch = null;

    var operation = operationOfAjaxUrl(theUrl);

    // the in-page link guiV2link just started (must be the same operation)
    if (operation != null && dispatch.linkOperation === operation) {
      return originalAjax.apply(this, arguments);
    }
    // a screen
    if (operation != null && allowedOperations[operation] === true) {
      return originalAjax.apply(this, arguments);
    }
    var description = operation == null ? ('(unrecognized url) ' + String(theUrl).substring(0, 200)) : operation;
    if (mode === 'logOnly') {
      if (window.console && window.console.warn) {
        window.console.warn('Grouper would refuse this operation from the browser URL, allowing since '
            + 'uiV2.urlOperationGuard.mode is logOnly: ' + description);
      }
      return originalAjax.apply(this, arguments);
    }
    refuse(description);
    return undefined;
  };

})();
