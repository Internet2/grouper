---
title: "Playwright tracing"
space: Grouper
pageId: 28549803
version: 3
lastUpdated: 2026-07-01T05:41:13.353Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549803/Playwright+tracing
---

If you are having issues with Playwright browsing an app, you can trace the browsing. Note, do not view the trace on the web since any passwords will be sent with the trace. You can instead view the trace with the Playwright tools.

Make sure you have these imports:

```
import java.io.File;
import java.nio.file.Paths;
import com.microsoft.playwright.Tracing;

```

Start recording the trace:

```
      grouperPage.getContext().tracing().start(new Tracing.StartOptions().setSources(true).setScreenshots(true).setSnapshots(true));
```

After using Playwright you can stop the trace and generate a zip file:

```
      gsh_builtin_gshTemplateOutput.addOutputLine("Trace file: " + new File("trace.zip").getAbsolutePath());

      grouperPage.getContext().tracing().stop(new Tracing.StopOptions()
        .setPath(Paths.get("trace.zip")));

```

Get the zip file and run command line (from your workstation)

```
com.microsoft.playwright.CLI show-trace /Users/mchyzer/git/grouper_v2_6/grouper/trace.zip
```

See all the screens, requests, responses, etc
