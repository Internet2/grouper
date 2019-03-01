<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['miscellaneousSubjectResolutionOverallBreadcrumb'] }</li>
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
                  </div>
                </div>
                <%@ include file="unresolvedSubjectsHelper.jsp"%>
              </div>
              
            </div>
