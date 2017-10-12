<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['attestationAuditLogDescription'] }</div>
                  <div class="span3" id="groupAttestationMoreActionsButtonContentsDivId">
                    <%@ include file="groupAttestationMoreActionsButtonContents.jsp"%>
                  </div>
                </div>

                <form class="form-inline form-small form-filter" id="groupFilterAuditFormId">
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
                  onclick="ajax('../app/UiV2Attestation.viewGroupAuditsFilter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'groupFilterAuditFormId,groupPagingAuditFormId,groupQuerySortAscendingFormId'}); return false;"
                  >${textContainer.text['groupAuditLogFilterFindEntriesButton']}</button>
                </form>


                <div id="groupAuditFilterResultsId">
                </div>                
