<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('folderRulesPageTitle', grouperRequestContainer.stemContainer.guiStem.stem.displayName)}

<input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

<%@ include file="../stem/stemHeader.jsp" %>

<div class="row-fluid">
  <div class="span12">
    <div id="messages"></div>
        
    <div class="tab-interface">
      <c:set var="grouperCurrentTab" value="none" />
      <%@ include file="../stem/stemTabs.jsp" %>
    </div>
     <div class="row-fluid">
      <div class="lead span9">${textContainer.text['rulesFolderSettingsTitle'] }</div>
      <div class="span3" id="grouperRulesFolderMoreActionsButtonContentsDivId">
        <%@ include file="rulesMoreActionsButtonContents.jsp"%>
      </div>
    </div>
  
    <div class="row-fluid">
      <div class="span12"> <p>${textContainer.text['rulesFolderDescription'] }</p></div>
    </div>
    
    <div class="row-fluid">
      <div class="span9"> 
        <a href="https://spaces.at.internet2.edu/display/Grouper/Grouper+rules+UI">${textContainer.text['rulesDocumentationLink']}</a>
      </div>
    </div>

    <%@ include file="rulesTableHelper.jsp"%>
    
  </div>
</div>