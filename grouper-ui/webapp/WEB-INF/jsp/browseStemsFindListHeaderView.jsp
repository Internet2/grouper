<%-- @annotation@
		  Dynamic tile used in the 'Find' browse mode, which 
		  renders the content displayed before the children for
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: browseStemsFindListHeaderView.jsp,v 1.4 2009-10-30 15:06:34 isgwb Exp $
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
<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
<fieldset>
		<div style="display:none"><input name="alreadyChecked" type="hidden" value="true"/></div>
   



