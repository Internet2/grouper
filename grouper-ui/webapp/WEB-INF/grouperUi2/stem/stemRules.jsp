<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('stemProvisioningPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

<input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

<%@ include file="../stem/stemHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <ul class="nav nav-tabs">
        <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemContents'] }</a></li>
        <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges}">
          <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Stem.stemPrivileges&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {dontScrollTop: true});" >${textContainer.text['stemPrivileges'] }</a></li>
        </c:if>
        <c:if test="${grouperRequestContainer.stemContainer.canReadPrivilegeInheritance}">
          <%@ include file="../stem/stemMoreTab.jsp" %>
        </c:if>
      </ul>
    </div>
   <div class="row-fluid">
      <div class="lead span9">${textContainer.text['rulesFolderSettingsTitle'] }</div>
      <div class="span3" id="grouperRulesFolderMoreActionsButtonContentsDivId">
        <%@ include file="rulesMoreActionsButtonContents.jsp"%>
      </div>
    </div>
    
    <div class="row-fluid">
      <div class="span9"> <p>${textContainer.text['rulesFolderDescription'] }</p></div>
    </div>
    
    <%@ include file="rulesTableHelper.jsp"%>
    
  </div>
</div>