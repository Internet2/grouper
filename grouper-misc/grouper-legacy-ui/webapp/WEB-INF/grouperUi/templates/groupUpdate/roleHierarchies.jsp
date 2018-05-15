<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: groupUpdate/roleHierarchies.jsp -->

<div class="section" style="min-width: 1000px">

  <grouper:subtitle key="simpleGroupUpdate.roleHierarchiesSectionHeader" />

  <div class="sectionBody">
    <form id="roleHierarchiesFormId" name="roleHierarchiesFormName" onsubmit="return false;" >
    
    <%-- signify which role we are talking about --%>
    <input type="hidden" name="groupToEditId" 
              value="${groupUpdateRequestContainer.groupToEdit.id}" />
    <table class="formTable formTableSpaced" cellspacing="2">
    
    
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleGroupUpdate.hierarchies.rolePath" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          ${grouper:escapeHtml(groupUpdateRequestContainer.groupToEdit.displayName)}
        </td>
      </tr>

      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleGroupUpdate.editHierarchyChange" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">

          <div class="combohint"><grouper:message key="simpleGroupUpdate.selectRoleHierarchyCombohint"/></div>
            <grouper:combobox filterOperation="SimpleGroupUpdateFilter.filterRoles" id="roleIdForHierarchy" 
              width="700" />

            <div style="margin-top: 5px;">
              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleGroupUpdate.addRoleImplies', {formIds: 'roleHierarchiesFormId'}); return false;" 
              value="${grouper:message('simpleGroupUpdate.addRoleThatImplies', true, false)} ${grouper:escapeHtml(groupUpdateRequestContainer.groupToEdit.displayExtension)}" 
              style="margin-top: 2px; white-space: nowrap;" />

              <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleGroupUpdate.addRoleImpliedBy', {formIds: 'roleHierarchiesFormId'}); return false;" 
              value="${grouper:message('simpleGroupUpdate.addRoleImpliedByThis', true, false)} ${grouper:escapeHtml(groupUpdateRequestContainer.groupToEdit.displayExtension)}"
              style="margin-top: 2px; white-space: nowrap;" />
            </div>
        </td>
      </tr>
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleGroupUpdate.rolesImply">
            <grouper:param>${groupUpdateRequestContainer.groupToEdit.displayExtension}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${groupUpdateRequestContainer.rolesThatImply}" var="roleThatImplies">
            
	            ${roleThatImplies.displayName}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleGroupUpdate.immediateRolesImply">
            <grouper:param>${groupUpdateRequestContainer.groupToEdit.displayExtension}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${groupUpdateRequestContainer.rolesThatImplyImmediate}" var="roleThatImpliesImmediate">
            
            <a href="#" onclick="if (confirm('${grouper:message('simpleGroupUpdate.deleteRoleImpliesConfirm', true, true)}')) {ajax('SimpleGroupUpdate.deleteRoleImplies?groupToEditId=${groupUpdateRequestContainer.groupToEdit.id}&roleIdSubmitted=${roleThatImpliesImmediate.id}');} return false;" 
	            ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
	            alt="${grouper:message('simpleGroupUpdate.deleteRoleImpliesImageAlt', true, false)}"
              onmouseover="Tip('${grouper:escapeJavascript(navMap['simpleGroupUpdate.deleteRoleImpliesImageAlt'])}')" 
              onmouseout="UnTip()"/></a>
            
            ${roleThatImpliesImmediate.displayName}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleGroupUpdate.hierarchies.rolePath" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          ${grouper:escapeHtml(groupUpdateRequestContainer.groupToEdit.displayName)}
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleGroupUpdate.immediateRolesImpliedBy">
            <grouper:param>${groupUpdateRequestContainer.groupToEdit.displayExtension}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${groupUpdateRequestContainer.rolesImpliedByImmediate}" var="roleImpliedByImmediate">
            
            <a href="#" onclick="if (confirm('${grouper:message('simpleGroupUpdate.deleteRoleImpliedByConfirm', true, true)}')) {ajax('SimpleGroupUpdate.deleteRoleImpliedBy?groupToEditId=${groupUpdateRequestContainer.groupToEdit.id}&roleIdSubmitted=${roleImpliedByImmediate.id}');} return false;" 
              ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
              alt="${grouper:message('simpleGroupUpdate.deleteRoleImpliedByImageAlt', true, false)}"
              onmouseover="Tip('${grouper:escapeJavascript(navMap['simpleGroupUpdate.deleteRoleImpliedByImageAlt'])}')" 
              onmouseout="UnTip()"/></a>
            
            ${roleImpliedByImmediate.displayName}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleGroupUpdate.rolesImpliedBy">
            <grouper:param>${groupUpdateRequestContainer.groupToEdit.displayExtension}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${groupUpdateRequestContainer.rolesImpliedBy}" var="roleImpliedBy">
            
              ${roleImpliedBy.name}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
    </table>
    </form>
    <br />
  </div>
</div>

<!-- End: groupUpdate/roleHierarchies.jsp -->
