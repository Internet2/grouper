<%-- @annotation@
		  Dynamic tile used in the 'Find' browse mode, which 
		  renders the content displayed before the children for
		  the active node.
--%><%--
  @author Gary Brown.
  @version $Id: browseStemsFindListHeaderView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
			<tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	  			<tiles:put name="view" value="findNewHeader"/>
				<tiles:put name="listInstruction" beanName="listInstruction"/>
  			</tiles:insert>

<div class="browseForFindMembersForm">
<form action="populateAssignNewMembers.do">
<fieldset>
		<div style="display:none"><input name="alreadyChecked" type="hidden" value="true"/></div>
   



