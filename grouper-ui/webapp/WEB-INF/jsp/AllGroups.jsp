<%-- @annotation@ 
		  Main page for the 'All' browse mode 
--%><%--
  @author Gary Brown.
  @version $Id: AllGroups.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<%-- h1 onmouseover="grouperTooltip('whatever, dude');">hey there</h1>  --%>

<%-- a href="#" onmouseover="grouperTooltip('Here is a tooltip')">This is the <span class="tooltip"
onmouseover="grouperTooltip('Term tooltip')">term</span> in there</a --%>



	<%-- grouper:infodot hideShowHtmlId="firstHideShow" / --%>

	<%-- id="firstHideShow0" style="display:none;" --%>
<%--div class="helpText" <grouper:hideShowTarget hideShowHtmlId="firstHideShow"  /> >
<grouper:message bundle="${nav}" key="groups.infodot.example" useNewTermContext="true" />
</div>
<br />
<a href="#" onclick="toggleTooltips('<grouper:message bundle="${nav}" key="groups.tooltips.disable" tooltipDisable="true"/>', 
'<grouper:message bundle="${nav}" key="groups.tooltips.enable" tooltipDisable="true"/>'); return false;" id="tooltipToggleLink"></a>
<script type="text/javascript">
  writeTooltipText('<grouper:message bundle="${nav}" key="groups.tooltips.disable" tooltipDisable="true"/>', 
  '<grouper:message bundle="${nav}" key="groups.tooltips.enable" tooltipDisable="true"/>');
</script><br / --%>
<%-- onmouseover="grouperTooltip('whatever, dude');">hey there</h1> --%>

<%-- a href="#" onclick="return toggleInfodots(event, '<grouper:message bundle="${nav}" key="infodot.disableText" tooltipDisable="true"/>', 
'<grouper:message bundle="${nav}" key="infodot.enableText" tooltipDisable="true"/>');" id="infodotToggleLink"></a>
<script type="text/javascript">
  writeInfodotText('<grouper:message bundle="${nav}" key="infodot.disableText" tooltipDisable="true"/>', 
  '<grouper:message bundle="${nav}" key="infodot.enableText" tooltipDisable="true"/>');
</script><br / --%>

<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<grouper:message bundle="${nav}" key="groups.all.can"/>
</div>
<h2 class="actionheader">
	<grouper:message bundle="${nav}" key="groups.heading.browse"/>
</h2>

<tiles:insert definition="browseStemsDef"/>


<tiles:insert definition="simpleSearchGroupsDef"/>

<tiles:insert definition="stemLinksDef"/>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchGroupsDef"/>
</c:otherwise>
</c:choose>  


