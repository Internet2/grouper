<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<span class="personSubject"><c:out value="${viewObject.description}" /></span>