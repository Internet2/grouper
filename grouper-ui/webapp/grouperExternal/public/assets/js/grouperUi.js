
function guiRoundCorners() {
  //round those corners
  //IE messes up
  if (!jQuery.browser.msie) {
    Nifty("div.sectionBody", "bottom");   
    Nifty("div.sectionHeader", "top");   
    Nifty("div#navbar"); 
  }  
}

$(document).ready(function(){

  var theJavascriptMessage = document.getElementById('javascriptMessage');
  
  if (!guiIsEmpty(theJavascriptMessage)) {
    theJavascriptMessage.style.display = 'none'; 
  }

  guiRoundCorners();
  
  // Initialize history plugin.
  // The callback is called at once by present location.hash. 

  processUrl();  
  var urlArgObjectMap = allObjects.appState.urlArgObjectMap();
  if (typeof urlArgObjectMap.operation == 'undefined') {
    //alert('going back to index: ' + location.href);
	
	//if the url is an external URL, then go to the external index page
    if (!guiIsEmpty(location.href) && location.href.indexOf("/grouperExternal/appHtml/grouper.html") != -1) {
      location.href = "grouper.html?operation=ExternalSubjectSelfRegister.index";
    } else {
      location.href = "grouper.html?operation=Misc.index";
    }
    return;
  }
});

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

    if (typeof urlArgObjectMap.membershipLiteName != 'undefined') {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "membershipLiteName=" +  urlArgObjectMap.membershipLiteName;
    }
    if (typeof urlArgObjectMap.groupId != 'undefined') {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "groupId=" +  urlArgObjectMap.groupId;
    }
    if (typeof urlArgObjectMap.groupName != 'undefined') {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "groupName=" +  urlArgObjectMap.groupName;
    }
    if (typeof urlArgObjectMap.subjectPickerName != 'undefined') {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "subjectPickerName=" +  urlArgObjectMap.subjectPickerName;
    }
    if (typeof urlArgObjectMap.subjectPickerElementName != 'undefined') {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "subjectPickerElementName=" +  urlArgObjectMap.subjectPickerElementName;
    }
    if (typeof urlArgObjectMap.attributeDefNamePickerName != 'undefined') {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "attributeDefNamePickerName=" +  urlArgObjectMap.attributeDefNamePickerName;
    }
    if (typeof urlArgObjectMap.attributeDefNamePickerElementName != 'undefined') {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "attributeDefNamePickerElementName=" +  urlArgObjectMap.attributeDefNamePickerElementName;
    }
    if (typeof urlArgObjectMap.externalSubjectInviteId != 'undefined') {
      ajaxUrl += ajaxUrl.indexOf("?") == -1 ? "?" : "&";
      ajaxUrl += "externalSubjectInviteId=" +  urlArgObjectMap.externalSubjectInviteId;
    }
    if (typeof urlArgObjectMap.externalSubjectInviteName != 'undefined') {
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

  if (typeof urlArgObjectMap.groupId != 'undefined') {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "groupId=" +  urlArgObjectMap.groupId;
  }
  if (typeof urlArgObjectMap.groupName != 'undefined') {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "groupName=" +  urlArgObjectMap.groupName;
  }
  if (typeof urlArgObjectMap.membershipLiteName != 'undefined') {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "membershipLiteName=" +  urlArgObjectMap.membershipLiteName;
  }
  if (typeof urlArgObjectMap.attributeDefIdForFilter != 'undefined') {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "attributeDefIdForFilter=" +  urlArgObjectMap.attributeDefIdForFilter;
  }
  if (typeof urlArgObjectMap.attributeDefId != 'undefined') {
    theUrl += theUrl.indexOf("?") == -1 ? "?" : "&";
    theUrl += "attributeDefId=" +  urlArgObjectMap.attributeDefId;
  }
  return theUrl;
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
  
  if (!guiStartsWith(theUrl, "../app/" )) {
    theUrl = "../app/" + theUrl; 
  }
  
  theUrl = guiDecorateUrl(theUrl);

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
  
  //for(var requestParam in options.requestParams) {
  //  alert(requestParam + ", " + options.requestParams[requestParam]);
  //}
  
  //send over form data
  
  var appState = allObjects.appState;
  
  options.requestParams.appState = JSON.stringify(appState);

  //if modal up, it wont block, so close modal before ajax
  //$.modal.close(); 
  $.blockUI();  
  $.ajax({
    url: theUrl,
    type: 'POST',
    cache: false,
    dataType: 'json',
    data: options.requestParams,
    timeout: 180000,
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
}

/**
 * process an ajax request
 * @param guiResponseJs
 */
function guiProcessJsonResponse(guiResponseJs) {
  
  //$.unblockUI(); 
  
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
    $.modal.close(); 
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
          if (!fastIsEmpty(css)) {
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
          theElement.checked = true;
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
        && theField.innerText && fastIsEmpty(theField.value)) {
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
function guiValidateDate(x) {
  return formatValid_datevalid("dateValid", null, x) == null;
}
/** validate that the date is valid */
function formatValid_datevalid(functionName, args, x) {
  //if x is blank, thats ok.
  if (fastIsEmpty(x)) {
    return null;
  }
  var re = new RegExp("^(\\d{4})(\\d{2})(\\d{2})$");
  var matches = x.match(re);
  if (!matches) {
    //this is a formatting problem
    return guiBuildErrorArgs(functionName, args);
  }
    
  //check for valid days, get the month number (int)
  var month = fastParseInt(matches[2]);
  var day = fastParseInt(matches[3]);
  var year = fastParseInt(matches[1]);
  
  return guiValidateDateHelper(month, day, year, args);
}


/** convert a string to a ccyymmdd or mm/dd/ccyy HH:MM:ss.SSS */
function guiConvertStringToDateOrTimestampHelper(input, isDate) {
  //empty ok
  if (guiIsEmpty(input)) {
    return input;
  }
  //if the date is valid, we are done
  if (isDate) {
    //if timestamp, convert to date
    input = guiConvertDateFromTimestamp(input);
  
    //if we are already valid we are all set
    if (guiValidateDate(input)) {
      return input;
    }
  }
  
  //if the timestamp is valid, we are done
  if (!isDate) {

    //if we are already valid we are all set
    if (fastValidateTimestamp(input)) {
      return input;
    }
  }
   
  //if its not ccyymmdd, then it has delimiters
  var origInput = input;
  input = fastTrim(input);
  
  //if today, we know the answer
  if (fastStrEqIgCase(input,"d")) {
    var now = fastNewDate();
    if (isDate) {
      return fastConvertStringToDateOrTimestamp(
        (1+now.getMonth()) + "/" + now.getDate() + "/" + now.getFullYear(), isDate);
    } 
    return fastConvertStringToDateOrTimestamp(
       (now.getMonth()+1) + "/" + now.getDate() + "/" + now.getFullYear()  
      + " " + now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds()
      + "." + now.getMilliseconds(), isDate);
  }
  
  var monthString=null; //2 padded month
  var yearString=null; //4 padded year
  var dayString=null; //2 padded day
  var re;
  var m;
  var theTime="";
  var foundMatch=false;
  var hourString=null;
  var minuteString=null;
  var secondString=null;
  var millisString=null;
  
  //allow a 8 digit date yyyymmdd
  re = new RegExp("^([0-9]{4})([0-9]{2})([0-9]{2})(.*)$");
  m = input.match(re);
  if (m) {
    yearString = m[1];
    monthString = m[2];
    dayString = m[3];
    theTime = m[4];
    foundMatch = true;
  }
  if (!foundMatch) {
    //allow a 6 digit date mmddyy
    re = new RegExp("^([0-9]{2})([0-9]{2})([0-9]{2})(.*)$");
    m = input.match(re);
    if (m) {
      monthString = m[1];
      dayString = m[2];
      yearString = m[3];
      theTime = m[4];
      foundMatch = true;
    }
  }
  //see if there is the month by text
  if (!foundMatch) {
    //try with 2 or 4 digit year
    //see if there is the date by text Jan 1 2004 or february 4, 2005
    re = new RegExp("^([a-zA-Z]+)\\D*([0-9]{1,2})\\D+([0-9]{1,4})(.*)$");
    m = input.match(re);
    if (m) {
      monthString = m[1].toLowerCase();
      monthString = fastMonthsNumber[monthString];
      if (monthString == null) {
        return origInput;
      }
      dayString = m[2];
      yearString = m[3];
      theTime = m[4];
      foundMatch = true;
    }
  }
  //see if there is the month by text
  if (!foundMatch) {
    //try with 2 or 4 digit year
    //see if there is the date by text 1 Jan 2004 or 4 february, 2005
    re = new RegExp("^([0-9]{1,2})\\W+([a-zA-Z]+)\\W*([0-9]{1,4})(.*)$");
    m = input.match(re);
    if (m) {
      dayString = m[1];
      monthString = m[2].toLowerCase();
      monthString = fastMonthsNumber[monthString];
      if (monthString == null) {
        return origInput;
      }
      yearString = m[3];
      theTime = m[4];
      foundMatch = true;
    }
  }
  //see if there is the year first, 4 digit only
  if (!foundMatch) {
    //try with 2 or 4 digit year
    re = new RegExp("^([0-9]{4})\\D+([0-9]{1,2})\\D+([0-9]{1,2})(.*)$");
    m = input.match(re);
    if (m) {
      yearString = m[1];
      monthString = m[2];
      dayString = m[3];
      theTime = m[4];
      foundMatch = true;
    }
  }
  //see if there is the month first, any digit year
  if (!foundMatch) {
    re = new RegExp("^([0-9]{1,2})\\D+([0-9]{1,2})\\D+([0-9]{1,4})(.*)$");
    m = input.match(re);
    if (m) {
      monthString = m[1];
      dayString = m[2];
      yearString = m[3];
      theTime = m[4];
      foundMatch = true;
    }
  }
  //see if there is the month first, any digit year
  if (!foundMatch) {
    re = new RegExp("^([0-9]{1,2})\\D+([0-9]{1,4})(.*)$");
    m = input.match(re);
    if (m) {
      monthString = m[1];
      dayString = "01";
      yearString = m[2];
      theTime = m[3];
      foundMatch = true;
    }
  }
  //see if we havent found the date yet
  if (!foundMatch) {
    return origInput;
  }
  //make sure no neglected numbers
  if (isDate) {
    if (fastHasDigits(theTime)) {
      return origInput;
    }
  } 
  monthString = fastPadIntString(monthString, 2);
  dayString = fastPadIntString(dayString, 2);

  if (yearString.length < 4) {
  
    var year = fastParseInt(yearString);
    yearString = fastPadIntString(yearString, 2);
    if (year == 99) {
      yearString = "9999";
    } else if (year < 40) {
      yearString = "20" + yearString;
    } else {
      yearString = "19" + yearString;
    }
  }
  //mainframe conversion
  if (fastStrEq(yearString, "9999") && fastStrEq(monthString, "99") && fastStrEq(dayString, "99")) {
    yearString = "2099";
    monthString = "12";
    dayString = "31";
  }
  var ret;
  if (isDate) {
    ret = yearString + monthString + dayString;
    return ret;
  }

  foundMatch = false;
  var theRest = "";
  if (fastIsEmpty(theTime)) {
    hourString = "00";
    minuteString = "00";
    secondString = "00";
    millisString = "000";
    foundMatch = true;
  }
  if (!foundMatch) {
    //timestamp, try millis, 1 to 3 of them
    re = new RegExp("^\\D*([0-9]{1,2})\\D+([0-9]{1,2})\\D+([0-9]{1,2})\\D+([0-9]{1,3})(.*)$");
    m = theTime.match(re);
    if (m) {
      hourString = m[1];
      minuteString = m[2];
      secondString = m[3];
      millisString = m[4];
      theRest = m[5];
      foundMatch = true;
    }
  }
  if (!foundMatch) {
    //timestamp, no millis
    re = new RegExp("^\\D*([0-9]{1,2})\\D+([0-9]{1,2})\\D+([0-9]{1,2})(.*)$");
    m = theTime.match(re);
    if (m) {
      hourString = m[1];
      minuteString = m[2];
      secondString = m[3];
      theRest = m[4];
      millisString = "000";
      foundMatch = true;
    }
  }
  if (!foundMatch) {
    //timestamp, no seconds or millis
    re = new RegExp("^\\D*([0-9]{1,2})\\D+([0-9]{1,2})(.*)$");
    m = theTime.match(re);
    if (m) {
      hourString = m[1];
      minuteString = m[2];
      secondString = "00";
      theRest = m[3];
      millisString = "000";
      foundMatch = true;
    }
  }
  if (!foundMatch || fastHasDigits(theRest)) {
    return origInput;
  }
  //see if there is an AM or PM in the timeString
  re = new RegExp("([aApP][mM])");
  theTime = fastDefaultString(theTime);
  m = theTime.match(re);
  var amPm = null;
  var hasAmPm = false;
  var isAm = true;
  if (m) {
    amPm = m[1].toLowerCase();
    hasAmPm = true;
    if (fastStrEq(amPm,"am")) {
      isAm = true;
    } else if (fastStrEq(amPm,"pm")) {
      isAm = false;
    } else {
      fastAlert("Illegal AM/PM: " + amPm, false, 5);
      return origInput;
    }
  } 
  if (hasAmPm) {
    var hour = fastParseInt(hourString);
    if (hour <= 12 && hour > 0) {
      hour = hour == 12 ? 0 : hour;
      if (!isAm) {
        hour+=12;
      }
      hourString = "" + hour;
    }
  }
  hourString = fastPadIntString(hourString, 2);
  minuteString = fastPadIntString(minuteString, 2);
  secondString = fastPadIntString(secondString, 2);
  millisString = fastPadIntString(millisString, 3);    
  
  ret = yearString + "/" + monthString + "/" + dayString + " " + hourString
    + ":" + minuteString + ":" + secondString + "." + millisString;
  return ret;
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
