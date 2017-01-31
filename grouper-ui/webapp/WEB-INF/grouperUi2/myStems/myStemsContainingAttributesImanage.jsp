<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myStemsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['myStemsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['myStemsTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2MyStems.myStems', {dontScrollTop: true});" >${textContainer.text['myStemsImanageTab'] }</a></li>
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2MyStems.myStemsContainingGroupsImanage', {dontScrollTop: true});" >${textContainer.text['myStemsContainingGroupsImanageTab'] }</a></li>
                  <li class="active"><a role="tab" aria-selected="true" href="#" onclick="return false;">${textContainer.text['myStemsContainingAttributesImanageTab'] }</a></li>
                </ul>
                <p class="lead">${textContainer.text['myStemsContainingAttributesImanageDescription'] }</p>
                <form class="form-inline form-filter" id="myStemsForm"
                    onsubmit="ajax('../app/UiV2MyStems.myStemsContainingAttributesImanageSubmit', {formIds: 'myStemsForm'}); return false;">
                  <div class="row-fluid">

                    <div class="span1">
                      <label for="stem-filter" style="white-space: nowrap;">${textContainer.text['myStemsFilterFor'] }</label>
                    </div>
                    <div class="span5">
                      <select id="stem-filter" class="span12" name="stemFilterType">
                        <option value="all">${textContainer.text['myStemsFilterOptionAll'] }</option>
                        <option value="createGroups">${textContainer.text['myStemsFilterOptionCreateGroups'] }</option>
                        <option value="createStems">${textContainer.text['myStemsFilterOptionCreateStems'] }</option>
                        <option value="attributeRead">${textContainer.text['myStemsFilterOptionAttributeRead'] }</option>
                        <option value="attributeUpdate">${textContainer.text['myStemsFilterOptionAttributeUpdate'] }</option>
<%-- TODO this doesnt exist in the API     <option value="groupsManage">${textContainer.text['myStemsFilterOptionGroupsManage'] }</option>   --%>
                      </select>
                      
                    </div>
                    <div class="span3"  style="white-space: nowrap;">
                      <input type="text" name="myStemsFilter" placeholder="${textContainer.textEscapeXml['myStemsSearchNamePlaceholder'] }" id="myStemsFilterId" class="span12"/>
                    </div>
                    <div class="span3"> &nbsp; &nbsp;
                      <button type="submit" class="btn" onclick="ajax('../app/UiV2MyStems.myStemsContainingAttributesImanageSubmit', {formIds: 'myStemsPagingFormId,myStemsForm'}); return false;">${textContainer.text['myStemsApplyFilterButton'] }</button>
                      <button type="submit" onclick="ajax('../app/UiV2MyStems.myStemsContainingAttributesImanageReset', {formIds: 'myStemsPagingFormId'}); return false;" class="btn">${textContainer.text['myStemsResetButton'] }</button>
                    </div>
                  </div>
                </form>
                <div id="myStemsResultsId">
                </div>
              </div>


