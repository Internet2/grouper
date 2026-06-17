<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreDataFieldsActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="button" onclick="$('#data-field-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#data-field-more-options li').first().focus();return true;});">
       ${textContainer.text['dataProviderChangeLogQueryMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="data-field-more-options">
       <li><a href="?operation=UiV2EntityDataFields.addDataProviderChangeLogQueryConfig" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2EntityDataFields.addDataProviderChangeLogQueryConfig'); return false;"
           >${textContainer.text['dataProviderChangeLogQueryMoreActionsAddButton'] }</a></li>
       <li><a href="?operation=UiV2EntityDataFields.viewEntityDataProviderChangeLogQueries" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2EntityDataFields.viewEntityDataProviderChangeLogQueries'); return false;"
           >${textContainer.text['dataProviderChangeLogQueryMoreActionsViewButton'] }</a></li>
   </ul>

 </div>
