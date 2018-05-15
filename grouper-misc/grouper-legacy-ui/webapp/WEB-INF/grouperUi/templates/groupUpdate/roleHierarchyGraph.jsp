<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: groupUpdate/roleHierarchyGraph.jsp -->

<div class="section" style="min-width: 900px">

  <grouper:subtitle key="simpleGroupUpdate.roleHierarchyGraphSectionHeader" />

  <div class="sectionBody">
    
    <table class="formTable formTableSpaced" cellspacing="2">
    
    
      <tr class="formTableRow">
        <td class="formTableLeft" style="vertical-align: middle">
          <label for="editActionChange">
            <grouper:message key="simpleGroupUpdate.hierarchies.rolePath" />
          </label>
        </td>
        <td class="formTableRight" style="white-space: nowrap;">
          ${grouper:escapeHtml(groupUpdateRequestContainer.groupToEdit.displayName)}
        </td>
      </tr>
      
    </table>
    <br />
    <br />
    
    <applet code="edu.internet2.middleware.networkGraph.TreeCollapse.class"
        archive="../../grouperExternal/public/networkGraph/jar/networkGraph.jar,../../grouperExternal/public/networkGraph/jar/collections-generic-4.01.jar,../../grouperExternal/public/networkGraph/jar/jung-api-2.0.1.jar,../../grouperExternal/public/networkGraph/jar/jung-algorithms-2.0.1.jar,../../grouperExternal/public/networkGraph/jar/jung-visualization-2.0.1.jar,../../grouperExternal/public/networkGraph/jar/jung-graph-impl-2.0.1.jar,../../grouperExternal/public/networkGraph/jar/colt-1.2.0.jar,../../grouperExternal/public/networkGraph/jar/concurrent-1.3.4.jar"
        width="${mediaMap['directedGraph.width']}" height="${mediaMap['directedGraph.height']}">
      <param name="width" value="${mediaMap['directedGraph.width']}"/>
      <param name="height" value="${mediaMap['directedGraph.height']}"/>
      <param name="text_radial" value="${grouper:escapeJavascript(navMap['directedGraph.radialLayoutToggle'])}"/>
      <param name="text_collapse" value="${grouper:escapeJavascript(navMap['directedGraph.collapseSelectedNode'])}"/>
      <param name="text_expand" value="${grouper:escapeJavascript(navMap['directedGraph.expandSelectedNode'])}"/>
      <param name="text_zoom" value="${grouper:escapeJavascript(navMap['directedGraph.zoom'])}"/>
      <param name="text_picking" value="${grouper:escapeJavascript(navMap['directedGraph.switchToSelectingMode'])}"/>
      <param name="text_transforming" value="${grouper:escapeJavascript(navMap['directedGraph.switchToTransformingMode'])}"/>
      
      <%-- param name="vertex_0" value="Arts and Sciences"/>
      <param name="edgeFrom_0" value="Arts and Sciences"/>
      <param name="edgeTo_0" value="Math"/>
      <param name="edgeFrom_1" value="Arts and Sciences"/>
      <param name="edgeTo_1" value="English"/ --%>
      
      <c:set var="index" value="0" />
      <c:forEach items="${groupUpdateRequestContainer.roleGraphStartingPoints}" var="startingPoint">
        <param name="vertex_${index}" value="${grouper:escapeJavascript(startingPoint)}"/>
        <c:set var="index" value="${index + 1}" />
      </c:forEach>      
      
      <c:set var="index" value="0" />
      <c:forEach items="${groupUpdateRequestContainer.roleGraphNodesFrom}" var="nodeFrom">
        <c:set var="nodeTo" value="${groupUpdateRequestContainer.roleGraphNodesTo[index]}" />
        <param name="edgeFrom_${index}" value="${grouper:escapeJavascript(nodeFrom)}"/>
        <param name="edgeTo_${index}" value="${grouper:escapeJavascript(nodeTo)}"/>
        <c:set var="index" value="${index + 1}" />
      </c:forEach>      
      
    </applet>
    
    
  </div>
</div>

<!-- End: groupUpdate/roleHierarchyGraph.jsp -->
