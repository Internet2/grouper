<%@ include file="../assetsJsp/commonTaglib.jsp"%>

     <c:if test="${grouperRequestContainer.indexContainer.showAnnounceHeader}">
        <div class="grouper-env-announce-outer">
          <div class="grouper-env-announce-inner">
            <span>${grouperRequestContainer.indexContainer.announceHeaderText}</span>
          </div>
        </div>
      </c:if>
      