<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}

              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.displayExtension)}
                <br /><small>${textContainer.text['stemEditTitle'] }</small></h1>
              </div>

            </div>
            <script language="javascript">
              $(document).ready(function() {
                $('#stemId').on('input',function(e){
                  if ($('#originalExtension').val() == $('#stemId').val()) {
                    $('#alternateNameDiv').show("slow");
                    $('#setAlternateNameIfRenameDiv').hide("slow");
                  } else {
                    $('#alternateNameDiv').hide("slow");
                    $('#setAlternateNameIfRenameDiv').show("slow");
                  }
                });
              });
            </script>
            <div class="row-fluid">
              <div class="span12">
                <form id="editStemForm" class="form-horizontal">

                  <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
                  <input type="hidden" id="originalExtension" value="${grouperRequestContainer.stemContainer.guiStem.stem.extension}" />
                  
                  <div class="control-group">
                    <label for="stemName" class="control-label">${textContainer.text['stemCreateNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="stemName" name="displayExtension" 
                        value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.displayExtension)}" /><span 
                        class="help-block">${textContainer.text['stemCreateNameDescription'] }</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="stemId" class="control-label">${textContainer.text['stemCreateIdLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="stemId" name="extension"
                      value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.extension)}"
                       /><span class="help-block">${textContainer.text['stemCreateIdDescription'] }</span>
                    </div>
                  </div>
                  <div id="alternateNameDiv" class="control-group">
                    <label for="stemAlternateName" class="control-label">${textContainer.text['stemCreateAlternateNameLabel'] }</label>
                    <div class="controls">
                      <input type="text" id="stemAlternateName" name="alternateName"
                        value="${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.alternateName)}" /><span
                        class="help-block">${textContainer.text['stemCreateAlternateNameDescription'] }</span>
                    </div>
                  </div>
                  <div id="setAlternateNameIfRenameDiv" class="control-group" style="display: none">
                    <label for="stemRenameUpdateAlternateName" class="control-label">${textContainer.text['stemRenameUpdateAlternateNameLabel'] }</label>
                    <div class="controls">
                      <input type="checkbox" id="stemRenameUpdateAlternateName" name="setAlternateNameIfRename" checked="checked" value="true" /><span class="help-block">${textContainer.text['stemRenameUpdateAlternateNameDescription']}</span>
                    </div>
                  </div>
                  <div class="control-group">
                    <label for="stemDescription" class="control-label">${textContainer.text['stemCreateDescriptionLabel'] }</label>
                    <div class="controls">
                      <textarea id="stemDescription" name=description rows="3" cols="40" class="input-block-level">${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.description)}</textarea><span 
                        class="help-block">${textContainer.text['stemCreateDescriptionDescription'] }</span>
                    </div>
                  </div>
                  <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2Stem.stemEditSubmit', {formIds: 'editStemForm'}); return false;">${textContainer.text['groupCreateSaveButton'] }</a> 
                  <a href="#" onclick="return guiV2link('operation=UiV2Stem.viewStem?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');" class="btn btn-cancel" role="button">${textContainer.text['stemCreateCancelButton'] }</a></div>
                </form>
              </div>
            </div>
