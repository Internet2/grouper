<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('subjectViewAuditsPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../subject/subjectTabs.jsp" %>

                <p class="lead">${textContainer.text['subjectDataFieldConfigsDescription'] }</p>

                <div id="subjectDataConfigResultsId">
                </div>                
              </div>
            </div>
            <!-- end group/groupViewAudits.jsp -->