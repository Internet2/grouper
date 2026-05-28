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
	
  if (typeof unsafe == 'undefined') {
    return 'undefined';
  } 
  if (unsafe == null) {
    return 'null';
  }

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


// Wraps a visualization label across multiple rows.
//   Hard breaks (each forms a new line; never merged with neighbors):
//     " or "  -> "or " starts the next line
//     " and " -> "and " starts the next line
//   For each resulting segment, width-wrap to targetWidth (50):
//     1. prefer a ", " break at/before targetWidth (comma stays at end of line)
//     2. else break at the last space at/before targetWidth; if that would
//        leave an orphan tail (< 8 chars), back up to the prior space
//     3. else extend the search forward; break at the next space if one
//        appears within hardWidth (75)
//     4. else hyphenate at hardWidth (only when a token has no spaces at all)
function wrapVisualizationLabel(text, targetWidth, hardWidth) {
  targetWidth = targetWidth || 50;
  hardWidth = hardWidth || 75;
  var segments = hardSplit(text);
  var rows = [];
  for (var i = 0; i < segments.length; i++) {
    var wrapped = widthWrap(segments[i], targetWidth, hardWidth);
    for (var j = 0; j < wrapped.length; j++) {
      rows.push(wrapped[j]);
    }
  }
  return rows;
}

function hardSplit(text) {
  var segments = [text];
  segments = applyKeywordSplit(segments, / or /i, "or ");
  segments = applyKeywordSplit(segments, / and /i, "and ");
  return segments;
}

function applyKeywordSplit(segments, regex, prefix) {
  var out = [];
  for (var i = 0; i < segments.length; i++) {
    var parts = segments[i].split(regex);
    for (var p = 0; p < parts.length; p++) {
      out.push(p === 0 ? parts[p] : prefix + parts[p]);
    }
  }
  return out;
}

var ORPHAN_TAIL = 8;

function widthWrap(text, targetWidth, hardWidth) {
  var rows = [];
  var line = text;
  while (line.length > targetWidth) {
    var commaIdx = line.lastIndexOf(", ", targetWidth);
    if (commaIdx > 0) {
      rows.push(line.substring(0, commaIdx + 1));
      line = line.substring(commaIdx + 2);
      continue;
    }
    var spaceIdx = line.lastIndexOf(' ', targetWidth);
    if (spaceIdx > 0) {
      if (line.length - spaceIdx - 1 < ORPHAN_TAIL) {
        var prior = line.lastIndexOf(' ', spaceIdx - 1);
        if (prior > 0) {
          spaceIdx = prior;
        }
      }
      rows.push(line.substring(0, spaceIdx));
      line = line.substring(spaceIdx + 1);
      continue;
    }
    var laterSpace = line.indexOf(' ', targetWidth);
    if (laterSpace > 0 && laterSpace <= hardWidth) {
      rows.push(line.substring(0, laterSpace));
      line = line.substring(laterSpace + 1);
      continue;
    }
    if (line.length > hardWidth) {
      rows.push(line.substring(0, hardWidth) + "-");
      line = line.substring(hardWidth);
      continue;
    }
    break;
  }
  if (line.length > 0) {
    rows.push(line);
  }
  return rows;
}


function getRawObjectNameUsingPrefs(node) {
  var objName = ($("#vis-settings-form input[name='drawObjectNameType']:checked").val() === "path") ? node.name : node.displayExtension;
  if (node.baseType === "stem" && !objName) {
    objName = "(Root folder)";
  }
  return objName;
}


function getObjectNameUsingPrefs(node) {
  //if (typeof node === "undefined") {
  //  return "unknown";
  //}

  var objName = ($("#vis-settings-form input[name='drawObjectNameType']:checked").val() === "path") ? node.name : node.displayExtension;
  // the root node has a blank name or display extension
  if (node.baseType === "stem" && !objName) {
    objName = "(Root folder)";
  }
  return escapeHTML(objName);
}


function getObjectLink(node, text) {
  //if (typeof node === "undefined") {
  //  return text;
  //}

  if (node.linkType) {
    return "<a href=\"javascript:followObject('" + node.linkType + "', '" + node.id + "')\">" + text + "</a>";
  } else {
    return text;
  }
}


// short description of a node for edge hover tooltips, e.g. "scripted group Faculty" or "data row hire info"
function visNodeShortDesc(graph, node) {
  var name = getObjectNameUsingPrefs(node);
  if (node.baseType === "compound_or" || node.baseType === "compound_and" || node.baseType === "data_row") {
    // these node names are already a full phrase, e.g. "Match ANY of the connected items"
    // or "Has a matching row in 'employeeData'"
    return name;
  }
  if (node.baseType === "data_attribute") {
    return "data attribute " + name;
  }
  var tag = (graph.styles[node.type] && graph.styles[node.type].displayTag) || "group";
  return tag + " " + name;
}


// plain-English relationship for an edge, taken from the arrow's label so the tooltip
// always matches what is drawn on the arrow (e.g. "must be in", "any of these")
function visEdgeRelationship(graph, link) {
  var edgeLabel = (graph.styles[link.type] && graph.styles[link.type].label) || '"references"';
  return edgeLabel.replace(/^"|"$/g, '');
}


function escapeText(unsafe) {
  
  if (typeof unsafe == 'undefined') {
    return 'undefined';
  } 
  if (unsafe == null) {
    return 'null';
  }

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
    if (graph.settings.abacScriptPreview && node.baseType === "abac_group" && node.populationCount != null && node.populationCount !== undefined) {
      contents += "Member count (if saved): " + node.populationCount + "<br/>";
    } else if (node.populationCount != null && node.populationCount !== undefined) {
      // skip regular member counts for nodes with population counts; population is shown separately below
    } else if (graph.settings.showAllMemberCounts && node.baseType === "group") {
      contents += "Total member count: " + node.allMemberCount + "<br/>";
    }

    // Direct member count (skip for nodes with population counts)
    if (!(node.populationCount != null && node.populationCount !== undefined)
        && graph.settings.showDirectMemberCounts && node.baseType === "group") {
      contents += "Direct member count: " + node.directMemberCount + "<br/>";
    }

    // ABAC population count (skip for abac_group since member count already shows this)
    if (node.populationCount != null && node.populationCount !== undefined
        && node.baseType !== "abac_group") {
      contents += "Population count: " + node.populationCount + "<br/>";
    }

    // contains (for stem), group direct members (group)
    if (node.linkType === "group" || node.baseType === "stem") {
      if (node.linkType === "stem") {
        contents += "Contains: ";
      } else if (node.baseType === "abac_group") {
        contents += node.abacTopConnective === "or" ? "Any of these must match: " : "All of these must match: ";
      } else {
        contents += "Direct group members: ";
      }
      var childContents = [];
      node.childNodeIds.forEach(function(id) {
          var obj = graph.nodes[id];

        // don't print composite factors and provisioners, because they are output elsewhere
        if (node.compositeLeftFactorId === id || node.compositeRightFactorId === id || obj.baseType === "provisioner") {
          return;
        } else {
          // surface ABAC edge polarity in the text view: the graph view shows this via the
          // arrow label, but the text view has no arrows so a negated reference (e.g.
          // "!entity.memberOf('test:GroupB')") otherwise looks identical to a positive one.
          var entry = (graph.styles[obj.type].displayTag || "unknown object") + ": " + getObjectLink(obj, getObjectNameUsingPrefs(obj));
          var edgeStyle = (node.childEdgeStyles && node.childEdgeStyles[id]) || "";
          if (edgeStyle === "edge_abac_and_not") {
            entry = "must not be in " + entry;
          } else if (edgeStyle === "edge_abac_or_not") {
            entry = "(not any of these) " + entry;
          }
          childContents.push(entry);
        }
      });

      if (childContents.length > 0) {
        contents += "<ul><li>" + childContents.join("</li><li>") + "</li></ul>";
      } else {
        contents += "none<br/>";
      }
    }

    // composite owner
    if (typeof node.compositeLeftFactorId !== "undefined" && typeof node.compositeRightFactorId !== "undefined" && node.compositeLeftFactorId !== "" && node.compositeRightFactorId !== "") {
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

    // direct membership in a group
    if (node.linkType === "group" || node.baseType === "subject") {
      contents += "Direct membership in these groups: ";
      var isDirectMember = false;
      node.parentNodeIds.forEach(function(id) {
        var obj = graph.nodes[id];

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

    // provision to
    node.childNodeIds.forEach(function(id) {
      var obj = graph.nodes[id];
      if (obj.baseType === "provisioner") {
        contents += "Provision to: " + getObjectNameUsingPrefs(obj) + "<br/>";
      }
    });

    if (node.type.endsWith("_is_member")) {
      contents += "Entity is member of group<br/>";
    } else if (node.type.endsWith("_is_not_member")) {
      contents += "Entity is not member of group<br/>";
    }

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


/**
 * for a base object type, return a "style=value ..." string; add in any hardcoded strings at the end
 */
function getStyleStringForType(graph, styleNames, objType, extraProperties) {
  var styles = [];

  if (graph.styles.hasOwnProperty(objType)) {
    styleNames.forEach(function(styleName) {
      if ((graph.styles[objType][styleName]||"") !== "") {
        styles.push(styleName + "=" + graph.styles[objType][styleName]);
      }
    });
  } else if (graph.hasOwnProperty("fallbackStyles") && graph.fallbackStyles.hasOwnProperty(objType)) {
    // fallback styles are like styles, but they are always guaranteed to be there, even when the particular
    // object type isn't in use. They are kept separate from styles so the latter can detect when an object is
    // actually being used, and can optionally be excluded from the legend if not
    styleNames.forEach(function(styleName) {
      if ((graph.fallbackStyles[objType][styleName]||"") !== "") {
        styles.push(styleName + "=" + graph.fallbackStyles[objType][styleName]);
      }
    });
  }

  styles.push(extraProperties);

  return styles.join(" ");
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

/**
 * Output representative nodes and edges in a subgraph that gets drawn as a boxed legend.
 * Use the actual styles from grouper.properties when available, but fall back in a few cases
 * to some hardcoded guesses -- e.g. if there is a start group but no normal group, we still want
 * to draw a group to showcase the specific edge style related to it. For the hardcoded fallbacks,
 * see function getStyleStringForType().
 */
function getGraphModuleD3Legend(graph) {
  var theLegend = "";

  var nodeStyles = ['shape', 'color', 'style', 'border', 'fontcolor', 'fillcolor'];
  var edgeStyles = ["arrowtail", "dir", "color", "style", "label"];
  theLegend += 'subgraph cluster1 {\n';
  theLegend += '  label = "Legend" ;\n';
  theLegend += '  shape=rectangle ;\n';
  theLegend += '  color = black ;\n';
  theLegend += '  fontsize = 20;\n';
  theLegend += '\n';

  // start stem
  if (graph.styles.hasOwnProperty("start_stem")) {
    theLegend += '  start_folder [' + getStyleStringForType(graph, nodeStyles, 'start_stem', ['label="start folder"']) + '] ;\n';
  }
  // start group
  if (graph.styles.hasOwnProperty("start_group") || graph.styles.hasOwnProperty("start_group_is_member") || graph.styles.hasOwnProperty("start_group_is_not_member") ||
      graph.styles.hasOwnProperty("start_simple_loader_group") || graph.styles.hasOwnProperty("start_simple_loader_group_is_member") || graph.styles.hasOwnProperty("start_simple_loader_group_is_not_member") ||
      graph.styles.hasOwnProperty("start_loader_group") || graph.styles.hasOwnProperty("start_loader_group_is_member") || graph.styles.hasOwnProperty("start_loader_group_is_not_member")) {
    theLegend += '  start_group [' + getStyleStringForType(graph, nodeStyles, 'start_group', ['label="start group"']) + '] ;\n';
  }
  // folder -- contains --> folder or group
  if (graph.styles.hasOwnProperty("stem")) {
    theLegend += '  folder [' + getStyleStringForType(graph, nodeStyles, 'stem', null) + '] ;\n';
    if (graph.styles.hasOwnProperty("group") || graph.styles.hasOwnProperty("group_is_member") || graph.styles.hasOwnProperty("group_is_not_member")) {
      theLegend += '  folder_or_group [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="folder or group"']) + '] ;\n';
    } else {
      theLegend += '  folder_or_group [label="folder or group"];\n';
    }

    theLegend += '  folder -> folder_or_group [label="contains"] ;\n';
  }
  // group1 -- has member --> group2
  if ((graph.styles.hasOwnProperty("group") || graph.styles.hasOwnProperty("group_is_member") || graph.styles.hasOwnProperty("group_is_not_member")) && graph.styles.hasOwnProperty("edge_membership")) {
    theLegend += '  group1 [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="group 1"']) + '];\n';
    theLegend += '  group2 [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="group 2"']) + '];\n';
    theLegend += '  group1 -> group2 [' + getStyleStringForType(graph, edgeStyles, 'edge_membership', ['label="has member"']) + '] ;\n';
  }
  // group -- has member --> subject
  if (graph.styles.hasOwnProperty("start_subject") /* && graph.styles.hasOwnProperty("group") */ && graph.styles.hasOwnProperty("edge_membership")) {
    theLegend += '  subject_member_group [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="group"']) + '];\n';
    theLegend += '  subject [' + getStyleStringForType(graph, nodeStyles, 'start_subject', ['label="start subject"']) + '];\n';
    theLegend += '  subject_member_group -> subject [' + getStyleStringForType(graph, edgeStyles, 'edge_membership', ['label="has member"']) + '] ;\n';
  }
  // loader group -- loads group --> group loaded
  if ((graph.styles.hasOwnProperty("loader_group") || graph.styles.hasOwnProperty("start_loader_group") || graph.styles.hasOwnProperty("loader_group_is_member") || graph.styles.hasOwnProperty("start_loader_group_is_member") || graph.styles.hasOwnProperty("loader_group_is_not_member") || graph.styles.hasOwnProperty("start_loader_group_is_not_member")) /* && graph.styles.hasOwnProperty("group") */ /* && graph.styles.hasOwnProperty("edge_loader") */) {
    theLegend += '  loader_group [' + getStyleStringForType(graph, nodeStyles, 'loader_group', ['label="loader group"']) + '];\n';
    theLegend += '  group_loaded [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="group loaded"']) + '];\n';
    theLegend += '  loader_group -> group_loaded [' + getStyleStringForType(graph, edgeStyles, 'edge_loader', ['label="loads group"']) + '] ;\n';
  }
  // simple loader group (self link)
  if ((graph.styles.hasOwnProperty("simple_loader_group") || graph.styles.hasOwnProperty("start_simple_loader_group") || graph.styles.hasOwnProperty("simple_loader_group_is_member") || graph.styles.hasOwnProperty("start_simple_loader_group_is_member") || graph.styles.hasOwnProperty("simple_loader_group_is_not_member") || graph.styles.hasOwnProperty("start_simple_loader_group_is_not_member")) && graph.styles.hasOwnProperty("edge_loader")) {
    theLegend += '  simple_loader_group [' + getStyleStringForType(graph, nodeStyles, 'simple_loader_group', ['label="simple loader group"']) + '];\n';
    theLegend += '  simple_loader_group -> simple_loader_group [' + getStyleStringForType(graph, edgeStyles, 'edge_loader', null) + '] ;\n';
  }

  // complement group -- in first but not second --<= first factor/second factor
  if (graph.styles.hasOwnProperty("complement_group") || graph.styles.hasOwnProperty("complement_group_is_member") || graph.styles.hasOwnProperty("complement_group_is_not_member") /* && graph.styles.hasOwnProperty("group")*/ ) {
    theLegend += '  complement_group [' + getStyleStringForType(graph, nodeStyles, 'complement_group', ['label="complement group"']) + '];\n';
    theLegend += '  complement_left_factor [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="include group"']) + '];\n';
    theLegend += '  complement_right_factor [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="exclude group"']) + '];\n';

    // it's possible only one of the factor edges is included; assume that getStyleStringForType() defaults the missing one so both lines are always there
    // the descriptive label on each edge style carries the meaning, so nothing extra is needed
    theLegend += '  complement_group -> complement_left_factor [' + getStyleStringForType(graph, edgeStyles, 'edge_complement_left', null) + '];\n';
    theLegend += '  complement_group -> complement_right_factor [' + getStyleStringForType(graph, edgeStyles, 'edge_complement_right', null) + '];\n';
  }

  // intersect group -- in first and in second --<= first factor/second factor
  if (graph.styles.hasOwnProperty("intersect_group") || graph.styles.hasOwnProperty("intersect_group_is_member") || graph.styles.hasOwnProperty("intersect_group_is_not_member") /* && graph.styles.hasOwnProperty("group")*/) {
    theLegend += '  intersect_group [' + getStyleStringForType(graph, nodeStyles, 'intersect_group', ['label="intersect group"']) + '];\n';
    theLegend += '  intersect_left_factor [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="group 1"']) + '];\n';
    theLegend += '  intersect_right_factor [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="group 2"']) + '];\n';

    // it's possible only one of the factor edges is included; assume that getStyleStringForType() defaults the missing one so both lines are always there
    // the descriptive label on each edge style carries the meaning, so nothing extra is needed
    theLegend += '  intersect_group -> intersect_left_factor [' + getStyleStringForType(graph, edgeStyles, 'edge_intersect_left', null) + '];\n';
    theLegend += '  intersect_group -> intersect_right_factor [' + getStyleStringForType(graph, edgeStyles, 'edge_intersect_right', null) + '];\n';

  }

  // abac (scripted) group -- script references --> referenced groups, data attributes, data rows.
  // Each reference edge (AND/AND NOT/OR/OR NOT) is shown when its style is in use, so the legend
  // covers OR scripts even when they have no compound node (a top-level OR references groups directly).
  var hasAbacGroup = graph.styles.hasOwnProperty("abac_group") || graph.styles.hasOwnProperty("abac_group_is_member") || graph.styles.hasOwnProperty("abac_group_is_not_member");
  var hasAbacAnd = graph.styles.hasOwnProperty("edge_abac_and");
  var hasAbacAndNot = graph.styles.hasOwnProperty("edge_abac_and_not");
  var hasAbacOr = graph.styles.hasOwnProperty("edge_abac_or");
  var hasAbacOrNot = graph.styles.hasOwnProperty("edge_abac_or_not");
  var hasCompoundOr = graph.styles.hasOwnProperty("compound_or") || graph.styles.hasOwnProperty("compound_or_is_member") || graph.styles.hasOwnProperty("compound_or_is_not_member");
  var hasCompoundAnd = graph.styles.hasOwnProperty("compound_and") || graph.styles.hasOwnProperty("compound_and_is_member") || graph.styles.hasOwnProperty("compound_and_is_not_member");

  if (hasAbacGroup || hasAbacAnd || hasAbacAndNot || hasAbacOr || hasAbacOrNot) {
    theLegend += '  abac_group [' + getStyleStringForType(graph, nodeStyles, 'abac_group', ['label="scripted group"']) + '];\n';
  }
  // positive reference: the referenced item (a group, attribute, row, or condition) the entity must match
  if (hasAbacAnd) {
    theLegend += '  abac_ref_group [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="referenced item"']) + '];\n';
    theLegend += '  abac_group -> abac_ref_group [' + getStyleStringForType(graph, edgeStyles, 'edge_abac_and', null) + '];\n';
  }
  // negated reference: the referenced item (a group, attribute, row, or condition) the entity must not match
  if (hasAbacAndNot) {
    theLegend += '  abac_not_group [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="referenced item"']) + '];\n';
    theLegend += '  abac_group -> abac_not_group [' + getStyleStringForType(graph, edgeStyles, 'edge_abac_and_not', null) + '];\n';
  }
  // compound condition shapes: the connected items combine with OR (any) or AND (all)
  if (hasCompoundOr) {
    theLegend += '  compound_or_legend [' + getStyleStringForType(graph, nodeStyles, 'compound_or', ['label="or condition"']) + '];\n';
  }
  if (hasCompoundAnd) {
    theLegend += '  compound_and_legend [' + getStyleStringForType(graph, nodeStyles, 'compound_and', ['label="and condition"']) + '];\n';
  }
  // OR option edges hang off the OR condition shape when there is one, otherwise off the scripted group
  if (hasAbacOr) {
    theLegend += '  abac_or_option [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="referenced item"']) + '];\n';
    theLegend += '  ' + (hasCompoundOr ? 'compound_or_legend' : 'abac_group') + ' -> abac_or_option [' + getStyleStringForType(graph, edgeStyles, 'edge_abac_or', null) + '];\n';
  }
  if (hasAbacOrNot) {
    theLegend += '  abac_or_not_option [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="referenced item"']) + '];\n';
    theLegend += '  ' + (hasCompoundOr ? 'compound_or_legend' : 'abac_group') + ' -> abac_or_not_option [' + getStyleStringForType(graph, edgeStyles, 'edge_abac_or_not', null) + '];\n';
  }

  // data attribute and data row nodes (check the is/is-not-member variants too, so the legend
  // still covers them when every such node happens to be a member or non-member)
  if (graph.styles.hasOwnProperty("data_attribute") || graph.styles.hasOwnProperty("data_attribute_is_member") || graph.styles.hasOwnProperty("data_attribute_is_not_member")) {
    theLegend += '  data_attr_legend [' + getStyleStringForType(graph, nodeStyles, 'data_attribute', ['label="entity attribute"']) + '];\n';
  }
  if (graph.styles.hasOwnProperty("data_row") || graph.styles.hasOwnProperty("data_row_is_member") || graph.styles.hasOwnProperty("data_row_is_not_member")) {
    theLegend += '  data_row_legend [' + getStyleStringForType(graph, nodeStyles, 'data_row', ['label="entity data row"']) + '];\n';
  }

  // provisioned group -- provisions to --> provisioner target
  if (graph.styles.hasOwnProperty("provisioner") /* && graph.styles.hasOwnProperty("group") */ && graph.styles.hasOwnProperty("edge_provisioner")) {
    theLegend += '  provisioner_source [' + getStyleStringForType(graph, nodeStyles, 'group', ['label="provisioned group"']) + '];\n';
    theLegend += '  provisioner [' + getStyleStringForType(graph, nodeStyles, 'provisioner', ['label="provisioner target"']) + '];\n';

    theLegend += '  provisioner_source -> provisioner [' + getStyleStringForType(graph, edgeStyles, 'edge_provisioner', ['label="provisions to"']) + '];\n';
  }

  // if using object types, display what they mean; use invisible nodes to force it to the right
  if (graph.settings.showObjectTypes && graph.settings.hasOwnProperty("objectTypesLegend")) {
    // create an invisible node with the same number of rows as the text node, so the nodes above it don't get shifted downward
    var numRowsInText = (graph.settings.objectTypesLegend.match(/,/g) || []).length;
    theLegend += '  invis_node [label="' + "\\l".repeat(numRowsInText) + '" style=invis];\n';
    theLegend += '  object_types [ label="' + graph.settings.objectTypesLegend + '" ];\n';
    theLegend += '  invis_node -> object_types [style=invis];\n';

  }

  // is member
  if (graph.styles.hasOwnProperty("group_is_member") || graph.styles.hasOwnProperty("start_group_is_member") || graph.styles.hasOwnProperty("loader_group_is_member") || graph.styles.hasOwnProperty("start_loader_group_is_member") || graph.styles.hasOwnProperty("simple_loader_group_is_member") || graph.styles.hasOwnProperty("start_simple_loader_group_is_member") || graph.styles.hasOwnProperty("complement_group_is_member") || graph.styles.hasOwnProperty("intersect_group_is_member") || graph.styles.hasOwnProperty("abac_group_is_member") || graph.styles.hasOwnProperty("data_attribute_is_member") || graph.styles.hasOwnProperty("data_row_is_member") || graph.styles.hasOwnProperty("compound_or_is_member") || graph.styles.hasOwnProperty("compound_and_is_member")) {
    theLegend += '  group_is_member [' + getStyleStringForType(graph, nodeStyles, 'group_is_member', ['label="entity is member of group"']) + '];\n';
  }

  // is not member
  if (graph.styles.hasOwnProperty("group_is_not_member") || graph.styles.hasOwnProperty("start_group_is_not_member") || graph.styles.hasOwnProperty("loader_group_is_not_member") || graph.styles.hasOwnProperty("start_loader_group_is_not_member") || graph.styles.hasOwnProperty("simple_loader_group_is_not_member") || graph.styles.hasOwnProperty("start_simple_loader_group_is_not_member") || graph.styles.hasOwnProperty("complement_group_is_not_member") || graph.styles.hasOwnProperty("intersect_group_is_not_member") || graph.styles.hasOwnProperty("abac_group_is_not_member") || graph.styles.hasOwnProperty("data_attribute_is_not_member") || graph.styles.hasOwnProperty("data_row_is_not_member") || graph.styles.hasOwnProperty("compound_or_is_not_member") || graph.styles.hasOwnProperty("compound_and_is_not_member")) {
    theLegend += '  group_is_not_member [' + getStyleStringForType(graph, nodeStyles, 'group_is_not_member', ['label="entity is not member of group"']) + '];\n';
  }

  theLegend += '}\n';

  return theLegend;
}


function drawGraphModuleD3() {
  const graph = visualizationObject; //shorter name

  // whether to include object type names in calculated label; determine once here instead of every loop
  var showObjectTypesLabel = (graph.settings.showObjectTypes);

  // whether to include counts in calculated label; determine once here instead of every loop
  var showCountLabel = (graph.settings.showAllMemberCounts || graph.settings.showDirectMemberCounts);

  var dot;

  try {
    /*
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
    */

    var drawObjectNameType = $("#vis-settings-form input[name='drawObjectNameType']:checked").val();

    //dot = 'digraph "Grouper Graph of: ' + escapeText(getObjectNameUsingPrefs(graph.nodes[graph.settings.startNode])) + "\n\n" + statString + '"' + " {\n";
    dot = "digraph \"\" {\n";
    dot += "node [" + graph.styles.graph.nodestyle + " ];\n";
    dot += "graph [" + graph.styles.graph.style + " ];\n";

    if (graph.settings.showLegend) {
      dot += getGraphModuleD3Legend(graph);
    }

    Object.values(graph.nodes).forEach(
      function(node) {
        var props = getStyleArray(graph, ["shape", "style", "color", "fontcolor", "border", "fillcolor"], node);
        if (node.linkType) {
            props.push("URL=\"javascript:followObject('" + node.linkType + "', '" + node.id + "')\"");
        }

        // a stem or group can have multiple rows in the label, depending on whether showing object types or counts
        var labelRows = [];
        var rawNodeName = getRawObjectNameUsingPrefs(node);
        if (rawNodeName.length > 60) {
          var wrappedRows = wrapVisualizationLabel(rawNodeName);
          for (var w = 0; w < wrappedRows.length; w++) {
            labelRows.push(escapeHTML(wrappedRows[w]));
          }
        } else {
          labelRows.push(escapeHTML(rawNodeName));
        }

        if (showObjectTypesLabel) {
          if (node.objectTypes && node.objectTypes.length > 0) {
            labelRows.push(escapeHTML(node.objectTypes.join(", ")));
          }
        }

        if (showCountLabel) {
          var labelCounts = [];

          if (graph.settings.abacScriptPreview && node.baseType === "abac_group" && node.populationCount != null && node.populationCount !== undefined) {
            labelCounts.push(node.populationCount + " member" + (node.populationCount === 1 ? "" : "s") + " (if saved)");
          } else if (node.populationCount != null && node.populationCount !== undefined) {
            // skip regular member counts for nodes with population counts; population is shown separately below
          } else if (node.baseType === "group" || node.baseType === "complement_group" || node.baseType === "intersect_group" || node.baseType === "abac_group"
               || node.type === "simple_loader_group" || node.type === "start_simple_loader_group"
               || node.type === "simple_loader_group_is_member" || node.type === "simple_loader_group_is_not_member"
               || node.type === "start_simple_loader_group_is_member" || node.type === "start_simple_loader_group_is_not_member") {
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

        // ABAC population count (skip for abac_group since member count already shows this)
        if (node.populationCount != null && node.populationCount !== undefined
            && node.baseType !== "abac_group") {
          labelRows.push("population: " + node.populationCount);
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
        var props = getStyleArray(graph, ["arrowtail", "dir", "color", "style", "label"], link);
        var source = graph.nodes[link.source];
        var target = graph.nodes[link.target];
        //debugger;
        if (source.baseType === "loader_group") {
          props.push('edgetooltip="' + getObjectNameUsingPrefs(source) + " is a loader job for group " + getObjectNameUsingPrefs(target) + '"');
        } else if (source.baseType === "stem") {
          props.push('edgetooltip="folder ' + getObjectNameUsingPrefs(source) + " contains " + graph.styles[target.type].displayTag +  " "  + getObjectNameUsingPrefs(target) + '"');
        } else if (target.baseType === "subject") {
          props.push('edgetooltip="' + getObjectNameUsingPrefs(source) + " has subject " + getObjectNameUsingPrefs(target) + ' as a direct member"');
        } else if (source.baseType === "intersect_group" || source.baseType === "complement_group"
                   || source.baseType === "abac_group" || source.baseType === "compound_or" || source.baseType === "compound_and"
                   || source.baseType === "data_row") {
          // mirror the arrow: "<source>  [<relationship>]  <target>", using the same plain-English
          // relationship that is drawn on the arrow's label (e.g. must be in, any of these)
          var relationship = visEdgeRelationship(graph, link);
          props.push('edgetooltip="' + visNodeShortDesc(graph, source) + " [" + relationship + "] " + visNodeShortDesc(graph, target) + '"');
        } else if (source.baseType === "group") {
          if (target.baseType === "provisioner") {
            props.push('edgetooltip="group ' + getObjectNameUsingPrefs(source) + " provisions to " + getObjectNameUsingPrefs(target) + '"');
          } else if (target.baseType === "group") {
            props.push('edgetooltip="group ' + getObjectNameUsingPrefs(source) + " has direct member " + getObjectNameUsingPrefs(target) + '"');
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
