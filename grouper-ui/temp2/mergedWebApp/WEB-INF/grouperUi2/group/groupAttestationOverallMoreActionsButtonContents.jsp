<%@ include file="../assetsJsp/commonTaglib.jsp"%>


                    <div class="btn-group btn-block">
                    
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreOverallAttestationActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#overall-attestation-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#overall-attestation-more-options li').first().focus();return true;});">
                          ${textContainer.text['stemAttestationViewMoreActionsButton'] } <span class="caret"></span></a>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="overall-attestation-more-options">

                        <li><a href="#" onclick="return guiV2link('operation=UiV2Attestation.attestationOverall'); return false;"
                            >${textContainer.text['miscellaneousAttestationOverallDecription'] }</a></li>

                        <li><a href="#" onclick="return guiV2link('operation=UiV2Attestation.allSettings'); return false;"
                            >${textContainer.text['groupAttestationOverallMoreActionsAllSettings'] }</a></li>

                        <c:if test="${grouperRequestContainer.attestationContainer.canRunDaemon}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Attestation.runDaemon'); return false;"
                              >${textContainer.text['groupAttestationMoreActionsRunDaemon'] }</a></li>
                        </c:if>


                      </ul>
                    </div>
