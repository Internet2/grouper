##This script will be sourced from gsh.sh. Use it to set custom JVM options
GSH_JVMARGS=""
##Uncomment and change values to override default memory settings
#MEM_START=64m
#MEM_MAX=512m

## Uncomment line below to load YourKit profiler agent
#GSH_JVMARGS="-agentlib:yjpagent $GSH_JVMARGS"

##Uncomment line below for JVM to open debug port. 
#GSH_JVMARGS="$GSH_JVMARGS -Xrunjdwp:transport=dt_socket,server=y,address=4001,suspend=n"

