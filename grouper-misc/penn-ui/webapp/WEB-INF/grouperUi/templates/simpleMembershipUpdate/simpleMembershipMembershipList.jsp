<%@ include file="../common/commonTaglib.jsp" %>
<div class="section">
  <grouperGui:subtitle key="simpleMembershipUpdate.membershipListSubtitle"/>
<div class="sectionBody"><br />

  <div class="pagingSummary">
    <grouperGui:message key="simpleMembershipUpdate.pagingLabelPrefix" />
    <grouperGui:paging refreshOperation="SimpleMembershipUpdate.retrieveMembers" 
      showSummaryOrButtons="true" pagingName="simpleMemberUpdateMembers"  />
  </div>
    
  <c:if test="${fn:length(simpleMembershipUpdateContainer.guiMembers) == 0}">
    <grouperGui:message key="simpleMembershipUpdate.noMembersFound" />
  
  </c:if>
  <form action="whatever" id="simpleMembershipUpdateDeleteMultipleForm" name="simpleMembershipUpdateDeleteMultipleForm">
    <c:forEach items="${simpleMembershipUpdateContainer.guiMembers}" var="guiMember">
  
      <div class="memberLink"><%-- input type="checkbox"    / --%> 
        <%-- checkbox for delete multiple --%>
        <span class="shows_simpleMembershipUpdateDeleteMultiple" 
            style="${grouperGui:hideShowStyle('simpleMembershipUpdateDeleteMultiple', true)}">
          <input type="checkbox" name="deleteMultiple_${guiMember.member.uuid}"  />
        </span>    
        <%-- image button to delete a member --%>
        <span class="hides_simpleMembershipUpdateDeleteMultiple" 
            style="${grouperGui:hideShowStyle('simpleMembershipUpdateDeleteMultiple', false)}">
          <a href="#" onclick="if (confirm('${grouperGui:message('simpleMembershipUpdate.deleteConfirm', true, true) }')) {ajax('SimpleMembershipUpdate.deleteSingle?memberId=${guiMember.member.uuid}');} return false;" 
          ><img src="../public/assets/images/page_cross.gif" height="14px" border="0" 
          alt="${grouperGui:message('simpleMembershipUpdate.deleteImageAlt', true, true) }"/></a>
        </span>
        &nbsp;
        <grouperGui:subjectIcon guiSubject="${guiMember.guiSubject}" /> 
        <span class="simpleMembershipUpdateMemberDescription">${fn:escapeXml(guiMember.guiSubject.screenLabel)}</span>
      </div> 
  
    </c:forEach>
  </form>
  <div class="buttonRow shows_simpleMembershipUpdateDeleteMultiple"  
      style="${grouperGui:hideShowStyle('simpleMembershipUpdateDeleteMultiple', true)}">
      <br />
    <input class="blueButton" type="submit" 
      onclick="ajax('SimpleMembershipUpdate.deleteMultiple', {formIds: 'simpleMembershipUpdateDeleteMultipleForm'}); return false;" 
      value="${grouperGui:message('simpleMembershipUpdate.deleteMultipleButton', true, false) }" 
      onmouseover="Tip('${grouperGui:message('deleteMultipleTooltip', true, false)}')" onmouseout="UnTip()" />
    &nbsp;
    <input class="blueButton" type="submit" 
      onclick="ajax('SimpleMembershipUpdate.deleteAll'); return false;" 
      value="${grouperGui:message('simpleMembershipUpdate.deleteAllButton', true, false) }" 
      onmouseover="Tip('${grouperGui:message('deleteAllTooltip', true, false)}')" onmouseout="UnTip()" />    
  </div>
  <div class="pagingButtons">
    <grouperGui:message key="simpleMembershipUpdate.pagingResultPrefix" />
      <grouperGui:paging showSummaryOrButtons="false" pagingName="simpleMemberUpdateMembers" 
      refreshOperation="SimpleMembershipUpdate.retrieveMembers" />
  </div>
<br />
</div>
</div>