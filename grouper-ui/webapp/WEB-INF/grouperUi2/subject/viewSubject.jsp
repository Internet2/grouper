<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('viewSubjectPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="memberships" />
                <%@ include file="../subject/subjectTabs.jsp" %>
                <p class="lead">${textContainer.text['subjectViewGroupsDescription'] }</p>
                
                <c:if test="${grouperRequestContainer.subjectContainer.guiSubject.isLocalEntityDisabled()}">
                  <p class="lead" style="color: red">${textContainer.text['localEntityViewLocalEntityDisabled'] }
                </c:if>
                
                <form class="form-inline form-small form-filter" id="groupFilterFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['subjectFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" name="membershipType">
                        <option value="">${textContainer.text['subjectFilterAllAssignments']}</option>
                        <option value="IMMEDIATE">${textContainer.text['subjectFilterDirectAssignments']}</option>
                        <option value="NONIMMEDIATE">${textContainer.text['subjectFilterIndirectAssignments']}</option>
                      </select>
                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['subjectFilterFormPlaceholder']}" 
                         name="filterText" id="table-filter" class="span12"/>
                    </div>

                    <div class="span3"><input type="submit" aria-controls="subjectFilterResultsId" class="btn"  id="filterSubmitId" value="${textContainer.textEscapeDouble['subjectApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Subject.filter?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'groupFilterFormId,groupPagingFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['subjectResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="subjectFilterResultsId" role="region" aria-live="polite">
                </div>                
              </div>
            </div>
            <!-- end group/viewSubject.jsp -->