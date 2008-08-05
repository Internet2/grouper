<%-- @annotation@
		 Standard tile used in baseDef which appears above the content space
		 and renders any Message object assigned to the request attribute key
		 'message'
--%><%--
  @author Gary Brown.
  @version $Id: message.jsp,v 1.4.4.1 2008-08-05 16:17:00 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<div class="grouperMessage">
<c:if test="${empty messages}">
<% 
List messages = new ArrayList();
Message message = (Message)request.getAttribute("message");
if(message!=null) messages.add(message);
request.setAttribute("messages",messages);
%>
</c:if>
<c:forEach var="message" items="${messages}">
<div class="<c:out value="${message.containerId}"/>"  >

<%-- print out prefix --%>
<grouper:message key="message.${message.containerId}" />

<!--message-->
<grouper:message bundle="${nav}" key="${message.text}">
<c:forEach var="arg" items="${message.args}">
   <grouper:param value="${arg}"/>
</c:forEach>
</grouper:message>
<!--/message-->
</div>
</c:forEach> 
</div>
<p>&nbsp;</p>
</grouper:recordTile>