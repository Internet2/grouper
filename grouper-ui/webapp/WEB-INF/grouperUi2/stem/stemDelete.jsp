<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='icon-angle-right'></i></span></li>
                <li class="active">Applications</li>
              </ul>
              --%>
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="icon-folder-close"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}
                <br /><small>${textContainer.text['stemDeleteTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <p>${textContainer.text['stemDeleteText'] }</p>
                <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Stem.stemDeleteSubmit?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">Delete</a> 
                <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Stem.viewStem&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}');" >Cancel</a></div>
              </div>
            </div>
