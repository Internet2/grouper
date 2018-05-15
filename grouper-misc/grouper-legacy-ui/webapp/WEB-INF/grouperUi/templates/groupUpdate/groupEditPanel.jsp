<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: simpleGroupUpdate/groupEditPanel.jsp -->

<%--Group edit --%>

<div class="section" style="min-width: 900px">


  <c:choose>
    <c:when test="${groupUpdateRequestContainer.create}">
      <grouper:subtitle key="simpleGroupUpdate.editSectionGroupRoleHeader" />
    </c:when>
    <c:otherwise>
      <grouper:subtitle key="${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb == 'role' ? 'simpleGroupUpdate.editSectionRoleHeader' : (groupUpdateRequestContainer.groupToEdit.typeOfGroupDb == 'entity' ? 'simpleGroupUpdate.editSectionEntityHeader' : 'simpleGroupUpdate.editSectionGroupHeader')}" />
    </c:otherwise>
  </c:choose>

  <div class="sectionBody">
    <form id="groupEditFormId" name="groupEditFormName" onsubmit="return false;" >
    <table class="formTable formTableSpaced" cellspacing="2">
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="folder">
            <grouper:message key="simpleGroupUpdate.folder" />
          </label>
          <c:if test="${groupUpdateRequestContainer.create}">
            <sup class="requiredIndicator">*</sup>
          </c:if>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          
          <c:choose>
				    <c:when test="${groupUpdateRequestContainer.create}">
              <div class="combohint"><grouper:message key="simpleGroupUpdate.selectFolderCombohint"/></div>
		          <grouper:combobox filterOperation="SimpleGroupUpdateFilter.filterCreatableNamespace" id="simpleGroupUpdatePickNamespace" 
		            width="700"/>
				    </c:when>
				    <c:otherwise>
					    <grouper:groupBreadcrumb showLeafNode="false" 
					      showCurrentLocationLabel="false" 
            	  groupName="${groupUpdateRequestContainer.groupToEdit.name}"  />
				    </c:otherwise>
				  </c:choose>
        </td>
      </tr>
      <c:if test="${!groupUpdateRequestContainer.create}">
	      <tr class="formTableRow">
	        <td class="formTableLeft">
            <grouper:message key="simpleGroupUpdate.editId" />
	        </td>
	        <td class="formTableRight">
	          ${groupUpdateRequestContainer.groupToEdit.uuid }
	          <input type="hidden" name="groupToEditId" 
	            value="${groupUpdateRequestContainer.groupToEdit.uuid }" />
	        </td>
	      </tr>
      </c:if>	      
       <tr class="formTableRow">
         <td class="formTableLeft">
           <c:if test="${groupUpdateRequestContainer.create}">
             <label for="groupToEditExtension">
           </c:if>
           <grouper:message key="simpleGroupUpdate.extension" />
           <c:if test="${groupUpdateRequestContainer.create}">
             </label>
             <sup class="requiredIndicator">*</sup>
           </c:if>
         </td>
         <td class="formTableRight">
           <c:choose>
             <c:when test="${groupUpdateRequestContainer.create}">
	             <input type="text" name="groupToEditExtension" />
             </c:when>
             <c:otherwise>
               ${grouper:escapeHtml(groupUpdateRequestContainer.groupToEdit.extension)}
             </c:otherwise>
           </c:choose>
         </td>
       </tr>
       <c:choose>
         <c:when test="${!groupUpdateRequestContainer.create}">
           <tr class="formTableRow">
             <td class="formTableLeft">
               <grouper:message key="simpleAttributeNameUpdate.editIdPath" />
             </td>
             <td class="formTableRight">
               ${grouper:escapeHtml(groupUpdateRequestContainer.groupToEdit.name)}
             </td>
           </tr>
         </c:when>
       </c:choose>
       <tr class="formTableRow">
         <td class="formTableLeft">

           <c:choose>
             <c:when test="${groupUpdateRequestContainer.create}">
		           <label for="groupToEditType">
		             <grouper:message key="simpleGroupUpdate.type" />
		           </label>
		           <sup class="requiredIndicator">*</sup>
             </c:when>
             <c:otherwise>
               <grouper:message key="simpleGroupUpdate.type" />
             </c:otherwise>
           </c:choose>
           
         </td>
         <td class="formTableRight">
           <input type="radio" name="groupToEditType" value="group" ${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb == 'group' ? 'checked="checked"' : '' } /><grouper:message key="simpleGroupUpdate.type.group" />
           &nbsp;
           <input type="radio" name="groupToEditType" value="role" ${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb == 'role' ? 'checked="checked"' : '' } /><grouper:message key="simpleGroupUpdate.type.role" />
           &nbsp;
           <input type="radio" name="groupToEditType" value="entity" ${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb == 'entity' ? 'checked="checked"' : '' } /><grouper:message key="simpleGroupUpdate.type.entity" />
         </td>
       </tr>
       
       <tr class="formTableRow">
         <td class="formTableLeft">
           <label for="groupToEditExtension">
             <grouper:message key="simpleGroupUpdate.displayExtension" />
           </label>
           <sup class="requiredIndicator">*</sup>
         </td>
         <td class="formTableRight">
           <input type="text" name="groupToEditDisplayExtension" 
             value="${grouper:escapeHtml(groupUpdateRequestContainer.groupToEdit.displayExtensionDb)}" />
         </td>
       </tr>

       <tr class="formTableRow">
         <td class="formTableLeft">
           <label for="groupToEditDescription">
             <grouper:message key="simpleGroupUpdate.description" />
           </label>
         </td>
         <td class="formTableRight">
            <textarea name="groupToEditDescription" rows="3" cols="40">${grouper:escapeHtml(groupUpdateRequestContainer.groupToEdit.description)}</textarea> 
         </td>
       </tr>
       

       <tr class="formTableRow">
         <td class="formTableLeft">
           <grouper:message key="simpleGroupUpdate.create.privs-for-all" />
         </td>
         <td class="formTableRight" style="white-space: nowrap;">
            
            <c:if test="${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb != 'entity'}" >
              
               <input type="checkbox" name="groupToEditAllowAllRead" 
                value="true" 
                ${groupUpdateRequestContainer.allowAllRead ? 'checked="checked"' : '' } />
                
               <grouper:message key="priv.read" /> 
               
               &nbsp;
            </c:if>             
            <input type="checkbox" name="groupToEditAllowAllView" 
              value="true" 
              ${groupUpdateRequestContainer.allowAllView ? 'checked="checked"' : '' } />
              
             <grouper:message key="priv.view" /> 
             
            <c:if test="${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb != 'entity'}" >
               &nbsp;

            <input type="checkbox" name="groupToEditAllowAllGroupAttrRead"
              value="true"
              ${groupUpdateRequestContainer.allowAllGroupAttrRead ? 'checked="checked"' : '' } />

             <grouper:message key="priv.groupAttrRead" />

             &nbsp;

              <input type="checkbox" name="groupToEditAllowAllOptin" 
                value="true" 
                ${groupUpdateRequestContainer.allowAllOptin ? 'checked="checked"' : '' } />
                
               <grouper:message key="priv.optin" /> 
               
               &nbsp;
               
              <input type="checkbox" name="groupToEditAllowAllOptout" 
                value="true" 
                ${groupUpdateRequestContainer.allowAllOptout ? 'checked="checked"' : '' } />
                
               <grouper:message key="priv.optout" /> 
            </c:if>                          
         </td>
       </tr>
       <tr>
       <td colspan="2">

         <c:if test="${!groupUpdateRequestContainer.create}">

           <input class="redButton" type="submit" 
            onclick="if (confirm('${grouper:message('simpleGroupUpdate.editPanelDeleteConfirm', true, true) }')) {ajax('../app/SimpleGroupUpdate.groupEditPanelDelete', {formIds: 'groupEditFormId'}); } return false;" 
            value="${grouper:message('simpleGroupUpdate.editPanelDelete', true, false)}" style="margin-top: 2px" />

         </c:if>
         <input class="redButton" type="submit" 
          onclick="window.location = 'grouper.html?operation=SimpleGroupUpdate.createEdit'; return false;" 
          value="${grouper:message('simpleGroupUpdate.editPanelCancel', true, false)}" style="margin-top: 2px" />
         <c:if test="${!groupUpdateRequestContainer.create}">

           <input class="blueButton" type="submit" 
            onclick="ajax('../app/SimpleGroupUpdate.groupEditPanelPrivilegesClearPaging', {formIds: 'groupEditFormId'}); return false;" 
            value="${grouper:message('simpleGroupUpdate.editPanelPrivileges', true, false)}" style="margin-top: 2px" />

           <c:if test="${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb == 'role'}">
             <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleGroupUpdate.roleHierarchies', {formIds: 'groupEditFormId'}); return false;" 
              value="${grouper:message('simpleGroupUpdate.editPanelRoleHierarchies', true, false)}" style="margin-top: 2px" />

             <input class="blueButton" type="submit" 
              onclick="ajax('../app/SimpleGroupUpdate.roleHierarchyGraph', {formIds: 'groupEditFormId'}); return false;" 
              value="${grouper:message('simpleGroupUpdate.editPanelRoleHierarchyGraphButton', true, false)}" style="margin-top: 2px" />

           </c:if>

          <c:if test="${groupUpdateRequestContainer.groupToEdit.typeOfGroupDb != 'entity'}" >
           <input class="blueButton" type="submit" 
            onclick="location.href = 'grouper.html?operation=SimpleMembershipUpdate.init&groupId=${groupUpdateRequestContainer.groupToEdit.id}'; return false;" 
            value="${grouper:message('simpleGroupUpdate.memberships', true, false)}" style="margin-top: 2px" />
          </c:if>
         </c:if>
         <input class="blueButton" type="submit" 
          onclick="ajax('../app/SimpleGroupUpdate.groupEditPanelSubmit', {formIds: 'groupEditFormId'}); return false;" 
          value="${grouper:message('simpleGroupUpdate.editPanelSubmit', true, false)}" style="margin-top: 2px" />
       </td>
       </tr> 
    </table>
    </form>
    <br />
  </div>
</div>


<div id="roleHierarchyPanel">
</div>

<div id="groupPrivilegesPanel">
</div>

<!-- End: simpleGroupUpdate/groupEditPanel.jsp -->
