<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <%@ include file="groupMoreTab.jsp" %>
                </ul>
                <div class="row-fluid">
                  <div class="lead span10">${textContainer.text['grouperLoaderManagedGroupsTitle'] }</div>
                  <div class="span2" id="grouperLoaderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperLoaderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <br />
                <div id="loaderManagedGroupsId">
                
                	<table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                  <thead>
                    <tr>
                    
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right">${textContainer.text['loaderManagedGroupsColumnHeaderGroupName'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right">${textContainer.text['loaderManagedGroupsColumnHeaderLastLoadedTime'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right">${textContainer.text['loaderManagedGroupsColumnHeaderSummary'] }</span></th>
                      
                    </tr>
                  </thead>
                  <tbody>

                    <c:forEach var="guiLoaderManagedGroup" items="${grouperRequestContainer.groupContainer.guiLoaderManagedGroups}" >
                      
                      <tr>
                      
                        <td>${guiLoaderManagedGroup.guiGroup.shortLinkWithIcon}</td>
                      
                        <td>
                          <c:choose>
                            <c:when test="${guiLoaderManagedGroup.grouperLoaderMetadataLastFullMillisSince1970 == null}">
                              ${guiLoaderManagedGroup.grouperLoaderMetadataLastIncrementalMillisSince1970}
                            </c:when>
                            
                           <%--  <c:when test="${guiHib3GrouperLoaderLog.loadedGuiGroup == null && guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.parentJobId != null}">
                            
                              ${textContainer.text['grouperLoaderLogsLoadedGroupNotFound']}
                            </c:when> --%>

                            <c:otherwise>
                              ${guiLoaderManagedGroup.grouperLoaderMetadataLastFullMillisSince1970}
                            </c:otherwise>
                          
                          </c:choose>
                        </td>
                        
                        <td>
                         ${guiLoaderManagedGroup.grouperLoaderMetadataLastSummary}
                        </td>
                        
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
                
                </div>
                
              </div>
            </div>
