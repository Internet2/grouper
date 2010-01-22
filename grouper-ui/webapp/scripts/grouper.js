function grouperTooltip(message) {
  if (!tooltipsEnabled()) {
    return;
  }
  //NOTE, we need to unescape the HTML, since it is in a javascript call...
  message = guiEscapeHtml(message, false);
  Tip(message, WIDTH, 400, FOLLOWMOUSE, false);
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

/** set a cookie, generally you just need to set the name and value */
function setCookie(name, value, expires, domain, path, secure) {
   var curCookie = name + "=" + escape(value) +
      ((expires) ? "; expires=" + expires.toGMTString() : "") +
      ((path) ? "; path=" + path : "") +
      ((domain) ? "; domain=" + domain : "") +
      ((secure) ? "; secure" : "");
      document.cookie = curCookie;
  }

  /** get a cookie value */
  function getCookie(cookiename) {
    var cookiestring=""+document.cookie;
    var index1=cookiestring.indexOf(cookiename);
    if (index1==-1 || cookiename=="") return ""; 
    var index2=cookiestring.indexOf(';',index1);
    if (index2==-1) index2=cookiestring.length; 
    return unescape(cookiestring.substring(index1+cookiename.length+1,index2));
  }

function isEmpty(x) {
   //fix a false positive
   if (typeof x == "number" && x == 0) {
      return false;
   }
  return typeof x == "undefined" || x == null 
    || (typeof x == "string" && x == "");
}

var grouperTooltipCookieName = "grouperTooltips";

/** see if tooltips are enabled, return true or false */
function tooltipsEnabled() {
  var grouperCookieValue = getCookie(grouperTooltipCookieName);
  if (isEmpty(grouperCookieValue)) {
    return true;
  }
  return "true" == grouperCookieValue;
}

/** see if infodots are enabled, return true or false */
function infodotsEnabled() {
  var grouperCookieValue = getCookie(grouperInfodotCookieName);
  if (isEmpty(grouperCookieValue)) {
    return true;
  }
  return "true" == grouperCookieValue;
}

function toggleTooltips(clickToDisableText, clickToEnableText) {
  //if was true, make false, and vice versa
  var newCookieValue = '' + !tooltipsEnabled();
  //this will be a session cookie
  setCookie(grouperTooltipCookieName, newCookieValue);
  
  writeTooltipText(clickToDisableText, clickToEnableText);
}

function writeTooltipText(clickToDisableText, clickToEnableText) {
  var toggleLink = document.getElementById('tooltipToggleLink');
  //if there, see if enabled
  var areTooltipsEnabled = tooltipsEnabled();
  //if nothing there, forget it
  if (!isEmpty(toggleLink)) {
    var newText = areTooltipsEnabled ? clickToDisableText : clickToEnableText;
  
    toggleLink.innerHTML = newText;  
  }
  
  //make sure stylesheet is ok
  document.getElementById('grouperTooltipStylesheet').disabled = !areTooltipsEnabled;

}

//init the stylesheet, forget about the text, element isnt there anyways
writeTooltipText();

var grouperInfodotCookieName = "grouperInfodots";

function toggleInfodots(event, clickToDisableText, clickToEnableText) {

  //stop the event from bubbling up (i.e. dont really click the button)
  eventCancelBubble(event);
  
  //if was true, make false, and vice versa
  var newCookieValue = '' + !infodotsEnabled();
  //this will be a session cookie
  setCookie(grouperInfodotCookieName, newCookieValue);
  
  writeInfodotText(clickToDisableText, clickToEnableText);
  
  return false;
}

function writeInfodotText(clickToDisableText, clickToEnableText) {
  var toggleLink = document.getElementById('infodotToggleLink');
  //if there, see if enabled
  var areInfodotsEnabled = infodotsEnabled();
  //if nothing there, forget it
  if (!isEmpty(toggleLink)) {
    var newText = areInfodotsEnabled ? clickToDisableText : clickToEnableText;
  
    toggleLink.innerHTML = newText;  
  }
  
  //make sure stylesheet is ok
  document.getElementById('grouperInfodotStylesheet').disabled = !areInfodotsEnabled;

}

//init the stylesheet, forget about the text, element isnt there anyways
writeInfodotText();

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
  var newStyle = "none";
  var newVisibility = "hidden";
  if (isHidden) {
    newStyle = "";
    newVisibility = "";
  }
  var suffix = 0;
  var currentElement;
  var didAny = false;
  while (currentElement = document.getElementById(idPrefix + "" + (suffix++))) {
    didAny = true;
    currentElement.style.display = newStyle;
    currentElement.style.visibility = newVisibility;
  }
  if (!didAny && alertIfNone) {
    window.alert("Nothing to hide or show for id: " + idPrefix);
  }
}

//these only exist (or not) in firefox
var isFirefox = document.getElementById && !document.all;

// Prevent google from yellowing controls.
function defeatGoogle(){
  if (isFirefox){
    // If google tries to change the colors of the textfields, set them back
    // to nothing. Firefox has different method calls for adding listeners;
    // the code explicit to firefox follows.
    function setListeners(){
      inputList = document.getElementsByTagName("INPUT");
      for(i=0;i<inputList.length;i++){
        inputList[i].addEventListener("propertychange", restoreStyles, false);
        inputList[i].style.backgroundColor = "";
      }
      selectList = document.getElementsByTagName("SELECT");
      for(i=0;i<selectList.length;i++){
        selectList[i].addEventListener("propertychange", restoreStyles, false);
        selectList[i].style.backgroundColor = "";
      }
    }
    // In firefox, the event listener has to be added after the method declaration.
    window.addEventListener("load", setListeners, false);
   } else {
    // If google tries to change the colors of the textfields, set them back
    // to nothing. IE has different method calls for adding listeners;
    // the code explicit to IE follows.
    function setListeners(){
      inputList = document.getElementsByTagName("INPUT");
      for(i=0;i<inputList.length;i++){
        inputList[i].attachEvent("onpropertychange",restoreStyles);
        inputList[i].style.backgroundColor = "";
      }
      selectList = document.getElementsByTagName("SELECT");
      for(i=0;i<selectList.length;i++){
        selectList[i].attachEvent("onpropertychange",restoreStyles);
        selectList[i].style.backgroundColor = "";
      }
    }
    // In IE, the event listener can be added before or after the method declaration.
    window.attachEvent("onload",setListeners);
   }
 }

