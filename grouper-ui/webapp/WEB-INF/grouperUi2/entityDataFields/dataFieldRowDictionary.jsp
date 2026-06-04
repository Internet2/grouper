<%@ include file="../assetsJsp/commonTaglib.jsp"%>
${grouper:title('miscAttestationDataFieldAndRowDictionaryLink')}

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li class="active">${textContainer.text['miscAttestationDataFieldAndRowDictionaryLink'] }</li>
  </ul>

  <div class="page-header blue-gradient">
    <div class="row-fluid">
      <div class="lead span8 pull-left">
        <h4>${textContainer.text['miscAttestationDataFieldAndRowDictionaryLink'] }</h4>
      </div>
    </div>
    <div class="row-fluid">
      <div class="span9"><p>${textContainer.text['dataFieldDictionaryPageDescription'] }</p></div>
    </div>
  </div>
</div>

<%-- ===== FILTER PANEL ===== --%>
<div class="row-fluid" style="margin-bottom: 15px;">
  <div class="span12">
    <form id="dictionaryFilterForm" class="form-inline" onsubmit="return dictionarySubmitFilter();" style="background: #f4f7fb; border: 1px solid #ddd; border-radius: 4px; padding: 12px 15px;">
      
      <%-- Top row: search text + submit + advanced + auto expand --%>
      <div class="row-fluid" style="margin-bottom: 8px;">
        <div class="span5" id="dictSearchTextTopDiv">
          <input type="text" id="dictSearchText" name="search" class="span12" 
            value="${grouper:escapeHtml(grouperRequestContainer.entityDataFieldsContainer.dictionaryFilterSearchText)}"
            placeholder="${textContainer.text['dataFieldDictionaryFilterSearchPlaceholder']}" />
        </div>
        <div class="span7">
          <button type="submit" class="btn btn-primary" id="dictSubmitTop">${textContainer.text['dataFieldDictionaryFilterSubmit']}</button>
          <button type="button" class="btn" id="dictAdvancedButton" onclick="dictionaryToggleAdvanced(); return false;">${textContainer.text['dataFieldDictionaryFilterAdvanced']}</button>
          <c:if test="${grouperRequestContainer.entityDataFieldsContainer.dictionaryHasResults}">
            <span id="dictExportButtons">
              <button type="button" class="btn" onclick="dictionaryExport('csv');">${textContainer.text['dataFieldDictionaryExportCsv']}</button>
              <button type="button" class="btn" onclick="dictionaryExport('json');">${textContainer.text['dataFieldDictionaryExportJson']}</button>
            </span>
          </c:if>
          <span id="dictAutoExpandSpan">
            <label class="checkbox" style="display: inline-block; margin-left: 15px;">
              <input type="checkbox" id="dictAutoExpandAll" name="autoExpandAll" value="true" ${grouperRequestContainer.entityDataFieldsContainer.dictionaryFilterAutoExpandAll ? 'checked' : ''} />
              ${textContainer.text['dataFieldDictionaryFilterAutoExpandAll']}
            </label>
          </span>
        </div>
      </div>
      
      <%-- Advanced panel: hidden by default, toggled by Advanced button --%>
      <c:set var="advancedOpen" value="${param.advancedOpen == 'true' || !empty grouperRequestContainer.entityDataFieldsContainer.dictionaryFilterDataRow || !empty grouperRequestContainer.entityDataFieldsContainer.dictionaryFilterPrivacyRealm}" />
      <div id="dictAdvancedPanel" style="${advancedOpen ? '' : 'display:none;'}">
        <table class="table table-condensed table-striped">
          <tbody>
            <tr>
              <td style="vertical-align: top; white-space: nowrap;">
                <strong><label for="dictSearchText" style="font-weight: bold;">${textContainer.text['dataFieldDictionaryFilterSearchText']}</label></strong>
              </td>
              <td id="dictSearchTextAdvancedCell">
                <br /><span class="description">${textContainer.text['dataFieldDictionaryFilterSearchDescription']}</span>
              </td>
            </tr>
            <tr>
              <td style="vertical-align: top; white-space: nowrap;">
                <strong><label for="dictDataRow" style="font-weight: bold;">${textContainer.text['dataFieldDictionaryFilterDataRow']}</label></strong>
              </td>
              <td>
                <select id="dictDataRow" name="dataRow" onchange="dictionaryOnDataRowChange();">
                  <option value="">${textContainer.text['dataFieldDictionaryFilterAny']}</option>
                  <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.dictionaryDataRowOptions}" var="opt">
                    <option value="${grouper:escapeHtml(opt[0])}" ${opt[0] == grouperRequestContainer.entityDataFieldsContainer.dictionaryFilterDataRow ? 'selected' : ''}>${grouper:escapeHtml(opt[1])}</option>
                  </c:forEach>
                </select>
                <br /><span class="description">${textContainer.text['dataFieldDictionaryFilterDataRowDescription']}</span>
              </td>
            </tr>
            <tr>
              <td style="vertical-align: top; white-space: nowrap;">
                <strong><label for="dictDataField" style="font-weight: bold;">${textContainer.text['dataFieldDictionaryFilterDataFieldInRow']}</label></strong>
              </td>
              <td>
                <select id="dictDataField" name="dataField">
                  <option value="">${textContainer.text['dataFieldDictionaryFilterAny']}</option>
                  <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.dictionaryDataFieldOptions}" var="opt">
                    <option value="${grouper:escapeHtml(opt[0])}" ${opt[0] == grouperRequestContainer.entityDataFieldsContainer.dictionaryFilterDataField ? 'selected' : ''}>${grouper:escapeHtml(opt[1])}</option>
                  </c:forEach>
                </select>
                <br /><span class="description">${textContainer.text['dataFieldDictionaryFilterDataFieldInRowDescription']}</span>
              </td>
            </tr>
            <tr>
              <td style="vertical-align: top; white-space: nowrap;">
                <strong><label for="dictPrivacyRealm" style="font-weight: bold;">${textContainer.text['dataFieldDictionaryFilterPrivacyRealm']}</label></strong>
              </td>
              <td>
                <select id="dictPrivacyRealm" name="privacyRealm">
                  <option value="">${textContainer.text['dataFieldDictionaryFilterAny']}</option>
                  <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.dictionaryPrivacyRealmOptions}" var="opt">
                    <option value="${grouper:escapeHtml(opt[0])}" ${opt[0] == grouperRequestContainer.entityDataFieldsContainer.dictionaryFilterPrivacyRealm ? 'selected' : ''}>${grouper:escapeHtml(opt[1])}</option>
                  </c:forEach>
                </select>
                <br /><span class="description">${textContainer.text['dataFieldDictionaryFilterPrivacyRealmDescription']}</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div id="dictAdvancedSubmitDiv">
          <button type="submit" class="btn btn-primary">${textContainer.text['dataFieldDictionaryFilterSubmit']}</button>
        </div>
      </div>
      
      <input type="hidden" name="submitted" value="true" />
      <input type="hidden" id="dictAdvancedOpenField" name="advancedOpen" value="${advancedOpen ? 'true' : 'false'}" />
    </form>
  </div>
</div>

<%-- ===== RESULTS ===== --%>
<div id="dictionaryResults" class="row-fluid">
  <c:if test="${grouperRequestContainer.entityDataFieldsContainer.dictionaryHasResults}">
     
     <c:set var="j" value="0" />
     <c:set var="autoExpand" value="${grouperRequestContainer.entityDataFieldsContainer.dictionaryFilterAutoExpandAll}" />
     <c:set var="isSysadmin" value="${grouperRequestContainer.entityDataFieldsContainer.canOperateOnEntityDataFieldConfigs}" />
     
     <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.guiDataFieldRowDictionaryTables}" var="guiDataFieldRowDictionaryTable">
      
      <div class="row-fluid" style="margin-top: 10px;">
         <div class="span12">
            <div class="control-group">
              <h4>${guiDataFieldRowDictionaryTable.title}
                <c:if test="${isSysadmin && !grouper:isBlank(guiDataFieldRowDictionaryTable.configId)}">
                  <a href="?operation=UiV2EntityDataFields.editDataRowConfig&dataRowConfigId=${grouper:escapeUrl(guiDataFieldRowDictionaryTable.configId)}" 
                     class="btn btn-mini" title="${textContainer.text['dataFieldDictionaryEditButton']}" style="margin-left: 5px;">
                    <i class="fa fa-pencil"></i>
                  </a>
                </c:if>
              </h4>
            </div>
            
            <c:if test="${!grouper:isBlank(guiDataFieldRowDictionaryTable.description)}">
              <div class="control-group"><span>${guiDataFieldRowDictionaryTable.description}</span></div>
            </c:if>
            <c:if test="${!grouper:isBlank(guiDataFieldRowDictionaryTable.dataOwner)}">
              <div class="control-group"><strong>${textContainer.text['entityDataFieldRowDictionaryHeaderDataOwner']}:</strong> ${guiDataFieldRowDictionaryTable.dataOwner}</div>
            </c:if>
            <c:if test="${!grouper:isBlank(guiDataFieldRowDictionaryTable.howToGetAccess)}">
              <div class="control-group"><strong>${textContainer.text['entityDataFieldRowDictionaryHeaderHowToGetAccess']}:</strong> ${guiDataFieldRowDictionaryTable.howToGetAccess}</div>
            </c:if>
            
            <c:if test="${!grouper:isBlank(guiDataFieldRowDictionaryTable.documentation)}">
              <div class="control-group">
                <a href="#" onclick="$('#documentation-${j}').toggle('slow'); return false;">${textContainer.text['entityDataFieldRowDictionaryTableDocumentation']}</a>
                <span id="documentation-${j}" style="display: none;">${guiDataFieldRowDictionaryTable.documentation}</span>
              </div>
            </c:if>
         </div>
      </div>
      
      <c:choose>
        <c:when test="${guiDataFieldRowDictionaryTable.canAccess == false}">
          <p>${textContainer.text['entityDataFieldRowDictionaryTableNoViewAccess']}</p>
        </c:when>

        <c:when test="${guiDataFieldRowDictionaryTable.guiDataFieldRowDictionary.size() == 0}">
          <p>${textContainer.text['entityDataFieldRowDictionaryTableNoData']}</p>
        </c:when>

        <c:otherwise>
        
      <table class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th style="width: 25%;">${textContainer.text['entityDataFieldRowDictionaryHeaderDataFieldAliases']}</th> 
            <th style="width: 40%;">${textContainer.text['entityDataFieldRowDictionaryHeaderDescription']}</th>
            <th style="width: 35%;">${textContainer.text['dataFieldDictionaryHeaderMoreInfo']}</th>
          </tr>
        </thead>
        <tbody>
         <c:set var="i" value="0" />
         <c:forEach items="${guiDataFieldRowDictionaryTable.guiDataFieldRowDictionary}" var="guiDataFieldRowDictionary">
              
            <tr>
              <%-- Column 1: Aliases + sysadmin edit + permalink --%>
              <td>
                <strong>${guiDataFieldRowDictionary.dataFieldAliases}</strong>
                <c:if test="${isSysadmin}">
                  <a href="?operation=UiV2EntityDataFields.editDataFieldConfig&dataFieldConfigId=${grouper:escapeUrl(guiDataFieldRowDictionary.dataFieldConfigId)}" 
                     class="btn btn-mini" title="${textContainer.text['dataFieldDictionaryEditButton']}" style="margin-left: 3px;">
                    <i class="fa fa-pencil"></i>
                  </a>
                </c:if>
                <a href="#" onclick="dictionaryCopyPermalink('${grouper:escapeJavascript(guiDataFieldRowDictionary.dataFieldConfigId)}', '${grouper:escapeJavascript(guiDataFieldRowDictionary.dataRowConfigId)}'); return false;"
                   title="${textContainer.text['dataFieldDictionaryPermalinkTitle']}" style="margin-left: 3px;">
                  <i class="fa fa-link"></i>
                </a>
              </td>
              
              <%-- Column 2: Description (truncated unless auto-expand) --%>
              <td>
                <c:choose>
                  <c:when test="${autoExpand}">
                    ${guiDataFieldRowDictionary.description}
                  </c:when>
                  <c:otherwise>
                    <div class="dict-desc-truncated" id="desc-${j}-${i}" style="max-height: 7.5em; overflow: hidden; position: relative;">
                      ${guiDataFieldRowDictionary.description}
                    </div>
                    <a href="#" class="dict-desc-toggle" data-target="desc-${j}-${i}" onclick="dictionaryToggleDesc(this); return false;" style="display:none;">${textContainer.text['dataFieldDictionaryMoreLink']}</a>
                  </c:otherwise>
                </c:choose>
              </td>
              
              <%-- Column 3: More information (collapsed) --%>
              <td style="font-size: 0.9em;">
                <%-- privilege with humanized text --%>
                <div>
                  ${textContainer.text['dataFieldDictionaryYourPrivilege']}: <strong>${guiDataFieldRowDictionary.privilege}</strong> &mdash; ${guiDataFieldRowDictionary.privilegeHumanized}
                  <c:if test="${!grouper:isBlank(guiDataFieldRowDictionary.privacyRealmConfigId)}">
                    (${textContainer.text['dataFieldDictionaryPrivacyRealmLabel']}: <strong>${guiDataFieldRowDictionary.privacyRealmConfigId}</strong>)
                  </c:if>
                </div>
                
                <%-- data type --%>
                <div>${textContainer.text['entityDataFieldRowDictionaryHeaderDataType']}: <c:choose><c:when test="${guiDataFieldRowDictionary.dataType == 'bool'}">${textContainer.text['dataFieldDictionaryDataTypeBool']}</c:when><c:when test="${guiDataFieldRowDictionary.dataType == 'integer'}">${textContainer.text['dataFieldDictionaryDataTypeInteger']}</c:when><c:when test="${guiDataFieldRowDictionary.dataType == 'timestamp'}">${textContainer.text['dataFieldDictionaryDataTypeTimestamp']}</c:when><c:otherwise>${textContainer.text['dataFieldDictionaryDataTypeString']}</c:otherwise></c:choose><c:if test="${guiDataFieldRowDictionary.multiValued}"> (${textContainer.text['dataFieldDictionaryMultiValued']})</c:if>
                </div>
                
                <%-- data owner (only if field overrides row's owner) --%>
                <c:if test="${!grouper:isBlank(guiDataFieldRowDictionary.dataOwner) && guiDataFieldRowDictionary.dataOwner != guiDataFieldRowDictionary.dataRowDataOwner}">
                  <div>${textContainer.text['entityDataFieldRowDictionaryHeaderDataOwner']}: ${guiDataFieldRowDictionary.dataOwner}</div>
                </c:if>
                
                <%-- how to get access (only if field overrides row's access info) --%>
                <c:if test="${!grouper:isBlank(guiDataFieldRowDictionary.howToGetAccess) && guiDataFieldRowDictionary.howToGetAccess != guiDataFieldRowDictionary.dataRowHowToGetAccess}">
                  <div>${textContainer.text['entityDataFieldRowDictionaryHeaderHowToGetAccess']}: ${guiDataFieldRowDictionary.howToGetAccess}</div>
                </c:if>
                
                <%-- examples toggle --%>
                <c:if test="${!grouper:isBlank(guiDataFieldRowDictionary.examples)}">
                  <div>
                    ${textContainer.text['entityDataFieldRowDictionaryHeaderExamples']}: 
                    <c:choose>
                      <c:when test="${autoExpand}">
                        <div style="margin-top: 4px;">${guiDataFieldRowDictionary.examples}</div>
                      </c:when>
                      <c:otherwise>
                        <a href="#" onclick="$('#examples-${j}-${i}').toggle('slow'); $(this).text($(this).text() === '${textContainer.text["dataFieldDictionaryShowLink"]}' ? '${textContainer.text["dataFieldDictionaryHideLink"]}' : '${textContainer.text["dataFieldDictionaryShowLink"]}'); return false;">${textContainer.text['dataFieldDictionaryShowLink']}</a>
                        <div id="examples-${j}-${i}" style="display: none; margin-top: 4px;">${guiDataFieldRowDictionary.examples}</div>
                      </c:otherwise>
                    </c:choose>
                  </div>
                </c:if>
                
                <%-- JEXL snippet with copy button --%>
                <c:if test="${!grouper:isBlank(guiDataFieldRowDictionary.jexlSnippet)}">
                  <div style="margin-top: 6px; background: #f9f9f9; border: 1px solid #eee; border-radius: 3px; padding: 4px 8px; font-family: monospace; font-size: 0.85em;">
                    <code id="jexl-${j}-${i}">${grouper:escapeHtml(guiDataFieldRowDictionary.jexlSnippet)}</code>
                    <button type="button" class="btn btn-mini" onclick="dictionaryCopyText('jexl-${j}-${i}');" title="${textContainer.text['dataFieldDictionaryCopyButton']}" style="margin-left: 6px;">
                      <i class="fa fa-copy"></i>
                    </button>
                  </div>
                </c:if>
              </td>
            </tr>
            <c:set var="i" value="${i+1}" />
         </c:forEach>
              
        </tbody>
       </table>
        </c:otherwise>
      </c:choose>
      
      <hr style="border: 1px solid #ccc;">
      <c:set var="j" value="${j+1}" />
     </c:forEach>

     <c:if test="${grouperRequestContainer.entityDataFieldsContainer.guiDataFieldRowDictionaryTables.size() == 0}">
       <p>${textContainer.text['dataFieldDictionaryNoResults']}</p>
     </c:if>

  </c:if>
</div>

<%-- ===== JAVASCRIPT ===== --%>
<script>
function dictionaryToggleAdvanced() {
  // opening advanced: move search text into the table form, export buttons + auto expand to bottom, hide Advanced button
  $('#dictSearchText').prependTo('#dictSearchTextAdvancedCell');
  $('#dictSearchText').removeClass('span12');
  $('#dictExportButtons').appendTo('#dictAdvancedSubmitDiv');
  $('#dictAutoExpandSpan').appendTo('#dictAdvancedSubmitDiv');
  $('#dictSubmitTop').hide();
  $('#dictAdvancedButton').hide();
  $('#dictAdvancedOpenField').val('true');
  $('#dictAdvancedPanel').show('fast');
}

function dictionaryOnDataRowChange() {
  // re-fetch field options: the selected row's fields if a row is chosen,
  // otherwise all data fields (server decides based on the dataRow param)
  ajax('../app/UiV2EntityDataFields.dataFieldDictionaryFieldsForRow', {formIds: 'dictionaryFilterForm'});
}

function dictionarySubmitFilter() {
  var params = 'operation=UiV2EntityDataFields.viewDataFieldAndRowDictionary&submitted=true';
  if ($('#dictAdvancedOpenField').val() === 'true') params += '&advancedOpen=true';
  if ($('#dictAutoExpandAll').is(':checked')) params += '&autoExpandAll=true';
  var row = $('#dictDataRow').val();
  if (row) params += '&dataRow=' + encodeURIComponent(row);
  var field = $('#dictDataField').val();
  if (field) params += '&dataField=' + encodeURIComponent(field);
  var realm = $('#dictPrivacyRealm').val();
  if (realm) params += '&privacyRealm=' + encodeURIComponent(realm);
  var search = $('#dictSearchText').val();
  if (search) params += '&search=' + encodeURIComponent(search);
  
  return guiV2link(params);
}

function dictionaryExport(format) {
  var url = '../app/UiV2EntityDataFields.dataFieldDictionaryExport?format=' + format;
  var row = $('#dictDataRow').val();
  if (row) url += '&dataRow=' + encodeURIComponent(row);
  var field = $('#dictDataField').val();
  if (field) url += '&dataField=' + encodeURIComponent(field);
  var realm = $('#dictPrivacyRealm').val();
  if (realm) url += '&privacyRealm=' + encodeURIComponent(realm);
  var search = $('#dictSearchText').val();
  if (search) url += '&search=' + encodeURIComponent(search);
  
  $.blockUI();
  fetch(url, { method: 'GET', credentials: 'same-origin' }).then(function(response) {
    var contentType = response.headers.get('Content-Type') || '';
    if (contentType.indexOf('application/octet-stream') !== -1 || contentType.indexOf('text/csv') !== -1 || contentType.indexOf('application/json') !== -1) {
      $.unblockUI();
      window.location.href = url;
    } else {
      return response.json().then(function(json) {
        guiProcessJsonResponse(json);
        $.unblockUI();
      });
    }
  }).catch(function() { $.unblockUI(); });
}

function dictionaryCopyText(elementId) {
  var text = document.getElementById(elementId).textContent;
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text);
  } else {
    var ta = document.createElement('textarea');
    ta.value = text;
    document.body.appendChild(ta);
    ta.select();
    document.execCommand('copy');
    document.body.removeChild(ta);
  }
}

function dictionaryCopyPermalink(fieldConfigId, rowConfigId) {
  var base = location.href.split('?')[0];
  var params = '?operation=UiV2EntityDataFields.viewDataFieldAndRowDictionary&submitted=true';
  if (rowConfigId) params += '&dataRow=' + encodeURIComponent(rowConfigId);
  params += '&dataField=' + encodeURIComponent(fieldConfigId);
  var url = base + params;
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url);
  } else {
    var ta = document.createElement('textarea');
    ta.value = url;
    document.body.appendChild(ta);
    ta.select();
    document.execCommand('copy');
    document.body.removeChild(ta);
  }
}

function dictionaryToggleDesc(link) {
  var targetId = $(link).data('target');
  var $div = $('#' + targetId);
  if ($div.css('max-height') !== 'none') {
    $div.css({'max-height': 'none', 'overflow': 'visible'});
    $(link).text('${textContainer.text["dataFieldDictionaryLessLink"]}');
  } else {
    $div.css({'max-height': '7.5em', 'overflow': 'hidden'});
    $(link).text('${textContainer.text["dataFieldDictionaryMoreLink"]}');
  }
}

$(function() {
  // if the advanced panel is open on page load, move elements into the form
  if ($('#dictAdvancedPanel').is(':visible')) {
    $('#dictSearchText').prependTo('#dictSearchTextAdvancedCell');
    $('#dictSearchText').removeClass('span12');
    $('#dictExportButtons').appendTo('#dictAdvancedSubmitDiv');
    $('#dictAutoExpandSpan').appendTo('#dictAdvancedSubmitDiv');
    $('#dictSubmitTop').hide();
    $('#dictAdvancedButton').hide();
  }
  // show "more" link only when description is actually truncated
  $('.dict-desc-truncated').each(function() {
    if (this.scrollHeight > this.clientHeight + 2) {
      $(this).next('.dict-desc-toggle').show();
    }
  });
  // after AJAX submit, scroll to the filter form so the page doesn't jump to the top
  if ($('#dictionaryResults').children().length > 0) {
    var $form = $('#dictionaryFilterForm');
    if ($form.length) {
      $('html, body').scrollTop($form.offset().top - 10);
    }
  }
});
</script>
