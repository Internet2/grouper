---
title: "Using the Playwright script recorder"
space: Grouper
pageId: 28549074
version: 4
lastUpdated: 2024-06-01T00:25:48.216Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549074/Using+the+Playwright+script+recorder
---

## Summary

This feature is a library of functions in the Grouper Java API that could use a headless browser to operate the Grouper UI (or other UIs).

The libraries under consideration are Selenium and Playwright.

A POC was attempted for Selenium to use a headless browser in the Grouper container and it was not successful.

A POC for Playwright was successful, so that is the choice.

Playwright is:

1. Backed by Microsoft
2. Several years old
3. Can record scripts in Java
4. Uses CSS selectors and the DOM
5. Waits for DOM elements to be available automatically
6. Easily handles Duo Universal Prompt
7. Is lightweight, there are no server components to install, is only a handful of jars
8. Can emulate several browsers

## Example of a script recording

I installed node and playwright on my workstation

```
install nodejs
mchyzer@wal-vl216-dhcp085 opt % npx playwright install
mchyzer@wal-vl216-dhcp085 opt % npx playwright codegen https://grouper.apps.upenn.edu
```

That last command pops up the recorder

## Grouper library of Grouper UI operations

We can create and maintain a set of UI operations people might want to take advantage of. Note, above in the GSH script, the authentication (including two-step) and URL will need to be handled, e.g.

```
    page.navigate("https://grouper.apps.upenn.edu");
    
    page.getByLabel("Username").click();
    page.getByLabel("Username").fill("mchyzer");
    page.getByLabel("Username").press("Tab");
    page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).click();
    page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).fill(GrouperConfig.retrieveConfig().propertyValueStringRequired("mchyzer.pass"));
    page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).press("Enter");
    
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("No, other people use this")).click();

```

Then some operation can occur

```
  public static void addMemberToGroup(Page page, Group group, Subject subject) {
    page.locator("[href='UiV2Main.index?operation=UiV2Group.viewGroup&groupId=" + group.getId() + "']").first().click();
    page.locator("#show-add-block").click();
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("import a list of members")).click();
    page.getByLabel("Copy/paste a list of member").check();
    page.locator("#entityListId").click();
    page.locator("#entityListId").fill(subject.getId());
    page.locator("#searchEntitySourceId").selectOption(subject.getSourceId());
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("OK")).click();
  }
```

## CSS selectors of the Grouper UI DOM

We can adjust the HTML attributes in the Grouper UI so it is easier to use tools like Playwright. As the Grouper UI changes (screens are changed, screen flows adjusted, etc), these methods will be changed as well so it will not break scripts that use the Grouper API methods.

## Expected use of Playwright

An institution could write a GSH template UI, WS, or daemon, which uses Playwright to do sanity testing on the UI or other functions in Grouper. i.e. The Grouper Java API could have some helper methods like the one above which change memberships, make things provisionable, kick off daemons, etc. These scripts could also include using the Grouper API to change changes in LDAP, SQL, WS, etc to see that provisioning occurred successfully. When Grouper is upgraded or other changes made, the sanity script could be run to make sure basic parts of Grouper are functioning.

## Security and setup

It is better to keep Grouper thin and keep testing libraries out of Grouper. But there is also a lot of value to have a library of methods so that each institution is not recording their own scripts, and more things than just the browser emulation is available (e.g. anything in the Grouper Java API, and SQL / LDAP / Provisioning DAO's are available).

pom.xml

```
    <!-- https://mvnrepository.com/artifact/com.microsoft.playwright/playwright -->
    <dependency>
        <groupId>com.microsoft.playwright</groupId>
        <artifactId>playwright</artifactId>
        <version>1.43.0</version>
    </dependency>

```

Some options:

1. Include Playwright jars and Grouper API library in container
  
  1. If an institution does not want to use it, then just do not use it
2. Include Playwright jars and Grouper API library in container, but an ENV variable would move the Playright jars out of the lib directory
  
  1. If the ENV variable is set to hobble Playwright, calls would get runtime errors
3. Multiple containers could be available for each version, a container with playwright, or a container without playwright
  
  1. This is not inline with Grouper training where we recommend the same derived image to all institutional envs
  2. There is overhead in having multiple containers that the Grouper team wants to avoid
4. Include Playwright jars and Grouper API library in container but not in the lib directory, but an ENV variable would move the Playright jars into the lib directory
  
  1. If the ENV variable is not set to enable Playwright, calls would get runtime errors
5. Playwright jars are in an OSGI plugin so they are only loaded if configured (via ENV var)
  
  1. If the ENV variable is not set to enable Playwright, calls would get runtime errors
  2. Note, this is a more complex programming model and might have unintended limitations
6. Playwright jars are not included in the container, but instructions to add them to a derived image are included
  
  1. If the jars are not added to the derived image, calls would get runtime errors
7. Do not include Playwright in Grouper, leave this to external tools
