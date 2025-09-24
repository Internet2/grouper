
<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<section class="grouper-summary">

  
  <div id="groupDetailsId">
    <table class="table table-condensed table-striped">
      <tbody>
        <c:if test="${not empty grouperRequestContainer.objectTypeContainer.guiConfiguredGrouperObjectTypesAttributeValues}">
          <!-- Types -->
          <tr>
            <td><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['objectTypeMoreActionsMenuLabel'] }</a></strong></td>
            <td>
              <ul>
                <c:forEach var="guiConfiguredGrouperObjectTypesAttributeValue" varStatus="status" items="${grouperRequestContainer.objectTypeContainer.guiConfiguredGrouperObjectTypesAttributeValues}">
                  <c:set var="objectType" value="${guiConfiguredGrouperObjectTypesAttributeValue.grouperObjectTypesAttributeValue.objectTypeName}" />
                  <li>
                    ${textContainer.text[grouper:concat2('objectTypeOption_',objectType)] }
                  </li>
                </c:forEach>
              </ul>
            </td>
          </tr>
        </c:if>
        <tr>
          <!-- MEMBERSHIP -->
          <td><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.viewGroupMembers&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['subjectMembershipsTab'] }</a></strong></td>
          <td>
             <ul>
              <c:choose>
                
                <c:when test="${grouperRequestContainer.groupSummaryContainer.notGroupMembersCount > 0 or 
                  grouperRequestContainer.groupSummaryContainer.totalMembersCount > 0 or
                  grouperRequestContainer.groupSummaryContainer.directMembersCount > 0
                 }">
                   <li>${grouperRequestContainer.groupSummaryContainer.notGroupMembersCount} ${textContainer.text['groupSummaryPageMembershipsNonGroupMembers'] }</li>
                   <li>${grouperRequestContainer.groupSummaryContainer.totalMembersCount} ${textContainer.text['groupSummaryPageMembershipsTotalMembers'] }</li>
                   <li>${grouperRequestContainer.groupSummaryContainer.directMembersCount} ${textContainer.text['groupSummaryPageMembershipsDirectMembers'] }</li>
                   <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.directGroupMembers}">
                      <li>${textContainer.text['groupSummaryPageMembershipsDirectGroupMembers']} 
                        <c:forEach var="directGroupMember" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.directGroupMembers}">
                          ${directGroupMember.shortLinkWithIcon}
                          <c:if test="${!status.last}">,</c:if>
                        </c:forEach>
                      </li>
                   </c:if>
                </c:when>
                <c:otherwise>
                  <li>${textContainer.text['groupSummaryPageMembershipsNone'] }</li>
                </c:otherwise>
              </c:choose>
              <c:choose>
                
                <c:when test="${grouperRequestContainer.groupSummaryContainer.groupAsMemberCount > 0}">
                   <li>${textContainer.text['groupSummaryPageMembershipsGroupUsedCountMessage'] }
                   <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.groupsWhereTheCurrentGroupIsMemberOf}"> 
                        <c:forEach var="groupWhereTheCurrentGroupIsMemberOf" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.groupsWhereTheCurrentGroupIsMemberOf}">
                          ${groupWhereTheCurrentGroupIsMemberOf.shortLinkWithIcon}
                          <c:if test="${!status.last}">,</c:if>
                        </c:forEach>
                   </c:if>
                   </li>
                </c:when>
                <c:otherwise>
                  <li>${textContainer.text['groupSummaryPageMembershipsGroupNotUsedMessage'] }</li>
                </c:otherwise>
              </c:choose>
            </ul>
          </td>
        </tr>
        <tr>
          <!-- PRIVILEGES -->
          <td><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['groupPrivilegesTab'] }</a></strong></td>
          <td>
            <ul>
              <c:choose>
                <c:when test="${grouperRequestContainer.groupSummaryContainer.nonGroupTotalPrivilegesCount > 0 or 
                  grouperRequestContainer.groupSummaryContainer.totalPrivilegesCount > 0 or
                  grouperRequestContainer.groupSummaryContainer.directPrivilegesCount > 0
                 }">
                   <li>${textContainer.text['groupSummaryPagePrivilegesNotGroupPrivilegesUsedCountMessage']}</li>
                   <li>${textContainer.text['groupSummaryPagePrivilegesTotalGroupPrivilegesUsedCountMessage']}</li>
                   <li>${textContainer.text['groupSummaryPagePrivilegesDirectGroupPrivilegesUsedCountMessage']}</li>
                   <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.directGroupPrivilegesGroups}">
                      <li>${textContainer.text['groupSummaryPagePrivilegesDirectGroupPrivileges']} 
                        <c:forEach var="directGroupPrivilegesGroup" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.directGroupPrivilegesGroups}">
                          ${directGroupPrivilegesGroup.shortLinkWithIcon}
                          <c:if test="${!status.last}">,</c:if>
                        </c:forEach>
                      </li>
                   </c:if>
                </c:when>
                <c:otherwise>
                  <li>${textContainer.text['groupSummaryPageMembershipsGroupNotUsedMessage'] }</li>
                </c:otherwise>
              </c:choose>
              
              <c:choose>
                <c:when test="${grouperRequestContainer.groupSummaryContainer.countOfWhereGroupIsBeingUsedInPrivileges > 0}">
                   <li>${textContainer.text['groupSummaryPagePrivilegesGroupUsedInOtherGroupsPrivileges']}
                   <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.groupsWhereGroupIsBeingUsedInPrivileges}"> 
                        <c:forEach var="groupWhereGroupIsBeingUsedInPrivileges" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.groupsWhereGroupIsBeingUsedInPrivileges}">
                          ${groupWhereGroupIsBeingUsedInPrivileges.shortLinkWithIcon}
                          <c:if test="${!status.last}">,</c:if>
                        </c:forEach>
                   </c:if>
                   </li>
                </c:when>
                <c:otherwise>
                  <li>${textContainer.text['groupSummaryPagePrivilegesGroupNotUsedMessage']}</li>
                </c:otherwise>
              </c:choose>
            </ul>
          </td>
        </tr>
        <c:if test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup}">
        <!--  LOADER -->
          <tr>
            <td><strong>
            <a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2GrouperLoader.loader&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['grouperMenuItemLoader'] }</a>
            </strong></td>
            <td>
              <ul>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperSqlLoader}">
                  <li>${textContainer.text['groupSummaryPageSqlLoadedGroup']}</li>
                  <li>${grouperRequestContainer.grouperLoaderContainer.sqlLoaderType} loader</li>
                  <li>
                  <c:if test="${!grouper.isBlank(grouperRequestContainer.grouperLoaderContainer.sqlQuery)}">
                    <pre>${grouperRequestContainer.grouperLoaderContainer.sqlQuery}</pre>
                  </c:if>
                  </li>
                </c:if>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperLdapLoader}">
                  <li>${textContainer.text['groupSummaryPageLdapLoadedGroup']}</li>
                  <li>${grouperRequestContainer.grouperLoaderContainer.ldapLoaderType} loader</li>
                  <c:if test="${!grouper.isBlank(grouperRequestContainer.grouperLoaderContainer.ldapLoaderFilter)}">
                    <pre>${grouperRequestContainer.grouperLoaderContainer.ldapLoaderFilter}</pre>
                  </c:if> 
                </c:if>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperJexlScriptLoader}">
                  <li>${textContainer.text['groupSummaryPageJexlLoadedGroup']}</li>
                  <c:if test="${!grouper.isBlank(grouperRequestContainer.grouperLoaderContainer.jexlScriptJexlScript)}">
                    <pre>${grouperRequestContainer.grouperLoaderContainer.jexlScriptJexlScript}</pre>
                  </c:if> 
                </c:if>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}">
                  <li>${textContainer.text['groupSummaryPageRecentMembershipsLoadedGroup']}</li>
                </c:if>
              </ul>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependenciesCount > 0}">
          <!--  ABAC SCRIPTED GROUP SECTION -->
          <tr>
            <td><strong>${textContainer.text['groupSummaryPageAbacScriptedGroup']}</strong></td>
            <td>
              <ul>
                  <li>${textContainer.text['groupSummaryPageAbacScriptedGroupDependenciesCountMessage']} 
                  <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependencies}"> 
                      <c:forEach var="abacScriptedGroupDependency" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependencies}">
                        ${abacScriptedGroupDependency.shortLinkWithIcon}
                        <c:if test="${!status.last}">,</c:if>
                      </c:forEach>
                  </c:if>
                  </li>
              </ul>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.composite or grouperRequestContainer.groupSummaryContainer.compositeSize > 0}">
          <!-- COMPOSITES -->
          <tr>
            <td><strong>${textContainer.text['groupComposites']}</strong></td>
            <td>
             <ul>
              <c:if test="${grouperRequestContainer.groupSummaryContainer.composite}">
               <li>
                ${textContainer.text['groupSummaryPageCompositeGroupMessage']}
                </li>
              </c:if>
              
              <c:if test="${grouperRequestContainer.groupSummaryContainer.compositeSize > 0}">
               <li>
                ${textContainer.text['groupSummaryPageCompositeFactorMessage']}
                <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.composites}"> 
                    <c:forEach var="composite" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.composites}">
                      ${composite.shortLinkWithIcon}
                      <c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                </c:if>
               </li>
              </c:if>
             </ul>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.provisioningAssignmentCount > 0}">
          <!-- PROVISIONING -->
          <tr>
            <td><strong><a href="javascript:void(0)" id="groupMoreActionsProvisioningButtonId" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningOnGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['provisioningMoreActionsMenuLabel'] }</a></strong></td>
            <td>
              <ul>
                <li>
                  ${textContainer.text['groupSummaryPageProvisionedTargetMessage']}
                  <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.guiGrouperProvisioningAttributeValues}"> 
                    <c:forEach var="guiGrouperProvisioningAttributeValue" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.guiGrouperProvisioningAttributeValues}">
                      ${guiGrouperProvisioningAttributeValue.externalizedName}
                      <c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                  </c:if>
                </li>
              </ul>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.attestation}">
          <!-- ATTESTATION -->
          <tr>
            <td><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Attestation.groupAttestation&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['attestationButton'] }</a></strong></td>
            <td>
            <ul>
              <li>
                ${textContainer.text['groupSummaryPageGroupAttestationMessage']}
              </li>
            </ul>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.attributeAssignmentsCount > 0}">
          <!-- ATTRIBUTES -->
          <tr>
            <td><strong><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2GroupAttributeAssignment.viewAttributeAssignments&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                >${textContainer.text['subjectAttributeAssignmentsButton'] }</a></strong></td>
            <td>
              <ul>
                <li>
                  ${textContainer.text['groupSummaryPageCustomAttributesAssignedMessage']}
                </li>
              </ul>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCount > 0 or grouperRequestContainer.groupSummaryContainer.rulesCountWhereGroupIsUsed > 0}">
          <!-- RULES -->
          <tr>
            <td><strong>
            <a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Group.viewGroupRules&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
              >${textContainer.text['stemViewRulesButton'] }</a></strong></td>
            <td>
              <ul>
                <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCount > 0}">
                 <li>
                  ${textContainer.text['groupSummaryPageRulesAssignedMessage']}
                  </li>
                </c:if>
                
                <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCountWhereGroupIsUsed > 0}">
                 <li>
                  ${textContainer.text['groupSummaryPageGroupUsedInRulesMessage']}
                 </li>
                </c:if>
              </ul>
            </td>
          </tr>
        </c:if>
        <tr>
          <!-- RECENT MEMBERSHIP CHANGES -->
          <td><strong>${textContainer.text['groupSummaryPageRecentMembershipChangesLabel']}</strong></td>
          <td>
           <ul>
              <c:choose>
                <c:when test="${grouperRequestContainer.groupSummaryContainer.newMembershipsInTheLastMonth > 0 or grouperRequestContainer.groupSummaryContainer.membershipsRemovedInTheLastMonth > 0}">
                  <li>
                  ${textContainer.text['groupSummaryPageRecentMembershipChangesMessage']}
                  </li>
                </c:when>
                <c:otherwise>
                  <li>${textContainer.text['groupSummaryPageNoRecentMembershipChangesMessage']}</li>
                </c:otherwise>
              </c:choose>
            </ul>
          </td>
        </tr>
        <tr>
          <!-- RECENT AUDITS -->
          <td><strong>${textContainer.text['groupSummaryPageRecentAuditsLabel']}</strong></td>
          <td>
            <ul>
              <c:choose>
                <c:when test="${grouperRequestContainer.groupSummaryContainer.auditsInTheLastMonth > 0}">
                  <li>
                  ${textContainer.text['groupSummaryPageRecentAuditsMessage']}
                  </li>
                </c:when>
                <c:otherwise>
                  <li>${textContainer.text['groupSummaryPageNoRecentAuditsMessage']}</li>
                </c:otherwise>
              </c:choose>
            </ul>
          </td>
        </tr>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.configurationUsedCount > 0}">
        <tr>
          <!-- CONFIGURATION -->
          <td><strong>${textContainer.text['groupSummaryPageConfigurationLabel']}</strong></td>
          <td>
            <ul>
             <li>
              ${textContainer.text['groupSummaryPageConfigurationMessage']} 
             </li>
            </ul>
          </td>
        </tr>
        </c:if>
      </tbody>
    </table>
  </div>
  
  
  <!-- FIELDS -->
  <h3>
  Fields
  </h3>
  <div id="groupDetailsId">
    <table class="table table-condensed table-striped">
      <tbody>
        <tr>
          <td><strong>${textContainer.text['groupLabelName']}</strong></td>
          <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelPath']}</strong></td>
          <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayName)}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelIdPath']}</strong></td>
          <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.name)}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelAlternateIdPath']}</strong></td>
          <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.alternateName)}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelId']}</strong></td>
          <td>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.extension)}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelCreated'] }</strong></td>
          <td>${grouperRequestContainer.groupContainer.guiGroup.createdString }</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelCreator'] }</strong></td>
          <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.groupContainer.guiGroup.group.creatorUuid)}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelLastEdited']}</strong></td>
          <td>${grouperRequestContainer.groupContainer.guiGroup.lastEditedString}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelLastEditedBy']}</strong></td>
          <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.groupContainer.guiGroup.group.modifierUuid)}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelTypeLabel']}</strong></td>
          <td>${textContainer.text[grouper:concat2('groupLabelType_',grouperRequestContainer.groupContainer.guiGroup.group.typeOfGroup)]}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelIdIndex']}</strong></td>
          <td>${grouperRequestContainer.groupContainer.guiGroup.group.idIndex}</td>
        </tr>
        <tr>
          <td><strong>${textContainer.text['groupLabelUuid']}</strong></td>
          <td>${grouperRequestContainer.groupContainer.guiGroup.group.uuid}</td>
        </tr>
      </tbody>
    </table>
  </div>
  
</section>
