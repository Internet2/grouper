<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">Applications</li>
              </ul>
              --%>
              ${grouperRequestContainer.groupContainer.guiGroup.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.groupContainer.guiGroup.group.displayExtension)}
                <br /><small>${textContainer.text['groupDeleteTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <p>${textContainer.text['groupDeleteText'] }</p>
                <div class="form-actions"><a href="#" class="btn btn-primary" onclick="ajax('../app/UiV2Group.groupDeleteSubmit?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;">${textContainer.text['groupDeleteDeleteButton'] }</a> 
                <a href="#" class="btn btn-cancel" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}');" >${textContainer.text['groupDeleteCancelButton'] }</a></div>
              </div>
            </div>
