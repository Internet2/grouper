<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: poc/fileManagerEditPanel.jsp: edit panel of file manager -->
        <c:choose>
          <c:when test="${pocFileManagerRequestContainer.hasAllowedCreateFolders}">
            <table class="formTable formTableSpaced" cellspacing="2" style="margin-top: 0px; margin-bottom: 0px">
              <tr class="formTableRow">
                <td class="formTableLeft" style="vertical-align: middle">
                  Containing folder name:
                </td>
                <td class="formTableRight">
                  <select name="folderId"  >
                    <c:forEach items="${pocFileManagerRequestContainer.allowedCreateFolders}" var="folder">
                      <option value="${folder.id}">${fn:escapeXml(folder.grouperDisplayExtension)}</option>
                    </c:forEach>
                  </select>
                </td>
              </tr>
              <tr class="formTableRow">
                <td class="formTableLeft" style="vertical-align: middle">
                  Object name:
                </td>
                <td class="formTableRight">
                  <input name="objectName" type="text" />
                </td>
              </tr>
              <tr>
                <td colspan="2">
            
                  <input class="blueButton" type="submit" 
                    onclick="ajax('../app/PocFileManager.createFolder', {formIds: 'fileManagerFormId'}); return false;" 
                    value="Create folder" style="margin-top: 2px" />
      
                  <input class="blueButton" type="submit" 
                    onclick="ajax('../app/PocFileManager.createFile', {formIds: 'fileManagerFormId'}); return false;" 
                    value="Create file" style="margin-top: 2px" />
      
                 <input class="blueButton" type="submit" 
                    onclick="window.open('grouper.html?operation=SimplePermissionUpdate.assignInit', 'grouperPermissionsWindow', 'scrollbars,resizable,menubar=yes,titlebar=yes,toolbar=yes'); return false;" 
                    value="Manage permissions" style="margin-top: 2px" />
      
               </td>
              </tr>
             </table>            
          </c:when>
          <c:otherwise>
            <table class="formTable formTableSpaced" cellspacing="2" style="margin-top: 0px; margin-bottom: 0px">
              <tr>
                <td colspan="2">You are not allowed to create files or folders</td>
              </tr>
            </table>          
          </c:otherwise>
        
        </c:choose>
<!-- End: poc/fileManagerEditPanel.jsp: main page -->
        