<!-- Start: externalSubjectSelfRegister/externalSubjectSelfRegister.jsp: main page -->

<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="bread-header-container">

  <div class="page-header blue-gradient">
    <h4>${textContainer.text['externalSubjectSelfRegister.registerTitle'] }</h4>
  </div>

 </div>
   
<div style="margin-top: 20px; width: 800px; text-align: right">
  <span class="requiredIndicator">*</span> ${textContainer.text['externalSubjectSelfRegister.indicatesRequiredField'] }
</div>
   
<div class="row-fluid">
  <div class="span12">
    <form id="selfRegisterFormId" class="form-horizontal">
    
 	  <c:forEach items="${externalRegisterContainer.registerFields}" var="registerField">
     		
        <div class="control-group">
 		  <label class="control-label">${registerField.label}
 		    <c:if test="${registerField.required}">
			  <span class="requiredIndicator">*</span>
            </c:if>
 		  </label>
         			
          <div class="controls">
            <c:choose>
          	  <c:when test="${registerField.readonly}">
                <span><c:out value="${registerField.value}"></c:out></span>
              </c:when>
          	  <c:otherwise>
                <input type="text" name="${registerField.paramName}" value="${registerField.value}" />
            	<span class="help-block">${registerField.tooltip }</span>
          	  </c:otherwise>
            </c:choose>
       
          </div>
        </div>
      </c:forEach>
   

   <div class="form-actions">
   
     <c:if test="${externalRegisterContainer.showDeleteButton}">
       <a href="#" 
	     onclick="if(confirm('${grouper:escapeJavascript(navMap['inviteExternalSubjects.confirmDelete'])}')) {ajax('../app/ExternalSubjectSelfRegister.delete', {formIds: 'selfRegisterFormId'});} return false;" 
	     onmouseover="Tip('${grouper:escapeJavascript(navMap['externalSubjectSelfRegister.deleteRecordButtonTooltip'])}')"
	     onmouseout="UnTip()"
	     class="btn btn-cancel redButton">${navMap['externalSubjectSelfRegister.deleteRecordButtonText']}</a>        
     </c:if>
   
     <a href="#" class="btn btn-primary"
  	   onclick="ajax('../app/ExternalSubjectSelfRegister.submit', {formIds: 'selfRegisterFormId'}); return false;"
  	   onmouseout="UnTip()"
  	   onmouseover="Tip('${grouper:escapeJavascript(navMap['externalSubjectSelfRegister.submitButtonTooltip'])}')">
  	     ${navMap['externalSubjectSelfRegister.submitButtonText']}</a> 
   </div> 
 </form></div>
</div>

<!-- End: externalSubjectSelfRegister/externalSubjectSelfRegister.jsp: main page -->