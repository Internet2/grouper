<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('stemAttributeAssignmentsPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="../stem/stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../stem/stemTabs.jsp" %>
                
                <div id="stemAttributeAssignments">
                  <%@ include file="stemAttributeAssignmentSection.jsp"%>
                </div>

              </div>
            </div>
