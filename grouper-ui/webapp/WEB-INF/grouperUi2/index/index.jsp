<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<!DOCTYPE html>
<html>
  <!-- start index.jsp -->
  <head><title>Grouper UI v2.2</title>
  <%@ include file="../assetsJsp/commonHead.jsp"%>
  </head>
  <body class="full">
    <div class="top-container">
      <div class="navbar navbar-static-top">
        <div class="navbar-inner">
          <div class="container-fluid"><a href="index.html"><img class="brand" src="../../${mediaMap['image.organisation-logo']}" alt="Logo" /></a>
            <div class="pull-right">
              <form action="search-results.html" class="navbar-search">
                <input type="text" placeholder="Search" class="search-query"><i class="icon-search"></i>
              </form>
            </div>
            <div class="navbar-text pull-right">Logged in as <a href="view-person.html" class="navbar-link">${guiSettings.loggedInSubject.screenLabel}</a> &middot; <a href="#" class="navbar-link">Log out</a> &middot; <a href="#" class="navbar-link">Help</a></div>
          </div>
        </div>
      </div>
      <div class="container-fluid">
        <%-- div id="messaging" class="row-fluid">
          <div class="alert alert-success">
            <button type="button" data-dismiss="alert" class="close">&times;</button>This is an example of a confirmation message. Click the "x" to dismiss this message.
          </div>
        </div --%>
        <div class="row-fluid">
          <div class="span9 main-content offset3">
            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li class="active">Home </li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>Grouper<br /><small>Institute of Higher Education</small></h1>
                <p>This website allows you to manage groups associated with your organization and the members of those groups. For a list of answers to frequently asked questions, refer to the <a href="#">support documentation</a>.</p>
              </div>
            </div>
            <div class="row-fluid">
              <div class="span12">
                <h3>${textContainer.text['indexRecentActivity']}<!-- Recent activity --></h3>

                <table class="table table-bottom-borders">
                  <thead></thead>
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
                      <%@ include file="../index/indexMyFavorites.jsp"%>
                    </div>
                  </div>

                  <div class="span4 well well-widget" id="indexCol1">
                    <c:set var="col" value="1" scope="request" />
                    <%@ include file="../index/indexColumnMenu.jsp"%>
                    <div id="indexCol1">
                      <%@ include file="../index/indexGroupsImanage.jsp"%>
                    </div>
                  </div>

                  <div class="span4 well well-widget" id="indexCol2">
                    <c:set var="col" value="2" scope="request" />
                    <%@ include file="../index/indexColumnMenu.jsp"%>
                    <div id="indexCol2">
                      <%@ include file="../index/indexMyServices.jsp"%>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="span3 left-column">
            <div class="btn-group btn-group-create"><a href="new-group.html" class="btn btn-bigger btn-create"><i class="icon-plus"></i> Create new group</a><a data-toggle="dropdown" class="btn btn-bigger btn-create dropdown-toggle"><span class="caret"></span></a>
              <ul class="dropdown-menu dropdown-menu-right">
                <li><a href="new-folder.html">Create new folder</a></li>
                <li><a href="new-group.html">Create new group</a></li>
                <li><a href="invite-external-users.html">Invite external users</a></li>
                <li class="divider"></li>
                <li><a href="bulk-add.html">Add members to a group</a></li>
              </ul>
            </div>
            <div class="leftnav-accordions">
              <button type="button" data-toggle="collapse" data-target="#demo2" class="btn btn-block btn-grouper first">Quick Links<i class="icon-plus"></i><i class="icon-minus"></i></button>
              <div id="demo2" class="collapse in">
                <div class="accordion-inner">
                  <ul class="nav nav-list">
                    <li><a href="my-groups.html">My Groups</a></li>
                    <li><a href="my-folders.html">My Folders</a></li>
                    <li><a href="my-favorites.html">My Favorites</a></li>
                    <li><a href="my-services.html">My Services</a></li>
                  </ul>
                </div>
              </div>
              <button type="button" class="btn btn-block btn-grouper last">Browse Folders</button>
              <div class="accordion-inner">
                <div id="tree1" class="explore-tree"></div>
              </div>
            </div>
          </div>
        </div>
        <hr>
        <footer>
          <p>&copy; Institute of Higher Education</p>
        </footer>
      </div>
    </div>
    <%@ include file="../assetsJsp/commonBottom.jsp"%>
  </body>
  <!-- end index.jsp -->
</html>