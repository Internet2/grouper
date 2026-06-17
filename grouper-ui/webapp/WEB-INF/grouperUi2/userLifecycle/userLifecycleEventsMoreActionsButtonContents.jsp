<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreUserLifecycleActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="button" onclick="$('#data-field-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#data-field-more-options li').first().focus();return true;});">
       ${textContainer.text['userLifecycleEventsMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="data-field-more-options">
       <li><a href="?operation=UiV2UserLifecycle.addUserLifecycleEventConfiguration" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.addUserLifecycleEventConfiguration'); return false;"
           >${textContainer.text['userLifecycleEventsMoreActionsAddButton'] }</a></li>
    <li><a href="?operation=UiV2UserLifecycle.viewUserLifecycleEventsConfigs" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecycleEventsConfigs'); return false;"
           >${textContainer.text['userLifecycleEventsMoreActionsViewButton'] }</a></li>
   </ul>

 </div>