<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.breadcrumbs}

              <div class="page-header blue-gradient">
                <h4>${textContainer.text['attributeDefNameHeaderAttributeDefinitionName'] }</h4>
                <h1> <i class="fa fa-cogs"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayExtension)}
                <br /><small>${textContainer.text['attributeDefNameDeleteTitle'] }</small></h1>
              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
                <p>${textContainer.text['attributeDefNameDeleteText'] }</p>
                <div class="form-actions">
                  <a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2AttributeDefName.deleteAttributeDefNameSubmit?attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}'); return false;">${textContainer.text['attributeDefNameDeleteDeleteButton'] }</a> 
                  <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id}');" >${textContainer.text['attributeDefNameDeleteCancelButton'] }</a>
                </div>
              </div>
            </div>
