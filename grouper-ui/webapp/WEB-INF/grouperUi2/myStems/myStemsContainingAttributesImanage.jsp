<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('myStemsBreadcrumb')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myStemsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['myStemsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['myStemsTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="?operation=UiV2MyStems.myStems" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2MyStems.myStems', {dontScrollTop: true});" >${textContainer.text['myStemsImanageTab'] }</a></li>
                  <li><a role="tab" href="?operation=UiV2MyStems.myStemsContainingGroupsImanage" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2MyStems.myStemsContainingGroupsImanage', {dontScrollTop: true});" >${textContainer.text['myStemsContainingGroupsImanageTab'] }</a></li>
                  <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;">${textContainer.text['myStemsContainingAttributesImanageTab'] }</a></li>
                </ul>
                <p class="lead">${textContainer.text['myStemsContainingAttributesImanageDescription'] }</p>
                <form class="form-horizontal form-filter" id="myStemsForm"
                    onsubmit="ajax('../app/UiV2MyStems.myStemsContainingAttributesImanageSubmit', {formIds: 'myStemsForm'}); return false;">
                  <div class="control-group">
                    <label class="control-label" for="stem-filter">${textContainer.text['myStemsFilterFor']}</label>
                    <div class="controls">
                      <select id="stem-filter" name="stemFilterType" style="width:100%; max-width:420px;"><option value="all">${textContainer.text['myStemsFilterOptionAll'] }</option>
                        <option value="createGroups">${textContainer.text['myStemsFilterOptionCreateGroups'] }</option>
                        <option value="createStems">${textContainer.text['myStemsFilterOptionCreateStems'] }</option>
                        <option value="attributeRead">${textContainer.text['myStemsFilterOptionAttributeRead'] }</option>
                        <option value="attributeUpdate">${textContainer.text['myStemsFilterOptionAttributeUpdate'] }</option>
<%-- TODO this doesnt exist in the API     <option value="groupsManage">${textContainer.text['myStemsFilterOptionGroupsManage'] }</option>   --%></select>
                    </div>
                  </div>
                  <div class="control-group">
                    <label class="control-label" for="myStemsFilterId">${textContainer.text['myStemsSearchNamePlaceholder']}</label>
                    <div class="controls">
                      <input type="text" name="myStemsFilter" id="myStemsFilterId" style="width:100%; max-width:420px;"/>
                    </div>
                  </div>
                  <div class="form-actions">
                    <button type="submit" class="btn" aria-controls="myStemsResultsId" onclick="ajax('../app/UiV2MyStems.myStemsContainingAttributesImanageSubmit', {formIds: 'myStemsPagingFormId,myStemsForm'}); return false;">${textContainer.text['myStemsApplyFilterButton'] }</button>
                    <button type="submit" onclick="ajax('../app/UiV2MyStems.myStemsContainingAttributesImanageReset', {formIds: 'myStemsPagingFormId'}); return false;" class="btn">${textContainer.text['myStemsResetButton'] }</button>
                  </div>
                </form>
                <div id="myStemsResultsId" role="region" aria-live="polite">
                </div>
              </div>


