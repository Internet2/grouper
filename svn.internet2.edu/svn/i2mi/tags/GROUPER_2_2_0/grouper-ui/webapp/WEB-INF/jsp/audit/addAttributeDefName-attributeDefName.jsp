<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: addAttributeDefName-attributeDefName.jsp,v 1.2 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<em><c:out value="${viewObject.fields.displayName}"/></em> <grouper:message key="audit.result.label.defined-by"/> 
<c:out value="${viewObject.fields.parentAttributeDefName}"/>