<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <%--
              <ul class="breadcrumb">
                <li><a href="index.html">Home </a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">Applications</li>
              </ul>
              --%>
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}
              <div class="page-header blue-gradient">
                <div class="row-fluid">
                  <div class="span10">
                    <h1><i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}</h1>
                    <p>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.description)}</p>
                    <div id="stemDetailsId" style="display: none;">
                      <table class="table table-condensed table-striped">
                        <tbody>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelPath'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.pathColonSpaceSeparated)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelIdPath'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.name)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelAlternateIdPath'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.alternateName)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelId'] }</strong></td>
                            <td>${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.stem.extension)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelCreated'] }</strong></td>
                            <td>${grouper:formatDateLong(grouperRequestContainer.stemContainer.guiStem.stem.createTimeLong)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelCreator'] }</strong></td>
                            <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.stemContainer.guiStem.stem.creatorUuid)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelLastEdited'] }</strong></td>
                            <td>${grouper:formatDateLong(grouperRequestContainer.stemContainer.guiStem.stem.modifyTimeLong)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelLastEditor'] }</strong></td>
                            <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.stemContainer.guiStem.stem.modifierUuid)}</td>
                          </tr>
                          <tr>
                            <td><strong>${textContainer.text['stemLabelUuid'] }</strong></td>
                            <td>${grouper:subjectStringLabelShort2fromMemberId(grouperRequestContainer.stemContainer.guiStem.stem.modifierUuid)}</td>
                          </tr>
                          
                          
                        </tbody>
                      </table>
                    </div>
                    <p id="stemDetailsMoreId"><a href="#" onclick="$('#stemDetailsId').show('slow'); $('#stemDetailsMoreId').hide(); $('#stemDetailsLessId').show(); return false" >${textContainer.text['guiMore']} <i class="fa fa-angle-down"></i></a></p>
                    <p id="stemDetailsLessId" style="display: none"><a href="#" onclick="$('#stemDetailsId').hide('slow'); $('#stemDetailsLessId').hide(); $('#stemDetailsMoreId').show(); return false" >${textContainer.text['guiLess']} <i class="fa fa-angle-up"></i></a></p>
                     
                  </div>
                  <div class="span2" id="stemMoreActionsButtonContentsDivId">
                    <%@ include file="stemMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
              </div>
            </div>
