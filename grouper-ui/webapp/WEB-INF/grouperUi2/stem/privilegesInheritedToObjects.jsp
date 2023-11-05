<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('stemInheritedPrivilegesPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.name)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
            
            <c:if test="${grouperRequestContainer.stemContainer.canUpdatePrivilegeInheritance}">
              <%-- show the add member button for privileges --%>
              <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
              <c:set target="${grouperRequestContainer.stemContainer}" property="showAddInheritedPrivileges" value="true" />
            </c:if>
            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <%@ include file="stemMoreTab.jsp" %>
                </ul>
                <p class="lead">${textContainer.text['stemPrivilegesInheritedDecription'] }</p>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="privilegesInheritedResultsId">
                </div>                
              </div>
            </div>
