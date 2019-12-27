<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2SubjectResolution.subjectResolutionMain');">${textContainer.text['miscellaneousSubjectResolutionOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousSubjectResolutionSubjectUnresolvedBreadcrumb'] }</li>
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
                    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousSubjectResolutionUnresolvedSubjectSubtitle']}</p>
                    <form class="form-inline form-small form-filter" id="usduFormId">

                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="subjectResolutionFilterTypeId">${textContainer.text['subjectResolutionFilterTypeLabel']}</label></strong></td>
                            <td>
                              <select name="includeDeleted" id="subjectResolutionFilterTypeId" style="width: 25em"
                                onchange="ajax('../app/UiV2SubjectResolution.viewUnresolvedSubjects', {formIds: 'usduFormId'}); return false;">
                                <option value="doNotShowDeleted" ${grouperRequestContainer.subjectResolutionContainer.showDeletedLabel == 'doNotShowDeleted' ? 'selected="selected"' : '' } >${textContainer.textEscapeXml['subjectResolutionFilterTypeOptionDoNotShowDelete']}</option>
                                <option value="showDeleted" ${grouperRequestContainer.subjectResolutionContainer.showDeletedLabel == 'showDeleted' ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['subjectResolutionFilterTypeOptionShowDeleted']}</option>
                                <option value="showAll" ${grouperRequestContainer.subjectResolutionContainer.showDeletedLabel == 'showAll' ? 'selected="selected"'  : '' }>${textContainer.textEscapeXml['subjectResolutionFilterTypeOptionShowAll']}</option>
                              </select>
                              <br />
                              <span class="description">${textContainer.text['subjectResolutionFilterDescription']}</span>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </form>
                    
                  </div>
                </div>
                
                <%@ include file="unresolvedSubjectsHelper.jsp"%>
              </div>
              
            </div>
