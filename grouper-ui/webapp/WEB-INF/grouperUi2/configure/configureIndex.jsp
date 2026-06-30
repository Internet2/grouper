<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:title('configurationFilesPageTitle')}
            <grouper:browserPage jspName="configureIndex" />
            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['configurationIndexBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['configurationIndexTitle'] }</h1>
                <span id="configureHeaderVersionGrouper" style="display:none"
                >${grouperRequestContainer.browserContainer.grouperApiVersion}</span>
                <span id="configureHeaderVersionContainer" style="display:none"
                >${grouperRequestContainer.browserContainer.grouperContainerVersion}</span>
                <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['configurationIndexSubtitle']}</p>
              </div>

            </div>
            <div class="row-fluid" style="margin-top: -30px"> <%-- since each row pushed down two br's, then start in negative --%>
              <div class="span12">
                <div class="row-fluid">
                  <div class="span1">
                    <br /><br /><a href="?operation=UiV2Configure.configure" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Configure.configure');" style="white-space: nowrap;"
                    >${textContainer.text['configurationIndexConfigFiles'] }</a>

                    <br /><br /><a href="?operation=UiV2Configure.ddlDeepCheck" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Configure.ddlDeepCheck');" style="white-space: nowrap;"
                    >${textContainer.text['configurationIndexDdlDeepCheck'] }</a>

                    <br /><br /><a href="?operation=UiV2Configure.upgradeTasks" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Configure.upgradeTasks');" style="white-space: nowrap;"
                    >${textContainer.text['configurationIndexUpgradeTasks'] }</a>
                  </div>
                </div>
              </div>
            </div>


