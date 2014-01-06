<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start stem/stemMoreActionsButtonContents.jsp -->

                    <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">

                      <a href="#" onclick="return guiV2link('operation=UiV2Stem.stemEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" class="btn btn-medium btn-block btn-primary">${textContainer.text['stemViewEditStemButton'] }</a>
                    
                    </c:if>
                    <div class="btn-group btn-block"><a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle">More actions <span class="caret"></span></a>
                      <ul class="dropdown-menu dropdown-menu-right">
                        <%-- add or remove to/from my favorites, this causes a success message --%>
                        <c:choose>
                          <c:when test="${grouperRequestContainer.stemContainer.favorite}">
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2Stem.removeFromMyFavorites?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" 
                            >Remove from my favorites</a></li>
                          </c:when>
                          <c:otherwise>
                            <li><a href="#" 
                            onclick="ajax('../app/UiV2Stem.addToMyFavorites?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" 
                            >Add to my favorites</a></li>
                          </c:otherwise>
                        </c:choose>

                        <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges || grouperRequestContainer.stemContainer.canCreateGroups }">

                          <li class="divider"></li>
                        </c:if>  
                        <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">
                          <li><a href="#" 
                          onclick="return guiV2link('operation=UiV2Stem.newStem', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['stemNewCreateNewStemMenuButton'] }</a></li>

                        </c:if>  
                        <c:if test="${grouperRequestContainer.stemContainer.canCreateGroups }">

                          <li><a href="#" 
                            onclick="return guiV2link('operation=UiV2Group.newGroup', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['groupNewCreateNewGroupMenuButton'] }</a></li>

                        </c:if>  

                        <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">
  
                          <li class="divider"></li>
                        
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemCopy&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >Copy folder</a></li>
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemDelete&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >Delete folder</a></li>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" class="btn btn-medium btn-block btn-primary">${textContainer.text['stemViewEditStemButton'] }</a></li>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemMove&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >Move folder</a></li>
                          <li class="divider"></li>
                          <li><a href="voew-audit-log.html">View audit log</a></li>
                        </c:if>
                      </ul>
                    </div>

                    