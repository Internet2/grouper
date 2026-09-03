<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreDataFieldsActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" onclick="$('#data-field-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#data-field-more-options li').first().focus();return true;});">
       ${textContainer.text['privacyRealmMoreActionsButton'] } <span class="caret"></span></button>

   <ul class="dropdown-menu dropdown-menu-right" id="data-field-more-options">
       <li><a href="?operation=UiV2EntityDataFields.addPrivacyRealmConfiguration" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2EntityDataFields.addPrivacyRealmConfiguration'); return false;"
           >${textContainer.text['privacyRealmMoreActionsAddButton'] }</a></li>
    <li><a href="?operation=UiV2EntityDataFields.viewPrivacyRealmConfigs" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2EntityDataFields.viewPrivacyRealmConfigs'); return false;"
           >${textContainer.text['privacyRealmMoreActionsViewButton'] }</a></li>
   </ul>

 </div>