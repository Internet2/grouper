<%-- @annotation@ 
			Displays exported membership data. Only used if export format does not specify a content-type 
--%><%--
  @author Gary Brown.
  @version $Id: ExportMembers.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef"/>


<h2 class="actionheader">
	<grouper:message bundle="${nav}" key="groups.heading.export-members"/>
</h2>

<form>
<textarea rows="30" cols="100" wrap="off"><c:out value="${data}"/></textarea>
</form>
<div class="linkButton">
<tiles:insert definition="callerPageButtonDef"/>
</div>

