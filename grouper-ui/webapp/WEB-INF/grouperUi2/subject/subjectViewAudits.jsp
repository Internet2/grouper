<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('subjectViewAuditsPageTitle', grouperRequestContainer.subjectContainer.guiSubject.subject.name)}

            <%@ include file="subjectHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../subject/subjectTabs.jsp" %>

                <p class="lead">${textContainer.text['subjectAuditLogDescription'] }</p>

                <form class="form-inline form-small form-filter" id="subjectFilterAuditFormId">
                  <input type="hidden" name="auditType" value="${grouperRequestContainer.subjectContainer.auditType}" />
                  <label for="date-filter">${textContainer.text['subjectAuditLogFilterByDate'] }</label>&nbsp;
                  <select id="date-filter" class="span2" name="filterType">
                    <option value="all" selected="selected">${textContainer.text['subjectAuditLogFilterType_all']}</option>
                    <option value="on">${textContainer.text['subjectAuditLogFilterType_on']}</option>
                    <option value="before">${textContainer.text['subjectAuditLogFilterType_before']}</option>
                    <option value="between">${textContainer.text['subjectAuditLogFilterType_between']}</option>
                    <option value="since">${textContainer.text['subjectAuditLogFilterType_since']}</option>
                  </select>
                  <input id="from-date" aria-label="${textContainer.text['ariaLabelGuiFromDate']}" name="filterFromDate" type="text" placeholder="${textContainer.text['subjectAuditLogFilterDatePlaceholder'] }" 
                    class="span2">&nbsp;( ${textContainer.text['subjectAuditLogFilterAndLabel'] }&nbsp;
                  <input id="to-date" aria-label="${textContainer.text['ariaLabelGuiToDate']}" name="filterToDate" type="text" placeholder="${textContainer.text['subjectAuditLogFilterDatePlaceholder'] }" 
                    class="span2">&nbsp;)
                  <label class="checkbox">
                    <input type="checkbox" name="showExtendedResults" value="true">${textContainer.text['subjectAuditLogFilterShowExtendedResults']}
                  </label>&nbsp;&nbsp;
                  <button type="submit" class="btn" id="auditLogSubmitButtonId"
                  onclick="ajax('../app/UiV2Subject.viewAuditsFilter?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}', {formIds: 'subjectFilterAuditFormId,subjectPagingAuditFormId,subjectQuerySortAscendingFormId'}); return false;"
                  >${textContainer.text['subjectAuditLogFilterFindEntriesButton']}</button>
                  <input type="button" class="btn" value="${textContainer.textEscapeDouble['subjectAuditLogExportButton'] }"
                    onclick="return configurationFileExport(event, '../app/UiV2Subject.viewAuditsExport?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}', {optionalFormElementNamesToSend: 'auditType,filterType,filterFromDate,filterToDate,showExtendedResults'}); return false;" />
                </form>


                <div id="subjectAuditFilterResultsId">
                </div>                
              </div>
            </div>
            <!-- end group/groupViewAudits.jsp -->