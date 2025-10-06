<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('groupPageTitle', grouperRequestContainer.groupContainer.guiGroup.group.displayName)}
            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />
            <grouper:browserPage jspName="viewGroup" />
            <%@ include file="groupHeader.jsp" %>

            <script language="javascript">
              $("#people-filter").on('change', function() {
                if (this.value == 'IMMEDIATE') {
                  $("#pit-filter").prop("selectedIndex", 0);
                } else if (this.value == 'NONIMMEDIATE') {
                  $("#enabled-disabled-filter").prop("selectedIndex", 0);
                  $("#pit-filter").prop("selectedIndex", 0);
                } else {
                  $("#enabled-disabled-filter").prop("selectedIndex", 0);
                }

                $("#pit-filter").trigger('change');
              });

              $("#enabled-disabled-filter").on('change', function() {
                if (this.value != 'nothing') {
                  $("#people-filter").prop("selectedIndex", 1);
                  $("#pit-filter").prop("selectedIndex", 0);
                }

                $("#pit-filter").trigger('change');
              });

              $("#custom-composite-filter").on('change', function() {
                if (this.value != 'nothing') {
                  $("#pit-filter").prop("selectedIndex", 0);
                }

                $("#pit-filter").trigger('change');
              });

              $("#pit-filter").on('change', function() {
                if (this.value == 'yes') {
                  $("#people-filter").prop("selectedIndex", 0);
                  $("#enabled-disabled-filter").prop("selectedIndex", 0);
                  $("#custom-composite-filter").prop("selectedIndex", 0);
                  $('.groupMembersAdvancedPITShow').show('slow');
                } else {
                  $('.groupMembersAdvancedPITShow').hide('slow');
                }
              });

              $("#advanced-button").on('click', function(e) {
                e.preventDefault();
                $('#groupFilterSubmitDiv').appendTo('#groupFilterAdvancedButtonRowDiv');
                $('#peopleFilterLabel').appendTo('#peopleFilterLabelCell');
                $('#people-filter').prependTo('#peopleFilterSelectCell');
                $('#table-filter').prependTo('#peopleFilterMemberNameCell');
                $('#table-filter').removeClass("span12");
                $('.groupMembersAdvancedShow').show('slow');
                $('.groupMembersAdvancedHide').hide('slow');
              });
            </script>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>
				
        				<div class="tab-interface">
                  <c:set var="grouperCurrentTab" value="summary" />
                  <%@ include file="../group/groupTabs.jsp" %>
        				</div>
                

                <c:if test="${!grouperRequestContainer.groupContainer.guiGroup.group.isEnabled()}">
                  <p class="lead" style="color: red">${textContainer.text['groupViewGroupDisabled'] }
                </c:if>
                
                <c:choose>
                  <c:when test="${grouperRequestContainer.groupContainer.canRead}">

                    <c:if test="${grouperRequestContainer.attestationContainer.hasAttestationConfigured && grouperRequestContainer.attestationContainer.canWriteAttestation}" >
                      <c:choose>
                        <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.needsRecertify}">
                          <p class="lead" style="color: red">${textContainer.text['attestationGroupNeedsAttestationNow'] }

                            <input type="submit" class="btn" value="${textContainer.textEscapeDouble['groupAttestationMarkAsReviewed'] }"
                              onclick="ajax('../app/UiV2Attestation.attestGroupFromOutside?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;" /> 
                            
                          </p>
                          <br />
                        </c:when>
                        <c:when test="${grouperRequestContainer.attestationContainer.guiAttestation.needsRecertifySoon}">
                          <p class="lead" style="color: red">${textContainer.text['attestationGroupNeedsAttestationSoon'] }

                            <input type="submit" class="btn" value="${textContainer.textEscapeDouble['groupAttestationMarkAsReviewed'] }"
                              onclick="ajax('../app/UiV2Attestation.attestGroupFromOutside?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;" /> 

                          </p>
                          <br />
                        </c:when>
                      </c:choose>
                    </c:if>
                    <c:if test="${mediaMap['uiV2.group.show.compositeAndFactors']=='true' && grouperRequestContainer.groupContainer.guiGroup.group.hasComposite}" >

                      <p class="compositeInfo">${textContainer.text['groupLabelCompositeOwnerMainPanel'] }
                      ${grouperRequestContainer.groupContainer.guiGroup.compositeOwnerText}</p>
                      
                    </c:if>
                    
                  
                  </c:when>
                
                </c:choose>
                
                <script>
                  //set this flag so we get one confirm message on this screen
                  confirmedChanges = false;
                </script>
                <div id="groupFilterResultsId" role="region" aria-live="polite">
                </div>                

              </div>
            </div>
            <!-- end group/viewGroup.jsp -->
