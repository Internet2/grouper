<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start attributeDef/attributeDefMoreActionsButtonContents.jsp -->

                    <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin }">
                      <%-- on the privs tab, show the add member button --%>            
                      <c:choose>
                        <c:when test="${grouperRequestContainer.attributeDefContainer.showAddMember}">
                          <button type="button" id="show-add-block" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"><i aria-hidden="true" class="fa fa-plus"></i> ${textContainer.text['attributeDefViewMoreActionsAddMembers'] }</button>
                        </c:when>
                        <c:otherwise>
                          <a href="?operation=UiV2AttributeDef.attributeDefEdit&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDef.attributeDefEdit&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;" class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['attributeDefViewEditAttributeDefButton'] }</a>
                        </c:otherwise>
                      </c:choose>
                    </c:if>
                    <div class="btn-group btn-block">
                    	<button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreAttributeDefActions']}" class="btn btn-medium btn-block dropdown-toggle" aria-haspopup="true" aria-expanded="false" onclick="$('#attribute-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#attribute-more-options li').first().focus();return true;});">
                    		${textContainer.text['attributeDefViewMoreActionsButton'] } <span class="caret"></span>
                    	</button>
                      <ul class="dropdown-menu dropdown-menu-right" id="attribute-more-options">
                        <%-- add or remove to/from my favorites, this causes a success message --%>
                        <c:choose>
                          <c:when test="${grouperRequestContainer.attributeDefContainer.favorite}">
                            <li><button type="button" class="grouper-menuitem" 
                            onclick="ajax('../app/UiV2AttributeDef.removeFromMyFavorites?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;" 
                            >${textContainer.text['attributeDefViewMoreActionsRemoveFromMyFavorites'] }</button></li>
                          </c:when>
                          <c:otherwise>
                            <li><button type="button" class="grouper-menuitem"
                            onclick="ajax('../app/UiV2AttributeDef.addToMyFavorites?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;" 
                            >${textContainer.text['attributeDefViewMoreActionsAddToMyFavorites'] }</button></li>
                          </c:otherwise>
                        </c:choose>


                        <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin }">
                          <li><a href="?operation=UiV2AttributeDef.attributeDefDelete&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDef.attributeDefDelete&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;"
                            >${textContainer.text['attributeDefViewDeleteAttributeDefButton'] }</a></li>
                          <li><a href="?operation=UiV2AttributeDef.attributeDefEdit&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDef.attributeDefEdit&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;"
                            >${textContainer.text['attributeDefViewEditAttributeDefButton'] }</a></li>
                          <li><a href="?operation=UiV2AttributeDefName.newAttributeDefName&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&objectStemId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDefName.newAttributeDefName&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&objectStemId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}'); return false;"
                            >${textContainer.text['attributeDefViewNewAttributeDefNameButton'] }</a></li>
                            <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'perm'}">
                              <li><a href="?operation=UiV2AttributeDefAction.newAttributeDefAction&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&objectStemId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDefAction.newAttributeDefAction&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&objectStemId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}'); return false;"
                                >${textContainer.text['attributeDefViewNewAttributeDefActionButton'] }</a></li>
                            </c:if>
                        </c:if>
                        <c:if test="${grouperRequestContainer.attributeDefContainer.canRead }">
                          <li><a href="?operation=UiV2AttributeDefAttributeAssignment.viewAttributeAssignments&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&objectStemId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDefAttributeAssignment.viewAttributeAssignments&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&objectStemId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}'); return false;"
                            >${textContainer.text['attributeDefViewAttributeAssignments'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.deprovisioningContainer.canReadDeprovisioning}">
                          <li><a href="?operation=UiV2Deprovisioning.deprovisioningOnAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Deprovisioning.deprovisioningOnAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;"
                            >${textContainer.text['deprovisioningMoreActionsMenuLabel'] }</a></li>
                        </c:if>  
                        <li><a href="?operation=UiV2AttributeDef.viewAttributeDefAssignedOwners&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}"
                              onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AttributeDef.viewAttributeDefAssignedOwners&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;">
                            ${textContainer.text['attributeDefViewAssignedEntitiesAttributeDefButton'] }</a>
                        </li>       
                        
                      </ul>
                    </div>

                    <!-- end attributeDef/attributeDefMoreActionsButtonContents.jsp -->
                    