<%@ include file="../assetsJsp/commonTaglib.jsp"%>

  <div class="btn-group btn-block">
  
    <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreGrouperSubjectSourcesActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" 
      aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#subject-source-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#subject-source-more-options li').first().focus();return true;});">
        ${textContainer.text['subjectSourcesMoreActionsButton'] } <span class="caret"></span></a>

    <ul class="dropdown-menu dropdown-menu-right" id="subject-source-more-options">
    	<li><a href="#" onclick="return guiV2link('operation=UiV2SubjectSource.viewSubjectSources'); return false;"
            >${textContainer.text['subjectSourcesMoreActionsViewButton'] }</a></li>
        <li><a href="#" onclick="return guiV2link('operation=UiV2SubjectSource.addSubjectSource'); return false;"
            >${textContainer.text['subjectSourcesMoreActionsAddButton'] }</a></li>
    </ul>

  </div>