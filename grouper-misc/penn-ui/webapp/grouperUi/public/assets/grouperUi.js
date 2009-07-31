
$(document).ready(function(){
  // Initialize history plugin.
  // The callback is called at once by present location.hash. 
  $.historyInit(pageload, "grouper.html");

});

/** init is called when app starts */
function init() {
  if (allObjects.guiSettings == null) {
    //alert('initting');
    ajax('../app/Misc/settings', initResult);
  } else {
    //skip that step
    processUrl();
  }
}

/** initResult is called in async callback to init */
function initResult(theGuiSettings) {

  allObjects.guiSettings = theGuiSettings;

  processUrl();
}

function initScreenHeaders() {
  document.title = allObjects.guiSettings.text.screenTitle;

  replaceHtmlWithTemplate('#topDiv', 'common.commonTop.html');
  
}

/** when the history button is pressed, just init for now I guess */
function pageload(hash) {
 init();
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
  
  initScreenHeaders();
  
  //operation%3DsimpleUpdate%26groupName%3Dtest%253Atest1
  //operation=simpleUpdate&groupName=test:test1
  //http://localhost:8089/grouperWs/grouperUi/appHtml/grouper.html#operation%3DsimpleMembershipUpdate%26groupName%3Dtest%253Atest1
  
  //map of url args
  var urlArgObjectMap = allObjects.appState.urlArgObjectMap();
  
  if (typeof urlArgObjectMap.operation == 'undefined') {
    $("#bodyDiv").html = "";
    alert("invalid URL, no operation");
  } else if (urlArgObjectMap.operation == 'simpleMembershipUpdate') {
    allObjects.simpleMembershipUpdateObj().init();
  } else if (urlArgObjectMap.operation == 'logout') {
    allObjects.pageHelpers.logoutAction();
  } else {
    $("#bodyDiv").html = "";
    alert("invalid URL, do not recognize operation: '" + urlArgObjectMap.operation + "'");
  }

}


function SimpleMembershipUpdate() {
  this.initted=false;                

  this.simpleMembershipUpdateInit = null;
  this.simpleMembershipList = null;

  this.init=function() {
    //only init if needed
    if (!allObjects.simpleMembershipUpdateObj().initted) {
    
      //clear this out since it looks weird if previous screen for a sec
      $("#bodyDiv").html("");
      
      ajax('../app/SimpleMembershipUpdate/init', this.initResult, this.requestParams());
    }
    this.refreshMemberList();
  };
  
  /** get the base params for a URL call */
  this.requestParams=function() {
    var theRequestParams = {};
    var urlArgObjectMap = allObjects.appState.urlArgObjectMap();
    
    if (typeof urlArgObjectMap.groupId != 'undefined') {
      theRequestParams.groupId = urlArgObjectMap.groupId;
    } else if (typeof urlArgObjectMap.groupName != 'undefined') {
      theRequestParams.groupName = urlArgObjectMap.groupName;
    } else {
      var error = "Error: need to pass in groupId or groupName in URL!";
      alert(error);
      throw error;
    }
    return theRequestParams;  
  };
  
  this.initResult=function(theSimpleMembershipUpdateInit) {
    allObjects.simpleMembershipUpdateObj().simpleMembershipUpdateInit = theSimpleMembershipUpdateInit;
    replaceHtmlWithTemplate('#bodyDiv', 'simpleMembershipUpdate.simpleMembershipUpdateMain.html');
    allObjects.simpleMembershipUpdateObj().initted = true;
  };
  
  this.refreshMemberList=function() {
      ajax('../app/SimpleMembershipUpdate/retrieveMembers', this.refreshMemberListResult, this.requestParams());
  };
  
  this.refreshMemberListResult=function(theSimpleMembershipList) {
    allObjects.simpleMembershipUpdateObj().simpleMembershipList = theSimpleMembershipList;
    replaceHtmlWithTemplate('#simpleMembershipResultsList', 'simpleMembershipUpdate.simpleMembershipMembershipList.html');
  };
}

/** object represents state of application */
function AppState() {

  this.urlInCache = null;
  
  /** dont access this directly, access with method: urlArgObjectMap() */
  this.urlArgObjects = null;
  
  /** function to get the map of url args */
  this.urlArgObjectMap = function() {
    //see if up to date
    if (this.urlInCache == location.href) {
      return this.urlArgObjects
    }
    //lets get url
    var url = location.href;
    var poundIndex = url.indexOf("#");
    if (poundIndex == -1) {
      //TODO fix this to some default screen
      alert("invalid url");
    }
    var poundString = url.substring(poundIndex + 1, url.length);
    poundString = URLDecode(poundString);
    //split out by ampersand
    var args = guiSplitTrim(poundString, "&");
    var argObject = new Object();
  
    for (var i=0;i<args.length;i++) {
      //split by =
      var equalsIndex = args[i].indexOf("=");
      if (equalsIndex == -1) {
        //TODO fix this to some default
        alert("invalid url, no equals in param: " + args[i]);
      }
      var key = args[i].substring(0,equalsIndex);
      var value = args[i].substring(equalsIndex+1,args[i].length);
      argObject[URLDecode(key)] = URLDecode(value);
      //alert(URLDecode(key) + " -> " + URLDecode(value));
    }
    this.urlInCache = url;
    this.urlArgObjects = argObject;  
    return this.urlArgObjects;
  };

}

/** object represents helpers in drawing html etc */
function PageHelpers() {

  /** logout object back from ajax call */
  this.logoutObject = null;

  /** called when logout URL is processed */
  this.logoutAction = function() {
  
    //check here instead of where the button is clicked, so that
    //back button will check
    var theLogout = confirm("Are you sure you want to log out?");
    
    if (theLogout) {
      allObjects.clearActionObjects();
      ajax('../app/Misc/logout', this.pageHelpersLogoutActionResult);
    }
  }

  /** called after logout ajax is processed */
  this.pageHelpersLogoutActionResult = function(theLogoutObject) {
      allObjects.pageHelpers.logoutObject = theLogoutObject;
      replaceHtmlWithTemplate('#bodyDiv', 'common.logout.html');
      $("#topDiv").html("");
  };


  /** confirm, then logout the user */ 
  this.logout = function(event) {

    eventCancelBubble(event);
    
    //var hash = this.href;
    //hash = hash.replace(/^.*#/, '');
    
    // moves to a new page. 
    // pageload is called at once. 
    // hash don't contain "#", "?"
    $.historyLoad(URLEncode("operation=logout"));

    return false;
  }

  /** function to return the HTML for browse stems location */
  this.browseStemsLocation = function(groupName) {
        //<div class="browseStemsLocation">
        //  <strong>Current location is:</strong>
        //  <br><div class="currentLocationList">
        //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../public/assets/folderOpen.gif" class="groupIcon" alt="">Root:  
        //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../public/assets/folderOpen.gif" class="groupIcon" alt="">penn:
        //    <img onmouseover="grouperTooltip('Folder - A tree structure used to organize groups, subfolders, and folder-level permissions');" onmouseout="UnTip()" src="../public/assets/folderOpen.gif" class="groupIcon" alt="Folder">etc:  
        //<span class="browseStemsLocationHere">
        //    <img onmouseover="grouperTooltip('Group - A collection of entities (members) which can be people, other groups or other things (e.g., resources)');" onmouseout="UnTip()" src="../public/assets/group.gif" class="groupIcon" alt="Folder">ldapUsers</span>
        //</div></div>
    var result = '<div class="browseStemsLocation"><strong>' + allObjects.guiSettings.text.browseStemsLocationLabel
      + ' </strong> &nbsp; \n';
    var theArray = guiSplitTrim(groupName, ":");
    for (var i=0;theArray!=null && i<theArray.length;i++) {
      //if its a folder
      if (i != theArray.length-1) {
        result += '<img onmouseover="grouperTooltip(\'Folder - A tree structure used to organize groups, '
          + 'subfolders, and folder-level permissions\');" onmouseout="UnTip()" src="../public/assets/folderOpen.gif" '
          + 'class="groupIcon" alt="Folder">' + guiEscapeHtml(theArray[i]) + ': ';
        
      } else {
        result += '<span class="browseStemsLocationHere">\n'
          + '<img onmouseover="grouperTooltip(\'Group - A collection of entities (members) which'
          + ' can be people, other groups or other things (e.g., resources)\');" onmouseout="UnTip()"'
          + ' src="../public/assets/group.gif" class="groupIcon" alt="Object">' + guiEscapeHtml(theArray[i]) + '</span>\n';
      }
    }
    result += '</div>\n';
    return result;
  };
}


/** starting point for all objects */
function AllObjects() {
  /** when app is initted, this is the GuiSettings bean which has params, text, templates, etc */
  this.guiSettings = null;

  this.appState = new AppState();

  this.pageHelpers = new PageHelpers();

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



/** generic ajax method takes a url, callback function, and params or forms */
function ajax(theUrl, successResultFunction, requestParams) {
  
  if (typeof requestParams == 'undefined') {
    requestParams = {};
  }
  
  var result = null;

  $.ajax({
    url: theUrl,
    type: 'POST',
    cache: false,
    dataType: 'json',
    data: requestParams,
    timeout: 30000,
    async: true,
    //TODO handle errors success better.  probably non modal disappearing reusable window
    error: function(error){
      alert('error' + error);        
    },
    //TODO process the response object
    success: function(json){
      successResultFunction.call(this, json);
    }
  });
  return result;
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

function grouperTooltip(message) {
//  if (!tooltipsEnabled()) {
//    return;
//  }
  Tip(message, WIDTH, 400, FOLLOWMOUSE, false);
} 

/** call this from button to hide/show some text */
function guiToggle(event, jqueryElementKey) {
  eventCancelBubble(event);
  $(jqueryElementKey).toggle('slow'); 
  return false;
}

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

//trim all whitespace off a string
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
function guiEscapeHtml(html) {
  var escaped = html;
  escaped = escaped.replace(/&/g, "&amp;"); 
  escaped = escaped.replace(/</g, "&lt;"); 
  escaped = escaped.replace(/>/g, "&gt;"); 
  escaped = escaped.replace(/"/g, "&quot;"); 
  return escaped;
}
