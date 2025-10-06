
<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<p class="lead">${textContainer.text['groupSummaryDecription'] }
&nbsp;
<span id="groupSummaryMoreId" style="font-size: 0.65em; font-weight: 400"><a href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGroupDetails']}"
   onclick="$('#groupSummaryMoreId').hide('slow'); ajax('../app/UiV2Group.viewGroupSummaryMore?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
   >${textContainer.text['guiGroupSummaryMore']} <i class="fa fa-angle-down"></i></a></span>
</p>

<section class="grouper-summary">

  
  <div id="groupDetailsId">
    <table class="table table-condensed" id="groupDetailsTableId">
      <tbody>
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
        <c:if test="${grouperRequestContainer.groupSummaryContainer.canRead}">
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
              ${textContainer.text['groupSummaryPageAbacScriptedGroupDependenciesCountMessage']} <br /><br />
              <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependencies}"> 
                  <c:forEach var="abacScriptedGroupDependency" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependencies}">
                    <li style="margin-left: 12px;">${abacScriptedGroupDependency.shortLinkWithIcon}
                    <c:if test="${!status.last}">,</c:if></li>
                  </c:forEach>
              </c:if>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.composite or grouperRequestContainer.groupSummaryContainer.compositeSize > 0}">
          <!-- COMPOSITES -->
          <tr>
            <td style="vertical-align: top;"><strong>${textContainer.text['groupComposites']}</strong></td>
            <td style="padding-left: 0px;">
              <c:if test="${grouperRequestContainer.groupSummaryContainer.composite}">
               <li style="margin-left: 12px;">
                ${textContainer.text['groupSummaryPageCompositeGroupMessage']}
                </li>
              </c:if>
              
              <c:if test="${grouperRequestContainer.groupSummaryContainer.compositeSize > 0}">
               <li style="margin-left: 12px;">
                ${textContainer.text['groupSummaryPageCompositeFactorMessage']}
                <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.composites}"> 
                    <c:forEach var="composite" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.composites}">
                      ${composite.shortLinkWithIcon}
                      <c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                </c:if>
               </li>
              </c:if>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.provisioningAssignmentCount > 0}">
          <!-- PROVISIONING -->
          <tr>
            <td style="vertical-align: top;"><strong><a href="javascript:void(0)" id="groupMoreActionsProvisioningButtonId" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['provisioningMoreActionsMenuLabel'] }</a></strong></td>
            <td style="padding-left: 0px;">
              <li style="margin-left: 12px;">
                ${textContainer.text['groupSummaryPageProvisionedTargetMessage']}
                <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.guiGrouperProvisioningAttributeValues}"> 
                  <c:forEach var="guiGrouperProvisioningAttributeValue" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.guiGrouperProvisioningAttributeValues}">
                    ${guiGrouperProvisioningAttributeValue.externalizedName}
                    <c:if test="${!status.last}">,</c:if>
                  </c:forEach>
                </c:if>
              </li>
            </td>
          </tr>
        </c:if>
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
        <c:if test="${grouperRequestContainer.groupSummaryContainer.attributeAssignmentsCount > 0}">
          <!-- ATTRIBUTES -->
          <tr>
            <td style="vertical-align: top;"><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2GroupAttributeAssignment.viewAttributeAssignments&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                >${textContainer.text['subjectAttributeAssignmentsButton'] }</a></strong></td>
            <td style="padding-left: 0px;">
              ${textContainer.text['groupSummaryPageCustomAttributesAssignedMessage']}
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCount > 0 or grouperRequestContainer.groupSummaryContainer.rulesCountWhereGroupIsUsed > 0}">
          <!-- RULES -->
          <tr>
            <td style="vertical-align: top;"><strong>
            <a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.viewGroupRules&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
              >${textContainer.text['stemViewRulesButton'] }</a></strong></td>
            <td>
              <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCount > 0}">
                ${textContainer.text['groupSummaryPageRulesAssignedMessage']}
              </c:if>
              
              <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCountWhereGroupIsUsed > 0}">
                ${textContainer.text['groupSummaryPageGroupUsedInRulesMessage']}
              </c:if>
            </td>
          </tr>
        </c:if>
        <tr>
          <!-- RECENT MEMBERSHIP CHANGES -->
          <td style="vertical-align: top;"><strong>${textContainer.text['groupSummaryPageRecentMembershipChangesLabel']}</strong></td>
          <td style="padding-left: 0px;">
            <c:choose>
              <c:when test="${grouperRequestContainer.groupSummaryContainer.newMembershipsInTheLastMonth > 0 or grouperRequestContainer.groupSummaryContainer.membershipsRemovedInTheLastMonth > 0}">
                ${textContainer.text['groupSummaryPageRecentMembershipChangesMessage']}
              </c:when>
              <c:otherwise>
                ${textContainer.text['groupSummaryPageNoRecentMembershipChangesMessage']}
              </c:otherwise>
            </c:choose>
          </td>
        </tr>
        <tr>
          <!-- RECENT AUDITS -->
          <td style="vertical-align: top;"><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.viewAudits&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&auditType=group'); return false;"
                            >${textContainer.text['groupSummaryPageRecentAuditsLabel']}</a></strong></td>
          <td style="padding-left: 0px;">
            <c:choose>
              <c:when test="${grouperRequestContainer.groupSummaryContainer.auditsInTheLastMonth > 0}">
                ${textContainer.text['groupSummaryPageRecentAuditsMessage']}
              </c:when>
              <c:otherwise>
                ${textContainer.text['groupSummaryPageNoRecentAuditsMessage']}
              </c:otherwise>
            </c:choose>
          </td>
        </tr>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.configurationUsedCount > 0}">
        <tr>
          <!-- CONFIGURATION -->
          <td style="vertical-align: top;"><strong>${textContainer.text['groupSummaryPageConfigurationLabel']}</strong></td>
          <td style="padding-left: 0px;">
              ${textContainer.text['groupSummaryPageConfigurationMessage']} 
          </td>
        </tr>
        </c:if>
        <!-- colspan across for next title -->
        <tr><td colspan="2" style="background-color: white"><br /><h3><p class="lead">${textContainer.text['groupSummaryFields'] }</p></h3><br /></td></tr>
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
          <td style="padding-left: 0px;">${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.name)}</td>
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
