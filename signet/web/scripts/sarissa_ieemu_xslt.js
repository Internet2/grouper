/**
 * ====================================================================
 * About
 * ====================================================================
 * Sarissa cross browser XML library - IE XSLT Emulation (deprecated)
 * @version 0.9.5.1
 * @author: Manos Batsis, mailto: mbatsis at users full stop sourceforge full stop net
 *
 * This script emulates Internet Explorer's transformNode and transformNodeToObject
 * for Mozilla and provides a common way to set XSLT parameters
 * via Sarissa.setXslParameter.
 *
 * All functionality in this file is DEPRECATED, the XSLTProcessor
 * should be used instead.
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
if(!Sarissa.IS_ENABLED_TRANSFORM_NODE && window.XSLTProcessor){
    /** 
     * <p><b>Deprecated, will be removed in 0.9.6 (use XSLTProcessor instead): </b>Extends the Element class to emulate IE's transformNodeToObject (deprecated).
     * <b>Note </b>: The transformation result <i>must </i> be well formed,
     * otherwise an error will be thrown</p>
     * @uses Mozilla's XSLTProcessor  
     * @deprecated use the XSLTProcessor instead
     * @argument xslDoc The stylesheet to use (a DOM Document instance)
     * @argument oResult The Document to store the transformation result
     */
    Element.prototype.transformNodeToObject = function(xslDoc, oResult){
        var oDoc = document.implementation.createDocument("", "", null);
        Sarissa.copyChildNodes(this, oDoc);
        oDoc.transformNodeToObject(xslDoc, oResult);
    };
    /**
     * <p><b>Deprecated, will be removed in 0.9.6 (use XSLTProcessor instead): </b> Extends the Document class to emulate IE's transformNodeToObject (deprecated).</p>
     * @uses Mozilla's XSLTProcessor  
     * @deprecated use the XSLTProcessor instead
     * @argument xslDoc The stylesheet to use (a DOM Document instance)
     * @argument oResult The Document to store the transformation result
     * @throws Errors that try to be informative
     */
    Document.prototype.transformNodeToObject = function(xslDoc, oResult){
        var xsltProcessor = null;
        try{
            xsltProcessor = new XSLTProcessor();
            if(xsltProcessor.reset){
                /* new nsIXSLTProcessor is available */
                xsltProcessor.importStylesheet(xslDoc);
                var newFragment = xsltProcessor.transformToFragment(this, oResult);
                Sarissa.copyChildNodes(newFragment, oResult);
            }else{
                /* only nsIXSLTProcessorObsolete is available */
                xsltProcessor.transformDocument(this, xslDoc, oResult, null);
            };
        }catch(e){
            if(xslDoc && oResult)
                throw "Failed to transform document. (original exception: "+e+")";
            else if(!xslDoc)
                throw "No Stylesheet Document was provided. (original exception: "+e+")";
            else if(!oResult)
                throw "No Result Document was provided. (original exception: "+e+")";
            else if(xsltProcessor == null)
                throw "Could not instantiate an XSLTProcessor object. (original exception: "+e+")";
            else
                throw e;
        };
    };
    /**
     * <p><b>Deprecated, will be removed in 0.9.6 (use XSLTProcessor instead): </b>Extends the Element class to emulate IE's transformNode (deprecated). </p>
     * <p><b>Note </b>: The result of your transformation must be well formed,
     * otherwise you will get an error</p>. 
     * @uses Mozilla's XSLTProcessor    
     * @deprecated use the XSLTProcessor instead
     * @argument xslDoc The stylesheet to use (a DOM Document instance)
     * @returns the result of the transformation serialized to an XML String
     */
    Element.prototype.transformNode = function(xslDoc){
        var oDoc = document.implementation.createDocument("", "", null);
        Sarissa.copyChildNodes(this, oDoc);
        return oDoc.transformNode(xslDoc);
    };
    /**
     * <p><b>Deprecated, will be removed in 0.9.6 (use XSLTProcessor instead): </b>Extends the Document class to emulate IE's transformNode (deprecated).</p>
     * <p><b>Note </b>: The result of your transformation must be well formed,
     * otherwise you will get an error</p>
     * @uses Mozilla's XSLTProcessor
     * @deprecated use the XSLTProcessor instead
     * @argument xslDoc The stylesheet to use (a DOM Document instance)
     * @returns the result of the transformation serialized to an XML String
     */
    Document.prototype.transformNode = function(xslDoc){
        var out = document.implementation.createDocument("", "", null);
        this.transformNodeToObject(xslDoc, out);
        var str = null;
        try{
            var serializer = new XMLSerializer();
            str = serializer.serializeToString(out);
        }catch(e){
            throw "Failed to serialize result document. (original exception: "+e+")";
        };
        return str;
    };
    Sarissa.IS_ENABLED_TRANSFORM_NODE = true;
};
/**
 * <p><b>Deprecated, will be removed in 0.9.6 (use XSLTProcessor instead): </b>Set xslt parameters.</p>
 * <p><b>Note </b> that this method can only work for the main stylesheet and not any included/imported files.</p>
 * @deprecated use the XSLTProcessor instead
 * @argument oXslDoc the target XSLT DOM Document
 * @argument sParamName the name of the XSLT parameter
 * @argument sParamValue the value of the XSLT parameter
 * @returns whether the parameter was set succefully
 */
Sarissa.setXslParameter = function(oXslDoc, sParamQName, sParamValue){
    try{
        var params = oXslDoc.getElementsByTagName(_SARISSA_IEPREFIX4XSLPARAM+"param");
        var iLength = params.length;
        var bFound = false;
        var param;
        if(sParamValue){
            for(var i=0; i < iLength && !bFound;i++){
                if(params[i].getAttribute("name") == sParamQName){
                        param = params[i];
                    while(param.firstChild)
                        param.removeChild(param.firstChild);
                    if(!sParamValue || sParamValue == null){
                    }else if(typeof sParamValue == "string"){ 
                        param.setAttribute("select", sParamValue);
                        bFound = true;
                    }else if(sParamValue.nodeName){
                        param.removeAttribute("select");
                        param.appendChild(sParamValue.cloneNode(true));
                        bFound = true;
                    }else if (sParamValue.item(0) && sParamValue.item(0).nodeType){
                        for(var j=0;j < sParamValue.length;j++)
                            if(sParamValue.item(j).nodeType)
                                param.appendChild(sParamValue.item(j).cloneNode(true));
                        bFound = true;
                    }else
                        throw "Failed to set xsl:param "+sParamQName+" (original exception: "+e+")";
                };
            };
        };
        return bFound;
    }catch(e){
        throw e;
        return false;
    };
};
