<%@ page import="edu.internet2.middleware.grouper.ui.GrouperUiFilter" %>
<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<c:set var="lang" value="${(empty GrouperUiFilter.retrieveLocale()) ? 'en' : GrouperUiFilter.retrieveLocale().getLanguage()}" />
<!DOCTYPE html>
<html lang="${lang}">
  <!-- start index/index.jsp -->
  <head><title>${textContainer.text['guiTitle']}</title>
  <%@ include file="../assetsJsp/commonHead.jsp"%>
  </head>
  <body class="full claro">
    <%@ include file="../assetsJsp/skipLink.jsp"%>
    <grouper:browserPage jspName="ajax" />
    <noscript>
      <h3 style="color: #990000">${textContainer.text['indexNoJavascript'] }</h3>    
    </noscript>
    <div class="top-container" id="theTopContainer">
      <%@ include file="../assetsJsp/environmentHeader.jsp"%>
      <%@ include file="../assetsJsp/announceHeader.jsp"%>
      <%@ include file="../assetsJsp/customHeader.jsp"%>
      <div class="navbar navbar-static-top">
        <div class="navbar-inner">
          <div class="container-fluid">
            <%@ include file="../assetsJsp/header.jsp"%>
            <div class="pull-right">

              <form id="searchForm" role="search" action="#" onsubmit="return guiV2link('operation=UiV2Main.searchSubmit', {optionalFormElementNamesToSend: 'searchQuery2'});" class="navbar-search">
                <input type="text" name="searchQuery2" id="mainPageSearchInput" placeholder="${textContainer.textEscapeXml['searchPlaceholder']}" aria-label="${textContainer.textEscapeXml['ariaLabelGuiSearch']}" class="search-query"><a href="?operation=UiV2Main.searchSubmit"
                  onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.searchSubmit', {optionalFormElementNamesToSend: 'searchQuery2'});" id="mainPageSearchButton"aria-label="${textContainer.text['ariaLabelGuiSearch']}"><i aria-hidden="true" class="fa fa-search"></i></a>
              </form>

            </div>
            <div class="navbar-text pull-right">${textContainer.text['indexLoggedInAs'] } 
              ${guiSettings.loggedInSubject.shortLink} 
              <c:if test="${mediaMap['logout.link.show']=='true'}">
                &middot; 
                <a href="?operation=Logout.logout"
                      onclick="return handleGuiV2LinkClick(event, 'operation=Logout.logout');" class="navbar-link">${textContainer.text['indexLogoutLink']}</a>
              </c:if>
              &middot; <a href="?operation=UiV2Main.help"
                      onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.help');">${textContainer.text['grouper.help'] }</a>
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
            <div class="btn-group btn-group-create"><a id="homepageCreateGroupButton" href="?operation=UiV2Group.newGroup"
              onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Group.newGroup', {optionalFormElementNamesToSend: 'objectStemId'});"
              class="btn btn-bigger btn-create" role="button"><i aria-hidden="true" class="fa fa-plus"></i> ${textContainer.text['groupNewCreateNewGroupMenuButton'] }</a>
              <%-- disclosure/menu trigger (GRP a11y item 3): native button already
                   provides the button role and Enter/Space/click, so the explicit
                   role="button" was redundant and is removed. aria-controls points at
                   the menu it opens so the relationship is exposed to assistive tech. --%>
              <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" class="btn btn-bigger btn-create dropdown-toggle"
              	aria-haspopup="true" aria-expanded="false" aria-controls="main-more-options" onclick="$('#main-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#main-more-options li').first().focus();return true;});">
              	<span class="caret"></span>
                <span class="visually-hidden">${textContainer.text['ariaLabelGuiMoreOptions']}</span>
            </button>
              <ul class="dropdown-menu dropdown-menu-right" id="main-more-options">
                <li><a href="?operation=UiV2Stem.newStem"
                  onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Stem.newStem', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['stemNewCreateNewStemMenuButton'] }</a></li>
                <li><a href="?operation=UiV2Group.newGroup"
                  onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Group.newGroup', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['groupNewCreateNewGroupMenuButton'] }</a></li>
                <li class="divider"></li>
                <li><a href="?operation=UiV2GroupImport.groupImport"
                  onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GroupImport.groupImport', {optionalFormElementNamesToSend: 'groupId'});">${textContainer.text['groupImportAddMembersToGroupMenuLink'] }</a></li>
              </ul>
            </div>
            <div class="leftnav-accordions">
              <%-- collapse disclosure trigger (GRP a11y item 3): removed the redundant
                   role="button" (native button already has it) and the malformed
                   "ariahaspopup" attribute - a collapse is a disclosure, not a menu, so it
                   has no popup. aria-controls names the region it shows/hides (#demo2). --%>
              <button type="button" data-toggle="collapse" data-target="#demo2" class="btn btn-block btn-grouper first" aria-expanded="true" aria-controls="demo2"
              	onclick="$('#demo2').hasClass('in') ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded','true');">
              	${textContainer.text['indexQuickLinksLabel']}
              	<i aria-hidden="true" class="fa fa-plus"></i><i aria-hidden="true" class="fa fa-minus"></i>
              </button>
              <div id="demo2" class="collapse in">
                <div class="accordion-inner">
                  <ul class="nav nav-list" id="quicklinks-nav">
                    <%@ include file="../assetsJsp/quicklinks-nav.jsp" %>
                  </ul>
                </div>
              </div>
              <button class="btn btn-block btn-grouper first" onclick="dojoInitMenu(true)" aria-label="${textContainer.text['indexBrowseFolders'] } ${textContainer.text['ariaLabelGuiRefreshFolderBrowse'] }">
                ${textContainer.text['indexBrowseFolders'] }
                <i aria-hidden="true" class="fa fa-exchange"></i>
                <span class="visually-hidden">${textContainer.text['ariaLabelGuiRefreshFolderBrowse'] }</span>
              </button>
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
            <!-- this is the main content element where the page content goes via ajax.
                 it is a main landmark (WCAG 1.3.1) so screen reader users can skip
                 the repeated header, search and left nav and go straight to page content.
                 note: ajax only ever replaces the INNER html of this element (all the
                 server side calls go through GuiScreenAction.newInnerHtmlFromJsp, which
                 the js applies with jquery .html()), so the landmark itself survives
                 every navigation and there is never more than one per page. -->
            <main id="grouperMainContentDivId" tabindex="-1">
            </main>
            <!-- end of the main content element where the page content goes -->
          </div>
          
        </div>
        <hr>
        <footer>
          <%@ include file="../assetsJsp/footer.jsp"%>
        </footer>
      </div>
    </div>
    <%@ include file="../assetsJsp/commonBottom.jsp"%>
  </body>
  <!-- end index.jsp -->
</html>
