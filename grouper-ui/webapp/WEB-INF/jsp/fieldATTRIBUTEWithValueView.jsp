<%-- @annotation@
		  Dynamic tile used to display a custom field name and value
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTEWithValueView.jsp,v 1.1 2006-02-21 16:50:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="formRow">
	<div class="formLeft">
		<c:out value="${viewObject.name}"/>
	</div>
	<div class="formRight">
		<c:out value="${group[viewObject.name]}"/>
	</div>
</div>
   



