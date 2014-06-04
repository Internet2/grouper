rem This script will be run from gsh.bat. Use it to set custom JVM options
set GSH_JVMARGS=

rem Uncomment and change values to override default memory settings
rem set MEM_START=64m
rem set MEM_MAX=512m

rem Uncomment line below to load YourKit profiler agent
rem set GSH_JVMARGS=-agentlib:yjpagent %GSH_JVMARGS%

rem Uncomment line below for JVM to open debug port. 
set GSH_JVMARGS= %GSH_JVMARGS% -Xrunjdwp:transport=dt_socket,server=y,address=4001,suspend=n 
