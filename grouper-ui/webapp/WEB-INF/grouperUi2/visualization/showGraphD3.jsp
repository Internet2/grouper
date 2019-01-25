<script>
var visualizationObject = {};

var dot = "";

function getStyleArray(graph, styleNames, obj) {
  var styles = [];
  styleNames.forEach(function(styleName) {
    if (graph.styles.hasOwnProperty(obj.type) && ((graph.styles[obj.type][styleName]||'') != '')) {
      styles.push(styleName + '=' + graph.styles[obj.type][styleName]);
    }
  });

  return styles;
}

function d3FollowObject(objectType, objectId) {
  var operation="";
  var operationMap = { group: "UiV2Visualization.groupView", stem: "UiV2Visualization.stemView", subject: "UiV2Visualization.subjectView"};
  $('#drawForm-operation').val(operationMap[objectType]);
  $('#drawForm-objectType').val(objectType);
  $('#drawForm-objectId').val(objectId);
  $('#visualizationDrawForm').submit();
  //ajax('../app/UiV2Visualization.buildGraph', {formIds: 'visualizationDrawForm', });
}


function drawGraphModuleD3() {
  const graph = visualizationObject; //shorter name
  dot = 'digraph "Grouper Graph of: ' + graph.statistics.startNode + '" {' + "\n";
  dot += 'node [ shape=rect; fontname="Courier,monospace"; fontsize="12.0";  ];' + "\n";
  dot += 'graph [  center=true; splines=spline; ratio=auto; ranksep = ".5"; nodesep = ".25 equally"; rankdir=LR; fontname="Courier,monospace"; bgcolor=gray91; packmode=clust; ];' + "\n";
  Object.values(graph.nodes).forEach(
    function(node) {
      var props = getStyleArray(graph, ['shape', 'style', 'color', 'fontcolor'], node);
      if (node.baseType != "provisioner") {
          props.push("URL=\"javascript:d3FollowObject('" + node.baseType + "', '" + node.id + "')\"");
            }

      if ((node.baseType == 'group') && graph.settings.showMemberCounts) {
        props.push('label=<' + node.name + '<br/>' + (node.memberCount||0) + '>');
      } else {
        props.push('label="' + (node.name||'(root)') + '"');
      }

      props.push("onclick=\"alert('ok " + node.id + "')\"");
        var row = '{"' + node.id + '" [' + props.join(' ') + '] ; }';
        console.debug(row);
        dot += row + "\n";
    });

  graph.links.forEach(
    function(link) {
    var styles = getStyleArray(graph, ['arrowtail', 'dir', 'color', 'style'], link);
        row = '"' + link.source + '" -> "' + link.target + '" [' +  styles.join('; ') + '];';
        console.debug(row);
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

  dot += "{ \"Statistics\n" + statString + "\" [ pos=\"1,5!\" shape=\"rectangle\" style=\"rounded,filled\"; fillcolor=\"navy\" fontcolor=\"white\" label=<" + statString + "> ]; };\n"
  dot += '}'+ "\n";

  $("#drawGraphPane").empty();
  d3.select("#drawGraphPane").graphviz().width($("#drawGraphPane").width()).fit(true).renderDot(dot);
  //d3.select("#drawGraphPane").graphviz().renderDot('digraph  {a -> b}');
}


function fetchGraph() {
  $("#drawGraphPane").html('<img style="display: block; margin-left: auto; margin-right: auto" alt="busy..."  id="groupVisGraphThrobberId" src="../../grouperExternal/public/assets/images/busy.gif" />');
  ajax('../app/UiV2Visualization.buildGraph', {formIds: 'visualizationDrawForm', });
}

$( document ).ready(function() {
  fetchGraph();
});

</script>
