<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<!DOCTYPE html>
<html>
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
                    <div class="pull-right">
                      <ul class="nav">
                        <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle"><i class="icon-cog edit-widget dropdown"></i></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li class="nav-header">Select a widget to display</li>
                            <li class="divider"></li>
                            <li><a href="#">Groups I Manage</a></li>
                            <li><a href="#">My Services</a></li>
                            <li><a href="#">My Folders</a></li>
                            <li><a href="#">My Memberships</a></li>
                            <li><a href="#">Recently Viewed Entities</a></li>
                          </ul>
                        </li>
                      </ul>
                    </div>
                    <h4>My Favorites</h4>
                    
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsMyFavorites}" var="guiGroup">
                        <li>
                          ${guiGroup.shortLinkWithIconAndPath}
                        </li>
                      
                      
                      </c:forEach>
                      
                    </ul>
                    
                    
                    <ul class="unstyled list-widget">
                      <li><a href="view-group.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."><i class="icon-group"></i> Admins</a><br/><small class="indent">Root : Applications : Wiki</small>
                      </li>
                      <li><a href="view-group.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."><i class="icon-group"></i> Editors</a><br/><small class="indent">Root : Applications : Wiki</small>
                      </li>
                      <li><a href="view-group.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."><i class="icon-group"></i> Senior Editors</a><br/><small class="indent">Root : Applications : Wiki</small>
                      </li>
                      <li><a href="view-folder.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."><i class="icon-folder-close"></i> Wiki</a><br/><small class="indent">Root : Applications</small>
                      </li>
                      <li><a href="view-folder.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."><i class="icon-folder-close"></i> Reference Groups</a><br/><small class="indent">Root</small>
                      </li>
                      <li><a href="view-person.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;Subject Name&lt;/strong&gt;&lt;br /&gt;Subject identifier"><i class="icon-user"></i> Abbott, Jane</a>
                      </li>
                      <li><a href="view-person.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;Subject Name&lt;/strong&gt;&lt;br /&gt;Subject identifier"><i class="icon-user"></i> Bartlett, Jim</a>
                      </li>
                      <li><a href="view-person.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;Subject Name&lt;/strong&gt;&lt;br /&gt;Subject identifier"><i class="icon-user"></i> Danielson, Mary</a>
                      </li>
                      <li><a href="view-person.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;Subject Name&lt;/strong&gt;&lt;br /&gt;Subject identifier"><i class="icon-user"></i> Smith, Michael</a>
                      </li>
                      <li><a href="view-person.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" title="&lt;strong&gt;Subject Name&lt;/strong&gt;&lt;br /&gt;Subject identifier"><i class="icon-user"></i> Sunny, Daniel</a>
                      </li>
                    </ul>
                    <p><strong><a href="my-favorites.html">View all favorites</a></strong></p>
                  </div>
                  <div class="span4 well well-widget">
                    <div class="pull-right">
                      <ul class="nav">
                        <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle"><i class="icon-cog edit-widget dropdown"></i></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li class="nav-header">Select a widget to display</li>
                            <li class="divider"></li>
                            <li><a href="#">Groups I Manage</a></li>
                            <li><a href="#">My Services</a></li>
                            <li><a href="#">My Folders</a></li>
                            <li><a href="#">My Memberships</a></li>
                            <li><a href="#">Recently Viewed Entities</a></li>
                          </ul>
                        </li>
                      </ul>
                    </div>
                    <h4>Groups I Manage</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiGroupsUserManagesAbbreviated}" var="guiGroup">
                        <li>
                        ${guiGroup.shortLinkWithIconAndPath }
                        </li>
                      
                      
                      </c:forEach>
                      
                    </ul>
                    <p><strong><a href="my-groups.html">View all groups</a>  </strong></p>
                  </div>
                  <div class="span4 well well-widget">
                    <div class="pull-right">
                      <ul class="nav">
                        <li class="dropdown"><a data-toggle="dropdown" href="#" class="dropdown-toggle"><i class="icon-cog edit-widget dropdown"></i></a>
                          <ul class="dropdown-menu dropdown-menu-right">
                            <li class="nav-header">Select a widget to display</li>
                            <li class="divider"></li>
                            <li><a href="#">Groups I Manage</a></li>
                            <li><a href="#">My Services</a></li>
                            <li><a href="#">My Folders</a></li>
                            <li><a href="#">My Memberships</a></li>
                            <li><a href="#">Recently Viewed Entities</a></li>
                          </ul>
                        </li>
                      </ul>
                    </div>
                    <h4>My Services</h4>
                    <ul class="unstyled list-widget">
                      <c:forEach items="${grouperRequestContainer.indexContainer.guiAttributeDefNamesMyServices}" var="guiAttributeDefName">
                        <%-- TODO work on this, should be from text file, and by attr def name --%>
                        <li><a href="view-group.html" rel="tooltip" data-html="true" data-delay-show='200' data-placement="right" 
                          <%-- &lt;strong&gt;FOLDER:&lt;/strong&gt;&lt;br /&gt;Full : Path : To : The : Entity&lt;br /&gt;&lt;br /&gt;This is the description for this entity. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. --%>
                          title="${grouper:escapeHtml(guiAttributeDefName.title)}"><i class="icon-group"></i> ${grouper:escapeHtml(guiAttributeDefName.attributeDefName.displayExtension) }</a><br/><small class="indent">${grouper:escapeHtml(guiAttributeDefName.pathColonSpaceSeparated) }</small>
                        </li>
                      </c:forEach>
                    </ul>
                    <p><strong><a href="my-services.html">View all services</a></strong></p>
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
</html>