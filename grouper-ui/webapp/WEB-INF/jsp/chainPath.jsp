<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="chainPath">
<div class="chainSubject"><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="currentSubject"/>
	  <tiles:put name="view" value="chainSubject"/>
  </tiles:insert>
 </div>
 <span class="chainLinkText"><fmt:message bundle="${nav}" key="groups.membership.chain.member-of"/></span>
<ul>
<c:forEach items="${viewObject}" var="group">
<li><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="group"/>
	  <tiles:put name="view" value="chainPath"/>
  </tiles:insert> <span class="chainLinkText"><fmt:message bundle="${nav}" key="groups.membership.chain.member-of"/></span></li>
</c:forEach>
<li><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="currentGroup"/>
	  <tiles:put name="view" value="chainGroup"/>
  </tiles:insert></li>
</ul>
</div>