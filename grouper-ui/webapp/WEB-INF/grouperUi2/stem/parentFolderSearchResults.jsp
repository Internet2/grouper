<%@ include file="../assetsJsp/commonTaglib.jsp"%>


                    <p>The table below lists folders where you are allowed to create new folders.</p>
                    <table class="table table-hover table-bordered table-striped table-condensed data-table">
                      <thead>
                        <tr>
                          <th class="sorted">Folder Path</th>
                          <th>Folder Name</th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:forEach items="${grouperRequestContainer.stemContainer.guiStemsParentStem}" 
                          var="guiStem" >
                          <%-- tr>
                            <td>Root : Applications</td>
                            <td><i class="icon-folder-close"></i> <a href="#" data-dismiss="modal">Directories</a></td>
                          </tr --%>
                          <tr>
                            <td>${guiStem.pathColonSpaceSeparated}</td>
                            <td><i class="icon-folder-close"></i> <a href="#" data-dismiss="modal">${guiStem.displayExtension }</a></td>
                          </tr>
                        </c:forEach>
                      </tbody>
                    </table>
                    <div class="data-table-bottom clearfix">
                      <div class="pull-right">Showing 1-10 of 25 &middot; <a href="#">First</a> | <a href="#">Prev</a> | <a href="#">Next</a> | <a href="#">Last</a></div>    
                    </div>
                          