<%-- @annotation@ 
			Displays exported membership data. Only used if export format does not specify a content-type 
--%><%--
  @author Gary Brown.
  @version $Id: ExportMembers.jsp,v 1.3 2008-04-03 07:48:21 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<tiles:insert definition="showStemsLocationDef"/>

<grouper:subtitle key="groups.heading.export-members" />

<form>
<textarea rows="30" cols="100" wrap="off"><c:out value="${data}"/></textarea>
</form>
<div class="linkButton">
<tiles:insert definition="callerPageButtonDef"/>
</div>

