<%-- @annotation@
		  Dynamic tile used to display a List field with value
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: fieldLISTWithValueView.jsp,v 1.1 2006-02-21 16:50:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="fieldWithValue">
	<div class="formRow">
		<div class="formLeft">
			<c:out value="${viewObject.name}"/>
		</div>
		<div class="formRight">
			<span class="fieldIsList"><fmt:message bundle="${nav}" key="groups.summary.field-is-list"/></span>
		</div>
	</div>
</div>
   



