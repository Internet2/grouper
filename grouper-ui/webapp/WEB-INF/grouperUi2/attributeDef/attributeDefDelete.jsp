<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:titleFromKeyAndText('attributeDefDeleteTitle', grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.displayName)}

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.parentUuid}" />

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">Applications</li>
              </ul>
              --%>
              ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.extension)}
                <br /><small>${textContainer.text['attributeDefDeleteTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
            <c:choose>
              <c:when test="${grouperRequestContainer.attributeDefContainer.configPreventUiDeletion}">
                <p>${textContainer.text['attributeDefDeleteUiDisallowedText'] }</p>
                <div class="form-actions">
                  <button type="button" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}');" >${textContainer.text['groupDeleteCancelButton'] }</button>
                </div>
              </c:when>
              <c:otherwise>
                <p>${textContainer.text['attributeDefDeleteText'] }</p>
                <div class="form-actions"><button type="button" class="btn btn-primary" onclick="ajax('../app/UiV2AttributeDef.attributeDefDeleteSubmit?attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}'); return false;">${textContainer.text['attributeDefDeleteDeleteButton'] }</button> 
                <button type="button" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.id}');" >${textContainer.text['groupDeleteCancelButton'] }</button></div>
              </div>
              </c:otherwise>
            </c:choose>
            </div>
