<!-- ./webapp/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp -->

<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start stem/stemMoreActionsButtonContents.jsp -->

                    <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">
                      <%-- on the privs tab, show the add member button --%>
                      <c:choose>
                        <c:when test="${grouperRequestContainer.stemContainer.showAddMember}">
                          <a id="show-add-block" href="#" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"><i class="fa fa-plus"></i> ${textContainer.text['stemViewMoreActionsAddMembers'] }</a>
                        </c:when>
                        <c:otherwise>
                          <a href="#" onclick="return guiV2link('operation=UiV2Stem.stemEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['stemViewEditStemButton'] }</a>
                        </c:otherwise>
                      </c:choose>
                    </c:if>
                    <%-- HJ 20150319 
                    <div class="btn-group btn-block"><a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle">More actions<span class="caret"></span></a>
                    --%>
                    <div class="btn-group btn-block">
                    	<a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle" 
                    		aria-haspopup="true" aria-label="${textContainer.text['ariaLabelGuiMoreStemActions']}" aria-expanded="false" role="menu" 
                    		onclick="$('#stem-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#stem-more-options li').first().focus();return true;});">
                    		${textContainer.text['stemViewMoreActionsButton'] } <span class="caret"></span></a>
                      <ul class="dropdown-menu dropdown-menu-right" id="stem-more-options">
                        <%-- add or remove to/from my favorites, this causes a success message --%>
                        <c:choose>
                          <c:when test="${grouperRequestContainer.stemContainer.favorite}">
                            <li><a href="#"
                            onclick="ajax('../app/UiV2Stem.removeFromMyFavorites?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                            <%-- HJ 20150319 
                            >Remove from my favorites</a></li>
                            --%>
                            >${textContainer.text['stemViewMoreActionsRemoveFromMyFavorites']}</a></li>
                          </c:when>
                          <c:otherwise>
                            <li><a href="#"
                            onclick="ajax('../app/UiV2Stem.addToMyFavorites?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                            <%-- HJ 20150319 
                            >Add to my favorites</a></li>
                            --%>
                            >${textContainer.text['stemViewMoreActionsAddToMyFavorites']}</a></li>
                          </c:otherwise>
                        </c:choose>

                        <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges || grouperRequestContainer.stemContainer.canCreateGroups }">

                          <li class="divider"></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.stemContainer.canCreateStems }">
                          <li><a href="#"
                          onclick="return guiV2link('operation=UiV2Stem.newStem', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['stemNewCreateNewStemMenuButton'] }</a></li>

                        </c:if>
                        <c:if test="${grouperRequestContainer.stemContainer.canCreateGroups }">

                          <li><a href="#"
                            onclick="return guiV2link('operation=UiV2Group.newGroup', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['groupNewCreateNewGroupMenuButton'] }</a></li>

                        </c:if>
                        
                        <c:if test="${grouperRequestContainer.stemContainer.canReadAttributes }">
                        
                          <li class="divider"></li>
                          <li>
                            <a href="#" onclick="return guiV2link('operation=UiV2StemAttributeAssignment.viewAttributeAssignments&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">
                              ${textContainer.text['stemViewAttributeAssignmentsButton'] }
                            </a>
                          </li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">

                          <li class="divider"></li>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemCopy&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['stemViewCopyStemButton'] }</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemDelete&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['stemViewDeleteStemButton'] }</a></li>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['stemViewEditStemButton'] }</a></li>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemMove&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['stemViewMoveStemButton'] }</a></li>
                          <li class="divider"></li>
                          <c:if test="${isWheelGroupMember || grouperRequestContainer.stemContainer.canAdminPrivileges}">
                            <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Attestation.stemAttestation&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['attestationButton'] }</a></li>
                          </c:if>         
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.viewAudits&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['stemViewAuditButton'] }</a></li>
                        </c:if>
                      </ul>
                    </div>

