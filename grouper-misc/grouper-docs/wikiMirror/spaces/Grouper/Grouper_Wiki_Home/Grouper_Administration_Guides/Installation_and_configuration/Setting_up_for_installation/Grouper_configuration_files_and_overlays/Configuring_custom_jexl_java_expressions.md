---
title: "Configuring custom jexl java expressions"
space: Grouper
pageId: 28555497
version: 5
lastUpdated: 2026-07-01T05:38:06.304Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555497/Configuring+custom+jexl+java+expressions
---

## Overview

 [JEXL is an expression language in Java](https://commons.apache.org/proper/commons-jexl/). Grouper evaluates JEXL expressions in many places (provisioning translations, the loader, the custom UI, rules, and more).

 Depending on the API and what the JEXL is used for, the evaluation may allow references to **static methods on Java classes you deploy**. For example, the provisioning framework translations allow static class references. (The legacy PSPNG provisioner also allowed this; PSPNG was removed in v6.)

 To call your own Java from a JEXL expression you compile a class with a public static method, deploy it as a jar in the Grouper webapp, and reference it by its fully qualified `package.Class.method(...)` name.

 > Applies to v4+. Two preconditions:
> 
> 
> 
> - The calling API must allow static classes (the `allowStaticClasses` flag on the EL evaluation) — not every place that evaluates JEXL does.
> - **Privileges:** deploying a jar requires write access to the Grouper application server / container filesystem (the webapp `WEB-INF/lib`) and the ability to restart Grouper — i.e. a Grouper installer/administrator, not an end user.

 

## Compile the class

 First compile a class. You can compile it outside Grouper, check out the Grouper code, add a Maven dependency, or compile in the container — whatever is convenient.

 This example uses Grouper libraries, so add a simple Maven dependency (for example, in Eclipse). Set the `<version>` to the Grouper version you run.

 pom.xml

 
```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>sampleJexl</groupId>
  <artifactId>sampleJexl</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <dependencies>
    <!-- https://mvnrepository.com/artifact/edu.internet2.middleware.grouper/grouper -->
    <dependency>
        <groupId>edu.internet2.middleware.grouper</groupId>
        <artifactId>grouper</artifactId>
        <version>2.5.35</version>
    </dependency>
  </dependencies>
  <build>
    <sourceDirectory>src</sourceDirectory>
    <plugins>
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.1</version>
        <configuration>
          <source>1.8</source>
          <target>1.8</target>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

 Make a class with a public static method:

 
```
package sampleJexl;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class SampleClass {

  public static void main(String[] args) {

  }

  public static String tableauADGroupName(String groupName) {
    /* Strip contextual stuff */
    String tableauName = groupName.replace("app:tableau:org:","")
                          .replaceAll(":roles:",":")
                          .replaceAll(":protected:",":")
                          .replaceAll(":projects:",":");
    /*
      convert some:prefix:blah:blah_suffix
      to just some:prefix:blah_suffix
      That is squeeze out the last folder before the group.
    */
    /* by indexes */
    /*
      int lastColon = tableauName.lastIndexOf(":");
      int prevColon = tableauName.lastIndexOf(":",lastColon - 1);
      tableauName = tableauName.substring(0,prevColon) + tableauName.substring(lastColon);
    */
    /* With grouper util methods */
    String parentFolder = GrouperUtil.parentStemNameFromName(tableauName);
    String grandParentFolder = GrouperUtil.parentStemNameFromName(parentFolder);
    String groupSuffix = GrouperUtil.suffixAfterChar(tableauName, ':');
    tableauName = grandParentFolder + ":" + groupSuffix;
    /* _ and : turn into - */
    tableauName = tableauName.replaceAll("[_:]","-");
    /* Take the last 48 chars, or the whole string,
      so the full group name is <= 64 chars as required by AD
    */
    int cutoff = Math.max(tableauName.length() - 48, 0);
    tableauName = tableauName.substring(cutoff);
    /* Add the prefix */
    tableauName = "Grouper-Tableau-" + tableauName;
    return tableauName;
  }

}

```

 

## Test the expression

 To invoke a static method, use the fully qualified package, class, and method name. In this case the package is `sampleJexl`, the class is `SampleClass`, and the method is `tableauADGroupName`.

 
```
  public static void main(String[] args) {

    // Grouper-Tableau-Enterprise-HR-LaborRelations_project_leaders
    String result = GrouperUtil.substituteExpressionLanguage(
        "${sampleJexl.SampleClass.tableauADGroupName('app:tableau:org:Enterprise:HR:projects:LaborRelations:roles:LaborRelations_project_leaders')}",
        null, true, false, false);

    if (!"Grouper-Tableau-Enterprise-HR-LaborRelations-project-leaders".equals(result)) {
      throw new RuntimeException(result);
    }

    System.out.println("Success");
  }

```

 

## Deploy the jar

 Package the compiled class (and its source, if you like) into a jar. One simple approach is to copy the class file and source into a directory, make a zip, and rename it to `.jar`:

 

 

 Add the jar to `slashRoot/opt/grouper/grouperWebapp/WEB-INF/lib` (or include it in a sub-image), and restart Grouper.
