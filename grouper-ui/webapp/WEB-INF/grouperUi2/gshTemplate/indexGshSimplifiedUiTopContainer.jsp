<%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <div class="top-container" id="theTopContainer">
      <%@ include file="../assetsJsp/environmentHeader.jsp"%>
    
      <div class="container-fluid">
        <div class="row-fluid">
          
          
          <div class="span9 main-content" style="padding: 0">
            <div class="row-fluid">
              <div class="span11" style="margin-left: 1.5em; margin-right: 1em">
                <img class="brand" src="../../${mediaMap['image.organisation-logo']}" alt="Logo" style="margin-top: 1.5em; margin-bottom: 1.5em" />
                <div class="navbar-text pull-right" style="white-space: normal;">${textContainer.text['indexLoggedInAs'] } 
                  ${guiSettings.loggedInSubject.screenLabelShort2noLink} 
                    &middot; 
                    <a href="?operation=Logout.logout"
                          onclick="return handleGuiV2LinkClick(event, 'operation=Logout.logout');" class="navbar-link">${textContainer.text['indexLogoutLink']}</a>
                 </div>
              
              </div>
            </div>      
            <div id="messaging" class="row-fluid">
            </div>
            <!-- main landmark (WCAG 1.3.1).  as with the custom ui shell, the landmark
                 goes here rather than on the enclosing span9 main-content div, which also
                 holds the logo and the logged-in-as/logout row. -->
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
