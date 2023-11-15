<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('stemObjectTypesPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="../stem/stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                  <%@ include file="../stem/stemMoreTab.jsp" %>
                </ul>
                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['objectTypeFolderSettingsTitle'] }</div>
                  <div class="span3" id="grouperTypesFolderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperObjectTypesFolderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <c:set var="ObjectType" value="Folder" />
                <%@ include file="objectTypeObjectSettingsView.jsp"%>

              </div>
            </div>
