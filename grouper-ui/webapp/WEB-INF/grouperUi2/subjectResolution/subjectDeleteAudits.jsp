<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2SubjectResolution.subjectResolutionMain');">${textContainer.text['miscellaneousSubjectResolutionOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousSubjectResolutionAuditsBreadcrumb'] }</li>
              </ul>
                            
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="lead span9"><h1>${textContainer.text['miscellaneousSubjectResolutionMainDecription'] }</h1></div>
                  <c:if test="${grouperRequestContainer.subjectResolutionContainer.allowedToSubjectResolution}">
                    <div class="span3" id="subjectResolutionMainMoreActionsButtonContentsDivId">
                      <%@ include file="subjectResolutionMainMoreActionsButtonContents.jsp" %>
                    </div>
                  </c:if>
                </div>
                <div class="row-fluid">
                  <div class="span12">
                    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousSubjectResolutionAuditEntriesSubtitle']}</p>
                  </div>
                </div>
                
                <form class="form-inline form-small form-filter" id="subjectDeleteAuditFormId">
                  <label for="date-filter">${textContainer.text['subjectResolutionSubjectDeleteLogFilterByDate'] }</label>&nbsp;
                  <select id="date-filter" class="span2" name="filterType">
                    <option value="all" selected="selected">${textContainer.text['subjectResolutionSubjectDeleteLogFilterType_all']}</option>
                    <option value="on">${textContainer.text['subjectResolutionSubjectDeleteLogFilterType_on']}</option>
                    <option value="before">${textContainer.text['subjectResolutionSubjectDeleteLogFilterType_before']}</option>
                    <option value="between">${textContainer.text['subjectResolutionSubjectDeleteLogFilterType_between']}</option>
                    <option value="since">${textContainer.text['subjectResolutionSubjectDeleteLogFilterType_since']}</option>
                  </select>
                  (&nbsp;
                  <input id="from-date" name="filterFromDate" type="text" placeholder="${textContainer.text['subjectResolutionSubjectDeleteLogFilterDatePlaceholder'] }" 
                    class="span2">&nbsp; ${textContainer.text['subjectResolutionSubjectDeleteLogFilterAndLabel'] }&nbsp;
                  <input id="to-date" name="filterToDate" type="text" placeholder="${textContainer.text['subjectResolutionSubjectDeleteLogFilterDatePlaceholder'] }" 
                    class="span2">&nbsp;)
                  <label class="checkbox">
                    <input type="checkbox" name="showExtendedResults" value="true">${textContainer.text['subjectResolutionSubjectDeleteLogFilterShowExtendedResults']}
                  </label>&nbsp;&nbsp;
                  <button type="submit" class="btn" id="auditLogSubmitButtonId"
                  onclick="ajax('../app/UiV2SubjectResolution.viewSubjectDeleteAudits', {formIds: 'subjectDeleteAuditFormId,unresolvedSubjectsPagingFormId'}); return false;"
                  >${textContainer.text['subjectResolutionSubjectDeleteLogFilterFindEntriesButton']}</button>
                </form>
                
                <div id="subjectResolutionSubjectDeleteAuditFilterResultsId">
                </div>
                
              </div>
              
            </div>
