<%@ include file="../assetsJsp/commonTaglib.jsp"%>

 <table class="table table-condensed table-striped">
    <tbody>
    
      <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupDirectMembersCount'] }</strong></td>
          <td>
             <p>${grouperRequestContainer.objectTypeContainer.userFriendlyStringForConfiguredAttributes}</p>
          </td>
      </tr>
      
      <%-- <c:if test="${not empty grouperRequestContainer.grouperLoaderContainer.loaderManagedGroup}">
      <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupDirectMembersCount'] }</strong></td>
          <td>
             <p>${grouperRequestContainer.objectTypeContainer.userFriendlyStringForConfiguredAttributes}</p>
          </td>
      </tr>
      </c:if> --%>
      
      

      <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupDirectMembersCount'] }</strong></td>
          <td>
            ${grouperRequestContainer.groupSummaryContainer.directMembersCount}
          </td>
      </tr>
      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupIndirectMembersCount'] }</strong></td>
        <td>
        ${grouperRequestContainer.groupSummaryContainer.indirectMembersCount}
          <br />
        </td>
      </tr>
      
      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupDirectGroupMembersCount'] }</strong></td>
        <td>
          ${grouperRequestContainer.groupSummaryContainer.directGroupMembersCount}
          <br />
        </td>
      </tr>
      
      <c:if test="${grouperRequestContainer.groupSummaryContainer.directGroupMembers != null &&
       !empty grouperRequestContainer.groupSummaryContainer.directGroupMembers}">
       
       <%-- add a table and indent it to the right a little bit --%>
      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupDirectGroupMembers'] }</strong></td>
        <td>
          <table class="table table-condensed table-striped"
            style="margin-left: 20px;">
            <tbody>
              <%-- for each direct group member, create a row --%>
              <c:forEach
                items="${grouperRequestContainer.groupSummaryContainer.directGroupMembers}"
                var="directGroupMember">
                <tr>
                  <td><a
                    href="?operation=UiV2Group.viewGroup&groupId=${directGroupMember.id}"
                    onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Group.viewGroup&groupId=${directGroupMember.id}', {dontScrollTop: true});">
                      ${directGroupMember.name} </a></td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </td>
         </tr>
        </c:if>
      
      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupAttestationEnabled'] }</strong></td>
        <td>
         <c:choose>
            <c:when test="${grouperRequestContainer.groupSummaryContainer.attestation}">
              ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
            </c:when>
            <c:otherwise>
              ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
            </c:otherwise>
           </c:choose>
          <br />
          <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
        </td>
      </tr>
      
      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupIsComposite'] }</strong></td>
        <td>
         <c:choose>
            <c:when test="${grouperRequestContainer.groupSummaryContainer.composite}">
              ${textContainer.text['provisioningConfigTableHeaderProvisionableYesLabel']}
            </c:when>
            <c:otherwise>
              ${textContainer.text['provisioningConfigTableHeaderProvisionableNoLabel']}
            </c:otherwise>
           </c:choose>
          <br />
          <%-- <span class="description">${textContainer.text['provisioningUsersInGroupCountHint']}</span> --%>
        </td>
      </tr>
      
      
      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupCompositeSize'] }</strong></td>
        <td>
        ${grouperRequestContainer.groupSummaryContainer.compositeSize}
          <br />
        </td>
      </tr>
      
      
      <c:if test="${grouperRequestContainer.groupSummaryContainer.composites != null &&
       !empty grouperRequestContainer.groupSummaryContainer.composites}">
       
       <%-- add a table and indent it to the right a little bit --%>
      <tr>
        <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupComposites'] }</strong></td>
        <td>
          <table class="table table-condensed table-striped"
            style="margin-left: 20px;">
            <tbody>
              <%-- for each composite, create a row --%>
              <c:forEach
                items="${grouperRequestContainer.groupSummaryContainer.composites}"
                var="composite">
                <tr>
                  <td><a
                    href="?operation=UiV2Group.viewGroup&groupId=${composite.factorOwnerUuid}"
                    onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Group.viewGroup&groupId=${composite.factorOwnerUuid}', {dontScrollTop: true});">
                      ${composite.ownerGroup.name} </a></td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </td>
         </tr>
        </c:if>
        
         <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong>${textContainer.text['groupProvisioningAssignmentCount'] }</strong></td>
            <td>
            ${grouperRequestContainer.groupSummaryContainer.provisioningAssignmentCount}
              <br />
            </td>
          </tr>
      
      
    </tbody>
  </table>
                
