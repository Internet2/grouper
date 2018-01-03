<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start attributeDefName/attributeDefNameMoreActionsButtonContents.jsp -->

                    <%--
                    <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin }">
                      <a href="#" onclick="return guiV2link('operation=UiV2AttributeDefName.editAttributeDefName&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}'); return false;" class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['attributeDefNameViewEditAttributeDefNameButton'] }</a>
                    </c:if>
                    --%>
                    
                    <div class="btn-group btn-block">
                      <a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreAttributeNameActions']}" href="#" class="btn btn-medium btn-block dropdown-toggle" aria-haspopup="true" aria-expanded="false" 
                        role="menu" onclick="$('#attribute-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#attribute-more-options li').first().focus();return true;});">
                        ${textContainer.text['attributeDefViewActionsButton'] } <span class="caret"></span></a>
                      <ul class="dropdown-menu dropdown-menu-right" id="attribute-more-options">
                        <li><a href="#"
                             onclick="return guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}'); return false;">
                            ${textContainer.text['attributeDefViewAttributeDefNameButton'] }
                          </a>
                        </li>

                        <c:choose>
                          <c:when test="${grouperRequestContainer.attributeDefNameContainer.favorite}">
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2AttributeDefName.removeFromMyFavorites?attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}'); return false;" 
                            >${textContainer.text['attributeDefNameViewMoreActionsRemoveFromMyFavorites'] }</a></li>
                          </c:when>
                          <c:otherwise>
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2AttributeDefName.addToMyFavorites?attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}'); return false;" 
                            >${textContainer.text['attributeDefNameViewMoreActionsAddToMyFavorites'] }</a></li>
                          </c:otherwise>
                        </c:choose>

                        <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin}">
                          <li><a href="#"
                              onclick="return guiV2link('operation=UiV2AttributeDefName.editAttributeDefName&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}'); return false;">
                            ${textContainer.text['attributeDefEditAttributeDefNameButton'] }</a>
                          </li>                                 
                          <li><a href="#" 
                            onclick="return guiV2link('operation=UiV2AttributeDefName.deleteAttributeDefName&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}'); return false;" class="actions-delete-attributeDef">
                            ${textContainer.text['attributeDefDeleteAttributeDefNameButton'] }</a></li>
                        </c:if>
                      </ul>
                    </div>


                    
                    <!-- end attributeDefName/attributeDefNameMoreActionsButtonContents.jsp -->
                    