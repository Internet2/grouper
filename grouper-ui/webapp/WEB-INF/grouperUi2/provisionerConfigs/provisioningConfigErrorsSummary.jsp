 <%@ include file="../assetsJsp/commonTaglib.jsp"%>
    
    
    <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary != null}">
    
    <c:choose>
    <c:when test="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.errorsCount > 0}">
     
     <div class="row-fluid">
     <div class="span3">
     <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
      <tbody>
      
        <tr>
          <th colspan="2">
            <h4 style="margin-top: 1em">${textContainer.text['provisionerErrorSummaryLabel']}</h4>
          </th>
        </tr>
                            
        <tr>
          <td style="vertical-align: top; white-space: nowrap;"><strong><label style="font-weight: bold;">${textContainer.text['provisionerErrorSummaryTotalErrors']}</label></strong></td>
           <td>
               ${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.errorsCount}
            </td>
        </tr>
        
        <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.groupErrorsCount > 0}">
          <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label style="font-weight: bold;">${textContainer.text['provisionerErrorSummaryGroupErrors']}</label></strong></td>
             <td>
                 ${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.groupErrorsCount}
              </td>
          </tr>
          
          <tr>
            <td colspan="2">
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
            <td style="vertical-align: top; white-space: nowrap;"><strong><label style="font-weight: bold;">${textContainer.text['provisionerErrorSummaryEntityErrors']}</label></strong></td>
             <td>
                 ${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.entityErrorsCount}
              </td>
          </tr>
          
         <tr>
            <td colspan="2">
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
            <td style="vertical-align: top; white-space: nowrap;"><strong><label style="font-weight: bold;">${textContainer.text['provisionerErrorSummaryMembershipErrors']}</label></strong></td>
             <td>
                 ${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrorSummary.membershipErrorsCount}
              </td>
          </tr>
          
         <tr>
            <td colspan="2">
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
     </div>
     </div>
   </c:when>
   <c:otherwise>
    <div class="row-fluid">
      <div class="span9">
        <p><b>
        ${textContainer.text['provisionerConfigNoErrorsFound'] } 
        ${grouperRequestContainer.provisionerConfigurationContainer.guiProvisionerConfiguration.provisionerConfiguration.configId}
        </b></p>
      </div>
    </div>
  </c:otherwise>
  </c:choose>
    
    
    </c:if>