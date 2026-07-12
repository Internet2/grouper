---
title: "Installing playwright in Grouper"
space: Grouper
pageId: 28549812
version: 3
lastUpdated: 2026-07-01T05:41:12.411Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549812/Installing+playwright+in+Grouper
---

Set these variables in the container:

```
GROUPER_PLAYWRIGHT_MOVE_JARS=true
GROUPER_PLAYWRIGHT_INSTALL_OS_LIBS=true
```

Note, if you are using container maturity 1+ (derived image) it will work just fine. If you are using maturity level -1 (quickstart) or 0 (image as is), then it needs to start as root to install the libraries. Also it needs internet access to get the libraries.

If you are on Grouper before version v4.17.8, v5.17.3, then you should add this to your derived image Dockerfile

```
  dnf install -y alsa-lib at-spi2-atk at-spi2-core atk bash cairo cups-libs dbus-libs expat flac-libs gdk-pixbuf2 glib2 glibc gtk3 libX11 libXcomposite libXdamage libXext libXfixes libXrandr libXtst libcanberra-gtk3 libdrm libgcc libstdc++ libxcb libxkbcommon libxshmfence libxslt mesa-libgbm nspr nss nss-util pango policycoreutils policycoreutils-python-utils zlib
```

## Notes

For Chromium browser, you should be good to go. For Firefox, there are some errors that are thrown, so it generally should not be used, For webkit, the OS libraries cannot be installed so it should not be used.

When Playright starts this is logged and should be ignored:

```
BEWARE: your OS is not officially supported by Playwright; downloading fallback build for ubuntu20.04-x64.
Downloading Chromium 124.0.6367.29 (playwright build v1112) from https://playwright.azureedge.net/builds/chromium/1112/chromium-linux.zip
|                                                                                |   0% of 155.3 MiB
|■■■■■■■■                                                                        |  10% of 155.3 MiB
|■■■■■■■■■■■■■■■■                                                                |  20% of 155.3 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■                                                        |  30% of 155.3 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                                |  40% of 155.3 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                        |  50% of 155.3 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                |  60% of 155.3 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                        |  70% of 155.3 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                |  80% of 155.3 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■        |  90% of 155.3 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■| 100% of 155.3 MiB
Chromium 124.0.6367.29 (playwright build v1112) downloaded to /home/tomcat/.cache/ms-playwright/chromium-1112
BEWARE: your OS is not officially supported by Playwright; downloading fallback build for ubuntu20.04-x64.
Downloading FFMPEG playwright build v1009 from https://playwright.azureedge.net/builds/ffmpeg/1009/ffmpeg-linux.zip
|                                                                                |   0% of 2.6 MiB
|■■■■■■■■                                                                        |  10% of 2.6 MiB
|■■■■■■■■■■■■■■■■                                                                |  20% of 2.6 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■                                                        |  30% of 2.6 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                                |  40% of 2.6 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                        |  50% of 2.6 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                |  60% of 2.6 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                        |  70% of 2.6 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                |  80% of 2.6 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■        |  90% of 2.6 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■| 100% of 2.6 MiB
FFMPEG playwright build v1009 downloaded to /home/tomcat/.cache/ms-playwright/ffmpeg-1009
BEWARE: your OS is not officially supported by Playwright; downloading fallback build for ubuntu20.04-x64.
Downloading Firefox 124.0 (playwright build v1447) from https://playwright.azureedge.net/builds/firefox/1447/firefox-ubuntu-20.04.zip
|                                                                                |   0% of 85.4 MiB
|■■■■■■■■                                                                        |  10% of 85.4 MiB
|■■■■■■■■■■■■■■■■                                                                |  20% of 85.4 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■                                                        |  30% of 85.4 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                                |  40% of 85.4 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                        |  50% of 85.4 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                |  60% of 85.4 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                        |  70% of 85.4 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                |  80% of 85.4 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■        |  90% of 85.4 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■| 100% of 85.4 MiB
Firefox 124.0 (playwright build v1447) downloaded to /home/tomcat/.cache/ms-playwright/firefox-1447
BEWARE: your OS is not officially supported by Playwright; downloading fallback build for ubuntu20.04-x64.
Downloading Webkit 17.4 (playwright build v1992) from https://playwright.azureedge.net/builds/webkit/1992/webkit-ubuntu-20.04.zip
|                                                                                |   0% of 133.7 MiB
|■■■■■■■■                                                                        |  10% of 133.7 MiB
|■■■■■■■■■■■■■■■■                                                                |  20% of 133.7 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■                                                        |  30% of 133.7 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                                |  40% of 133.7 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                        |  50% of 133.7 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                                |  60% of 133.7 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                        |  70% of 133.7 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■                |  80% of 133.7 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■        |  90% of 133.7 MiB
|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■| 100% of 133.7 MiB
Webkit 17.4 (playwright build v1992) downloaded to /home/tomcat/.cache/ms-playwright/webkit-1992
Playwright Host validation warning: 
╔══════════════════════════════════════════════════════╗
║ Host system is missing dependencies to run browsers. ║
║ Missing libraries:                                   ║
║     libicudata.so.66                                 ║
║     libicui18n.so.66                                 ║
║     libicuuc.so.66                                   ║
║     libjpeg.so.8                                     ║
║     libwebp.so.6                                     ║
║     libflite_cmu_grapheme_lang.so.1                  ║
║     libflite_cmu_grapheme_lex.so.1                   ║
║     libflite_cmu_indic_lang.so.1                     ║
║     libflite_cmu_indic_lex.so.1                      ║
║     libflite_cmu_us_awb.so.1                         ║
║     libflite_cmu_us_rms.so.1                         ║
║     libflite_cmu_us_slt.so.1                         ║
║     libpcre.so.3                                     ║
║     libffi.so.7                                      ║
║     libx264.so                                       ║
╚══════════════════════════════════════════════════════╝
    at validateDependenciesLinux (/opt/tomcat/temp/playwright-java-17743695008213168558/package/lib/server/registry/dependencies.js:216:9)
    at async Registry._validateHostRequirements (/opt/tomcat/temp/playwright-java-17743695008213168558/package/lib/server/registry/index.js:587:43)
    at async Registry._validateHostRequirementsForExecutableIfNeeded (/opt/tomcat/temp/playwright-java-17743695008213168558/package/lib/server/registry/index.js:685:7)
    at async Registry.validateHostRequirementsForExecutablesIfNeeded (/opt/tomcat/temp/playwright-java-17743695008213168558/package/lib/server/registry/index.js:674:43)
    at async t.<anonymous> (/opt/tomcat/temp/playwright-java-17743695008213168558/package/lib/cli/program.js:119:7)

```
