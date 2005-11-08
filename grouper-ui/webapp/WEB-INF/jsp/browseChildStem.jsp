<%-- @annotation@
		  Dynamic tile used by all browse modes to render
		  child stems as links
--%><%--
  @author Gary Brown.
  @version $Id: browseChildStem.jsp,v 1.2 2005-11-08 15:47:09 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<html:link page="/browseStems${browseMode}.do" 
		   paramId="currentNode" 
		   paramName="viewObject" 
		   paramProperty="stemId"
		   title="${navMap['browse.expand.stem']} ${viewObject.displayExtension}">
				<span class="stemView"><c:out value="${viewObject.displayExtension}"/></span>
</html:link>
