---
title: "Grouper configuration files and overlays"
space: Grouper
pageId: 28549156
version: 72
lastUpdated: 2026-07-07T15:09:33.778Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549156/Grouper+configuration+files+and+overlays
---

> In a v2.4 patch, and in v2.5+, Grouper can store configuration in the database rather than in configuration files. This is the recommended approach. See [Grouper configuration in the database and UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555638/Grouper+configuration+in+the+database+and+UI).

## Overview

Some Grouper configuration files can have overlays, so that only the changes from the config file defaults need to be tracked in an institution-specific config file. Configs can also be stored centrally on a server across multiple webapps or standalone Grouper applications. There can be a default configuration file and an override file, so that only the changes from the default are tracked in the overlay.

Using overlays can make Grouper more easily deployable across environments, and more easily upgradable.

Overlays are available in v2.2+ for the following config files:

- grouper.properties
- grouper.hibernate.properties
- grouper-loader.properties
- grouper-ui.properties
- grouper-ws.properties
- grouper.client.properties
- subject.properties
- grouper.cache.properties (v2.3.0 patch+)
- grouper.text.en.us.properties

> **Access required:** editing these files is a server-side task — it needs filesystem/container access to the Grouper application and a restart (or waiting for the reload interval, see below). It is not controlled by a Grouper privilege. Where possible, prefer storing configuration in the database (above), which can be edited in the UI by a Grouper sysadmin.

## How it works

Each properties file has a base file and a config file. For example, there is a grouper.base.properties and a grouper.properties. Both are located on the classpath in the default package (e.g. WEB-INF/classes/grouper.base.properties and WEB-INF/classes/grouper.properties). Generally all the default settings live in the base file, and only the things that are overridden are in the grouper.properties. This is a change in v2.2+; before that, all properties were in the grouper.properties file and the example file was used just to show which configs are possible.

## Specify the hierarchy

If you do not want to use the base and the overlay in the classpath, you can specify which files are used for the properties. This must be specified in the base or config file. Each config file has its own key for this hierarchy, listed in the base config file. The example below is for grouper.properties. It is recommended to include the classpath base properties, though it is up to you. You can specify config files by classpath or file location.

## Edge cases

If you override a key with an empty value, that will blank out that config value.

## Config file reloading

You can specify the number of seconds between checks for differences in the config file. This is not a trivial check, so it is recommended to be no more often than every 60 seconds. If -1 is specified, the file is never re-checked. If 0 is specified, it is checked each time a config param is referenced. This property must be specified in the base or config file; it cannot be put in the other files specified in the hierarchy. The default is 600 seconds. Here is an example for grouper.properties:

```text
grouper.config.secondsBetweenUpdateChecks = 60

```

## Computing and referencing config values

### Refer to other properties in the same config file

In both of the cases below, the value for the property "somethingWhatever1" is: **prefix something to be reused suffix**

Before the v2.3.0 patch, you can do this:

```text
somethingWhatever = something to be reused
somethingWhatever1.elConfig = prefix ${edu.internet2.middleware.grouperClient.util.GrouperClientConfig.retrieveConfig().propertyValueString("somethingWhatever")} suffix
```

In v2.3.0+ you can do this:

```text
somethingWhatever = something to be reused
somethingWhatever1 = prefix $$somethingWhatever$$ suffix
```

### Expression language (EL) in property values

You can specify a property key so that its value includes expression language scriptlets. The class [edu.internet2.middleware.grouperClient.util.GcElUtilsSafe](https://software.internet2.edu/grouper/doc/2.5.x/grouper-misc/grouperClient/apidocs/edu/internet2/middleware/grouperClient/util/GcElUtilsSafe.html) can be referenced as "elUtils". To specify a key as EL, append the suffix `.elConfig` to the config key.

To use a custom class in the EL, write a class with a static method, compile it, and put it in a jar on the classpath. There should be a default constructor in the class. Refer to the fully qualified class in EL:

```java
package edu.internet2.middleware.grouperClient.config;

/**
 * some test class for EL
 * @author mchyzer
 *
 */
public class SomeTestElClass {

  /**
   * some method for EL
   * @param a
   * @param b
   * @return the result
   */
  public static String someMethod(String a, String b) {
    return a + b + " something else";
  }

}

```

EL in a properties file:

```text
some.config.2.elConfig = ${edu.internet2.middleware.grouperClient.config.SomeTestElClass.someMethod('start', ' middle')}

```

This results in the value (for key some.config.2): start middle something else

Another example — look in another non-Grouper properties file:

```text
hibernate.connection.url.elConfig = ${ edu.internet2.middleware.grouper.util.GrouperUtil.propertiesFromFile(new("java.io.File","/Users/mchyzer/git/grouper_v2_5/grouper/conf/myfile.properties"), false).getProperty("myurl") }
```

### Environment variables

If you want a Grouper properties config file to read a value from an environment variable, any property can be an env var. Configure it in a config file as follows (for property a.b.c):

```text
a.b.c.elConfig = ${elUtils.processEnvVarOrFile('SOME_ENV_VAR')}
```

Do the following two things:

1. append `.elConfig` to the property name
2. set the value to `${elUtils.processEnvVarOrFile('JAVA_HOME')}`

Note, if the value of the env var is itself a variable, it will be resolved from there.

Example. When I have this in grouper.properties:

```text
# in cases where grouper is logging or emailing, it will use this to differentiate test vs dev vs prod
grouper.env.name = GROUPERDEMO_2_2_2
```

I can print it out in GSH:

```text
gsh 1% edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertyValueString("grouper.env.name")
GROUPERDEMO_2_2_2
```

Add an env variable:

```bash
[appadmin@i2midev1 bin]$ export GROUPER_ENV=GROUPER_2_2_2_fromEnv
[appadmin@i2midev1 bin]$ echo $GROUPER_ENV
GROUPER_2_2_2_fromEnv
```

Change grouper.properties:

```text
# grouper.env.name = GROUPERDEMO_2_2_2
grouper.env.name.elConfig = ${java.lang.System.getenv().get('GROUPER_ENV')}
```

Restart GSH, try again:

```text
gsh 0% edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertyValueString("grouper.env.name") GROUPER_2_2_2_fromEnv
```

### Escape characters

You can escape characters; when they are read by the property config framework, they will be unescaped.

| **Unicode** | **Value** |
| --- | --- |
| `U+0024` | $ |
| `U+0020` | space |
| `U+007B` | { |
| `U+007D` | } |
| `U+000A` | newline \n |
| `U+002B` | + |

### Special variables

You can use these special vars: `$space$` and `$newline$` to represent a space or a newline.

## Multi-level overlays

Some sites may choose to have two levels of overlay files above the base file, as follows:

- grouper.base.properties (unmodified built-in properties)
- grouper.properties (institution-wide)
- grouper.local.properties (specific to institution and environment, e.g. the "test" env overlays)

## Reading configs from Java (GSH templates or scripts)

```java
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;
import edu.internet2.middleware.grouperClient.config.GrouperUiApiTextConfig;

public class Test63main {

  public static void main(String[] args) {

    GrouperSession.startRootSession();

    boolean defaultBoolean = false;
    GrouperConfig.retrieveConfig().propertyValueBoolean("key", defaultBoolean);
    GrouperConfig.retrieveConfig().propertyValueBooleanRequired("key");
    GrouperConfig.retrieveConfig().propertyValueString("stringKey");
    GrouperConfig.retrieveConfig().propertyValueStringRequired("stringKey");
    GrouperConfig.retrieveConfig().propertyValueString("stringKey2", "defaultValue");
    int defaultInt = 999;
    GrouperConfig.retrieveConfig().propertyValueInt("intKey", defaultInt);
    GrouperConfig.retrieveConfig().propertyValueInt("intKey");
    GrouperConfig.retrieveConfig().propertyValueIntRequired("intKey");

    Map<String, String> propertiesMap = GrouperHibernateConfig.retrieveConfig().propertiesMap(Pattern.compile("^something\\.([^.]+)\\..*$"));

    Set<String> propertyConfigIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(Pattern.compile("^something\\.([^.]+)\\..*$"));

    GrouperUiConfigInApi.retrieveConfig().propertyValueString("key");

    GrouperWsConfigInApi.retrieveConfig().propertyValueString("key");

    // this will eval all jexl scripts.  if you want the jexl script to show on screen use HTML for dollar: $
    GrouperTextContainer.textOrNull("someUiKey");

    // dont eval jexl scripts, just get the raw value
    GrouperUiApiTextConfig.retrieveTextConfig().propertyValueString("someKey");
  }

}

```

## See also

[Grouper configuration in the database and UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555638/Grouper+configuration+in+the+database+and+UI)
