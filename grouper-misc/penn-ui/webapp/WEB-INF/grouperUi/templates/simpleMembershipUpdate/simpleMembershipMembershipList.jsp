<%@ include file="../common/commonTaglib.jsp" %>
<div class="section">
  <grouperGui:subtitle key="simpleMembershipUpdate.membershipListSubtitle"/>
<div class="sectionBody"><br />

  <div class="pagingSummary">
    <grouperGui:message key="simpleMembershipUpdate.pagingLabelPrefix" />
    <grouperGui:paging showSummaryOrButtons="true" pagingName="simpleMemberUpdateMembers"  />
  </div>
    
  <c:if test="${fn:length(simpleMembershipUpdateContainer.guiMembers) == 0}">
    <grouperGui:message key="simpleMembershipUpdate.noMembersFound" />
  
  </c:if>
  
  <c:forEach items="${simpleMembershipUpdateContainer.guiMembers}" var="guiMember">

    <div class="memberLink"><%-- input type="checkbox"    / --%> 
    <a href="#" onclick="if (confirm('${grouperGui:message('simpleMembershipUpdate.deleteConfirm', true, true) }')) {ajax('SimpleMembershipUpdate.deleteSingle?memberId=${guiMember.member.uuid}');} return false;" 
    ><img src="../public/assets/page_cross.gif" height="14px" border="0" 
    alt="${grouperGui:message('simpleMembershipUpdate.deleteImageAlt', true, true) }"/></a>
    &nbsp;
    <grouperGui:subjectIcon guiSubject="${guiMember.guiSubject}" /> 
    <span class="simpleMembershipUpdateMemberDescription">${fn:escapeXml(guiMember.guiSubject.screenLabel)}</span></div> 

  </c:forEach>
  <div class="pagingButtons">
    <grouperGui:message key="simpleMembershipUpdate.pagingResultPrefix" />
      <grouperGui:paging showSummaryOrButtons="false" pagingName="simpleMemberUpdateMembers" 
      refreshOperation="SimpleMembershipUpdate.retrieveMembers" />
  </div>
<br />
</div>
</div>