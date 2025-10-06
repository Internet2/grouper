<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('subjectVisualizationPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

            <!-- start subject/subjectVisualization.jsp -->

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
            
            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../subject/subjectTabs.jsp" %>

                <%@ include file="../visualization/visualizationMain.jsp" %>

              </div>
            </div>
            <!-- end subject/subjectVisualization.jsp -->
