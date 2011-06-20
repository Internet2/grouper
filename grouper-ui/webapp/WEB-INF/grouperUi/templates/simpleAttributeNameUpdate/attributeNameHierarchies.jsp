<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeNameUpdate/attributeNameHierarchies.jsp -->

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeNameUpdate.attributeDefNameSetSectionHeader" />

  <div class="sectionBody">
    <form id="attributeNameHierarchiesFormId" name="attributeNameHierarchiesFormName" onsubmit="return false;" >
    
    <%-- signify which attribute def we are talking about --%>
    <input type="hidden" name="attributeDefNameToEditId" 
              value="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.id}" />
    <table class="formTable formTableSpaced" cellspacing="2">
    
    
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleAttributeNameUpdate.hierarchies.attributeDefName" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          ${grouper:escapeHtml(attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayName)}
        </td>
      </tr>
    
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleAttributeNameUpdate.editHierarchyChange" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">

          <%-- note, I think we want a constraint where attribute names only imply attribute names in the same attribute def --%>
          <input type="hidden" name="attributeDefId" 
            value="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.attributeDefId}" />

          <div class="combohint"><grouper:message key="simpleAttributeNameUpdate.selectAttributeDefNameHierarchyCombohint"/></div>
          <table width="900" cellpadding="0" cellspacing="0">
            <tr valign="top">
              <td style="padding: 0px" width="710">
                <grouper:combobox filterOperation="SimpleAttributeNameUpdateFilter.filterAttributeDefNames" id="attributeDefNameIdForHierarchy" 
                  width="700" additionalFormElementNames="attributeDefId" />
              </td>
              <td>
                <input class="blueButton" type="submit" 
                onclick="ajax('../app/SimpleAttributeNameUpdateFilter.addAttributeNameThatImplies', {formIds: 'attributeNameHierarchiesFormId'}); return false;" 
                value="${grouper:message('simpleAttributeNameUpdate.addAttributeNameThatImplies', true, false)} ${grouper:escapeHtml(attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayExtension)}" 
                style="margin-top: 2px; white-space: nowrap;" />
              </td>
              <td>
                <input class="blueButton" type="submit" 
                onclick="ajax('../app/SimpleAttributeNameUpdateFilter.addAttributeNameImpliedByThis', {formIds: 'attributeNameHierarchiesFormId'}); return false;" 
                value="${grouper:message('simpleAttributeNameUpdate.addAttributeNameImpliedByThis', true, false)} ${grouper:escapeHtml(attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayExtension)}"
                style="margin-top: 2px; white-space: nowrap;" />
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleAttributeNameUpdate.attributeNamesImply">
            <grouper:param>${attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayExtension}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeNameUpdateRequestContainer.attributeDefNamesThatImplyThis}" var="attributeNameThatImplies">
            
	            ${attributeNameThatImplies.name}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleAttributeNameUpdate.immediateAttributeNamesImply">
            <grouper:param>${attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayExtension}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeNameUpdateRequestContainer.attributeDefNamesThatImplyThisImmediate}" var="attributeNameThatImpliesImmediate">
            
            <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeNameUpdate.deleteAttributeNameImpliesConfirm', true, true)}')) {ajax('SimpleAttributeNameUpdate.deleteAttributeNameImplies?attributeDefNameToEditId=${attributeNameUpdateRequestContainer.attributeDefNameToEdit.id}&attributeDefNameIdForHierarchy=${attributeNameThatImpliesImmediate.id}');} return false;" 
	            ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
	            alt="${grouper:message('simpleAttributeNameUpdate.deleteAttributeNameImpliesImageAlt', true, false)}"
              onmouseover="Tip('${grouper:escapeJavascript(navMap['simpleAttributeNameUpdate.deleteAttributeNameImpliesImageAlt'])}')" 
              onmouseout="UnTip()"/></a>
            
            ${attributeNameThatImpliesImmediate.displayName}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleAttributeNameUpdate.hierarchies.attributeDefName" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          ${grouper:escapeHtml(attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayName)}
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleAttributeNameUpdate.immediateAttributeNamesImpliedBy">
            <grouper:param>${attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayExtension}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeNameUpdateRequestContainer.attributeDefNamesImpliedByThisImmediate}" var="attributeNameImpliedByImmediate">
            
            <a href="#" onclick="if (confirm('${grouper:message('simpleAttributeNameUpdate.deleteAttributeNameImpliedByConfirm', true, true)}')) {ajax('SimpleAttributeNameUpdate.deleteAttributeNameImpliedBy?attributeDefNameToEditId=${attributeNameUpdateRequestContainer.attributeDefNameToEdit.id}&attributeDefNameIdForHierarchy=${attributeNameImpliedByImmediate.id}');} return false;" 
              ><img src="../../grouperExternal/public/assets/images/page_cross.gif" height="14px" border="0" 
              alt="${grouper:message('simpleAttributeNameUpdate.deleteAttributeNameImpliedByImageAlt', true, false)}"
              onmouseover="Tip('${grouper:escapeJavascript(navMap['simpleAttributeNameUpdate.deleteAttributeNameImpliedByImageAlt'])}')" 
              onmouseout="UnTip()"/></a>
            
            ${attributeNameImpliedByImmediate.displayName}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: top; white-space: nowrap;">
          <grouper:message key="simpleAttributeNameUpdate.attributeNamesImpliedBy">
            <grouper:param>${attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayExtension}</grouper:param>
          </grouper:message>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          <c:forEach items="${attributeNameUpdateRequestContainer.attributeDefNamesImpliedByThis}" var="attributeNameImpliedBy">
            
              ${attributeNameImpliedBy.name}
            
            <br />
          </c:forEach>
        </td>
      </tr>
      
      
    </table>
    </form>
    <br />
  </div>
</div>

<!-- End: simpleAttributeNameUpdate/attributeNameHierarchies.jsp -->
