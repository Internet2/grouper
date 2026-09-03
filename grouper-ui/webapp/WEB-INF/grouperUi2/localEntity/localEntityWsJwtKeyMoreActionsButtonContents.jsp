<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- <div class="btn-group btn-block">
  <button type="button" id="show-add-block" onclick="showHideSubjectAssignAttributeBlock()" 
    class="btn btn-medium btn-primary btn-block">
      <i aria-hidden="true" class="fa fa-plus"></i> ${textContainer.text['localEntityCreateWsJwtKeyButton'] }
  </button>               
</div> --%>

        <div class="btn-group btn-block">
        
          <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreLocalEntityWsJwtKeyActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
            aria-haspopup="true" aria-expanded="false" onclick="$('#wsJwtKey-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#wsJwtKey-more-options li').first().focus();return true;});">
              ${textContainer.text['localEntityWsJwtKeyMoreActionsButton'] } <span class="caret"></span></button>

          <ul class="dropdown-menu dropdown-menu-right" id="wsJwtKey-more-options">

            <c:if test="${grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword == null}" >
              <li><a href="?operation=UiV2LocalEntity.createNewWsJwtKey&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2LocalEntity.createNewWsJwtKey&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
                  >${textContainer.text['localEntityCreateWsJwtKeyButton'] }</a></li>
            </c:if>

            <c:if test="${grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword != null}" >
              <li><a href="?operation=UiV2LocalEntity.editWsJwtKey&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&grouperPasswordId=${grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword.grouperPassword.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2LocalEntity.editWsJwtKey&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&grouperPasswordId=${grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword.grouperPassword.id}'); return false;"
                  >${textContainer.text['localEntityWsJwtKeyMoreActionsEditSettings'] }</a></li>
             
             <li>&nbsp;</li>     
             
             
             
             
             
             <li>
             
             <%-- <a href="?operation=UiV2LocalEntity.deleteWsJwtKey&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2LocalEntity.deleteWsJwtKey&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
             >${textContainer.text['localEntityWsJwtKeyMoreActionsDeleteKey'] }</a> --%>
             
             
             <a href="#"
                        onclick="if (confirm('${textContainer.textEscapeSingleDouble['localEntityWsJwtKeyConfirmDeleteKey']}')) { return ajax('../app/UiV2LocalEntity.deleteWsJwtKey?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}');}">${textContainer.text['localEntityWsJwtKeyMoreActionsDeleteKey'] }</a>
             
             </li>
             
             <li>
             
           <%--   <a href="?operation=UiV2LocalEntity.deleteWsJwtKeyCreateNew&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2LocalEntity.deleteWsJwtKeyCreateNew&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
             >${textContainer.text['localEntityWsJwtKeyMoreActionsDeleteKeyAndCreateNew'] }</a> --%>
             
             <a href="#"
                        onclick="if (confirm('${textContainer.textEscapeSingleDouble['localEntityWsJwtKeyConfirmDeleteAndCreateNewKey']}')) { return ajax('../app/UiV2LocalEntity.deleteWsJwtKeyCreateNew?subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}');}">${textContainer.text['localEntityWsJwtKeyMoreActionsDeleteKeyAndCreateNew'] }</a>
             
             </li>
            </c:if>

          </ul>
        </div>