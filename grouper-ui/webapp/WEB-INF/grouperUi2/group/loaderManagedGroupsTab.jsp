<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../group/groupTabs.jsp" %>
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
                      
                        <td>${guiLoaderManagedGroup.groupBeingManaged.shortLinkWithIcon}</td>
                      
                        <td>
                          <c:choose>
                            <c:when test="${guiLoaderManagedGroup.grouperLoaderMetadataLastFullMillisSince1970 == null}">
                              ${guiLoaderManagedGroup.grouperLoaderMetadataLastIncrementalMillisSince1970}
                            </c:when>

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
