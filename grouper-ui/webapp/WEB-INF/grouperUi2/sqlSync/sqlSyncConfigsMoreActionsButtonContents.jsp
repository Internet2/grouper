<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <div class="btn-group btn-block">
 
   <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreSqlSyncActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
     aria-haspopup="true" aria-expanded="false" onclick="$('#sql-sync-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#sql-sync-more-options li').first().focus();return true;});">
       ${textContainer.text['sqlSyncMoreActionsButton'] } <span class="caret"></span></button>

   <ul class="dropdown-menu dropdown-menu-right" id="sql-sync-more-options">
       <li><a href="?operation=UiV2SqlSyncConfiguration.addSqlSyncConfiguration" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2SqlSyncConfiguration.addSqlSyncConfiguration'); return false;"
           >${textContainer.text['sqlSyncMoreActionsAddButton'] }</a></li>
    <li><a href="?operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations'); return false;"
           >${textContainer.text['sqlSyncMoreActionsViewButton'] }</a></li>
   </ul>

 </div>