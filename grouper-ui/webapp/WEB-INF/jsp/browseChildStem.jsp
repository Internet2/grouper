<%-- @annotation@
		  Dynamic tile used by all browse modes to render
		  child stems as links
--%><%--
  @author Gary Brown.
  @version $Id: browseChildStem.jsp,v 1.3 2007-03-14 10:04:48 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<html:link page="/browseStems${browseMode}.do" 
		   paramId="currentNode" 
		   paramName="viewObject" 
		   paramProperty="stemId"
		   title="${navMap['browse.expand.stem']} ${viewObject.displayExtension}">
				<span class="stemView"><c:out value="${viewObject[mediaMap['stem.display']]}"/></span>
</html:link>
