<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />

            <div class="bread-header-container">
              <c:set target="${grouperRequestContainer.stemContainer.guiStem}" property="showBreadcrumbLink" value="true" />
              <c:set target="${grouperRequestContainer.stemContainer.guiStem}" property="showBreadcrumbLinkSeparator" value="false" />
              ${grouperRequestContainer.stemContainer.guiStem.breadcrumbs}
              <div class="page-header blue-gradient">
                <h1> <i class="fa fa-folder"></i> ${grouper:escapeHtml(grouperRequestContainer.stemContainer.guiStem.guiDisplayExtension)}
                <br /><small>${textContainer.text['stemAttestationTitle'] }</small></h1>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
              	<div id="stemAttestation">
                
                </div>
              </div>
            </div>