---
title: "Grouper custom template via GSH - V2 - dynamic drop downs and text"
space: Grouper
pageId: 28555130
version: 5
lastUpdated: 2026-07-01T05:38:52.969Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555130/Grouper+custom+template+via+GSH+-+V2+-+dynamic+drop+downs+and+text
---

In v4.10.3+ and v5.7.1+, GSH templates can have dynamic drop downs (or maybe other form elements) from a V2 template that can be based on the user or other input values. These could be retrieved from external system. The template can also dynamically hide elements, or set values for elements.

To implement this, override method decorateTemplateForUiDisplay() in your GshTemplateV2 subclass. Add pairs of keys and values as MultiKey items for the drop down options. Note, if a MultiKey has a third value like '<optgroup>', the html will be used directly instead of creating a drop down item. This can be used to make category headings in the list.

| Variable or method | Class | Description |
| --- | --- | --- |
| gshTemplateDecorateForUiInput | GshTemplateDecorateForUiInput | parameter passed to the method. |
| gshTemplateDecorateForUiInput    .getEventConfigId() | String | name of the variable that had a change in value |
| gshTemplateDecorateForUiInput    .getGshTemplateInputConfigAndValues() | Map<String, GshTemplateInputConfigAndValue> | Map of UI widgets, with the input variable (gsh_input_*) name as the key, You can call get() to retrieve an item, and remove() to hide it from the UI screen |
| gshTemplateInputConfigAndValue | GshTemplateInputConfigAndValue | The UI item and associated values. Call    - getValue() - get the current value if a text field - getValueOrDefault() - get the current value, or the default value configured in the template - setValue(String) - Set the value of the item to a specific string - getGshTemplateInputConfig() - retrieve the UI widget handler to control various UI display options, like the label, description, and drop down keys and values |
| gshTemplateInputConfig() | GshTemplateInputConfig | Input config associated with this UI element. Call    - setDropdownKeysAndLabels(List<MultiKey>) - sets drop down keys and values. - setDescription(String) - setLabel(String) - setRequired(boolean) - setValidationMessage(String) |

Sample code:

```
public class MyExampleTemplate extends GshTemplateV2 {
    ...
    @Override
    public void decorateTemplateForUiDisplay(GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput) {
        GshTemplateInputConfigAndValue foodConfigAndValue = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().get("gsh_input_bundleId")

        /* drop down for food choice */
        List<MultiKey> foodItems = new ArrayList<MultiKey>()
        foodItems.add(new MultiKey("", ""))
        foodItems.add(new MultiKey(null, null, "<optgroup label=\"Fruits\">"))
        foodItems.add(new MultiKey("apple", "Apple"))
        foodItems.add(new MultiKey("banana", "Banana"))
        foodItems.add(new MultiKey(null, null, "</optgroup>"))

        foodItems.add(new MultiKey(null, null, "<optgroup label=\"Vegetables\">"))
        foodItems.add(new MultiKey("carrot", "Carrot"))
        foodItems.add(new MultiKey("spinach", "Spinach"))
        foodItems.add(new MultiKey(null, null, "</optgroup>"))

        foodConfigAndValue.getGshTemplateInputConfig().setDropdownKeysAndLabels(foodItems)
    }
    ...
}

```

## Examples

Here drop down 2 is blank, the text field is hidden.

Selecting an option from drop down 1 will populate the options for drop down 2

It also shows the textfield and pre-populates the value

If drop down 1 is changed, the options for drop down 2 change

And the value for the textfield changes

Note that is drop down 2 changes (not 1), then the value does not change.

## Config

grouper.properties

```
grouperGshTemplate.templateWithDynamicDropdown.defaultRunButtonFolderUuidOrName = test
grouperGshTemplate.templateWithDynamicDropdown.folderShowType = allFolders
grouperGshTemplate.templateWithDynamicDropdown.gshTemplate = //
grouperGshTemplate.templateWithDynamicDropdown.input.0.description = Drop down
grouperGshTemplate.templateWithDynamicDropdown.input.0.dropdownValueFormat = dynamicFromTemplate
grouperGshTemplate.templateWithDynamicDropdown.input.0.formElementType = dropdown
grouperGshTemplate.templateWithDynamicDropdown.input.0.label = Drop down
grouperGshTemplate.templateWithDynamicDropdown.input.0.name = gsh_input_dropDown
grouperGshTemplate.templateWithDynamicDropdown.input.1.description = The drop down 2
grouperGshTemplate.templateWithDynamicDropdown.input.1.dropdownValueFormat = dynamicFromTemplate
grouperGshTemplate.templateWithDynamicDropdown.input.1.formElementType = dropdown
grouperGshTemplate.templateWithDynamicDropdown.input.1.label = Drop down 2
grouperGshTemplate.templateWithDynamicDropdown.input.1.name = gsh_input_dropDown2
grouperGshTemplate.templateWithDynamicDropdown.input.2.description = Defaults when changing first drop down, not second
grouperGshTemplate.templateWithDynamicDropdown.input.2.label = Text field
grouperGshTemplate.templateWithDynamicDropdown.input.2.name = gsh_input_textField
grouperGshTemplate.templateWithDynamicDropdown.input.2.validationType = none
grouperGshTemplate.templateWithDynamicDropdown.moreActionsLabel = Dynamic dropdown
grouperGshTemplate.templateWithDynamicDropdown.numberOfInputs = 3
grouperGshTemplate.templateWithDynamicDropdown.runAsType = GrouperSystem
grouperGshTemplate.templateWithDynamicDropdown.runButtonGroupOrFolder = folder
grouperGshTemplate.templateWithDynamicDropdown.securityRunType = wheel
grouperGshTemplate.templateWithDynamicDropdown.showInMoreActions = true
grouperGshTemplate.templateWithDynamicDropdown.showOnFolders = true
grouperGshTemplate.templateWithDynamicDropdown.templateDescription = Dynamic dropdown
grouperGshTemplate.templateWithDynamicDropdown.templateName = Dynamic dropdown
grouperGshTemplate.templateWithDynamicDropdown.templateVersion = V2
 
```

## GSH script

```
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateDecorateForUiInput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfigAndValue;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class Test72dropDownDynamic extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    gsh_builtin_gshTemplateOutput.assignRedirectToGrouperOperation("NONE");

    String dropDownValue = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_dropDown");
    
    gsh_builtin_gshTemplateOutput.addOutputLine("First drop down is: " + dropDownValue);
  }

  @Override
  public void decorateTemplateForUiDisplay(GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput) {
    GshTemplateInputConfigAndValue dropDownConfigAndValue = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().get("gsh_input_dropDown");
    
    // dynamically set the options for drop down 1
    List<MultiKey> localDropdownKeysAndLabels = new ArrayList<MultiKey>();
    localDropdownKeysAndLabels.add(new MultiKey("", ""));
    localDropdownKeysAndLabels.add(new MultiKey("key1", "value1"));
    localDropdownKeysAndLabels.add(new MultiKey("key2", "value2"));
    dropDownConfigAndValue.getGshTemplateInputConfig().setDropdownKeysAndLabels(localDropdownKeysAndLabels);
    
    GshTemplateInputConfigAndValue dropDownConfigAndValue2 = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().get("gsh_input_dropDown2");
    
    // if drop down 1 has somethign set
    if (!StringUtils.isBlank(dropDownConfigAndValue.getValueOrDefault())) {

      // if the first option is selected, set the drop down 2 options to one set 
      if (StringUtils.equals("key1", dropDownConfigAndValue.getValueOrDefault())) {
        List<MultiKey> localDropdownKeysAndLabels2 = new ArrayList<MultiKey>();
        localDropdownKeysAndLabels2.add(new MultiKey("", ""));
        localDropdownKeysAndLabels2.add(new MultiKey("key1a", "value1a"));
        localDropdownKeysAndLabels2.add(new MultiKey("key1b", "value1b"));
        dropDownConfigAndValue2.getGshTemplateInputConfig().setDropdownKeysAndLabels(localDropdownKeysAndLabels2);
        
        // if the second option is selected, set the drop down 2 options to another set
      } else if (StringUtils.equals("key2", dropDownConfigAndValue.getValueOrDefault())) {
        List<MultiKey> localDropdownKeysAndLabels2 = new ArrayList<MultiKey>();
        localDropdownKeysAndLabels2.add(new MultiKey("", ""));
        localDropdownKeysAndLabels2.add(new MultiKey("key2a", "value2a"));
        localDropdownKeysAndLabels2.add(new MultiKey("key2b", "value2b"));
        dropDownConfigAndValue2.getGshTemplateInputConfig().setDropdownKeysAndLabels(localDropdownKeysAndLabels2);
        
      }
      
    }

    // if drop down 1 is blank, hide the textfield (it is shown by default)
    if (StringUtils.isBlank(dropDownConfigAndValue.getValue())) {
      
      gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().remove("gsh_input_textField");

      // if drop down 1 was changed, then set the text field to its value (overwrite whats there)
    } else if (StringUtils.equals("gsh_input_dropDown", gshTemplateDecorateForUiInput.getEventConfigId())) {
      
      GshTemplateInputConfigAndValue textFieldConfigAndValue = gshTemplateDecorateForUiInput.getGshTemplateInputConfigAndValues().get("gsh_input_textField");
      
      // if the first option for drop down 1 is there, then set to one thing
      if (StringUtils.equals("key1", dropDownConfigAndValue.getValue())) {
        textFieldConfigAndValue.setValue("<key1 value default>");
        
        // if the second option for drop down 1 is there, then set to another thing
      } else if (StringUtils.equals("key2", dropDownConfigAndValue.getValue())) {
        textFieldConfigAndValue.setValue("<key2 value default>");
      }
      
      // if drop down 1 is blank, leave the textfield unchanged
    }

  }

}
```
