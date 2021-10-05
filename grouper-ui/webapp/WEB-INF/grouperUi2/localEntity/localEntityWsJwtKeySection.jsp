<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  
                
         <div class="row-fluid">
         <div class="span9" style="margin-bottom: 20px;">
           <div class="lead">${textContainer.text['localEntityWsJwtKeyTitle'] }</div>
           <%-- <span>${textContainer.text['subjectAttributeAssignmentsDescription'] }</span> --%>
         </div>
         <div class="span3" id="localEntityWsJwtKeyMoreActionsButtonContentsDivId">
           <%@ include file="localEntityWsJwtKeyMoreActionsButtonContents.jsp"%>
         </div>
        </div>
        <div class="row-fluid">
         <div id="wsJwtKey-create-download-block-container" class="well hide">
          <%-- <form id="wsJwtKeyCreateDownloadForm" class="form-horizontal">
             <input type="hidden" name="subjectId" value="${grouperRequestContainer.subjectContainer.guiSubject.subject.id}" />
             <div class="control-group">
               <label class="control-label">${textContainer.text['subjectAssignAttributeAttributeDefNameLabel'] }</label>
               <div class="controls">
                 <grouper:combobox2 idBase="attributeDefNameCombo" style="width: 30em"
                   filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
                   value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.objectAttributeDefId)}"
                   />
                 <span class="help-block">${textContainer.text['subjectAssignAttributeAttributeDefNameDescription'] }</span>
               
               </div>
             </div>
             
             <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2SubjectAttributeAssignment.assignAttributeSubmit', {formIds: 'assignAttributeSubjectForm'}); return false;">${textContainer.text['subjectAssignAttributeButton'] }</a> 
             </div>
           </form> --%>
         
         </div>
         </div>
         <div id="viewWsJwtKey">
           
         </div>
                  
