<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreWsTrustedJwtsActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" onclick="$('#jwt-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#jwt-more-options li').first().focus();return true;});">
       ${textContainer.text['wsTrustedJwtsMoreActionsButton'] } <span class="caret"></span></button>

   <ul class="dropdown-menu dropdown-menu-right" id="jwt-more-options">
       <li><a href="?operation=UiV2AuthenticationConfig.addWsTrustedJwt" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AuthenticationConfig.addWsTrustedJwt'); return false;"
           >${textContainer.text['wsTrustedJwtsMoreActionsAddButton'] }</a></li>
    <li><a href="?operation=UiV2AuthenticationConfig.viewWsTrustedJwts" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2AuthenticationConfig.viewWsTrustedJwts'); return false;"
           >${textContainer.text['wsTrustedJwtsMoreActionsViewButton'] }</a></li>
   </ul>

 </div>