<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.attributeDefContainer}" property="showAddMember" value="true" />

            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <ul class="nav nav-tabs">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectMembershipsTab'] }</a></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsGroupPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectPrivilegesTab'] }</a></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsStemPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectStemPrivilegesTab'] }</a></li>
                  <li class="active"><a href="#" onclick="return false;" >${textContainer.text['subjectAttributePrivilegesTab'] }</a></li>
                </ul>


                <p class="lead">${textContainer.text['thisSubjectsAttributeDefPrivilegesDescription'] }</p>
                <form class="form-inline form-small form-filter" id="groupFilterPrivilegesFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['groupFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" class="span12" name="privilegeField">
                        <option value="">${textContainer.text['groupFilterAllAttributeDefs'] }</option>
                        <option value="attrAdmin">${textContainer.text['thisGroupsPrivilegesPriv_attrAdmin'] }</option>
                        <option value="attrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_attrUpdate'] }</option>
                        <option value="attrRead">${textContainer.text['thisGroupsPrivilegesPriv_attrRead'] }</option>
                        <option value="attrView">${textContainer.text['thisGroupsPrivilegesPriv_attrView'] }</option>
                        <option value="attrOptin">${textContainer.text['thisGroupsPrivilegesPriv_attrOptin'] }</option>
                        <option value="attrOptout">${textContainer.text['thisGroupsPrivilegesPriv_attrOptout'] }</option>
                        <option value="attrDefAttrRead">${textContainer.text['thisGroupsPrivilegesPriv_attrDefAttrRead'] }</option>
                        <option value="attrDefAttrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_attrDefAttrUpdate'] }</option>
                      </select>

                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['thisGroupsPrivilegesAttributeDefFilterFormPlaceholder']}" 
                         name="privilegeFilterText" id="table-filter" class="span12"/>
                    </div>

                    <div class="span3"><input type="submit" class="btn"  id="filterSubmitId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Subject.filterThisSubjectsAttributeDefPrivileges?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="thisSubjectsAttributeDefPrivilegesFilterResultsId">
                </div>                
              </div>
            </div>
            <!-- end subject/thisSubjectsAttributeDefPrivileges.jsp -->