<%-- @annotation@
		 Standard tile which displays a page title and possibly subtitle
		 below the subheader
--%><%--
  @author Gary Brown.
  @version $Id: title.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:if test="${!empty title || !empty subtitle}">
	<div class="tableheader">
		<c:if test="${!empty title}">
    		<h1 id="title">
        		<fmt:message bundle="${nav}" key="${title}"/><span id="subtitle">
    	<c:if test="${!empty subtitle}">
    
        		&nbsp;-&nbsp;<fmt:message bundle="${nav}" key="${subtitle}">
					<c:forEach var="arg" items="${subtitleArgs}">
						<fmt:param value="${arg}"/>
					</c:forEach>
				</fmt:message>
    		
		</c:if>
		</span>
    		</h1>
    	</c:if>
				
	</div>
</c:if>
</grouper:recordTile>

