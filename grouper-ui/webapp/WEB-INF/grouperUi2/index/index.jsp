<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<!DOCTYPE html>
<html>
  <!-- start index.jsp -->
  <head><title>Grouper UI v2.2</title>
  <%@ include file="../assetsJsp/commonHead.jsp"%>
  </head>
  <body class="full claro">
    <div class="top-container">
      <div class="navbar navbar-static-top">
        <div class="navbar-inner">
          <div class="container-fluid"><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');"><img class="brand" src="../../${mediaMap['image.organisation-logo']}" alt="Logo" /></a>
            <div class="pull-right">
              <form id="searchForm" action="#" onsubmit="return guiV2link('operation=UiV2Main.searchSubmit', {optionalFormElementNamesToSend: 'searchQuery'});" class="navbar-search">
                <input type="text" name="searchQuery" placeholder="${textContainer.textEscapeXml['searchPlaceholder']}" class="search-query"><a href="#" 
                  onclick="return guiV2link('operation=UiV2Main.searchSubmit', {optionalFormElementNamesToSend: 'searchQuery'});"><i class="fa fa-search"></i></a>
              </form>
            </div>
            <div class="navbar-text pull-right">Logged in as <a href="view-person.html" class="navbar-link">${guiSettings.loggedInSubject.screenLabel}</a> &middot; <a href="#" class="navbar-link">Log out</a> &middot; <a href="#" class="navbar-link">Help</a></div>
          </div>
        </div>
      </div>
      <div class="container-fluid">
        <div id="messaging" class="row-fluid">
          <%-- this is where messages go --%>
        </div>
        <div class="row-fluid">
          <div class="span9 main-content offset3">
            <!-- this is the main content div where the page content goes via ajax -->
            <div id="grouperMainContentDivId">
            </div>
            <!-- end of the main content div where the page content goes -->
          </div>
          <div class="span3 left-column">
            <div class="btn-group btn-group-create"><a href="#" 
              onclick="return guiV2link('operation=UiV2Group.newGroup', {optionalFormElementNamesToSend: 'objectStemId'});"
              class="btn btn-bigger btn-create"><i class="fa fa-plus"></i> ${textContainer.text['groupNewCreateNewGroupMenuButton'] }</a><a data-toggle="dropdown" class="btn btn-bigger btn-create dropdown-toggle"><span class="caret"></span></a>
              <ul class="dropdown-menu dropdown-menu-right">
                <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2Stem.newStem', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['stemNewCreateNewStemMenuButton'] }</a></li>
                <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2Group.newGroup', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['groupNewCreateNewGroupMenuButton'] }</a></li>
                <li><a href="invite-external-users.html">Invite external users</a></li>
                <li class="divider"></li>
                <li><a href="bulk-add.html">Add members to a group</a></li>
              </ul>
            </div>
            <div class="leftnav-accordions">
              <button type="button" data-toggle="collapse" data-target="#demo2" class="btn btn-block btn-grouper first">${textContainer.text['indexQuickLinksLabel']}<i class="fa fa-plus"></i><i class="fa fa-minus"></i></button>
              <div id="demo2" class="collapse in">
                <div class="accordion-inner">
                  <ul class="nav nav-list">
                    <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2MyGroups.myGroups');">${textContainer.text['indexMyGroupsButton'] }</a></li>
                    <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2MyStems.myStems');">${textContainer.text['indexMyStemsButton'] }</a></li>
                    <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2Main.myFavorites');">${textContainer.text['indexMyFavoritesButton'] }</a></li>
                    <li><a href="#" 
                  onclick="return guiV2link('operation=UiV2Main.myServices');">${textContainer.text['indexMyServicesButton'] }</a></li>
                    <li><a href="../../populateAllGroups.do">${textContainer.text['ui-lite.fromInvite-admin-link'] }</a></li>
                    <li><a href="../../grouperUi/appHtml/grouper.html?operation=Misc.index">${textContainer.text['ui-lite.fromInvite-link'] }</a></li>
                  </ul>
                </div>
              </div>
              <button type="button" class="btn btn-block btn-grouper last">Browse Folders</button>
              <div class="accordion-inner">
                <script>
                  $(document).ready(function(){
                    folderMenuStore = dojo.store.JsonRest({
                      target:"UiV2Main.folderMenu?",
                      mayHaveChildren: function(object){
                        // see if it has a children property
                        return "children" in object;
                      },
                      getChildren: function(object, onComplete, onError){
                        // retrieve the full copy of the object
                        this.get(object.id).then(function(fullObject){
                          // copy to the original object so it has the children array as well.
                          object.children = fullObject.children;
                          // now that full object, we should have an array of children
                          onComplete(fullObject.children);
                        }, function(error){
                          // an error occurred, log it, and indicate no children
                          console.error(error);
                          onComplete([]);
                        });
                      },
                      getRoot: function(onItem, onError){
                        // get the root object, we will do a get() and callback the result
                        this.get("root").then(onItem, onError);
                      },
                      getLabel: function(object){
                        // just get the name
                        return object.name;
                      }
                      
                    });

                    // Custom TreeNode class (based on dijit.TreeNode) that allows rich text labels
                    //var MyTreeNode = dojo.declare(dijit.Tree._TreeNode, {
                    //    _setLabelAttr: {node: "labelNode", type: "innerHTML"}
                    //});
                    
                    folderTree = new dijit.Tree({
                      model: folderMenuStore,
                      //_createTreeNode: function(args){
                      //   return new MyTreeNode(args);
                      //},
                      getIconClass: function(/*dojo.store.Item*/ item, /*Boolean*/ opened){
                        //return (!item || this.model.mayHaveChildren(item)) ? (opened ? "dijitFolderOpened" : "dijitFolderClosed") : "dijitLeaf"
                        if (!item || this.model.mayHaveChildren(item)) {
                          if (opened) {
                            return "dijitFolderOpened";
                          } 
                          return "dijitFolderClosed";
                        }
                        if (item.theType == 'group') {
                          //font-awesome icons...
                          return "fa fa-group";
                        }
                        if (item.theType == 'attributeDef') {
                          //font-awesome icons...
                          return "fa fa-cog";
                        }
                        if (item.theType == 'attributeDefName') {
                          //font-awesome icons...
                          return "fa fa-cogs";
                        }
                      },
                      onClick: function(item){
                        // Get the URL from the item, and navigate to it
                        if (item.theType == 'stem') {
                          guiV2link('operation=UiV2Stem.viewStem&stemId=' + item.id);                          
                        } else if (item.theType == 'group') {
                          guiV2link('operation=UiV2Group.viewGroup&groupId=' + item.id);                          
                        } else if (item.theType == 'attributeDef') {
                        } else if (item.theType == 'attributeDefName') {
                        } else {
                          alert('ERROR: cant find theType on object with id: ' + item.id);
                        }
                      }
                    }, "folderTree"); // make sure you have a target HTML element with this id
                    folderTree.startup();
                  });
                
                </script>
              
                <div id="folderTree"></div>
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