<%@ include file="../assetsJsp/commonTaglib.jsp"%>


            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-cog"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayExtension)}
                <br /><small>${textContainer.text['attributeDefNameEditTitle'] }</small></h1>
              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
                <form id="editAttributeDefNameInheritanceForm" class="form-horizontal">
                
                  <input type="hidden" name="attributeDefNameId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}" />
                  
                  <div class="control-group">
                    <label class="control-label" style="padding-top: 0px;">${textContainer.text['attributeDefNameEditAttributeDefNameInheritance'] }</label>
                    <div class="controls">
                      <span>${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.shortLinkWithIcon}</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="attributeDeNameEditAttributeDefNamesThatImply">
                        <grouper:param>${attributeNameUpdateRequestContainer.attributeDefNameToEdit.extension}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls">
                      <c:forEach items="${attributeNameUpdateRequestContainer.attributeDefNamesThatImplyThis}" var="attributeDefNameThatImply">
                        <label class="inline">
                            ${attributeDefNameThatImply.extension}
                        </label>
                      </c:forEach> 
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="attributeDeNameEditAttributeDefNamesThatImmediateImply">
                        <grouper:param>${attributeNameUpdateRequestContainer.attributeDefNameToEdit.extension}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls">
                      <c:forEach items="${attributeNameUpdateRequestContainer.allAttributeDefNamesForCurrentAttributeDef}" var="attributeDefName">
                        <c:if test="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.id != attributeDefName.id}">
                          <label class="checkbox inline">
                            <c:set var="checked" value="false" />
                            <c:forEach items="${attributeNameUpdateRequestContainer.attributeDefNamesThatImplyThisImmediate}" var="defNameThatImmediatelyImply">
                              <c:if test="${defNameThatImmediatelyImply.id == attributeDefName.id}">
                                <c:set var="checked" value="true"></c:set>
                              </c:if>
                            </c:forEach>
                            <input type="checkbox" name="defNamesThatImmediatelyImply" value="${attributeDefName.id}" ${checked ? 'checked="checked"' : '' }/>
                              ${attributeDefName.extension}
                          </label>
                        </c:if>
                      </c:forEach>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="attributeDeNameEditAttributeDefNamesImpliedBy">
                        <grouper:param>${attributeNameUpdateRequestContainer.attributeDefNameToEdit.extension}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls">
                      <c:forEach items="${attributeNameUpdateRequestContainer.attributeDefNamesImpliedByThis}" var="defNameImpliedBy">
                        <label class="inline">
                          ${defNameImpliedBy.extension}
                        </label>
                      </c:forEach>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label class="control-label">
                      <grouper:message key="attributeDeNameEditAttributeDefNamesImpliedByImmediate">
                        <grouper:param>${attributeNameUpdateRequestContainer.attributeDefNameToEdit.extension}</grouper:param>
                      </grouper:message>
                    </label>
                    <div class="controls">
                      <c:forEach items="${attributeNameUpdateRequestContainer.allAttributeDefNamesForCurrentAttributeDef}" var="attributeDefName">
                        <c:if test="${attributeNameUpdateRequestContainer.attributeDefNameToEdit.id != attributeDefName.id}">
                          <label class="checkbox inline">
                            <c:set var="checked" value="false" />
                            <c:forEach items="${attributeNameUpdateRequestContainer.attributeDefNamesImpliedByThisImmediate}" var="defNameImpliedByImmediate">
                              <c:if test="${defNameImpliedByImmediate.id == attributeDefName.id}">
                                <c:set var="checked" value="true"></c:set>
                              </c:if>
                            </c:forEach>
                            <input type="checkbox" name="defNamesImpliedByImmediate" value="${attributeDefName.id}" ${checked ? 'checked="checked"' : '' }/>
                              ${attributeDefName.extension}
                          </label>
                        </c:if>
                      </c:forEach>
                    </div>
                  </div>
                 
                  <div class="form-actions">
                    <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2AttributeDefName.editAttributeDefNameInheritanceSubmit', {formIds: 'editAttributeDefNameInheritanceForm'}); return false;">${textContainer.text['attributeDefNameInheritanceEditSaveButton'] }</a> 
                    <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}');" >${textContainer.text['attributeDefNameInhertianceEditCancelButton'] }</a>
                  </div>
                </form>
              </div>
            </div>
            
            
            