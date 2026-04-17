//TODO put this in nav.properties and put base HTML pages into JSP
var guiAjaxSessionProblem = "There was an error communicating with the server.  Your session probably expired.  You will be redirected to login again.";

function guiRoundCorners() {
  //round those corners
  //IE messes up
//  if (!jQuery.browser.msie) {
//    Nifty("div.sectionBody", "bottom");   
//    Nifty("div.sectionHeader", "top");   
    //this leaves a white line for some reason...
    //Nifty("div#navbar"); 
//  }  
}

$(document).ready(function(){

  var theJavascriptMessage = document.getElementById('javascriptMessage');
  
  if (!guiIsEmpty(theJavascriptMessage)) {
    theJavascriptMessage.style.display = 'none'; 
  }

  guiRoundCorners();
  
  // Initialize history plugin.
  // The callback is called at once by present location.hash. 

  var urlArgObjectMap = allObjects.appState.urlArgObjectMap();

  if (location.href.indexOf('/UiV2') == -1) {

    processUrl();  
    
    if (typeof urlArgObjectMap.operation == 'undefined' && !guiIsEmpty(location.href) && location.href.indexOf("/test/") != -1) {
      //nothing
      return;
    }
  } else {
    
    History.Adapter.bind(window,'statechange',function(){ 

      var State = History.getState(); // Note: We are using History.getState() instead of event.state
      
      if (typeof State.data != 'undefined' && State.data != null
          && typeof State.data.handleStateInitially != undefined && State.data.handleStateInitially == false ) {
        
        //null this out for next time
        State.data.handleStateInitially = null;
        return;
      }

      // State.hash is /grouper/grouperUi/app/UiV2Main.index?operation=UiV2Main.indexMain
      //alert(State.hash);
      guiProcessUrlForAjax(State.hash);

    });

    //if(location.href);
    //UiV2Main.index
    //urlArgObjectMap.operation
    if (typeof urlArgObjectMap.operation == 'undefined') {
      urlArgObjectMap.operation = 'UiV2Main.indexMain';
      History.pushState(null, null, "?operation=" + urlArgObjectMap.operation);
    } else {
      guiProcessUrlForAjax(location.href);
      guiScrollTop();
    }
    
  }
  
});

/**
 * alternate background colors of visible rows of a table
 */
function guiStripeTable(jquerySelectorOfTable) {
  
  var rowIndex = 0;
  $(jquerySelectorOfTable + " tr:visible").not(".grouperIgnoreStripe").each(function() {
    if (rowIndex % 2 == 1) {
      $(this).removeClass('grouperTableRowOdd').addClass('grouperTableRowEven');
    } else {
      $(this).removeClass('grouperTableRowEven').addClass('grouperTableRowOdd');
    }
    rowIndex++;
  });
}
/**
 * go to a url, e.g. operation=UiV2Group.viewGroup&groupId=abc123
 * @param url
 */
function guiV2link(url, options) {

  $('.messaging').hide().empty();

  if (typeof options == 'undefined') {
    options = {};
  }
  
  if (!options.dontScrollTop) {
    guiScrollTop();
  }
  url = '?' + url;  
  // http://localhost:8097/grouper/grouperUi/app/UiV2Main.indexCustomUi?operation=UiV2Group.viewGroup&groupId=61bcaad67d57438ab1fea11c426c2f64
  var browserUrl = location.href;
  var servletIndex = browserUrl.indexOf("/app/");
  
  var navigate = false;
  
  if (servletIndex != -1) {
    servletIndex += "/app/".length;
    var servlet = browserUrl.substring(servletIndex, browserUrl.length);

    var questionIndex = servlet.indexOf("?");
    if (questionIndex > -1) {
      servlet = servlet.substring(0, questionIndex);
    }
    if (servlet.includes("UiV2Main.indexCustomUi") && !url.includes("operation=UiV2CustomUi.")) {
      url = "UiV2Main.index" + url;
      navigate = true;
    }
    if (!servlet.includes("UiV2Main.indexCustomUi") && url.includes("operation=UiV2CustomUi.")) {
      url = "UiV2Main.indexCustomUi" + url;
      navigate = true;
    }
  }

  url += _addUrlOptions(url, options);

  if (navigate) {
    window.location.href = url;
    return false;
  }
  var handleStateChangeInitially = true;

  var stateObj = { };

  if (typeof options.handleStateInitially != 'undefined' && options.handleStateInitially == false ) {
    stateObj.handleStateInitially = false;
  }
  
  History.pushState(stateObj, null, url);

  //return false so the browser navigate
  return false;
}

/* Used for button clicks, add extra parameters to the url */
function _addUrlOptions(url, options) {
  var result = "";

  options = options || {};

  // Historically this code uses the key optionalFormElementNamesToSend.
  // (Some call sites may pass optionalFormElementNames; accept both.)
  var namesCsv = options.optionalFormElementNamesToSend || options.optionalFormElementNames;
  if (namesCsv) {

    var additionalFormElementNamesArray = guiSplitTrim(namesCsv, ",");
  
    for (var i = 0; i < additionalFormElementNamesArray.length; i++) {
      var name = additionalFormElementNamesArray[i];
  
      // It's ok if it is not there
      var nodes = document.getElementsByName(name);
      if (!nodes || nodes.length === 0 || !nodes[0]) continue;
  
      // Use the first element as the "representative" field.
      // guiFieldValues will aggregate for radio/checkbox groups by name.
      var valueOrValues = guiFieldValues(nodes[0]);
  
      // If nothing selected for checkbox/radio/multi-select, guiFieldValues returns []
      if (Array.isArray(valueOrValues)) {
        if (valueOrValues.length === 0) continue;
  
        for (var j = 0; j < valueOrValues.length; j++) {
          result += (url.indexOf("?") === -1 && result === "") ? "?" : "&";
          result += encodeURIComponent(name) + "=" + encodeURIComponent(valueOrValues[j]);
        }
      } else {
        // Skip null/undefined/empty-string if you want (matching old behavior loosely)
        if (valueOrValues === null || typeof valueOrValues === "undefined") continue;
  
        result += (url.indexOf("?") === -1 && result === "") ? "?" : "&";
        result += encodeURIComponent(name) + "=" + encodeURIComponent(valueOrValues);
      }
    }
  }
  return result;
}


/**
 * take a url for ajax with an operation=Something.else and call ajax with it
 * @param url
 */
function guiProcessUrlForAjax(url) {

  //clear the error div(2)...
  $('#messaging').hide().empty();
  $('.messaging').hide().empty();
  
  var poundIndex = url.indexOf("?");
  if (poundIndex == -1) {
    poundIndex = url.indexOf("#");
    if (poundIndex == -1) {
      //alert('cant find opreation! ' + State.hash);
      return;
    }
  }
  var poundString = url.substring(poundIndex + 1, url.length);
  
  var args = guiSplitTrim(poundString, "&");
  var ajaxUrl = '../app/';
  var foundOperation = false;
  for (var i=0;i<args.length;i++) {

    //split by =
    var equalsIndex = args[i].indexOf("=");
    if (equalsIndex == -1) {
      return allObjects.appState.urlArgObjects;
    }
    var key = args[i].substring(0,equalsIndex);
    var value = args[i].substring(equalsIndex+1,args[i].length);
    if (key == 'operation') {
      ajaxUrl += value;
      if (args.length > 1) {
        ajaxUrl += '?';
      }
      foundOperation = true;
      
      //if this is a public operation, then replace the part of the path to make it public
      if (guiStartsWith(ajaxUrl, '../app/UiV2Public.')) {
        ajaxUrl = guiReplaceString(ajaxUrl, '../app/UiV2Public.', '../public/UiV2Public.');
      }
      
    } else {
      ajaxUrl += key + '=' + value;
      if (i < args.length-1) {
        ajaxUrl += '&';
      }
    }
  }
  if (foundOperation) {
    ajax(ajaxUrl);    
  }

}

function guiScrollTop() {
  window.scrollTo(0,0);
  window.scroll(0,0);
  if (document.all){
    document.body.scrollLeft = 0;
    document.body.scrollTop = 0;
  } else{
    window.pageXOffset = 0;
    window.pageYOffset = 0;
  }
}

/**
 * add a success message to top
 * @param message
 */
function guiMessageSuccess(message) {
  guiMessageHelper('success', message);
}

/**
 * add am info message to top
 * @param message
 */
function guiMessageInfo(message) {
  guiMessageHelper('info', message);
}

/**
 * add an error message to top
 * @param message
 */
function guiMessageError(message) {
  guiMessageHelper('error', message);
}

/**
 * add a message to the ui v2 screen
 * @param messageType must be success, info, error
 * @param message the escaped message for the screen, or could be HTML
 */
function guiMessageHelper(messageType, message, shouldEmpty=true) {
  
  if (messageType != 'success' && messageType != 'info' && messageType != 'error') {
    alert('messageType must be success, info, or error: ' + messageType);
  }
  
  var finalMessage = '<div role="alert" class="alert alert-' + messageType 
    + '"><button type="button" class="close" data-dismiss="alert" aria-label="Close">&times;</button>'
    + '<span class="messageText">' + message + '</span></div>';
  $('#messaging').hide();
  if (shouldEmpty) {
    $('#messaging').empty();
    $('#messaging').append(finalMessage).slideDown('slow');
  } else {
    $('#messaging').append(finalMessage).show();
  }
  $('#messaging').focus();

}

/** sees if input ends with ending */
function guiEndsWith(input, ending) {
  if (guiIsEmpty(input) || guiIsEmpty(ending)) {
    return false;
  }
  var inputString = "" + input;
  var lastIndex = inputString.lastIndexOf(ending);
  return lastIndex == input.length - ending.length;

}


/** replace html in an element with a template (substituted).  
  jqueryKey e.g. #topDiv
  templateName (replace slashes with dots) e.g. common.commonTop.html */
function replaceHtmlWithTemplate(jqueryKey, templateName) {

  var template = allObjects.guiSettings.templates[templateName];
  if (typeof template == 'undefined') {
    alert("Error: cant find template: " + templateName);
  }
  var html = template.process(allObjects);
  
  $(guiEscapeSelectorIfNeeded(jqueryKey)).html(html);
  
}

function processUrl() {
  
  //operation%3DsimpleUpdate%26groupName%3Dtest%253Atest1
  //operation=simpleUpdate&groupName=test:test1
  //http://localhost:8089/grouperWs/grouperUi/appHtml/grouper.html#operation%3DsimpleMembershipUpdate%26groupName%3Dtest%253Atest1
  
  //map of url args
  var urlArgObjectMap = allObjects.appState.urlArgObjectMap();
  
  if (typeof urlArgObjectMap.operation == 'undefined') {
    $("#bodyDiv").html = "";
    //alert("invalid URL, no operation");
  } else {
    var ajaxUrl = '../app/' + urlArgObjectMap.operation;

    if (typeof urlArgObjectMap.membershipLiteName != 'undefined' && ajaxUrl.indexOf('membershipLiteName=') == -1) {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "membershipLiteName=" +  urlArgObjectMap.membershipLiteName;
    }
    if (typeof urlArgObjectMap.groupId != 'undefined' && ajaxUrl.indexOf('groupId=') == -1) {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "groupId=" +  urlArgObjectMap.groupId;
    }
    if (typeof urlArgObjectMap.groupName != 'undefined' && ajaxUrl.indexOf('groupName=') == -1) {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "groupName=" +  urlArgObjectMap.groupName;
    }
    if (typeof urlArgObjectMap.subjectPickerName != 'undefined' && ajaxUrl.indexOf('subjectPickerName=') == -1) {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "subjectPickerName=" +  urlArgObjectMap.subjectPickerName;
    }
    if (typeof urlArgObjectMap.subjectPickerElementName != 'undefined' && ajaxUrl.indexOf('subjectPickerElementName=') == -1) {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "subjectPickerElementName=" +  urlArgObjectMap.subjectPickerElementName;
    }
    if (typeof urlArgObjectMap.attributeDefNamePickerName != 'undefined'  && ajaxUrl.indexOf('attributeDefNamePickerName=') == -1) {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "attributeDefNamePickerName=" +  urlArgObjectMap.attributeDefNamePickerName;
    }
    if (typeof urlArgObjectMap.attributeDefNamePickerElementName != 'undefined' && ajaxUrl.indexOf('attributeDefNamePickerElementName=') == -1) {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "attributeDefNamePickerElementName=" +  urlArgObjectMap.attributeDefNamePickerElementName;
    }
    if (typeof urlArgObjectMap.externalSubjectInviteId != 'undefined' && ajaxUrl.indexOf('externalSubjectInviteId=') == -1) {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "externalSubjectInviteId=" +  urlArgObjectMap.externalSubjectInviteId;
    }
    if (typeof urlArgObjectMap.externalSubjectInviteName != 'undefined' && ajaxUrl.indexOf('externalSubjectInviteName=') == -1) {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "externalSubjectInviteName=" +  urlArgObjectMap.externalSubjectInviteName;
    }
    ajax(ajaxUrl);
  }
}

/** object represents state of application */
function AppState() {

  /** if the app is initted or not */
  this.initted = false;

  /** if the simple membership update is initted */
  this.inittedSimpleMembershipUpdate = false
  
  this.urlInCache = null;
  
  /** dont access this directly, access with method: urlArgObjectMap() */
  this.urlArgObjects = null;
  
  /** map of hide shows by name */
  this.hideShows = {};
  
  /** map of pagers by name */
  this.pagers = {};
  
  /** function to get the map of url args
   * note we need a placeholder for this function in the Java AppState object which gets null and sets an object (JSON function)
   */
  this.urlArgObjectMap = function() {
    //see if up to date
    if (allObjects.appState.urlInCache == location.href) {
      return allObjects.appState.urlArgObjects;
    }
    var argObject = new Object();
    allObjects.appState.urlArgObjects = argObject;  

    //lets get url
    var url = location.href;
    var poundIndex = url.indexOf("?");
    if (poundIndex == -1) {
      poundIndex = url.indexOf("#");
      if (poundIndex == -1) {
        return allObjects.appState.urlArgObjects;
      }
    }
    var poundString = url.substring(poundIndex + 1, url.length);
    
    //not sure why this is here... it should decode after splitting out the ampersands
    //poundString = URLDecode(poundString);
    
    //split out by ampersand
    var args = guiSplitTrim(poundString, "&");
    for (var i=0;i<args.length;i++) {
      //split by =
      var equalsIndex = args[i].indexOf("=");
      if (equalsIndex == -1) {
        return allObjects.appState.urlArgObjects;
      }
      var key = args[i].substring(0,equalsIndex);
      var value = args[i].substring(equalsIndex+1,args[i].length);
      argObject[URLDecode(key)] = URLDecode(value);
      //alert(URLDecode(key) + " -> " + URLDecode(value));
    }
    allObjects.appState.urlInCache = url;
    
    return allObjects.appState.urlArgObjects;
  };

}

/** starting point for all objects */
function AllObjects() {
  /** when app is initted, this is the GuiSettings bean which has params, text, templates, etc */
  this.guiSettings = null;

  this.appState = new AppState();

  this.simpleMembershipUpdate = null;

  /** function to lazy load the simple membership update object */
  this.simpleMembershipUpdateObj = function() {
    if (allObjects.simpleMembershipUpdate == null) {
      //clear out all
      allObjects.clearActionObjects();
      allObjects.simpleMembershipUpdate = new SimpleMembershipUpdate();
    }
    return allObjects.simpleMembershipUpdate;
    
  };

  /** clear out the action objects */
  this.clearActionObjects = function() {
    allObjects.simpleMembershipUpdate = null;
  };

}

/** starting point for all objects */
var allObjects = new AllObjects();

/**
 * decoreate a url to add state
 * @param url
 * @return the url
 */
function guiDecorateUrl(theUrl) {
  var urlArgObjectMap = allObjects.appState.urlArgObjectMap();

  if (typeof urlArgObjectMap.groupId != 'undefined' && theUrl.indexOf('groupId=') == -1) {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "groupId=" +  urlArgObjectMap.groupId;
  }
  if (typeof urlArgObjectMap.groupName != 'undefined' && theUrl.indexOf('groupName=') == -1) {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "groupName=" +  urlArgObjectMap.groupName;
  }
  if (typeof urlArgObjectMap.membershipLiteName != 'undefined' && theUrl.indexOf('membershipLiteName=') == -1) {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "membershipLiteName=" +  urlArgObjectMap.membershipLiteName;
  }
  if (typeof urlArgObjectMap.attributeDefIdForFilter != 'undefined' && theUrl.indexOf('attributeDefIdForFilter=') == -1) {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "attributeDefIdForFilter=" +  urlArgObjectMap.attributeDefIdForFilter;
  }
  if (typeof urlArgObjectMap.attributeDefId != 'undefined' && theUrl.indexOf('attributeDefId=') == -1) {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "attributeDefId=" +  urlArgObjectMap.attributeDefId;
  }
  if (typeof urlArgObjectMap.attributeDefNameId != 'undefined' && theUrl.indexOf('attributeDefNameId=') == -1) {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "attributeDefNameId=" +  urlArgObjectMap.attributeDefNameId;
  }
  return theUrl;
}


function dojoInitMenu(autoSelectNode) {

  // Guardrails: if jQuery/jsTree aren't present, bail out quietly.
  if (!window.jQuery || !jQuery.fn || typeof jQuery.fn.jstree !== 'function') {
    return;
  }


    // Recreate #folderTree if it was destroyed
  var folderTreeDiv = document.getElementById('folderTree');
  if (!folderTreeDiv) {
    $('#folderTreeContainerId').append('<div id="folderTree"></div>');
  }

  // Always re-select after potential append
  var $treeEl = $('#folderTree');

  // ---- Cleanup previous tree instance + handlers (safe to call repeatedly) ----
  // 1) Destroy any existing jsTree instance
  if ($treeEl.data('jstree')) {
    try { $treeEl.jstree('destroy'); } catch (e) {}
  }
  // 2) Remove any event handlers we added in prior dojoInitMenu runs
  $treeEl.off('.dojoInitMenu');
  // 3) Clear leftover DOM from previous render (destroy usually does this, but this is extra-safe)
  $treeEl.empty();


  function getIcon(item) {
    // Folder-ish nodes
    var hasChildrenMarker = (item && Object.prototype.hasOwnProperty.call(item, 'children'));
    if (!item || hasChildrenMarker || item.theType === 'stem' || item.root) {
      // you can swap this for your own CSS class if you had dijitFolderClosed
      return 'jstree-folder';
    }

    // Leaf types
    // font awesome icons dont show in jstree for some reason
    // so dont show any icons and just have the icon in the label
    if (item.theType === 'truncatedItems') return 'grouper-jstree-no-icon';
    if (item.theType === 'group') return 'grouper-jstree-no-icon';
    if (item.theType === 'entity') return 'grouper-jstree-no-icon';
    if (item.theType === 'attributeDef') return 'grouper-jstree-no-icon';
    if (item.theType === 'attributeDefName') return 'grouper-jstree-no-icon';

    return 'jstree-file';
  }

  function hasChildren(item) {
    // Dojo used: "children" in object
    if (!item) return false;

    if (Array.isArray(item.children)) return item.children.length > 0;

    // some APIs return children: true to indicate lazy-loadable
    if (typeof item.children === 'boolean') return item.children;

    // stems typically have children
    if (item.theType === 'stem' || item.root) return true;

    return false;
  }

  function toJsTreeNode(item, parentIdForUniq) {
        // jsTree requires globally-unique node ids.
    // The API's special "truncatedItems" rows can reuse the parent id (or otherwise collide),
    // which causes jsTree to overwrite/replace existing nodes.
    // Make truncatedItems ids unique for the tree, but keep the original id in node.data for click logic.
    var nodeId = String(item.id);
    if (item && item.theType === 'truncatedItems') {
      nodeId = 'truncatedItems:' + String(item.id) + ':' + String(parentIdForUniq || '');
    }

    return {
      id: nodeId,
      text: item.name,     // Dojo getLabel() => object.name
      icon: getIcon(item),
      children: hasChildren(item), // true => show expander + lazy load
      data: item           // keep original payload for click logic
    };
  }

  function childrenToJsTreeNodes(childrenArray, parentIdForUniq) {
    if (!Array.isArray(childrenArray)) return [];
    return childrenArray
      .filter(Boolean)
      .map(function (child) { return toJsTreeNode(child, parentIdForUniq); });
  }

  // Init jsTree with lazy AJAX loading
  // We add a visible "Root" node at the top, then lazy-load its children from ?root.
  $('#folderTree').jstree({
    core: {
      themes: { stripes: false },
      data: function (node, cb) {
        // 1) Initial load: return a single synthetic Root node
        if (node.id === '#') {
          cb([
            {
              id: 'root',
              text: 'Root',
              icon: 'jstree-folder',
              children: true,            // show expander + lazy load
              data: { root: true, theType: 'stem', id: 'root', name: 'Root' } // synthetic Root payload for click logic
            }
          ]);
          return;
        }

        // 2) Expanding "Root": fetch real root children from the server
        if (node.id === 'root') {
          $.ajax({
            url: 'UiV2Main.folderMenu?root',
            type: 'GET',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            dataType: 'json',
            cache: true,
            timeout: 150000
          }).done(function (rootObj) {
            // Show the real "root folder" (e.g. "temp") as a node under synthetic Root.
            // Also inline its immediate children so opening it shows contents right away.
            var top = rootObj ? toJsTreeNode(rootObj) : null;
            if (top && rootObj && Array.isArray(rootObj.children)) {
              top.children = childrenToJsTreeNodes(rootObj.children, rootObj.id);
            }
            cb(top ? [top] : []);
          }).fail(function () {
            cb([]);
          });
          return;
        }

        // 3) Expanding any other node: fetch its children
        $.ajax({
          url: 'UiV2Main.folderMenu?' + encodeURIComponent(node.id),
          type: 'GET',
          headers: { 'X-Requested-With': 'XMLHttpRequest' },
          dataType: 'json',
          cache: true,
          timeout: 150000
        }).done(function (fullObj) {
          cb(childrenToJsTreeNodes(fullObj && fullObj.children, fullObj && fullObj.id));
        }).fail(function () {
          cb([]);
        });
      }
    },
    plugins: ['wholerow'] // optional, nicer click target
  });

  // Expand synthetic Root (and the real root folder under it) by default on initial render.
  // This triggers the UiV2Main.folderMenu?root AJAX call via the jsTree lazy loader.
  (function expandRootByDefault() {
    var $tree = $('#folderTree');

    function doExpand() {
      var inst = $tree.jstree(true);
      if (!inst) return;

            // Open synthetic Root; this triggers UiV2Main.folderMenu?root via the jsTree lazy loader.
      // Do NOT auto-open any children under Root.
      inst.open_node('root');
    }

    // If already ready, expand immediately; otherwise wait.
    if ($tree.data('jstree')) {
      try {
        var instNow = $tree.jstree(true);
        if (instNow && instNow._ready) {
          doExpand();
          return;
        }
      } catch (e) {}
    }

    $tree.one('ready.jstree', function () {
      doExpand();
    });
  })();

  // Click handler (your Dojo onClick logic)
  // Remove any previous handler first (dojoInitMenu can be called multiple times)
  $treeEl.off('activate_node.jstree.dojoInitMenu')
       .on('activate_node.jstree.dojoInitMenu', function (e, data) {
        
    if (data && data.event && data.event.preventDefault) {
      data.event.preventDefault();
      if (data.event.stopPropagation) data.event.stopPropagation();
    }        

        var item = data && data.node && data.node.data;
    if (!item) return;

    // Synthetic Root node: navigate to the Root stem
    if (item.root === true) {
      // Match existing navigation style used elsewhere in this file
      guiV2link('operation=UiV2Stem.viewStem&stemId=root');
      return;
    }

    function hasId(x) {
      return typeof x.id !== 'undefined' && x.id !== null;
    }

    if (item.theType === 'stem') {
      if (hasId(item)) guiV2link('operation=UiV2Stem.viewStem&stemId=' + item.id);

    } else if (item.theType === 'entity') {
      if (hasId(item)) guiV2link('operation=UiV2Subject.viewSubject&sourceId=grouperEntities&subjectId=' + item.id);

    } else if (item.theType === 'group') {
      if (hasId(item)) guiV2link('operation=UiV2Group.viewGroup&groupId=' + item.id);

    } else if (item.theType === 'attributeDef') {
      if (hasId(item)) guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=' + item.id);

    } else if (item.theType === 'attributeDefName') {
      if (hasId(item)) guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=' + item.id);

    } else if (item.theType === 'truncatedItems') {
      if (hasId(item)) guiV2link('operation=UiV2Stem.viewStem&stemId=' + item.id);

    } else {
      alert('ERROR: cant find theType on object with id: ' + item.id + ': ' + item.theType);
    }
  });

  // Auto-select/open path like your Dojo code
  if (autoSelectNode) {
    var itemId = null;
    var itemType = null;

    var uri = new URI(location.href);
    uri.search(function (data) {
      if (data.operation === "UiV2Stem.viewStem") {
        itemId = (data.stemName !== undefined) ? data.stemName : data.stemId;
        itemType = (data.stemName !== undefined) ? "stemName" : "stem";

      } else if (data.operation === "UiV2Visualization.stemView") {
        itemId = data.objectId;
        itemType = "stem";

      } else if (data.operation === "UiV2Group.viewGroup") {
        itemId = (data.groupName !== undefined) ? data.groupName : data.groupId;
        itemType = (data.groupName !== undefined) ? "groupName" : "group";

      } else if (data.operation === "UiV2Subject.viewSubject" && data.sourceId === "grouperEntities") {
        itemId = data.subjectId;
        itemType = "group";

      } else if (data.operation === "UiV2Visualization.groupView") {
        itemId = data.objectId;
        itemType = "group";

      } else if (data.operation === "UiV2AttributeDef.viewAttributeDef") {
        itemId = (data.nameOfAttributeDef !== undefined) ? data.nameOfAttributeDef : data.attributeDefId;
        itemType = (data.nameOfAttributeDef !== undefined) ? "nameOfAttributeDef" : "attributeDef";

      } else if (data.operation === "UiV2AttributeDefName.viewAttributeDefName") {
        itemId = (data.nameOfAttributeDefName !== undefined) ? data.nameOfAttributeDefName : data.attributeDefNameId;
        itemType = (data.nameOfAttributeDefName !== undefined) ? "nameOfAttributeDefName" : "attributeDefName";
      }

            if (itemType !== null) {
        $.ajax({
          url: "UiV2Main.folderMenuObjectPath",
          type: "POST",
          headers: { 'X-Requested-With': 'XMLHttpRequest' },
          cache: true,
          dataType: 'json',
          data: { id: itemId, type: itemType },
          timeout: 150000
        }).done(function (json) {
          openFolderTreePathToObjectJsTree(json);
        }).fail(function () {
          // If we can't resolve the path, at least expand Root so the user sees something.
          openFolderTreePathToObjectJsTree();
        });
      } else {
        // If the current page doesn't map to a specific object ("at Root"),
        // expand/select Root so the user sees Root and its immediate children.
        openFolderTreePathToObjectJsTree();
      }
    });
  }

  // ---- Path open/select helper (lazy-load safe-ish) ----
  function extractPathIds(pathJson) {
    // Try a few common shapes:
    // 1) ["id1","id2","id3"]
    // 2) [{id:"id1"},{id:"id2"}]
    // 3) { path: [...] }
    var p = pathJson;

    if (p && Array.isArray(p.path)) p = p.path;

    if (Array.isArray(p)) {
      if (p.length && typeof p[0] === 'string') return p.map(String);
      if (p.length && typeof p[0] === 'object') return p.map(x => String(x.id));
    }
    return [];
  }

  function openFolderTreePathToObjectJsTree(pathJson) {
    // If nothing is passed, assume we're at Root and expand it.
    var ids = extractPathIds(pathJson);

    if (!ids || !ids.length) {
      ids = ['root'];
    }

    // Our jsTree has a synthetic "root" node at the top. Ensure the open path starts there
    // so lazy-loading works consistently.
    if (ids[0] !== 'root') {
      ids.unshift('root');
    }

    // De-dupe consecutive ids in case "root" is present twice
    ids = ids.filter(function (id, idx) {
      return idx === 0 || id !== ids[idx - 1];
    });

    var $tree = $('#folderTree');

    // Run callback when the tree is ready AND the synthetic root node exists.
    function withTreeReady(cb) {
      try {
        if ($tree.data('jstree')) {
          var instNow = $tree.jstree(true);
          if (instNow && instNow.get_node && instNow.get_node('root', false)) {
            cb(instNow);
            return;
          }
        }
      } catch (e) {
        // fall through to ready handler
      }

      $tree.one('ready.jstree', function () {
        cb($tree.jstree(true));
      });
    }

    withTreeReady(function (inst) {
      // Root-only case: expand Root AND also expand the real root folder under it
      // so the user sees Root -> <root folder> -> its objects.
      if (ids.length === 1 && ids[0] === 'root') {
                // Root-only: expand Root so the user sees Root and its immediate children.
        // Do NOT select Root here because select_node triggers navigation.
        // Do NOT auto-open any children under Root.
        inst.open_node('root');
        return;
      }

      // Otherwise, open each node in order; opening triggers loading of its children
      function openNext(i) {
        if (i >= ids.length) {
          var last = ids[ids.length - 1];
                    inst.deselect_all();
          inst.select_node(last);
          inst.open_node(last);
          return;
        }

        var id = ids[i];
        var node = inst.get_node(id);

        if (node) {
          inst.open_node(id, function () { openNext(i + 1); });
        } else {
          // If the node isn't present yet, open the previous node to force-load it,
          // or refresh and retry.
          var prev = (i === 0) ? null : ids[i - 1];
          if (prev && inst.get_node(prev)) {
            inst.open_node(prev, function () { openNext(i); });
          } else {
            inst.refresh(false, false);
            setTimeout(function () { openNext(i); }, 75);
          }
        }
      }

      openNext(0);
    });
  }
}




//function dojoClearTree(theTree, theStore) {
//
//  dojoInitMenu();
//  
//  if (true) {
//    return;
//  }
//  
//  var rootNode = theTree.rootNode;
//  
//  rootNode.collapse(); 
//
//  //if you are using the loading/rpc tree controller  then update the state 
//  // of the node so that it will refetch on the next expand. 
//  //    When an empty folder node appears, it is "NotLoaded" first,
//  //    then after dojo.data query it becomes "Loading" and, finally "Loaded"
//  rootNode.state = 'NotLoaded'; 
//
//  //Loop through the children and call destroy. 
//  for(var i=rootNode.item.children.length -1; i >= 0 ; --i) { 
//    theStore.remove(rootNode.item.children[i]); 
//  }   
//  
//  rootNode.item.children = null;
//}

/**
 * see if two strings are equal without considering case
 */
function guiEqualsIgnoreCase(a, b) {
  if (a==b) {
    return true;
  }
  if (guiIsEmpty(a) || guiIsEmpty(b)) {
    return false;
  }
  return a.toLowerCase() == b.toLowerCase();
}


function guiAssignCurrentTimestampToBrowserElement() {
  var ajaxElement = $('span.grouperJspClass:contains("ajax")');
  if (ajaxElement.length == 0) {
    return;
  }
  var date = new Date(Date.now());
  
  // This the expected timestamp for playwright automation on the server side: 2024-06-17T16:50:49.000315Z
  var dateString = date.getUTCFullYear() + '-' + ((date.getUTCMonth() + 1) < 10 ? '0' : '') + (date.getUTCMonth() + 1) + '-' + (date.getUTCDate() < 10 ? '0' : '') 
    + date.getUTCDate() + 'T' + (date.getUTCHours() < 10 ? '0' : '') + date.getUTCHours() + ':' + (date.getUTCMinutes() < 10 ? '0' : '') + date.getUTCMinutes() + ':'
    + (date.getSeconds() < 10 ? '0' : '') + date.getSeconds() + '.' + (date.getMilliseconds() < 100 ? '0' : '')
    + (date.getUTCMilliseconds() < 10 ? '0' : '') + date.getUTCMilliseconds() + '000Z';
  ajaxElement.attr("data-gr-page-loadtime", dateString);
 
}


/** generic ajax method takes a url, callback function, and params or forms.
 * 
 * To pass in params to send to the server, pass in params like this:
 * Note: menuHtmlId, menuRadioGroup, and menutItemId are the param names, and
 * zoneId, group, and idClicked are variables whose values will be the param values:
 * 
 *  ajax(operation, {requestParams: {menuHtmlId: zoneId, menuRadioGroup: group, menuItemId: idClicked }});
 */
function ajax(theUrl, options) {

  //hide messaging
  $('#messaging').hide().empty();

  if (!guiStartsWith(theUrl, "../app/" ) && !guiStartsWith(theUrl, "../public/")) {
    theUrl = "../app/" + theUrl; 
  }
  
  theUrl = guiDecorateUrl(theUrl);

  if (typeof options == 'undefined') {
    options = {};
  }
  
  if (typeof options.requestParams == 'undefined') {
    options.requestParams = {};
  }
  
  if (!guiIsEmpty(options.formIds) || !guiIsEmpty(options.formIdsOptional)) {

    var formIdsOptionalArray = guiIsEmpty(options.formIdsOptional) ?
        new Array() : guiSplitTrim(options.formIdsOptional, ",");
    var formIdsArray = guiIsEmpty(options.formIds) ?
        new Array() : guiSplitTrim(options.formIds, ",");
    
    //add optional forms to send
    for (var i = 0; i<formIdsOptionalArray.length; i++) {
      var formId = formIdsOptionalArray[i];
      var theForm = $("#" + formId);
      if (theForm && theForm.length > 0) {
        formIdsArray.push(formId);
      }
    }
    
    for (var i = 0; i<formIdsArray.length; i++) {
      var formId = formIdsArray[i];
      
      //get elements in form
      var theForm = $("#" + formId);
      if (!theForm || theForm.length == 0) {
        alert('Cant find form by id: "' + formId + '"!');
        return;
      }
      
      theForm = theForm[0];
      for(var j=0;j<theForm.elements.length;j++) {
        var element = theForm.elements[j];
        
        options.requestParams[element.name] = guiFieldValues(element);
        
        //alert(element.id + ' - ' + element.nodeName.toUpperCase() + " - " + element.name + " - " + element.type + " - " + options.requestParams[element.name]);
        
      }
    }
  }

  if (typeof options.formElementNamesToSend != 'undefined' && options.formElementNamesToSend != null) { 
    
    //add additional form element names to filter based on other things on the screen 
    var additionalFormElementNamesArray = guiSplitTrim(options.formElementNamesToSend, ","); 
    for (var i = 0; i<additionalFormElementNamesArray.length; i++) { 
      var additionalFormElementName = additionalFormElementNamesArray[i]; 

      var element = document.getElementsByName(additionalFormElementName)[0];
      
      options.requestParams[element.name] = guiFieldValues(element);
    } 
  } 

  
  //add owasp token
  var owaspTokenName = 'OWASPCSRFTOKEN';
  var owaspCsrfTokenHeader = {};
  if (document.getElementsByName(owaspTokenName) != null
      && document.getElementsByName(owaspTokenName).length > 0
      && document.getElementsByName(owaspTokenName)[0] != null) {
    owaspCsrfTokenHeader[owaspTokenName] = document.getElementsByName(owaspTokenName)[0].value;
  }
  
  //make sure combos have the right state
  //for(var combo in allComboboxes) {
  //  alert('here: ' + combo + ", " + allComboboxes.length);
  //  
  //  allComboboxes[combo].confirmValue();
  //}
  //alert('here');
  
  //for(var requestParam in options.requestParams) {
  //  alert(requestParam + ", " + options.requestParams[requestParam]);
  //}
  
  //send over form data
  
  var appState = allObjects.appState;
  
  options.requestParams.appState = JSON.stringify(appState);

  //if modal up, it wont block, so close modal before ajax
  //$.modal.close(); 
  $.blockUI();  
  
  grouperOriginalAjaxUrl = theUrl;  
  
  $.ajax({
    url: theUrl,
    headers: owaspCsrfTokenHeader,
    type: 'POST',
    cache: false,
    dataType: 'json',
    data: options.requestParams,
    timeout: 1800000,
    async: true,
    //TODO handle errors success better.  probably non modal disappearing reusable window
    error: function(jqXHR, textStatus, errorThrown) {
        
      $.unblockUI();
      
      guiAssignCurrentTimestampToBrowserElement();
      
      // for an ajax request, server sent back 401
      // it's probably because session has timed out
      // let's refresh the page so that user can log back in
      if (jqXHR.status == 401) {
        
        if (location.href.indexOf('grouperRedirectAuthn') != -1) {
          
          if (location.href.indexOf('ajaxError') == -1) {
            location.href = "../../grouperExternal/public/UiV2Public.index?operation=UiV2Public.postIndex&function=UiV2Public.error&code=ajaxError&grouperRedirectAuthn=true";
          }
          // it looks like it's not the first time redirect. let's not redirect anymore
        } else {
          var newLocation = location.href;
          if (newLocation.indexOf('?') == -1) {
            newLocation += '?grouperRedirectAuthn=true';
          } else {
            newLocation += '&grouperRedirectAuthn=true';
          }
          
          location.href = newLocation;
        }
        return;
      }
      
      //what happens is there is an XSRF problem, and ajax will auto-redirect
      //the result of that redirect, to: https://server/grouperAppName/grouperExternal/public/UiV2Public.index?operation=UiV2Public.postIndex&function=UiV2Public.error&code=csrf&OWASP_CSRFTOKEN=abc123
      //that redirect will have an HTTP header of X-Grouper-path, and we should redirect the browser to it
      
      //if we are already on an error page, then stay there...
      if (location.href.indexOf('function=UiV2Public.error') == -1) {
      
        var grouperPath = jqXHR.getResponseHeader("X-Grouper-path");
        if (!guiIsEmpty(grouperPath)) {
          grouperPath = decodeURIComponent(grouperPath);
          
          //if this path is for XSRF, then lets just refresh the browser and alert an error message
          //../../grouperExternal/public/UiV2Public.index?operation=UiV2Public.postIndex&function=UiV2Public.error&code=csrf&OWASP_CSRFTOKEN=OOEE-4GAC-VUIS-YI7V-9BTD-X7MD-NO7E-AM8F
          //TODO in Grouper 2.3+ the indexOf UiV2 can be taken out
          if (grouperPath.indexOf('code=csrf') >= 0 && location.href.indexOf('UiV2') >= 0) {
            
            //there are two cases, if it was a get, or if it is a post...
            //well both are posts, but the "gets" are the ones that go into the URL for the back button, thats how we can tell
            //so look in the ajax url, and see what the operation is, and compare to the browser url
            var grouperOriginalAjaxOperation = guiGetOperationFromUrl(grouperOriginalAjaxUrl);
            var locationOperation = guiGetOperationFromUrl(location.href);
  
            //lets add something bogus to the request so the request is actually sent and not retrieved from cache
            var newLocation=location.href;
            
            if (newLocation.indexOf('csrfExtraParam') == -1) {
            
              if (newLocation.indexOf('?') == -1) {
                newLocation += '?csrfExtraParam=xyz';
              } else {
                newLocation += '&csrfExtraParam=xyz';
              }
    
              //this means its a post
              if (grouperOriginalAjaxOperation != null && grouperOriginalAjaxOperation != locationOperation) {
                alert(grouperCsrfText);            
              }
              
              location.href=newLocation;
              return;
            }
          }      
          location.href=grouperPath;
          return;
        }
  
        location.href = "../../grouperExternal/public/UiV2Public.index?operation=UiV2Public.postIndex&function=UiV2Public.error&code=ajaxError";
      }
      
    },
    success: function(json){
      guiProcessJsonResponse(json);
      $.unblockUI();  
      guiAssignCurrentTimestampToBrowserElement();
    }
  });
  
  return false;
}

/**
 * based on a url, get the operation param out of there
 * @param url
 */
function guiGetOperationFromUrl(url) {
  if (guiIsEmpty(url)) {
    return null;
  }
  //../app/UiV2MyGroups.myGroupsJoin
  if (url.indexOf('../app/') == 0) {
    //is there a question mark?
    var questionIndex = url.indexOf('?');
    if (questionIndex == -1) {
      //strip off the front
      return url.substring(7, url.length);
    }
    //substring until question mark
    return url.substring(7,questionIndex);
  }
  
  //in url param
  var operationEqualsIndex = url.indexOf('operation=');
  if (operationEqualsIndex == -1) {
    return null;
  }
  operationEqualsIndex += 10;
  var andIndex = url.indexOf('&', operationEqualsIndex);
  if (andIndex == -1) {
    return url.substring(operationEqualsIndex, url.length);
  }
  //there is an &, go to that
  return url.substring(operationEqualsIndex, andIndex);
}

/**
 * process an ajax request
 * @param guiResponseJs
 */
function guiProcessJsonResponse(guiResponseJs) {

  //$.unblockUI();

  //remove validation icons
  $(".validationError").remove();
  
  //message if session ends
  if (guiResponseJs.guiAjaxSessionProblem) {
    guiAjaxSessionProblem = guiResponseJs.guiAjaxSessionProblem;
  }

  //put new pagers in the app state
  if (guiResponseJs.pagers) {
    for(var pagerName in guiResponseJs.pagers) {
      allObjects.appState.pagers[pagerName] = guiResponseJs.pagers[pagerName];
    }
  }

  //put new pagers in the app state
  if (guiResponseJs.hideShows) {
    for(var hideShowName in guiResponseJs.hideShows) {
      allObjects.appState.hideShows[hideShowName] = guiResponseJs.hideShows[hideShowName];
    }
  }
  
  var foundAlert = false;
  
  for (var i=0; i<guiArrayLength(guiResponseJs.actions); i++ ) {
    
    var action = guiResponseJs.actions[i];
    
    if (!guiIsEmpty(action.alert)) {
     
      if (foundAlert) {
        continue; 
      }
      
      foundAlert=true;
      
      //lets get all the alerts since we cant popup multiple alerts, but not this one
      for (var j=0; j<guiArrayLength(guiResponseJs.actions); j++ ) {
        //skip if this one
        if (j==i) {
          continue; 
        }
        var action2 = guiResponseJs.actions[j];
        if (!guiIsEmpty(action2.alert)) {
          //append with newlines
          action.alert += "<br /><br />" + action2.alert;
          action2.alert = null;
        }
      
      }      
    }
    
    guiProcessAction(action);
    
  }

  //see if there are actions
  //if (successResultFunction) {
  //  successResultFunction.call(this, json);
  //}
  
  //round those corners
  guiRoundCorners();

}

// selectors need to be escaped with dots and other chars
function guiEscapeSelectorIfNeeded(s){
  
  // if its there then dont worry about it
  if ($(s).length > 0) {
    return s;
  }
  return s.replace( /(:|\.|\[|\])/g, "\\$1" );
}

/**
 * process an action
 * @param action
 */
function guiProcessAction(guiScreenAction) {
  //make an assignment to something
  if (!guiIsEmpty(guiScreenAction.assignmentName)) {
    eval(guiScreenAction.assignmentName + " = guiScreenAction.assignmentObject");
  }
  //evaluate an arbitrary script
  if (!guiIsEmpty(guiScreenAction.script)) {
    eval(guiScreenAction.script);
  }
  //replace some html
  if (!guiIsEmpty(guiScreenAction.innerHtmlJqueryHandle) && guiIsEmpty(guiScreenAction.validationMessage)) {
     grouperDestroyTomSelectInContainer(guiScreenAction.innerHtmlJqueryHandle);
     $(guiEscapeSelectorIfNeeded(guiScreenAction.innerHtmlJqueryHandle)).html(guiScreenAction.html);
  }

  //append html
  if (!guiIsEmpty(guiScreenAction.appendHtmlJqueryHandle)) {
    $(guiEscapeSelectorIfNeeded(guiScreenAction.appendHtmlJqueryHandle)).append(guiScreenAction.html);
  }

  //hide/shows
  if (!guiIsEmpty(guiScreenAction.hideShowNameToShow)) {
    guiHideShow(null, guiScreenAction.hideShowNameToShow, true);
  }
  
  if (!guiIsEmpty(guiScreenAction.hideShowNameToHide)) {
    guiHideShow(null, guiScreenAction.hideShowNameToHide, false);
  }
  
  if (!guiIsEmpty(guiScreenAction.hideShowNameToToggle)) {
    guiHideShow(null, guiScreenAction.hideShowNameToToggle);
  }
  
  if (typeof guiScreenAction.closeModal != 'undefined' && guiScreenAction.closeModal) {
    if ($ && $.modal && typeof($.modal.close) == "function") {
      $.modal.close(); 
    }
  }
  
  //do an alert
  if (!guiIsEmpty(guiScreenAction.alert)) {
    $.unblockUI();

    //default to centered
    var centered = (typeof guiScreenAction.alertCentered == 'undefined') || guiScreenAction.alertCentered;

    grouperBootstrapAlert(guiScreenAction.alert, centered);
  }

  
  // do a dialog (Bootstrap 2.2.2)
  if (!guiIsEmpty(guiScreenAction.dialog)) {
    $.unblockUI();

    // mimic the old behavior: positioned at [20,20]
    grouperBootstrapDialog(guiScreenAction.dialog, false, null);
  }
  if (!guiIsEmpty(guiScreenAction.optionValues)) {
    var optionValues = guiScreenAction.optionValues;
    for (var i=0;i<optionValues.length;i++) {
      var selectName = guiScreenAction.optionValuesSelectName;
      var options = optionValues[i].optionValues;
      var optionString = '';
      if (options != null) {
        for (var j=0;j<options.length;j++) {
          var label = options[j].label;
          var css = options[j].css;
          var value = options[j].value;
          
          //escape stuff:
          value = guiReplaceString(value, '"', '&quot;');
          value = guiReplaceString(value, '<', '&lt;');
          value = guiReplaceString(value, '>', '&gt;');
          label = guiReplaceString(label, '<', '&lt;');
          label = guiReplaceString(label, '>', '&gt;');
          
          optionString += '<option value="' + value + '"';
          if (!guiIsEmpty(css)) {
            optionString += ' class="' + css + '"';
          }
          optionString += '>' + label + '</option>';
        }
      }
      var selectElement = guiGetElementByName(selectName);
      $(guiEscapeSelectorIfNeeded(selectElement)).html(optionString);
    }
  }
  if (!guiIsEmpty(guiScreenAction.formFieldName)) {
    guiFormElementAssignValue(guiScreenAction.formFieldName, guiScreenAction.formFieldValues);
  }
  if (!guiIsEmpty(guiScreenAction.message)) {
    if (!guiIsEmpty(guiScreenAction.messageAppend)) {
      guiMessageHelper(guiScreenAction.messageType, guiScreenAction.message, false);
    } else {
      guiMessageHelper(guiScreenAction.messageType, guiScreenAction.message);
    }
    guiScrollTop();
  }
  if (!guiIsEmpty(guiScreenAction.validationMessage)) {
    guiMessageHelper(guiScreenAction.messageType, guiScreenAction.validationMessage, false);
    guiScrollTop();
    //put up the validation error thing
    //TODO if the handle doesnt exist, throw error to help develop, sometimes the error is thrown before JSP is drawn wont work
    
    // single quote doesnt get escaped right.  will get converted to double quote.  dont use single quotes in alerts!
    var alertText = guiScreenAction.validationMessage.replace(/'/g, "\""); 
      
    alertText = guiEscapeHtml(alertText, true);
    
    if (!guiIsEmpty(guiScreenAction.innerHtmlJqueryHandle)) {
      $(guiEscapeSelectorIfNeeded(guiScreenAction.innerHtmlJqueryHandle)).after('&nbsp;<a class="validationError" href="#" onclick="alert(\'' + alertText + '\'); return false;"><i class="fa fa-exclamation-triangle fa-lg" style="color:#CC3333;"></i></span>');
    }
  }
}

/**
 * Show a Bootstrap 2.2.2 modal containing arbitrary HTML.
 * Removes itself from the DOM when closed.
 *
 * @param htmlBody HTML string to render in the modal body
 * @param centered if false, positions near top/left (20px,20px); if true uses Bootstrap default centering
 * @param title optional title string (null for no header)
 */
function grouperBootstrapDialog(htmlBody, centered, title) {

  var modalId = 'grouperBootstrapDialogModal';
  $('#' + modalId).remove();

  var headerHtml = '';
  if (title != null && title !== '') {
    headerHtml =
      '<div class="modal-header">' +
        '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
        '<h3>' + title + '</h3>' +
      '</div>';
  }

  var $modal = $(
    '<div id="' + modalId + '" class="modal hide fade" tabindex="-1" role="dialog" aria-hidden="true">' +
      headerHtml +
      '<div class="modal-body"></div>' +
    '</div>'
  );

  $modal.find('.modal-body').html(htmlBody);

  if (centered === false) {
    $modal.css({
      top: '20px',
      left: '20px',
      marginLeft: '0',
      width: 'auto'
    });
  }

  $('body').append($modal);

  $modal.on('hidden', function () {
    $(this).remove();
  });

  $modal.modal({ backdrop: true, keyboard: true, show: true });
}

/**
 * Show an “OK” alert modal using Bootstrap 2.2.2 (no SimpleModal).
 * @param htmlMessage HTML string to show in the modal body
 * @param centered if false, positions near top/left (20px, 20px); if true uses Bootstrap default centering
 */
function grouperBootstrapAlert(htmlMessage, centered) {

  // Remove any previous instance
  var modalId = 'grouperBootstrapAlertModal';
  $('#' + modalId).remove();

  var $modal = $(
    '<div id="' + modalId + '" class="modal hide fade" tabindex="-1" role="dialog" aria-hidden="true">' +
      '<div class="modal-header">' +
        '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
        '<h3>Alert</h3>' +
      '</div>' +
      '<div class="modal-body"></div>' +
      '<div class="modal-footer">' +
        '<button class="btn btn-primary" data-dismiss="modal">OK</button>' +
      '</div>' +
    '</div>'
  );

  // Insert the HTML message
  $modal.find('.modal-body').html(htmlMessage);

  // Optional positioning: mimic SimpleModal position [20,20]
  if (centered === false) {
    // Bootstrap 2 centers with left:50% + negative margin-left; override it.
    $modal.css({
      top: '20px',
      left: '20px',
      marginLeft: '0',
      width: 'auto'
    });
  }

  // Append and show
  $('body').append($modal);

  // Clean up after close
  $modal.on('hidden', function () {
    $(this).remove();
  });

  $modal.modal({ backdrop: true, keyboard: true, show: true });
}

/**
 * this is for xstream json... if there is an object which isnt an array, turn it into an array
 * if object doesnt exist, return it (or lack thereof)
 */
function guiConvertToArray(someVar, convertIfNull) {
  if (!convertIfNull && !someVar) {
    return someVar;
  }
  //these are array functions and fields...
  if (!someVar || !someVar.length || !someVar.sort) {
    var theArray = new Array();
    theArray[0] = someVar;
    return theArray;
  }
  return someVar;
}

/**
 * non null string
 * @param x
 * @return non null value
 */
function guiDefaultString(x) {
  return x == null ? "" : x;
} 

/** set form element(s) to values */
function guiFormElementAssignValue(name, values) {
  
  values = guiConvertToArray(values, true);
  
  for (var i=0;i<values.length;i++) {
    var value = guiToString(guiDefaultString(values[i]));
    var theElements = guiGetElementsByName(name);
    if (theElements == null) {
      
      alert('Error: cant find element with name: ' + name);
    }
    for (var j=0;j<theElements.length;j++) {
      var theElement = theElements[j];
      
      if (theElement.nodeName.toUpperCase() == "INPUT" 
        && (theElement.type.toUpperCase() == "TEXT" || theElement.type.toUpperCase() == "HIDDEN")) {
        theElement.value = value;
      } else if (theElement.nodeName.toUpperCase() == "INPUT" 
        && (theElement.type.toUpperCase() == "CHECKBOX"
        || theElement.type.toUpperCase() == "RADIO")) {
        
        //unselect all if first pass
        if (i==0 && j==0) {
          for (var l=0;l<theElements.length;l++) {
            theElements[l].checked = false;
          }
        }
        
        if (theElement.value == value || 
          (guiIsEmpty(theElement.value) && guiIsEmpty(value))) {
          //instead of setting checked to true, call click, which fires onchange
          theElement.click();
        }
      } else if (theElement.nodeName.toUpperCase() == "SELECT") {
        var options = theElement.options;
        if (options) {
          //unselect all if first pass
          if (i==0 && j==0) {
            for (var l=0;l<options.length;l++) {
              options[l].selected = false;
            }
          }

          for (var k=0;k<options.length;k++) {
            var option = options[k];
            if (option.value == value || 
              (guiIsEmpty(option.value) && guiIsEmpty(value))) {
              option.selected = true;
            }
          }
        }
      } else if (theElement.nodeName.toUpperCase() == "TEXTAREA") {
        //alert(theElement.name);
        //theElement.innerHTML = value;
        var jqueryTextarea = $(theElement);
        jqueryTextarea.html(value);
      } else {
        alert('Error: form element type not implemented for assignment: ' + theElement.nodeName
          + ", " + theElement.type);
      }
    }
  }
  
}


/**
 * replace a string in another string (all occurrences)
 * 
 * @param input
 * @param stringToFind
 * @param stringToReplace
 * @return the new string
 */
function guiReplaceString(input, stringToFind, stringToReplace) {
  if (guiIsEmpty(input) || guiIsEmpty(stringToFind)) {
    return input;
  }
  input = guiToString(input);
  var index = input.indexOf(stringToFind);
  var ret = "";
  if (index == -1) return input;
  ret += input.substring(0,index) + stringToReplace;
  if ( index + stringToFind.length < input.length) {
    ret += guiReplaceString(input.substring(index + stringToFind.length, input.length), stringToFind, stringToReplace);
  }
  return ret;
}

/** convert input into a non-null string */
function guiToString(input) {
  if (typeof input == "number" && input == 0) {
    return "0";
  }
  if (typeof input == "undefined" || input==null) {
    return "";
  }
  return ""+input;
}

/** when a javascript link click happens, dont let the a href click happen */
function eventCancelBubble(event) {
  if (!event) var event = window.event;
  
  // There is no event or window.event
  // when onchange() is called from javascript
  // on a select with autoButtonId and autoForm
  // in FireFox 1.7
  if (event != null){
    //ms
    event.cancelBubble = true;
    //net
    if (event.stopPropagation) event.stopPropagation();
  }
}

/**
 * create a tooltip
 * @param message
 */
function grouperTooltip(message) {
  // NOTE: message is in a javascript call, so we need to unescape the HTML.
  message = guiEscapeHtml(message, false);

  // Prefer Bootstrap 2.2.2 tooltip (bootstrap.js) if present.
  // This relies on being invoked from an inline handler like:
  //   onmouseover="grouperTooltip('...')"
  // If we can't infer the element, fall back gracefully.
  if (typeof $ != 'undefined' && $.fn && typeof $.fn.tooltip == 'function') {

    // Determine the element that triggered the event.
    var element = null;
    if (typeof window != 'undefined' && window.event && window.event.srcElement) {
      // IE
      element = window.event.srcElement;
    } else if (typeof window != 'undefined' && window.event && window.event.target) {
      // Some browsers still populate window.event
      element = window.event.target;
    }

    if (element) {
      // Prefer the closest element with class grouperTooltip, else just use the element.
      var $el = $(element);
      var $anchor = $el.closest('.grouperTooltip');
      if ($anchor.length > 0) {
        $el = $anchor;
      }

      // Accessibility: if this is a non-focusable element (e.g. span), make it focusable.
      // That enables keyboard users to discover the tooltip.
      if (!$el.is('a,button,input,select,textarea') && !$el.attr('tabindex')) {
        $el.attr('tabindex', '0');
      }

      // If we add tabindex, also show/hide tooltip on focus/blur.
      // Namespace handlers so we can safely rebind.
      $el.off('.grouperTooltipA11y');
      $el.on('focus.grouperTooltipA11y', function () {
        // Mirror the last known title
        try { $(this).tooltip('show'); } catch (e) {}
      });
      $el.on('blur.grouperTooltipA11y', function () {
        try { $(this).tooltip('hide'); } catch (e) {}
      });

      // Store content and show immediately.
      // Use trigger: 'manual' so our UnTip() can consistently hide it.
      // Use container: 'body' so it isn't clipped by overflow/positioning.
      $el.attr('data-original-title', message);
      $el.tooltip({
        trigger: 'manual',
        html: true,
        container: 'body',
        placement: 'top'
      });

      $el.tooltip('show');

      // Accessibility: link triggering element to the tooltip via aria-describedby.
      // Bootstrap 2 doesn't add this automatically.
      try {
        var $tip = $el.data('tooltip') ? $el.data('tooltip').tip() : null;
        if ($tip && $tip.length) {
          var tipId = $tip.attr('id');
          if (!tipId) {
            tipId = 'grouperTooltip_' + (new Date().getTime()) + '_' + Math.floor(Math.random() * 100000);
            $tip.attr('id', tipId);
          }
          // Ensure the tooltip container has role=tooltip
          if (!$tip.attr('role')) {
            $tip.attr('role', 'tooltip');
          }
          $el.attr('aria-describedby', tipId);
        }
      } catch (e) {
        // ignore
      }

      return;
    }
  }
}

function UnTip() {
  if (typeof $ != 'undefined' && $.fn && typeof $.fn.tooltip == 'function') {
    var element = null;
    if (typeof window != 'undefined' && window.event && window.event.srcElement) {
      element = window.event.srcElement;
    } else if (typeof window != 'undefined' && window.event && window.event.target) {
      element = window.event.target;
    }
    if (element) {
      var $el = $(element);
      var $anchor = $el.closest('.grouperTooltip');
      if ($anchor.length > 0) {
        $el = $anchor;
      }

      // Remove aria-describedby linkage if we added it
      $el.removeAttr('aria-describedby');

      // Hide, then destroy to avoid accumulating handlers on repeated mouseovers.
      $el.tooltip('hide');
      $el.tooltip('destroy');

      // Remove the focus/blur handlers we added
      $el.off('.grouperTooltipA11y');

      return;
    }
  }
}

/** call this from button to hide/show some text */
function guiToggle(event, jqueryElementKey) {
  eventCancelBubble(event);
  $(guiEscapeSelectorIfNeeded(jqueryElementKey)).toggle('slow'); 
  return false;
}

/** go to an anchor link on the same page */
function goToAnchor(anchor) {
  var location = ""+window.location;
  var charIndex = location.indexOf("#");
  if (charIndex >= 0) {
    location = location.substring(0, charIndex);
  }
  window.location = location + "#" + anchor;
  return false;
}

/** 
 * call this from button to hide/show some text
 * 
 * Each hide show has a name, and it should be unique in the app, so be explicit, 
 * below you see "hideShowName", that means whatever name you pick
 * 
 * First add this css class to elements which should show when the state is show:
 * shows_hideShowName
 * 
 * Then add this to things which are in the opposite toggle state: hides_hideShowName
 * 
 * Then add this to the button(s):
 * buttons_hideShowName
 *  
 * In the business logic, you must init the hide show before the JSP draws (this has name,
 * text when shown, hidden, if show initially, and if store in session):
 * GuiHideShow.init("simpleMembershipUpdateAdvanced", false, 
 *    GrouperUiUtils.message("simpleMembershipUpdate.hideAdvancedOptionsButton"), 
 *       GrouperUiUtils.message("simpleMembershipUpdate.showAdvancedOptionsButton"), true);
 *
 * Finally, use these EL functions to display the state correctly in JSP:
 * Something that is hidden/shown
 * style="${grouper:hideShowStyle('hideShowName', true)}
 * 
 * Button text:
 * ${grouper:hideShowButtonText('hideShowName')}
 * 
 * In the button, use this onclick:
 * onclick="return guiHideShow(event, 'hideShowName');"
 * 
 * @param shouldShow is undefined or null to toggle, true to show, false to now show
 */
function guiHideShow(event, hideShowName, shouldShow) {
  eventCancelBubble(event);
  
  //lets get the hide show object 
  var hideShow = allObjects.appState.hideShows[hideShowName];
  
  //this shouldnt happen
  if (typeof hideShow == 'undefined') {
    alert("Cant find hideShow: " + hideShowName); 
    return;
  }

  //get the button
  var buttons = $(guiEscapeSelectorIfNeeded('.buttons_' + hideShowName)); 
  if (!buttons) {
    buttons = new Array(); 
  }
  
  //see if we are mandating a show, or if we are leaving it to the object model
  if (typeof shouldShow == 'undefined' || shouldShow == null) {
     shouldShow = !hideShow.showing;
  }
  
  //see if currently showing
  if (shouldShow) {
    //note: dont use hide('slow') or show('slow') since it turns to block display
    $(guiEscapeSelectorIfNeeded('.shows_' + hideShowName)).fadeIn('slow');
    $(guiEscapeSelectorIfNeeded('.hides_' + hideShowName)).fadeOut('slow');
    for (var i = 0; i < buttons.length; i++) { 
      var button = buttons[i];
      //could be an image or something
      if (!guiIsEmpty(button.innerHTML)) {
        $(button).html(hideShow.textWhenShowing); 
      }
    }
  } else {
    $(guiEscapeSelectorIfNeeded('.shows_' + hideShowName)).fadeOut('slow');
    $(guiEscapeSelectorIfNeeded('.hides_' + hideShowName)).fadeIn('slow');
    for (var i = 0; i < buttons.length; i++) { 
      var button = buttons[i];
      //could be an image or something
      if (!guiIsEmpty(button.innerHTML)) {
        $(button).html(hideShow.textWhenHidden); 
      }
    }
  }
  
  //toggle (or hard set)
  hideShow.showing = shouldShow;
  
  return false;
}

/**
 * see if an element is an inline element
 * @param element
 * @return true if inline
 */
//function guiIsInline(element) {
//  
//}

/**
 * fade out if not span or a tag, or button or whatever
 */
//function guiHide(String jqueryHandle) {
//  
//  var elements = $(jqueryHandle);
//
//  //dont worry if nothing there
//  if (guiIsEmpty(elements) || elements.length == 0) {
//    return;
//  }
//  
//  for (var i=0;i<elements.length;i++) {
//    var element = elements[i];
//  }
//}

/**
 * split and trim a string to an array of strings
 */
function guiSplitTrim(input, separator) {
 if (input == null) {
   return input;
  }
  //trim the string
  input = guiTrim(input);
 if (input == null) {
   return input;
  }
  //loop through the array and trim it
 var theArray = input.split(separator);
 for(var i=0;theArray!=null && i<theArray.length;i++) {
     theArray[i] = guiTrim(theArray[i]);
  }
  return theArray; 
}

/**
 * trim all whitespace off a string
 */
function guiTrim(x) {
  if (!x) {
    return x;
  }
  var i = 0;
  while (i < x.length) {
    if (guiIsWhiteSpace(x.charAt(i))) {
      i++;
    } else {
      break;
    }
  }
  if (i==x.length) {
    return "";
  }
  x = x.substring(i,x.length);
  i = x.length-1;
  while (i >= 0) {
    if (guiIsWhiteSpace(x.charAt(i))) {
      i--;
    } else {
      break;
    }   
  }
  if (i < 0) {
    return x;
  }
  return x.substring(0,i+1);
}
function guiIsWhiteSpace(x) {
  return x==" " || x=="\n" || x=="\t" || x=="\r";
}

function URLDecode(string) {
 return decodeURIComponent(string.replace(/\+/g,  " "));
}
function URLEncode(string) {
 return encodeURIComponent(string);
}

/**
 * escape html from a string: less than, greater than, ampersand, and quote
 */
function guiEscapeHtml(html, isEscape) {
  if (isEscape) {
    var escaped = html;
    escaped = escaped.replace(/&/g, "&amp;"); 
    escaped = escaped.replace(/</g, "&lt;"); 
    escaped = escaped.replace(/>/g, "&gt;"); 
    escaped = escaped.replace(/"/g, "&quot;"); 
    escaped = escaped.replace(/'/g, "&apos;"); 
    return escaped;
  } else {
    var unescaped = html;
    unescaped = unescaped.replace(/&apos;/g, "'"); 
    unescaped = unescaped.replace(/&quot;/g, '"'); 
    unescaped = unescaped.replace(/&gt;/g, ">"); 
    unescaped = unescaped.replace(/&lt;/g, "<"); 
    unescaped = unescaped.replace(/&amp;/g, "&"); 
    return unescaped;
    
  }
}

/**
 * find array length
 * @param x
 * @return the length of array x
 */
function guiArrayLength(x) {
  if (guiIsEmpty(x)) {
    return 0;
  }
  if (!x.sort) {
    alert('This is not an array: ' + x); 
  }
  return x.length;
}

/**
 * see if  variable is empty
 * @param x to see if empty
 * @return true if variable is empty
 */
function guiIsEmpty(x) {
  if (typeof x == "number" && x == 0) {
     return false;
  }
 return typeof x == "undefined" || x == null 
   || (typeof x == "string" && x == "");
}

/**
 * see if a string starts with another string
 * @param a
 * @param b
 * @return true if starts with
 */
function guiStartsWith(a, b) {
  if (a.indexOf(b) == 0) {
    return true;
  } else {
    return false;
  }
}

/** Get the value of the field, if it is a radio or checkbox, 
get all of the same name and aggregate the values.  if nothing
in there will return an empty array. */
function guiFieldValues(theField) {
   if (theField.nodeName.toUpperCase() == "INPUT" 
     && (theField.type.toUpperCase() == "RADIO"
     || theField.type.toUpperCase() == "CHECKBOX")) {
     var result = new Array();
     var elements = guiGetElementsByName(theField.name);
     var index = 0;
     for (var i=0;i<elements.length;i++) {
        if (elements[i].checked) {
           result[index++] = elements[i].value;
        }
     }  
     return result;     
   }
   
   if (theField.nodeName.toUpperCase() == "SELECT" && theField.multiple==true) {
     var result = new Array();
     var index = 0;
     if (theField.options) {
       for (i=0; i<theField.options.length; i++) {
         if (theField.options[i].selected) {
           result[index++] = theField.options[i].value;
         }
       }
     }
     
     return result;
   }   

   //else just to jquery
   var theValue = guiFieldValue(theField);
   return theValue;
}


/** Get the value of the field whether it is a textfield or select */
function guiFieldValue(theField) {
   if (theField.nodeName.toUpperCase() == "SELECT") {
      var selectedIndex = theField.selectedIndex;
      selectedIndex = selectedIndex < 0 ? 0 : selectedIndex;
      //there isnt even an option there...
      if (theField.options.length <= selectedIndex) {
         return "";
      }
      return theField.options[selectedIndex].value;
   } else if (theField.nodeName.toUpperCase() == "TEXTAREA"
        && theField.innerText && guiIsEmpty(theField.value)) {
     return theField.innerText;
   } else if (theField.type.toUpperCase() == "CHECKBOX") {      
     var checkboxes = guiGetElementsByName(theField.name);
     for (var i=0;i<checkboxes.length;i++) {
        if (checkboxes[i].checked) {
           //just set one, good enough for required valid
           return checkboxes[i].value;
        }
     }  
     return "";
   }
   return theField.value;
}

/** print an object */
function guiPrintObject(object) {
  var theString = 'Start object ' + object + "\n";
  var j = 0;
  for (var theField in object) {
    try {
      var fieldObject = object[theField];
      if (typeof fieldObject != 'function') {
        theString += theField + ": " + fieldObject + "\n";
      }
    } catch(err) {
      theString += theField + ": errorHappened\n";
    }
     if (j++ > 15) {
       alert(theString);
       j = 0;
       theString = '';
     }
  }
  alert(theString + "end object " + object);

}

/** get elements by name, filter due to ie8 which returns elements by id or name */
function guiGetElementsByName(theName) {
  var theElements = document.getElementsByName(theName);
  if (theElements != null) {
    
    var theElementsTemp = theElements;
    theElements = new Array();
    for (var i=0;i<theElementsTemp.length;i++) {
      //guiPrintObject(theElementsTemp[i]);
      if (theElementsTemp[i].name == theName) {
        //alert('keeping ' + theElementsTemp[i]);
        theElements[theElements.length] = theElementsTemp[i];
      } else {
        //alert('removing ' + theElementsTemp[i] + ", " + theElementsTemp[i].name + ", " + theName);
      }
    }
  }  
  return theElements;
  
}

/** get an element from the document object by name.  if no elements, null, if multiple, then alert */
function guiGetElementByName(theName) {
   var theElements = guiGetElementsByName(theName);
   if (theElements != null) {
     
      if (theElements.length == 1) {
         return theElements[0];
      } else if (theElements.length > 1) {
         alert("Elements should be 1 for element " + theName + " but instead it is " + theElements.length);
      }
   }
   return null;
}

/**
 * called from paging tag, sets the paging data to send to server, 
 * and calls the refresh operation
 * @param pagingName
 * @param pageNumber is 1 indexed
 * @param refreshOperation
 * @return false so it doesnt navigate
 */
function guiGoToPage(pagingName, pageNumber, refreshOperation) {
  var pager = allObjects.appState.pagers[pagingName];
  if (guiIsEmpty(pager)) {
    alert("Error: Cant find pager: '" + pagingName + "'"); 
    return;
  }
  pager.pageNumber = pageNumber;
  ajax(refreshOperation);
  return false;
}

/**
 * called from paging tag, sets the paging size
 * and calls the refresh operation
 * @param pagingName
 * @param pageSize
 * @param refreshOperation
 * @return false so it doesnt navigate
 */
function guiPageSize(pagingName, pageSize, refreshOperation) {
  var pager = allObjects.appState.pagers[pagingName];
  if (guiIsEmpty(pager)) {
    alert("Error: Cant find pager for page size: '" + pagingName + "'"); 
    return;
  }
  //might get into a problem if we dont set this back to 1
  pager.pageNumber = 1;
  pager.pageSize = pageSize;
  ajax(refreshOperation);
  return false;
}

/** GROUPER UI FUNCTIONS */
/** see if infodots are enabled, return true or false */
function infodotsEnabled() {
  var grouperCookieValue = getCookie(grouperInfodotCookieName);
  if (isEmpty(grouperCookieValue)) {
    return true;
  }
  return "true" == grouperCookieValue;
}
/** hide or show an element by id, return false to not navigate to link */
function grouperHideShow(event, elementIdToHideShow, forceShow) {

  eventCancelBubble(event);

  var theElement = document.getElementById(elementIdToHideShow + '0');
  
  //see if shown or hidden
  var isHidden = isEmpty(theElement) ? true : theElement.style.display == 'none'

  if (!forceShow || isHidden) {
  
    hideShow(isHidden, elementIdToHideShow, true);
  }
  
  return false;
}

/** hide or show an element.
 * @param isHidden is the current state of the element (it will change)
 * @param idPrefix is the prefix of the hideShow id (that can be multiple)
 * @param alertIfNone is true if you want an alert to go if not exists
 */
function hideShow(isHidden, idPrefix, alertIfNone) {
  var show = false;
  if (isHidden) {
    show = true;
  }
  var suffix = 0;
  var currentElement;
  var didAny = false;
  while (currentElement = document.getElementById(idPrefix + "" + (suffix++))) {
    didAny = true;
    if (show) {
      //note: dont use hide('slow') or show('slow') since it turns to block display
      $(currentElement).fadeIn('slow'); 
    } else {
      $(currentElement).fadeOut('slow'); 
    }
  }
  if (!didAny && alertIfNone) {
    window.alert("Nothing to hide or show for id: " + idPrefix);
  }
}

function isEmpty(x) {
  //fix a false positive
  if (typeof x == "number" && x == 0) {
     return false;
  }
 return typeof x == "undefined" || x == null 
   || (typeof x == "string" && x == "");
}

/** END GROUPER UI FUNCTIONS */

function guiInt(input) {
  if (guiIsEmpty(input)) {
    return null;
  }
  var originalInput = input;
  
  //if the string is "09" then we want to ignore the 0's
  while (input.length > 1 && input.charAt(0) == '0') {
     input = input.substring(1,input.length);
  }
  
  var theInt = parseInt(input);
  if (theInt + "" == input + "") {
    return theInt;
  }
  alert("cant convert '" + originalInput + "' to int");
}

/**
 * get a form element from a form by name
 * @param form
 * @param elementName
 * @return the form element or null if not there
 */
function guiFormElement(form, elementName) {
  
  for(var i=0;i<form.elements.length;i++) {
    var theElement = form.elements[i];
    if (theElement.name == elementName) {
      return theElement; 
    }
  }
  return null;
}
 
/** redefine this function if you want to set current date to something else */
function guiNewDate() {
  return new Date();
}

/**
 * Return the m/d/yyyy string for the current date
 */
function guiNow() {
  var now = guiNewDate();
  var x = (now.getMonth()+1) + "/" + now.getDate()+ "/" +  now.getFullYear();
  return x;
}

/** convert a string to a ccyymmdd or mm/dd/ccyy HH:MM:ss.SSS */
function guiConvertStringToDateOrTimestamp(input, isDate) {
  var ret = guiConvertStringToDateOrTimestampHelper(input, isDate);
  if (isDate) {
    //dont return an invalid value
    if (guiValidateDate(ret)) {
      return ret;
    }
    return input;
  }
  if (guiValidateTimestamp(ret)) {
    return ret;
  }
  return input;
}

/** validate a month, day, year, give a helpful error message or null if no error */
function guiValidateDateHelper(month, day, year, args) {
  if (month > 12 || month < 1 || day < 1) {
    return guiBuildErrorArgs("dateValidInvalidMonth", args);
  }
  var isValid = day <= 31 && (month == 1 || month == 3 || month == 5 || month == 7 || month == 8
   || month == 10 || month == 12);
  isValid = isValid || (day <= 30 && (month == 4 || month == 6 || month == 9 || month == 11));
  if (isValid) { return null; }
  if (month != 2) {
    return guiBuildErrorArgs("dateValidInvalidDay", args);
  }
  //now month == 2, make sure the days is right
  // century years are only leap years if divisible by 400
  var isLeap=(year%4==0 && (year%100!=0 || year%400==0));
  if (day > 29 || (day == 29 && !isLeap)) {
    return guiBuildErrorArgs("dateValidLeapYear", args);
  }
 
  return null;

}

/**
 * 
 * @param event
 * @return
 */
function guiSubmitFileForm(event, formJqueryHandle, operation) {
  
  eventCancelBubble(event);

  //clear the error div... / messaging
  $('#messaging').hide().empty();

  //make sure there is a hidden field for appState
  var appState = allObjects.appState;
  
  var appStateJson = JSON.stringify(appState);
  
  var forms = $(formJqueryHandle);
  
  for (var i=0;i<forms.length;i++) {
    var form = forms[i];
    var appStateElement = guiFormElement(form, "appState");
    if (appStateElement == null) {
      //add this to the form
      $(form).append('<input type="hidden" name="appState" />');
      appStateElement = guiFormElement(form, "appState");
    }
    //add the app state to it (sends the hide shows and pagers and stuff
    appStateElement.value = appStateJson;
  }
  
  var options = {
      // GRP-2249: member batch import incompatible with content security policy blocking frame-ancestor;
      // File upload seems to still work even if iframe is not set to true
      //iframe: true,
      dataType: "json",
      success:    function(json) { 
        guiProcessJsonResponse(json);
        $.unblockUI();
      },
      error:    function(json) { 
        $.unblockUI();
      },
      url: operation
  };
  //$.modal.close(); 
  $.blockUI();  
  $(formJqueryHandle).ajaxSubmit(options);
  return false;
}

/** add a css to the page */
function guiAddCss(cssUrl) {
  var linkElement=document.createElement("link");
  linkElement.rel = "stylesheet";
  linkElement.type = "text/css";
  linkElement.href = cssUrl;
    
  var headElement = document.getElementsByTagName("head")[0];         
  headElement.appendChild(linkElement);
  
}

/** get the opener or give a friendly error */
function guiOpener() {
  
  if (opener == null) {
    alert('Error: opener is null, was this screen opened from another application?'); 
  }
  return opener;
}

function guiWindowClose() {
  if (opener == null) {
    alert('Error: opener is null, was this screen opened from another application?'); 
  }
  window.close();
  return false;
}

/**
 * submit the subject to a url
 * @param subjectId
 * @param sourceId
 * @param name
 * @param description
 * @return false
 */
function guiSubmitSubjectPickerToUrl(subjectPickerElementName, subjectId, screenLabel) {
  document.getElementById("subject.subjectPickerElementName.elementId").value = subjectPickerElementName;
  document.getElementById("subject.id.elementId").value = subjectId;
  document.getElementById("subject.screenLabel.elementId").value = screenLabel;
  document.getElementById("submitToUrlFormId").submit();
  return false;
}

/**
 * submit the attributeDefName to a url
 * attributeDefNamePickerElementName
 * @param subjectId
 * @param sourceId
 * @param name
 * @param description
 * @return false
 */
function guiSubmitAttributeDefNamePickerToUrl(attributeDefNamePickerElementName, attributeDefNameId, 
    screenLabel, attributeDefNameName, attributeDefNameDisplayName, attributeDefNameDescription) {
  //alert(screenLabel);
  document.getElementById("attributeDefName.attributeDefNamePickerElementName.elementId").value = attributeDefNamePickerElementName;
  document.getElementById("attributeDefName.id.elementId").value = attributeDefNameId;
  document.getElementById("attributeDefName.screenLabel.elementId").value = screenLabel;
  document.getElementById("attributeDefName.attributeDefNameName.elementId").value = attributeDefNameName;
  document.getElementById("attributeDefName.attributeDefNameDisplayName.elementId").value = attributeDefNameDisplayName;
  document.getElementById("attributeDefName.attributeDefNameDescription.elementId").value = attributeDefNameDescription;
  document.getElementById("submitToUrlFormId").submit();
  return false;
}

/**
 * scroll to the bottom of the page
 */
function guiScrollTo(jqueryId) {
  
  //got this here: http://beski.wordpress.com/2009/04/21/scroll-effect-with-local-anchors-jquery/
  var targetOffset = $(guiEscapeSelectorIfNeeded(jqueryId)).offset();
  var targetTop = targetOffset.top;
  
  //$('html, body').animate({scrollTop: $(document).height()},1500);
  $('html, body').animate({scrollTop: targetTop},500);
}

//just keep the state for each screen once so they dont have to keep confirming
var confirmedChanged = false;

/**
 * only check once per screen that changes can be made
 * @param prompt
 * @returns {Boolean} true if should proceed
 */
function confirmChange(prompt) {
  if (!confirmedChanged) {
    if (confirm(prompt)) {
      confirmedChanged = true;
    } else {
      return false;
    }
  }
  return true;
}

/** this does three things.  When typing in name field, syncs to id field if checkbox checked
 * when clicking checkbox, either sync and disable, or enable the id field
 * or when clicking the id field, if disabled, give a helpful message
*/
function syncNameAndId(nameElementId, idElementId, nameDifferentThanIdElementId, isElementClick, elementMessage) {

  var nameDifferentThanIdChecked = $(guiEscapeSelectorIfNeeded('#' + nameDifferentThanIdElementId)).is(':checked');
  
  //if someone clicks on the disabled textfield, then tell them they need to check the checkbox
  if (isElementClick) {
    if (!nameDifferentThanIdChecked) {
      alert(elementMessage);
    }
    return;
  } 

  //if its checked, then sync up the id with the name
  if (!nameDifferentThanIdChecked) {
    $(guiEscapeSelectorIfNeeded('#' + idElementId)).attr('disabled', 'disabled');
    var nameValue = $('#' + nameElementId).val();
    //set this in the id
    $(guiEscapeSelectorIfNeeded('#' + idElementId)).val(nameValue);
  } else {
    $(guiEscapeSelectorIfNeeded('#' + idElementId)).attr('disabled', null);
  }
  
}

/**
 * show/hide the add member block on click of Add Members button
 * Also add attributes for accessibility
 * */
function showHideMemberAddBlock() {
  
  $('#add-block-container').toggle('slow');
  if ($("#add-member-control-group").attr("aria-expanded") === 'true') {
    $("#add-member-control-group").attr("aria-expanded","false");
    $("#add-member-control-group").removeAttr("role");
  } else {
    $("#add-member-control-group").attr("aria-expanded","true");
    $("#add-member-control-group").attr("role", "alert");
    $('#groupAddMemberComboId')[0].tomselect.focus();
  } 
}

/**
 * show/hide the assign permission block on click of Assign permission button
 * Also add attributes for accessibility
 * */
function showHideAssignPermissionBlock() {
  
  $('#assign-permission-block-container').toggle('slow');
  if ($("#assign-permission-block-container").attr("aria-expanded") === 'true') {
    $("#assign-permission-block-container").attr("aria-expanded","false");
    $("#assign-permission-block-container").removeAttr("role");
  } else {    
    $("#assign-permission-block-container").attr("aria-expanded","true");
    $("assign-permission-block-container").attr("role", "alert");
    $('#permissionDefComboId')[0].tomselect.focus();
  } 
}

/**
 * show/hide stem assign attribute block on click of Assign attribute button
 * Also add attributes for accessibility
 * */
function showHideStemAssignAttributeBlock() {
  
  $('#assign-stem-attribute-block-container').toggle('slow');
  if ($("#assign-stem-attribute-block-container").attr("aria-expanded") === 'true') {
    $("#assign-stem-attribute-block-container").attr("aria-expanded","false");
    $("#assign-stem-attribute-block-container").removeAttr("role");
  } else {    
    $("#assign-stem-attribute-block-container").attr("aria-expanded","true");
    $("assign-stem-attribute-block-container").attr("role", "alert");
    $('#parentFolderComboId')[0].tomselect.focus();
  } 
}

/**
 * show/hide group assign attribute block on click of Assign attribute button
 * Also add attributes for accessibility
 * */
function showHideGroupAssignAttributeBlock() {
  
  $('#assign-group-attribute-block-container').toggle('slow');
  if ($("#assign-group-attribute-block-container").attr("aria-expanded") === 'true') {
    $("#assign-group-attribute-block-container").attr("aria-expanded","false");
    $("#assign-group-attribute-block-container").removeAttr("role");
  } else {    
    $("#assign-group-attribute-block-container").attr("aria-expanded","true");
    $("assign-group-attribute-block-container").attr("role", "alert");
    $('#parentFolderComboId')[0].tomselect.focus();
  } 
}

/**
 * show/hide attribute def assign attribute block on click of Assign attribute button
 * Also add attributes for accessibility
 * */
function showHideAttributeDefAssignAttributeBlock() {
  
  $('#assign-attribute-def-attribute-block-container').toggle('slow');
  if ($("#assign-attribute-def-attribute-block-container").attr("aria-expanded") === 'true') {
    $("#assign-attribute-def-attribute-block-container").attr("aria-expanded","false");
    $("#assign-attribute-def-attribute-block-container").removeAttr("role");
  } else {    
    $("#assign-attribute-def-attribute-block-container").attr("aria-expanded","true");
    $("assign-attribute-def-attribute-block-container").attr("role", "alert");
    $('#parentFolderComboId')[0].tomselect.focus();
  } 
}

/**
 * show/hide subject assign attribute block on click of Assign attribute button
 * Also add attributes for accessibility
 * */
function showHideSubjectAssignAttributeBlock() {
  
  $('#assign-subject-attribute-block-container').toggle('slow');
  if ($("#assign-subject-attribute-block-container").attr("aria-expanded") === 'true') {
    $("#assign-subject-attribute-block-container").attr("aria-expanded","false");
    $("#assign-subject-attribute-block-container").removeAttr("role");
  } else {    
    $("#assign-subject-attribute-block-container").attr("aria-expanded","true");
    $("assign-subject-attribute-block-container").attr("role", "alert");
    $('#parentFolderComboId')[0].tomselect.focus();
  } 
}

/**
 * show/hide local entity ws jwt key block on click of Create and download key button
 * Also add attributes for accessibility
 * */
function showHideLocalEntityCreateDownloadKeyBlock() {
  
  $('#wsJwtKey-create-download-block-container').toggle('slow');
  if ($("#wsJwtKey-create-download-block-container").attr("aria-expanded") === 'true') {
    $("#wsJwtKey-create-download-block-container").attr("aria-expanded","false");
    $("#wsJwtKey-create-download-block-container").removeAttr("role");
  } else {    
    $("#wsJwtKey-create-download-block-container").attr("aria-expanded","true");
    $("wsJwtKey-create-download-block-container").attr("role", "alert");
    $('#parentFolderComboId')[0].tomselect.focus();
  } 
}

/**
 * show/hide membership assign attribute block on click of Assign attribute button
 * Also add attributes for accessibility
 * */
function showHideMembershipAssignAttributeBlock() {
  
  $('#assign-membership-attribute-block-container').toggle('slow');
  if ($("#assign-membership-attribute-block-container").attr("aria-expanded") === 'true') {
    $("#assign-membership-attribute-block-container").attr("aria-expanded","false");
    $("#assign-membership-attribute-block-container").removeAttr("role");
  } else {    
    $("#assign-membership-attribute-block-container").attr("aria-expanded","true");
    $("assign-membership-attribute-block-container").attr("role", "alert");
    $('#parentFolderComboId')[0].tomselect.focus();
  } 
}

/**
 * show/hide the relative/absolute date ranges in the group/folder audit log history chart
 * */
function showHideActivityChartFormDates(element) {
  document.querySelectorAll('.chartRangeOption').forEach(function(element) {
    element.classList.remove('active')
  })

  if (element.id === 'dateFromRelativeOptionId') {
    document.getElementById('dateRangeTypeId').value = 'relative'
    if ($("#date-range-relative-block-container").attr("aria-expanded") === 'true') {
      // do nothing
    } else {
      $('#date-range-relative-block-container').toggle('fast');
      $("#date-range-relative-block-container").attr("aria-expanded", "true");
      $("#date-range-relative-block-container").attr("role", "alert");
    }

    if ($("#date-range-absolute-block-container").attr("aria-expanded") === 'true') {
      $('#date-range-absolute-block-container').toggle('fast');
      $("#date-range-absolute-block-container").attr("aria-expanded", "false");
      $("#date-range-absolute-block-container").removeAttr("role");
    }
  }

  else if (element.id === 'dateFromAbsoluteOptionId') {
    document.getElementById('dateRangeTypeId').value = 'absolute'
    if ($("#date-range-absolute-block-container").attr("aria-expanded") === 'true') {
      // do nothing
    } else {
      $('#date-range-absolute-block-container').toggle('fast');
      $("#date-range-absolute-block-container").attr("aria-expanded", "true");
      $("#date-range-date-range-absolute-block-container-block-container").attr("role", "alert");
    }

    if ($("#date-range-relative-block-container").attr("aria-expanded") === 'true') {
      $('#date-range-relative-block-container').toggle('fast');
      $("#date-range-relative-block-container").attr("aria-expanded", "false");
      $("#date-range-relative-block-container").removeAttr("role");
    }
  }

  element.classList.add('active')

  return false
}

/**
 * show the privileges block on click of Custom Privileges radio button
 * Also add attributes for accessibility
 * */
function showCustomPrivilege(elementId) {
  
  $('#'+elementId).show('slow');
  $('#'+elementId).attr("aria-expanded","true");
  $('#'+elementId).attr("role", "alert");
  
  //$("#add-members-privileges").show('slow');
  //$("#add-members-privileges").attr("aria-expanded","true");
  //$("#add-members-privileges").attr("role", "alert");
  
}

/**
 * Hide the privileges block on click of Default Privileges radio button
 * Also add attributes for accessibility
 * */
function hideCustomPrivilege(elementId) {
  
  $('#'+elementId).hide('slow');
  $('#'+elementId).attr("aria-expanded","false");
  $('#'+elementId).removeAttr("role");
  
  //$("#add-members-privileges").hide('slow');
  //$("#add-members-privileges").attr("aria-expanded","false");
  //$("#add-members-privileges").removeAttr("role");
  
}


// this will set the url in the browser so the back button works with the filter
function grouperAssignDaemonUrl() {
  var url = window.location.href; 
  var question = url.indexOf('?'); 
  if (question > 0) { 
    url = url.substring(0,question); 
  } 
  url += '?operation=UiV2Admin.daemonJobs';
  url += '&daemonJobsFilter=' + $("#daemonJobsFilterId").val();
  url += '&daemonJobsCommonFilter=' + $("#daemonJobsCommonFilterId option:selected").val();
  url += '&daemonJobsStatusFilter=' + $("#daemonJobsStatusFilterId option:selected").val();
  url += '&daemonJobsFilterShowExtendedResults[]=' + ($("#daemonJobsFilterShowExtendedResultsId").is(':checked') ? 'on' : '');
  url = encodeURI(url);
  history.pushState(null, null, url);
}

// theres a problem with back button and ajax where scheduled tasks stay around, kill them all
// pass in the current entry minus 1
function grouperCancelAllScheduledTasks(taskStart) {
  for (var i = taskStart; i >= 0; i--) {
    window.clearInterval(i);
    window.clearTimeout(i);
    if (typeof window.mozCancelAnimationFrame === "function") {
      window.mozCancelAnimationFrame(i); // Firefox
    }
  }
}

/**
 * Execute the ajax request for a Grouper TomSelect combobox.
 *
 * URL / query syntax notes:
 * - `url` should be the base endpoint with any required params already on it (e.g. groupId).
 * - This helper appends `name=` as a query param (uses `?` or `&` as appropriate) and an encoded query.
 *   (So do NOT pass a url that already includes `name=`.)
 * - If you need additional filter params from other form fields, pass their names via `extraUrlOptions`
 *   and `_addUrlOptions(...)` will append them.
 *
 * Expected JSON:
 * - Either an array of items, or an object with an `items` array.
 * - Each item should have at least: { id: "...", name: "..." }
 * - Optional: htmlLabel, used for rendering if present.
 *
 * @param url base url to call (should NOT already include name=)
 * @param extraUrlOptions options passed to _addUrlOptions
 * @param query the query string (typed text, or id for exact lookup)
 * @param appendWildcard if true, appends '*' to the query (server-side wildcard)
 * @param callback callback(items)
 */
function grouperRegisterComboboxAjax(url, extraUrlOptions, query, appendWildcard, callback) {

  try {
    // Decide whether to start query params with '?' or '&'
    // (If the base url already has a '?', we append with '&', otherwise start with '?')
    var joinChar = (url && url.indexOf('?') === -1) ? '?' : '&';

    // Build the name query parameter.
    // Note: encodeURIComponent encodes the whole value, including '*'. That's OK if the server decodes it.
    var baseUrl = url + joinChar + 'name=' + encodeURIComponent(query + (appendWildcard ? '*' : ''));

    // Append extra filter params from screen state.
    // `_addUrlOptions` returns the leading ? or & as needed.
    var extra = _addUrlOptions(baseUrl, extraUrlOptions || {});
    var finalUrl = baseUrl + extra;

    // Build request headers
    var headers = { 'Accept': 'application/json' };

    // Mark request as AJAX (some server-side logic relies on this conventional header)
    headers['X-Requested-With'] = 'XMLHttpRequest';

    // Add OWASP CSRF token header if present on the page
    var owaspTokenName = 'OWASPCSRFTOKEN';
    var owaspTokenEls = document.getElementsByName(owaspTokenName);
    if (owaspTokenEls != null && owaspTokenEls.length > 0 && owaspTokenEls[0] != null) {
      headers[owaspTokenName] = owaspTokenEls[0].value;
    }

    fetch(finalUrl, { headers: headers })
      .then(async (response) => {
        var text = await response.text();
        if (!response.ok) {
          throw new Error('HTTP ' + response.status + ': ' + text);
        }

        // Parse JSON, but keep the raw text so we can show it in an error.
        var json;
        try {
          json = JSON.parse(text);

          /*
           * Example JSON response:
           *
           * {
           *   "items": [
           *     {
           *       "id": "jdbc||test.subject.2",
           *       "name": "description.test.subject.2",
           *       "htmlLabel": "<!--guiSubjectLongLinkWithIcon--><i class=\"fa fa-user\"></i>  description.test.subject.2"
           *     }
           *   ],
           *   "label": "name",
           *   "identifier": "id"
           * }
           */
        } catch (e) {
          throw new Error('Non-JSON response: ' + text);
        }

        // Normalize to an array of items.
        var items = Array.isArray(json) ? json : (json.items || []);
        callback(items);
      })
      .catch((err) => {
        // Network, HTTP, or JSON parse errors end up here.
        console.error('TomSelect load error:', err);
        callback();
      });
  } catch (e) {
    // Any synchronous error building the URL, etc.
    console.error('TomSelect load exception:', e);
    callback();
  }
}

/**
 * Register a Tom Select combobox on a selector.
 *
 * Behavior summary:
 * - Normal typing:
 *   - Only loads after 2+ characters
 *   - Appends '*' to the query to use server-side wildcard matching
 *   - Uses Tom Select throttling (loadThrottle) to avoid spamming requests
 *   - Clears prior search results so each search shows fresh options
 *
 * - Enter-to-lookup (optional):
 *   - If options.useEnterForLookup === true, pressing Enter triggers an *exact* lookup (no '*')
 *   - Exact lookup is executed immediately (bypasses loadThrottle)
 *   - If the response includes an exact id match (or only one result), it is auto-selected
 *   - If fewer than 2 characters are typed, Enter does nothing
 *   - After Enter, the control exits (no dropdown, no cursor)
 *
 * - Initial value:
 *   - If `value` (an id) is passed, we call grouperComboboxSetId(...) so the label renders like a selection
 *
 * CSS note (optional UI behavior):
 * - To hide the selected item chip while typing, put this in your CSS:
 *     .grouper-ts-typing .ts-control .item { display:none !important; }
 *
 * @param jquerySelector e.g. "#someId"
 * @param url base endpoint, e.g. "../app/UiV2Group.addMemberFilter?groupId=..." (no name=)
 * @param additionalFormElementNames comma-separated form element names to send along with ajax request
 * @param value initial id to select (label is resolved via ajax)
 * @param options e.g. {searchDelay: 500, useEnterForLookup: true}
 */
function grouperRegisterCombobox(jquerySelector, url, additionalFormElementNames, value, options) {

  options = options || {};
  var useEnterForLookup = (options.useEnterForLookup === true);

  // Names of additional form elements to send as extra params on the ajax request.
  // `_addUrlOptions` will read these elements and add them to the URL.
  var extraUrlOptions = {
    optionalFormElementNamesToSend: additionalFormElementNames
  };

  // Clear any previous search results before showing new ones.
  // Tom Select retains options internally; without clearing, an old option can appear at the top on subsequent searches
  function grouperResetOptionsKeepSelection(tsInstance) {
    try {
      var keep = [];
      var selectedValues = (tsInstance.items || []).slice();
      for (var i = 0; i < selectedValues.length; i++) {
        var opt = tsInstance.options && tsInstance.options[selectedValues[i]];
        if (opt) {
          keep.push(opt);
        }
      }
      if (typeof tsInstance.clearOptions === 'function') {
        tsInstance.clearOptions();
      }
      if (keep.length && typeof tsInstance.addOption === 'function') {
        tsInstance.addOption(keep);
      }
    } catch (e) {
      // ignore
    }
  }

  // Tom Select configuration.
  // - loadThrottle is Tom Select's request debounce.
  // - If searchDelay is null/undefined, default to 500ms.
  var tomSelectOptions = {
    maxItems: 1,
    loadThrottle: (typeof options.searchDelay !== 'undefined' && options.searchDelay !== null) ? options.searchDelay : 500,

    onItemAdd: function() {
      // Blur after the user selects an option so it behaves like a typical single-select.
      // Also ensure we are not in "typing" mode so the selected item is visible again.
      try {
        if (this.wrapper) {
          this.wrapper.classList.remove('grouper-ts-typing');
        }
      } catch (e) {
        // ignore
      }
      this.blur();
    },

    valueField: 'id',
    labelField: 'name',
    searchField: ['name', 'id'],     // still fine to keep so if id is there it still shows
    shouldSort: false,
    // dont let tomselect score things, we will let the server do it
    score: function() { return function() { return 1; }; },
    maxOptions: 200,

    shouldLoad: function(query) {
      // Normal typing: wait until 2+ chars.
      return query.length >= 2;
    },

    load: function(query, callback) {
      // Normal typing uses wildcard matching and Tom Select throttling (loadThrottle).
      grouperRegisterComboboxAjax(url, extraUrlOptions, query, true, function(items) {
        // Reset options so each search shows only the latest results (plus any current selection).
        grouperResetOptionsKeepSelection(this);

        // Provide results to Tom Select.
        callback(items);
      }.bind(this));
    },

    // Render htmlLabel if provided (server can send icon markup).
    // Force nowrap so long labels don't wrap.
    render: {
      option: function(item, escape) {
        return '<div style="white-space: nowrap;">' + (item.htmlLabel || escape(item.name)) + '</div>';
      },
      item: function(item, escape) {
        return '<div style="white-space: nowrap;">' + (item.htmlLabel || escape(item.name)) + '</div>';
      }
    }
  };

  var ts = new TomSelect(jquerySelector, tomSelectOptions);

  // Remember ajax config on the instance so programmatic setters don't need url/extra params.
  ts._grouperUrl = url;
  ts._grouperExtraUrlOptions = extraUrlOptions;

  // Hide the selected item in the control while the user is typing.
  // - Clicking/focusing should NOT hide the selected item.
  // - Once the user types, it hides (CSS controls the actual hiding).
  function grouperSetTypingMode(isTyping) {
    if (!ts.wrapper) {
      return;
    }
    if (isTyping) {
      ts.wrapper.classList.add('grouper-ts-typing');
    } else {
      ts.wrapper.classList.remove('grouper-ts-typing');
    }
  }

  if (ts.control_input) {
    ts.control_input.addEventListener('focus', function() {
      // Clicking/focusing should NOT hide the selected item.
      grouperSetTypingMode(false);
    });
    ts.control_input.addEventListener('input', function() {
      // Hide only once they actually start typing.
      grouperSetTypingMode(ts.control_input.value && ts.control_input.value.length > 0);
    });
    ts.control_input.addEventListener('blur', function() {
      grouperSetTypingMode(false);
    });
  }

  // Enter handling:
  // - Attach to ts.wrapper (capture phase) instead of control_input because Tom Select may replace the input.
  // - On Enter, do an exact lookup immediately (no '*') and bypass loadThrottle.
  // - After Enter, always exit the control UI (no dropdown, no cursor).
  if (useEnterForLookup && ts.wrapper) {

    var grouperEnterHandler = function(e) {
      var key = e.key || e.keyCode;
      if (key !== 'Enter' && key !== 13) {
        return;
      }

      // If the dropdown is open and an option is highlighted, let TomSelect's
      // native Enter handling select that option (keyboard accessibility).
      if (ts.isOpen && ts.activeOption) {
        return;
      }

      var inputEl = ts.control_input || (e.target && e.target.tagName && e.target.tagName.toUpperCase() === 'INPUT' ? e.target : null);
      if (!inputEl) {
        return;
      }

      var q = inputEl.value;
      if (!q || q.length < 2) {
        // If fewer than 2 characters are typed, do not trigger Enter-to-lookup logic.
        return;
      }

      e.preventDefault();
      e.stopPropagation();

      // Exit UI immediately.
      try { ts.close(); } catch (e2) {}
      try { ts.blur(); } catch (e2) {}

      // Exact lookup (no '*') on Enter.
      grouperRegisterComboboxAjax(url, extraUrlOptions, q, false, function(items) {
        // If we got a direct id match (or a single result), select it.
        var match = null;
        if (items && items.length) {
          for (var i = 0; i < items.length; i++) {
            if (String(items[i].id) === String(q)) {
              match = items[i];
              break;
            }
          }
          if (!match && items.length === 1) {
            match = items[0];
          }
        }

        if (match) {
          ts.addOption(match);
          ts.setValue(match.id, true);

          // Keep only the selected option so old search results don't float to the top later.
          grouperResetOptionsKeepSelection(ts);
        }

        // Always exit TomSelect UI after Enter: no dropdown, no cursor.
        try { ts.close(); } catch (e3) {}
        try { ts.blur(); } catch (e3) {}
      });
    };

    ts.wrapper.addEventListener('keydown', grouperEnterHandler, true);
  }

  // If an initial value is provided, call the programmatic setter (which will do an exact lookup and set the label).
  if (!guiIsEmpty(value)) {
    grouperComboboxSetId(jquerySelector, value);
  }

  return ts;
}

function grouperDestroyTomSelectInContainer(containerSelectorOrEl) {
  var el = (typeof containerSelectorOrEl === 'string')
    ? document.querySelector(containerSelectorOrEl)
    : containerSelectorOrEl;

  if (!el) return;

  el.querySelectorAll('input,select').forEach(function(field) {
    if (field.tomselect) {
      try { field.tomselect.destroy(); } catch (e) {}
    }
  });
}

/**
 * Programmatically select an id in a Grouper TomSelect combobox and resolve its label via AJAX.
 *
 * This uses the url/extraUrlOptions remembered on the TomSelect instance created by
 * grouperRegisterCombobox(...):
 *   ts._grouperUrl
 *   ts._grouperExtraUrlOptions
 *
 * Example:
 *   grouperComboboxSetId('#users', 'jdbc||test.subject.2');
 *
 * @param jquerySelector selector string ("#users") OR a jQuery object ($("#users"))
 * @param idValue the id to select
 */
function grouperComboboxSetId(jquerySelector, idValue) {

  if (idValue == null || idValue === '') {
    return;
  }

  // Support either a selector string or a jQuery object.
  var selector = null;
  if (typeof jquerySelector === 'string') {
    selector = jquerySelector;
  } else if (jquerySelector && jquerySelector.jquery && jquerySelector.length) {
    selector = jquerySelector.selector || null;
  }

  var el = null;
  if (selector) {
    el = document.querySelector(selector);
  } else if (jquerySelector && jquerySelector.jquery && jquerySelector.length) {
    el = jquerySelector[0];
  }

  if (!el) {
    throw new Error('grouperComboboxSetId: element not found');
  }
  if (!el.tomselect) {
    throw new Error('grouperComboboxSetId: TomSelect instance not found on element');
  }

  var ts = el.tomselect;

  // Pull url + extra params from the TomSelect instance created by grouperRegisterCombobox.
  var url = ts._grouperUrl;
  if (!url) {
    throw new Error('grouperComboboxSetId: url not found on TomSelect instance; call grouperRegisterCombobox first');
  }
  var extraUrlOptions = ts._grouperExtraUrlOptions || {};

  // Exact lookup (no '*') using the id so the server returns the item with the label.
  grouperRegisterComboboxAjax(url, extraUrlOptions, String(idValue), false, function(items) {

    if (items && items.length) {
      // Prefer exact id match; otherwise use the first result.
      var found = null;
      for (var i = 0; i < items.length; i++) {
        if (String(items[i].id) === String(idValue)) {
          found = items[i];
          break;
        }
      }
      if (!found) {
        found = items[0];
      }

      ts.addOption(found);
      ts.setValue(found.id, true);
      return;
    }

    // Fallback: set the raw value even if we couldn't resolve the label.
    ts.setValue(idValue, true);
  });
}


/**
 * Refreshes the dijit Treefolder navigation to expand folders to the current
 * object and highlight it. It simulates manually clicking
 * from root; i.e., if the object hasn't been loaded, it will query all
 * the unloaded folders in the path and load them
 */
function openFolderTreePathToObject(pathArray) {
  folderTree.set('path', pathArray);
}

function showLinkToRefreshSubjectSourceAttributes(focusOnElementName) {
	var href = window.location.href;
	if (href.indexOf('editSubjectSource') != -1) {
	  var subjectSourceId = $('#config_id_id').val();	
      var url = '../app/UiV2SubjectSource.editSubjectSource?focusOnElementName='+focusOnElementName+'&subjectSourceId='+subjectSourceId;
      ajax(url, {formIds: 'sourceConfigDetails'});	
	} else {
		ajax('../app/UiV2SubjectSource.addSubjectSource?focusOnElementName='+focusOnElementName+'&subjectSourceConfigId='+ $('#subjectSourceConfigId').val() +'&subjectSourceConfigType='+$('#subjectSourceConfigTypeId').val(), {formIds: 'sourceConfigDetails'});	
	}
	 
	return false;
	
}

function showLinkToRefreshProvisioningConfig(focusOnElementName, provisionerConfigId, provisionerConfigType) {

  var href = window.location.href;
  if (href.indexOf('editProvisionerConfiguration') != -1) {
    var url = '../app/UiV2ProvisionerConfiguration.editProvisionerConfiguration?focusOnElementName='+focusOnElementName+'&provisionerConfigId='+provisionerConfigId+'&provisionerConfigType='+provisionerConfigType;
    ajax(url, {formIds: 'provisionerConfigDetails'});  
  } else {
    ajax('../app/UiV2ProvisionerConfiguration.addProvisionerConfiguration?focusOnElementName='+focusOnElementName+'&provisionerConfigId='+provisionerConfigId+'&provisionerConfigType='+provisionerConfigType, {formIds: 'provisionerConfigDetails'}); 
  }
   
  return false;
  
}

function handleGuiV2LinkClick(event, url, options) {
  // Check if the event is a left-click without Ctrl or Meta (Cmd) keys
  if (!event.ctrlKey && !event.metaKey && event.button === 0) {
    event.preventDefault(); // Prevent the default action (navigating to the URL)

    // Perform the AJAX call here
    return guiV2link(url, options)
  } else {
    // right-click etc. goes to the url page without ajax
    if (options !== null) {
      event.currentTarget.href += _addUrlOptions(event.currentTarget.href, options);
    }
    return true;
  }
}

function configurationFileExport(event, url, options) {
    event.preventDefault();
    url += _addUrlOptions(url, options);

    $.blockUI();

    fetch(url, {
      method: 'GET',
      credentials: 'same-origin'
    }).then(function(response) {
      var contentType = response.headers.get('Content-Type') || '';
      if (contentType.indexOf('application/octet-stream') !== -1) {
        $.unblockUI();
        window.location.href = url;
      } else {
        return response.json().then(function(json) {
          guiProcessJsonResponse(json);
          $.unblockUI();
        });
      }
    }).catch(function() {
      $.unblockUI();
    });

    return false;
}

// sometimes window is blocked on back button
$(window).on("unload", function() {
  $.unblockUI();
});

(function ($) {
  $(window).on('load', function () {
    setTimeout(function () {
      $('a[role="button"]').on('keyup', function(e) {
        var keyD = e.key !== undefined ? e.key : e.keyCode;
        if ( (keyD === 'Enter' || keyD === 13) || (['Spacebar', ' '].indexOf(keyD) >= 0 || keyD === 32)) {
          e.preventDefault();
          this.click();
        }
      });
    }, 1000);
  });
})(jQuery);

/**
 * Copy text from an element to the clipboard and show a subtle "Copied!" tooltip.
 * @param elementId the ID of the element whose innerText to copy
 */
function grouperCopyToClipboard(elementId) {
  var el = document.getElementById(elementId);
  if (!el) return;
  var text = el.innerText;
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(function() {
      grouperShowCopiedTooltip(el);
    });
  } else {
    var textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.opacity = '0';
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
    grouperShowCopiedTooltip(el);
  }
}

/**
 * Copy the full job message text from an abbreviateTextarea container.
 * Works before and after the "more" button is clicked because it reads from the textarea
 * element (which always contains the full text), falling back to the span if no textarea
 * exists (i.e. the message was short enough to display in full).
 * @param linkEl the clicked anchor element (the copy icon link)
 */
function grouperCopyJobMessage(linkEl) {
  var container = linkEl.parentNode.querySelector('.jobMessageContainer');
  if (!container) return;
  var textarea = container.querySelector('textarea');
  var text;
  if (textarea) {
    text = textarea.value;
  } else {
    text = container.innerText;
  }
  if (!text) return;
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(function() {
      grouperShowCopiedTooltip(linkEl);
    });
  } else {
    var tempArea = document.createElement('textarea');
    tempArea.value = text;
    tempArea.style.position = 'fixed';
    tempArea.style.opacity = '0';
    document.body.appendChild(tempArea);
    tempArea.select();
    document.execCommand('copy');
    document.body.removeChild(tempArea);
    grouperShowCopiedTooltip(linkEl);
  }
}

/**
 * Show a brief green "Copied to clipboard" label next to the element, then fade and remove it.
 * Uses the externalized text from grouperCopiedToClipboardText (set in commonBottom.jsp)
 * with a fallback to "Copied to clipboard" if the variable is not set.
 * @param el the DOM element near which to show the tooltip
 */
function grouperShowCopiedTooltip(el) {
  var tip = document.createElement('span');
  tip.textContent = (typeof grouperCopiedToClipboardText !== 'undefined' && grouperCopiedToClipboardText)
      ? grouperCopiedToClipboardText : 'Copied to clipboard';
  tip.style.cssText = 'margin-left:6px;color:#5cb85c;font-size:0.85em;font-weight:bold;opacity:1;transition:opacity 0.5s';
  el.parentNode.insertBefore(tip, el.nextSibling && el.nextSibling.nextSibling);
  setTimeout(function() { tip.style.opacity = '0'; }, 1200);
  setTimeout(function() { if (tip.parentNode) tip.parentNode.removeChild(tip); }, 1800);
}
