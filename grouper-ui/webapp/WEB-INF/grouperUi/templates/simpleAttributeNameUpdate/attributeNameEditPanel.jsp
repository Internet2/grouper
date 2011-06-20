<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleAttributeUpdate/attributeEditPanel.jsp -->

<%--Attribute edit --%>

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleAttributeNameUpdate.editSectionHeader" />

  <div class="sectionBody">
    <form id="attributeNameEditFormId" name="attributeNameEditFormName" onsubmit="return false;" >
    <table class="formTable formTableSpaced" cellspacing="2">
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="folder">
            <grouper:message key="simpleAttributeNameUpdate.attributeDef" />
          </label>
          <c:if test="${attributeNameUpdateRequestContainer.create}">
            <sup class="requiredIndicator">*</sup>
          </c:if>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          
          <c:choose>
				    <c:when test="${attributeNameUpdateRequestContainer.create}">
              <div class="combohint"><grouper:message key="simpleAttributeNameUpdate.selectAttributeDefCombohint"/></div>
		          <grouper:combobox filterOperation="SimpleAttributeNameUpdateFilter.filterAttributeDefs" id="simpleAttributeNameUpdateNewAttributeDef" 
		            width="700"  comboDefaultText="${attributeNameUpdateRequestContainer.attributeDefForFilter.name}"  
                comboDefaultValue="${attributeNameUpdateRequestContainer.attributeDefForFilter.id}"  />
				    </c:when>
				    <c:otherwise>
              ${grouper:escapeHtml(attributeNameUpdateRequestContainer.attributeDefNameToEdit.attributeDef.name)}
				    </c:otherwise>
				  </c:choose>
        </td>
      </tr>
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="folder">
            <grouper:message key="simpleAttributeNameUpdate.folder" />
          </label>
          <c:if test="${attributeNameUpdateRequestContainer.create}">
            <sup class="requiredIndicator">*</sup>
          </c:if>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          
          <c:choose>
            <c:when test="${attributeNameUpdateRequestContainer.create}">
              <div class="combohint"><grouper:message key="simpleAttributeNameUpdate.selectFolderCombohint"/></div>
              <grouper:combobox filterOperation="SimpleAttributeNameUpdateFilter.filterCreatableNamespace" id="simpleAttributeNameUpdatePickNamespace" 
                width="700" comboDefaultText="${attributeNameUpdateRequestContainer.attributeDefForFilter.stem.name}"  
                comboDefaultValue="${attributeNameUpdateRequestContainer.attributeDefForFilter.stem.uuid}" />
            </c:when>
            <c:otherwise>
              <grouper:groupBreadcrumb showLeafNode="false" 
                showCurrentLocationLabel="false" 
                groupName="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayName}"  />
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
      <c:if test="${!attributeNameUpdateRequestContainer.create}">
	      <tr class="formTableRow">
	        <td class="formTableLeft">
            <grouper:message key="simpleAttributeNameUpdate.editId" />
	        </td>
	        <td class="formTableRight">
	          ${attributeNameUpdateRequestContainer.attributeDefNameToEdit.id }
	          <input type="hidden" name="attributeDefNameToEditId" 
	            value="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.id }" />
	        </td>
	      </tr>
      </c:if>	      
       <tr class="formTableRow">
         <td class="formTableLeft">
           <c:if test="${attributeNameUpdateRequestContainer.create}">
             <label for="attributeDefNameToEditExtension">
           </c:if>
           <grouper:message key="simpleAttributeNameUpdate.extension" />
           <c:if test="${attributeNameUpdateRequestContainer.create}">
             </label>
             <sup class="requiredIndicator">*</sup>
           </c:if>
         </td>
         <td class="formTableRight">
           <c:choose>
             <c:when test="${attributeNameUpdateRequestContainer.create}">
	             <input type="text" name="attributeDefNameToEditExtension" 
	               value="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.extension}" />
             </c:when>
             <c:otherwise>
               ${grouper:escapeHtml(attributeNameUpdateRequestContainer.attributeDefNameToEdit.extension)}
             </c:otherwise>
           </c:choose>
         </td>
       </tr>
        <c:if test="${!attributeNameUpdateRequestContainer.create}">
          <tr class="formTableRow">
            <td class="formTableLeft">
              <grouper:message key="simpleAttributeNameUpdate.editIdPath" />
            </td>
            <td class="formTableRight">
              ${grouper:escapeHtml(attributeNameUpdateRequestContainer.attributeDefNameToEdit.name)}
            </td>
          </tr>
        </c:if>       
       <tr class="formTableRow">
         <td class="formTableLeft">
           <label for="attributeDefNameToEditExtension">
             <grouper:message key="simpleAttributeNameUpdate.displayExtension" />
           </label>
           <sup class="requiredIndicator">*</sup>
         </td>
         <td class="formTableRight">
           <input type="text" name="attributeDefNameToEditDisplayExtension" 
             value="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.displayExtension}" />
         </td>
       </tr>
       <tr class="formTableRow">
         <td class="formTableLeft">
           <label for="attributeDefNameToEditDescription">
             <grouper:message key="simpleAttributeNameUpdate.description" />
           </label>
         </td>
         <td class="formTableRight">
            <%-- TODO escape the description for HTML --%>
            <textarea name="attributeDefNameToEditDescription" rows="3" cols="40">${attributeNameUpdateRequestContainer.attributeDefNameToEdit.description}</textarea> 
         </td>
       </tr>
       <tr>
       <td colspan="2">

         <c:if test="${!attributeNameUpdateRequestContainer.create}">

           <input class="redButton" type="submit" 
            onclick="if (confirm('${grouper:message('simpleAttributeNameUpdate.editPanelDeleteConfirm', true, true) }')) {ajax('../app/SimpleAttributeNameUpdate.attributeNameEditPanelDelete', {formIds: 'attributeNameEditFormId'}); } return false;" 
            value="${attributeNameUpdateRequestContainer.text.editPanelDelete}" style="margin-top: 2px" />

         </c:if>

         <input class="redButton" type="submit" 
          onclick="window.location = 'grouper.html?operation=SimpleAttributeNameUpdate.createEditAttributeNames'; return false;" 
          value="${attributeNameUpdateRequestContainer.text.editPanelCancel}" style="margin-top: 2px" />
         
         <%-- make sure it is a permission... --%>
         <c:if test="${(!attributeNameUpdateRequestContainer.create) && attributeNameUpdateRequestContainer.attributeDefNameToEdit.attributeDef.attributeDefTypeDb == 'perm'}">
           <input class="blueButton" type="submit" 
            onclick="ajax('../app/SimpleAttributeNameUpdate.attributeNameEditPanelHierarchies', {formIds: 'attributeNameEditFormId'}); return false;" 
            value="${attributeNameUpdateRequestContainer.text.editPanelHierarchies}" style="margin-top: 2px" />

           <input class="blueButton" type="submit" 
            onclick="ajax('../app/SimpleAttributeNameUpdate.attributeNameEditPanelHierarchiesGraph', {formIds: 'attributeNameEditFormId'}); return false;" 
            value="${grouper:message('simpleAttributeNameUpdate.editPanelHierarchyGraph', true, true) }" style="margin-top: 2px" />

         </c:if>
       
         <input class="blueButton" type="submit" 
          onclick="window.location = 'grouper.html?operation=SimpleAttributeUpdate.createEdit&attributeDefId=${attributeNameUpdateRequestContainer.attributeDef.id}'; return false;" 
          value="${grouper:message('simpleAttributeNameUpdate.editPanelAttributeDef', true, true) }" style="margin-top: 2px" />
       
       
         <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleAttributeNameUpdate.attributeNameEditPanelSubmit', {formIds: 'attributeNameEditFormId'}); return false;" 
          value="${attributeNameUpdateRequestContainer.text.editPanelSubmit}" style="margin-top: 2px" />
       
       </td>
       </tr>
    </table>
    </form>
    <br />
  </div>
</div>


<div id="attributeNameHierarchiesPanel">
</div>

<!-- End: simpleAttributeNameUpdate/attributeNameEditPanel.jsp -->
