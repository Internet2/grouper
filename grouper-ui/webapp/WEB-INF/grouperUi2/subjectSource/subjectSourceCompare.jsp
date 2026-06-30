<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscellaneousSubjectSourceConfigCompareBreadcrumb')}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2SubjectSource.viewSubjectSources" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2SubjectSource.viewSubjectSources');">${textContainer.text['miscellaneousSubjectSourcesOverallBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['miscellaneousSubjectSourceConfigCompareBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h4>${textContainer.text['miscellaneousSubjectSourcesMainDescription'] }</h4></div>
                  <div class="span2 pull-right">
                    <%@ include file="subjectSourcesMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                ${textContainer.text['miscellaneousSubjectSourcesCompareDescription'] }
                <br /><br />
                <form class="form-horizontal" id="subjectApiCompareForm"
                    onsubmit="return false;">

                  <div class="control-group">
                    <label for="actAsId" class="control-label">${textContainer.text['subjectSourcesCompareActAsSubjectIdOrIdentifier'] }</label>
                    <div class="controls">
                      <input type="text" id="actAsId" name="actAsName" />
                      <span class="help-block">${textContainer.text['subjectSourcesCompareActAsSubjectIdOrIdentifierLabel'] }</span>
                    </div>
                  </div>
				
                  <div class="control-group">
                    <label for="subjectApiSourceIdId" class="control-label">${textContainer.text['subjectSourcesCompareSourceId'] }</label>
                    <div class="controls">
                      <input type="hidden" name="subjectApiSourceIdName" id="subjectApiSourceIdId" value="${grouper:escapeHtml(grouperRequestContainer.subjectSourceContainer.source.id)}" />                    	
                      ${grouper:escapeHtml(grouperRequestContainer.subjectSourceContainer.source.id)}
                      <span class="help-block">${textContainer.text['subjectSourcesCompareSourceIdLabel'] }</span>
                    </div>
                  </div>

                  <div class="control-group">
                    <label for="otherSubjectApiSourceIdId" class="control-label">${textContainer.text['subjectSourcesCompareOtherSourceId'] }</label>
                    <div class="controls">
                      <select name="otherSubjectApiSourceIdId" id="otherSubjectApiSourceIdId">
                        <c:forEach items="${grouperRequestContainer.subjectSourceContainer.sources}" var="source" >
                          <option value="${grouper:escapeHtml(source.id)}">
                            ${grouper:escapeHtml(source.id)} (${grouper:escapeHtml(source.name)})
                          </option>
                        </c:forEach>
                      </select>
                      <span class="help-block">
                        ${grouperRequestContainer.subjectSourceContainer.source.enabled ? textContainer.text['subjectSourcesCompareDisabledSourceIdLabel'] : textContainer.text['subjectSourcesCompareEnabledSourceIdLabel'] }
                      </span>
                    </div>
                  </div>


                  <div class="control-group">
                    <label for="subjectIdsId" class="control-label">${textContainer.text['subjectSourcesCompareSubjectId'] }</label>
                    <div class="controls">
                      <textarea id="subjectIdsId" name="subjectIdsName" rows="5"></textarea> 
                      <span class="help-block">${textContainer.text['subjectSourcesCompareSubjectIdLabel'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="subjectIdentifiersId" class="control-label">${textContainer.text['subjectSourcesCompareSubjectIdentifier'] }</label>
                    <div class="controls">
                      <textarea id="subjectIdentifiersId" name="subjectIdentifiersName" rows="5"></textarea> 
                      <span class="help-block">${textContainer.text['subjectSourcesCompareSubjectIdentifierLabel'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="searchStringsId" class="control-label">${textContainer.text['subjectSourcesCompareSearchString'] }</label>
                    <div class="controls">
                      <textarea id="searchStringsId" name="searchStringsName" rows="5"></textarea>
                      <span class="help-block">${textContainer.text['subjectSourcesCompareSearchStringLabel'] }</span>
                    </div>
                  </div>
                  
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2SubjectSource.compareSubjectSourcesSubmit', {formIds: 'subjectApiCompareForm'}); return false;">${textContainer.text['subjectSourcesCompareSubmitButton'] }</a> 
                  
                  <a class="btn btn-cancel" role="button"
                          onclick="return guiV2link('operation=UiV2SubjectSource.viewSubjectSources'); return false;"
                          >${textContainer.text['subjectSourcesAddFormCancelButton'] }</a>
                  </div>
                  
                </form>
                <div id="subjectApiCompareResultsId">
                </div>
              </div>
           </div>

