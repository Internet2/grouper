<%-- @annotation@ 
		  Main page for the 'All' browse mode 
--%><%--
  @author Gary Brown.
  @version $Id: AllGroups.jsp,v 1.5 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<%-- h1 onmouseover="grouperTooltip('whatever, dude');">hey there</h1>  --%>

<%-- a href="#" onmouseover="grouperTooltip('Here is a tooltip')">This is the <span class="tooltip"
onmouseover="grouperTooltip('Term tooltip')">term</span> in there</a --%>



	<%-- grouper:infodot hideShowHtmlId="firstHideShow" / --%>

	<%-- id="firstHideShow0" style="display:none;" --%>
<%--div class="helpText" <grouper:hideShowTarget hideShowHtmlId="firstHideShow"  /> >
<grouper:message key="groups.infodot.example" useNewTermContext="true" />
</div>
<br />
<a href="#" onclick="toggleTooltips('<grouper:message key="groups.tooltips.disable" tooltipDisable="true"/>', 
'<grouper:message key="groups.tooltips.enable" tooltipDisable="true"/>'); return false;" id="tooltipToggleLink"></a>
<script type="text/javascript">
  writeTooltipText('<grouper:message key="groups.tooltips.disable" tooltipDisable="true"/>', 
  '<grouper:message key="groups.tooltips.enable" tooltipDisable="true"/>');
</script><br / --%>
<%-- onmouseover="grouperTooltip('whatever, dude');">hey there</h1> --%>

<%-- a href="#" onclick="return toggleInfodots(event, '<grouper:message key="infodot.disableText" tooltipDisable="true"/>', 
'<grouper:message key="infodot.enableText" tooltipDisable="true"/>');" id="infodotToggleLink"></a>
<script type="text/javascript">
  writeInfodotText('<grouper:message key="infodot.disableText" tooltipDisable="true"/>', 
  '<grouper:message key="infodot.enableText" tooltipDisable="true"/>');
</script><br / --%>

<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<grouper:message key="groups.all.can"/>
</div>

<div class="section">
<grouper:subtitle key="groups.heading.browse" />

<tiles:insert definition="browseStemsDef"/>
</div>

<tiles:insert definition="simpleSearchGroupsDef"/>

<tiles:insert definition="stemLinksDef"/>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchGroupsDef"/>
</c:otherwise>
</c:choose>  


