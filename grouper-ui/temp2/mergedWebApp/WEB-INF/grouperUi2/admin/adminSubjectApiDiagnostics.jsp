<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['adminSubjectApiDiagnosticsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['adminSubjectApiDiagnosticsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['adminSubjectApiDiagnosticsTitle'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <div class="span12">
                <form class="form-horizontal" id="subjectApiDiagnosticsForm"
                    onsubmit="return false;">

                  <div class="control-group">
                    <label for="subjectApiSourceIdId" class="control-label">${textContainer.text['adminSubjectApiDiagnosticsSourceId'] }</label>
                    <div class="controls">
                      <select name="subjectApiSourceIdName" id="subjectApiSourceIdId" 
                          onchange="ajax('../app/UiV2Admin.subjectApiDiagnosticsSourceIdChanged', {formIds: 'subjectApiDiagnosticsForm'}); return false;">
                        <option value="" ></option>
                        
                        <c:forEach items="${grouperRequestContainer.subjectContainer.sources}" var="source" >
                          <option value="${grouper:escapeHtml(source.id)}">
                            ${grouper:escapeHtml(source.name) } (
                              <c:forEach var="subjectType" items="${source.subjectTypes}" varStatus="typeStatus">
                                <c:if test="${typeStatus.count>1}">, </c:if>
                                ${grouper:escapeHtml(subjectType)}
                              </c:forEach>
                            )                               
                          </option>
                        </c:forEach>
                        
                        
                      </select>                    
                      <span class="help-block">${textContainer.text['adminSubjectApiDiagnosticsSourceIdLabel'] }</span>
                    </div>
                  </div>

                  <div class="control-group">
                    <label for="subjectIdId" class="control-label">${textContainer.text['adminSubjectApiDiagnosticsSubjectId'] }</label>
                    <div class="controls">
                      <input type="text" id="subjectIdId" name="subjectIdName" value="" /> 
                      <span class="help-block">${textContainer.text['adminSubjectApiDiagnosticsSubjectIdLabel'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="subjectIdentifierId" class="control-label">${textContainer.text['adminSubjectApiDiagnosticsSubjectIdentifier'] }</label>
                    <div class="controls">
                      <input type="text" id="subjectIdentifierId" name="subjectIdentifierName" value="" /> 
                      <span class="help-block">${textContainer.text['adminSubjectApiDiagnosticsSubjectIdentifierLabel'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="searchStringId" class="control-label">${textContainer.text['adminSubjectApiDiagnosticsSearchString'] }</label>
                    <div class="controls">
                      <input type="text" id="searchStringId" name="searchStringName" value="" /> 
                      <span class="help-block">${textContainer.text['adminSubjectApiDiagnosticsSearchStringLabel'] }</span>
                    </div>
                  </div>
                  
                  <div class="control-group">
                    <label for="actAsComboID" class="control-label">${textContainer.text['adminSubjectApiDiagnosticsActAs'] }</label>
                    <div class="controls">
                      <grouper:combobox2 idBase="actAsCombo" style="width: 30em"
                                      filterOperation="../app/UiV2Admin.subjectApiDiagnosticsActAsCombo"/>
                      <span class="help-block">${textContainer.text['adminSubjectApiDiagnosticsActAsLabel'] }</span>
                    </div>
                  </div>
                  
                  
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Admin.subjectApiDiagnosticsRun', {formIds: 'subjectApiDiagnosticsForm'}); return false;">${textContainer.text['subjectApiDiagnosticsSubmitButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');" class="btn btn-cancel" role="button">${textContainer.text['groupCreateCancelButton'] }</a></div>
                  
                </form>
                <div id="subjectApiDiagnosticsResultsId">
                </div>
              </div>


