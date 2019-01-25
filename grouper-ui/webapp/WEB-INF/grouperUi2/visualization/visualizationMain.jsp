<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<script type="text/javascript">
$( document ).ready(function() {
    $( "#drawNumParentsLevels" ).prop( "disabled", document.getElementById('drawNumParentsAll').checked );
    $( "#drawNumChildrenLevels" ).prop( "disabled", document.getElementById('drawNumChildrenAll').checked );
    $( "#drawMaxSiblings" ).prop( "disabled", document.getElementById('drawMaxSiblingsAll').checked );

    $('#drawNumParentsAll').change(function(){ $( "#drawNumParentsLevels" ).prop( "disabled", this.checked ); });
    $('#drawNumChildrenAll').change(function(){ $( "#drawNumChildrenLevels" ).prop( "disabled", this.checked ); });
    $('#drawMaxSiblingsAll').change(function(){ $( "#drawMaxSiblings" ).prop( "disabled", this.checked ); });
});
</script>

<c:if test="${grouperRequestContainer.visualizationContainer.drawModule == 'd3'}" >
    <%@ include file="showGraphD3.jsp"%>
</c:if>
<c:if test="${grouperRequestContainer.visualizationContainer.drawModule == 'text'}" >
    <%@ include file="showGraphText.jsp"%>
</c:if>

<div class="row-fluid">
<div class="lead">${textContainer.text['visualization.title']}</div>

<form class="form-inline form-small" method="get" id="visualizationDrawForm">
<input type="hidden" name="operation" id="drawForm-operation" value="${grouperRequestContainer.visualizationContainer.operation}" />
<input type="hidden" name="objectId" id="drawForm-objectId" value="${grouperRequestContainer.visualizationContainer.objectId}" />
<input type="hidden" name="objectType" id="drawForm-objectType" value="${grouperRequestContainer.visualizationContainer.objectType}" />
<label for="drawModule">${textContainer.text['visualization.form.method']}</label>

<input type="radio" name="drawModule" id="drawModule-text" value="text" ${grouperRequestContainer.visualizationContainer.drawModule=="text" ? "checked": ""}/>
    <label for="drawModule-text">Text</label>
<input type="radio" name="drawModule" id="drawModule-d3" value="d3" ${grouperRequestContainer.visualizationContainer.drawModule=="d3" ? "checked": ""} />
    <label for="drawModule-d3">D3 (graphical)</label>

<p>${textContainer.text['visualization.form.filterSubHeading']}: </p>
<label for="drawNumParentsAll">${textContainer.text['visualization.form.parents']}:</label>&nbsp;
<input type="checkbox" name="drawNumParentsAll" id="drawNumParentsAll" value="true" ${grouperRequestContainer.visualizationContainer.drawNumParentsLevels <= -1 ? "checked": ""} />
    <label for="drawNumParentsAll">${textContainer.text['visualization.form.filterShowAll']}</label>
<input type="text" name="drawNumParentsLevels" id="drawNumParentsLevels" class="span1" value="${grouperRequestContainer.visualizationContainer.drawNumParentsLevels}" />

<label for="drawNumChildrenAll">${textContainer.text['visualization.form.children']}:</label>&nbsp;
<input type="checkbox" name="drawNumChildrenAll" id="drawNumChildrenAll" value="true" ${grouperRequestContainer.visualizationContainer.drawNumChildrenLevels <= -1 ? "checked": ""}/>
    <label for="drawNumChildrenAll">${textContainer.text['visualization.form.filterShowAll']}</label>
<input type="text" name="drawNumChildrenLevels" id="drawNumChildrenLevels" class="span1" value="${grouperRequestContainer.visualizationContainer.drawNumChildrenLevels}" />

<label for="drawMaxSiblingsAll">${textContainer.text['visualization.form.maxSiblings']}:</label>&nbsp;
<input type="checkbox" name="drawMaxSiblingsAll" id="drawMaxSiblingsAll" value="true" ${grouperRequestContainer.visualizationContainer.drawMaxSiblings <= 0 ? "checked": ""} />
    <label for="drawMaxSiblingsAll">${textContainer.text['visualization.form.filterShowAll']}</label>
<input type="text" name="drawMaxSiblings" id="drawMaxSiblings" class="span1" value="${grouperRequestContainer.visualizationContainer.drawMaxSiblings}" /><br/>

<label for="drawShowStems">${textContainer.text['visualization.form.showStems']}:
<input type="checkbox" name="drawShowStems" value="true" ${grouperRequestContainer.visualizationContainer.drawShowStems ? "checked": ""}/><br/>

<label for="drawShowLoaders">${textContainer.text['visualization.form.showLoaders']}:
<input type="checkbox" name="drawShowLoaders" value="true" ${grouperRequestContainer.visualizationContainer.drawShowLoaders ? "checked": ""}/><br/>

<label for="drawShowProvisioners">${textContainer.text['visualization.form.showProvisioners']}:
<input type="checkbox" name="drawShowProvisioners" value="true" ${grouperRequestContainer.visualizationContainer.drawShowProvisioners ? "checked": ""}/><br/>

<label for="drawShowMemberCounts">${textContainer.text['visualization.form.showMemberCounts']}:
<input type="checkbox" name="drawShowMemberCounts" value="true" ${grouperRequestContainer.visualizationContainer.drawShowMemberCounts ? "checked": ""}/><br/>

<input type="submit" class="btn" name="drawRefresh" value="${textContainer.text['visualization.form.submit']}" />

</form>
</div>

<div class="row-fluid">
<div class="lead">${textContainer.text['visualization.graph.title']}</div>
    <div id="drawGraphPane">
    </div>
</div>
