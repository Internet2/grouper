<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <form id="attributeDefNamesToDeleteFormId">
                <c:set var="isAdmin" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.hasAdmin}" />
                <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <c:if test="${isAdmin}" >
                      <tr>
                        <td colspan="4" class="table-toolbar gradient-background"><a href="#" onclick="ajax('../app/UiV2AttributeDef.deleteAttributeDefNames?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {formIds: 'attributeDefFilterFormId,attributeDefPagingFormId,attributeDefNamesToDeleteFormId'}); return false;" class="btn">${textContainer.text['attributeDefRemoveSelectedAttributeDefNamesButton'] }</a></td>
                      </tr>
                    </c:if>
                    <tr>
                      <c:if test="${isAdmin}" >
                        <th>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" onchange="$('.attributeDefNameCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                          </label>
                        </th>
                      </c:if>
                      <th class="sorted">${textContainer.text['attributeDefHeaderStem'] }</th>
                      <th class="sorted">${textContainer.text['attributeDefHeaderName'] }</th>
                      <c:if test="${isAdmin}" >
                        <th style="width:100px;">${textContainer.text['headerChooseAction']}</th>
                      </c:if>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach items="${grouperRequestContainer.attributeDefContainer.guiAttributeDefNames}" var="guiAttributeDefName">
                      <tr>
                        <c:if test="${isAdmin}" >
                          <td>
                            <label class="checkbox checkbox-no-padding">
                              <input type="checkbox" name="attributeDefName_${i}" aria-label="${textContainer.text['attributeDefDetailsCheckboxAriaLabel']}"
                               value="${guiAttributeDefName.attributeDefName.id}" class="attributeDefNameCheckbox" />
                            </label>
                          </td>
                        </c:if>
                        <td>${guiAttributeDefName.parentGuiStem.shortLinkWithIcon }</td>
                        <td>${guiAttributeDefName.shortLinkWithIcon }</td>
                        <c:if test="${isAdmin}" >
                          <td>
                            <div class="btn-group">
                            	<a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreAttributeNameActions']}" href="#" class="btn btn-mini dropdown-toggle" aria-haspopup="true" aria-expanded="false" 
                            		role="menu" onclick="$('#attribute-more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#attribute-more-options${i} li').first().focus();return true;});">
                            		${textContainer.text['attributeDefViewActionsButton'] } <span class="caret"></span></a>
                              <ul class="dropdown-menu dropdown-menu-right" id="attribute-more-options${i}">
                                <li><a href="#"
                                	   onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDefName&attributeDefNameId=${guiAttributeDefName.attributeDefName.id}'); return false;">
                                		${textContainer.text['attributeDefViewAttributeDefNameButton'] }
                                	</a>
                                </li>
                                <c:if test="${isAdmin}">
                                	<li><a href="#"
                                	 		onclick="return guiV2link('operation=UiV2AttributeDef.editAttributeDefName&attributeDefNameId=${guiAttributeDefName.attributeDefName.id}&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;">
                                		${textContainer.text['attributeDefEditAttributeDefNameButton'] }</a>
                                	</li>                                	
                                  <li><a href="#" onclick="ajax('../app/UiV2AttributeDef.deleteAttributeDefName?attributeDefNameId=${guiAttributeDefName.attributeDefName.id}', {formIds: 'attributeDefFilterFormId,attributeDefPagingFormId'}); return false;" class="actions-delete-attributeDef">${textContainer.text['attributeDefDeleteAttributeDefNameButton'] }</a></li>
                                </c:if>
                              </ul>
                            </div>
                          </td>
                        </c:if>
                      </tr>
                      <c:set var="i" value="${i+1}" />
                    </c:forEach>
                  </tbody>
                </table>
              </form>
              <div class="data-table-bottom gradient-background">
                <grouper:paging2 guiPaging="${grouperRequestContainer.attributeDefContainer.guiPaging}" formName="attributeDefPagingForm" ajaxFormIds="attributeDefFilterFormId"
                  refreshOperation="../app/UiV2AttributeDef.filter?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" />
              </div>
