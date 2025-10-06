<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- for the new group or new stem button --%>
<input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

<%@ include file="../group/groupHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <c:set var="grouperCurrentTab" value="none" />
      <%@ include file="../group/groupTabs.jsp" %>
    </div>
    <div class="row-fluid">
      <div class="lead span9">${textContainer.text['provisioningGroupSettingsTitle'] }</div>
    </div>
    <div class="row-fluid">
      <div class="span9"> <p>${textContainer.text['provisioningGroupSettingsDescription'] }</p></div>
    </div>
    
    <%@ include file="provisioningGroupProvisionersTableHelper.jsp"%>
    
    <c:set var="ObjectType" value="Group" />
    <%@ include file="provisioningSettingsView.jsp"%>
    
  </div>
</div>
