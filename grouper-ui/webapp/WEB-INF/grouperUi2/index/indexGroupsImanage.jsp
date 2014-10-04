<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                    <!-- start indexGroupsIManage.jsp -->
                    <h4>${textContainer.text['indexMyGroupsTitle'] }</h4>
                    <c:choose>
                      <c:when test="${grouperRequestContainer.indexContainer.groupsImanageRetrieved}">
                        <ul class="unstyled list-widget">
                          <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsUserManagesAbbreviated}" var="guiGroup">
                            <li>
                            ${guiGroup.shortLinkWithIconAndPath }
                            </li>
                          </c:forEach>
                        </ul>
                      </c:when>
                      <c:otherwise>
                        <a href="#" onclick="ajax('UiV2Main.indexColGroupsImanage?col=${col}&storePref=false'); return false;">${textContainer.text['indexMyGroupsTitleLoad'] }</a>
                        <br /><br /><br /><br/>
                      </c:otherwise>
                    </c:choose>
                    
                    <p><strong><a href="#" 
                  onclick="return guiV2link('operation=UiV2MyGroups.myGroups');">${textContainer.text['indexMyGroupsViewAllGroups'] }</a>  </strong></p>
                    <!-- end indexGroupsIManage.jsp -->
                    