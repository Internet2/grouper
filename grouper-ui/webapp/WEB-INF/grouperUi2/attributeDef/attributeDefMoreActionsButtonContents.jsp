<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start attributeDef/attributeDefMoreActionsButtonContents.jsp -->

                    <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin }">
                      <%-- on the privs tab, show the add member button --%>            
                      <c:choose>
                        <c:when test="${grouperRequestContainer.attributeDefContainer.showAddMember}">
                          <a id="show-add-block" href="#" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"><i class="fa fa-plus"></i> ${textContainer.text['attributeDefViewMoreActionsAddMembers'] }</a>
                        </c:when>
                        <c:otherwise>
                          <a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.attributeDefEdit&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;" class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['attributeDefViewEditAttributeDefButton'] }</a>
                        </c:otherwise>
                      </c:choose>
                    </c:if>
                    <div class="btn-group btn-block">
                    	<a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreAttributeDefActions']}" href="#" class="btn btn-medium btn-block dropdown-toggle" aria-haspopup="true" aria-expanded="false" 
                    		role="menu" onclick="$('#attribute-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#attribute-more-options li').first().focus();return true;});">
                    		${textContainer.text['groupViewMoreActionsButton'] } <span class="caret"></span>
                    	</a>
                      <ul class="dropdown-menu dropdown-menu-right" id="attribute-more-options">
                        <%-- add or remove to/from my favorites, this causes a success message --%>
                        <c:choose>
                          <c:when test="${grouperRequestContainer.attributeDefContainer.favorite}">
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2AttributeDef.removeFromMyFavorites?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;" 
                            >${textContainer.text['attributeDefViewMoreActionsRemoveFromMyFavorites'] }</a></li>
                          </c:when>
                          <c:otherwise>
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2AttributeDef.addToMyFavorites?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;" 
                            >${textContainer.text['attributeDefViewMoreActionsAddToMyFavorites'] }</a></li>
                          </c:otherwise>
                        </c:choose>


                        <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin }">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.attributeDefDelete&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;"
                            >${textContainer.text['attributeDefViewDeleteAttributeDefButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDef.attributeDefEdit&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;"
                            >${textContainer.text['attributeDefViewEditAttributeDefButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDefName.newAttributeDefName&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&objectStemId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}'); return false;"
                            >${textContainer.text['attributeDefViewNewAttributeDefNameButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2AttributeDefAction.newAttributeDefAction&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}&objectStemId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}'); return false;"
                            >${textContainer.text['attributeDefViewNewAttributeDefActionButton'] }</a></li>
                        </c:if>
                      </ul>
                    </div>

                    <!-- end attributeDef/attributeDefMoreActionsButtonContents.jsp -->
                    