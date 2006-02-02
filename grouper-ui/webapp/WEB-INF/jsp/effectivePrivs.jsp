<%-- @annotation@
		  Shows effective privileges for subject over group or stem
--%><%--
  @author Gary Brown.
  @version $Id: effectivePrivs.jsp,v 1.3 2006-02-02 16:38:08 isgwb Exp $
--%>	
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<jsp:useBean id="membershipMap" class="java.util.HashMap"/>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:set target="${membershipMap}" property="subjectId" value="${subject.id}"/>
<c:set target="${membershipMap}" property="subjectType" value="${subject.subjectType}"/>
<c:set target="${membershipMap}" property="callerPageId" value="${thisPageId}"/>

<c:set var="linkSeparator">  
	<tiles:insert definition="linkSeparatorDef" flush="false">
	</tiles:insert>
</c:set>
 <%--  Use params to make link title descriptive for accessibility --%>		
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.subject.summary">
		 		<fmt:param value="${viewObject.description}"/>
		</fmt:message></c:set>

<div class="effectivePrivs">
<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="subject"/>
	  <tiles:put name="view" value="current"/>
	  <tiles:put name="params" beanName="membershipMap"/>
	  <tiles:put name="linkTitle" beanName="linkTitle"/>
</tiles:insert>

<c:forEach var="groupOrStemPrivEntry" items="${extendedSubjectPriv}">
<tiles:insert definition="effectivePrivDef" flush="false">
	<tiles:put name="groupOrStemPrivEntry" beanName="groupOrStemPrivEntry"/>
	<tiles:put name="params" beanName="membershipMap"/>
	<tiles:put name="linkSeparator" beanName="linkSeparator"/>
</tiles:insert>
</c:forEach>

</div>
</grouper:recordTile>