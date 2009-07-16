<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: addAttributeDefName-attributeDefName.jsp,v 1.1 2009-07-16 11:33:34 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<em><c:out value="${viewObject.fields.displayName}"/></em> <grouper:message bundle="${nav}" key="audit.result.label.defined-by"/> 
<c:out value="${viewObject.fields.parentAttributeDefName}"/>