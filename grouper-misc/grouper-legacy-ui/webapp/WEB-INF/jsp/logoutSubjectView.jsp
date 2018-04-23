<%-- @annotation@
		  Dynamic tile used to render a subject in the welcome message on subjeader
--%><%--
  @author Gary Brown.
  @version $Id: subjectView.jsp,v 1.10 2008-04-16 09:10:32 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<%@page import="org.apache.commons.lang.StringUtils"%>

<tiles:importAttribute ignore="true"/><c:set var="attrKey" value="*subject.display.${viewObject.source.id}"/><c:if test="${empty mediaMap[attrKey]}"><c:set var="attrKey" value="subject.display.default"/></c:if>
<c:if test="${viewObject.isGroup}"><img <grouper:tooltip key="group.icon.tooltip"/> 
    src="grouper/images/group.gif" class="groupIcon" alt="Group" 
    /></c:if><c:if test="${empty inLink}"><span class="<c:out value="${viewObject.subjectType}"/>Subject"></c:if>
	<c:set var="subjectString" value="${viewObject[mediaMap[attrKey]]}"/><%
  			
  			String subjectString = (String)pageContext.getAttribute("subjectString");
			String maxLengthStr = null;
  			int maxLength=80;
  			try {
  				maxLengthStr = GrouperUiFilter.retrieveMediaProperties().getProperty("welcome.subject.max-length");
  				maxLength=Integer.parseInt(maxLengthStr);
  			}catch(Exception e) {
  				//Just swallow
  			}
  			if(subjectString != null && subjectString.length() > maxLength) {
  				subjectString = StringUtils.abbreviate(subjectString,maxLength);
  				pageContext.setAttribute("subjectString",subjectString);
  			}
  			
  			%><c:out value="${subjectString}" escapeXml="false"/>
	
	<c:if test="${empty inLink}"></span></c:if>


  			