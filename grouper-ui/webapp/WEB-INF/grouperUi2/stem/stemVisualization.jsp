<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('stemVisualizationPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12 tab-interface">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../stem/stemTabs.jsp" %>

                <%@ include file="../visualization/visualizationMain.jsp" %>

              </div>
            </div>
