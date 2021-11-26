<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGlobalAttributeResolverConfigActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#resolver-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#jwt-more-options li').first().focus();return true;});">
       ${textContainer.text['wsTrustedJwtsMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="resolver-more-options">
       <li><a href="#" onclick="return guiV2link('operation=UiV2GlobalAttributeResolverConfig.addGlobalAttributeResolverConfig'); return false;"
           >${textContainer.text['globalAttributeResolverConfigsMoreActionsAddButton'] }</a></li>
    <li><a href="#" onclick="return guiV2link('operation=UiV2GlobalAttributeResolverConfig.viewGlobalAttributeResolverConfigs'); return false;"
           >${textContainer.text['globalAttributeResolverConfigsMoreActionsViewButton'] }</a></li>
   </ul>

 </div>