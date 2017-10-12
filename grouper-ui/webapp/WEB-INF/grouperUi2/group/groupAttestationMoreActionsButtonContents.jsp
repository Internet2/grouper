<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start group/groupAttestationMoreActionsButtonContents.jsp -->

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGroupAttestationActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#group-attestation-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#group-attestation-more-options li').first().focus();return true;});">
                          ${textContainer.text['groupAttestationViewMoreActionsButton'] } <span class="caret"></span></a>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="group-attestation-more-options">

                        <c:if test="${grouperRequestContainer.attestationContainer.canWriteAttestation}" >
	                        <li><a href="#" onclick="return guiV2link('operation=UiV2Attestation.editGroupAttestation&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
	                            >${textContainer.text['groupAttestationMoreActionsEditAttestation'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.attestationContainer.canWriteAttestation}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Attestation.clearGroupAttestation?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupAttestationMoreActionsClearAttestation'] }</a></li>
                        </c:if>
                        
                        <c:if test="${grouperRequestContainer.attestationContainer.canWriteAttestation}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Attestation.attestGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupAttestationMoreActionsAttestGroup'] }</a></li>
                        </c:if>
                        
                        <c:if test="${grouperRequestContainer.attestationContainer.canReadAncestorAttestation}" >
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Attestation.editStemAttestation&stemId=${grouperRequestContainer.attestationContainer.parentStemWithAttestation.id}'); return false;"
                              >${textContainer.text['groupAttestationMoreActionsViewFolderAttestation'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.attestationContainer.canRunDaemon}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Attestation.runDaemon'); return false;"
                              >${textContainer.text['groupAttestationMoreActionsRunDaemon'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.attestationContainer.canReadAttestation}" >
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Attestation.viewGroupAudits&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupAttestationViewAuditButton'] }</a></li>
                        </c:if>
                          

                      </ul>
                    </div>

                    <!-- end group/groupAttestationMoreActionsButtonContents.jsp -->
