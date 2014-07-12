<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <!-- start stem/stemViewAudits.jsp -->
            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <%@ include file="stemHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <ul class="nav nav-tabs">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
                  <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
                    <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
                  </c:if>
                </ul>

                <p class="lead">${textContainer.text['stemAuditLogDescription'] }</p>

                <form class="form-inline form-small form-filter" id="stemFilterAuditFormId">
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
                  onclick="ajax('../app/UiV2Stem.viewAuditsFilter?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemFilterAuditFormId,stemPagingAuditFormId,stemQuerySortAscendingFormId'}); return false;"
                  >${textContainer.text['groupAuditLogFilterFindEntriesButton']}</button>
                </form>


                <div id="stemAuditFilterResultsId">
                </div>                
              </div>
            </div>
            <!-- end stem/stemViewAudits.jsp -->