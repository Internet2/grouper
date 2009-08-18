<%@ include file="../common/commonTaglib.jsp" %>
<!-- Start: $Id: simpleMembershipMembershipList.jsp,v 1.9 2009-08-18 13:08:36 mchyzer Exp $ -->
<%-- this is the section of the screen which shows a box and the member list inside --%>
<div class="section">
  <grouperGui:subtitle key="simpleMembershipUpdate.membershipListSubtitle"/>
<div class="sectionBody"><br />

  <%-- if showing member filter, then show the combobox --%>
  <div class="shows_simpleMembershipUpdateMemberFilter"  
      style="${grouperGui:hideShowStyle('simpleMembershipUpdateMemberFilter', true)}">
    
    <form id="simpleMembershipMemberFilterForm" name="simpleMembershipUpdateAddMemberForm" action="whatever">
    <%-- describe the combobox, since it doesnt look like something you would type in to --%>
    <div class="combohint"><grouperGui:message key="simpleMembershipUpdate.filterMemberCombohint"/></div>
    <%-- note, the combobox does not currently auto adjust its width, so just make it really wide --%>
    <table width="900" cellpadding="0" cellspacing="0">
      <tr valign="top">
        <td style="padding: 0px" width="710">
          <%-- show the combobox --%>
          <grouperGui:combobox filterOperation="SimpleMembershipUpdate.filterMembers" id="simpleMembershipFilterMember" 
            width="700"/>
        </td>
        <td>
          <%-- add member button --%>
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleMembershipUpdate.retrieveMembers', {formIds: 'simpleMembershipMemberFilterForm'}); return false;" 
          value="${grouperGui:message('simpleMembershipUpdate.filterMemberButton', true, false) }" style="margin-top: 2px" />
        </td>
      </tr>
    </table>
    </form>
    <br />
    
    <%-- if the member filter for screen is not empty, then tell the user what we are filtering on --%>
    <c:if test="${! empty simpleMembershipUpdateContainer.memberFilterForScreen}">
      <span class="simpleMembershipUpdateFilterLabel"><grouperGui:message key="simpleMembershipUpdate.filterLabel" /></span>
      <span class="simpleMembershipUpdateFilterValue"><c:out value="${simpleMembershipUpdateContainer.memberFilterForScreen}" /></span>
      <span class="simpleMembershipUpdateFilterClear"
        ><a href="#" onclick="ajax('SimpleMembershipUpdate.clearMemberFilter'); return false;"
          ><grouperGui:message key="simpleMembershipUpdate.clearFilterButton" /></a></span>
      <br />
    </c:if>

  </div>

  <%-- paging summary shows which records, and page size --%>
  <div class="pagingSummary">
    <grouperGui:message key="simpleMembershipUpdate.pagingLabelPrefix" />
    <grouperGui:paging refreshOperation="SimpleMembershipUpdate.retrieveMembers" 
      showSummaryOrButtons="true" pagingName="simpleMemberUpdateMembers"  />
  </div>
  
  <%-- message if no members --%>  
  <c:if test="${fn:length(simpleMembershipUpdateContainer.guiMembers) == 0}">
    <grouperGui:message key="simpleMembershipUpdate.noMembersFound" />
  
  </c:if>
  
  <%-- form for delete buttons, loop through members --%>
  <form action="whatever" id="simpleMembershipUpdateDeleteMultipleForm" name="simpleMembershipUpdateDeleteMultipleForm">
    <c:forEach items="${simpleMembershipUpdateContainer.guiMembers}" var="guiMember">
  
      <div class="memberLink">
       
        <%-- checkbox for delete multiple, if showing --%>
        <span class="shows_simpleMembershipUpdateDeleteMultiple" 
            style="${grouperGui:hideShowStyle('simpleMembershipUpdateDeleteMultiple', true)}">
          <input type="checkbox" name="deleteMultiple_${guiMember.member.uuid}"  />
        </span>    
        
        <%-- image button to delete a member, if showing --%>
        <span class="hides_simpleMembershipUpdateDeleteMultiple" 
            style="${grouperGui:hideShowStyle('simpleMembershipUpdateDeleteMultiple', false)}">
          <a href="#" onclick="if (confirm('${grouperGui:message('simpleMembershipUpdate.deleteConfirm', true, true) }')) {ajax('SimpleMembershipUpdate.deleteSingle?memberId=${guiMember.member.uuid}');} return false;" 
          ><img src="../public/assets/images/page_cross.gif" height="14px" border="0" 
          alt="${grouperGui:message('simpleMembershipUpdate.deleteImageAlt', true, true) }"/></a>
        </span>
        &nbsp;
        <%-- show an icon for the subject --%>
        <grouperGui:subjectIcon guiSubject="${guiMember.guiSubject}" /> 
        <%-- the screen representation of the subject --%>
        <span class="simpleMembershipUpdateMemberDescription">${fn:escapeXml(guiMember.guiSubject.screenLabel)}</span>
        <%-- show the triangle next to the member for more operations --%>
        <a class="memberMenuButton" href="#"
            ><img src="../public/assets/images/bullet_arrow_down.png" border="0" id="memberMenuButton_${guiMember.member.uuid}"
            alt="${grouperGui:message('simpleMembershipUpdate.memberMenuAlt', true, true) }"/></a>
      </div> 
  
    </c:forEach>
    <%-- member menu div, and attach to buttons --%>
    <grouperGui:menu menuId="memberMenu"
      operation="SimpleMembershipUpdate.memberMenu" 
      structureOperation="SimpleMembershipUpdate.memberMenuStructure" 
      contextZoneJqueryHandle=".memberMenuButton" contextMenu="true" />
  </form>
  <%-- if showing delete multiple, then show buttons for delete selected, and delete all --%>
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
  <%-- show the google like paging buttons at the bottom to pick a page to go to --%>
  <div class="pagingButtons">
    <grouperGui:message key="simpleMembershipUpdate.pagingResultPrefix" />
      <grouperGui:paging showSummaryOrButtons="false" pagingName="simpleMemberUpdateMembers" 
      refreshOperation="SimpleMembershipUpdate.retrieveMembers" />
  </div>
<br />
</div>
</div>
<!-- End: $Id: simpleMembershipMembershipList.jsp,v 1.9 2009-08-18 13:08:36 mchyzer Exp $ -->
