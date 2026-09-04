<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- a11y GRP-7301: the trigger id is scoped to this menu rather than the shared literal
     "more-action-button" the older screens use, and aria-controls names the <ul> it opens.
     This include renders on the recipes screen alongside a per row actions menu, so a shared
     id would be a live duplicate on the page. --%>
 <div class="btn-group btn-block">

   <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreMcpRecipesActions']}" id="mcp-recipes-more-action-button" aria-controls="mcp-recipes-more-options" class="btn btn-medium btn-block dropdown-toggle"
     aria-haspopup="true" aria-expanded="false" onclick="$('#mcp-recipes-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#mcp-recipes-more-options li').first().focus();return true;});">
       ${textContainer.text['mcpRecipesMoreActionsButton'] } <span class="caret"></span></button>

   <ul class="dropdown-menu dropdown-menu-right" id="mcp-recipes-more-options">
       <li><a href="?operation=UiV2Mcp.addMcpRecipe" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Mcp.addMcpRecipe'); return false;"
           >${textContainer.text['mcpRecipesMoreActionsAddButton'] }</a></li>
   </ul>

 </div>
