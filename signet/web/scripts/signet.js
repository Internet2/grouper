function openWindow(theURL,winName,features) { 
  window.open(theURL,winName,features);
}

function showSearch() {
	var toga = document.getElementById('Mylist');
	var togb = document.getElementById('Search');	
		toga.style.display = 'none';	
		togb.style.display = 'block';			
}

function showResult() {
	var toga = document.getElementById('Results');
	var togb = document.getElementById('Browse');
		toga.style.display = 'block';	
		togb.style.display = 'none';
}

function showBrowse() {
	var toga = document.getElementById('Results');
	var togb = document.getElementById('Browse');
		toga.style.display = 'none';	
		togb.style.display = 'block';
}

function CheckAll(){
	document.checkform.row1.checked = document.checkform.allbox.checked;
	document.checkform.row2.checked = document.checkform.allbox.checked;
	document.checkform.row3.checked = document.checkform.allbox.checked;
	document.checkform.row4.checked = document.checkform.allbox.checked;
	document.checkform.row5.checked = document.checkform.allbox.checked;
	document.checkform.row6.checked = document.checkform.allbox.checked;
}

var req;

// This code was adapted from www.xml.com/pub/a/2005/02/09/xml-http-request.html.
function loadXMLDoc(url)
{
  req = new XMLHttpRequest();
  req.onreadystatechange = processReqChange;
  req.open("GET", url, true);
  req.send(null);
}

function printXMLDoc(xmlDoc)
{
  return printXMLNode("", xmlDoc.documentElement, "");
} 

function printXMLNode(accumulator, node, prefix)
{
  var nodeDescription;
  if ((typeof node) == "object")
  {
    if (node instanceof Array)
    {
      nodeDescription = "Array";
    }
    else
    {
      if (node.nodeName == "#text")
      {
        nodeDescription = node.nodeValue;
      }
      else
      {
        nodeDescription = "<" + node.nodeName + " " + printAttributes(node) + ">";
      }
    }
  }
  else
  {
    nodeDescription = (typeof node);
  }
  
  accumulator = accumulator + "\n" + prefix + nodeDescription;
  var children = node.childNodes;
  for (var i = 0; i < children.length; i++)
  {
    accumulator = printXMLNode(accumulator, children[i], prefix + "--");
  }
  
  if (node.nodeName != "#text")
  {
    accumulator = accumulator + "\n" + prefix + "<" + node.tagName + " />";
  }
  return accumulator;
}

function printAttributes(node)
{
  var outStr = "";
  var attrs = node.attributes;
  if (attrs == null)
  {
    return outStr;
  }
  
  for (var i = 0; i < attrs.length; i++)
  {
    if (i > 0)
    {
      outStr += " ";
    }
    
    outStr = outStr + attrs.item(i).nodeName + "=" + attrs.item(i).nodeValue;
  }
  
  return outStr;
}

var newWindow;

// This code was adapted from www.xml.com/pub/a/2005/02/09/xml-http-request.html.
function processReqChange()
{
  if (req.readyState == 4) // Request is complete
  {
    if (req.status == 200) // Status is OK
    {
      // The response contains the name of the Javascript method to invoke in order
      // to process this query-result, and the query-result-value to pass to that
      // method. This makes is possible for this method to detect, parse, and forward
      // query-response data to any number of methods that actually do something useful
      // with that data.
         
      response = req.responseXML.documentElement;
      
//      parseError = req;
//      if (parseError.errorCode != 0)
//      {
//        alert("errorCode: " + parseError.errorCode + "\n" +
//          "filepos: " + parseError.filepos + "\n" +
//          "line: " + parseError.line + "\n" +
//          "linepos: " + parseError.linepos + "\n" +
//          "reason: " + parseError.reason + "\n" +
//          "srcText: " + parseError.srcText + "\n" +
//          "url: " + parseError.url);
//      }
      
      methodElement = response.getElementsByTagName('method')[0];
      methodElementFirstChild = methodElement.firstChild;
      methodElementFirstChildData = methodElementFirstChild.data;
      method = methodElementFirstChildData;
      
      result = response.getElementsByTagName('result')[0];
      
      evalString = method + "(result);";
      eval(evalString);
    }
    else
    {
      alert("There was a problem retrieving the XML data:\n" + req.statusText);
    }
  }
}

function showPersonSearchResults(personSearchResults)
{
  var resultsDiv = document.getElementById('PersonSearchResults');
  resultsDiv.style.display = 'block';
  
  if (resultsDiv.firstChild != null)
  {
    resultsDiv.removeChild(resultsDiv.firstChild);
  }

  var resultStr = Sarissa.serialize(personSearchResults);
  // Here's an embarrassing little hack: On FireFox, Sarissa.serialize includes an
  // XML namespace of "a0" on each element. This prevents it from recognizing those
  // tags as legitimate HTML. So, we'll strip those namespace-prefixes off if they're
  // present. There's probably some way of preventing them in the first place, but
  // I haven't figured that out yet.
  var re = new RegExp("a0:", "g");
  resultStr = resultStr.replace(re, "");
  
  resultsDiv.innerHTML = resultStr;
}