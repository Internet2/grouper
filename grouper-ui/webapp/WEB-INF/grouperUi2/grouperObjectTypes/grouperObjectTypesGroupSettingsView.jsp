<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('groupObjectTypesPageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}

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
      <div class="lead span9">${textContainer.text['objectTypeGroupSettingsTitle'] }</div>
      <div class="span3" id="grouperTypesGroupMoreActionsButtonContentsDivId">
        <%@ include file="grouperObjectTypesGroupMoreActionsButtonContents.jsp"%>
      </div>
    </div>
     <c:set var="ObjectType" value="Group" />
    <%@ include file="objectTypeObjectSettingsView.jsp"%>
    
  </div>
</div>
