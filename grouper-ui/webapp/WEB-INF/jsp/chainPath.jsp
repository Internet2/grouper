<%-- @annotation@ 
	Displays chain of effective memberships by which a subject is a member of a group	
 --%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<jsp:useBean id="membershipMap" class="java.util.HashMap"/>
<c:set target="${membershipMap}" property="contextGroup" value="${currentGroup.id}"/>
<c:set target="${membershipMap}" property="contextSubject" value="${GroupFormBean.map.contextSubject}"/>
<c:set target="${membershipMap}" property="contextSubjectId" value="${currentSubject.id}"/>
<c:set target="${membershipMap}" property="contextSubjectType" value="${currentSubject.subjectType}"/>
<c:set target="${membershipMap}" property="subjectId" value="${currentSubject.id}"/>
<c:set target="${membershipMap}" property="subjectType" value="${currentSubject.subjectType}"/>
<c:set target="${membershipMap}" property="asMemberOf" value="${viewObject[0].id}"/>
<c:set target="${membershipMap}" property="callerPageId" value="${thisPageId}"/>
<div class="chainPath">
<div class="chainSubject"><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="currentSubject"/>
	  <tiles:put name="view" value="chainSubject"/>
  </tiles:insert>
 </div>
 <span class="chainLinkText"><html:link page="/populateGroupMember.do" name="membershipMap" title="${navMap['groups.membership.through.title']} ${viewObject[0].desc}"><fmt:message bundle="${nav}" key="groups.membership.chain.member-of"/></html:link></span>
<ul>
<c:forEach items="${viewObject}" var="group" varStatus="status">
<c:set target="${membershipMap}" property="subjectId" value="${group.id}"/>
<c:set target="${membershipMap}" property="subjectType" value="group"/>
<c:choose>
	<c:when test="${chainSize > 0 && status.count lt (chainSize+1)}">
		<c:set target="${membershipMap}" property="asMemberOf" value="${viewObject[status.count].id}"/>
	</c:when>
	<c:otherwise>
		<c:set target="${membershipMap}" property="asMemberOf" value="${currentGroup.id}"/>
	</c:otherwise>
</c:choose>

<c:set target="${group}" property="contextGroup" value="${currentGroup.id}"/>
<c:set target="${group}" property="contextSubjectId" value="${currentSubject.id}"/>
<c:set target="${group}" property="contextSubjectType" value="${currentSubject.subjectType}"/>
<c:set target="${group}" property="callerPageId" value="${thisPageId}"/>
<li><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="group"/>
	  <tiles:put name="view" value="chainPath"/>
  </tiles:insert> <span class="chainLinkText"><html:link page="/populateGroupMember.do" name="membershipMap" title="${navMap['groups.membership.through.title']} ${group.desc}"><fmt:message bundle="${nav}" key="groups.membership.chain.member-of"/></html:link></span></li>
</c:forEach>
<li><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="currentGroup"/>
	  <tiles:put name="view" value="chainGroup"/>
  </tiles:insert></li>
</ul>
</div>