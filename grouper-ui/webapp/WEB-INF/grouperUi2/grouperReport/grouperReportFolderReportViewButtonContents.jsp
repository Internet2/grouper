<%@ include file="../assetsJsp/commonTaglib.jsp"%>

  <c:set var="configInstance" value="${grouperRequestContainer.grouperReportContainer.grouperReportConfigInstance}" />
  <div class="btn-group btn-block">
  
    <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGrouperReportActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
      aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#grouper-report-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouper-report-more-options li').first().focus();return true;});">
        ${textContainer.text['grouperReportMoreActionsButton'] } <span class="caret"></span></a>

    <ul class="dropdown-menu dropdown-menu-right" id="grouper-report-more-options">
    
      <c:if test="${fn:length(configInstance.guiReportInstances) > 0}">
	      <c:set var="mostRecentGuiReportInstance" value="${configInstance.guiReportInstances[0]}" />
	      <li><a href="../app/UiV2GrouperReport.downloadReportForFolder?attributeAssignId=${mostRecentGuiReportInstance.reportInstance.attributeAssignId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}">${textContainer.text['grouperReportConfigTableReportActionsDownloadMostRecent'] }</a></li>
	      <c:if test="${guiReportConfig.canRead}">
	       <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.viewLogs&jobName=grouper_report_${grouperRequestContainer.stemContainer.guiStem.stem.id}_${configInstance.reportConfigBean.attributeAssignmentMarkerId}');">${textContainer.text['grouperReportConfigTableReportActionsReportLogs'] }</a></li>
	      </c:if>
      </c:if>
     
	    <c:if test="${grouperRequestContainer.grouperReportContainer.canWriteGrouperReports }">
	      <c:choose>
	        <c:when test="${configInstance.reportConfigBean.reportConfigEnabled}">
	          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.changeReportConfigStatusForFolder&newStatus=disable&attributeAssignmentMarkerId=${configInstance.reportConfigBean.attributeAssignmentMarkerId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">${textContainer.text['grouperReportConfigTableReportActionsDisbleReportConfig'] }</a></li>
	        </c:when>
	        <c:otherwise>
	          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.changeReportConfigStatusForFolder&newStatus=enable&attributeAssignmentMarkerId=${configInstance.reportConfigBean.attributeAssignmentMarkerId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">${textContainer.text['grouperReportConfigTableReportActionsEnableReportConfig'] }</a></li>
	        </c:otherwise>
	      </c:choose>
	      <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperReport.deleteReportConfigForFolder&attributeAssignmentMarkerId=${configInstance.reportConfigBean.attributeAssignmentMarkerId}&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');">${textContainer.text['grouperReportConfigTableReportActionsDeleteReportConfig'] }</a></li>                             
	    </c:if>
    </ul>

  </div>