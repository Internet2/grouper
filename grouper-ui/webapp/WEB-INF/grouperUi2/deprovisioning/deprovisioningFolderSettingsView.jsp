<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="../stem/stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../stem/stemTabs.jsp" %>
                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['deprovisioningStemSettingsTitle'] }</div>
                  <div class="span3" id="deprovisioningFolderMoreActionsButtonContentsDivId">
                    <%@ include file="deprovisioningFolderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <c:set var="ObjectType" 
                    value="Folder" />
                <%@ include file="deprovisioningObjectSettingsView.jsp"%>

              </div>
            </div>
