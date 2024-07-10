<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<%@ page import="edu.internet2.middleware.grouper.ui.GrouperUiFilter" %>
<c:set var="lang" value="${(empty GrouperUiFilter.retrieveLocale()) ? 'en' : GrouperUiFilter.retrieveLocale().getLanguage()}" />
<grouper:browserPage jspName="indexCustomUi" />
<!DOCTYPE html>
<html lang="${lang}">
  <!-- start grouperUi2/index/indexCustomUi.jsp -->
  <head><title>${textContainer.text['guiTitle']}</title>
  <%@ include file="../assetsJsp/commonHead.jsp"%>
  </head>
  <body class="full claro">
    <grouper:browserPage jspName="ajax" />
    <noscript>
      <h3 style="color: #990000">${textContainer.text['indexNoJavascript'] }</h3>    
    </noscript>
    
    <div class="top-container" id="theTopContainer">
    </div>
    
    <%@ include file="../assetsJsp/commonBottom.jsp"%>
  </body>
  <!-- end index.jsp -->
</html>
