<%@ include file="../assetsJsp/commonTaglib.jsp"%>


            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${attributeUpdateRequestContainer.attributeDefToEdit.parentUuid}" />

            <div class="bread-header-container">
               ${attributeUpdateRequestContainer.guiAttributeDefToEdit.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-cog"></i>${grouper:escapeHtml(attributeUpdateRequestContainer.action)}
                <br /><small>${textContainer.text['attributeDefActionEditTitle'] }</small></h1>
              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
                <form id="editAttributeDefActionForm" class="form-horizontal">
                
                  <input type="hidden" name="attributeDefActionId" value="${attributeUpdateRequestContainer.attributeAssignAction.id}" />
                  
                  <div class="control-group">
                    <label class="control-label" style="padding-top: 0px;">${textContainer.text['attributeDeActionEditAttributeDefAction'] }</label>
                    <div class="controls">
                      <span>${grouper:escapeHtml(attributeUpdateRequestContainer.action)}</span>
                      <%-- <span class="help-block">${textContainer.text['attributeDefNameEditIntoAttributeDefDescription'] }</span> --%>
                    </div>
                  </div>
                  
                  <div class="control-group">
                  	<label class="control-label">
                      <grouper:message key="attributeDeActionEditActionsThatImply">
            					  <grouper:param>${attributeUpdateRequestContainer.action}</grouper:param>
                      </grouper:message>
                  	</label>
                    <div class="controls">
                      <c:forEach items="${attributeUpdateRequestContainer.actionsThatImply}" var="actionThatImply">
                        <label class="inline">
                            ${actionThatImply}
                        </label>
                      </c:forEach> 
                    </div>
                  </div>
                  
                  <div class="control-group">
                  	<label class="control-label">
                      <grouper:message key="attributeDeActionEditActionsThatImmediatelyImply">
            					  <grouper:param>${attributeUpdateRequestContainer.action}</grouper:param>
                      </grouper:message>
                  	</label>
                    <div class="controls">
                      <c:forEach items="${attributeUpdateRequestContainer.actions}" var="action">
                        <c:if test="${action != attributeUpdateRequestContainer.action}">
                          <label class="checkbox inline">
                            <c:set var="checked" value="false" />
                            <c:forEach items="${attributeUpdateRequestContainer.actionsThatImplyImmediate}" var="actionThatImmediatelyImply">
                              <c:if test="${actionThatImmediatelyImply == action}">
                                <c:set var="checked" value="true"></c:set>
                              </c:if>
                            </c:forEach>
                            <input type="checkbox" name="actionsThatImmediatelyImply" value="${action}" ${checked ? 'checked="checked"' : '' }/>
                              ${action}
                          </label>
                        </c:if>
                      </c:forEach>
                    </div>
                  </div>
                  
                  <div class="control-group">
                  	<label class="control-label">
                      <grouper:message key="attributeDeActionEditActionsImpliedBy">
            					  <grouper:param>${attributeUpdateRequestContainer.action}</grouper:param>
                      </grouper:message>
                  	</label>
                    <div class="controls">
                      <c:forEach items="${attributeUpdateRequestContainer.actionsImpliedBy}" var="actionImpliedBy">
                        <label class="inline">
	                        ${actionImpliedBy}
	                      </label>
                      </c:forEach>
                    </div>
                  </div>
                  
                  <div class="control-group">
                  	<label class="control-label">
                      <grouper:message key="attributeDeActionEditActionsImmediatelyImpliedBy">
            					  <grouper:param>${attributeUpdateRequestContainer.action}</grouper:param>
                      </grouper:message>
                  	</label>
                    <div class="controls">
                      <c:forEach items="${attributeUpdateRequestContainer.actions}" var="action">
                        <c:if test="${action != attributeUpdateRequestContainer.action}">
                          <label class="checkbox inline">
	                          <c:set var="checked" value="false" />
	                          <c:forEach items="${attributeUpdateRequestContainer.actionsImpliedByImmediate}" var="actionImpliedByImmediate">
	                            <c:if test="${actionImpliedByImmediate == action}">
	                              <c:set var="checked" value="true"></c:set>
	                            </c:if>
	                          </c:forEach>
	                          <input type="checkbox" name="actionsImpliedByImmediate" value="${action}" ${checked ? 'checked="checked"' : '' }/>
	                            ${action}
                          </label>
                        </c:if>
                      </c:forEach>
                    </div>
                  </div>
                 
                  <div class="form-actions">
                    <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2AttributeDefAction.attributeDefActionEditSubmit', {formIds: 'editAttributeDefActionForm'}); return false;">${textContainer.text['attributeDefActionEditSaveButton'] }</a> 
                    <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2AttributeDefAction.attributeDefActions&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}');" >${textContainer.text['attributeDefActionEditCancelButton'] }</a>
                  </div>
                </form>
              </div>
            </div>
            
            
            