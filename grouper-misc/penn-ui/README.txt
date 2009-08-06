Grouper UI lite.

This is a low-build dev env.  If you can get a copy of JavaRebel (free for open source
developers), it will help you develop with this UI.  Even if not, not having to build
will help reduce development time.

First copy the build.example.properties to build.properties.  Set the grouper.home
location in build.properties.

Open with eclipse, and it will compile classes to the WEB-INF/classes dir.  

You cant get started until all libs are copied to WEB-INF lib, and grouper conf files 
are copied to WEB-INF/classes.  You can do this with the "local" target.

Note that if your dev env does a clean build, it will delete grouper conf files in 
WEB-INF/classes.  You will need to run a "local" target again.

The grouper API is not included in this CVS project, 
