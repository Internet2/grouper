
$(document).ready(function(){
  // Initialize history plugin.
  // The callback is called at once by present location.hash. 

  var urlArgObjectMap = allObjects.appState.urlArgObjectMap();
  if (typeof urlArgObjectMap.operation == 'undefined') {
    location.href = "grouper.html#operation=Misc.index";
    //dont return since in FF document.ready will not be called again
  }
  
  $.historyInit(pageload, "grouper.html");

  
});

/** init is called when app starts */
function init() {

  //skip that step
  processUrl();
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
  
  //operation%3DsimpleUpdate%26groupName%3Dtest%253Atest1
  //operation=simpleUpdate&groupName=test:test1
  //http://localhost:8089/grouperWs/grouperUi/appHtml/grouper.html#operation%3DsimpleMembershipUpdate%26groupName%3Dtest%253Atest1
  
  //map of url args
  var urlArgObjectMap = allObjects.appState.urlArgObjectMap();
  
  if (typeof urlArgObjectMap.operation == 'undefined') {
    $("#bodyDiv").html = "";
    alert("invalid URL, no operation");
  } else {
    var ajaxUrl = '../app/' + urlArgObjectMap.operation;
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
  
  /** function to get the map of url args */
  this.urlArgObjectMap = function() {
    //see if up to date
    if (allObjects.appState.urlInCache == location.href) {
      return allObjects.appState.urlArgObjects;
    }
    var argObject = new Object();
    allObjects.appState.urlArgObjects = argObject;  

    //lets get url
    var url = location.href;
    var poundIndex = url.indexOf("#");
    if (poundIndex == -1) {
      return allObjects.appState.urlArgObjects;
    }
    var poundString = url.substring(poundIndex + 1, url.length);
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

/**
 * register a combobox div
 * @param divId
 * @param width
 * @param useImages true or false, if images are in the combobox
 * @param filterUrl
 */
function guiRegisterDhtmlxCombo(divId, width, useImages, filterUrl ) {
  /* long hand...
   var simpleMembershipUpdateAddMemberSelect=new dhtmlXCombo(
      "simpleMembershipUpdateAddMember","simpleMembershipUpdateAddMember",200, 'image');
    simpleMembershipUpdateAddMemberSelect.enableFilteringMode(
       true,"../app/SimpleMembershipUpdate.filterUsers",false); */
  var theCombo=new dhtmlXCombo(
      divId,divId,width, useImages ? 'image' : undefined);
  theCombo.enableFilteringMode(true,filterUrl,false);
  
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
      ajax('../app/Misc/logout');
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
function ajax(theUrl, options) {
  
  if (!guiStartsWith(theUrl, "../app/" )) {
    theUrl = "../app/" + theUrl; 
  }
  
  if (typeof options == 'undefined') {
    options = {};
  }
  
  if (typeof options.requestParams == 'undefined') {
    options.requestParams = {};
  }
  
  if (!guiIsEmpty(options.formIds)) {
    
    var formIdsArray = guiSplitTrim(options.formIds, ",");
    for (var i = 0; i<formIdsArray.length; i++) {
      var formId = formIdsArray[i];
      
      //get elements in form
      var theForm = $("#" + formId);
      if (!theForm || theForm.length == 0) {
        fastAlert('Cant find form by id: "' + formId + '"!');
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
  
  //send over form data
  
  var appState = allObjects.appState;
  
  options.requestParams.appState = JSON.stringify(appState);
  
  var result = null;

  $.blockUI();  
  $.ajax({
    url: theUrl,
    type: 'POST',
    cache: false,
    dataType: 'json',
    data: options.requestParams,
    timeout: 30000,
    async: true,
    //TODO handle errors success better.  probably non modal disappearing reusable window
    error: function(error){
      $.unblockUI(); 
      alert('error' + error);        
    },
    //TODO process the response object
    success: function(json){
      var guiResponseJs = json;
      for (var i=0; i<guiArrayLength(guiResponseJs.actions); i++ ) {
        
        var action = guiResponseJs.actions[i];
        guiProcessAction(action);
        
      }

      //see if there are actions
      //if (successResultFunction) {
      //  successResultFunction.call(this, json);
      //}

    }
  });
  return result;
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
  if (!guiIsEmpty(guiScreenAction.innerHtmlJqueryHandle)) {
     $(guiScreenAction.innerHtmlJqueryHandle).html(guiScreenAction.innerHtml);
  }
  //do an alert
  if (!guiIsEmpty(guiScreenAction.alert)) {
    $.unblockUI(); 
    //alert(guiScreenAction.alert);
    //$.modal.close();
    $('<div style="text-align=center"><div class="simplemodal-guiinner">' + guiScreenAction.alert + '</div><div class="simplemodal-buttonrow"><button class=\'simplemodal-close blueButton\'>OK</button></div></div>').modal();
  }
  
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
     var elements = document.getElementsByName(theField.name);
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
        && theField.innerText && fastIsEmpty(theField.value)) {
     return theField.innerText;
   } else if (theField.type.toUpperCase() == "CHECKBOX") {      
     var checkboxes = document.getElementsByName(theField.name);
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
/** get an element from the document object by name.  if no elements, null, if multiple, then alert */
function guiGetElementByName(theName) {
   var theElements = document.getElementsByName(theName);
   if (theElements != null) {
      if (theElements.length == 1) {
         return theElements[0];
      } else if (theElements.length > 1) {
         fastAlert("Elements should be 1 for element " + theName + " but instead it is " + theElements.length);
      }
   }
   return null;
}

