 <%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary != null && grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.errorsCount > 0}">
     <table class="table table-condensed table-striped">
      <tbody>
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['provisionerErrorSummaryTotalErrors']}</label></strong></td>
           <td>
               ${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.errorsCount}
            </td>
        </tr>
        
        <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.groupErrorsCount > 0}">
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['provisionerErrorSummaryGroupErrors']}</label></strong></td>
             <td>
                 ${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.groupErrorsCount}
              </td>
          </tr>
          
          <tr>
            <td>
              <table>
                <c:forEach var="typeCount" items="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.groupErrorTypeCount}">
                  <tr>
                    <td style="vertical-align: top; white-space: nowrap;"><strong><label>${typeCount.key}</label></strong></td>
                    <td>${typeCount.value}</td>
                  </tr>
                </c:forEach>
              </table>
            </td>
          
          </tr>
          
        </c:if>
        <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.entityErrorsCount > 0}">
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['provisionerErrorSummaryEntityErrors']}</label></strong></td>
             <td>
                 ${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.entityErrorsCount}
              </td>
          </tr>
          
          <tr>
            <td>
              <table>
                <c:forEach var="typeCount" items="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.entityErrorTypeCount}">
                  <tr>
                    <td style="vertical-align: top; white-space: nowrap;"><strong><label>${typeCount.key}</label></strong></td>
                    <td>${typeCount.value}</td>
                  </tr>
                </c:forEach>
              </table>
            </td>
          
          </tr>
          
        </c:if>
        <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.membershipErrorsCount > 0}">
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label>${textContainer.text['provisionerErrorSummaryMembershipErrors']}</label></strong></td>
             <td>
                 ${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.membershipErrorsCount}
              </td>
          </tr>
          
          <tr>
            <td>
              <table>
                <c:forEach var="typeCount" items="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.membershipsErrorTypeCount}">
                  <tr>
                    <td style="vertical-align: top; white-space: nowrap;"><strong><label>${typeCount.key}</label></strong></td>
                    <td>${typeCount.value}</td>
                  </tr>
                </c:forEach>
              </table>
            </td>
          
          </tr>
          
        </c:if>
        
       </tbody>
     </table>
   </c:if>