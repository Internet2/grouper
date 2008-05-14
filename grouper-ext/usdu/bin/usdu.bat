@echo off

# Run Grouper's Unresolvable Subject Deletion Utility

java -cp @GROUPER_EXT_BIN@ LauncherBootstrap -executablename usdu -launchfile usdu.launcher.xml usdu "$@"
