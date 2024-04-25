<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  ${grouper:titleFromKeyAndText('groupRulesPageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}

            <!-- start group/groupViewAudits.jsp -->
            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
                <div class="tab-interface">
                  <ul class="nav nav-tabs">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                    <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                      <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                    </c:if>
                    <%@ include file="groupMoreTab.jsp" %>
                  </ul>
            </div>
    
       <div class="row-fluid">
          <div class="lead span9">${textContainer.text['rulesGroupSettingsTitle'] }</div>
          <div class="span3" id="grouperRulesFolderMoreActionsButtonContentsDivId">
            <%@ include file="rulesMoreActionsButtonContents.jsp"%>
          </div>
        </div>
        
        <div class="row-fluid">
          <div class="span9"> <p>${textContainer.text['rulesGroupDescription'] }</p></div>
        </div>
        
        <div class="row-fluid">
          <div class="span9"> 
            <a href="https://spaces.at.internet2.edu/display/Grouper/Grouper+rules+UI">${textContainer.text['rulesDocumentationLink']}</a>
          </div>
        </div>
        
        <%@ include file="rulesTableHelper.jsp"%>
        
      </div>
</div>