

<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<script src="../../grouperExternal/public/assets/js/grouperVisualization.js?updated=20190216" type="text/javascript"></script>

<div class="lead">${textContainer.text['visualization.title']}
  <a href="#" aria-label="Set visualization options" id="visualization-settings-button" class="btn btn-medium" aria-expanded="false" onclick="$('#visualization-settings').toggle()">
  <span class="fa fa-cog"></span><span class="caret"></span>
  </a>
</div>

<form class="form-horizontal form-inline" method="get" id="vis-settings-form">
  <input type="hidden" name="operation" id="vis-settings-operation" value="${grouperRequestContainer.visualizationContainer.operation}" />
  <input type="hidden" name="objectId" id="vis-settings-objectid" value="${grouperRequestContainer.visualizationContainer.objectId}" />
  <input type="hidden" name="objectType" id="vis-settings-objecttype" value="${grouperRequestContainer.visualizationContainer.objectType}" />

  <div id="visualization-settings" style="display: none">
    <table class="table table-condensed table-striped">
      <tbody>
        <!-- graph method -->
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label>${textContainer.text['visualization.form.method']}</label></strong>
          </td>
          <td>
            <input type="radio" name="drawModule" id="vis-settings-module-d3" value="d3" ${grouperRequestContainer.visualizationContainer.drawModule=="d3" ? "checked": ""} />
            <label for="vis-settings-module-d3">D3 (graphical)</label>
            <input type="radio" name="drawModule" id="vis-settings-module-text" value="text" ${grouperRequestContainer.visualizationContainer.drawModule=="text" ? "checked": ""}/>
            <label for="vis-settings-module-text">Text</label>
            <br/>
            <span class="description">${textContainer.text['visualization.form.method.description']}</span>
          </td>
        </tr>
        <!-- show name type -->
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label>${textContainer.text['visualization.form.objectNameType']}:</label></strong>
          </td>
          <td>
            <input type="radio" name="drawObjectNameType" id="vis-settings-name-dispextension" value="displayExtension" ${grouperRequestContainer.visualizationContainer.drawObjectNameType=="displayExtension" ? "checked": ""} />
            <label for="vis-settings-name-dispextension">${textContainer.text['visualization.form.objectNamesByDisplayExtension']}</label>
            <input type="radio" name="drawObjectNameType" id="vis-settings-name-path" value="path" ${grouperRequestContainer.visualizationContainer.drawObjectNameType=="path" ? "checked": ""} />
            <label for="vis-settings-name-path">${textContainer.text['visualization.form.objectNamesByFullPath']}</label>
            <br/>
            <span class="description">${textContainer.text['visualization.form.objectNameType.description']}</span>
          </td>
        </tr>
        <!-- parent levels -->
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label for="vis-settings-parents-all">${textContainer.text['visualization.form.showParents']}:</label></strong>
          </td>
          <td>
            <input type="checkbox" name="drawNumParentsAll" id="vis-settings-parents-all" value="true" ${grouperRequestContainer.visualizationContainer.drawNumParentsLevels <= -1 ? "checked": ""} />
            <label for="vis-settings-parents-all">${textContainer.text['visualization.form.filterShowAll']}</label>
            <input type="text" name="drawNumParentsLevels" id="vis-settings-parents-levels" class="span1" value="${grouperRequestContainer.visualizationContainer.drawNumParentsLevels}" />
            <br/>
            <span class="description">${textContainer.text['visualization.form.showParents.description']}</span>
          </td>
        </tr>
        <!-- child levels -->
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label for="vis-settings-children-all">${textContainer.text['visualization.form.showChildren']}:</label></strong>
          </td>
          <td>
            <input type="checkbox" name="drawNumChildrenAll" id="vis-settings-children-all" value="true" ${grouperRequestContainer.visualizationContainer.drawNumChildrenLevels <= -1 ? "checked": ""} />
            <label for="vis-settings-children-all">${textContainer.text['visualization.form.filterShowAll']}</label>
            <input type="text" name="drawNumChildrenLevels" id="vis-settings-children-levels" class="span1" value="${grouperRequestContainer.visualizationContainer.drawNumChildrenLevels}" />
            <br/>
            <span class="description">${textContainer.text['visualization.form.showChildren.description']}</span>
          </td>
        </tr>
        <!-- sibling levels -->
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label for="vis-settings-siblings-all">${textContainer.text['visualization.form.maxSiblings']}</label>:</strong>
          </td>
          <td>
            <input type="checkbox" name="drawMaxSiblingsAll" id="vis-settings-siblings-all" value="true" ${grouperRequestContainer.visualizationContainer.drawMaxSiblings <= 0 ? "checked": ""} />
            <label for="vis-settings-siblings-all">${textContainer.text['visualization.form.filterShowAll']}</label>
            <input type="text" name="drawMaxSiblings" id="vis-settings-siblings" class="span1" value="${grouperRequestContainer.visualizationContainer.drawMaxSiblings}" />
            <br/>
            <span class="description">${textContainer.text['visualization.form.maxSiblings.description']}</span>
          </td>
        </tr>
        <!-- show stems -->
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label for="vis-settings-show-stems">${textContainer.text['visualization.form.showStems']}</label>:</strong>
          </td>
          <td>
            <input type="checkbox" name="drawShowStems" id="vis-settings-show-stems" value="true" ${grouperRequestContainer.visualizationContainer.drawShowStems ? "checked": ""}/>
            <br/>
            <span class="description">${textContainer.text['visualization.form.showStems.description']}</span>
          </td>
        </tr>
        <!-- show loaders -->
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label for="vis-settings-show-loaders">${textContainer.text['visualization.form.showLoaders']}</label>:</strong>
          </td>
          <td>
            <input type="checkbox" name="drawShowLoaders" id="vis-settings-show-loaders" value="true" ${grouperRequestContainer.visualizationContainer.drawShowLoaders ? "checked": ""}/>
            <br/>
            <span class="description">${textContainer.text['visualization.form.showLoaders.description']}</span>
          </td>
        </tr>
        <!-- show provisioners -->
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label for="vis-settings-show-provisioners">${textContainer.text['visualization.form.showProvisioners']}</label>:</strong>
          </td>
          <td>
            <input type="checkbox" name="drawShowProvisioners" id="vis-settings-show-provisioners" value="true" ${grouperRequestContainer.visualizationContainer.drawShowProvisioners ? "checked": ""}/>
            <br/>
            <span class="description">${textContainer.text['visualization.form.showProvisioners.description']}</span>
          </td>
        </tr>
        <!-- show member counts -->
        <tr>
          <td style="vertical-align: top; white-space: nowrap;">
            <strong><label for="vis-settings-show-member-counts">${textContainer.text['visualization.form.showMemberCounts']}</label>:</strong>
          </td>
          <td>
            <input type="checkbox" name="drawShowMemberCounts" id="vis-settings-show-member-counts" value="true" ${grouperRequestContainer.visualizationContainer.drawShowMemberCounts ? "checked": ""}/>
            <br/>
            <span class="description">${textContainer.text['visualization.form.showMemberCounts.description']}</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
  <input type="submit" class="btn" name="drawRefresh" value="${textContainer.text['visualization.form.submit']}" onclick="fetchGraph(); return false;" />
</form>

<div id="vis-copy-dot-output-btn" style="display: none; padding 3px;">
  <a href="#" aria-label="Show .dot output text" class="btn btn-medium" aria-expanded="false" onclick="setCopyWindowDot(); $('#vis-copy-dot-output').toggle('slow')">
    ${textContainer.text['visualization.graph.copyDot']}
    <span class="caret"></span>
  </a>
  <a href="#" aria-label="Show SVG output" class="btn btn-medium" aria-expanded="false" onclick="setCopyWindowSVG(); $('#vis-copy-dot-output').toggle('slow')">
    ${textContainer.text['visualization.graph.copySVG']}
    <span class="caret"></span>
  </a>

  <div id="vis-copy-dot-output" style="display: none">
    <textarea id="vis-copy-dot-output-txt" cols="80" rows="20" style="width: 99%"></textarea>
  </div>
</div>
<!--<div id="vis-graph-svg-outer" style="display: none; position: fixed; left: 0; background: white; border: white 12px solid">-->
<div id="vis-graph-svg-outer" style="display: none; background: white; border: 1px solid gray; height: 500px; width: 100%">
  <div id="vis-graph-svg-pane"></div>
</div>
<div id="vis-graph-text" style="display: none"></div>
