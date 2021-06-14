<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreSqlSyncActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#sql-sync-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#sql-sync-more-options li').first().focus();return true;});">
       ${textContainer.text['sqlSyncMoreActionsButton'] } <span class="caret"></span></a>

   <ul class="dropdown-menu dropdown-menu-right" id="sql-sync-more-options">
       <li><a href="#" onclick="return guiV2link('operation=UiV2SqlSyncConfiguration.addSqlSyncConfiguration'); return false;"
           >${textContainer.text['sqlSyncMoreActionsAddButton'] }</a></li>
    <li><a href="#" onclick="return guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations'); return false;"
           >${textContainer.text['sqlSyncMoreActionsViewButton'] }</a></li>
   </ul>

 </div>