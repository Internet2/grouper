<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreDataFieldsActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="button" onclick="$('#data-field-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#data-field-more-options li').first().focus();return true;});">
       ${textContainer.text['dataFieldsMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="data-field-more-options">
       <li><a href="?operation=UiV2EntityDataFields.addDataFieldConfiguration" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2EntityDataFields.addDataFieldConfiguration'); return false;"
           >${textContainer.text['dataFieldsMoreActionsAddButton'] }</a></li>
    <li><a href="?operation=UiV2EntityDataFields.viewEntityDataFields" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2EntityDataFields.viewEntityDataFields'); return false;"
           >${textContainer.text['dataFieldsMoreActionsViewButton'] }</a></li>
   </ul>

 </div>