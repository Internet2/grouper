<%@ include file="../common/commonTaglib.jsp"%>
<!-- externalSubjectSelfRegister/externalSubjectSelfRegister.jsp: main page -->

<grouper:title key="externalSubjectSelfRegister.registerTitle" />

<div style="margin-left: 20px; margin-top: 20px; width: 800px; text-align: right">
  <span class="requiredIndicator">*</span> indicates a required field
</div>
<div class="section" style="margin-top: 0px; width:800px">
  <grouper:subtitle key="externalSubjectSelfRegister.registerSectionHeader" />
  <div class="sectionBody">
    <%-- shows the group information --%>
    <table class="formTable " cellspacing="2" style="margin-bottom: 0">
      <tbody>
        <c:forEach items="${externalRegisterContainer.registerFields}" var="registerField">
      
          <tr class="formTableRow">
          <%-- c:if test="${!simpleMembershipUpdateContainer.showNameRowByDefault}" --%>
            <td class="formTableLeft">
            <grouper:message value="${registerField.label}"
        valueTooltip="${registerField.tooltip}" />
		         <c:if test="${registerField.required}">
		           <span class="requiredIndicator">*</span>
		         </c:if>
            </td>
            <td class="formTableRight">
              <c:choose>
                <c:when test="${registerField.readonly}">
                  <c:out value="${registerField.value}"></c:out>
                </c:when>
                <c:otherwise>
                  <input type="text"  />
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
        <tr>
          <td colspan="2" style="text-align: right">
            <div class="buttonRow">
					    <input class="redButton" type="submit" 
					      onclick="ajax('SimpleMembershipUpdate.deleteMultiple', {formIds: 'simpleMembershipUpdateDeleteMultipleForm'}); return false;" 
					      value="Delete record" 
					      onmouseover="Tip('${grouper:escapeJavascript(simpleMembershipUpdateContainer.text.deleteMultipleTooltip)}')" 
					      onmouseout="UnTip()" />
					    &nbsp;
					    <input class="blueButton" type="submit" 
					      onclick="ajax('SimpleMembershipUpdate.deleteAll'); return false;" 
					      value="Submit" 
					      onmouseover="Tip('${grouper:escapeJavascript(simpleMembershipUpdateContainer.text.deleteAllTooltip)}')" 
					      onmouseout="UnTip()" />    
					  </div>
				  </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>
