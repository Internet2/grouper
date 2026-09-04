<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <form id="attributeDefActionsToDeleteFormId">
                <c:set var="isAdmin" value="${grouperRequestContainer.attributeDefContainer.canAdmin}" />
                <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <c:if test="${isAdmin}" >
                      <tr>
                        <td colspan="3" class="table-toolbar gradient-background"><button type="button" onclick="ajax('../app/UiV2AttributeDefAction.deleteAttributeDefActions?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {formIds: 'attributeDefFilterFormId,attributeDefActionsToDeleteFormId'}); return false;" class="btn">${textContainer.text['attributeDefRemoveSelectedAttributeDefActionsButton'] }</button></td>
                      </tr>
                    </c:if>
                    <tr>
                      <c:if test="${isAdmin}" >
                        <th>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.attributeDefActionCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                            <span class="sr-only">${textContainer.text['attributeDefDetailsCheckboxAriaLabel']}</span>
                          </label>
                        </th>
                      </c:if>
                      <th>${textContainer.text['attributeDefHeaderAction'] }</th>
                      <th style="width:100px;">${textContainer.text['headerChooseAction']}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach items="${grouperRequestContainer.attributeDefContainer.attributeAssignActions}" var="attributeAssignAction">
                      <tr>
                        <c:if test="${isAdmin}" >
                          <td>
                            <label class="checkbox checkbox-no-padding">
                              <input type="checkbox" name="attributeDefAction_${i}" aria-label="${textContainer.text['attributeDefDetailsCheckboxAriaLabel']}"
                               value="${attributeAssignAction.id}" class="attributeDefActionCheckbox" />
                            </label>
                          </td>
                        </c:if>
                        <td>${attributeAssignAction.name}</td>
                        <td>
                          <div class="btn-group">
                          	<button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreAttributeActionActions']}" class="btn btn-mini dropdown-toggle" aria-haspopup="true" aria-expanded="false" onclick="$('#attribute-more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#attribute-more-options${i} li').first().focus();return true;});">
                          		${textContainer.text['attributeDefViewActionsButton'] } <span class="caret"></span></button>
                            <ul class="dropdown-menu dropdown-menu-right" id="attribute-more-options${i}">
                            
                              <c:if test="${isAdmin}">
                              	<li><a href="?operation=UiV2AttributeDefAction.editAttributeDefAction&attributeDefActionId=${attributeAssignAction.id}&attributeDefId=${attributeAssignAction.attributeDefId}"
                              	 		onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDefAction.editAttributeDefAction&attributeDefActionId=${attributeAssignAction.id}&attributeDefId=${attributeAssignAction.attributeDefId}'); return false;">
                              		${textContainer.text['attributeDefEditAttributeDefActionButton'] }</a>
                              	</li>                                	
                                <li><a href="?operation=UiV2AttributeDefAction.deleteAttributeDefAction&attributeDefActionId=${attributeAssignAction.id}&attributeDefId=${attributeAssignAction.attributeDefId}"
                                    onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDefAction.deleteAttributeDefAction&attributeDefActionId=${attributeAssignAction.id}&attributeDefId=${attributeAssignAction.attributeDefId}'); return false;" class="actions-delete-attributeDefAction">
                                  ${textContainer.text['attributeDefDeleteAttributeDefActionButton'] }</a></li>
                              </c:if>
                            </ul>
                          </div>
                        </td>
                      </tr>
                      <c:set var="i" value="${i+1}" />
                    </c:forEach>
                  </tbody>
                </table>
              </form>
