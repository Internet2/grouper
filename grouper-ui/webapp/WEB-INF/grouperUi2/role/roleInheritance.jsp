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
                    <label class="control-label">
                      <grouper:message key="roleInheritanceEditRolesThatImply">
                        <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls">
                      <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.rolesThatImplyThis}" var="roleThatImply">
                        <label class="inline">
                            ${roleThatImply.displayExtension}
                        </label>
                      </c:forEach> 
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="roleInheritanceEditRolesThatImmediatelyImply">
                        <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls">
                      <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.allRoles}" var="role">
                        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.group.id != role.id}">
                          <label class="checkbox inline">
                            <c:set var="checked" value="false" />
                            <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.rolesThatImplyThisImmediate}" var="roleThatImmediatelyImply">
                              <c:if test="${roleThatImmediatelyImply.id == role.id}">
                                <c:set var="checked" value="true"></c:set>
                              </c:if>
                            </c:forEach>
                            <input type="checkbox" name="rolesThatImmediatelyImply" value="${role.id}" ${checked ? 'checked="checked"' : '' }/>
                              ${role.extension}
                          </label>
                        </c:if>
                      </c:forEach>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="roleInheritanceEditRolesImpliedBy">
                        <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls">
                      <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.rolesImpliedByThis}" var="roleImpliedBy">
                        <label class="inline">
                          ${roleImpliedBy.extension}
                        </label>
                      </c:forEach>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="roleInheritanceEditRolesImpliedByImmediate">
                        <grouper:param>${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls">
                      <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.allRoles}" var="role">
                        <c:if test="${grouperRequestContainer.groupContainer.guiGroup.group.id != role.id}">
                          <label class="checkbox inline">
                            <c:set var="checked" value="false" />
                            <c:forEach items="${grouperRequestContainer.roleInheritanceContainer.rolesImpliedByThisImmediate}" var="roleImpliedByImmediate">
                              <c:if test="${roleImpliedByImmediate.id == role.id}">
                                <c:set var="checked" value="true"></c:set>
                              </c:if>
                            </c:forEach>
                            <input type="checkbox" name="rolesImpliedByImmediate" value="${role.id}" ${checked ? 'checked="checked"' : '' }/>
                              ${role.extension}
                          </label>
                        </c:if>
                      </c:forEach>
                    </div>
                  </div>
                 
                  <div class="form-actions">
                    <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Role.editRoleInheritanceSubmit', {formIds: 'editRoleInheritanceForm'}); return false;">${textContainer.text['roleInheritanceEditSaveButton'] }</a> 
                    <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" >${textContainer.text['roleInhertianceEditCancelButton'] }</a>
                  </div>
                  
                </form>
              </div>
            </div>
            
            
            
