<%@ include file="../assetsJsp/commonTaglib.jsp"%>


            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.parentUuid}" />

            <div class="bread-header-container">
              ${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.breadcrumbs}
              <div class="page-header blue-gradient">
  
                <div class="row-fluid">
                  <div class="span3 pull-right" id="attributeDefNameMoreActionsButtonContentsDivId">
                    <br /><br />
                    <%@ include file="attributeDefNameMoreActionsButtonContents.jsp"%>
                  </div>
    
                  <div class="span9 pull-left">
                    <h4>${textContainer.text['attributeDefNameHeaderAttributeDefinitionName'] }</h4>
                    <h1> <i class="fa fa-cogs"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayExtension)}
                    </h1>
                  </div>
                  
                  <div class="span12">
                  
                    <div id="attributeDefNameDetailsId">
                      <table class="table table-condensed table-striped">
                        <tbody>

                          <tr class="attributeDefNameDetailsName">
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameDescriptionLabel']}</strong></td>
                            <td>
                              ${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.description)}
                              <br /><span class="help-block description">${textContainer.text['attributeDefNameDescriptionDescription'] }</span>
                            </td>
                          </tr>

                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameEditAttributeDefLabel']}</strong></td>
                            <td>
                              <span>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.shortLinkWithIcon}</span>
                              <br /><span class="help-block description">${textContainer.text['attributeDefNameEditIntoAttributeDefDescription'] }</span>
                            </td>
                          </tr>
                          
                          <tr>
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameEditFolderLabel']}</strong></td>
                            <td>
                              <span>${grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.parentGuiStem.shortLinkWithIcon}</span>
                              <br /><span class="help-block description">${textContainer.text['attributeDefNameEditIntoFolderDescription'] }</span>
                            </td>
                          </tr>

                          <tr class="attributeDefNameDetailsName" style="display: none;">
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameIdLabel']}</strong></td>
                            <td>
                              <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.extension)}</span>
                              <span class="help-block description">${textContainer.text['attributeDefNameIdDescription'] }</span>
                            </td>
                          </tr>

                          <tr class="attributeDefNameDetailsName" style="display: none;">
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameNameLabel']}</strong></td>
                            <td>
                              <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayExtension)}</span>
                              <br /><span class="help-block description">${textContainer.text['attributeDefNameNameDescription'] }</span>
                            </td>
                          </tr>


                          <tr class="attributeDefNameDetailsName" style="display: none;">
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNamePathLabel']}</strong></td>
                            <td>
                              <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.displayName)}</span>
                              <br /><span class="help-block description">${textContainer.text['attributeDefNamePathDescription'] }</span>
                            </td>
                          </tr>

                          <tr class="attributeDefNameDetailsName" style="display: none;">
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameIdPathLabel']}</strong></td>
                            <td>
                              <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.name)}</span>
                              <br /><span class="help-block description">${textContainer.text['attributeDefNameIdPathDescription'] }</span>
                            </td>
                          </tr>


                          <tr class="attributeDefNameDetailsName" style="display: none;">
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameLabelCreated']}</strong></td>
                            <td>
                              <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.createdString)}</span>
                              <br /><span class="help-block description">${textContainer.text['attributeDefNameDescriptionCreated'] }</span>
                            </td>
                          </tr>


                          <tr class="attributeDefNameDetailsName" style="display: none;">
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameLabelLastEdited']}</strong></td>
                            <td>
                              <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.lastEditedString)}</span>
                              <br /><span class="help-block description">${textContainer.text['attributeDefNameDescriptionLastEdited'] }</span>
                            </td>
                          </tr>


                          <tr class="attributeDefNameDetailsName" style="display: none;">
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameLabelIdIndex']}</strong></td>
                            <td>
                              <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.idIndex)}</span>
                              <br /><span class="help-block description">${textContainer.text['attributeDefNameDescriptionIdIndex'] }</span>
                            </td>
                          </tr>

                          <tr class="attributeDefNameDetailsName" style="display: none;">
                            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['attributeDefNameLabelUuid']}</strong></td>
                            <td>
                              <span>${grouper:escapeHtml(grouperRequestContainer.attributeDefNameContainer.guiAttributeDefName.attributeDefName.id)}</span>
                              <br /><span class="help-block description">${textContainer.text['attributeDefNameDescriptionUuid'] }</span>
                            </td>
                          </tr>

                        </tbody>
                      </table>
                    </div>
                    <p id="attributeDefNameDetailsMoreId"><a href="#" aria-label="${textContainer.text['ariaLabelGuiMoreAttributeDefNameDetails']}" id="moreButtonId" onclick="$('.attributeDefNameDetailsName').show('slow'); $('#attributeDefNameDetailsMoreId').hide(); $('#attributeDefNameDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="fa fa-angle-down"></i></a></p>
                    <p id="attributeDefNameDetailsLessId" style="display: none"><a href="#" onclick="$('.attributeDefNameDetailsName').hide('slow'); $('#attributeDefNameDetailsLessId').hide(); $('#attributeDefNameDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="fa fa-angle-up"></i></a></p>
                                      
                  </div>
                </div> <%-- row fluid --%>
  
              </div>
            </div> <%-- bread header --%>
              