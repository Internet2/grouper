<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['miscellaneousBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['miscellaneousTitle'] }</h1>
                <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['miscellaneousSubtitle']}</p>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <div class="row-fluid">
                  <div class="span1">
                    <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance && grouperRequestContainer.indexContainer.showGlobalInheritedPrivilegesLink}">
                      <a href="#" onclick="return guiV2link('operation=UiV2Main.globalInheritedPrivileges');" style="white-space: nowrap;"
                      >${textContainer.text['miscellaneousGlobalInheritedPrivileges'] }</a>
                    </c:if>
                    <c:if test="${grouperRequestContainer.adminContainer.subjectApiDiagnosticsShow}">
                      <br /><br /><a href="#" onclick="return guiV2link('operation=UiV2Admin.subjectApiDiagnostics');" style="white-space: nowrap;"
                      >${textContainer.text['adminSubjectApiDiagnosticsLink'] }</a>
                    </c:if>
                    <c:if test="${grouperRequestContainer.adminContainer.instrumentationShow}">
                      <br /><br /><a href="#" onclick="return guiV2link('operation=UiV2Admin.instrumentation');" style="white-space: nowrap;"
                      >${textContainer.text['adminInstrumentationLink'] }</a>
                    </c:if>
                    <c:if test="${grouperRequestContainer.grouperLoaderContainer.canSeeLoaderOverall}">
                      <br /><br /><a href="#" onclick="return guiV2link('operation=UiV2GrouperLoader.loaderOverall');" style="white-space: nowrap;"
                      >${textContainer.text['adminLoaderLink'] }</a>
                    </c:if>
                  </div>
                </div>
              </div>
            </div>


