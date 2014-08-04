<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.breadcrumbs}
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="span10">
                    <h4>${textContainer.text['attributeDefHeaderAttributeDefinition'] }</h4>
                    <h1><i class="fa fa-cog"></i> ${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.extension)}</h1>
                    <p>${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.description)}</p>
                    <div id="attributeDefDetailsId" style="display: none;">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelType']}</strong></td>
                            <td><grouper:message key="simpleAttributeUpdate.type.${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.attributeDefTypeDb}" /></td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelValueType']}</strong></td>
                            <td>
                              <c:choose>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'marker'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.marker'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'floating'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.floating'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'integer'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.integer'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'memberId'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.memberId'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'string'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.string'] }
                                </c:when>
                                <c:when test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.valueTypeDb == 'timestamp'}">
                                  ${textContainer.text['simpleAttributeUpdate.valueType.timestamp'] }
                                </c:when>
                              </c:choose>
                            
                            </td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelAssignTo']}</strong></td>
                            <td>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToAttributeDef }">
                                ${textContainer.text['attributeDefAssignTo.attributeDef'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToAttributeDefAssn }">
                                ${textContainer.text['attributeDefAssignTo.attributeDefAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToEffMembership }">
                                ${textContainer.text['attributeDefAssignTo.membership'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToEffMembershipAssn }">
                                ${textContainer.text['attributeDefAssignTo.membershipAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToGroup }">
                                ${textContainer.text['attributeDefAssignTo.group'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToGroupAssn }">
                                ${textContainer.text['attributeDefAssignTo.groupAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToImmMembership }">
                                ${textContainer.text['attributeDefAssignTo.immediateMembership'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToImmMembershipAssn }">
                                ${textContainer.text['attributeDefAssignTo.immediateMembershipAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToMember }">
                                ${textContainer.text['attributeDefAssignTo.member'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToMemberAssn }">
                                ${textContainer.text['attributeDefAssignTo.memberAssign'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToStem }">
                                ${textContainer.text['attributeDefAssignTo.stem'] } <br />
                              </c:if>
                              <c:if test="${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.assignToStemAssn }">
                                ${textContainer.text['attributeDefAssignTo.stemAssign'] } <br />
                              </c:if>
                            </td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefMultiAssignable']}</strong></td>
                            <td>${textContainer.text[grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.multiAssignable ? 'attributeDefMultiAssignableYes' : 'attributeDefMultiAssignableNo']}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefMultiValued']}</strong></td>
                            <td>${textContainer.text[grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.multiValued ? 'attributeDefMultiValuedYes' : 'attributeDefMultiValuedNo']}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelIdPath']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.name)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelId']}</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.extension)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelCreated'] }</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.createdString }</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelCreator'] }</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.creatorGuiSubject.shortLinkWithIcon}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelLastEdited']}</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.lastEditedString}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelLastEditedBy']}</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.lastUpdatedByGuiSubject.shortLinkWithIcon}</td>
                          </tr>
                          <c:if test="${grouperRequestContainer.attributeDefContainer.canAdmin }">
                            <tr>
                              <td><strong>${textContainer.text['groupLabelPrivilegesAssignedToEveryone']}</strong></td>
                              <td>
                                ${grouperRequestContainer.attributeDefContainer.guiAttributeDef.privilegeLabelsAllowedByGrouperAll }
                              
                              </td>
                            </tr>
                          </c:if>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelIdIndex']}</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.idIndex}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['attributeDefLabelUuid']}</strong></td>
                            <td>${grouperRequestContainer.attributeDefContainer.guiAttributeDef.attributeDef.uuid}</td>
                          </tr>

                        </tbody>
                      </table>
                    </div>
                    <p id="attributeDefDetailsMoreId"><a href="#" id="moreButtonId" onclick="$('#attributeDefDetailsId').show('slow'); $('#attributeDefDetailsMoreId').hide(); $('#attributeDefDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="fa fa-angle-down"></i></a></p>
                    <p id="attributeDefDetailsLessId" style="display: none"><a href="#" onclick="$('#attributeDefDetailsId').hide('slow'); $('#attributeDefDetailsLessId').hide(); $('#attributeDefDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="fa fa-angle-up"></i></a></p>
                  </div>
                  <div class="span2" id="attributeDefMoreActionsButtonContentsDivId">
                    <%@ include file="attributeDefMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
