---
title: "Grouper developers: repeat group config indexes"
space: GrIntDev
pageId: 48792698
version: 2
lastUpdated: 2026-07-12T07:01:13.246Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792698/Grouper+developers+repeat+group+config+indexes
---

When there is a repeatGroup in a UI config item:

```
# description
# {valueType: "string", order: 23500, required: true, showEl: "${numberOfInputs > $i$ && !externalizedText}", repeatGroup: "input", repeatCount: 50}
# grouperGshTemplate.testGshTemplate.input.$i$.description =	
```

Then the configs look like this:

```
grouperGshTemplate.myConfig.input.4.description = My description
```

We need a label on the screen for that, and specific to that group, if there is a name, the externalized text is (1 indexed):

```
config.GshTemplateConfiguration.attribute.input.i.description.label = Input __i+1__: description
```

Or zero indexed

```
config.GshTemplateConfiguration.attribute.input.i.description.label = Input __i__: description
```

That will get substituted in the subsection or input label either to the name of that grouped element, or the index or index plus one

The code that does the specific substitution is in GrouperConfigurationModuleBase.formatIndexes()

```
      // GSH templates
      if (StringUtils.equals(preIndex, "input")) {
        name = (String)this.retrieveObjectValueSubstituteMap().get("input." + index + ".name");
      }

```
