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