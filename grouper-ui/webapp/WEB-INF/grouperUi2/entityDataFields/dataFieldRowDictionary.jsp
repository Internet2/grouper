<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li class="active">${textContainer.text['miscAttestationDataFieldAndRowDictionaryLink'] }</li>
  </ul>

  <div class="page-header blue-gradient">

    <div class="row-fluid">
      <div class="lead span8 pull-left">
        <h4>${textContainer.text['miscAttestationDataFieldAndRowDictionaryLink'] }</h4>
      </div>
    </div>
  </div>
</div>

<div class="row-fluid">
      
     <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.guiDataFieldRowDictionaryTables}" var="guiDataFieldRowDictionaryTable">
      
      <c:if test="${!grouper:isBlank(guiDataFieldRowDictionaryTable.dataRowAlias)}">
        
         <div class="row-fluid">
            <div class="span12">
                <div class="control-group">
                  <label class="control-label" style="padding-top: 0px; display: inline;">${textContainer.text['entityDataFieldRowDictionaryHeaderDataFieldAliases'] }: </label>
                  <span>${guiDataFieldRowDictionaryTable.dataRowAlias}</span>
                </div>
                
                <div class="control-group">
                  <label class="control-label" style="padding-top: 0px; display: inline;">${textContainer.text['entityDataFieldRowDictionaryHeaderDescription'] }: </label>
                  <span>${guiDataFieldRowDictionaryTable.description}</span>
                </div>
                
                <div class="control-group">
                  <label class="control-label" style="padding-top: 0px; display: inline;">${textContainer.text['entityDataFieldRowDictionaryHeaderDataOwner'] }: </label>
                  <span>${guiDataFieldRowDictionaryTable.dataOwner}</span>
                </div>
                
                <div class="control-group">
                  <label class="control-label" style="padding-top: 0px; display: inline;">${textContainer.text['entityDataFieldRowDictionaryHeaderHowToGetAccess']}: </label>
                  <span>${guiDataFieldRowDictionaryTable.howToGetAccess}</span>
                </div>
                
           </div>
         </div>
        
      </c:if>
      
      <table
        class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['entityDataFieldRowDictionaryHeaderDataFieldAliases']}</th> 
            <th>${textContainer.text['entityDataFieldRowDictionaryHeaderDescription']}</th>
            <th>${textContainer.text['entityDataFieldRowDictionaryHeaderPrivilege']}</th>
            <th>${textContainer.text['entityDataFieldRowDictionaryHeaderDataType']}</th>
            <th>${textContainer.text['entityDataFieldRowDictionaryHeaderDataOwner']}</th>
            <th>${textContainer.text['entityDataFieldRowDictionaryHeaderHowToGetAccess']}</th>
            <th>${textContainer.text['entityDataFieldRowDictionaryHeaderExamples']}</th>
          </tr>
        </thead>
        <tbody>
         <c:set var="i" value="0" />
         <c:forEach items="${guiDataFieldRowDictionaryTable.guiDataFieldRowDictionary}" var="guiDataFieldRowDictionary">
              
            <tr>
              <td style="white-space: nowrap;">
                ${guiDataFieldRowDictionary.dataFieldAliases}
              </td>
              
              <td style="white-space: nowrap;">
                ${guiDataFieldRowDictionary.description}
              </td>
              
              <td style="white-space: nowrap;">
                ${guiDataFieldRowDictionary.privilege}
              </td>
              
              <td style="white-space: nowrap;">
                ${guiDataFieldRowDictionary.dataType}
              </td>
              
              <td style="white-space: nowrap;">
                ${guiDataFieldRowDictionary.dataOwner}
              </td>
              
              <td style="white-space: nowrap;">
                ${guiDataFieldRowDictionary.howToGetAccess}
              </td>
              
              <td style="white-space: nowrap;">
                ${guiDataFieldRowDictionary.examples}
              </td>
              </tr>
              
         </c:forEach>
              
        </tbody>
      </table>

     </c:forEach>
</div>
