<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('groupProvisioningPageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}
<grouper:browserPage jspName="provisioningGroupProvisioners" />
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
      <div class="lead span9">${textContainer.text['provisioningGroupProvisioningTitle'] }</div>
    </div>
    
    <div class="row-fluid">
      <div class="span9"> <p>${textContainer.text['provisioningGroupProvisioningDescription'] }</p></div>
    </div>
    
       <div class="row-fluid">
      <div class="span12"> 
       <a href="#" onclick="$('#provisioningGroupDocumentation').toggle('slow'); return false;">
        ${textContainer.text['provisioning.documentationLink'] }
       </a>
      </div>
    </div>
    
    <div class="row-fluid">
      <div class="span12" style="min-height: 10px;"> 
        <div id="provisioningGroupDocumentation" style="display: none; font-weight:normal;">
         ${textContainer.text['provisioning.group.documentation']}
        </div>
      </div>
   </div>
    
    <%@ include file="provisioningGroupProvisionersTableHelper.jsp"%>
    
  </div>
</div>
        