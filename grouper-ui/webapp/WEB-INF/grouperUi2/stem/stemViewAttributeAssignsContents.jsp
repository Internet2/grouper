<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <form id="stemQuerySortAscendingFormId">
                  <input type="hidden" name="querySortAscending" value="${grouperRequestContainer.stemContainer.guiSorting.ascending}" /> 
                </form>

                <table class="table table-hover table-bordered table-striped table-condensed data-table">
                  <thead>
                    <tr>
                      <th>${textContainer.text['stemViewAttributeAssignmentsColumnAssignmentType']}</th>
                      <th>${textContainer.text['stemViewAttributeAssignmentsColumnAttributeName']}</th>
                      <th>${textContainer.text['stemViewAttributeAssignmentsColumnEnabled']}</th>
                      <th>${textContainer.text['stemViewAttributeAssignmentsColumnAssignmentValues']}</th>
                      <th>${textContainer.text['stemViewAttributeAssignmentsColumnAttributeDefinition']}</th>
                      <th>${textContainer.text['stemViewAttributeAssignmentsColumnAction']}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach items="${grouperRequestContainer.stemContainer.guiAttributeAssigns}" 
                      var="guiAttributeAssign" >
                      <c:set var="attributeAssign" value="${guiAttributeAssign.attributeAssign}" />
                      <tr>
                        <td>Direct</td>
                        <td style="white-space: nowrap">${attributeAssign.attributeDefName.displayExtension}</td>
                        <td>
                         <c:choose>
					        <c:when test="${attributeAssign.enabled}">
					          ${textContainer.text['stemAttributeAssignEnabledYes']}
					        </c:when>
					        <c:otherwise>
					        	${textContainer.text['stemAttributeAssignEnabledNo']}
					        </c:otherwise>
					      </c:choose>
					    </td>
                        <td>
                          <c:if test="${fn:length(attributeAssign.valueDelegate.attributeAssignValues) > 0}">
                            <table>
                              <thead>
                        	    <tr>
                        	      <th>Value</th>
                                  <th>Actions</th>
                        	    </tr>
                              </thead>
                        		
                              <c:forEach items="${attributeAssign.valueDelegate.attributeAssignValues}" var="attributeAssignValue">
                 			    <tr>
                 	              <td>${grouper:escapeHtml(attributeAssignValue.valueFriendly)}</td>
                                  <td>Show dropdown</td>
                 			    </tr>
                              </c:forEach>
                            </table>
                          </c:if>
                        </td>
                        <td>${attributeAssign.attributeDef.extension}</td>
                        <td>
                          <%-- <div class="btn-group">
                            	<a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreAttributeNameActions']}" href="#" class="btn btn-mini dropdown-toggle" aria-haspopup="true" aria-expanded="false" 
                            		role="menu" onclick="$('#attribute-more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#attribute-more-options${i} li').first().focus();return true;});">
                            		${textContainer.text['attributeDefViewActionsButton'] } <span class="caret"></span></a>
                              <ul class="dropdown-menu dropdown-menu-right" id="attribute-more-options${i}">
                                <li><a href="#"
                                	   onclick="return guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=${guiAttributeDefName.attributeDefName.id}'); return false;">
                                		${textContainer.text['attributeDefViewAttributeDefNameButton'] }
                                	</a>
                                </li>
                                <c:if test="${isAdmin}">
                                	<li><a href="#"
                                	 		onclick="return guiV2link('operation=UiV2AttributeDefName.editAttributeDefName&attributeDefNameId=${guiAttributeDefName.attributeDefName.id}&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;">
                                		${textContainer.text['attributeDefEditAttributeDefNameButton'] }</a>
                                	</li>                                	
                                  <li><a href="#" onclick="ajax('../app/UiV2AttributeDefName.deleteAttributeDefName?attributeDefNameId=${guiAttributeDefName.attributeDefName.id}', {formIds: 'attributeDefFilterFormId,attributeDefPagingFormId'}); return false;" class="actions-delete-attributeDef">${textContainer.text['attributeDefDeleteAttributeDefNameButton'] }</a></li>
                                </c:if>
                              </ul>
                          </div> --%>
                        </td>
                      </tr>
                      
                      <%-- Show the attribute assign values now for the given attribute assign --%>
                      
                      <c:forEach items="${guiAttributeAssign.guiAttributeAssigns}" var="guiAttributeAssignAssign">
                        <tr>
                          <td>Metadata on assignment</td>
                          <td>${guiAttributeAssignAssign.attributeAssign.attributeDefName.displayExtension}</td>
                          <td>
                            <c:choose>
					          <c:when test="${guiAttributeAssignAssign.attributeAssign.enabled}">
					            ${textContainer.text['stemAttributeAssignEnabledYes']}
					          </c:when>
					          <c:otherwise>
					        	${textContainer.text['stemAttributeAssignEnabledNo']}
					          </c:otherwise>
					        </c:choose>
                          </td>
                          <td>
                            <c:if test="${fn:length(guiAttributeAssignAssign.attributeAssign.valueDelegate.attributeAssignValues) > 0}">
                              <table>
                                <thead>
                        	      <tr>
                        	        <th>Value</th>
                                    <th>Actions</th>
                        	      </tr>
                                </thead>
	                            <c:forEach items="${guiAttributeAssignAssign.attributeAssign.valueDelegate.attributeAssignValues}" var="attributeAssignValue">
	                              <tr>
	                 	            <td>${grouper:escapeHtml(attributeAssignValue.valueFriendly)}</td>
	                                <td>Show dropdown</td>
	                 			  </tr>
	                            </c:forEach>
                              </table>
                            </c:if>
                          </td>
                          <td>${guiAttributeAssignAssign.attributeAssign.attributeDef.extension}</td>
                          <td>Dropdown</td>
                        </tr>
                      </c:forEach>
                      
                    </c:forEach>
                  </tbody>
                </table>
                <%-- <div class="data-table-bottom gradient-background">
                  <grouper:paging2 guiPaging="${grouperRequestContainer.stemContainer.guiPaging}" formName="stemPagingAuditForm" ajaxFormIds="stemFilterAuditFormId, stemQuerySortAscendingFormId"
                    refreshOperation="../app/UiV2Stem.viewAuditsFilter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                </div> --%>


