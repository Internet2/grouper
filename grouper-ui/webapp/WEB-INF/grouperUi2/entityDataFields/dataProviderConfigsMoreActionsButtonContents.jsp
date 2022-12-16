<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreDataFieldsActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#data-field-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#data-field-more-options li').first().focus();return true;});">
       ${textContainer.text['dataProvidersMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="data-field-more-options">
       <li><a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.addDataProviderConfig'); return false;"
           >${textContainer.text['dataProviderMoreActionsAddButton'] }</a></li>
       <li><a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewDataProviders'); return false;"
           >${textContainer.text['dataProviderMoreActionsViewButton'] }</a></li>
   </ul>

 </div>