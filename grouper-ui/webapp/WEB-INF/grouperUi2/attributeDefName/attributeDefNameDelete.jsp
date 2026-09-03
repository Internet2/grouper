<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('attributeDefNameDeleteTitle', grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.breadcrumbs}

              <div class="page-header blue-gradient">
                <h4>${textContainer.text['attributeDefNameHeaderAttributeDefinitionName'] }</h4>
                <h1> <i aria-hidden="true" class="fa fa-cogs"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayExtension)}
                <br /><small>${textContainer.text['attributeDefNameDeleteTitle'] }</small></h1>
              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
            <c:choose>
              <c:when test="${grouperRequestContainer.attributeDefNameContainer.configPreventUiDeletion}">
                <p>${textContainer.text['attributeDefNameDeleteUiDisallowedText'] }</p>
                <div class="form-actions">
                  <button type="button" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}');" >${textContainer.text['attributeDefNameDeleteCancelButton'] }</button>
                </div>
              </c:when>
              <c:otherwise>
                <p>${textContainer.text['attributeDefNameDeleteText'] }</p>
                <div class="form-actions">
                  <button type="button" class="btn btn-primary" onclick="ajax('../app/UiV2AttributeDefName.deleteAttributeDefNameSubmit?attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}'); return false;">${textContainer.text['attributeDefNameDeleteDeleteButton'] }</button>
                  <button type="button" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}');" >${textContainer.text['attributeDefNameDeleteCancelButton'] }</button>
                </div>
              </c:otherwise>
            </c:choose>
              </div>
            </div>
