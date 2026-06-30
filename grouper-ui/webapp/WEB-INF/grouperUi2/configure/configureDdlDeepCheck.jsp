<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('configurationDdlDeepCheckPageTitle')}
            <grouper:browserPage jspName="configureDdlDeepCheck" />
            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Configure.index" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Configure.index');">${textContainer.text['configurationIndexBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['configurationDdlDeepCheckBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['configurationDdlDeepCheckTitle'] }</h1>
                <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['configurationDdlDeepCheckSubtitle']}</p>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <div class="form-actions">
                  <a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Configure.ddlDeepCheckSubmit'); return false;">${textContainer.text['configurationDdlDeepCheckRunButton'] }</a>
                </div>
              </div>
            </div>
            <c:if test="${grouperRequestContainer.configurationContainer.ddlCompareResult != null}">
            <div class="row-fluid">
              <div class="span12">
                <h3>${textContainer.text['configurationDdlDeepCheckResultsLabel'] }</h3>
                <pre style="max-height: 600px; overflow: auto; white-space: pre-wrap; word-wrap: break-word;">${grouperRequestContainer.configurationContainer.ddlCompareResultHtml}</pre>
              </div>
            </div>
            </c:if>
