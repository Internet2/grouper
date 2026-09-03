<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="btn-group btn-block">
  <c:if test="${grouperRequestContainer.attributeDefContainer.canUpdate}">
    <button type="button" id="show-add-block" onclick="showHideAttributeDefAssignAttributeBlock()" 
    class="btn btn-medium btn-primary btn-block">
      <i aria-hidden="true" class="fa fa-plus"></i> ${textContainer.text['attributeDefAssignAttributeButton'] }
  </button>
  </c:if>
</div>