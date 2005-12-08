<%-- @annotation@ 
			Displays subject attributes
--%><%--
  @author Gary Brown.
  @version $Id: subjectInfo.jsp,v 1.2 2005-12-08 15:33:28 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="SubjectInfo">
<div class="formRow">
	<div class="formLeft">
		<c:out value="ID"/>
	</div>
	<div class="formRight">
		<c:out value="${subject['id']}"/>
	</div>
</div>
<c:forEach var="attrName" items="${subjectAttributeNames}">

<div class="formRow">
	<div class="formLeft">
		<c:out value="${attrName}"/>
	</div>
	<div class="formRight">
		<c:out value="${subject[attrName]}"/>
	</div>
</div>
 
</c:forEach>
<div class="formRow">
	<div class="formLeft">
		<fmt:message bundle="${nav}" key="subject.summary.subject-type"/>
	</div>
	<div class="formRight">
		<c:out value="${subject.subjectType}"/>
	</div>
</div>
<div style="clear:left;"></div>
</div><!--/SubjectInfo-->
</grouper:recordTile>
