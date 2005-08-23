<%-- @annotation@
		  Dynamic tile which is used by genericList mechanism
		  as an alternative header when listing group members
--%><%--
  @author Gary Brown.
  @version $Id: memberLinksHeaderView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="view" value="genericListHeader"/>
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="pager" beanName="pager"/>
	  <tiles:put name="noResultsMsg" beanName="noResultsMsg"/>
  </tiles:insert>
<div class="instructions"><fmt:message bundle="${nav}" key="groups.list-members.instructions"/></div>
