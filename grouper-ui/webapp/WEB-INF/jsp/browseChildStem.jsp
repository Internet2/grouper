<%-- @annotation@
		  Dynamic tile used by all browse modes to render
		  child stems as links
--%><%--
  @author Gary Brown.
  @version $Id: browseChildStem.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<html:link page="/browseStems${browseMode}.do" 
		   paramId="currentNode" 
		   paramName="viewObject" 
		   paramProperty="stemId"
		   title="${navMap['browse.expand.stem']} ${viewObject.displayExtension}">
				<span class="stemView"><c:out value="${viewObject.displayExtension}"/></span>
</html:link>
