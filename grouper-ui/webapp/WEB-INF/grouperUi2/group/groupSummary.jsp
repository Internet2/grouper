
<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<!-- Optional: reuse your existing CSS; these are only helpers -->
<style>
  .grouper-summary h3 { margin:18px 0 6px; font-size:14px; font-weight:600; }
  .summary-table { width:100%; border-collapse:collapse; margin-bottom:10px; }
  .summary-table th { text-align:left; padding:8px 10px; font-weight:600; background:#f7f9fb; }
  .summary-table td { padding:8px 10px; }
  .summary-table tr + tr th, .summary-table tr + tr td { border-top:1px solid #e7edf3; }
  .num { text-align:right; font-variant-numeric: tabular-nums; }
  .hint { font-size:11px; color:#6b7280; font-weight:400; }
  .muted { color:#6b7280; }
  .help { font-size:12px; color:#64748b; cursor:help; margin-left:4px; }
  .status { font-weight:600; }
  .status-ok { color:#0f766e; }
  .status-none { color:#6b7280; }
  .inline-list a { display:inline-block; margin-right:8px; }
</style>

<section class="grouper-summary">
  
  <!-- Types -->
  <c:if test="${not empty grouperRequestContainer.objectTypeContainer.guiConfiguredGrouperObjectTypesAttributeValues}">
  <h3>
    Types
  </h3>
  <ul>
  
  <c:forEach var="guiConfiguredGrouperObjectTypesAttributeValue" items="${grouperRequestContainer.objectTypeContainer.guiConfiguredGrouperObjectTypesAttributeValues}">
    <li>
      <b>
      ${guiConfiguredGrouperObjectTypesAttributeValue.grouperObjectTypesAttributeValue.objectTypeName}
      </b>
      ${textContainer.text[guiConfiguredGrouperObjectTypesAttributeValue.objectTypeDescriptionKey]}
    </li>
  </c:forEach>
  
  </ul>
  </c:if>
  
  <!-- MEMBERSHIP -->
  <h3>
    Memberships
  </h3>
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
      <li>This group is not used in any other groups</li>
    </c:otherwise>
  </c:choose>
  
  </ul>
  
<!--  LOADER -->
<c:if test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup}">
  <h3>
    Loader
  </h3>
  <ul>
      <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperSqlLoader}">
        <li>This is a SQL loaded group</li>
        <li> This is a ${grouperRequestContainer.grouperLoaderContainer.sqlLoaderType} loader</li>
      </c:if>
      <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperLdapLoader}">
        <li>This is a LDAP loaded group</li>
        <li> This is a ${grouperRequestContainer.grouperLoaderContainer.ldapLoaderType} loader</li>
      </c:if>
      <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperJexlScriptLoader}">
        <li>This is a Jexl scripted loaded group</li>
      </c:if>
      <c:if test="${grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}">
        <li>This is a recent memberships loaded group</li>
      </c:if>
  </ul>
</c:if>

  <!-- COMPOSITES -->
  <c:if test="${grouperRequestContainer.groupSummaryContainer.composite or grouperRequestContainer.groupSummaryContainer.compositeSize > 0}">
  <h3>
    Composites
  </h3>
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
  
  </c:if>
  
  <!-- PROVISIONING -->
 <c:if test="${grouperRequestContainer.groupSummaryContainer.provisioningAssignmentCount > 0}">
  <h3>
    Provisioning
  </h3>
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
  
</c:if>
  
  
 <!-- ATTESTATION -->
 <c:if test="${grouperRequestContainer.groupSummaryContainer.attestation}">
  <h3>
    Attestation
  </h3>
  <ul>
  <li>
    Attestation is assigned on this group, last attested on ${grouperRequestContainer.groupSummaryContainer.attestationDateCertified}
  </li>
 </ul>
</c:if>

<!-- ATTRIBUTES -->
 <c:if test="${grouperRequestContainer.groupSummaryContainer.attributeAssignmentsCount > 0}">
  <h3>
    Attributes
  </h3>
  <ul>
  <li>
    There are ${grouperRequestContainer.groupSummaryContainer.attributeAssignmentsCount} attributes assigned to this group
  </li>
 </ul>
</c:if>

<!-- RULES -->
 <c:if test="${grouperRequestContainer.groupSummaryContainer.rulesCount > 0 or grouperRequestContainer.groupSummaryContainer.rulesCountWhereGroupIsUsed > 0}">
  <h3>
    Rules
  </h3>
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
  
  </c:if>
  
<!-- RECENT MEMBERSHIP CHANGES -->
  <h3>
    Recent membership changes
  </h3>
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
  
<!-- RECENT AUDITS -->
  <h3>
    Recent audits
  </h3>
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
  
</section>
