
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
                <c:forEach var="guiConfiguredGrouperObjectTypesAttributeValue" items="${grouperRequestContainer.objectTypeContainer.guiConfiguredGrouperObjectTypesAttributeValues}">
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
                   <li>${grouperRequestContainer.groupSummaryContainer.notGroupMembersCount} non-group members</li>
                   <li>${grouperRequestContainer.groupSummaryContainer.totalMembersCount} total members</li>
                   <li>${grouperRequestContainer.groupSummaryContainer.directMembersCount} direct members</li>
                   <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.directGroupMembers}">
                      <li>Direct group members: 
                        <c:forEach var="directGroupMember" items="${grouperRequestContainer.groupSummaryContainer.directGroupMembers}">
                          ${directGroupMember.name},
                        </c:forEach>
                      </li>
                   </c:if>
                </c:when>
                <c:otherwise>
                  <li>none</li>
                </c:otherwise>
              </c:choose>
              <c:choose>
                
                <c:when test="${grouperRequestContainer.groupSummaryContainer.groupAsMemberCount > 0}">
                   <li>This group is used in ${grouperRequestContainer.groupSummaryContainer.groupAsMemberCount} other groups
                   <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.groupsWhereTheCurrentGroupIsMemberOf}"> 
                        <c:forEach var="groupWhereTheCurrentGroupIsMemberOf" items="${grouperRequestContainer.groupSummaryContainer.groupsWhereTheCurrentGroupIsMemberOf}">
                          ${groupWhereTheCurrentGroupIsMemberOf.name},
                        </c:forEach>
                   </c:if>
                   </li>
                </c:when>
                <c:otherwise>
                  <li>Not a member of any other groups</li>
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
                   <li>${grouperRequestContainer.groupSummaryContainer.nonGroupTotalPrivilegesCount} non-group privileges</li>
                   <li>${grouperRequestContainer.groupSummaryContainer.totalPrivilegesCount} total privileges</li>
                   <li>${grouperRequestContainer.groupSummaryContainer.directPrivilegesCount} direct privileges</li>
                   <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.directGroupPrivilegesGroups}">
                      <li>Direct group privileges: 
                        <c:forEach var="directGroupPrivilegesGroup" items="${grouperRequestContainer.groupSummaryContainer.directGroupPrivilegesGroups}">
                          ${directGroupPrivilegesGroup.name},
                        </c:forEach>
                      </li>
                   </c:if>
                </c:when>
                <c:otherwise>
                  <li>none</li>
                </c:otherwise>
              </c:choose>
              
              <c:choose>
                <c:when test="${grouperRequestContainer.groupSummaryContainer.countOfWhereGroupIsBeingUsedInPrivileges > 0}">
                   <li>This group is used in ${grouperRequestContainer.groupSummaryContainer.countOfWhereGroupIsBeingUsedInPrivileges} other groups
                   <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.groupsWhereGroupIsBeingUsedInPrivileges}"> 
                        <c:forEach var="groupWhereGroupIsBeingUsedInPrivileges" items="${grouperRequestContainer.groupSummaryContainer.groupsWhereGroupIsBeingUsedInPrivileges}">
                          ${groupWhereGroupIsBeingUsedInPrivileges.name},
                        </c:forEach>
                   </c:if>
                   </li>
                </c:when>
                <c:otherwise>
                  <li>Not in privileges of any other groups</li>
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
                  <li>SQL loaded group</li>
                  <li>${grouperRequestContainer.grouperLoaderContainer.sqlLoaderType} loader</li>
                  <li>
                  <c:if test="${!grouper.isBlank(grouperRequestContainer.grouperLoaderContainer.sqlQuery)}">
                    <pre>${grouperRequestContainer.grouperLoaderContainer.sqlQuery}</pre>
                  </c:if>
                  </li>
                </c:if>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperLdapLoader}">
                  <li>LDAP loaded group</li>
                  <li>${grouperRequestContainer.grouperLoaderContainer.ldapLoaderType} loader</li>
                  <c:if test="${!grouper.isBlank(grouperRequestContainer.grouperLoaderContainer.ldapLoaderFilter)}">
                    <pre>${grouperRequestContainer.grouperLoaderContainer.ldapLoaderFilter}</pre>
                  </c:if> 
                </c:if>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperJexlScriptLoader}">
                  <li>Jexl scripted loaded group</li>
                  <c:if test="${!grouper.isBlank(grouperRequestContainer.grouperLoaderContainer.jexlScriptJexlScript)}">
                    <pre>${grouperRequestContainer.grouperLoaderContainer.jexlScriptJexlScript}</pre>
                  </c:if> 
                </c:if>
                <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}">
                  <li>Recent memberships loaded group</li>
                </c:if>
              </ul>
            </td>
          </tr>
        </c:if>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependenciesCount > 0}">
          <!--  ABAC SCRIPTED GROUP SECTION -->
          <tr>
            <td><strong>ABAC scripted group</strong></td>
            <td>
              <ul>
                  <li>Used in ${grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependenciesCount} groups 
                  <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependencies}"> 
                      <c:forEach var="abacScriptedGroupDependency" items="${grouperRequestContainer.groupSummaryContainer.abacScriptedGroupDependencies}">
                        ${abacScriptedGroupDependency.name},
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
            <td><strong>Composites</strong></td>
            <td>
             <ul>
              <c:if test="${grouperRequestContainer.groupSummaryContainer.composite}">
               <li>
                This group is a composite owner of ${grouperRequestContainer.groupSummaryContainer.compositeLeftGroup.name} {grouperRequestContainer.groupSummaryContainer.compositeType} {grouperRequestContainer.groupSummaryContainer.compositeRightGroup.name}
                </li>
              </c:if>
              
              <c:if test="${grouperRequestContainer.groupSummaryContainer.compositeSize > 0}">
               <li>
                This group is a composite factor in ${grouperRequestContainer.groupSummaryContainer.compositeSize} other groups
                <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.composites}"> 
                    <c:forEach var="composite" items="${grouperRequestContainer.groupSummaryContainer.composites}">
                      ${composite.ownerGroup.name},
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
                  This group is provisioned to ${grouperRequestContainer.groupSummaryContainer.provisioningAssignmentCount} targets
                  <c:if test="${grouperRequestContainer.groupSummaryContainer.compositeSize > 0}">
                  This group is a composite factor in ${grouperRequestContainer.groupSummaryContainer.compositeSize} other groups
                  <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.guiGrouperProvisioningAttributeValues}"> 
                      <c:forEach var="guiGrouperProvisioningAttributeValue" items="${grouperRequestContainer.groupSummaryContainer.guiGrouperProvisioningAttributeValues}">
                        ${guiGrouperProvisioningAttributeValue.externalizedName},
                      </c:forEach>
                  </c:if>
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
                Attestation is assigned on this group, last attested on ${grouperRequestContainer.groupSummaryContainer.attestationDateCertified}
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
                  There are ${grouperRequestContainer.groupSummaryContainer.attributeAssignmentsCount} attributes assigned to this group
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
                  There are ${grouperRequestContainer.groupSummaryContainer.rulesCount} rules assigned to this group
                  </li>
                </c:if>
                
                <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCountWhereGroupIsUsed > 0}">
                 <li>
                  This group is used in ${grouperRequestContainer.groupSummaryContainer.rulesCountWhereGroupIsUsed} rules assigned to other groups/folders
                 </li>
                </c:if>
              </ul>
            </td>
          </tr>
        </c:if>
        <tr>
          <!-- RECENT MEMBERSHIP CHANGES -->
          <td><strong>Recent membership changes</strong></td>
          <td>
           <ul>
              <c:choose>
                <c:when test="${grouperRequestContainer.groupSummaryContainer.newMembershipsInTheLastMonth > 0 or grouperRequestContainer.groupSummaryContainer.membershipsRemovedInTheLastMonth > 0}">
                  <li>
                  There are ${grouperRequestContainer.groupSummaryContainer.newMembershipsInTheLastMonth} new memberships and ${grouperRequestContainer.groupSummaryContainer.membershipsRemovedInTheLastMonth} removed memberships in the last month
                  </li>
                </c:when>
                <c:otherwise>
                  <li>There are no recent membership changes to this group</li>
                </c:otherwise>
              </c:choose>
            </ul>
          </td>
        </tr>
        <tr>
          <!-- RECENT AUDITS -->
          <td><strong>Recent audits</strong></td>
          <td>
            <ul>
              <c:choose>
                <c:when test="${grouperRequestContainer.groupSummaryContainer.auditsInTheLastMonth > 0}">
                  <li>
                  There are ${grouperRequestContainer.groupSummaryContainer.auditsInTheLastMonth} audit entries for this group in the last month
                  </li>
                </c:when>
                <c:otherwise>
                  <li>There are no recent audits to this group</li>
                </c:otherwise>
              </c:choose>
            </ul>
          </td>
        </tr>
        <c:if test="${grouperRequestContainer.groupSummaryContainer.configurationUsedCount > 0}">
        <tr>
          <!-- CONFIGURATION -->
          <td><strong>Configuration</strong></td>
          <td>
            <ul>
             <li>
              Used in ${grouperRequestContainer.groupSummaryContainer.configurationUsedCount} configurations 
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
