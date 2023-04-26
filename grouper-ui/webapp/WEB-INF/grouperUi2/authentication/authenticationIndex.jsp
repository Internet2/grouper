<%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <div class="bread-header-container">
      <ul class="breadcrumb">
        <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
        <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
        <li class="active">${textContainer.text['authenticationIndexBreadcrumb'] }</li>
      </ul>
      <div class="page-header blue-gradient">
        <h1>${textContainer.text['authenticationIndexTitle'] }</h1>
        <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['authenticationIndexSubtitle']}</p>
      </div>

    </div>
    <div class="row-fluid" style="margin-top: -30px"> <%-- since each row pushed down two br's, then start in negative --%>
      <div class="span12">
        <div class="row-fluid">
          <div class="span1">
            <br /><br /><a href="#" onclick="return guiV2link('operation=UiV2AuthenticationConfig.viewWsTrustedJwts');" style="white-space: nowrap;"
            >${textContainer.text['authenticationIndexWsAuth'] }</a>
          </div>
        </div>
        
      </div>
    </div>


