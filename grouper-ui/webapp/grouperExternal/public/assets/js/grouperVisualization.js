var visualizationObject = {}; // returned from UiV2Visualization.buildGraph
var visualizationDotOutput = "";

function visIsShowingMemberCounts() {
  return document.getElementById("vis-settings-show-all-member-counts").checked || document.getElementById("vis-settings-show-direct-member-counts").checked;
}

$(document).ready(function() {
    $("#vis-settings-parents-levels").prop("disabled", document.getElementById("vis-settings-parents-all").checked);
    $("#vis-settings-children-levels").prop("disabled", document.getElementById("vis-settings-children-all").checked);
    $("#vis-settings-siblings").prop("disabled", document.getElementById("vis-settings-siblings-all").checked);
    $("#vis-settings-include-group-member-counts").prop("disabled", !visIsShowingMemberCounts());

    $("#vis-settings-parents-all").change(function(){ $("#vis-settings-parents-levels").prop("disabled", this.checked); });
    $("#vis-settings-children-all").change(function(){ $("#vis-settings-children-levels").prop("disabled", this.checked); });
    $("#vis-settings-siblings-all").change(function(){ $("#vis-settings-siblings").prop("disabled", this.checked); });
    $("#vis-settings-show-all-member-counts").change(function(){ $("#vis-settings-include-group-member-counts").prop("disabled", !visIsShowingMemberCounts()); });
    $("#vis-settings-show-direct-member-counts").change(function(){ $("#vis-settings-include-group-member-counts").prop("disabled", !visIsShowingMemberCounts()); });
});

// Visualization fullscreen lightbox
function openVisualizationModal() {
  d3.select("#visualization-fullscreen-content").graphviz().fit(true).renderDot(visualizationDotOutput);
  document.getElementById('visualization-fullscreen').style.display = "block";
}

// Exit visualization fullscreen
function closeVisualizationModal() {
  document.getElementById('visualization-fullscreen').style.display = "none";
}


function fetchGraph() {
  // hide the button to show the D3 dot output until successful return
  $("#vis-copy-dot-output-btn").hide();

  // This adds a spinner while fetching the graph. Not needed since the ajax call adds one to the full page
  //$("#vis-graph-svg-pane").html('<img style="display: block; margin-left: auto; margin-right: auto" alt="busy..."  id="groupVisGraphThrobberId" src="../../grouperExternal/public/assets/images/busy.gif" />');
  ajax("../app/UiV2Visualization.buildGraph", {formIds: "vis-settings-form" });
}


// https://stackoverflow.com/a/28458409
// escape bad characters in javascript strings
function escapeHTML(unsafe) {
  // CH need to escape more chars for some reason...
  return unsafe.replace(/[<>&"']/g, function(m) {
    switch (m) {
      case '<':
        return '&lt;';
      case '>':
        return '&gt;';
      case '&':
        return '&amp;';
      case '"':
        return '&quot;';
      case "'":
        return '&#39;';
    }
  });
}


function getObjectNameUsingPrefs(node) {
  var objName = ($("#vis-settings-form input[name='drawObjectNameType']:checked").val() === "path") ? node.name : node.displayExtension;
  // the root node has a blank name or display extension
  if (node.baseType === "stem" && !objName) {
    objName = "(Root folder)";
  }
  return escapeHTML(objName);
}


function getObjectLink(node, text) {
  if (node.linkType) {
    return "<a href=\"javascript:followObject('" + node.linkType + "', '" + node.id + "')\">" + text + "</a>";
  } else {
    return text;
  }
}


function escapeText(unsafe) {
  return unsafe.replace('"', '\\"');
}


// if we want to immediately display the graph instead of waiting to click the Generate button
//$(document).ready(function() {
//  fetchGraph();
//});


function title(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

// invoked by return from ajax
function drawGraphModuleText() {
  var graph = visualizationObject; // less verbose name
  var contents = "";

  var i;
  for (i=0; i < graph.sortedNodeIds.length; i+=1) {
    var node = graph.nodes[graph.sortedNodeIds[i]];
    var startNodeText = (graph.settings.startNode === node.id) ? " (current object)" : "";

    contents += "<div class=\"visualization-text-node\"><strong>" + title(graph.styles[node.type].displayTag||"unknown object type") + startNodeText + ":</strong> ";
    contents += ((node.baseType === "stem" && !node.name) ? "(Root folder)" : escapeHTML(node.displayExtension)) + "<br/>";
    contents += "Path: " + getObjectLink(node, escapeHTML(node.name)) + "<br/>";

    // description
    if (node.description !== null && node.description !== "") {
      contents += "Description: " + escapeHTML(node.description) + "<br/>";
    }

    // object types
    if (graph.settings.showObjectTypes) {
      if (node.objectTypes && node.objectTypes.length > 0) {
        contents += "Object types: " + escapeHTML(node.objectTypes.join(", ")) + "<br/>";
      }
    }

    // All member count
    if (graph.settings.showAllMemberCounts && node.baseType === "group") {
      contents += "Total member count: " + node.allMemberCount + "<br/>";
    }

    // Direct member count
    if (graph.settings.showDirectMemberCounts && node.baseType === "group") {
      contents += "Direct member count: " + node.directMemberCount + "<br/>";
    }

    // contains (for stem), direct membership in a group (group, subject)
    if (node.linkType === "group" || node.baseType === "stem" || node.baseType === "subject") {
      contents += (node.linkType === "stem" ? "Contains: " : "Direct membership in these groups: ");
      if (node.childNodeIds.length > 0) {
        contents += "<ul>";
        node.childNodeIds.forEach(function(id) {
          var obj = graph.nodes[id];
          contents += "<li>" + (graph.styles[obj.type].displayTag||"unknown object") + ": " + getObjectLink(obj, getObjectNameUsingPrefs(obj)) + "</li>";
        });
        contents += "</ul>";
      } else {
        contents += "none<br/>";
      }
    }

    // provision to
    node.childNodeIds.forEach(function(id) {
      var obj = graph.nodes[id];
      if (obj.baseType === "provisioner") {
        contents += "Provision to: " + getObjectNameUsingPrefs(obj) + "<br/>";
      }
    });

    // composite owner
    if (node.compositeLeftFactorId !== "" && node.compositeRightFactorId !== "") {
      var leftNode = graph.nodes[node.compositeLeftFactorId];
      var rightNode = graph.nodes[node.compositeRightFactorId];
      contents += "Composite owner of " +  getObjectLink(leftNode, getObjectNameUsingPrefs(leftNode));

      switch (node.baseType) {
        case "complement_group":
          contents += " minus ";
          break;
        case "intersect_group":
          contents += " intersected with ";
          break;
        default:
          contents += " *unknown operation* ";
      }

      contents += getObjectLink(rightNode, getObjectNameUsingPrefs(rightNode)) + "<br/>";
    }

    //group direct members
    if (node.linkType === "group") {
      contents += "Direct group members: ";
      var isDirectMember = false;
      node.parentNodeIds.forEach(function(id) {
        var obj = graph.nodes[id];

        // don't print composite factors, because they were already output
        if (node.compositeLeftFactorId === obj.id || node.compositeRightFactorId === obj.id) {
        return;
        }

        if (obj.baseType === "group") {
          if (!isDirectMember) {
            // print ul on first found
            contents += "<ul>";
            isDirectMember = true;
          }
          contents += "<li>" + (graph.styles[obj.type].displayTag||"unknown object") + ": " + getObjectLink(obj, getObjectNameUsingPrefs(obj)) + "</li>";
        }
      });
      if (isDirectMember) {
        // found at least one group
        contents += "</ul>";
      } else {
        contents += "none<br/>";
      }
    }

    //Loaded by
    node.parentNodeIds.forEach(function(id) {
      var obj = graph.nodes[id];
      if (obj.baseType === "loader_group") {
        contents += "Loaded by job: " + getObjectLink(obj, getObjectNameUsingPrefs(obj)) + "<br/>";
      }
    });

    contents += "</div>";
  }

  $("#vis-graph-svg-outer").hide();
  $("#vis-graph-text").html(contents);
  $("#vis-graph-text").show();

}


function getStyleArray(graph, styleNames, obj) {
  var styles = [];
  styleNames.forEach(function(styleName) {
    if (graph.styles.hasOwnProperty(obj.type) && ((graph.styles[obj.type][styleName]||"") !== "")) {
      styles.push(styleName + "=" + graph.styles[obj.type][styleName]);
    }
  });

  return styles;
}


// invoked from clicking a href of an object in the D3 graph
function followObject(objectType, objectId) {
  var operationMap = { group: "UiV2Visualization.groupView", stem: "UiV2Visualization.stemView", subject: "UiV2Visualization.subjectView"};
  if (operationMap.hasOwnProperty(objectType)) {
    $("#vis-settings-operation").val(operationMap[objectType]);
    $("#vis-settings-objectid").val(objectId);
    $("#vis-settings-objecttype").val(objectType);
    $("#vis-settings-form").submit();
    //ajax('../app/UiV2Visualization.buildGraph', {formIds: 'vis-settings-form', });
  }
}

function drawGraphModuleD3() {
  const graph = visualizationObject; //shorter name

  // whether to include object type names in calculated label; determine once here instead of every loop
  var showObjectTypesLabel = (graph.settings.showObjectTypes);

  // whether to include counts in calculated label; determine once here instead of every loop
  var showCountLabel = (graph.settings.showAllMemberCounts || graph.settings.showDirectMemberCounts);

  var dot;

  try {
    var statString = "Graph Edges: " + graph.statistics.numEdges + "\n";
    statString += "Total memberships: " + graph.statistics.totalMemberCount + "\n";
    statString += "Direct memberships: " + graph.statistics.directMemberCount + "\n";
    statString += "Nodes: " + graph.statistics.numNodes + "\n";
    statString += "Loader Jobs: " + graph.statistics.numLoaderJobs + "\n";
    statString += "Loaded Groups: " + graph.statistics.numGroupsFromLoaders + "\n";
    statString += "Provisioner Targets: " + graph.statistics.numProvisioners + "\n";
    statString += "Provisioned Groups: " + graph.statistics.numGroupsToProvisioners + "\n";
    statString += "Skipped Folders: " + graph.statistics.numSkippedFolders + "\n";
    statString += "Skipped Groups: " + graph.statistics.numSkippedGroups + "\n";

    var drawObjectNameType = $("#vis-settings-form input[name='drawObjectNameType']:checked").val();

    dot = 'digraph "Grouper Graph of: ' + escapeText(getObjectNameUsingPrefs(graph.nodes[graph.settings.startNode])) + "\n\n" + statString + '"' + " {\n";
    dot += "node [" + graph.styles.graph.nodestyle + " ];\n";
    dot += "graph [" + graph.styles.graph.style + " ];\n";
    Object.values(graph.nodes).forEach(
      function(node) {
        var props = getStyleArray(graph, ["shape", "style", "color", "fontcolor"], node);
        if (node.linkType) {
            props.push("URL=\"javascript:followObject('" + node.linkType + "', '" + node.id + "')\"");
        }

        // a stem or group can have multiple rows in the label, depending on whether showing object types or counts
        var labelRows = [];
        labelRows.push(escapeHTML(getObjectNameUsingPrefs(node)));

        if (showObjectTypesLabel) {
          if (node.objectTypes && node.objectTypes.length > 0) {
            labelRows.push(escapeHTML(node.objectTypes.join(", ")));
          }
        }

        if (showCountLabel) {
          var labelCounts = [];

          if (node.baseType === "group" || node.baseType === "complement_group" || node.baseType === "intersect_group"
               || node.type === "simple_loader_group" || node.type === "start_simple_loader_group") {
            if (graph.settings.showAllMemberCounts) {
              labelCounts.push((node.allMemberCount||0)+ " member" + (node.allMemberCount === 1 ? "" : "s"));
            }
            if (graph.settings.showDirectMemberCounts) {
              labelCounts.push((node.directMemberCount||0)+ " direct member" + (node.directMemberCount === 1 ? "" : "s"));
            }
          }

          if (labelCounts.length > 0) {
            labelRows.push(labelCounts.join(", "));
          }
        }

        if (labelRows.length <= 1) {
          props.push('label="' + labelRows[0] + '"');
        } else {
          props.push("label=<" + labelRows.join("<br/>") + ">");
        }

        //when the label is path/extension, use the opposite for the tooltip
        var tooltip = (drawObjectNameType === "path") ? escapeHTML(node.displayExtension) : escapeHTML(node.name);
        if (node.description !== null && node.description !== "") {
          tooltip += "\n" + escapeHTML(node.description);
        }
        props.push('tooltip="' + graph.styles[node.type].displayTag + ": " + tooltip + '"');

        var row = '{"' + node.id + '" [' + props.join(" ") + "] ; }";
        //console.debug(row);
        dot += row + "\n";
    });

    graph.links.forEach(
      function(link) {
        var props = getStyleArray(graph, ["arrowtail", "dir", "color", "style"], link);
        var source = graph.nodes[link.source];
        var target = graph.nodes[link.target];
        //debugger;
        if (source.baseType === "loader_group") {
          props.push('edgetooltip="' + getObjectNameUsingPrefs(source) + " is a loader job for group " + getObjectNameUsingPrefs(target) + '"');
        } else if (source.baseType === "stem") {
          props.push('edgetooltip="folder ' + getObjectNameUsingPrefs(source) + " contains " + graph.styles[target.type].displayTag +  " "  + getObjectNameUsingPrefs(target) + '"');
        } else if (source.baseType === "subject") {
          props.push('edgetooltip="subject ' + getObjectNameUsingPrefs(source) + " is a direct member of " + getObjectNameUsingPrefs(target) + '"');
        } else if (target.baseType === "intersect_group" || target.baseType === "complement_group") {
          var factorType = "";
          if (target.compositeLeftFactorId === source.id) {
            factorType = "left";
          } else if (target.compositeRightFactorId === source.id) {
            factorType = "right";
          }
          props.push('edgetooltip="group ' + getObjectNameUsingPrefs(graph.nodes[link.source]) + " is a " + factorType + " factor in " + graph.styles[target.type].displayTag + " " + getObjectNameUsingPrefs(graph.nodes[link.target]) + '"');
        } else if (source.baseType === "group") {
          if (target.baseType === "provisioner") {
            props.push('edgetooltip="group ' + getObjectNameUsingPrefs(source) + " provisions to " + getObjectNameUsingPrefs(target) + '"');
          } else if (target.baseType === "group") {
            props.push('edgetooltip="group ' + getObjectNameUsingPrefs(source) + " is a direct member of " + getObjectNameUsingPrefs(target) + '"');
          }
        }

        var row = '"' + link.source + '" -> "' + link.target + '" [' +  props.join('; ') + '];';
        //console.debug(row);
        dot += row + "\n";
      });

    dot += "}\n";

    // copy to outer scope so can be used elsewhere
    visualizationDotOutput = dot;

    //$("#vis-graph-svg-pane").empty();  // this is effective in clearing out the svg, but the second time it draws it, the mouse and wheel are no longer responsive

  } catch(error) {
    var msg = "There was an error generating the graphviz .dot output (" + error + ")";
    visualizationDotOutput = dot;
    $("#vis-graph-svg-outer").hide();
    $("#vis-graph-text").text(msg);
    $("#vis-graph-text").show();
    $("#vis-copy-dot-output-btn").show();
    return;
  }

  try {
    //d3.select("#vis-graph-svg-pane").graphviz().renderDot(dot);
    d3.select("#vis-graph-svg-pane").graphviz().fit(true).renderDot(dot);
    //d3.select("#vis-graph-svg-pane").graphviz().width($("#vis-graph-svg-pane").width()).renderDot(dot);
    //d3.select("#vis-graph-svg-pane").graphviz().width($("#vis-graph-svg-pane").width()).fit(true).renderDot(dot);
    //d3.select("#vis-graph-svg-pane").graphviz().renderDot('digraph  {a -> b}');

    $("#vis-graph-text").hide();
    $("#vis-graph-svg-outer").show();
    $("#vis-copy-dot-output-txt").html(dot);
    $("#vis-copy-dot-output-btn").show();

  } catch(error2) {
    var msg2 = "There was an error converting from GraphViz .dot to svg (" + error2 + ")";
    $("#vis-graph-svg-outer").hide();
    $("#vis-graph-text").text(msg2);
    $("#vis-graph-text").show();
    $("#vis-copy-dot-output-txt").html(dot);
    $("#vis-copy-dot-output-btn").show();

  }
}


//opens a new window with the contents of a div. Not yet working, may be security or popup issues
//function saveElement(elementId) {
//    window.open('data:text/plain;charset=utf-8;,' + encodeURIComponent($(elementId).html()));
//}


function setCopyWindowDot() {
  $("#vis-copy-dot-output-txt").val(visualizationDotOutput);
}

function setCopyWindowSVG() {
  var xmlNode = $("#vis-graph-svg-pane svg")[0];
  if (typeof xmlNode !== 'undefined') {
    $("#vis-copy-dot-output-txt").val((new XMLSerializer).serializeToString(xmlNode));
  }
}
