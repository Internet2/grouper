<%-- @annotation@
		 Standard tile which displays a page title and possibly subtitle
		 below the subheader
--%><%--
  @author Gary Brown.
  @version $Id: title.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:if test="${!empty title || !empty subtitle}">
	<div>
		<c:if test="${!empty title}">
    		<h1 id="title">
        		<grouper:message bundle="${nav}" key="${title}" tooltipDisable="true" /><span id="subtitle">
    	<c:if test="${!empty subtitle}">
    
        		&nbsp;-&nbsp;<grouper:message bundle="${nav}" key="${subtitle}">
					<c:forEach var="arg" items="${subtitleArgs}">
						<grouper:param value="${arg}"/>
					</c:forEach>
				</grouper:message>
    		
		</c:if>
		</span>
    		</h1>
    	</c:if>
				
	</div>
</c:if>
</grouper:recordTile>

