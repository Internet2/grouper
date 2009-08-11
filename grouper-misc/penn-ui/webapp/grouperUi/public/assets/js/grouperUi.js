
function guiRoundCorners() {
  //round those corners
  //IE messes up
  if (!jQuery.browser.msie) {
    Nifty("div.sectionBody", "bottom");   
    Nifty("div.sectionHeader", "top");   
    Nifty("div#navbar"); 
  }  
}

/**
 * function called onload page
 */
function guiOnload() {
  guiRoundCorners();
  
  var theJavascriptMessage = document.getElementById('javascriptMessage');
  
  if (!guiIsEmpty(theJavascriptMessage)) {
    theJavascriptMessage.style.display = 'none'; 
  }
  
}

$(document).ready(function(){

  guiRoundCorners();
  
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
  
  /** map of hide shows by name */
  this.hideShows = {};
  
  /** map of pagers by name */
  this.pagers = {};
  
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



/** generic ajax method takes a url, callback function, and params or forms.
 * 
 * To pass in params to send to the server, pass in params like this:
 * Note: menuHtmlId, menuRadioGroup, and menutItemId are the param names, and
 * zoneId, group, and idClicked are variables whose values will be the param values:
 * 
 *  ajax(operation, {requestParams: {menuHtmlId: zoneId, menuRadioGroup: group, menuItemId: idClicked }});
 */
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
      guiProcessJsonResponse(json);
    }
  });
  return result;
}

/**
 * process an ajax request
 * @param guiResponseJs
 */
function guiProcessJsonResponse(guiResponseJs) {
  
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
  
  for (var i=0; i<guiArrayLength(guiResponseJs.actions); i++ ) {
    
    var action = guiResponseJs.actions[i];
    guiProcessAction(action);
    
  }

  //see if there are actions
  //if (successResultFunction) {
  //  successResultFunction.call(this, json);
  //}

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
  if (!guiIsEmpty(guiScreenAction.innerHtmlJqueryHandle)) {
     $(guiScreenAction.innerHtmlJqueryHandle).html(guiScreenAction.innerHtml);
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
    $.modal.close(); 
  }
  
  //do an alert
  if (!guiIsEmpty(guiScreenAction.alert)) {
    $.unblockUI(); 
    //alert(guiScreenAction.alert);
    //$.modal.close();
    $('<div class="simplemodal-guiinner">' 
        + guiScreenAction.alert + '<div class="simplemodal-buttonrow"><button class=\'simplemodal-close blueButton\'>OK</button></div></div>').modal({close: true, position: [20,20]});
  }
  
  //do an alert
  if (!guiIsEmpty(guiScreenAction.dialog)) {
    $.unblockUI(); 
    $('<div class=".simplemodal-dialoginner">' 
        + guiScreenAction.dialog + '</div>').modal({close: true, position: [20,20]});
  }
  
}

function guiX(x) {
  alert(x);
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
 *    GuiUtils.message("simpleMembershipUpdate.hideAdvancedOptionsButton"), 
 *       GuiUtils.message("simpleMembershipUpdate.showAdvancedOptionsButton"), true);
 *
 * Finally, use these EL functions to display the state correctly in JSP:
 * Something that is hidden/shown
 * style="${grouperGui:hideShowStyle('hideShowName', true)}
 * 
 * Button text:
 * ${grouperGui:hideShowButtonText('hideShowName')}
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
    menu.loadXML(structureOperation);
    
    menu.attachEvent("onClick", function(id, zoneId, casState){
      menu.hideContextMenu();
      ajax(operation, {requestParams: {menuHtmlId: zoneId, menuItemId: id }});
    });
    menu.attachEvent("onCheckboxClick", function(id, state, zoneId, casState){
      menu.hideContextMenu();
      ajax(operation, {requestParams: {menuHtmlId: zoneId, menuItemId: id, menuCheckboxChecked: !state }});
      return true;
    });
    menu.attachEvent("onRadioClick", function(group, idChecked, idClicked, zoneId, casState){
      menu.hideContextMenu();
      ajax(operation, {requestParams: {menuHtmlId: zoneId, menuRadioGroup: group, menuItemId: idClicked }});
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
 * 
 * @param event
 * @return
 */
function guiSubmitFileForm(event, formJqueryHandle, operation) {
  eventCancelBubble(event);
  var options = {
      iframe: true, 
      dataType: "json", 
      success:    function(json) { 
        guiProcessJsonResponse(json);
      },
      url: operation
  };
  $(formJqueryHandle).ajaxSubmit(options);
  return false;
}
