<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreCustomUiActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#custom-ui-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#custom-ui-more-options li').first().focus();return true;});">
       ${textContainer.text['customUiMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="custom-ui-more-options">
       <li><a href="#" onclick="return guiV2link('operation=UiV2CustomUiConfig.addCustomUiConfig'); return false;"
           >${textContainer.text['customUiMoreActionsAddButton'] }</a></li>
    <li><a href="#" onclick="return guiV2link('operation=UiV2CustomUiConfig.viewCustomUiConfigs'); return false;"
           >${textContainer.text['customUiMoreActionsViewButton'] }</a></li>
   </ul>

 </div>