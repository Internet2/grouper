<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.stemId}" />

            <%@ include file="../attributeDef/attributeDefHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <ul class="nav nav-tabs">
                  <li><a role="tab"  aria-selected="true" href="#" onclick="return false;" >${textContainer.text['attributeDefAttributeDefNameTab'] }</a></li>
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
                
                <form class="form-inline form-small form-filter" id="editDeprovisioningFormId">
                  <input type="hidden" name="attributeDefId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}" />
                  <table class="table table-condensed table-striped">
                    <tbody>
                      <c:set var="ObjectType" 
                          value="AttributeDef" />

                      <%@ include file="deprovisioningObjectSettingsEditHelper.jsp" %>
                      <tr>
                        <td></td>
                        <td
                          style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
                          <input type="submit" class="btn btn-primary"
                          aria-controls="deprovisioningSubmitId" id="submitId"
                          value="${textContainer.text['deprovisioningEditButtonSave'] }"
                          onclick="ajax('../app/UiV2Deprovisioning.deprovisioningOnAttributeDefEditSave?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}', {formIds: 'editDeprovisioningFormId'}); return false;">
                          &nbsp; <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2Deprovisioning.deprovisioningOnAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;"
                          >${textContainer.text['deprovisioningEditButtonCancel'] }</a>
                        </td>
                      </tr>

                    </tbody>
                  </table>
                  
                </form>

  </div>
</div>

    <c:if test="${grouperRequestContainer.indexContainer.menuRefreshOnView}">
      <script>dojoInitMenu(${grouperRequestContainer.indexContainer.menuRefreshOnView});</script>
    </c:if>