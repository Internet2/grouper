/**
 * ====================================================================
 * About
 * ====================================================================
 * Sarissa cross browser XML library - DHTML module
 * @version 0.9.5.1
 * @author: Manos Batsis, mailto: mbatsis at users full stop sourceforge full stop net
 *
 * This module contains some convinient DHTML tricks based on Sarissa 
 *
 * ====================================================================
 * Licence
 * ====================================================================
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 or
 * the GNU Lesser General Public License version 2.1 as published by
 * the Free Software Foundation (your choice of the two).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License or GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * or GNU Lesser General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * or visit http://www.gnu.org
 *
 */
/**
 * Update an element with response of a GET request on the given URL. 
 * @param sFromUrl the URL to make the request to
 * @param oTargetElement the element to update
 * @param xsltproc (optional) the transformer to use on the returned
 *                  content before updating the target element with it
 */
Sarissa.updateContentFromURI = function(sFromUrl, oTargetElement, xsltproc) {
    try{
        document.body.style.cursor = "wait";
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open("GET", sFromUrl);
        function sarissa_dhtml_loadHandler() {
            if (xmlhttp.readyState == 4) {
                document.body.style.cursor = "auto";
                Sarissa.updateContentFromNode(xmlhttp.responseXML, oTargetElement, xsltproc);
            };
        };
        xmlhttp.onreadystatechange = sarissa_dhtml_loadHandler;
        xmlhttp.send(null);
        document.body.style.cursor = "auto";
    }
    catch(e){
        document.body.style.cursor = "auto";
        throw e;
    };
};

/**
 * Update an element's content with the given DOM node.
 * @param sFromUrl the URL to make the request to
 * @param oTargetElement the element to update
 * @param xsltproc (optional) the transformer to use on the given 
 *                  DOM node before updating the target element with it
 */
Sarissa.updateContentFromNode = function(oNode, oTargetElement, xsltproc){
    try{
        document.body.style.cursor = "wait";
        Sarissa.clearChildNodes(oTargetElement);
        // check for parsing errors
        var ownerDoc = oNode.nodeType == Node.DOCUMENT_NODE?oNode:oNode.ownerDocument;
        if(ownerDoc.parseError && ownerDoc.parseError != 0){
            var pre = document.createElement("pre");
            pre.appendChild(document.createTextNode(Sarissa.getParseErrorText(ownerDoc)));
            oTargetElement.appendChild(pre);
        }
        else{
            if(xsltproc){
                oNode = xsltproc.transformToDocument(oNode);
            };
            if(oNode.nodeType == Node.DOCUMENT_NODE){
                oTargetElement.innerHTML = Sarissa.serialize(oNode.documentElement);
            }
            else if(oNode.nodeType == Node.ELEMENT_NODE){
                if(oNode == oNode.ownerDocument.documentElement){
                    oTargetElement.innerHTML = Sarissa.serialize(oNode);
                }
                else{
                    oTargetElement.appendChild(oTargetElement.importNode(oNode, true));
                };
            };
        };
        document.body.style.cursor = "auto";
    }
    catch(e){
        document.body.style.cursor = "auto";
    throw e;
    };
};

