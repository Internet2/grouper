<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2SubjectResolution.subjectResolutionMain');">${textContainer.text['miscellaneousSubjectResolutionOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousSubjectResolutionSubjectSearchBreadcrumb'] }</li>
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
                    <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousSubjectResolutionSearchSubjectSubtitle']}</p>
                    
                    <form class="form-inline form-small form-filter" id="searchPageForm"
                    onsubmit="ajax('../app/UiV2SubjectResolution.searchSubjectsSubmit', {formIds: 'searchPageForm'}); return false;">
                  <div class="row-fluid">              
                    <div class="span2">
                      <label for="searchFormSearch">${textContainer.text['find.search-for'] }</label>
                    </div>
                    <div class="span3" style="white-space: nowrap;">
                      
                      <%-- <grouper:combobox2 idBase="groupAddMemberCombo" style="width: 30em"
                          filterOperation="../app/UiV2SubjectResolution.addMemberFilter"/>
                        ${textContainer.text['miscellaneousSubjectResolutionSearchSubjectEntityLabel']} --%>
                        
                       <input type="text" name="subjectId" placeholder="${textContainer.text['subjectResolutionSubjectsSearchQueryTextFieldPlaceholder']}"
                        aria-label="${textContainer.text['ariaLabelSubjectResolutionSubjectsSearchQueryTextFieldPlaceholder']}">
                      
                    </div>
                  </div>
                  <div class="form-actions">
                    <div class="span2">&nbsp;</div>
                    <a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2SubjectResolution.searchSubjectsSubmit', {formIds: 'searchPageForm'}); return false;">${textContainer.text['searchButton'] }</a>
                  </div>
                </form>
                
                <!-- fill the div with subjectSearchContents.jsp  -->
                <div id="searchResultId">
                </div>
                    
                  </div>
                </div>
                
               
                
              </div>
            </div>
                
             