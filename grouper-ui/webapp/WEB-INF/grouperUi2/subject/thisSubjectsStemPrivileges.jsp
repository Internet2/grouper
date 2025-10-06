<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('stemPrivilegesPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

            <!-- start subject/thisSubjectsStemPrivileges.jsp -->

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
            
            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="stemPrivileges" />
                <%@ include file="../subject/subjectTabs.jsp" %>

                <p class="lead">${textContainer.text['thisSubjectsStemPrivilegesDescription'] }</p>
                <form class="form-inline form-small form-filter" id="groupFilterPrivilegesFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['groupFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" class="span12" name="privilegeField">
                        <option value="">${textContainer.text['groupFilterAllStems'] }</option>
                        <option value="stemAdmin">${textContainer.text['thisGroupsPrivilegesPriv_stemAdmin'] }</option>
                        <option value="create">${textContainer.text['thisGroupsPrivilegesPriv_create'] }</option>
                        <option value="stemAttrRead">${textContainer.text['thisGroupsPrivilegesPriv_stemAttrRead'] }</option>
                        <option value="stemAttrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_stemAttrUpdate'] }</option>
                      </select>

                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['thisSubjectsPrivilegesStemFilterFormPlaceholder']}" 
                         name="privilegeFilterText" id="table-filter" class="span12" aria-label="${textContainer.text['ariaLabelGuiEntityName']}"/>
                    </div>

                    <div class="span3"><input type="submit" class="btn" aria-controls="thisSubjectsStemPrivilegesFilterResultsId" id="filterSubmitId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Subject.filterThisSubjectsStemPrivileges?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="thisSubjectsStemPrivilegesFilterResultsId" role="region" aria-live="polite">
                </div>                
              </div>
            </div>
            <!-- end subject/thisSubjectsStemPrivileges.jsp -->
