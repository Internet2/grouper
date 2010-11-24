//v.2.5 build 91111

/*
Copyright DHTMLX LTD. http://www.dhtmlx.com
You allowed to use this component or parts of it under GPL terms
To use it on other terms or get Professional edition of the component please contact us at sales@dhtmlx.com
*/
dhtmlXCombo.prototype.attachChildCombo = function(_chcombo,xml){if(!this._child_combos){this._child_combos = []};this._has_childen = 1;this._child_combos[this._child_combos.length] = _chcombo;_chcombo.show(0);var that = this;var _arg_length = arguments.length;this.attachEvent("onChange",function(){for(var i = 0;i < that._child_combos.length;i++){if(that._child_combos[i]==_chcombo){_chcombo.show(1);_chcombo.callEvent("onMasterChange",[that.getActualValue(),that])}};if(that.getActualValue()=="") {that.showSubCombo(that,0);return};if(_chcombo._xml){if(_arg_length ==1)xml = _chcombo._xml;_chcombo._xml = that.deleteParentVariable(xml);_chcombo._xml += ((_chcombo._xml.indexOf("?")!=-1)?"&":"?")+"parent="+that.getActualValue()}else{if(xml){_chcombo.clearAll(true);_chcombo.loadXML(xml+((xml.indexOf("?")!=-1)?"&":"?")+"parent="+that.getActualValue())}}})
};dhtmlXCombo.prototype.setAutoSubCombo = function(xml,name){if(arguments.length == 1)name = "subcombo";if(!this._parentCombo){var z = new dhtmlXCombo(this.DOMParent,name,this.DOMelem.style.width)
 z._parentCombo = this}else {var z = new dhtmlXCombo(this._parentCombo.DOMParent,name,this._parentCombo.DOMelem.style.width)
 z._parentCombo = this._parentCombo};if(this._filter)z._filter = 1;if(this._xml){if(arguments.length > 0)z._xml = xml;else 
 z._xml = this._xml;xml = z._xml;z._autoxml = this._autoxml;if(this._xmlCache)z._xmlCache=[]};this.attachChildCombo(z,xml) 
 return z};dhtmlXCombo.prototype.detachChildCombo = function(_chcombo){for(var i = 0;i < this._child_combos.length;i++){this._child_combos[i] == _chcombo;this._child_combos.splice(i,1)};_chcombo.show(1)};dhtmlXCombo.prototype.showSubCombo = function(combo,state){if(combo._child_combos){for(var i = 0;i < combo._child_combos.length;i++){combo._child_combos[i].show(state);combo.showSubCombo(combo._child_combos[i],0)}}};dhtmlXCombo.prototype.deleteParentVariable = function(str){str = str.replace(/parent\=[^&]*/g,"").replace(/\?\&/,"?");return str};
//v.2.5 build 91111

/*
Copyright DHTMLX LTD. http://www.dhtmlx.com
You allowed to use this component or parts of it under GPL terms
To use it on other terms or get Professional edition of the component please contact us at sales@dhtmlx.com
*/