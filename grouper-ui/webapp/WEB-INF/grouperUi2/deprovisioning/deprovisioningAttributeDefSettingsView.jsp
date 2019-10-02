<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}" />

            <%@ include file="../attributeDef/attributeDefHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});" >${textContainer.text['attributeDefAttributeDefNameTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb == 'perm'}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2AttributeDefAction.attributeDefActions&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});">${textContainer.text['attributeDefAttributeDefActionTab'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2AttributeDef.attributeDefPrivileges&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {dontScrollTop: true});" >${textContainer.text['attributeDefPrivilegesTab'] }</a></li>
                  </c:if>
                  <c:if test="${grouperRequestContainer.attributeDefContainer.canReadPrivilegeInheritance}">
                    <%@ include file="../attributeDef/attributeDefMoreTab.jsp" %>
                  </c:if>
                </ul>

                <div class="row-fluid">
                  <div class="lead span9">${textContainer.text['deprovisioningAttributeDefSettingsTitle'] }</div>
                  <div class="span3" id="deprovisioningAttributeDefMoreActionsButtonContentsDivId">
                    <%@ include file="deprovisioningAttributeDefMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                <%@ include file="deprovisioningObjectSettingsView.jsp"%>

              </div>
            </div>
