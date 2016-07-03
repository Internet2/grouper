<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <!-- start subject/thisSubjectsStemPrivileges.jsp -->

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.stemContainer}" property="showAddMember" value="true" />
            
            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <ul class="nav nav-tabs">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectMembershipsTab'] }</a></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsGroupPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectPrivilegesTab'] }</a></li>
                  <li class="active"><a href="#" onclick="return false;" >${textContainer.text['subjectStemPrivilegesTab'] }</a></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectAttributePrivilegesTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance}">
                    <%@ include file="subjectMoreTab.jsp" %>
                  </c:if>
                </ul>


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
                         name="privilegeFilterText" id="table-filter" class="span12"/>
                    </div>

                    <div class="span3"><input type="submit" class="btn"  id="filterSubmitId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Subject.filterThisSubjectsStemPrivileges?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="thisSubjectsStemPrivilegesFilterResultsId">
                </div>                
              </div>
            </div>
            <!-- end subject/thisSubjectsStemPrivileges.jsp -->
