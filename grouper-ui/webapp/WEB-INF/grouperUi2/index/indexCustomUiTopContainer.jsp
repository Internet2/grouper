<%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <div class="top-container" id="theTopContainer">
      <%@ include file="../assetsJsp/environmentHeader.jsp"%>
    
      <div class="container-fluid">
        <div id="messaging1" class="row-fluid">
        </div>
        <div class="row-fluid">
          
          
          <div class="span9 main-content" style="padding: 0">
            <div class="row-fluid">
              <div class="span11" style="margin-left: 1.5em; margin-right: 1em">
                <c:set var="theLogo" value="${grouperRequestContainer.customUiContainer.textTypeToText['logo']}"/>
                <img class="brand" src="${ !grouper:isBlank(theLogo) ? theLogo : ('../../'.concat(mediaMap['image.organisation-logo']))}" alt="Logo" style="margin-top: 1.5em; margin-bottom: 1.5em" />
                <div class="navbar-text pull-right" style="white-space: normal;">${textContainer.text['indexLoggedInAs'] } 
                  ${guiSettings.loggedInSubject.screenLabelShort2noLink} 
                  <c:if test="${mediaMap['logout.link.show']=='true'}">
                    &middot; 
                    <a href="?operation=Logout.logout"
                          onclick="return handleGuiV2LinkClick(event, 'operation=Logout.logout');" class="navbar-link">${textContainer.text['indexLogoutLink']}</a>
                  </c:if>
                  &middot;   
                  <c:set var="theHelpLink" value="${grouperRequestContainer.customUiContainer.textTypeToText['helpLink']}"/>
                  ${ !grouper:isBlank(theHelpLink) ? theHelpLink : textContainer.text['guiCustomUiHelpDefaultLink']}
                 </div>
              
              </div>
            </div>      
            <!-- main landmark (WCAG 1.3.1).  note the landmark goes here and NOT on the
                 enclosing span9 main-content div, since on this custom ui shell that div
                 also holds the logo and the logged-in-as/logout row, which are not
                 main content. -->
            <main id="grouperMainContentDivId" tabindex="-1"></main>
            <!-- end of the main content element where the page content goes -->
          </div>
          
        </div>
        <hr>
        <footer>
          <p>&copy; ${textContainer.text['institutionName'] }</p>
        </footer>
      </div>
    </div>
