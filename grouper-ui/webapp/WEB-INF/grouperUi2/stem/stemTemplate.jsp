<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('stemTemplatePageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../stem/stemTabs.jsp" %>
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
              <div id="stemTemplate">
                
              
              </div>
            </div>
