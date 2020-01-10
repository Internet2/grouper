<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <!-- indexMain.jsp -->
              <div class="bread-header-container">
                <ul class="breadcrumb">
                  <li class="active">${textContainer.text['myGroupsHomeBreadcrumb'] } </li>
                </ul>
                <div class="page-header blue-gradient">
                  <h1>${textContainer.text['grouperAppName']}<br /><small>${textContainer.text['institutionName'] }</small></h1>
                  <p>${textContainer.text['indexGrouperDescription']}</p>
                </div>
              </div>
              <div class="row-fluid">
                <div class="span12">
                  <h3>${textContainer.text['indexRecentActivity']}<!-- Recent activity --></h3>
  
                  <table class="table table-bottom-borders">
                    <thead>
                    	<tr>
                    		<th>${textContainer.text['indexRecentActivityTableHeader']}</th>
                    		<th>${textContainer.text['indexRecentActivityDateTableHeader']}</th>
                    	</tr>
                    </thead>
                    <tbody>
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiAuditEntriesRecentActivity}" var="guiAuditEntry">
  
                        <tr>
                          <td>
                            ${guiAuditEntry.auditLine }
                          <%-- <strong>Added</strong> <a href="#">John Smith</a> as a member of the&nbsp;<a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>&nbsp;group. --%>
                          </td>
                          <td>${guiAuditEntry.guiDate}</td>
                        </tr>                    
                      
                      </c:forEach>
                    </tbody>
                  </table>
  
                  <%--
                  <table class="table table-bottom-borders">
                    <thead></thead>
                    <tbody>
                      <tr>
                        <td><strong>Added</strong> <a href="#">John Smith</a> as a member of the&nbsp;<a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>&nbsp;group.
                        </td>
                        <td>2/1/2013 8:03 AM</td>
                      </tr>
                      <tr>
                        <td><strong>Revoked</strong> <a href="#">Bob Weston</a>'s membership in the&nbsp;<a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Editors</a>&nbsp;group.
                        </td>
                        <td>1/21/2013 9:00 AM</td>
                      </tr>
                      <tr>
                        <td>...</td>
                        <td></td>
                      </tr>
                      <tr>
                        <td><strong>Assigned</strong> the ADMIN privilege to <a href="#">Jane Clivemore</a> in the&nbsp;<a href="#" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.">Senior Editors</a>&nbsp;group.
                        </td>
                        <td>12/20/2012 9:00 AM</td>
                      </tr>
                    </tbody>
                  </table>
                  --%>
                  <div class="row-fluid">     
  
                    <div class="span4 well well-widget">
                      <c:set var="col" value="0" scope="request" />
                      <%@ include file="../index/indexColumnMenu.jsp"%>
                      <div id="indexCol0">
                        <%-- make this a non dynamic include so we can pick the name of the JSP --%>
                        <jsp:include page="../index/index${grouperRequestContainer.indexContainer.panelCol0}.jsp" />
                      </div>
                    </div>
  
                    <div class="span4 well well-widget">
                      <c:set var="col" value="1" scope="request" />
                      <%@ include file="../index/indexColumnMenu.jsp"%>
                      <div id="indexCol1">
                        <%-- make this a non dynamic include so we can pick the name of the JSP --%>
                        <jsp:include page="../index/index${grouperRequestContainer.indexContainer.panelCol1}.jsp" />
                      </div>
                    </div>
  
                    <div class="span4 well well-widget">
                      <c:set var="col" value="2" scope="request" />
                      <%@ include file="../index/indexColumnMenu.jsp"%>
                      <div id="indexCol2">
                        <%-- make this a non dynamic include so we can pick the name of the JSP --%>
                        <jsp:include page="../index/index${grouperRequestContainer.indexContainer.panelCol2}.jsp" />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <!-- end indexMain.jsp -->
              