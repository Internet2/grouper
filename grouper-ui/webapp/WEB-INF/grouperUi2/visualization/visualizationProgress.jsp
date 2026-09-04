<%@ include file="../assetsJsp/commonTaglib.jsp"%>
 
<c:choose>
  <c:when test="${grouperRequestContainer.visualizationContainer.progressBean == null || grouperRequestContainer.visualizationContainer.progressBean.complete}">
    ${textContainer.text['visualization.title']}
    <button type="button" aria-label="Set visualization options" id="visualization-settings-button" class="btn btn-medium" aria-expanded="false" onclick="$('#visualization-settings').toggle()">
      <span aria-hidden="true" class="fa fa-cog"></span><span class="caret"></span>
    </button>
  </c:when>
  <c:otherwise>
    <i aria-hidden="true" class="fa fa-spinner fa-spin"></i> ${textContainer.text['visualizationProgressSubheading']}
  </c:otherwise>
</c:choose>