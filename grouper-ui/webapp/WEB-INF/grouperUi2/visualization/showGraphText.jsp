<script>
var visualizationObject = {
  'nodes': [],
  'styles': {},
  'statistics': {}
};

// invoked by return from ajax
function drawGraphModuleText() {
  const graph = visualizationObject; // less verbose name
  var INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
  var contents = "";

  for (i=0; i < graph.nodes.length; ++i) {
    var node = graph.nodes[i];
    contents += INDENT.repeat(node.indent);

    contents += (graph.styles[node.type].properties.label + ' ' || '    ');

    if (node.baseType == 'stem') {
      contents += '<a href="UiV2Main.index?operation=UiV2Visualization.stemView&amp;drawModule=text&amp;stemId=' + node.id + '">' + node.name + '</a>';
    } else if (node.baseType == 'group') {
      contents += '<a href="UiV2Main.index?operation=UiV2Visualization.groupView&amp;drawModule=text&amp;groupId=' + node.id + '">' + node.name + '</a>';
    } else {
      contents += node.name;
    }

    if (node.memberCount > 0) {
      contents += " (" + node.memberCount + ")";
    }

    contents += "<br/>";
  }

  $("#drawGraphPane").html(contents);
}

function fetchGraph() {
  $("#drawGraphPane").html('<img style="display: block; margin-left: auto; margin-right: auto" alt="busy..."  id="groupVisGraphThrobberId" src="../../grouperExternal/public/assets/images/busy.gif" />');
  ajax('../app/UiV2Visualization.buildGraph', {formIds: 'visualizationDrawForm', });
}

$( document ).ready(function() {
  fetchGraph();
});

</script>
