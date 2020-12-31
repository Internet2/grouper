<%@ include file="../assetsJsp/commonTaglib.jsp"%>


<div id="id_${grouperRequestContainer.provisionerDiagnosticsContainer.uniqueDiagnosticsId}">

</div>

<div class="span6">
                   
   <a class="btn btn-cancel" role="button"
        onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations&uniqueDiagnosticsId=${grouperRequestContainer.provisionerDiagnosticsContainer.uniqueDiagnosticsId}'); return false;"
        >${textContainer.text['provisionerConfigEditFormCancelButton'] }</a>
 
 </div>