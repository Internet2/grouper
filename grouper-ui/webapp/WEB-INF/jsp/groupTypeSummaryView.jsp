<%-- @annotation@
		  Dynamic tile used to render a GroupType - for group summary
--%><%--
  @author Gary Brown.
  @version $Id: groupTypeSummaryView.jsp,v 1.1 2006-02-21 16:50:18 isgwb Exp $
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
			<tiles:put name="view" value="groupSummaryFields"/>
			<tiles:put name="itemView" value="withValue"/>
			<tiles:put name="listless" value="TRUE"/>
		</tiles:insert>
	</div>
</div>
			
