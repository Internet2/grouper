<%-- GRP-7351: the CELLS of one config property row. Extracted from configure.jsp so a single row
     can be re-rendered in place after an edit instead of redrawing the whole listing. This is the
     inside of the tr, not the tr itself, so it can be dropped into an existing row_N with
     newInnerHtmlFromJsp, the same way configurationFileEditEntry.jsp is. Statically included so
     the listing loop pays no per row dispatch, and so it inherits guiConfigProperty /
     configItemMetadata / i from whoever includes it. No taglib directive on purpose. --%>
                              <td style="vertical-align: top">
                                
                                <c:if test="${guiConfigProperty.fromDatabase}">
                                  <input type="checkbox" aria-label="${textContainer.text['configurationSelectConfigCheckboxAriaLabel']}" 
                                    name="propertyNameName" value="${grouper:escapeUrl(configItemMetadata.keyOrSampleKey)}"
                                    class="configCheckbox" />
                                </c:if>
                                
                              </td>
		                          <td style="vertical-align: top">
		                             
		                            <div class="btn-group"><button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiConfigurationFilesActions']}" class="btn btn-mini dropdown-toggle"
		                              aria-haspopup="true" aria-expanded="false" onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
		                                ${textContainer.text['groupViewActionsButton'] } 
		                                <span class="caret"></span>
		                              </button>
		                              <ul class="dropdown-menu dropdown-menu-right" id="more-options${i}">
		                                <li><a href="#" onclick="return ajax('../app/UiV2Configure.configurationFileItemEdit?configFile=${grouper:escapeUrl(grouperRequestContainer.configurationContainer.configFileName.configFileName)}&propertyNameName=${grouper:escapeUrl(configItemMetadata.keyOrSampleKey)}&index=${i}');" class="actions-revoke-membership">${textContainer.text['configurationFilesActionEdit'] }</a></li>
		                                <c:if test="${guiConfigProperty.fromDatabase }">
		                                  <li><a href="#" onclick="return ajax('../app/UiV2Configure.configurationFileItemDelete?configFile=${grouper:escapeUrl(grouperRequestContainer.configurationContainer.configFileName.configFileName)}&propertyNameName=${grouper:escapeUrl(configItemMetadata.keyOrSampleKey)}&index=${i}');" class="actions-revoke-membership">${textContainer.text['configurationFilesActionDelete'] }</a></li>
		                                </c:if>
		                              </ul>
		                            </div>
		                          </td>
		                          <td style="vertical-align: top">
		                            <b>${grouper:escapeHtml(configItemMetadata.keyOrSampleKey)}</b><br />
		                            <c:if test="${guiConfigProperty.hasType}">
		                              <span style="font-size: 90%">${textContainer.text['configurationTypeLabel']} ${grouper:escapeHtml(guiConfigProperty.type) }</span>
		                            </c:if>
		                            <c:if test="${configItemMetadata.multiple}">
		                              <span style="font-size: 90%">${textContainer.text['configurationMultiple']}</span>
		                            </c:if>
		                            <c:if test="${! grouper:isBlank(configItemMetadata.mustExtendClass)}">
		                              <span style="font-size: 90%">${textContainer.text['configurationMustExtendClass']} ${configItemMetadata.mustExtendClass }</span>
		                            </c:if>
		                            <c:if test="${! grouper:isBlank(configItemMetadata.mustImplementInterface)}">
		                              <span style="font-size: 90%">${textContainer.text['configurationMustImplementInterface']} ${configItemMetadata.mustImplementInterface }</span>
		                            </c:if>
		                            
		                          </td>
		                          <td style="vertical-align: top">
		                            <%-- keep whitespace out of the equation to make copy/paste on screen easier --%>
		                            <b>
                                <c:choose>
                                  <%-- newlines means code so pre-format --%>
                                  <c:when test="${guiConfigProperty.propertyValueContainsNewline}">
                                     <pre><grouper:abbreviateTextarea text="${guiConfigProperty.propertyValue}"  showCharCount="50" cols="20" rows="3"/></pre>
                                  </c:when>
                                  <c:otherwise>
                                     <grouper:abbreviateTextarea text="${guiConfigProperty.propertyValue}"  showCharCount="50" cols="20" rows="3"/>
                                  </c:otherwise>
                                </c:choose>
		                            </b>
		                            
		                            <c:if test="${guiConfigProperty.scriptlet}"><br />
		                              <span style="font-size: 90%">${textContainer.text['configurationElScriptletLabel']} ${grouper:escapeHtml(guiConfigProperty.scriptletForUi) }</span>
		                            </c:if><c:if test="${! grouper:isBlank(guiConfigProperty.unprocessedValueIfDifferent)}"><br />
		                              <span style="font-size: 90%">${textContainer.text['configurationUnprocessedValueIfDifferentLabel']} ${grouper:escapeHtml(guiConfigProperty.unprocessedValueIfDifferent) }</span>
		                            </c:if><c:if test="${! grouper:isBlank(guiConfigProperty.cronDescription)}"><br />
		                              <span style="font-size: 90%">${textContainer.text['configurationCronLabel']} ${grouper:escapeHtml(guiConfigProperty.cronDescription) }</span>
		                            </c:if><c:if test="${! grouper:isBlank(configItemMetadata.sampleValue)}"><br />
		                              <span style="font-size: 90%">${textContainer.text['configurationSampleValueLabel']} ${grouper:escapeHtml(configItemMetadata.sampleValue) }</span>
		                            </c:if><c:if test="${! grouper:isBlank(configItemMetadata.comment)}"><br />
		                              <span style="font-size: 90%">${grouper:escapeHtml(configItemMetadata.comment) }</span>
		                            </c:if></td>
		                          <td style="vertical-align: top; white-space: nowrap">
		                            ${guiConfigProperty.valueFromWhere}
		                            <c:if test="${! grouper:isBlank(guiConfigProperty.baseValueIfDifferent)}">
		                              <br />
		                              <span style="font-size: 90%">${textContainer.text['configurationBaseValueIfDifferent']} ${grouper:escapeHtml(guiConfigProperty.baseValueIfDifferent) }</span>
		                            </c:if>
		                          </td>
