<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: poc/fileManagerFilesAndFolders.jsp: sections which shows files and folders -->

<div class="section">

  <grouper:subtitle label="Folders and files" />

  <div class="sectionBody">
  
    <c:if test="${!pocFileManagerRequestContainer.hasAllowedFolders}">
      You are not allowed to see any files or folders
    </c:if>
    
    <c:forEach items="${pocFileManagerRequestContainer.allAllowedFolders}" var="folder">
      <grouper:groupBreadcrumb showGrouperTooltips="false" showCurrentLocationLabel="false" showLeafNode="false" groupName="${folder.colonName}" /><br />
      <c:forEach items="${folder.files}" var="file">
        <grouper:groupBreadcrumb showGrouperTooltips="false" showCurrentLocationLabel="false" showLeafNode="false" groupName="${folder.colonName}" />
        <img src="../../grouperExternal/public/assets/images/page_white.png"  class="groupIcon"
        onmouseout="UnTip()" onmouseover="grouperTooltip('File');"/>${fn:escapeXml(file.name)}
        <br />
      </c:forEach>
    </c:forEach>
  </div>
  
</div>
  
<!-- End: poc/fileManagerFilesAndFolders.jsp: main page -->
