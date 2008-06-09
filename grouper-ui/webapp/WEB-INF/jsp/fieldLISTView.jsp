<%-- @annotation@
		  Dynamic tile used to render a field which is a list field
--%><%--
  @author Gary Brown.
  @version $Id: fieldLISTView.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="field">
	<div class="formRow">
		<div class="formLeft">
			<c:out value="${viewObject.name}"/>
		</div>
		<div class="formRight">
			<span class="fieldIsList"><grouper:message bundle="${nav}" key="groups.summary.field-is-list"/></span>
		</div>
	</div>
</div>
   



