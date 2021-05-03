<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

<%@ include file="../group/groupHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <ul class="nav nav-tabs">
        <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
        <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
          <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
        </c:if>
        <%@ include file="../group/groupMoreTab.jsp" %>
      </ul>
    </div>
    <div class="row-fluid">
      <div class="lead span9">${textContainer.text['provisioningGroupLogsTitle'] }</div>
      <div class="span3" id="grouperProvisioningGroupMoreActionsButtonContentsDivId">
        <%@ include file="provisioningGroupMoreActionsButtonContents.jsp"%>
      </div>
    </div>
    <div class="row-fluid">
      <div class="span9"> <p>${textContainer.text['provisioningGroupLogsDescription'] }</p></div>
    </div>
    
    <%@ include file="provisioningGroupProvisionersTableHelper.jsp"%>
    
    <form class="form-inline form-small" name="provisioningTargetGroupLogsFormName" id="provisioningTargetGroupLogsFormId">
     	<table class="table table-hover table-bordered table-striped table-condensed data-table">
          <thead>        
            <tr>
              <th>${textContainer.text['provisionerLogsTableHeaderSyncTimestamp']}</th>
              <th>${textContainer.text['provisionerLogsTableHeaderStatus']}</th>
              <th>${textContainer.text['provisionerLogsTableHeaderRecordsProcessed']}</th>
              <th>${textContainer.text['provisionerLogsTableHeaderRecordsChanged']}</th>
              <th>${textContainer.text['provisionerLogsTableHeaderJobTookMillis']}</th>
              <th>${textContainer.text['provisionerLogsTableHeaderServer']}</th>
              <th>${textContainer.text['provisionerLogsTableHeaderDescription']}</th>
            </tr>
          </thead>
          <tbody>
          
          <c:forEach items="${grouperRequestContainer.provisioningContainer.gcGrouperSyncLogs}" var="gcGrouperSyncLog" >
	       <tr>
			                   <td style="white-space: nowrap;">
                    			${gcGrouperSyncLog.syncTimestamp}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${gcGrouperSyncLog.statusDb}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${gcGrouperSyncLog.recordsProcessed}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${gcGrouperSyncLog.recordsChanged}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${gcGrouperSyncLog.jobTookMillis}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
                    			${gcGrouperSyncLog.server}
			                   </td>
			                   
			                   <td style="white-space: nowrap;">
			                   <grouper:abbreviateTextarea text="${gcGrouperSyncLog.descriptionOrDescriptionClob}" 
							 	showCharCount="30" cols="20" rows="3"/>
			                   </td>
			                   
			                </tr>
	       
         </c:forEach>
          
          </tbody>
        </table>
        </form>
        
        <c:if test="${fn:length(grouperRequestContainer.provisioningContainer.gcGrouperSyncLogs) > 0}">
	        <div class="data-table-bottom gradient-background">
	           <grouper:paging2 guiPaging="${grouperRequestContainer.provisioningContainer.guiPaging}" 
	           	formName="provisioningTargetGroupLogsPagingForm" ajaxFormIds="provisioningTargetGroupLogsFormId"
	               refreshOperation="../app/UiV2Provisioning.viewProvisioningTargetLogsOnGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}&provisioningTargetName=${grouperRequestContainer.provisioningContainer.targetName}" />
	      	</div>
        </c:if>
     
  </div>
</div>
        