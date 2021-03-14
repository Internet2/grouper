<%@ include file="../assetsJsp/commonTaglib.jsp"%>


<div id="id_${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.uniqueDiagnosticsId}">

</div>

<div class="span6">
   <br /><br />           
   <a class="btn btn-cancel" role="button"
        onclick="return guiV2link('operation=UiV2ProvisionerConfiguration.viewProvisionerConfigurations&uniqueDiagnosticsId=${grouperRequestContainer.grouperProvisioningDiagnosticsContainer.uniqueDiagnosticsId}'); return false;"
        >${textContainer.text['grouperProvisioningDiagnosticsBack'] }</a>
 
 </div>