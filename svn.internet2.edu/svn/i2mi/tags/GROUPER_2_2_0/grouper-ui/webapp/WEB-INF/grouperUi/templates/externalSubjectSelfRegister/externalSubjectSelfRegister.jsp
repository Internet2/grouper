<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: externalSubjectSelfRegister/externalSubjectSelfRegister.jsp: main page -->

<grouper:title key="externalSubjectSelfRegister.registerTitle" />

<div style="margin-left: 20px; margin-top: 20px; width: 800px; text-align: right">
  <span class="requiredIndicator">*</span> indicates a required field
</div>
<div class="section" style="margin-top: 0px; width:800px">
  <c:choose>
    <c:when test="${externalRegisterContainer.insert}">
      <grouper:subtitle key="externalSubjectSelfRegister.registerSectionHeader" />
    </c:when>
    <c:otherwise>
      <grouper:subtitle key="externalSubjectSelfRegister.registerSectionHeaderEdit" />
    </c:otherwise>
  </c:choose>
  <div class="sectionBody">
    <form action="whatever" id="selfRegisterFormId" name="selfRegisterFormName">
	    <%-- shows the self register table --%>
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
	                  <input type="text" name="${registerField.paramName}" value="${registerField.value}" />
	                </c:otherwise>
	              </c:choose>
	            </td>
	          </tr>
	        </c:forEach>
	        <tr>
	          <td colspan="2" >
	            <div class="buttonRow" style="width: 600px; text-align: right">
	              <c:if test="${externalRegisterContainer.showDeleteButton}">
	                <input class="redButton" type="submit" 
	                  onclick="if(confirm('${grouper:escapeJavascript(navMap['inviteExternalSubjects.confirmDelete'])}')) {ajax('ExternalSubjectSelfRegister.delete', {formIds: 'selfRegisterFormId'});} return false;" 
	                  value="${navMap['externalSubjectSelfRegister.deleteRecordButtonText']}" 
	                  onmouseover="Tip('${grouper:escapeJavascript(navMap['externalSubjectSelfRegister.deleteRecordButtonTooltip'])}')" 
	                  onmouseout="UnTip()" />
	                &nbsp;
	              </c:if>
						    <input class="blueButton" type="submit" 
						      onclick="ajax('ExternalSubjectSelfRegister.submit', {formIds: 'selfRegisterFormId'}); return false;" 
						      value="${navMap['externalSubjectSelfRegister.submitButtonText']}" 
						      onmouseover="Tip('${grouper:escapeJavascript(navMap['externalSubjectSelfRegister.submitButtonTooltip'])}')" 
						      onmouseout="UnTip()" />    
						  </div>
					  </td>
	        </tr>
	      </tbody>
	    </table>
	  </form>
  </div>
</div>
<!-- End: externalSubjectSelfRegister/externalSubjectSelfRegister.jsp: main page -->
