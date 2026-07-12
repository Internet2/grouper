---
title: "Grouper plugins classpath OSGI with Felix"
space: GrIntDev
pageId: 48792669
version: 12
lastUpdated: 2026-07-12T07:01:11.545Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792669/Grouper+plugins+classpath+OSGI+with+Felix
---

Grouper 2.6.6 has OSGI with Apache Felix. This allows plugins which can have jar dependencies that Grouper would not be dependent on so we do not end up in [Java jar hell](https://dzone.com/articles/what-is-jar-hell).

The intent is not to do dynamic modules, or modules with dependencies on other modules, just simple classloader separation of jars. Any changes to plugins would need a restart of Grouper. User-defined plugins are supported.

Plugins would depend on Grouper in a compile-time only mode, and only to get the interface and javabeans of the plugin contract. There might be another jar which depends on Grouper and does not add any dependencies in order to use the Grouper jars and API.

## OSGI interface

Grouper defines an interface that the plugin can implement including simple Javabeans as inputs and outputs. Implementations of this interface could be plugins or not (if not needing other jar dependencies). The interfaces and javabeans should be in their own package.

An example of this could be the provisioning framework DAO interface. There is a bean for input and output of each method to make this extensible without overloading methods.

Note: do not overload any method names!

All beans and the interface must be in the same package and nothing else should be in that package.

```
package edu.internet2.middleware.grouper.plugins.testInterface;

public class SamplePluginProviderInput {

  private String input1;

  public String getInput1() {
    return input1;
  }

  public void setInput1(String input1) {
    this.input1 = input1;
  }
  
}
```

```
package edu.internet2.middleware.grouper.plugins.testInterface;

public class SamplePluginProviderOutput {

  private String output1;

  public String getOutput1() {
    return output1;
  }

  public void setOutput1(String output1) {
    this.output1 = output1;
  } 
}
```

```
package edu.internet2.middleware.grouper.plugins.testInterface;

public interface SamplePluginProviderService {
 
    public SamplePluginProviderOutput provide(SamplePluginProviderInput providerInput);
 
}
```

## Make an implementation that has a jar (that shouldnt be in the Grouper classloader)

[https://github.com/Internet2/grouper/tree/GROUPER_2_6_BRANCH/grouper-misc/test-plugin](https://github.com/Internet2/grouper/tree/GROUPER_2_6_BRANCH/grouper-misc/test-plugin)

In this case it is just commons-lang but its just an example. Note, this is a separate project

```
package edu.internet2.middleware.grouper.plugins.testImplementation;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderInput;
import edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderOutput;
import edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderService;

/**
 * some implementation
 * @author mchyzer
 *
 */
public class SamplePluginProviderServiceImpl implements SamplePluginProviderService {

  /**
   * 
   */
  @Override
  public SamplePluginProviderOutput provide(SamplePluginProviderInput providerInput) {
    SamplePluginProviderOutput providerOutput = new SamplePluginProviderOutput();
    if (StringUtils.equals(providerInput.getInput1(), "hey")) {
      providerOutput.setOutput1(providerInput.getInput1() + " hey output");
    } else {
      providerOutput.setOutput1(providerInput.getInput1() + " nonhey output");
    }
    return providerOutput;
  }
 
}
```

## Plugin pom for maven

There are two pom.xml's. One is to register this jar in maven. Note, the jar in maven will NOT have all the dependencies embedded. Note this jar will not be in the grouper container anywhere, it is just stored in maven central (or your institution's maven) so there is a snapshot of it.

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>test-plugin</groupId>
  <artifactId>test-plugin</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>test-plugin</name>
  <description>test-plugin</description>
  <packaging>jar</packaging>
  <dependencies>
    <dependency>
      <groupId>edu.internet2.middleware.grouper</groupId>
      <artifactId>grouper</artifactId>
      <version>2.6.0-SNAPSHOT</version>
      <scope>compile</scope>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.apache.commons/commons-lang3 -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.12.0</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <configuration>
          <archive>
            <index>true</index>
            <manifestEntries>
              <Specification-Title>${project.name}</Specification-Title>
              <Implementation-Vendor>${project.organization.name}</Implementation-Vendor>
              <Implementation-Title>${project.artifactId}</Implementation-Title>
              <Implementation-Version>${project.version}</Implementation-Version>
              <Build-Timestamp>${maven.build.timestamp}</Build-Timestamp>
            </manifestEntries>
          </archive>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

## Plugin pom for plugin that includes dependencies

pom-with-dependencies.pom. note in the pom it specifies which jars are included in the bundle. This artifact is not stored in maven central (for space reasons), and is only used in container

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <!-- goals: -f pom-with-dependencies.pom install  -->
  <modelVersion>4.0.0</modelVersion>
  <groupId>test-plugin</groupId>
  <artifactId>test-plugin-with-dependencies</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>test-plugin</name>
  <description>test-plugin</description>
  <dependencies>
    <dependency>
      <groupId>edu.internet2.middleware.grouper</groupId>
      <artifactId>grouper</artifactId>
      <version>2.6.0-SNAPSHOT</version>
      <scope>compile</scope>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.apache.commons/commons-lang3 -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.12.0</version>
    </dependency>
  </dependencies>
  <packaging>bundle</packaging>
  <build>
    <plugins>
        <plugin>
            <groupId>org.apache.felix</groupId>
            <artifactId>maven-bundle-plugin</artifactId>
            <extensions>true</extensions>
            <configuration>
                <instructions>
                    <Bundle-SymbolicName>
                        ${project.groupId}.${project.artifactId}
                    </Bundle-SymbolicName>
                    <Bundle-Name>${project.artifactId}</Bundle-Name>
                    <Bundle-Version>${project.version}</Bundle-Version>
                    <Private-Package></Private-Package>
                    <Export-Package>
                        edu.internet2.middleware.grouper.plugins.testImplementation
                    </Export-Package>
                    <Import-Package>
                        edu.internet2.middleware.grouper.plugins.testInterface
                    </Import-Package>
                    <Embed-Dependency>commons-lang3</Embed-Dependency>
                </instructions>
            </configuration>
        </plugin>
    </plugins>
  </build>
</project>
```

## Inspect the jar (bundle)

Note the dependent jars are in the jar (and will not be in the classpath of Grouper)

## Plugin path

Plugins without embedded dependencies are not located in the Grouper container anywhere. Plugins with embedded dependencies must be in the directory:

```
/opt/grouper/grouperWebapp/WEB-INF/grouperPlugins
```

An example jar path would be

```
/opt/grouper/grouperWebapp/WEB-INF/grouperPlugins/test-plugin-with-dependencies-0.0.1.jar
```

## Configure the plugin

Note, in this case, the interface and all beans that the interface uses in methods must be located in the Grouper package: edu.interent2.middleware.grouper.some.package

grouper.properties

```
# jar name of plugin e.g. my-plugin-with-embedded-dependencies-1.2.3.jar
# {valueType: "string", required: true, order: 10000}
# grouperOsgiPlugin.testConfigId.jarName =

# number of implementations that this jar provides
# {valueType: "integer", required: true, order: 11000, formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"] }
# grouperOsgiPlugin.testConfigId.numberOfImplementations =

# implementation class that implements an interface, e.g. some.package.SomeClass
# {valueType: "string", required: true, order: 12000, showEl: "${numberOfImplementations > $i$}", repeatGroup: "osgiImplementation", repeatCount: 10}
# grouperOsgiPlugin.testConfigId.osgiImplementation.$i$.implementationClass =

# interface that the class implements.  e.g. edu.interent2.middleware.grouper.some.package.SomeInterface
# {valueType: "string", required: true, order: 13000, showEl: "${numberOfImplementations > $i$}", repeatGroup: "osgiImplementation", repeatCount: 10}
# grouperOsgiPlugin.testConfigId.osgiImplementation.$i$.implementsInterface =

grouperOsgiPlugin.somePluginConfigId.jarName = test-plugin-with-dependencies-0.0.1-SNAPSHOT.jar
grouperOsgiPlugin.somePluginConfigId.numberOfImplementations = 1
grouperOsgiPlugin.somePluginConfigId.osgiImplementation.0.implementationClass = edu.internet2.middleware.grouper.plugins.testImplementation.SamplePluginProviderServiceImpl
grouperOsgiPlugin.somePluginConfigId.osgiImplementation.0.implementsInterface = edu.internet2.middleware.grouper.plugins.testInterface.SamplePluginProviderService

```

## Invoke a plugin

Specify the plugin version, and implementation class

```
import edu.internet2.middleware.grouper.plugins.GrouperPluginManager;

// ...
      
      SamplePluginProviderInput providerInput = new SamplePluginProviderInput();
      providerInput.setInput1("hey");
      
      SamplePluginProviderService someGrouperInterface = GrouperPluginManager.retrievePluginImplementation("test-plugin-with-dependencies-0.0.1-SNAPSHOT.jar", SamplePluginProviderService.class, "edu.internet2.middleware.grouper.plugins.testImplementation.SamplePluginProviderServiceImpl");
  
      SamplePluginProviderOutput providerOutput = someGrouperInterface.provide(providerInput);      
```

or, dont worry about plugin version, if there is more than one plugin with prefix, throw error. If one is there, use it

```
import edu.internet2.middleware.grouper.plugins.GrouperPluginManager;

// ...
      
      SamplePluginProviderInput providerInput = new SamplePluginProviderInput();
      providerInput.setInput1("hey");
      
      // dont worry about version
      SamplePluginProviderService someGrouperInterface = GrouperPluginManager.retrievePluginImplementation("test-plugin-with-dependencies", SamplePluginProviderService.class, "edu.internet2.middleware.grouper.plugins.testImplementation.SamplePluginProviderServiceImpl");
  
      SamplePluginProviderOutput providerOutput = someGrouperInterface.provide(providerInput);

```

or, if there is only one implementation of an interface in a plugin, can be simplified. If there is more than one implementation, throw error

```
import edu.internet2.middleware.grouper.plugins.GrouperPluginManager;

// ...
      
      SamplePluginProviderInput providerInput = new SamplePluginProviderInput();
      providerInput.setInput1("hey");
      
      SamplePluginProviderService someGrouperInterface = GrouperPluginManager.retrievePluginImplementation("test-plugin-with-dependencies", SamplePluginProviderService.class);
  
      SamplePluginProviderOutput providerOutput = someGrouperInterface.provide(providerInput);

```

## Developers

If you want to change the path where plugins are loaded from (for development only), change this in the grouper.properties. You can also adjust the felix cache dir if not in container.

```
# directory of plugins, default to /opt/grouper/grouperWebapp/WEB-INF/grouperPlugins
# {valueType: "string", required: true, order: 1000}
grouper.osgi.jar.dir = /opt/grouper/grouperWebapp/WEB-INF/grouperPlugins

# directory of felix cache of plugins, default to /opt/grouper/grouperWebapp/WEB-INF/grouperFelixCache
# {valueType: "string", required: true, order: 2000}
grouper.felix.cache.rootdir = /opt/grouper/grouperWebapp/WEB-INF/grouperFelixCache
```
