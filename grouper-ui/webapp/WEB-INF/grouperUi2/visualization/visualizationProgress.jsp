<%@ include file="../assetsJsp/commonTaglib.jsp"%>
 
<c:choose>
  <c:when test="${grouperRequestContainer.visualizationContainer.progressBean == null || grouperRequestContainer.visualizationContainer.progressBean.complete}">
    ${textContainer.text['visualization.title']}
    <a href="#" aria-label="Set visualization options" id="visualization-settings-button" class="btn btn-medium" aria-expanded="false" onclick="$('#visualization-settings').toggle()">
      <span class="fa fa-cog"></span><span class="caret"></span>
    </a>
  </c:when>
  <c:otherwise>
    <i class="fa fa-spinner fa-spin"></i> ${textContainer.text['visualizationProgressSubheading']}
  </c:otherwise>
</c:choose>