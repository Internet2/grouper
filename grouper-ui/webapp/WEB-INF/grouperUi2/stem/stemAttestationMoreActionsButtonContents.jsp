<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start stem/stemAttestationMoreActionsButtonContents.jsp -->

                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreStemAttestationActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#group-attestation-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#group-attestation-more-options li').first().focus();return true;});">
                          ${textContainer.text['stemAttestationViewMoreActionsButton'] } <span class="caret"></span></a>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="group-attestation-more-options">

                        <c:if test="${grouperRequestContainer.attestationContainer.canWriteAttestation}" >
	                        <li><a href="#" onclick="return guiV2link('operation=UiV2Attestation.editStemAttestation&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
	                            >${textContainer.text['stemAttestationMoreActionsEditAttestation'] }</a></li>
                        </c:if>

                      </ul>
                    </div>

                    <!-- end stem/stemAttestationMoreActionsButtonContents.jsp -->
