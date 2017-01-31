/**
 * @author mchyzer
 * $Id: TfCombobox.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;



/**
 * <pre>
 * Note, the idBase + "Id" is the dojo id of the control
 * The idBase + "Name" is what is submitted to the server
 * The idBase + "NameDisplay" is what is submitted to the server if something is typed in but not selected
 * The idBase + "ErrorId" is the span for where validation errors can go
 * </pre>
 */
public class GrouperComboboxTag2 extends SimpleTagSupport {

  /**
   * id and class of elements, and name of combobox. Make this unique in page.
   * e.g. personPicker.  The id of the tag will be personPickerId, name will be
   * personPickerName.  Will generate a QueryReadStore too... 
   */
  private String idBase;

  /**
   * style, could include the width of the textfield
   */
  private String style;

  /**
   * class to use when drawing the control.  default is claro.  should be a dojo class theme, e.g.  claro, tundra, nihilo and soria
   */
  private String classCss;

  /**
   * if there should be a down arrow to click.  Default to false.  Generally this is useful only for 
   * combos with less then a few hundred options
   */
  private Boolean hasDownArrow = null;

  /**
   * search delay in ms defaults to 500
   */
  private Integer searchDelay = 500;

  /**
   * the operation to call when filtering, relative to this page url to call
   */
  private String filterOperation;

  /**
   * the default value (will be submitted) which should appear in the combo box when drawn.  Will lookup the label via ajax
   */
  private String value;

  /**
   * send more form element names to the filter operation, comma separated
   */
  private String additionalFormElementNames;

  
  /**
   * id and class of elements, and name of combobox. Make this unique in page.
   * e.g. personPicker.  The id of the tag will be personPickerId, name will be
   * personPickerName.  Will generate a QueryReadStore too... 
   * @param idBase1 the idBase to set
   */
  public void setIdBase(String idBase1) {
    this.idBase = idBase1;
  }

  
  /**
   * style, could include the width of the textfield
   * @param style1 the style to set
   */
  public void setStyle(String style1) {
    this.style = style1;
  }

  
  /**
   * class to use when drawing the control.  default is claro.  should be a dojo class theme, e.g.  claro, tundra, nihilo and soria
   * @param classCss1 the classCss to set
   */
  public void setClassCss(String classCss1) {
    this.classCss = classCss1;
  }

  
  /**
   * if there should be a down arrow to click.  Default to false.  Generally this is useful only for 
   * combos with less then a few hundred options
   * @param hasDownArrow1 the hasDownArrow to set
   */
  public void setHasDownArrow(Boolean hasDownArrow1) {
    this.hasDownArrow = hasDownArrow1;
  }

  
  /**
   * search delay in ms defaults to 500
   * @param searchDelay1 the searchDelay to set
   */
  public void setSearchDelay(Integer searchDelay1) {
    this.searchDelay = searchDelay1;
  }

  /**
   * 
   * @return pro
   */
  public int searchDelayProcessed() {
    if (this.searchDelay == null || this.searchDelay < 0) {
      return 500;
    }
    return this.searchDelay;
  }
  
  /**
   * default downarrow to false
   * @return if has down arrow
   */
  public boolean hasDownArrowProcessed() {
    if (this.hasDownArrow == null) {
      return false;
    }
    return this.hasDownArrow;
  }
  
  /**
   * the operation to call when filtering, relative to this page url to call
   * @param filterOperation1 the filterOperation to set
   */
  public void setFilterOperation(String filterOperation1) {
    this.filterOperation = filterOperation1;
  }

  
  /**
   * the default value (will be submitted) which should appear in the combo box when drawn.  Will lookup the label via ajax
   * @param value1 the value to set
   */
  public void setValue(String value1) {
    this.value = value1;
  }

  
  /**
   * send more form element names to the filter operation, comma separated
   * @param additionalFormElementNames1 the additionalFormElementNames to set
   */
  public void setAdditionalFormElementNames(String additionalFormElementNames1) {
    this.additionalFormElementNames = additionalFormElementNames1;
  }
  

  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {

    StringBuilder result = new StringBuilder();
    
    //<div data-dojo-type="dojox.data.QueryReadStore" id="personPickerStoreId" formElementNamesToSend="anotherItemName"
    //  data-dojo-props="url:'../twoFactorUi/app/UiMain.personPicker'" data-dojo-id="personPickerStoreDojoId" style="display: inline"></div>
    //<input id="personPickerId" name="personPickerName"  searchDelay="500" value="10021368"
    //    data-dojo-props="store:personPickerStoreDojoId" class="claro" style="width: 40em"
    //    autoComplete="false" data-dojo-type="dijit/form/FilteringSelect" hasDownArrow="false" /><!-- 
    //     -18 for no down arrow, -36 for down arrow --><img 
    //    style="position: relative; top: 5px; left: -18px; display: none; " alt="busy..."  id="personPickerThrobberId"
    //    src="../../grouperExternal/pubilc/assets/images/busy.gif" class="comboThrobber" />
    //<script>
    //dojo.ready(function(){
    //  dijit.byId('personPickerId').onChange = function(evt) {
    //    this.focusNode.setSelectionRange(0,0);
    //  }
    //});
    //</script>
    
    //unregister old ones since if they are replaced in divs they will give you an error
    result.append("    <script>\n");
    result.append("      dojoUnregisterWidget('" + this.idBase + "Id');");
    result.append("      dojoUnregisterWidget('" + this.idBase + "StoreId');");
    result.append("    </script>\n");
    
    //put a table around it so the validation image can be to the right
    result.append("<table style=\"padding: 0; border-spacing: 0\" ><tr><td>\n");
    
    //store
    result.append("    <div style=\"display: inline; white-space: nowrap; \"><div data-dojo-type=\"dojox.data.QueryReadStore\" id=\"" + this.idBase + "StoreId\" ");
    if (!StringUtils.isBlank(this.additionalFormElementNames)) {
      result.append(" formElementNamesToSend=\"" + this.additionalFormElementNames + "\" ");
    }
    result.append(" data-dojo-props=\"url:'" + this.filterOperation 
        + "'\" data-dojo-id=\"" + this.idBase + "StoreDojoId\" style=\"display: none\"></div>\n" );

    result.append("    <input id=\"" + this.idBase + "Id\" name=\"" + this.idBase + "Name\"  searchDelay=\"" 
        + this.searchDelayProcessed() + "\" ");
    
    if (!StringUtils.isBlank(this.value)) {
      result.append(" value=\"" + this.value + "\" ");
    }
    
    result.append(" required=\"false\" data-dojo-props=\"store:" + this.idBase + "StoreDojoId, labelType:'html', labelFunc : function(item, store) { return store.getValue(item, 'htmlLabel'); } \" ");

    //placeHolder?
    
    if (!StringUtils.isBlank(this.classCss)) {
      result.append(" class=\"" + this.classCss + "\" ");
    }
    
    if (!StringUtils.isBlank(this.style)) {
      result.append(" style=\"" + this.style + "\" \n");
    }
    
    result.append("    autocomplete=\"false\" data-dojo-type=\"dijit/form/FilteringSelect\" hasDownArrow=\"" + this.hasDownArrowProcessed() + "\" />");

    
    
    //note, no whitespace between input and throbber
    // <!-- 
    //     -18 for no down arrow, -36 for down arrow --><img 
    //    style="position: relative; top: 5px; left: -18px; display: none; " alt="busy..."  id="personPickerThrobberId"
    //    src="../../grouperExternal/pubilc/assets/busy.gif" class="comboThrobber" />
    result.append("<img \n");
    result.append("     style=\"position: relative; top: 0px; left: " + (this.hasDownArrowProcessed() ? "-38" : "-20") + "px; display: none; \" alt=\"busy...\"  id=\"" + this.idBase + "ThrobberId\"\n");
    result.append("     src=\"../../grouperExternal/public/assets/images/busy.gif\" class=\"comboThrobber\" />\n");

    result.append("    <input id=\"" + this.idBase + "IdDisplay\" name=\"" + this.idBase + "NameDisplay\"  type=\"hidden\" value=\"\" />\n"); 
    
    //<script>
    //dojo.ready(function(){
    //  dijit.byId('personPickerId').onChange = function(evt) {
    //    this.focusNode.setSelectionRange(0,0);
    //  }
    //});
    //</script>
    result.append("    <script>\n");
    result.append("      $( document ).ready(function(){\n");
    //we dont want this to happen too soon, schedule 500ms out
    result.append("        setTimeout(function(){\n");
    //add the filtering select to the list of ids
    //result.append("        dojoFilteringSelectBaseIds['" + this.idBase + "'] = true;\n");
    result.append("          dojoAddFilteringSelectBaseId('" + this.idBase + "');\n");
    result.append("          dijit.byId('" + this.idBase + "Id').onChange = function(evt) {\n");
    result.append("            this.focusNode.setSelectionRange(0,0);\n");
    result.append("          }\n");
    if (GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.disableEnterKeyOnCombobox", true)) {
      result.append("          grouperDisableEnterOnCombo('#" + this.idBase + "Id');\n");
    }
    result.append("        },1000);\n");
    
//    result.append("        dijit.byId('" + this.idBase + "Id').labelFunc = function(item, store) {\n");
//    result.append("          alert(item);\n");
//    result.append("          return item.htmlLabel;\n");
//    result.append("        }\n");
    
//              "<li>", store.getValue(item, "title"), "</li>",
    result.append("      });\n");
    result.append("    </script></div>\n");

    //put a table around it so the error message can be to the right
    result.append("</td><td><span id=\"" + this.idBase + "ErrorId" + "\"></span></td></tr></table>");

    
    //System.out.println(result);
    
    this.getJspContext().getOut().print(result.toString());
  }

}
