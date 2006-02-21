<%-- @annotation@
		  Dynamic tile which allows users to edit custom group attributes
--%><%--
  @author Gary Brown.
  @version $Id: fieldATTRIBUTEEditValueView.jsp,v 1.1 2006-02-21 16:50:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="fieldWithValue">
	<div class="formRow">
		<div class="formLeft">
			<c:out value="${viewObject.name}"/>
		</div>
		<div class="formRight">
			<input type="text" name="attr.<c:out value="${viewObject.name}"/>" value="<c:out value="${group[viewObject.name]}"/>" size="50"/>
			
		</div>
	</div>
</div>
   



