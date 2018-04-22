<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
				        <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Subject.viewSubject&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectMembershipsTab'] }</a></li>
                  <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;" >${textContainer.text['subjectPrivilegesTab'] }</a></li>
                  <li><a href="#" role="tab" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsStemPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectStemPrivilegesTab'] }</a></li>
                  <li><a href="#" role="tab" onclick="return guiV2link('operation=UiV2Subject.thisSubjectsAttributeDefPrivileges&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}', {dontScrollTop: true});" >${textContainer.text['subjectAttributePrivilegesTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance}">
                    <%@ include file="subjectMoreTab.jsp" %>
                  </c:if>
                </ul>

                <p class="lead">${textContainer.text['subjectAuditLogDescription'] }</p>

                <form class="form-inline form-small form-filter" id="subjectFilterAuditFormId">
                  <label for="date-filter">${textContainer.text['subjectAuditLogFilterByDate'] }</label>&nbsp;
                  <select id="date-filter" class="span2" name="filterType">
                    <option value="all" selected="selected">${textContainer.text['subjectAuditLogFilterType_all']}</option>
                    <option value="on">${textContainer.text['subjectAuditLogFilterType_on']}</option>
                    <option value="before">${textContainer.text['subjectAuditLogFilterType_before']}</option>
                    <option value="between">${textContainer.text['subjectAuditLogFilterType_between']}</option>
                    <option value="since">${textContainer.text['subjectAuditLogFilterType_since']}</option>
                  </select>
                  <input id="from-date" name="filterFromDate" type="text" placeholder="${textContainer.text['subjectAuditLogFilterDatePlaceholder'] }" 
                    class="span2">&nbsp;( ${textContainer.text['subjectAuditLogFilterAndLabel'] }&nbsp;
                  <input id="to-date" name="filterToDate" type="text" placeholder="${textContainer.text['subjectAuditLogFilterDatePlaceholder'] }" 
                    class="span2">&nbsp;)
                  <label class="checkbox">
                    <input type="checkbox" name="showExtendedResults" value="true">${textContainer.text['subjectAuditLogFilterShowExtendedResults']}
                  </label>&nbsp;&nbsp;
                  <button type="submit" class="btn" id="auditLogSubmitButtonId"
                  onclick="ajax('../app/UiV2Subject.viewAuditsFilter?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}', {formIds: 'subjectFilterAuditFormId,subjectPagingAuditFormId,subjectQuerySortAscendingFormId'}); return false;"
                  >${textContainer.text['subjectAuditLogFilterFindEntriesButton']}</button>
                </form>


                <div id="subjectAuditFilterResultsId">
                </div>                
              </div>
            </div>
            <!-- end group/groupViewAudits.jsp -->