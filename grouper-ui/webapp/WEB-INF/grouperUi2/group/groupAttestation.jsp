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
                  <div class="lead span10">${textContainer.text['groupAttestationTitle'] }</div>
                  <div class="span2" id="grouperLoaderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperLoaderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <c:choose>
                  <c:when test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup}">
                    <p>${textContainer.text['grouperLoaderIsGrouperLoader'] }</p>
                  </c:when>
                  <c:otherwise>
                    <p>${textContainer.text['noAttestationConfigured'] }</p>
                  </c:otherwise>
                  
                  attestationConfiguredForGroup = Attestation is configured on this group
attestationConfiguredForStem = Attestation is configured on this folder
attestationConfiguredForAncestorStem = Attestation is configured an ancestor folder
                  
                  
                </c:choose>

                <c:choose>
                  <c:when test="${true}">
                    <table class="table table-condensed table-striped">
                      <tbody>
                        <tr>
                          <td style="vertical-align: top; white-space: nowrap;"><strong>Label</strong></td>
                          <td>value<br />
                            <span class="description">description</span>
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </c:when>
                </c:choose>

                <div id="groupAttestation">
                
                </div>

              </div>
            </div>
