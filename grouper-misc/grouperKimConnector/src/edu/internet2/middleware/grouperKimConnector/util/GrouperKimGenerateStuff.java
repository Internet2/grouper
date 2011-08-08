/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.util;


/**
 * generate things for grouper kim
 */
public class GrouperKimGenerateStuff {

  /**
   * @param args
   */
  public static void main(String[] args) {
    //System.out.println(generateEdlPermissionsDefinitions());
    System.out.println(generateEdlPermissionsHtml());
  }

  /**
   * generate edl definitions
   * @return edl definitions
   */
  public static String generateEdlPermissionsDefinitions() {
    
    StringBuilder result = new StringBuilder("<!-- Generated from GrouperKimGenerateStuff from grouperKimConnector project -->\n");
    int size = 50;
    for (int i=0;i<size;i++) {
      result.append("      <fieldDef name=\"attributeDefName" + i + "\" title=\"Attribute def name" + i + "\">\n"
                  + "        <display>\n"
                  + "          <type>text</type>\n"
                  + "          <meta>\n"
                  + "            <name>size</name>\n"
                  + "            <value>100</value>\n"
                  + "          </meta>\n"
                  + "        </display>\n");
      if (i==0) {
        result.append("        <validation required=\"true\">\n");
        result.append("          <message>Please enter an org</message>\n");
        result.append("        </validation>\n");

      }
      result.append("      </fieldDef>\n");
      
      
      result.append("      <fieldDef name=\"attributeDefName" + i + "extension\" title=\"Attribute def name" + i + " extension\">\n"
          + "        <display>\n"
          + "          <type>text</type>\n"
          + "          <meta>\n"
          + "            <name>size</name>\n"
          + "            <value>20</value>\n"
          + "          </meta>\n"
          + "        </display>\n"
          + "      </fieldDef>\n");


    }
    
    for (int i=0;i<size;i++) {
      result.append("    <fieldDef name=\"attributeDefNameAddRemove" + i + "\" title=\"Org add remove" + i + "\">\n"
                  + "      <display>\n"
                  + "        <type>radio</type>\n"
                  + "        <values title=\"Add\">add</values>\n"
                  + "        <values title=\"Remove\">remove</values>\n"
                  + "      </display>\n"
                  + "    </fieldDef>\n");
    }

    
    return result.toString();
  }
  
  /**
   * generate edl html
   * @return edl
   */
  public static String generateEdlPermissionsHtml() {
    
    StringBuilder result = new StringBuilder("<!-- Generated from GrouperKimGenerateStuff from grouperKimConnector project -->\n");
    for (int i=0;i<50;i++) {
      if (i==0) {
        result.append("<xsl:template name=\"orgTableAddRemove0_9\">\n");
      } else if (i % 10 == 0) {
        result.append("</xsl:template>\n");
        result.append("<xsl:template name=\"orgTableAddRemove" + i + "_" + (i+9) + "\">\n");
      }
      result.append("    <tr valign=\"top\" id=\"attributeDefNameTr" + i + "Id\" style=\"display: none;\" class=\"orgTableRow\">\n"
                  + "      <td class=\"fieldLabel\">Org</td>\n"
                  + "      <td valign=\"top\" class=\"fieldAsterisk\">" + (i==0 ? "*" : "") + "</td>\n"
                  + "      <td class=\"fieldInfo\" >\n"
                  + "        <table cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 0; margin: 0;\">\n"
                  + "          <tr>\n"
                  + "            <td>\n"
                  + "              <xsl:call-template name=\"widget_render\">\n"
                  + "                <xsl:with-param name=\"fieldName\" select=\"'attributeDefName" + i + "extension'\" />\n"
                  + "                <xsl:with-param name=\"renderCmd\" select=\"'input'\" />\n"
                  + "                <xsl:with-param name=\"readOnly\">true</xsl:with-param>\n"
                  + "              </xsl:call-template>\n"
                  + "            </td>\n"
                  + "            <td>\n"
                  + "              <xsl:choose>\n"
                  + "                <xsl:when test=\"($isAtNodeInitiated)\">\n"
//                  + "                  <button style=\"white-space: nowrap;\"\n" 
//                  + "                    onclick=\"var theWindow = window.open('http://localhost:8089/grouper/grouperUi/appHtml/grouper.html?operation=AttributeDefNamePicker.index&amp;attributeDefNamePickerName=orgPicker&amp;attributeDefNamePickerElementName=attributeDefName" + i + "','orgPicker', 'scrollbars,resizable,width=700,height=500'); theWindow.focus(); return false;\"\n"
//                  + "                  >Find org</button>\n"
                  + "                <xsl:value-of disable-output-escaping=\"yes\"\n" 
                  + "                  select=\"java:edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtils.xslGrouperButton('AttributeDefNamePicker.index', 'attributeDefNamePickerName', 'orgPicker', 'attributeDefNamePickerElementName', 'attributeDefName" + i + "', 'orgPicker', '700', '500', 'Find org')\" />\n"
                  + "                                    <xsl:text disable-output-escaping=\"yes\">&amp;nbsp;</xsl:text>\n"
                  + "                      <a href=\"#\" onclick=\"assignValueByName('attributeDefName" + i + "', ''); assignValueByName('attributeDefName" + i + "extension', ''); return false;\">Clear</a>\n"
                  + "                </xsl:when>\n"
                  + "              </xsl:choose>\n"
                  + "              <span class=\"attributeDefNameAddRemoveSpan\">\n"
                  + "                <xsl:call-template name=\"widget_render\">\n"
                  + "                  <xsl:with-param name=\"fieldName\" select=\"'attributeDefNameAddRemove" + i + "'\" />\n"
                  + "                  <xsl:with-param name=\"renderCmd\" select=\"'input'\" />\n"
                  + "                  <xsl:with-param name=\"readOnly\" select=\"$isPastInitiated\" />\n"
                  + "                </xsl:call-template>\n"
                  + "              </span>\n"
                  + "            </td>\n"
                  + "            <td id=\"attributeDefName" + i + "Id\" style=\"padding-left: 5px\">\n"
                  + "              <xsl:call-template name=\"widget_render\">\n"
                  + "                <xsl:with-param name=\"fieldName\" select=\"'attributeDefName" + i + "'\" />\n"
                  + "                <xsl:with-param name=\"renderCmd\" select=\"'input'\" />\n"
                  + "                <xsl:with-param name=\"readOnly\">true</xsl:with-param>\n"
                  + "              </xsl:call-template>\n"
                  + "            </td>\n"
                  + "          </tr>\n"
                  + "        </table>\n"
                  + "      </td>\n"
                  + "    </tr>\n");
    }
    result.append("</xsl:template>\n");
    return result.toString();
  }
  
}
