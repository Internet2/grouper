<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                  <li class="active">${textContainer.text['grouper.help'] }</li>
              </ul>

              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="lead span9"><h1>${textContainer.text['grouper.help'] }</h1></div>
                </div>
              </div>
            </div>

            <div class="row-fluid">
              <%@ include file="../public/help.jsp"%>
            </div>
