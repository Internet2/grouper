<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- <div class="btn-group btn-block">
  <a id="show-add-block" href="javascript:void(0);" onclick="showHideSubjectAssignAttributeBlock()" 
    class="btn btn-medium btn-primary btn-block" role="button">
      <i class="fa fa-plus"></i> ${textContainer.text['localEntityCreateWsJwtKeyButton'] }
  </a>               
</div> --%>

        <div class="btn-group btn-block">
        
          <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreLocalEntityWsJwtKeyActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
            aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#wsJwtKey-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#wsJwtKey-more-options li').first().focus();return true;});">
              ${textContainer.text['localEntityWsJwtKeyMoreActionsButton'] } <span class="caret"></span></a>

          <ul class="dropdown-menu dropdown-menu-right" id="wsJwtKey-more-options">

            <c:if test="${grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword == null}" >
              <li><a href="#" onclick="return guiV2link('operation=UiV2LocalEntity.createNewWsJwtKey&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
                  >${textContainer.text['localEntityCreateWsJwtKeyButton'] }</a></li>
            </c:if>

            <c:if test="${grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword != null}" >
              <li><a href="#" onclick="return guiV2link('operation=UiV2LocalEntity.editWsJwtKey&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}&grouperPasswordId=${grouperRequestContainer.grouperPasswordContainer.guiGrouperPassword.grouperPassword.id}'); return false;"
                  >${textContainer.text['localEntityWsJwtKeyMoreActionsEditSettings'] }</a></li>
                  
             <li><a href="#" onclick="return guiV2link('operation=UiV2LocalEntity.deleteWsJwtKey&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
             >${textContainer.text['localEntityWsJwtKeyMoreActionsDeleteKey'] }</a></li>
             
             <li><a href="#" onclick="return guiV2link('operation=UiV2LocalEntity.deleteWsJwtKeyCreateNew&subjectId=${grouperRequestContainer.subjectContainer.guiSubject.subject.id}&sourceId=${grouperRequestContainer.subjectContainer.guiSubject.subject.sourceId}'); return false;"
             >${textContainer.text['localEntityWsJwtKeyMoreActionsDeleteKeyAndCreateNew'] }</a></li>
            </c:if>

          </ul>
        </div>