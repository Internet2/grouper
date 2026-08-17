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
                        <button type="button" class="btn-link" style="padding:0;border:0;background:none;font-family:inherit;font-size:inherit;line-height:inherit;vertical-align:baseline;" onclick="ajax('UiV2Main.indexColGroupsImanage?col=${col}&storePref=false'); return false;">${textContainer.text['indexMyGroupsTitleLoad'] }</button>
                        <br /><br /><br /><br/>
                      </c:otherwise>
                    </c:choose>
                    
                    <p><strong><a href="?operation=UiV2MyGroups.myGroups"
                  onclick="return handleGuiV2LinkClick(event, 'operation=UiV2MyGroups.myGroups');">${textContainer.text['indexMyGroupsViewAllGroups'] }</a>  </strong></p>
                    <!-- end indexGroupsIManage.jsp -->
                    