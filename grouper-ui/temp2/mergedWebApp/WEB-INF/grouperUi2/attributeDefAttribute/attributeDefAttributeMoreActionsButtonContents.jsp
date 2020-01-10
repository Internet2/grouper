<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="btn-group btn-block">
  <c:if test="${grouperRequestContainer.attributeDefContainer.canUpdate}">
    <a id="show-add-block" href="javascript:void(0);" onclick="showHideAttributeDefAssignAttributeBlock()" 
    class="btn btn-medium btn-primary btn-block" role="button">
      <i class="fa fa-plus"></i> ${textContainer.text['attributeDefAssignAttributeButton'] }
  </a>
  </c:if>
</div>