<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<!DOCTYPE html>
<html>
  <!-- start index.jsp -->
  <head><title>${textContainer.text['guiTitle']}</title>
  <%@ include file="../assetsJsp/commonHead.jsp"%>
  </head>
  <body class="full claro">
    <noscript>
      <h3 style="color: #990000">${textContainer.text['indexNoJavascript'] }</h3>    
    </noscript>
    <div class="top-container">
      <div class="navbar navbar-static-top">
        <div class="navbar-inner">
          <div class="container-fluid"><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');"><img class="brand" src="../../${mediaMap['image.organisation-logo']}" alt="Logo" /></a>
            <div class="pull-right">
              <form id="searchForm" action="#" onsubmit="return guiV2link('operation=UiV2Main.searchSubmit', {optionalFormElementNamesToSend: 'searchQuery'});" class="navbar-search">
                <input type="text" name="searchQuery" placeholder="${textContainer.textEscapeXml['searchPlaceholder']}" class="search-query"><a href="#" 
                  onclick="return guiV2link('operation=UiV2Main.searchSubmit', {optionalFormElementNamesToSend: 'searchQuery'});" aria-label="Search"><i class="fa fa-search"></i></a>
              </form>
            </div>
            <div class="navbar-text pull-right">${textContainer.text['indexLoggedInAs'] } 
              ${guiSettings.loggedInSubject.shortLink} 
              <c:if test="${mediaMap['logout.link.show']=='true'}">
                &middot; 
                <a href="../../logout.do" class="navbar-link">${textContainer.text['indexLogoutLink']}</a> 
              </c:if>
              <%-- MCH 20140406: not sure what to do with help... 
              &middot; <a href="#" class="navbar-link">Help</a> --%>
             </div>
          </div>
        </div>
      </div>
      <div class="container-fluid">
        <div id="messaging" class="row-fluid">
          <%-- this is where messages go --%>
        </div>
        <div class="row-fluid">
          
          <div class="span3 left-column">
            <div class="btn-group btn-group-create"><a href="#" 
              onclick="return guiV2link('operation=UiV2Group.newGroup', {optionalFormElementNamesToSend: 'objectStemId'});"
              class="btn btn-bigger btn-create" role="button"><i class="fa fa-plus"></i> ${textContainer.text['groupNewCreateNewGroupMenuButton'] }</a>
              <a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-bigger btn-create dropdown-toggle" 
              	aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#main-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#main-more-options li').first().focus();return true;});">
              	<span class="caret"></span>
              </a>
              <ul class="dropdown-menu dropdown-menu-right" id="main-more-options">
                <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2AttributeDef.newAttributeDef', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['attributeDefNewCreateNewAttributeDefMenuButton'] }</a></li>
                <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2Stem.newStem', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['stemNewCreateNewStemMenuButton'] }</a></li>
                <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2Group.newGroup', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['groupNewCreateNewGroupMenuButton'] }</a></li>
                <li class="divider"></li>
                <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2GroupImport.groupImport', {optionalFormElementNamesToSend: 'groupId'});">${textContainer.text['groupImportAddMembersToGroupMenuLink'] }</a></li>
              </ul>
            </div>
            <div class="leftnav-accordions">
              <button type="button" data-toggle="collapse" data-target="#demo2" class="btn btn-block btn-grouper first">${textContainer.text['indexQuickLinksLabel']}<i class="fa fa-plus"></i><i class="fa fa-minus"></i></button>
              <div id="demo2" class="collapse in">
                <div class="accordion-inner">
                  <ul class="nav nav-list">
                    <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2MyGroups.myGroups');">${textContainer.text['indexMyGroupsButton'] }</a></li>
                    <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2MyStems.myStems');">${textContainer.text['indexMyStemsButton'] }</a></li>
                    <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2Main.myFavorites');">${textContainer.text['indexMyFavoritesButton'] }</a></li>
                    <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2Main.myServices');">${textContainer.text['indexMyServicesButton'] }</a></li>
                    <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2Main.myActivity');">${textContainer.text['indexMyActivityButton'] }</a></li>
                  <c:if test="${grouperRequestContainer.rulesContainer.canReadPrivilegeInheritance && grouperRequestContainer.indexContainer.showMiscellaneousLink}">
                    <li><a href="#" 
                      onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['indexMiscellaneousButton'] }</a></li>
                  </c:if>
                   
                  <c:if test="${grouperRequestContainer.indexContainer.adminUIQuickLinkDisplayed=='true'}">
                  	<li><a id="adminiu-link" href="../../populateAllGroups.do">${textContainer.text['ui-lite.fromInvite-admin-link'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.indexContainer.liteUIQuickLinkDisplayed=='true'}">
                  	<li><a id="liteiu-link" href="../../grouperUi/appHtml/grouper.html?operation=Misc.index">${textContainer.text['ui-lite.fromInvite-link'] }</a></li>
                  </c:if>
                  </ul>
                </div>
              </div>
              <div class="btn btn-block btn-grouper last" style="cursor: default;">${textContainer.text['indexBrowseFolders'] }
              <i class="fa fa-refresh" style="cursor: pointer;"
                onclick="dojoInitMenu()"
              ></i></div>
              <div class="accordion-inner">
                <script>
                  $(document).ready(function(){
                    dojoInitMenu();
                  });
                </script>
                <div id="folderTreeContainerId">
                  <div id="folderTree"></div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="span9 main-content offset3">
            <!-- this is the main content div where the page content goes via ajax -->
            <div id="grouperMainContentDivId">
            </div>
            <!-- end of the main content div where the page content goes -->
          </div>
          
        </div>
        <hr>
        <footer>
          <p>&copy; ${textContainer.text['institutionName'] }</p>
        </footer>
      </div>
    </div>
    <%@ include file="../assetsJsp/commonBottom.jsp"%>
  </body>
  <!-- end index.jsp -->
</html>
