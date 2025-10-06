<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('groupPrivilegesPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <c:set var="grouperCurrentTab" value="groupPrivileges" />
                <%@ include file="../subject/subjectTabs.jsp" %>

                <p class="lead">${textContainer.text['thisSubjectsGroupPrivilegesDescription'] }</p>
                <form class="form-inline form-small form-filter" id="groupFilterPrivilegesFormId">
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['groupFilterFor'] }</label>
                    </div>
                    <div class="span4">
                      <select id="people-filter" class="span12" name="privilegeField">
                        <option value="">${textContainer.text['groupFilterAllGroups'] }</option>
                        <option value="admin">${textContainer.text['thisGroupsPrivilegesPriv_admin'] }</option>
                        <option value="update">${textContainer.text['thisGroupsPrivilegesPriv_update'] }</option>
                        <option value="read">${textContainer.text['thisGroupsPrivilegesPriv_read'] }</option>
                        <option value="view">${textContainer.text['thisGroupsPrivilegesPriv_view'] }</option>
                        <option value="optin">${textContainer.text['thisGroupsPrivilegesPriv_optin'] }</option>
                        <option value="optout">${textContainer.text['thisGroupsPrivilegesPriv_optout'] }</option>
                        <option value="groupAttrRead">${textContainer.text['thisGroupsPrivilegesPriv_groupAttrRead'] }</option>
                        <option value="groupAttrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_groupAttrUpdate'] }</option>
                        <%-- option value="create">${textContainer.text['thisGroupsPrivilegesPriv_create'] }</option>
                        <option value="stemAdmin">${textContainer.text['thisGroupsPrivilegesPriv_stemAdmin'] }</option>
                        <option value="stemAttrRead">${textContainer.text['thisGroupsPrivilegesPriv_stemAttrRead'] }</option>
                        <option value="stemAttrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_stemAttrUpdate'] }</option>
                        <option value="attrAdmin">${textContainer.text['thisGroupsPrivilegesPriv_attrAdmin'] }</option>
                        <option value="attrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_attrUpdate'] }</option>
                        <option value="attrRead">${textContainer.text['thisGroupsPrivilegesPriv_attrRead'] }</option>
                        <option value="attrView">${textContainer.text['thisGroupsPrivilegesPriv_attrView'] }</option>
                        <option value="attrOptin">${textContainer.text['thisGroupsPrivilegesPriv_attrOptin'] }</option>
                        <option value="attrOptout">${textContainer.text['thisGroupsPrivilegesPriv_attrOptout'] }</option>
                        <option value="attrDefAttrRead">${textContainer.text['thisGroupsPrivilegesPriv_attrDefAttrRead'] }</option>
                        <option value="attrDefAttrUpdate">${textContainer.text['thisGroupsPrivilegesPriv_attrDefAttrUpdate'] }</option --%>
                      </select>

                    </div>
                    <div class="span4">
                      <input type="text" placeholder="${textContainer.textEscapeXml['thisSubjectsPrivilegesGroupFilterFormPlaceholder']}" 
                         name="privilegeFilterText" id="table-filter" class="span12" aria-label="${textContainer.text['ariaLabelGuiEntityName']}"/>
                    </div>

                    <div class="span3"><input type="submit" class="btn" aria-controls="thisSubjectsGroupPrivilegesFilterResultsId" id="filterSubmitId" value="${textContainer.textEscapeDouble['groupApplyFilterButton'] }"
                        onclick="ajax('../app/UiV2Subject.filterThisSubjectsGroupPrivileges?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {formIds: 'groupFilterPrivilegesFormId,groupPagingPrivilegesFormId'}); return false;"> 
                      <a class="btn" role="button" onclick="$('#people-filter').val(''); $('#table-filter').val(''); $('#filterSubmitId').click(); return false;">${textContainer.text['groupResetButton'] }</a>
                    </div>
                    
                  </div>
                </form>
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="thisSubjectsGroupPrivilegesFilterResultsId" role="region" aria-live="polite">
                </div>                
              </div>
            </div>
            <!-- end group/thisSubjectsGroupPrivileges.jsp -->
