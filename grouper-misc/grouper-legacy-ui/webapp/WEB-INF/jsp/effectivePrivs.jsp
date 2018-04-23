<%-- @annotation@
		  Shows effective privileges for subject over group or stem
--%><%--
  @author Gary Brown.
  @version $Id: effectivePrivs.jsp,v 1.7 2009-09-09 15:10:03 mchyzer Exp $
--%>	
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<jsp:useBean id="membershipMap" class="java.util.HashMap"/>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:set target="${membershipMap}" property="subjectId" value="${subject.id}"/>
<c:set target="${membershipMap}" property="subjectType" value="${subject.subjectType}"/>
<c:set target="${membershipMap}" property="sourceId" value="${subject.sourceId}"/>
<c:set target="${membershipMap}" property="callerPageId" value="${thisPageId}"/>

<c:set var="linkSeparator">  
	<tiles:insert definition="linkSeparatorDef" flush="false">
	</tiles:insert>
</c:set>
 <%--  Use params to make link title descriptive for accessibility --%>		
<c:set var="linkTitle"><grouper:message key="browse.to.subject.summary" tooltipDisable="true">
		 		<grouper:param value="${viewObject.description}"/>
		</grouper:message></c:set>

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