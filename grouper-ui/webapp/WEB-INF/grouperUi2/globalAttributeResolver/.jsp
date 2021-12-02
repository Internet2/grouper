<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreWsTrustedJwtsActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#jwt-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#jwt-more-options li').first().focus();return true;});">
       ${textContainer.text['wsTrustedJwtsMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="jwt-more-options">
       <li><a href="#" onclick="return guiV2link('operation=UiV2AuthenticationConfig.addWsTrustedJwt'); return false;"
           >${textContainer.text['wsTrustedJwtsMoreActionsAddButton'] }</a></li>
    <li><a href="#" onclick="return guiV2link('operation=UiV2AuthenticationConfig.viewWsTrustedJwts'); return false;"
           >${textContainer.text['wsTrustedJwtsMoreActionsViewButton'] }</a></li>
   </ul>

 </div>