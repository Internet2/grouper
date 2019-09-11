
/**
 * Sample script to generate a GraphViz .dot file and convert to svg
 *
 * From gsh, bind two parameters, inFilename and inObject, and then call the script; e.g.:
 *
 * gs = GrouperSession.startRootSession();
 * def binding = new Binding()
 * binding.inFilename = "C:/Temp/x.dot"
 * binding.inObject = GroupFinder.findByName(gs, 'basis:org:110100:contractor')
 * def ret = new GroovyShell(binding).evaluate(new File('../misc/visualization/buildSvgExample.groovy'))
 *
 */

import edu.internet2.middleware.grouper.*

import java.text.SimpleDateFormat
import edu.internet2.middleware.grouper.app.graph.RelationGraph
import edu.internet2.middleware.grouper.app.visualization.VisualSettings
import edu.internet2.middleware.grouper.app.visualization.VisualStyle
import edu.internet2.middleware.grouper.app.visualization.StyleObjectType
import org.apache.commons.lang.StringEscapeUtils

// no def -> global scope
DRAW_OBJECT_NAME_TYPE = "path"  // path | name



def escapeHTML(unsafe) {
  if (unsafe == null) {
    return 'null'
  } else {
      return StringEscapeUtils.escapeHtml(unsafe)
  }
}

def getObjectNameUsingPrefs(node) {
    def objName = (DRAW_OBJECT_NAME_TYPE == "path") ? node.grouperObjectName : node.grouperObject.displayExtension
    // the root node has a blank name or display extension
    if (node.stem && !objName) {
        objName = "(Root folder)"
    }
    return escapeHTML(objName)
}


String tstamp = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm").format(new Date())

gs = GrouperSession.startRootSession()

//Stem obj = StemFinder.findByName(gs, 'basis:org:110100')

// inObject needs to be a Grouper object bound from the caller using def binding = new Binding(); binding.inObject = ...
if (inObject == null) {
    throw new RuntimeException("Please bind inObject before running the script")
}

if (inFilename == null) {
    throw new RuntimeException("Please bind inFilename as the output filename, minus the .svg ending")
}
//Group obj = GroupFinder.findByName(gs, 'basis:org:110100:staff')

RelationGraph graph = new RelationGraph().assignStartObject(inObject)

graph.assignParentLevels(-1)
graph.assignChildLevels(-1)
graph.assignShowAllMemberCounts(true)
graph.assignShowStems(true)
graph.assignShowLoaderJobs(true)
graph.assignShowAllMemberCounts(true)
graph.assignShowDirectMemberCounts(true)
graph.assignIncludeGroupsInMemberCounts(false)
graph.assignShowObjectTypes(true)
//graph.assignMaxSiblings(4)
//graph.assignSkipFolderNamePatterns(["etc:.*", "ref:loader:.*"] as Set)
graph.assignSkipFolderNamePatterns(["etc:(attribute|legacy|provisioning|reportConfig|objectTypes|workflow|pspng|deprovisioning|grouperUi|workflowEditors)"] as Set)

graph.build()


ss = new VisualSettings().getStyleSet("dot")


def dotFile = new File(inFilename).newWriter()


dotFile.write """digraph "Grouper Graph of: ${inObject.name}
at ${tstamp}" {
\tnode\t[
\t\tshape=rect;
\t\tfontname="${ss.defaultStyle.getProperty("font","")}"; fontsize="${ss.defaultStyle.getProperty("font_size","")}"; 
\t\t];
\tgraph\t[ 
\t\t${ss.getStyle("graph").getProperty("style")} 
\t\t];

"""


// todo escape html in node names and descriptions

graph.edges.each { e ->
    String linkParam

    objStyles = []
    [VisualStyle.Property.ARROWTAIL, VisualStyle.Property.DIR, VisualStyle.Property.COLOR, VisualStyle.Property.STYLE].each { p ->
        val = ss.getStyleProperty(e.styleObjectType, p.name)
        if (val != null) {
            objStyles.add("${p.name}=${val}")
        }
    }
   
    if (e.fromNode.stem) linkParam = "operation=UiV2Stem.viewStem%26stemId=${e.fromNode.grouperObject.id}"
    else if (e.fromNode.group) linkParam = "operation=UiV2Group.viewGroup%26groupId=${e.fromNode.grouperObject.id}"

    if (e.fromNode.loaderGroup) {
        objStyles.add('edgetooltip="' + getObjectNameUsingPrefs(e.fromNode) + " is a loader job for group " + getObjectNameUsingPrefs(e.toNode) + '"')
    } else if (e.fromNode.stem) {
        objStyles.add('edgetooltip="folder ' + getObjectNameUsingPrefs(e.fromNode) + " contains " + ss.getStyleProperty(e.toNode.styleObjectType, "displayTag") + " " + getObjectNameUsingPrefs(e.toNode) + '"')
    } else if (e.toNode.subject) {
        objStyles.add('edgetooltip="' + getObjectNameUsingPrefs(e.fromNode) + " has subject " + getObjectNameUsingPrefs(e.toNode) + ' as a direct member"')
    } else if (e.fromNode.intersectGroup || e.fromNode.complementGroup) {
        def factorType = ""
        if (e.styleObjectType in [StyleObjectType.EDGE_INTERSECT_LEFT, StyleObjectType.EDGE_COMPLEMENT_LEFT] ) {
            factorType = "left"
        } else if (e.styleObjectType in [StyleObjectType.EDGE_INTERSECT_RIGHT, StyleObjectType.EDGE_COMPLEMENT_RIGHT] ) {
            factorType = "right"
        }
        objStyles.add('edgetooltip="' + ss.getStyleProperty(e.fromNode.styleObjectType, "displayTag") + " " + getObjectNameUsingPrefs(e.fromNode) + ' has group ' + getObjectNameUsingPrefs(e.toNode) + " as a " + factorType + ' factor"')
    } else if (e.fromNode.group) {
        if (e.toNode.provisionerTarget) {
            objStyles.add('edgetooltip="group ' + getObjectNameUsingPrefs(e.fromNode) + " provisions to " + getObjectNameUsingPrefs(e.toNode) + '"')
        } else {
            objStyles.add('edgetooltip="group ' + getObjectNameUsingPrefs(e.fromNode) + " has direct member " + getObjectNameUsingPrefs(e.toNode) + '"')
        }
    }

//    objStyles.add("edgeURL=\"https://localhost/grouper/grouperUi/app/UiV2Main.index?${linkParam}\"")
//    objStyles.add("headtooltip=\"${e.fromNode.grouperObject.name}\"")
//    objStyles.add("headURL=\"https://localhost/grouper/grouperUi/app/UiV2Main.index?${linkParam}\";")

    dotFile.write "\"${e.fromNode.grouperObject.id}\" -> \"${e.toNode.grouperObject.id}\""
    dotFile.write " [${objStyles.join("; ")}]"
    //    Add comment after each line with friendly names instead of ids; not as useful since the tooltips have been improved
    //dotFile.write "# ${e.fromNode.grouperObject.name} -> ${e.toNode.grouperObject.name}"
    dotFile.write("\n")
}


// skipped folders node
if (graph.getNumSkippedFolders() > 0) {
    def objStyles = []
    [VisualStyle.Property.SHAPE, VisualStyle.Property.STYLE, VisualStyle.Property.COLOR].each { p ->
        val = ss.getStyleProperty(StyleObjectType.SKIP_STEM.getName(), p.name)
        if (val != null) {
            objStyles.add("${p.name}=${val}")
        }
    }
    
    def labelRows = []
//    labelRows.add("Skipped folders: ${graph.getSkipFolderNamePatterns().join("|")}")
    labelRows.add("Skipped folders")
    labelRows.add(graph.getSkipFolderNamePatterns())
    labelRows.add("Number skipped = ${graph.getNumSkippedFolders()}")
    objStyles.add("label=<" + labelRows.join("<br/>") + ">")
    dotFile.write "{\"SkippedFolderNode\" [${objStyles.join("; ")} ] ; }\n"
}


graph.nodes.each { n ->
    def objStyles = []
    ["shape", "style", "color", "fontcolor", "border"].each { p ->
        val = ss.getStyleProperty(n.styleObjectType, p)
        if (val != null) {
            objStyles.add("${p}=${val}")
        }
    }
    
    if (n.stem) {
        objStyles.add("URL=\"operation=UiV2Stem.viewStem%26stemId=${n.grouperObjectId}\"")
    } else if (n.group) {
        objStyles.add("URL=\"operation=UiV2Group.viewGroup%26groupId=${n.grouperObjectId}\"")
    }

    def labelRows = []
    labelRows.add(escapeHTML(getObjectNameUsingPrefs(n)))

    //if (showObjectTypesLabel) {
    if (n.objectTypeNames && n.objectTypeNames.size() > 0) {
        def objectTypesCopy = n.objectTypeNames.clone()
        //(this is Java) Collections.copy(objectTypesCopy, n.objectTypeNames)
        if (n.loaderGroup) {
            objectTypesCopy.add("loader")
        }
        if (n.complementGroup) {
            objectTypesCopy.add("complement")
        }
        if (n.intersectGroup) {
            objectTypesCopy.add("intersection")
        }

        labelRows.add(escapeHTML(objectTypesCopy.join(", ")))
    }

    def labelCounts = []

    if (n.group || n.complementGroup || n.intersectGroup || n.simpleLoaderGroup) {
        labelCounts.add((n.allMemberCount?:0).toString() + " member" + (n.allMemberCount == 1 ? "" : "s"))
        labelCounts.add((n.directMemberCount?:0).toString()+ " direct member" + (n.directMemberCount == 1 ? "" : "s"))
    }

    if (labelCounts.size() > 0) {
        labelRows.add(labelCounts.join(", "))
    }

    if (labelRows.size() <= 1) {
        objStyles.add('label="' + labelRows[0] + '"')
    } else {
        objStyles.add("label=<" + labelRows.join("<br/>") + ">")
    }

    dotFile.write "{\"${n.grouperObject.id}\""
    dotFile.write " [${objStyles.join("; ")}${objStyles.size>0 ? ";":""} ];}\n"
}

statString = """${graph.startNode.grouperObject.name?:"(Root)"}
at ${tstamp}
Graph Edges: ${graph.edges.size()}
Total memberships: ${graph.showAllMemberCounts ? "${graph.totalMemberCount}" : "(not included)"}
Direct memberships: ${graph.showDirectMemberCounts ? "${graph.directMemberCount}" : "(not included)"}
Nodes: ${graph.nodes.size()}
Loader Jobs: ${graph.numLoaders}
Provisioner Targets: ${graph.numProvisioners}
Loaded Groups: ${graph.numGroupsFromLoaders}
Provisioned Groups: ${graph.numGroupsToProvisioners}
Skipped Folders: ${graph.numSkippedFolders}
Skipped Groups: ${graph.numSkippedGroups}"""

statStringHtml = statString.replace("\n", "<BR/>")

dotFile.write "{\"Statistics\n${statString}\" [ pos=\"1,5!\" shape=\"rectangle\" style=\"rounded,filled\"; fillcolor=\"navy\" label=<<TABLE BORDER=\"0\" ALIGN=\"LEFT\" CELLBORDER=\"0\" CELLPADDING=\"5\" CELLSPACING=\"0\" WIDTH=\"155\"><TR><TD BALIGN=\"LEFT\"><FONT FACE=\"Courier\" POINT-SIZE=\"16\" COLOR=\"white\">${statStringHtml}</FONT></TD></TR></TABLE>> ]; };\n"
dotFile.write "}\n"

dotFile.close()

def converter = "dot -Tsvg -o ${inFilename}.svg ${inFilename}".execute()
converter.waitFor()
if (converter.exitValue()) {
    println "Error Running svg command -- ${converter.text}"
} else {
    println "Converted to svg in file ${inFilename}.svg"
}
