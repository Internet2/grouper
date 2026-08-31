---
title: "Customizing the Grouper UI"
space: Grouper
pageId: 28545413
version: 62
lastUpdated: 2026-07-01T05:47:15.192Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545413/Customizing+the+Grouper+UI
---

[Grouper UI Properties](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793144/Grouper+UI+Properties)

How to Customize the Grouper User Interface (UI)  
  
As of Grouper 2.2, most of the Grouper UI configuration is now in grouper-ui-ng.base.properties, and you can override it in the grouper-ui.properties file or database. Customizable string values that appear in the UI are defined in grouperText/grouper.textNg.en.us.base.properties, and you can override values in grouperText/grouper.text.en.us.properties or in the database configuration for "grouper.text.en.us.properties". See the [Configuration Overlay page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549156/Grouper+configuration+files+and+overlays) for details.

Some settings for Grouper UI customization are described here. Look in grouper-ui-ng.base.properties for more settings in the UI.

### Adding Custom CSS Files

Grouper has its own CSS stylesheets, but provides a mechanism for site-specific stylesheets to be loaded after the Grouper stylesheets. This allows sites to override/extend existing styles and to add new styles. Such changes can alter the position of screen elements, fonts, colors, etc.

Custom stylesheets can be enumerated in grouper-ui property **css.additional**. This is a space separated list of one or more .css files which are inserted into the HEAD of all Grouper pages. The .css files are referenced in order and after any Grouper CSS files. This means that your CSS files can override any Grouper style definition.

Since pages in the UI have the base UI /grouperUi/app/UiV2Main.index, you can reference the root of the webapp with "../../".

### Changing the Internet2 Logo

The grouper-ui property **image.organisation-logo** is the path to the organization logo that appears in the banner. The default value is **grouperExternal/public/assets/images/organisation-logo.gif**. To change this to your own image you can either:

a. Replace this image in your installation with a different image (in the container, the full path is /opt/grouper/grouperWebapp/grouperExternal/public/assets/images/organisation-logo.gif)

b. Add your image to your installation, and then update property **image.organisation-logo** with the new location. The Grouper UI engine will automatically prepend the value with "../../" in the HTML source, effectively the root of the Grouper webapp. Note that this also means that only relative locations work, not full internet URLs.

Note that the default template in Grouper only supports an image in the banner, not additional text.

### Changing the Institution Name

The institution name that appears on the main page and in the footer is defined in the text resource bundle (grouperText/grouper.textNg.en.us.base.properties), property **institutionName**. You will likely want to change this from its default "Institute of Higher Education". This can be configured in overlay file grouperText/grouper.text.en.us.properties, or in the database configuration for config file "grouper.text.en.us.properties".

### Changing the browser title

Set an env name in grouper.properties and use this in externalized text (v4.9.0+)

```
grouperAppName = PennGroups

browserTitleSuffix = , $$grouperAppName$$ ${edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertyValueString("grouper.env.name")}
```

### Changing the Default Language (internationalization, i18n)

The Grouper UI supports internationalization for all navigational text and instructions. Text strings in UI templates are keyword pointers to the actual text in Java ResourceBundle files, which are property files stored in WEB-INF/classes/grouperText/grouper.textNg.<language>.<country>.base.properties. The default language is English (en_us), but a set of French translations (fr_fr) is packaged with Grouper.

The language resources and the default language can be changed in grouper.properties as in the example below. To add more resource bundles, add the file to directory WEB-INF/classes/grouperText/. In the example below, English and French languages would be available, and the preferred language as defined in the browser would be used.

> grouper.text.defaultBundleIndex = 0
> 
> grouper.text.bundle.0.language = en  
> grouper.text.bundle.0.country = us  
> grouper.text.bundle.0.fileNamePrefix = grouperText/grouper.text.en.us  
> grouper.text.bundle.1.language = fr  
> grouper.text.bundle.1.country = fr  
> grouper.text.bundle.1.fileNamePrefix = grouperText/grouper.text.fr.fr

### Changing UI Text

In the standard UI, all navigational text and instructions are derived from a Java ResourceBundle grouperText/grouper.textNg.en.us.base.properties (or other language equivalent). Custom properties to override default values can be added to overlay property file grouperText/grouper.text.en.us.properties, or a database configuration entry can be added to configuration "grouper.text.en.us.properties". Note that in the UI configuration, only overrides will be shown, not the large list of default values from the base properties file.

If you create your own JSP templates or Java classes, you are not under any obligation to use ResourceBundles instead of directly entering hard-coded text. However, if you wish to contribute code back to Grouper, such a contribution would be more useful if it used externalized text.

### Providing Feedback and Getting Help

The Grouper UI is intended to be extensible and not to force unnecessary constraints, however, it is only as sites try to make their own customisations that the true extensibility can be tested. If while customising the Grouper UI you find yourself forced to modify standard Grouper UI sources (of any kind), or find that you cannot easily do what you want to, please offer feedback to, or request help via the [grouper-users mailing list](http://www.internet2.edu/communities-groups/middleware/grouper-working-group/#group-participate).

### See Also

[Grouper UI Properties](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793144/Grouper+UI+Properties)

[New Grouper 2.2 UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543095/User+Interface)

Grouper UI Building and Installation
