<%-- @annotation@
		Tile which displays the generic subject search functionality. Designed to be embedded in actual forms
--%><%--
  @author Gary Brown.
  @version $Id: subjectSearchFragment.jsp,v 1.1 2005-11-08 15:32:57 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute/>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="subjectSearchFragment">
<div class="formRow"><hr/></div>
<div class="formRow">
<div class="formLeft"><fmt:message bundle="${nav}" key="find.search-source"/></div>
</div>
<div class="formRow"><hr/></div>
<c:forEach var="source" items="${subjectSources}" varStatus="sourceStatus">

<c:if test="${source.id == subjectSource}"><c:set var="checked"> checked="checked"</c:set></c:if>
	<div class="formRow">
	<div class="formLeft">
	<input type="radio" 
					       value="<c:out value="${source.id}"/>" <c:out value="${checked}"/>
						   name="subjectSource"
						   id="<c:out value="${source.id}Radio"/>"/>
	</div>
	<div class="formRight"><label for="<c:out value="${source.id}Radio"/>"><c:out value="${source.name}"/> (
	<c:forEach var="subjectType" items="${source.subjectTypes}" varStatus="typeStatus">
		<c:if test="${typeStatus.count>1}">, </c:if><c:out value="${subjectType}"/>
	</c:forEach>
	)</label>
		</div>
</div>
<c:if test="${!empty groupInsert && source.id=='grouperAdapter'}">
	<tiles:insert definition="${groupInsert}"/>
</c:if>
<c:remove var="checked"/>
<div class="formRow"><hr/></div>
</c:forEach>
</div>
</grouper:recordTile>