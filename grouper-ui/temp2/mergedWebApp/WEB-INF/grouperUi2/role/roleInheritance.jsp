<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-group"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['roleInheritanceEditTitle'] }</small></h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <form id="editRoleInheritanceForm" class="form-horizontal">

                  <input type="hidden" name="groupId" value="${grouperRequestContainer.groupContainer.guiGroup.group.id}" />
                  
                  <div class="control-group">
                    <label for="roleInhertianceRoleComboId" class="control-label">${textContainer.text['editRoleInheritanceLabel'] }</label>
                    
                    <div class="controls">
                      <input type="hidden" name="typeOfGroup" value="role" />
	                    <grouper:combobox2 idBase="roleInhertianceRoleCombo" style="width: 30em"
	                       filterOperation="../app/UiV2Group.groupUpdateFilter"
	                       additionalFormElementNames="typeOfGroup" />
                    </div>
                    <br />
                    <div class="control-group" style="margin-bottom: 5px">
	                    <div class="controls">
	                      <a href="#" 
	                        onclick="ajax('../app/UiV2Role.addRoleImplies', {formIds: 'editRoleInheritanceForm'}); return false;"
	                        class="btn bulk-add-another-group" role="button">
	                        <grouper:message key="roleInheritanceEditRolesThatImplyButtonText">
                            <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                          </grouper:message>
	                        </a>
	                        
	                      <a href="#" 
	                        onclick="ajax('../app/UiV2Role.addRoleImpliedBy', {formIds: 'editRoleInheritanceForm'}); return false;"
	                        class="btn bulk-add-another-group" role="button">
	                         <grouper:message key="roleInheritanceEditRolesImpliedByButtonText">
                            <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                          </grouper:message>  
	                      </a>
	                        
	                    </div>
                    </div>
                    
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="roleInheritanceEditRolesThatImply">
                        <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls" style="padding-top: 5px;">
                      <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.rolesThatImplyThis}" var="roleThatImply">
                        ${roleThatImply.displayExtension}
                        <br/>
                      </c:forEach>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="roleInheritanceEditRolesThatImmediatelyImply">
                        <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls" style="padding-top: 5px;">
                      <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.rolesThatImplyThisImmediate}" var="roleThatImmediatelyImply">
                        ${roleThatImmediatelyImply.extension}
                        <a href="#" onclick="ajax('../app/UiV2Role.deleteRoleImplies?roleId=${roleThatImmediatelyImply.id}', {formIds: 'editRoleInheritanceForm'}); return false;"><i class="fa fa-times" style="color: #aaaaaa"></i></a>
                         <br />
                      </c:forEach>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="roleInheritanceEditRolesImpliedBy">
                        <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls" style="padding-top: 5px;">
                      <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.rolesImpliedByThis}" var="roleImpliedBy">
                        ${roleImpliedBy.extension}
                        <br/>
                      </c:forEach>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="roleInheritanceEditRolesImpliedByImmediate">
                        <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls" style="padding-top: 5px;">
                      <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.rolesImpliedByThisImmediate}" var="roleImpliedByImmediate">
                        ${roleImpliedByImmediate.extension} 
                        <a href="#" onclick="ajax('../app/UiV2Role.deleteRoleImpliedBy?roleId=${roleImpliedByImmediate.id}', {formIds: 'editRoleInheritanceForm'}); return false;"><i class="fa fa-times" style="color: #aaaaaa"></i></a>
                         <br />
                      </c:forEach>
                    </div>
                  </div>
                  
                </form>
              </div>
            </div>
            
            
            
