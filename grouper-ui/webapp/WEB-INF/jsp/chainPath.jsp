<%-- @annotation@ 
	Displays chain of effective memberships by which a subject is a member of a group	
 --%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<jsp:useBean id="membershipMap" class="java.util.HashMap"/>
<c:set target="${membershipMap}" property="contextGroup" value="${currentGroup.id}"/>
<c:set target="${membershipMap}" property="contextSubject" value="${GroupFormBean.map.contextSubject}"/>
<c:set target="${membershipMap}" property="contextSubjectId" value="${currentSubject.id}"/>
<c:set target="${membershipMap}" property="contextSubjectType" value="${currentSubject.subjectType}"/>
<c:set target="${membershipMap}" property="contextSourceId" value="${currentSubject.sourceId}"/>
<c:set target="${membershipMap}" property="subjectId" value="${currentSubject.id}"/>
<c:set target="${membershipMap}" property="subjectType" value="${currentSubject.subjectType}"/>
<c:set target="${membershipMap}" property="sourceId" value="${currentSubject.sourceId}"/>
<c:set target="${membershipMap}" property="asMemberOf" value="${viewObject[0].id}"/>
<c:set target="${membershipMap}" property="callerPageId" value="${thisPageId}"/>
<div class="chainPath">
<div class="chainSubject"><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="currentSubject"/>
	  <tiles:put name="view" value="chainSubject"/>
  </tiles:insert>
 </div>
 	<c:choose>
		<c:when test="${!empty viewObject[0].owner && viewObject[0].owner.hasComposite}">
			<tiles:insert definition="dynamicTileDef" flush="false">
				<tiles:put name="viewObject" beanName="currentSubject"/>
				<tiles:put name="view" value="isIndirectMemberOf"/>
				<tiles:put name="params" beanName="membershipMap"/>
				<tiles:put name="linkTitle" value="${navMap['groups.membership.through.title']} ${viewObject[0].owner.displayExtension}"/>
			</tiles:insert>
		</c:when>
		<c:otherwise>
			<tiles:insert definition="dynamicTileDef" flush="false">
				<tiles:put name="viewObject" beanName="currentSubject"/>
				<tiles:put name="view" value="isMemberOf"/>
				<tiles:put name="params" beanName="membershipMap"/>
				<tiles:put name="linkTitle" value="${navMap['groups.membership.through.title']} ${viewObject[0].desc}"/>
			</tiles:insert>
		</c:otherwise>
	</c:choose>
	
 
<ul>
<c:forEach items="${viewObject}" var="group" varStatus="status">
<c:set target="${membershipMap}" property="subjectId" value="${group.id}"/>
<c:set target="${membershipMap}" property="subjectType" value="group"/>
<c:set target="${membershipMap}" property="sourceId" value="g:gsa"/>
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
<c:set target="${group}" property="contextSourceId" value="${currentSubject.sourceId}"/>
<c:set target="${group}" property="callerPageId" value="${thisPageId}"/>

  
   	<tiles:insert definition="singleChainPath" flush="false">
		<tiles:put name="params" beanName="membershipMap"/>
		<tiles:put name="group" beanName="group"/>
		<tiles:put name="currentSubject" beanName="currentSubject"/>
		<tiles:put name="linkSeparator" beanName="linkSeparator"/>

	</tiles:insert>
</c:forEach>
<li><tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="currentGroup"/>
	  <tiles:put name="view" value="chainGroup"/>
  </tiles:insert></li>
</ul>
</div>