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

var requestObject;

// This code was adapted from the following web pages:
//
//   www.xml.com/pub/a/2005/02/09/xml-http-request.html
//   www.francisshanahan.com/zuggest.aspx

function loadXMLDoc(url)
{
  if (window.XMLHttpRequest)
  {
    requestObject = new XMLHttpRequest();

    if (requestObject)
    {
      requestObject.onreadystatechange = processReqChange;
      requestObject.open("GET", url, true);
      
      // This line prevents any browser from using its internal cache to satisfy
      // this request. This sidesteps a bug in Safari (June 2005).
      // For more information, see this URL:
      //    http://www.bitterpill.org/logid/1117777362000
      requestObject.setRequestHeader
        ('If-Modified-Since', 'Wed, 15 Nov 1995 00:00:00 GMT');

      requestObject.send(null);
    }
  }
  else if (window.ActiveXObject)
  {
    requestObject = new ActiveXObject("Microsoft.XMLHTTP");

    if (requestObject)
    {
      requestObject.onreadystatechange = processReqChange;
      requestObject.open("GET", url, true);
      
      // This line prevents any browser from using its internal cache to satisfy
      // this request. This sidesteps a bug in Safari (June 2005).
      // For more information, see this URL:
      //    http://www.bitterpill.org/logid/1117777362000
      requestObject.setRequestHeader
        ('If-Modified-Since', 'Wed, 15 Nov 1995 00:00:00 GMT');
        
      requestObject.send();
    }
  }
}

function processReqChange()
{
  if (requestObject.readyState == 4) // Request is complete
  {
    if (requestObject.status == 200) // Status is OK
    {
      var searchResultsElement = document.getElementById('PersonSearchResults');
      searchResultsElement.style.display = 'block';
      searchResultsElement.innerHTML = requestObject.responseText;
    }
    else
    {
      alert
        ("There was a problem retrieving the data: requestObject="
         + requestObject
         + "\nrequestObject.status = "
         + requestObject.status
         + "\nrequestObject.statusText = "
         + requestObject.statusText);
    }
  }
}
