
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
    
    if (typeof urlArgObjectMap.operation == 'undefined') {
      //alert('going back to index: ' + location.href);
      
      //if the url is an external URL, then go to the external index page
      if (!guiIsEmpty(location.href) && location.href.indexOf("/grouperExternal/appHtml/grouper.html") != -1) {
        location.href = "grouper.html?operation=ExternalSubjectSelfRegister.index";
      } else if (!guiIsEmpty(location.href) && location.href.indexOf("/test/") != -1) {
        //nothing
      } else {
        location.href = "grouper.html?operation=Misc.index";
      }
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
 * go to a url, e.g. operation=UiV2Group.viewGroup&groupId=abc123
 * @param url
 */
function guiV2link(url, options) {

  if (typeof options == 'undefined') {
    options = {};
  }
  
  if (!options.dontScrollTop) {
    guiScrollTop();
  }
  url = '?' + url;  
  if (typeof options.optionalFormElementNamesToSend != 'undefined' && options.optionalFormElementNamesToSend != null) { 
    
    //add additional form element names to filter based on other things on the screen 
    var additionalFormElementNamesArray = guiSplitTrim(options.optionalFormElementNamesToSend, ","); 
    for (var i = 0; i<additionalFormElementNamesArray.length; i++) { 
      var additionalFormElementName = additionalFormElementNamesArray[i]; 

      //its ok if it is not there
      if (document.getElementsByName(additionalFormElementName) != null
          && document.getElementsByName(additionalFormElementName).length > 0
          && document.getElementsByName(additionalFormElementName)[0] != null) {
        url += url.indexOf("?") == -1 ? "?" : "&"; 
        url += additionalFormElementName + "="; 
        //this will work for simple elements 
        url += encodeURIComponent(document.getElementsByName(additionalFormElementName)[0].value); 
      }
    } 
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

/**
 * take a url for ajax with an operation=Something.else and call ajax with it
 * @param url
 */
function guiProcessUrlForAjax(url) {

  //clear the error div...
  $('#messaging').hide().empty();
  
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
function guiMessageHelper(messageType, message) {
  
  if (messageType != 'success' && messageType != 'info' && messageType != 'error') {
    alert('messageType must be success, info, or error: ' + messageType);
  }
  
  var finalMessage = '<div role="alert" class="alert alert-' + messageType 
    + '"><button type="button" class="close" data-dismiss="alert">&times;</button>'
    + message + '</div>';
  $('#messaging').hide().empty().append(finalMessage).slideDown('slow');
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
  
  $(jqueryKey).html(html);
  
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
    poundString = URLDecode(poundString);
    
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

/** list of combos by id */
var allComboboxes = new Object();

/**
 * register a combobox div
 * @param divId is comboname plus "Id"
 * @param width
 * @param useImages true or false, if images are in the combobox
 * @param filterUrl
 * @param additionalFormElementNames send more form element names to the filter operation, comma separated
 */
function guiRegisterDhtmlxCombo(divId, comboName, width, useImages, filterUrl, comboDefaultText, comboDefaultValue, additionalFormElementNames ) {
  /* long hand...
   var simpleMembershipUpdateAddMemberSelect=new dhtmlXCombo(
      "simpleMembershipUpdateAddMemberDiv","simpleMembershipUpdateAddMember",200, 'image');
    simpleMembershipUpdateAddMemberSelect.enableFilteringMode(
       true,"../app/SimpleMembershipUpdate.filterUsers",false); */
  var theCombo=new dhtmlXCombo(
      divId,comboName,width, useImages ? 'image' : undefined);
  
  filterUrl = guiDecorateUrl(filterUrl);
  
  comboboxFormElementNamesToSend[comboName] = additionalFormElementNames;
  
  //remember the combo names for when the request goes out
  theCombo.enableFilteringMode(true,filterUrl,false);
  
  //note, for some reason the default value has to be above the default text...
  if (!guiIsEmpty(comboDefaultValue)) {
    theCombo.setComboValue(comboDefaultValue);
  }
  if (!guiIsEmpty(comboDefaultText)) {
    theCombo.setComboText(comboDefaultText);
  }

  //add hidden throbber
  var textfield = $('div#' + divId + ' :text');
  var textfieldDropdownImage = $('div#' + divId +   ' .dhx_combo_img');

  textfieldDropdownImage.after(
      "<img style='position:absolute;top:2px;display:none' alt='busy...' " +
      "src='../../grouperExternal/public/assets/images/busy.gif' id='comboThrobberId_"
      + divId + "' class='comboThrobber'  />");


  //add a throbbber
  theCombo.attachEvent("onXLS",function(){
    var textfieldDropdownImage = $('div#' + divId +   ' .dhx_combo_img');

    var leftOffset = textfieldDropdownImage.css('display') == 'none' ? 0 : (-1 * textfieldDropdownImage.width());

    var textfieldDiv = $('div#' + divId + ' .dhx_combo_box');
    var throbber = $('#comboThrobberId_' + divId);

    leftOffset = ((textfieldDiv.width() - (throbber.width() + 3)) + leftOffset);
    throbber.css("left", leftOffset + "px");
    throbber.show();
  });

  //remove throbber
  theCombo.attachEvent("onXLE",function(){
    $('#comboThrobberId_' + divId).hide();
  });
  
  //keep this so we can control it later
  allComboboxes[comboName] = theCombo;  
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

/**
 * unregister a widget
 * @param id
 */
function dojoUnregisterWidget(id) {
  
  var widget = dijit.byId(id);
  if (widget != null) {
    if (typeof widget.destroyRecursive != 'undefined') {
      widget.destroyRecursive();
    }
  }
}

/** init the left tree menu */
function dojoInitMenu(autoSelectNode) {

  if ((typeof folderMenuStore != 'undefined') && (folderMenuStore != null)) {
    if (typeof folderMenuStore.destroyRecursive != 'undefined') {
      folderMenuStore.destroyRecursive();
    }
  }
  if ((typeof folderTree != 'undefined') && (folderTree != null)) {
    if (typeof folderTree.destroyRecursive != 'undefined') {
      folderTree.destroyRecursive();
    }
  }
  
  var folderTreeDiv = document.getElementById('folderTree');
  
  if (isEmpty(folderTreeDiv)) {
    //it was destroyed, add a child in the contrainer
    $('#folderTreeContainerId').append('<div id="folderTree"></div>');
  }
  
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
        guiV2link('operation=UiV2AttributeDef.viewAttributeDef&attributeDefId=' + item.id);                          
        //location.href='../../grouperUi/appHtml/grouper.html?operation=SimpleAttributeUpdate.createEdit&attributeDefId=' + item.id;
      } else if (item.theType == 'attributeDefName') {
    	  guiV2link('operation=UiV2AttributeDefName.viewAttributeDefName&attributeDefNameId=' + item.id); 
        //location.href='../../grouperUi/appHtml/grouper.html?operation=SimpleAttributeNameUpdate.createEditAttributeNames&attributeDefNameId=' + item.id;

      } else {
        alert('ERROR: cant find theType on object with id: ' + item.id);
      }
    }
  }, "folderTree"); // make sure you have a target HTML element with this id

  if (autoSelectNode) {
    var itemId = null;
    var uri = new URI(location.href);
    uri.search(function (data) {
      if (data.stemId != undefined) {
        itemId = data.stemId;
      }
      if (data.groupId != undefined) {
        itemId = data.groupId;
      }
      if (data.attributeDefId != undefined) {
        itemId = data.attributeDefId;
      }

    });

    if (itemId != null) {
      folderTree.autoExpand = true;
    }
  }

  folderTree.startup();

  if (autoSelectNode) {
    folderTree.onLoadDeferred.then(function () {
      var selectedNode = null;
      if (itemId != null) {
        var array = folderTree.getNodesByItem(itemId);
        for (index = 0; index < array.length; index++) {
          selectedNode = array[index];
          selectedNode.setSelected(true);

          var parent = selectedNode.getParent();
          var sibling = parent.getNextSibling();
          while (sibling != null) {
            sibling.collapse();
            sibling = sibling.getNextSibling();
          }
        }
      }
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
  
  //copy display values of filtering selects to its hidden field
  //see if that function even exists
  if (typeof dojoCopyFilteringSelectDisplays != 'undefined') {
    dojoCopyFilteringSelectDisplays();
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
        
        //see if dhtmlx
//        if (element.type == "hidden") {
//
//          var theCombo = dhtmlxCombos[element.name];
//          if (typeof theCombo != 'undefined') {
//            //it is dhtmlx, get the label too just in case
//            options.requestParams[element.name + '_dhtmlxComboLabel'] = theCombo.getComboText();
//          }
//        }
      }
    }
  }

  //add owasp token
  
  
  var owaspCsrfTokenName = 'OWASP_CSRFTOKEN';
  var owaspCsrfTokenHeader = {};
  if (document.getElementsByName(owaspCsrfTokenName) != null
      && document.getElementsByName(owaspCsrfTokenName).length > 0
      && document.getElementsByName(owaspCsrfTokenName)[0] != null) {
    owaspCsrfTokenHeader = {OWASP_CSRFTOKEN: document.getElementsByName(owaspCsrfTokenName)[0].value};
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
    timeout: 180000,
    async: true,
    //TODO handle errors success better.  probably non modal disappearing reusable window
    error: function(jqXHR, textStatus, errorThrown) {
        
      $.unblockUI();
      
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
    //TODO process the response object
    success: function(json){
      guiProcessJsonResponse(json);
      $.unblockUI();  
    }
  });
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
  
  if (typeof dojo != 'undefined' && typeof dojo.parser != 'undefined') {
    //parse the new doc for changes
    dojo.parser.parse();
  }
  
  //round those corners
  guiRoundCorners();

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
     $(guiScreenAction.innerHtmlJqueryHandle).html(guiScreenAction.html);
  }

  //append html
  if (!guiIsEmpty(guiScreenAction.appendHtmlJqueryHandle)) {
    $(guiScreenAction.appendHtmlJqueryHandle).append(guiScreenAction.html);
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
    
    //alert(guiScreenAction.alert);
    //$.modal.close();
    $('<div class="guimodal simplemodal-guiinner' + (centered ? ' simplemodal-guiinnerCentered' : '') + '">' 
        + guiScreenAction.alert + '<div class="simplemodal-buttonrow"><button class=\'simplemodal-close blueButton\'>OK</button></div></div>').modal({close: true, position: [20,20]});
  }
  
  //do an alert
  if (!guiIsEmpty(guiScreenAction.dialog)) {
    $.unblockUI(); 
    $('<div class="guimodal simplemodal-dialoginner">' 
        + guiScreenAction.dialog + '</div>').modal({close: true, position: [20,20]});
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
      $(selectElement).html(optionString);
    }
  }
  if (!guiIsEmpty(guiScreenAction.formFieldName)) {
    guiFormElementAssignValue(guiScreenAction.formFieldName, guiScreenAction.formFieldValues);
  }
  if (!guiIsEmpty(guiScreenAction.message)) {
    guiMessageHelper(guiScreenAction.messageType, guiScreenAction.message);
    guiScrollTop();
  }
  if (!guiIsEmpty(guiScreenAction.validationMessage)) {
    guiMessageHelper(guiScreenAction.messageType, guiScreenAction.validationMessage);
    guiScrollTop();
    //put up the validation error thing
    //TODO if the handle doesnt exist, throw error to help develop, sometimes the error is thrown before JSP is drawn wont work
    $(guiScreenAction.innerHtmlJqueryHandle).after('&nbsp;<a class="validationError" href="#" onclick="alert(\'' + guiEscapeHtml(guiScreenAction.validationMessage, true) + '\'); return false;"><i class="fa fa-exclamation-triangle fa-lg" style="color:#CC3333;"></i></span>');
  }
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
  
  //see if combo
  if (!guiIsEmpty(allComboboxes[name])) {
    if (guiIsEmpty(values)) {
      allComboboxes[name].clearAll(true);
    } else {
      values = guiConvertToArray(values, true);
      //assign a value
      if (values.length == 1) {
        if (guiIsEmpty(values[0])) {
          allComboboxes[name].clearAll(true);
        } else {
          allComboboxes[name].setComboValue(values[0]);
        }
      } else {
        alert("Cant have length more than 1");
      }
    }
    
    return;
  }

  
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
  //NOTE, we need to unescape the HTML, since it is in a javascript call...
  message = guiEscapeHtml(message, false);
  //not sure why this was max width 400, now it is 0 so it doesnt truncate...
  Tip(message, WIDTH, 0, FOLLOWMOUSE, false);
} 

/** call this from button to hide/show some text */
function guiToggle(event, jqueryElementKey) {
  eventCancelBubble(event);
  $(jqueryElementKey).toggle('slow'); 
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
  var buttons = $('.buttons_' + hideShowName); 
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
    $('.shows_' + hideShowName).fadeIn('slow');
    $('.hides_' + hideShowName).fadeOut('slow');
    for (var i = 0; i < buttons.length; i++) { 
      var button = buttons[i];
      //could be an image or something
      if (!guiIsEmpty(button.innerHTML)) {
        $(button).html(hideShow.textWhenShowing); 
      }
    }
  } else {
    $('.shows_' + hideShowName).fadeOut('slow');
    $('.hides_' + hideShowName).fadeIn('slow');
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

/** this is the id of the link or button which opened a context menu */
var guiMenuIdOfMenuTarget;

/**
 * @param menuId is the id of the HTML element of the menu
 * @param operation is when events occur (onclick), then that operation is called via ajax
 * @param structureOperation is the operation called to define the structure of the menu
 * @param isContextMenu is true if context menu, false if not
 * @param contextZoneJqueryHandle is the jquery handle (e.g. #someId) which this menu should be attached to.  note
 * that any element you are attaching to must have an id attribute defined
 */
function guiInitDhtmlxMenu(menuId, operation, structureOperation, isContextMenu, contextZoneJqueryHandle) {
// here is the long hand form of this method
//  var menu;
//  function initAdvancedMenu() {
//
//    menu = new dhtmlXMenuObject("advancedMenu", "dhx_blue");
//    menu.addContextZone("advancedLink");
//    menu.setImagePath("../public/assets/dhtmlx/menu/imgs/");
//    menu.setIconsPath("../public/assets/dhtmlx/menu/icons/");
//
//    menu.renderAsContextMenu();
//    //menu.loadXML("dhtmlxmenu.xml?e="+new Date().getTime());
//    menu.loadXML("../app/SimpleMembershipUpdate.advancedMenuStructure");
//    menu.attachEvent("onClick", function(id, zoneId, casState){
//      menu.hideContextMenu();
//      ajax("SimpleMembershipUpdate.advancedMenu", {requestParams: {menuHtmlId: zoneId, menuItemId: id }});
//    });
//    menu.attachEvent("onCheckboxClick", function(id, state, zoneId, casState){
//      menu.hideContextMenu();
//      ajax("SimpleMembershipUpdate.advancedMenu", {requestParams: {menuHtmlId: zoneId, menuItemId: id, menuCheckboxChecked: !state }});
//      return true;
//    });
//    menu.attachEvent("onRadioClick", function(group, idChecked, idClicked, zoneId, casState){
//      menu.hideContextMenu();
//      ajax("SimpleMembershipUpdate.advancedMenu", {requestParams: {menuHtmlId: zoneId, menuRadioGroup: group, menuItemId: idClicked }});
//      return true;
//    });
//    
//  }
//  $(document).ready(initAdvancedMenu);

  
  var theFunction = function() {

    var menu = new dhtmlXMenuObject(menuId, "dhx_blue");
    if (isContextMenu) {
      menu.renderAsContextMenu();
      var elements = $(contextZoneJqueryHandle);
      if (guiIsEmpty(elements)) {
        alert("Cant find context zone elements for menu: " + menuId + ", " + contextZoneJqueryHandle);
        return;
      }
      
      elements.click(function(e){
        //stache this in global variable, assume only one menu at a time
        guiMenuIdOfMenuTarget = $(e.target)[0].id
        menu.showContextMenu(e.pageX, e.pageY);
        return false;
      }); 
      
      //cant do it this way since the x,y is wrong
      //for (var i=0; i<elements.length; i++) {
      //  if (guiIsEmpty(elements[i].id)) {
      //    alert("Cant find id in html context zone element: " + menuId); 
      //    return;
      //  }
      //  menu.addContextZone(elements[i].id);
      //}
    }
    menu.setImagePath("../public/assets/dhtmlx/menu/imgs/");
    menu.setIconsPath("../public/assets/dhtmlx/menu/icons/");
  
    //menu.loadXML("dhtmlxmenu.xml?e="+new Date().getTime());
    if (!guiStartsWith(structureOperation, "../app/" )) {
      structureOperation = "../app/" + structureOperation; 
    }
    
    structureOperation = guiDecorateUrl(structureOperation);
    
    menu.loadXML(structureOperation);
    
    menu.attachEvent("onClick", function(id, zoneId, casState){
      var itemType = menu.getItemType(id);
      if ("radio" == itemType || "checkbox" == itemType) {
        return;
      }
      menu.hideContextMenu();
      var requestParams = {menuHtmlId: zoneId, menuItemId: id, menuEvent: 'onClick' };
      //if there is the same menu multiple places on the screen, we might want to know about it
      if (!guiIsEmpty(guiMenuIdOfMenuTarget)) {
        requestParams.menuIdOfMenuTarget = guiMenuIdOfMenuTarget;
      }
      //alert('menu.onClick()');
      ajax(operation, {requestParams: requestParams});
    });
    menu.attachEvent("onCheckboxClick", function(id, state, zoneId, casState){
      //menu.hideContextMenu();
      var requestParams = {menuHtmlId: zoneId, menuItemId: id, menuCheckboxChecked: !state, menuEvent: 'onCheckboxClick' };
      //if there is the same menu multiple places on the screen, we might want to know about it
      if (!guiIsEmpty(guiMenuIdOfMenuTarget)) {
        requestParams.menuIdOfMenuTarget = guiMenuIdOfMenuTarget;
      }
      //alert('menu.onCheckboxClick()');
      ajax(operation, {requestParams: requestParams});
      return true;
    });
    menu.attachEvent("onRadioClick", function(group, idChecked, idClicked, zoneId, casState){
      //menu.hideContextMenu();
      var requestParams = {menuHtmlId: zoneId, menuRadioGroup: group, menuItemId: idClicked, menuEvent: 'onRadioClick' };
      //if there is the same menu multiple places on the screen, we might want to know about it
      if (!guiIsEmpty(guiMenuIdOfMenuTarget)) {
        requestParams.menuIdOfMenuTarget = guiMenuIdOfMenuTarget;
      }
      //alert('menu.onRadioClick()');
      ajax(operation, {requestParams: requestParams});
      return true;
    });
  };
  $(document).ready(theFunction);
}

/**
 * parse an int if not an int already
 * @param input
 * @return
 */
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
 
var guiCalendars = new Array();

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

function guiCalendarImageClick(formElementId) {
  if (guiCalendars[formElementId].parent != null && guiCalendars[formElementId].isVisible()) {
    guiCalendars[formElementId].hide();
  } else {
  
    var textfield = document.getElementById(formElementId);

    var textfieldDate = textfield.value;
  
    //if nothing, default to today  
    if (guiIsEmpty(textfieldDate)) {
      textfieldDate = guiNow();
    }

    //convert to yyyymmdd
    var yyyymmdd = formatValid_dateformattodb('dateformattodb', null, textfieldDate);

    if (guiValidateDate(yyyymmdd)) {
      //set the date to the control
      var year = yyyymmdd.substring(0,4);
      var month = yyyymmdd.substring(4,6);
      var day = yyyymmdd.substring(6,8);
      //guiConvertStringToDateOrTimestamp(yyyymmdd, true)
      var theDate=new Date();
      theDate.setFullYear(year);
      theDate.setMonth(month-1);
      theDate.setDate(day);
      guiCalendars[formElementId].setDate(theDate);
    }
  
  
    guiCalendars[formElementId].show();
  }
  return false;
}

/** format a date from screen to the DB, format will go to ccyymmdd */
function formatValid_dateformattodb(functionName, args, x) {
  
  return guiConvertStringToDateOrTimestamp(x, true); 
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



function guiCalendarInit(formElementId) {

  //mCal = new dhtmlxCalendarObject("caltext6", false, {isWinHeader: true, isWinDrag: true});
  //mCal.draw();
  //mCal.hide();
  //mCal.attachEvent("onClick", function(date){
  //  var theDate = mCal.getFormatedDate('%Y%m%d', date);
  //  document.getElementById("text6").value = theDate;
  //  document.getElementById("text6").onblur();
  //  
  //  mCal.close();
  //
  //}) 
  
  //  <a href="#" onclick="return guiCalendarImageClick2('text6');"><img 
  //src="../gui/images/calendar.gif" border="0" alt="Pick a date" height="16" 
  //width="16" id="PZE8OLPL_image" /></a><span id="text6_theCalendar2" style="position: absolute;"></span>
  //<script>
  //  guiCalendarInit2("text6");
  //</script>

  var imageElementId = formElementId + "_theCalendar";
  var theCalendar = new dhtmlxCalendarObject(imageElementId, false, 
    {isWinHeader: true, isWinDrag: true});
  theCalendar.draw();
  theCalendar.hide();
  
  guiCalendars[formElementId] = theCalendar;

  theCalendar.attachEvent("onClick", function(date){
    var theDate = guiCalendars[formElementId].getFormatedDate('%Y%m%d', date);
    document.getElementById(formElementId).value = theDate;
    document.getElementById(formElementId).onblur();
    guiCalendars[formElementId].close();

  });   
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
      iframe: true, 
      dataType: "json", 
      success:    function(json) { 
        guiProcessJsonResponse(json);
      },
      url: operation
  };
  //$.modal.close(); 
  //$.blockUI();  
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
  var targetOffset = $(jqueryId).offset();
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

//MCH 20131223: keep track of the ids of filtering selects.  note, some might be gone due to ajax
//note the naming convention, if the bsae id is peoplePicker, then the id is peoplePickerId,
//name is peoplePickerName, displays are peoplePickerIdDisplay, and peoplePickerNameDisplay
var dojoFilteringSelectBaseIds = {};

//MCH 20131223: copy the display value of filtering selects to the hidden field so it is submitted
//with the filtering select value
function dojoAddFilteringSelectBaseId(dojoFilteringSelectBaseId) {

  dojoFilteringSelectBaseIds[dojoFilteringSelectBaseId] = true;

}

//MCH 20131223: copy the display value of filtering selects to the hidden field so it is submitted
//with the filtering select value
function dojoCopyFilteringSelectDisplays() {

  //loop through all the filtering selects that have been registered
  for(var dojoFilteringSelectBaseId in dojoFilteringSelectBaseIds) {
  
    var filteringSelect = dijit.byId(dojoFilteringSelectBaseId + 'Id');
    
    //if it hasnt been removed by javascript
    if (filteringSelect != null) {
      
      var displayValue = filteringSelect.get('displayedValue');
      
      //set this in the value of the display hidden field
      var displayInput = document.getElementById(dojoFilteringSelectBaseId + 'IdDisplay');
      
      if (displayInput != null) {
        displayInput.value = displayValue;
      }
    }
  }  
}

/** this does three things.  When typing in name field, syncs to id field if checkbox checked
 * when clicking checkbox, either sync and disable, or enable the id field
 * or when clicking the id field, if disabled, give a helpful message
*/
function syncNameAndId(nameElementId, idElementId, nameDifferentThanIdElementId, isElementClick, elementMessage) {

  var nameDifferentThanIdChecked = $('#' + nameDifferentThanIdElementId).is(':checked');
  
  //if someone clicks on the disabled textfield, then tell them they need to check the checkbox
  if (isElementClick) {
    if (!nameDifferentThanIdChecked) {
      alert(elementMessage);
    }
    return;
  } 

  //if its checked, then sync up the id with the name
  if (!nameDifferentThanIdChecked) {
    $('#' + idElementId).attr('disabled', 'disabled');
    var nameValue = $('#' + nameElementId).val();
    //set this in the id
    $('#' + idElementId).val(nameValue);
  } else {
    $('#' + idElementId).attr('disabled', null);
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
	  $("#groupAddMemberComboId").focus();
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
	  $("#assign-permission-block-container").append(input);
	  $("#assign-permission-block-container").attr("aria-expanded","true");
	  $("assign-permission-block-container").attr("role", "alert");
	  $("#permissionDefComboId").focus();
  }	
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


/**
 * call this with form or html dom element inside form
 * @param jqueryHandle e.g. #add-members-form
 */
function grouperDisableEnterOnCombo(jqueryHandleOfFormElement) {
  var jqueryElement = $(jqueryHandleOfFormElement);
  //if (!jqueryElement.is('form')) {
  //  jqueryElement = jqueryElement.closest('form');
  //}
  if (jqueryElement.length !== 0) {
    jqueryElement.on('keyup keypress', function(e) {
      var keyCode = e.keyCode || e.which;
      if (keyCode === 13) {
        e.preventDefault();
        return false;
      }
    });
  }
}
