<%-- @annotation@
		  Dynamic tile used to render a GroupType so fields can be edited
--%><%--
  @author Gary Brown.
  @version $Id: groupTypeEditAttributesView.jsp,v 1.1 2006-02-21 16:50:18 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<div class="formRow">
	<div class="formLeft">
		<c:out value="${viewObject.name}"/>
	</div>
	<div class="formRight">
		<tiles:insert definition="dynamicTileDef">
			<tiles:put name="viewObject" beanName="viewObject" beanProperty="fields"/>
			<tiles:put name="view" value="editAttributesFields"/>
			<tiles:put name="itemView" value="editValue"/>
			<tiles:put name="listless" value="TRUE"/>
		</tiles:insert>
	</div>
</div>
			
