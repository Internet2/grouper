<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: addGroupField-groupField.jsp,v 1.1 2009-07-16 11:33:34 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:out value="${viewObject.fields.type}"/> <em><c:out value="${viewObject.fields.name}"/></em>
<grouper:message bundle="${nav}" key="audit.result.label.to-object"/>

<em><c:out value="${viewObject.fields.groupTypeName}"/></em>
