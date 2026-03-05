
<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<section class="grouper-summary">

  <%-- tell add member to refresh audits --%>
  <form id="groupRefreshPartFormId">
    <input type="hidden" name="groupRefreshPart" value="summary" /> 
  </form> 
  
  <div id="groupDetailsId">
    <table class="table table-condensed" id="groupDetailsTableId">
      <tbody>
        <!-- colspan across for next title -->
        <tr class="grouperIgnoreStripe"><td colspan="2" style="background-color: white"><p style="display: block; margin-top: 0;" />
        <h3 style="margin-bottom: 0px; padding-bottom: 0px"><p class="lead" style="margin-bottom: 0px; padding-bottom: 0px">${textContainer.text['groupSummaryDecription'] }
          <c:if test="${grouperRequestContainer.groupContainer.guiGroup.canRead or grouperRequestContainer.groupContainer.guiGroup.canUpdate}">
            &nbsp;
            <span id="groupSummaryMoreId" style="font-size: 0.65em; font-weight: 400"><a href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGroupDetails']}"
               onclick="$('#groupSummaryMoreId').hide('slow'); ajax('../app/UiV2Group.viewGroupSummaryMore?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
               >${textContainer.text['guiGroupSummaryMore']} <i class="fa fa-angle-down"></i></a></span>
          </c:if>
        </p></h3>
        
        <span style="font-size: 0.8em">${textContainer.text['guiGroupSummaryDisclaimer']}</span>
        
        <p style="display: block; margin-top: 0;" /></td></tr>
      
        <%@ include file="../group/groupSummaryCustom.jsp"%>
      
        <c:if test="${not empty grouperRequestContainer.objectTypeContainer.guiConfiguredGrouperObjectTypesAttributeValues}">
          <!-- Types -->
          <tr>
            <td style="vertical-align: top"><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['objectTypeMoreActionsMenuLabel'] }</a></strong></td>
            <td style="padding-left: 0px;">
              <c:forEach var="guiConfiguredGrouperObjectTypesAttributeValue" varStatus="status" items="${grouperRequestContainer.objectTypeContainer.guiConfiguredGrouperObjectTypesAttributeValues}">
                <c:if test="${fn:length(grouperRequestContainer.objectTypeContainer.guiConfiguredGrouperObjectTypesAttributeValues) > 1}"><li style="margin-left: 12px;"></c:if>
                <c:set var="objectType" value="${guiConfiguredGrouperObjectTypesAttributeValue.grouperObjectTypesAttributeValue.objectTypeName}" />
                ${textContainer.text[grouper:concat2('objectTypeOptionBold_',objectType)] }
                <c:if test="${fn:length(grouperRequestContainer.objectTypeContainer.guiConfiguredGrouperObjectTypesAttributeValues) > 1}"></li></c:if>
              </c:forEach>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.canRead}">
          <tr>
            <!-- MEMBERSHIP -->
            <td style="vertical-align: top"><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.viewGroupMembers&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupMembersTab'] }</a></strong></td>
            <td style="padding-left: 0px;" id="groupMembershipSummaryCellId">
              <%-- Note: this is also in groupSummaryMoreMemberships.jsp  --%>
              ${grouperRequestContainer.groupSummaryContainer.totalMembersCount} ${textContainer.text['groupSummaryPageMembershipsTotalMembers'] }
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.canAdmin}">
          <tr style="display: none" id="groupPrivilegeSummaryRowId">
            <!-- PRIVILEGES -->
            <td style="vertical-align: top;"><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupPrivilegesTab'] }</a></strong></td>
            <td style="padding-left: 0px;" id="groupPrivilegeSummaryCellId">
            </td>
          </tr>
          <c:if test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup}">
          <!--  LOADER -->
            <tr>
              <td style="vertical-align: top;"><strong>
              <a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2GrouperLoader.loader&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperMenuItemLoader'] }</a>
              </strong></td>
              <td style="padding-left: 0px;">
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperSqlLoader}">
                  <li style="margin-left: 12px;">${textContainer.text['groupSummaryPageSqlLoadedGroup']}</li>
                  <li style="margin-left: 12px;">${grouperRequestContainer.grouperLoaderContainer.sqlLoaderType} loader</li>
                  <li style="margin-left: 12px;">
                  <c:if test="${!grouper.isBlank(grouperRequestContainer.grouperLoaderContainer.sqlQuery)}">
                    <pre>${grouperRequestContainer.grouperLoaderContainer.sqlQuery}</pre>
                  </c:if>
                  </li>
                </c:if>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperLdapLoader}">
                  <li style="margin-left: 12px;">${textContainer.text['groupSummaryPageLdapLoadedGroup']}</li>
                  <li style="margin-left: 12px;">${grouperRequestContainer.grouperLoaderContainer.ldapLoaderType} loader</li>
                  <c:if test="${!grouper.isBlank(grouperRequestContainer.grouperLoaderContainer.ldapLoaderFilter)}">
                    <pre>${grouperRequestContainer.grouperLoaderContainer.ldapLoaderFilter}</pre>
                  </c:if> 
                </c:if>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperJexlScriptLoader}">
                  ${textContainer.text['groupSummaryPageJexlLoadedGroup']}<br /><br />
                  <c:if test="${!grouper.isBlank(grouperRequestContainer.grouperLoaderContainer.jexlScriptJexlScript)}">
                    <pre>${grouperRequestContainer.grouperLoaderContainer.jexlScriptJexlScript}</pre>
                  </c:if> 
                </c:if>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}">
                  <li style="margin-left: 12px;">${textContainer.text['groupSummaryPageRecentMembershipsLoadedGroup']}</li>
                </c:if>
              </td>
            </tr>
          </c:if>
          
          <c:if test="${grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependenciesCount > 0}">
            <!--  ABAC SCRIPTED GROUP SECTION -->
            <tr>
              <td style="vertical-align: top;"><strong>${textContainer.text['groupSummaryPageAbacScriptedGroup']}</strong></td>
              <td style="padding-left: 0px;">
                ${textContainer.text['groupSummaryPageAbacScriptedGroupDependenciesCountMessage']}:
                <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependencies}"> 
                    <c:forEach var="abacScriptedGroupDependency" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependencies}">
                      ${abacScriptedGroupDependency.shortLinkWithIcon}<c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                </c:if>
              </td>
            </tr>
          </c:if>
        </c:if>
        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.canAdmin}">
          <c:if test="${grouperRequestContainer.groupSummaryContainer.composite or grouperRequestContainer.groupSummaryContainer.compositeSize > 0}">
            <!-- COMPOSITES -->
            <tr>
              <td style="vertical-align: top;"><strong>${textContainer.text['groupComposites']}</strong></td>
              <td style="padding-left: 0px;">
                <c:if test="${grouperRequestContainer.groupSummaryContainer.composite}">
                  ${textContainer.text['groupSummaryPageCompositeGroupMessage']}
                </c:if>
                
                <c:if test="${grouperRequestContainer.groupSummaryContainer.compositeSize > 0}">
                  ${textContainer.text['groupSummaryPageCompositeFactorMessage']}
                  <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.composites}"> 
                      <c:forEach var="composite" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.composites}">
                        ${composite.shortLinkWithIcon}
                        <c:if test="${!status.last}">,</c:if>
                      </c:forEach>
                  </c:if>
                </c:if>
              </td>
            </tr>
          </c:if>
        </c:if>
        <!-- PROVISIONING -->
        <tr style="display: none" id="groupConfigurationProvisioningRowId">
          <td style="vertical-align: top;"><strong><a href="javascript:void(0)" id="groupMoreActionsProvisioningButtonId" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                          >${textContainer.text['provisioningMoreActionsMenuLabel'] }</a></strong></td>
          <td style="padding-left: 0px;" id="groupProvisioningSummaryCellId">

          </td>
        </tr>
        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.canUpdate}">
          <c:if test="${grouperRequestContainer.groupSummaryContainer.attestation}">
            <!-- ATTESTATION -->
            <tr>
              <td style="vertical-align: top;"><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Attestation.groupAttestation&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['attestationButton'] }</a></strong></td>
              <td style="padding-left: 0px;">
                ${textContainer.text['groupSummaryPageGroupAttestationMessage']}
              </td>
            </tr>
          </c:if>
        </c:if>
        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.canGroupAttrRead}">
          <!-- ATTRIBUTES -->
          <tr style="display: none" id="groupAttributesSummaryRowId">
            <td style="vertical-align: top;"><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2GroupAttributeAssignment.viewAttributeAssignments&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                >${textContainer.text['subjectAttributeAssignmentsButton'] }</a></strong></td>
            <td style="padding-left: 0px;" id="groupAttributesSummaryCellId">
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.canAdmin}">
          <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCount > 0 or grouperRequestContainer.groupSummaryContainer.rulesCountWhereGroupIsUsed > 0}">
            <!-- RULES -->
            <tr>
              <td style="vertical-align: top;"><strong>
              <a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.viewGroupRules&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                >${textContainer.text['stemViewRulesButton'] }</a></strong></td>
              <td style="padding-left: 0px;">
                <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCount > 0}">
                  ${textContainer.text['groupSummaryPageRulesAssignedMessage']}
                </c:if>
                
                <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCountWhereGroupIsUsed > 0}">
                  ${textContainer.text['groupSummaryPageGroupUsedInRulesMessage']}
                </c:if>
              </td>
            </tr>
          </c:if>
        </c:if>
        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.canRead}">
          <tr style="display: none" id="groupRecentMembershipsSummaryRowId">
            <!-- RECENT MEMBERSHIP CHANGES -->
            <td style="vertical-align: top;"><strong>${textContainer.text['groupSummaryPageRecentMembershipChangesLabel']}</strong></td>
            <td style="padding-left: 0px;" id="groupRecentMembershipsSummaryCellId">
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.canAdmin}">
          <tr style="display: none" id="groupAuditsSummaryRowId">
            <!-- RECENT AUDITS -->
            <td style="vertical-align: top;"><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.viewAudits&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&auditType=group'); return false;"
                              >${textContainer.text['groupSummaryPageRecentAuditsLabel']}</a></strong></td>
            <td style="padding-left: 0px;" id="groupAuditsSummaryCellId">
            </td>
          </tr>
        </c:if>
        <tr style="display: none" id="groupConfigurationSummaryRowId">
          <!-- CONFIGURATION -->
          <td style="vertical-align: top;"><strong>${textContainer.text['groupSummaryPageConfigurationLabel']}</strong></td>
          <td style="padding-left: 0px;" id="groupConfigurationSummaryCellId">
              
          </td>
        </tr>
        <!-- colspan across for next title -->
        <tr class="grouperIgnoreStripe"><td colspan="2" style="background-color: white"><p style="display: block; margin-top: 0;" /><h3><p 
          class="lead">${textContainer.text['groupSummaryFields'] }</p></h3><p style="display: block; margin-top: 0;" /></td></tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelName" /></strong></td>
          <td style="padding-left: 0px;">${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelPath" /></strong></td>
          <td style="padding-left: 0px;">${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayName)}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelIdPath" /></strong></td>
          <td style="padding-left: 0px;"><span id="groupSummaryIdPath">${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.name)}</span> <a href="#" onclick="grouperCopyToClipboard('groupSummaryIdPath'); return false;" title="${textContainer.text['copyToClipboardTooltip']}" style="margin-left:4px;"><i class="fa fa-clone"></i></a></td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelAlternateIdPath" /></strong></td>
          <td style="padding-left: 0px;">${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.alternateName)}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelId" /></strong></td>
          <td style="padding-left: 0px;">${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.extension)}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelCreated" /></strong></td>
          <td style="padding-left: 0px;">${grouperRequestContainer.groupContainer.guiGroup.createdString}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelCreator" /></strong></td>
          <td style="padding-left: 0px;">${grouper:subjectStringLabelShortWithIconFromMemberId(grouperRequestContainer.groupContainer.guiGroup.group.creatorUuid)}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelLastEdited" /></strong></td>
          <td style="padding-left: 0px;">${grouperRequestContainer.groupContainer.guiGroup.lastEditedString}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelLastEditedBy" /></strong></td>
          <td style="padding-left: 0px;">${grouper:subjectStringLabelShortWithIconFromMemberId(grouperRequestContainer.groupContainer.guiGroup.group.modifierUuid)}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelTypeLabel" /></strong></td>
          <td style="padding-left: 0px;">${textContainer.text[grouper:concat2('groupLabelType_', grouperRequestContainer.groupContainer.guiGroup.group.typeOfGroup)]}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelIdIndex" /></strong></td>
          <td style="padding-left: 0px;">${grouperRequestContainer.groupContainer.guiGroup.group.idIndex}</td>
        </tr>
        <tr>
          <td style="vertical-align: top;"><strong><grouper:message key="groupLabelUuid" /></strong></td>
          <td style="padding-left: 0px;">${grouperRequestContainer.groupContainer.guiGroup.group.uuid}</td>
        </tr>
      </tbody>
    </table>
  </div>
  
</section>
