<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                     
  <c:set  value="${grouperRequestContainer.externalSystemContainer.guiGrouperExternalSystem}" var="guiGrouperExternalSystem"/>
  
  <tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="externalSystemConfigId">${textContainer.text['grouperExternalSystemConfigIdLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <input type="text" style="width: 30em" value="${grouper:escapeHtml(guiGrouperExternalSystem.grouperExternalSystem.configId)}"
         name="externalSystemConfigId" id="externalSystemConfigId" />
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
        data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperExternalSystemConfigIdHint']}</span>
    </td>
  </tr>
  	
  	<tr>
    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="externalSystemTypeId">${textContainer.text['grouperExternalSystemTypeLabel']}</label></strong></td>
    <td style="vertical-align: top; white-space: nowrap;">&nbsp;</td>
    <td>
      <select name="externalSystemType" id="externalSystemTypeId" style="width: 30em"
      onchange="ajax('../app/UiV2ExternalSystem.addExternalSystem', {formIds: 'externalSystemConfigDetails'}); return false;"
      >
       
        <option value=""></option>
        <c:forEach items="${grouperRequestContainer.externalSystemContainer.allExternalSystemTypes}" var="externalSystem">
          <option value="${externalSystem['class'].name}"
              ${guiGrouperExternalSystem.grouperExternalSystem['class'].name == externalSystem['class'].name ? 'selected="selected"' : '' }
              >${externalSystem.title}</option>
        </c:forEach>
      </select>
      <span class="requiredField" rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
      data-original-title="${textContainer.textEscapeDouble['grouperRequiredTooltip']}">*</span>
      <br />
      <span class="description">${textContainer.text['grouperExternalSystemTypeHint']}</span>
    </td>
  </tr>
  
  <c:if test="${!grouper:isBlank(grouperRequestContainer.externalSystemContainer.html)}">
  	${grouperRequestContainer.externalSystemContainer.html}
  </c:if>