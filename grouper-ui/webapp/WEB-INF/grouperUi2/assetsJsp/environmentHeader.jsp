<%@ include file="../assetsJsp/commonTaglib.jsp"%>

     <c:if test="${grouperRequestContainer.indexContainer.showEnvironmentHeader}">
        <div class="grouper-env-banner-outer">
          <div class="grouper-env-banner-inner">
            <span>${grouperRequestContainer.indexContainer.environmentHeaderText}</span>
          </div>
        </div>
      </c:if>
      