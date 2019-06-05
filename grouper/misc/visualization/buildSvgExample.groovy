
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

String tstamp = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm").format(new Date())

gs = GrouperSession.startRootSession();

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

graph.build()


ss = new VisualSettings().getStyleSet("dot")


def dotFile = new File(inFilename).newWriter()


dotFile.write """digraph "Grouper Graph of: ${inObject.name}
at ${tstamp}" {
\tnode\t[
\t\tshape=none;
\t\tfontname="${ss.defaultStyle.getProperty("font","")}"; fontsize="${ss.defaultStyle.getProperty("font_size","")}"; 
\t\t];
\tgraph\t[ 
\t\tcenter=true; splines=spline; ratio=auto;
\t\tranksep = ".5"; nodesep = ".25 equally"; rankdir=LR;
\t\tfontname="${ss.getStyle("graph").getProperty("font","")}"; ${ss.getStyle("graph").getProperty("style")}
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
   
    //todo is the edgeUrl different from the headUrl?
    //todo don't set any link if not group/stem
    if (e.fromNode.stem) linkParam = "operation=UiV2Stem.viewStem%26stemId=${e.fromNode.grouperObject.id}"
    else if (e.fromNode.group) linkParam = "operation=UiV2Group.viewGroup%26groupId=${e.fromNode.grouperObject.id}"

    dotFile.write " \"${e.fromNode.grouperObject.id}\" -> \"${e.toNode.grouperObject.id}\""
    dotFile.write " [ ${objStyles.join("; ")}${objStyles.size>0 ? ";":""} edgetooltip=\"${e.fromNode.grouperObject.name} - ${e.fromNode.grouperObject.description}\" edgeURL=\"https://localhost/grouper/grouperUi/app/UiV2Main.index?${linkParam}\"; headtooltip=\"${e.fromNode.grouperObject.name}\" headURL=\"https://localhost/grouper/grouperUi/app/UiV2Main.index?${linkParam}\"; ] ;"
    dotFile.write "# ${e.fromNode.grouperObject.name} -> ${e.toNode.grouperObject.name}\n"
}


// skipped folders node
if (graph.getNumSkippedFolders()) {
    def objStyles = []
    [VisualStyle.Property.SHAPE, VisualStyle.Property.STYLE, VisualStyle.Property.COLOR].each { p ->
        val = ss.getStyleProperty(StyleObjectType.SKIP_STEM.getName(), p.name)
        if (val != null) {
            objStyles.add("${p.name}=${val}")
        }
    }
    
    String title = graph.getSkipFolderNamePatterns().join("|")

    dotFile.write "{ \"SkippedFolderNode\" [${objStyles.join(" ")} label=<<FONT><TABLE BORDER=\"1\" ALIGN=\"CENTER\" CELLBORDER=\"0\" CELLPADDING=\"1\" CELLSPACING=\"0\"><TR><TD ALIGN=\"CENTER\" BORDER=\"1\" WIDTH=\"24\" TITLE=\"${title}\" HREF=\"https://localhost/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem%26stemId=0247d71e5a7c4659abdf0ad7d82ae150\" ><FONT>${title}<BR/></FONT></TD></TR><TR><TD TITLE=\"${title}\" HREF=\"https://localhost/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Stem.viewStem%26stemId=0247d71e5a7c4659abdf0ad7d82ae150\" BORDER=\"1\"><FONT>Skipped Nodes = ${graph.getNumSkippedFolders()}</FONT></TD></TR></TABLE></FONT>>] ; }"
}


graph.nodes.each { n ->
    String linkParam

    def objStyles = []
    [VisualStyle.Property.SHAPE, VisualStyle.Property.STYLE, VisualStyle.Property.COLOR].each { p ->
        val = ss.getStyleProperty(n.styleObjectType, p.name)
        if (val != null) {
            objStyles.add("${p.name}=${val}")
        }
    }
    
    // the border style goes into the label html, not the whole node
    String labelStyles = ss.getStyleProperty(n.styleObjectType, "label_styles", "")
    String labelFontStyle = ss.getStyleProperty(n.styleObjectType, "label_fontstyles", "")
   
    if (n.stem) linkParam = "operation=UiV2Stem.viewStem%26stemId=${n.grouperObject.id}"
    else if (n.group) linkParam = "operation=UiV2Group.viewGroup%26groupId=${n.grouperObject.id}"

    String objectTypesRow = ""
    if (n.objectTypeNames != null) {
        objectTypesRow = "<TR><TD ALIGN=\"CENTER\" BORDER=\"1\"><FONT>${n.objectTypeNames.join(", ")}</FONT></TD></TR>"
    }

    String memberCtRow = ""
    if (n.group && ! n.loaderGroup) {
        memberCtRow = "<TR><TD ALIGN=\"CENTER\" BORDER=\"1\"><FONT>${n.allMemberCount} member${n.allMemberCount == 1 ? "" : "s"}, ${n.directMemberCount} direct member${n.directMemberCount == 1 ? "" : "s"}</FONT></TD></TR>"
    }

    dotFile.write "{ \"${n.grouperObject.id}\""
    dotFile.write " [${objStyles.join("; ")}${objStyles.size>0 ? ";":""} label=<<FONT><TABLE BORDER=\"1\" ALIGN=\"CENTER\" CELLBORDER=\"0\" CELLPADDING=\"1\" CELLSPACING=\"0\" ${labelStyles}>${objectTypesRow}<TR><TD ALIGN=\"CENTER\" BORDER=\"1\" WIDTH=\"${8 * n.grouperObject.name.length()}\" TITLE=\"${n.grouperObject.name?:"(Root)"}\" HREF=\"https://localhost/grouper/grouperUi/app/UiV2Main.index?${linkParam}\" ><FONT ${labelFontStyle}>${n.grouperObject.name?:"(Root)"}<BR/></FONT></TD></TR>${memberCtRow}</TABLE></FONT>>] ; }\n"
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

dotFile.write "{ \"Statistics\n${statString}\" [ pos=\"1,5!\" shape=\"rectangle\" style=\"rounded,filled\"; fillcolor=\"navy\" label=<<TABLE BORDER=\"0\" ALIGN=\"LEFT\" CELLBORDER=\"0\" CELLPADDING=\"5\" CELLSPACING=\"0\" WIDTH=\"155\"><TR><TD BALIGN=\"LEFT\"><FONT FACE=\"Courier\" POINT-SIZE=\"16\" COLOR=\"white\">${statStringHtml}</FONT></TD></TR></TABLE>> ]; };\n"
dotFile.write "}\n"

dotFile.close()

def converter = "dot -Tsvg -o ${inFilename}.svg ${inFilename}".execute()
converter.waitFor()
if (converter.exitValue()) {
    println "Error Running svg command -- ${converter.text}"
} else {
    println "Converted to svg in file ${inFilename}.svg"
}
