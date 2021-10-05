<%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcommon.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcombo.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxcalendar.js"></script>

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/dhtmlxmenu.js"></script>
    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/menu/ext/dhtmlxmenu_ext.js"></script>
    <link rel="stylesheet" type="text/css" href="../../grouperExternal/public/assets/dhtmlx/menu/skins/dhtmlxmenu_dhx_blue.css" />

    <script type="text/javascript" src="../../grouperExternal/public/assets/dhtmlx/ext/dhtmlxcombo_extra.js"></script>

   <form class="form-inline form-small form-filter" id="editLocalEntityWsJwtKeyFormId">
       <input type="hidden" name="subjectId" value="${grouperRequestContainer.subjectContainer.guiSubject.subject.id}" />
       <input type="hidden" name="sourceId" value="${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" />
       <table class="table table-condensed table-striped">
         <tbody>
           
				  <tr>
				    <td style="vertical-align: top; white-space: nowrap;"><strong><label for="cidrId">${textContainer.text['localEntityWsJwtAllowedFromCidrsLabel']}</label></strong></td>
				    <td>
				      <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword.grouperPassword.allowedFromCidrs)}"
				         name="localEntityAllowedFromCidrs" id="cidrId" />
				      <br />
				      <span class="description">${textContainer.text['localEntityAllowedFromCidrsHint']}</span>
				    </td>
				  </tr>
				  
				  <tr>
            <td style="vertical-align: top; white-space: nowrap;"><strong><label for="expiratationDateId">${textContainer.text['localEntityWsJwtExpirationDateLabel']}</label></strong></td>
            <td>
              <input type="text" style="width: 30em" value="${grouper:escapeHtml(grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword.expiresAtString)}"
                 name="localEntityExpiresAtDate" id="expiratationDateId" />
              <br />
              <span class="description">${textContainer.text['localEntityExpiresAtDateHint']}</span>
            </td>
          </tr>
           
           <tr>
             <td>
               <input type="hidden" name="mode" value="edit">
             </td>
             <td
               style="white-space: nowrap; padding-top: 2em; padding-bottom: 2em;">
               <input type="submit" class="btn btn-primary"
               aria-controls="editLocalEntityWsJwtKeyFormId" id="submitId"
               value="${textContainer.text['localEntityViewWsJwtKeySaveButton'] }"
               onclick="ajax('../app/UiV2LocalEntity.editWsJwtKeySubmit', {formIds: 'editLocalEntityWsJwtKeyFormId'}); return false;">
               &nbsp; <a class="btn btn-cancel" role="button"
               onclick="return guiV2link('operation=UiV2LocalEntity.viewLocalEntityWSJwtKeys&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
               >${textContainer.text['localEntityViewWsJwtKeyCancelButton'] }</a>
             </td>
           </tr>

         </tbody>
       </table>
       
     </form>
