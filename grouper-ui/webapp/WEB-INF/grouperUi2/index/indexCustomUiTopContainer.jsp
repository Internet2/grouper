<%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <div class="top-container" id="theTopContainer">
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
                    <a href="#"
                          onclick="return guiV2link('operation=Logout.logout');" class="navbar-link">${textContainer.text['indexLogoutLink']}</a>
                  </c:if>
                  &middot;   
                  <c:set var="theHelpLink" value="${grouperRequestContainer.customUiContainer.textTypeToText['helpLink']}"/>
                  ${ !grouper:isBlank(theHelpLink) ? theHelpLink : textContainer.text['guiCustomUiHelpDefaultLink']}
                 </div>
              
              </div>
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
