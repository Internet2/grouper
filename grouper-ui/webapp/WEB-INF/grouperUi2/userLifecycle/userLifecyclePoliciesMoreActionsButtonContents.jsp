<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreUserLifecycleActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#data-field-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#data-field-more-options li').first().focus();return true;});">
       ${textContainer.text['userLifecyclePolicyMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="data-field-more-options">
       <li><a href="?operation=UiV2UserLifecycle.addUserLifecyclePolicyConfiguration" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.addUserLifecyclePolicyConfiguration'); return false;"
           >${textContainer.text['userLifecyclePolicyMoreActionsAddButton'] }</a></li>
    <li><a href="?operation=UiV2UserLifecycle.viewUserLifecyclePolicies" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2UserLifecycle.viewUserLifecyclePolicies'); return false;"
           >${textContainer.text['userLifecyclePolicyMoreActionsViewButton'] }</a></li>
   </ul>

 </div>