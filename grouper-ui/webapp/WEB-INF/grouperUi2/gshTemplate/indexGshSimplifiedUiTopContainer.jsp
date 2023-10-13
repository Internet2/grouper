<%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <div class="top-container" id="theTopContainer">
      <div class="container-fluid">
        <div class="row-fluid">
          
          
          <div class="span9 main-content" style="padding: 0">
            <div class="row-fluid">
              <div class="span11" style="margin-left: 1.5em; margin-right: 1em">
                <img class="brand" src="../../${mediaMap['image.organisation-logo']}" alt="Logo" style="margin-top: 1.5em; margin-bottom: 1.5em" />
                <div class="navbar-text pull-right" style="white-space: normal;">${textContainer.text['indexLoggedInAs'] } 
                  ${guiSettings.loggedInSubject.screenLabelShort2noLink} 
                    &middot; 
                    <a href="#"
                          onclick="return guiV2link('operation=Logout.logout');" class="navbar-link">${textContainer.text['indexLogoutLink']}</a>
                 </div>
              
              </div>
            </div>      
            <div id="messaging" class="row-fluid">
            </div>
            <div id="grouperMainContentDivId"></div>
            <!-- end of the main content div where the page content goes -->
          </div>
          
        </div>
        <hr>
        <footer>
          <p>&copy; ${textContainer.text['institutionName'] }</p>
        </footer>
      </div>
    </div>
