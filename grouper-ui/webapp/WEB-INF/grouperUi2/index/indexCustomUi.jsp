<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<grouper:browserPage jspName="indexCustomUi" />
<!DOCTYPE html>
<html>
  <!-- start index.jsp -->
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
