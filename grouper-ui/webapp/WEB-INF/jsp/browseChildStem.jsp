<%-- @annotation@
		  Dynamic tile used by all browse modes to render
		  child stems as links
--%><%--
  @author Gary Brown.
  @version $Id: browseChildStem.jsp,v 1.7 2008-04-10 19:50:25 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<%-- TODO change tooltip to real tooltip, and from nav.properties --%>
<img <grouper:tooltip key="stem.icon.tooltip"/>  alt="Folder"
src="grouper/images/folder.gif" class="groupIcon" 
/><html:link page="/browseStems${browseMode}.do" 
		   paramId="currentNode" 
		   paramName="viewObject" 
		   paramProperty="stemId"
		   title="${navMap['browse.expand.stem']} ${viewObject.displayExtension}">
				<span class="stemView"><c:out value="${viewObject[mediaMap['stem.display']]}"/></span>
</html:link>
