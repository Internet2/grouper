<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <!-- start group/groupViewAudits.jsp -->
            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
				<div class="tab-interface">
                  <ul class="nav nav-tabs">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                    <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                      <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                    </c:if>
                    <%@ include file="groupMoreTab.jsp" %>
                  </ul>
				</div>

                <p class="lead">${textContainer.text['groupAuditLogDescription'] }</p>

                <form class="form-inline form-small form-filter" id="groupFilterAuditFormId">
                  <input type="hidden" name="auditType" value="${grouperRequestContainer.groupContainer.auditType}" />
                  <label for="date-filter">${textContainer.text['groupAuditLogFilterByDate'] }</label>&nbsp;
                  <select id="date-filter" class="span2" name="filterType">
                    <option value="all" selected="selected">${textContainer.text['groupAuditLogFilterType_all']}</option>
                    <option value="on">${textContainer.text['groupAuditLogFilterType_on']}</option>
                    <option value="before">${textContainer.text['groupAuditLogFilterType_before']}</option>
                    <option value="between">${textContainer.text['groupAuditLogFilterType_between']}</option>
                    <option value="since">${textContainer.text['groupAuditLogFilterType_since']}</option>
                  </select>
                  <input id="from-date" name="filterFromDate" type="text" placeholder="${textContainer.text['groupAuditLogFilterDatePlaceholder'] }" 
                    class="span2">&nbsp;( ${textContainer.text['groupAuditLogFilterAndLabel'] }&nbsp;
                  <input id="to-date" name="filterToDate" type="text" placeholder="${textContainer.text['groupAuditLogFilterDatePlaceholder'] }" 
                    class="span2">&nbsp;)
                  <label class="checkbox">
                    <input type="checkbox" name="showExtendedResults" value="true">${textContainer.text['groupAuditLogFilterShowExtendedResults']}
                  </label>&nbsp;&nbsp;
                  <button type="submit" class="btn" id="auditLogSubmitButtonId"
                  onclick="ajax('../app/UiV2Group.viewAuditsFilter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterAuditFormId,groupPagingAuditFormId,groupQuerySortAscendingFormId'}); return false;"
                  >${textContainer.text['groupAuditLogFilterFindEntriesButton']}</button>
                </form>


                <div id="groupAuditFilterResultsId">
                </div>                
              </div>
            </div>
            <!-- end group/groupViewAudits.jsp -->