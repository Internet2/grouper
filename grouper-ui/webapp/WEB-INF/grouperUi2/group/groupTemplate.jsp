<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('groupTemplatePageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}

           <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../group/groupTabs.jsp" %>
              </div>
              <%-- <div>
                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['stemAttestationSettingsTitle'] }</div>
                  <div class="span3" id="stemAttestationMoreActionsButtonContentsDivId">
                    <%@ include file="stemAttestationMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <div class="row-fluid">
                  <div class="span12">
                    <p style="margin-top: 0em; margin-bottom: 1em">${textContainer.text['stemAttestationSettingsDescription']}</p>
                  </div>
                </div>
              </div> --%>
              <div id="groupTemplate">
                
              
              </div>
            </div>
