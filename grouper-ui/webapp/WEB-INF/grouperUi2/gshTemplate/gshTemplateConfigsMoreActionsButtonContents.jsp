<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGshTemplateActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#gsh-template-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#gsh-template-more-options li').first().focus();return true;});">
       ${textContainer.text['gshTemplatesMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="gsh-template-more-options">
       <li><a href="#" onclick="return guiV2link('operation=UiV2GshTemplateConfig.addGshTemplate'); return false;"
           >${textContainer.text['gshTemplatesMoreActionsAddButton'] }</a></li>
    <li><a href="#" onclick="return guiV2link('operation=UiV2GshTemplateConfig.viewGshTemplates'); return false;"
           >${textContainer.text['gshTemplatesMoreActionsViewButton'] }</a></li>
   </ul>

 </div>