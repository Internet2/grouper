<%@ include file="../common/commonTaglib.jsp" %>
<!-- Start: $Id: simpleMembershipMembershipList.jsp,v 1.3 2009-11-02 08:50:40 mchyzer Exp $ -->
<%-- this is the section of the screen which shows a box and the member list inside --%>
<div class="section" style="min-width: 750px">
  <grouper:subtitle label="${simpleMembershipUpdateContainer.text.membershipListSubtitle}" />
<div class="sectionBody"><br />

  <%-- if showing member filter, then show the combobox --%>
  <div class="shows_simpleMembershipUpdateMemberFilter"  
      style="${grouper:hideShowStyle('simpleMembershipUpdateMemberFilter', true)}">
    
    <form id="simpleMembershipMemberFilterForm" name="simpleMembershipUpdateAddMemberForm" action="whatever">
    <%-- describe the combobox, since it doesnt look like something you would type in to --%>
    <div class="combohint"><grouper:message value="${simpleMembershipUpdateContainer.text.filterMemberCombohint}"/></div>
    <%-- note, the combobox does not currently auto adjust its width, so just make it really wide --%>
      <%-- show the combobox --%>
      <grouper:combobox filterOperation="SimpleMembershipUpdateFilter.filterMembers" id="simpleMembershipFilterMember" 
        width="700"/>

       <div style="margin-top: 5px;">
          <%-- add member button --%>
          <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleMembershipUpdateFilter.retrieveMembersFilterButton', {formIds: 'simpleMembershipMemberFilterForm'}); return false;" 
          value="${simpleMembershipUpdateContainer.text.filterMemberButton}" style="margin-top: 2px" />
      </div>
    </form>
    <br />
    
    <%-- if the member filter for screen is not empty, then tell the user what we are filtering on --%>
    <c:if test="${! empty simpleMembershipUpdateContainer.memberFilterForScreen}">
      <table class="formTable" cellspacing="2">
        <tbody>
          <tr class="formTableRow">
            <td class="formTableLeft"><grouper:message value="${simpleMembershipUpdateContainer.text.filterLabel}" /></td>
      
            <td class="formTableRight" style="white-space: nowrap;"><c:out value="${simpleMembershipUpdateContainer.memberFilterForScreen}" />
              &nbsp; &nbsp; <span class="simpleMembershipUpdateFilterClear"
              ><a href="#" class="smallLink" onclick="ajax('SimpleMembershipUpdateFilter.clearMemberFilter'); return false;"
                ><grouper:message value="${simpleMembershipUpdateContainer.text.clearFilterButton}" /></a></span>
            </td>
          </tr>
      </tbody>
      </table>

      <br /><br />
    </c:if>

  </div>

  <%-- paging summary shows which records, and page size --%>
  <div class="pagingSummary">
    <grouper:message value="${simpleMembershipUpdateContainer.text.pagingLabelPrefix}" />
    <grouper:paging refreshOperation="SimpleMembershipUpdate.retrieveMembers" 
      showSummaryOrButtons="true" pagingName="simpleMemberUpdateMembers"  />
  </div>
  
  <%-- form for sort options --%>
  <c:if test="${! empty simpleMembershipUpdateContainer.memberSortIndexSelection}">

    <%-- note that this only works if member searching is enabled if the user is performing a search --%>
    <c:if test="${empty simpleMembershipUpdateContainer.memberFilterForScreen || ! empty simpleMembershipUpdateContainer.searchStringEnum}">
      <c:set var="selectedSortStringIndex" value="${simpleMembershipUpdateContainer.selectedSortStringEnum.index}" />
      <grouper:message value="${simpleMembershipUpdateContainer.text.sortBy}" />
      <form action="whatever" id="simpleMembershipUpdateMemberSortForm" name="simpleMembershipUpdateMemberSortForm">
        <select name="memberSortIndex" onchange="ajax('SimpleMembershipUpdate.retrieveMembers', {formIds: 'simpleMembershipUpdateMemberSortForm'}); return false;" >
          <c:forEach items="${simpleMembershipUpdateContainer.memberSortIndexSelection}" var="index">
            <c:set var="navId" value="member.sort.string${index}"  />
            <c:choose>
              <c:when test="${index == selectedSortStringIndex}">
                <option selected="selected" value="${index}">${grouper:message(navId, true, false)}</option>
              </c:when>
              <c:otherwise>
                <option value="${index}">${grouper:message(navId, true, false)}</option>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </select>
      </form>
      <br />
    </c:if>
  </c:if>
    
  <%-- message if no members --%>  
  <c:if test="${fn:length(simpleMembershipUpdateContainer.guiMembers) == 0}">
    <grouper:message value="${simpleMembershipUpdateContainer.text.noMembersFound}" />
  
  </c:if>
  
  <%-- form for delete buttons, loop through members --%>
  <form action="whatever" id="simpleMembershipUpdateDeleteMultipleForm" name="simpleMembershipUpdateDeleteMultipleForm">
    <c:forEach items="${simpleMembershipUpdateContainer.guiMembers}" var="guiMember">
  
      <div class="memberLink">
       
        <%-- checkbox for delete multiple, if showing --%>
        <span class="shows_simpleMembershipUpdateDeleteMultiple" 
            style="${grouper:hideShowStyle('simpleMembershipUpdateDeleteMultiple', true)}">
          <input type="checkbox" name="deleteMultiple_${guiMember.member.uuid}"  />
        </span>    
        
        <%-- image button to delete a member, if showing --%>
        <span class="hides_simpleMembershipUpdateDeleteMultiple" 
            style="${grouper:hideShowStyle('simpleMembershipUpdateDeleteMultiple', false)}">
          <a href="#" onclick="if (confirm('${simpleMembershipUpdateContainer.text.deleteConfirm}')) {ajax('SimpleMembershipUpdate.deleteSingle?memberId=${guiMember.member.uuid}');} return false;" 
          ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
          alt="${simpleMembershipUpdateContainer.text.deleteImageAlt }"/></a>
        </span>
        &nbsp;
        <%-- show an icon for the subject --%>
        <grouper:subjectIcon guiSubject="${guiMember.guiSubject}" /> 
        <%-- the string representation of the subject --%>
        <span class="simpleMembershipUpdateMemberDescription">
          <grouper:message valueTooltip="${grouper:escapeHtml(guiMember.guiSubject.screenLabelLongIfDifferent)}" 
             value="${grouper:escapeHtml(guiMember.guiSubject.screenLabel)}"  />
          <c:if test="${guiMember.hasDisabledString}">
            <span class="simpleMembershipUpdateDisabled">${guiMember.disabledDateString}</span>
          </c:if>
        </span>
        <%-- show the triangle next to the member for more operations --%>
        <a class="memberMenuButton" href="#"
            ><img src="../../grouperExternal/public/assets/images/bullet_arrow_down.png" border="0" id="memberMenuButton_${guiMember.member.uuid}"
            alt="${simpleMembershipUpdateContainer.text.memberMenuAlt}"/></a>
      </div> 
  
    </c:forEach>
    <%-- member menu div, and attach to buttons --%>
    <grouper:menu menuId="memberMenu"
      operation="SimpleMembershipUpdateMenu.memberMenu" 
      structureOperation="SimpleMembershipUpdateMenu.memberMenuStructure" 
      contextZoneJqueryHandle=".memberMenuButton" contextMenu="true" />
  </form>
  <%-- if showing delete multiple, then show buttons for delete selected, and delete all --%>
  <div class="buttonRow shows_simpleMembershipUpdateDeleteMultiple"  
      style="${grouper:hideShowStyle('simpleMembershipUpdateDeleteMultiple', true)}">
      <br />
    <input class="blueButton" type="submit" 
      onclick="ajax('SimpleMembershipUpdate.deleteMultiple', {formIds: 'simpleMembershipUpdateDeleteMultipleForm'}); return false;" 
      value="${simpleMembershipUpdateContainer.text.deleteMultipleButton}" 
      onmouseover="Tip('${grouper:escapeJavascript(simpleMembershipUpdateContainer.text.deleteMultipleTooltip)}')" 
      onmouseout="UnTip()" />
    &nbsp;
    <input class="blueButton" type="submit" 
      onclick="ajax('SimpleMembershipUpdate.deleteAll'); return false;" 
      value="${simpleMembershipUpdateContainer.text.deleteAllButton}" 
      onmouseover="Tip('${grouper:escapeJavascript(simpleMembershipUpdateContainer.text.deleteAllTooltip)}')" 
      onmouseout="UnTip()" />    
  </div>
  <%-- show the google like paging buttons at the bottom to pick a page to go to --%>
  <div class="pagingButtons">
    <grouper:message value="${simpleMembershipUpdateContainer.text.pagingResultPrefix}" />
      <grouper:paging showSummaryOrButtons="false" pagingName="simpleMemberUpdateMembers" 
      refreshOperation="SimpleMembershipUpdate.retrieveMembers" />
  </div>
<br />
</div>
</div>
<!-- End: $Id: simpleMembershipMembershipList.jsp,v 1.3 2009-11-02 08:50:40 mchyzer Exp $ -->
