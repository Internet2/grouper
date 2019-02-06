<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<script type="text/javascript">

var visualizationObject = {}; // returned from UiV2Visualization.buildGraph
//var visualizationDot = "*unitialized*";

// https://stackoverflow.com/a/28458409
// escape bad characters in javascript strings
var escapeHTML = function(unsafe) {
  return unsafe.replace(/[<"']/g, function(m) {
    switch (m) {
      case '<':
        return '&lt;';
      case '"':
        return '&quot;';
      case "'":
        return '&#39;';
    }
  });
};

$( document ).ready(function() {
    $( "#drawNumParentsLevels" ).prop( "disabled", document.getElementById('drawNumParentsAll').checked );
    $( "#drawNumChildrenLevels" ).prop( "disabled", document.getElementById('drawNumChildrenAll').checked );
    $( "#drawMaxSiblings" ).prop( "disabled", document.getElementById('drawMaxSiblingsAll').checked );

    $('#drawNumParentsAll').change(function(){ $( "#drawNumParentsLevels" ).prop( "disabled", this.checked ); });
    $('#drawNumChildrenAll').change(function(){ $( "#drawNumChildrenLevels" ).prop( "disabled", this.checked ); });
    $('#drawMaxSiblingsAll').change(function(){ $( "#drawMaxSiblings" ).prop( "disabled", this.checked ); });
});

function fetchGraph() {
  // hide the button to show the D3 dot output until successful return
  $('#drawCopyDotOutputLink').hide();

  // This adds a spinner while fetching the graph. Not needed since the ajax call adds one to the full page
  //$("#drawGraphPane").html('<img style="display: block; margin-left: auto; margin-right: auto" alt="busy..."  id="groupVisGraphThrobberId" src="../../grouperExternal/public/assets/images/busy.gif" />');
  ajax('../app/UiV2Visualization.buildGraph', {formIds: 'visualizationDrawForm', });
}

// if we want to immediately display the graph instead of waiting to click the Generate button
//$( document ).ready(function() {
//  fetchGraph();
//});


// invoked by return from ajax
function drawGraphModuleText() {
  const graph = visualizationObject; // less verbose name
  var INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
  var contents = "";

  for (i=0; i < graph.nodes.length; ++i) {
    var node = graph.nodes[i];
    contents += INDENT.repeat(node.indent);

    contents += (graph.styles[node.type].label + ' ' || '    ');

    if (node.baseType == 'group' || node.baseType == 'stem' || node.baseType == 'subject') {
      contents += "<a href=\"javascript:d3FollowObject('" + node.baseType + "', '" + node.id + "')\">" + getObjectNameUsingPrefs(node) + '</a>';
    } else {
      contents += getObjectNameUsingPrefs(node);
    }

/*
    contents += "<a href=\"javascript:d3FollowObject('" + node.baseType + "', '" + node.id + "')\""
    props.push("URL=\"javascript:d3FollowObject('" + node.baseType + "', '" + node.id + "')\"");
    if (node.baseType == 'stem') {
      contents += '<a href="UiV2Main.index?operation=UiV2Visualization.stemView&amp;drawModule=text&amp;stemId=' + node.id + '">' + getObjectNameUsingPrefs(node) + '</a>';
    } else if (node.baseType == 'group') {
      contents += '<a href="UiV2Main.index?operation=UiV2Visualization.groupView&amp;drawModule=text&amp;groupId=' + node.id + '">' + getObjectNameUsingPrefs(node) + '</a>';
    } else {
      contents += getObjectNameUsingPrefs(node);
    }
*/

    if (node.memberCount > 0) {
      contents += " (" + node.memberCount + ")";
    }

    contents += "<br/>";
  }

  $("#drawGraphPane").html(contents);
}


function getStyleArray(graph, styleNames, obj) {
  var styles = [];
  styleNames.forEach(function(styleName) {
    if (graph.styles.hasOwnProperty(obj.type) && ((graph.styles[obj.type][styleName]||'') != '')) {
      styles.push(styleName + '=' + graph.styles[obj.type][styleName]);
    }
  });

  return styles;
}


// invoked from clicking a href of an object in the D3 graph
function d3FollowObject(objectType, objectId) {
  var operation="";
  var operationMap = { group: "UiV2Visualization.groupView", stem: "UiV2Visualization.stemView", subject: "UiV2Visualization.subjectView"};
  $('#drawForm-operation').val(operationMap[objectType]);
  $('#drawForm-objectType').val(objectType);
  $('#drawForm-objectId').val(objectId);
  $('#visualizationDrawForm').submit();
  //ajax('../app/UiV2Visualization.buildGraph', {formIds: 'visualizationDrawForm', });
}

function getObjectNameUsingPrefs(node) {
  var objName = ($("#visualizationDrawForm input[name='drawObjectNameType']:checked").val() == "path") ? node.name : node.displayExtension;
  // the root node has a blank name or display extension
  if (node.baseType == 'stem' && objName == "") {
    objName = "(Root)";
  }
  return escapeHTML(objName);
}

function drawGraphModuleD3() {
  const graph = visualizationObject; //shorter name
  var dot = 'digraph "Grouper Graph of: ' + graph.settings.startNode + '" {' + "\n";
  dot += 'node [ shape=rect; fontname="Courier,monospace"; fontsize="12.0";  ];' + "\n";
  dot += 'graph [  center=true; splines=spline; ratio=auto; ranksep = ".5"; nodesep = ".25 equally"; rankdir=LR; fontname="Courier,monospace"; bgcolor=gray91; packmode=clust; ];' + "\n";
  Object.values(graph.nodes).forEach(
    function(node) {
      var props = getStyleArray(graph, ['shape', 'style', 'color', 'fontcolor'], node);
      if (node.baseType != "provisioner") {
          props.push("URL=\"javascript:d3FollowObject('" + node.baseType + "', '" + node.id + "')\"");
      }

      if ((node.baseType == 'group') && graph.settings.showMemberCounts) {
        props.push('label=<' + getObjectNameUsingPrefs(node) + '<br/>' + (node.memberCount||0) + '>');
      } else {
        props.push('label="' + getObjectNameUsingPrefs(node) + '"');
      }

      var row = '{"' + node.id + '" [' + props.join(' ') + '] ; }';
      //console.debug(row);
      dot += row + "\n";
    });

  graph.links.forEach(
    function(link) {
      var props = getStyleArray(graph, ['arrowtail', 'dir', 'color', 'style'], link);
      if (graph.nodes[link.source].type == "loader_group" || graph.nodes[link.source].type == "start_loader_group") {
        props.push('edgetooltip="' + getObjectNameUsingPrefs(graph.nodes[link.source]) + ' is a loader job for group ' + getObjectNameUsingPrefs(graph.nodes[link.target]) + '"');
      } else if (graph.nodes[link.target].type == "intersect_group" || graph.nodes[link.target].type == "start_intersect_group") {
        props.push('edgetooltip="group ' + getObjectNameUsingPrefs(graph.nodes[link.source]) + ' is an intersect factor in group ' + getObjectNameUsingPrefs(graph.nodes[link.target]) + '"');
      } else if (graph.nodes[link.target].type == "complement_group" || graph.nodes[link.target].type == "start_complement_group") {
        props.push('edgetooltip="group ' + getObjectNameUsingPrefs(graph.nodes[link.source]) + ' is a complement factor in group ' + getObjectNameUsingPrefs(graph.nodes[link.target]) + '"');
      } else if (graph.nodes[link.source].baseType == "subject") {
        props.push('edgetooltip="subject ' + getObjectNameUsingPrefs(graph.nodes[link.source]) + ' is a direct member of ' + getObjectNameUsingPrefs(graph.nodes[link.target]) + '"');
      } else if (graph.nodes[link.source].baseType == "stem") {
        props.push('edgetooltip="folder ' + getObjectNameUsingPrefs(graph.nodes[link.source]) + ' contains member ' + getObjectNameUsingPrefs(graph.nodes[link.target]) + '"');
      } else if (graph.nodes[link.source].baseType == "group") {
        if (graph.nodes[link.target].baseType == "provisioner") {
          props.push('edgetooltip="group ' + getObjectNameUsingPrefs(graph.nodes[link.source]) + ' provisions to ' + getObjectNameUsingPrefs(graph.nodes[link.target]) + '"');
        } else if (graph.nodes[link.target].baseType == "group") {
          props.push('edgetooltip="group ' + getObjectNameUsingPrefs(graph.nodes[link.source]) + ' contains member ' + getObjectNameUsingPrefs(graph.nodes[link.target]) + '"');
        }
      }
      row = '"' + link.source + '" -> "' + link.target + '" [' +  props.join('; ') + '];';
      //console.debug(row);
      dot += row + "\n";
    });

  var statString = "Graph Edges: " + graph.statistics.numEdges + "<br/>";
  statString += "Memberships: " + graph.statistics.numMemberships + "<br/>";
  statString += "Nodes: " + graph.statistics.numNodes + "<br/>";
  statString += "Loader Jobs: " + graph.statistics.numLoaderJobs + "<br/>";
  statString += "Loaded Groups: " + graph.statistics.numGroupsFromLoaders + "<br/>";
  statString += "Provisioner Targets: " + graph.statistics.numProvisioners + "<br/>";
  statString += "Provisioned Groups: " + graph.statistics.numGroupsToProvisioners + "<br/>";
  statString += "Skipped Folders: " + graph.statistics.numSkippedFolders + "<br/>";
  statString += "Skipped Groups: " + graph.statistics.numSkippedGroups + "<br/>";

  //dot += "{ \"Statistics\n" + statString + "\" [ pos=\"1,5!\" shape=\"rectangle\" style=\"rounded,filled\"; fillcolor=\"navy\" fontcolor=\"white\" label=<" + statString + "> ]; };\n"
  dot += '}'+ "\n";

  $("#drawGraphPane").empty();
  d3.select("#drawGraphPane").graphviz().width($("#drawGraphPane").width()).renderDot(dot);
  //d3.select("#drawGraphPane").graphviz().width($("#drawGraphPane").width()).fit(true).renderDot(dot);
  //d3.select("#drawGraphPane").graphviz().renderDot('digraph  {a -> b}');

  //visualizationDot = dot;

  $('#drawCopyDotOutput').html(dot);
  $('#drawCopyDotOutputLink').show();

}

</script>

<div class="row-fluid">
<div class="lead">${textContainer.text['visualization.title']}</div>

<form class="form-inline form-small" method="get" id="visualizationDrawForm">
<input type="hidden" name="operation" id="drawForm-operation" value="${grouperRequestContainer.visualizationContainer.operation}" />
<input type="hidden" name="objectId" id="drawForm-objectId" value="${grouperRequestContainer.visualizationContainer.objectId}" />
<input type="hidden" name="objectType" id="drawForm-objectType" value="${grouperRequestContainer.visualizationContainer.objectType}" />

<p>
<label for="drawModule">${textContainer.text['visualization.form.method']}</label>
<input type="radio" name="drawModule" id="drawModule-d3" value="d3" ${grouperRequestContainer.visualizationContainer.drawModule=="d3" ? "checked": ""} />
    <label for="drawModule-d3">D3 (graphical)</label>
<input type="radio" name="drawModule" id="drawModule-text" value="text" ${grouperRequestContainer.visualizationContainer.drawModule=="text" ? "checked": ""}/>
    <label for="drawModule-text">Text</label>
</p>

<p>
<label for="drawObjectNameType">${textContainer.text['visualization.form.objectNameTypeLabel']}:</label>&nbsp;
<input type="radio" name="drawObjectNameType" id="drawObjectNameDisplayExtension" value="displayExtension" ${grouperRequestContainer.visualizationContainer.drawObjectNameType=="displayExtension" ? "checked": ""} />
    <label for="drawObjectNameDisplayExtension">${textContainer.text['visualization.form.objectNamesByDisplayExtension']}</label>
<input type="radio" name="drawObjectNameType" id="drawObjectNamePath" value="path" ${grouperRequestContainer.visualizationContainer.drawObjectNameType=="path" ? "checked": ""} />
    <label for="drawObjectNamePath">${textContainer.text['visualization.form.objectNamesByName']}</label>
</p>

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

<input type="submit" class="btn" name="drawRefresh" value="${textContainer.text['visualization.form.submit']}" onclick="fetchGraph(); return false;" />

</form>
</div>

<div class="row-fluid">
<div class="lead">${textContainer.text['visualization.graph.title']}
  <input type="button" id="drawCopyDotOutputLink" style="display: none"
      value="${textContainer.text['visualization.graph.copyDot']}" onclick="$('#drawCopyDotOutput').toggle('slow');" />
</div>
<div class="row-fluid">
  <textarea id="drawCopyDotOutput" cols="80" rows="20" style="display: none; width: 99%"></textarea>
</div>
<div id="drawGraphPane">
</div>
