<%-- @annotation@
		  Dynamic tile used in the 'Find' browse mode, which 
		  renders the content displayed before the children for
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: browseStemsFindListHeaderView.jsp,v 1.3 2007-03-06 11:05:49 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
			<tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	  			<tiles:put name="view" value="findNewHeader"/>
				<tiles:put name="listInstruction" beanName="listInstruction"/>
  			</tiles:insert>

<div class="browseForFindMembersForm">
<form action="populateAssignNewMembers.do" method="post">
<input type="hidden" name="callerPageId" value="<c:out value="${grouperForm.map.callerPageId}"/>"/>
<fieldset>
		<div style="display:none"><input name="alreadyChecked" type="hidden" value="true"/></div>
   



