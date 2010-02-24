This project depends on grouper.  
Checkout Grouper source as project name "grouper_trunk" 
(I suggest in the same folder as grouper-ui_trunk).

There are some variables which must exist in Eclipse to point to Grouper.
In eclipse, right click on the project, properties, java build path, 
source tab, link source button.  Click the variables button, and 
link GROUPER_TRUNK_SOURCE with grouper trunk src/grouper, and 
GROUPER_TRUNK_CONF with grouper trunk conf dir.  Eclipse might complain
that those are on the classpath, thats ok, get out of it.  Then
to kick eclipse to recognize them, right click on the project and close and
open it.  Then you should see two source folders in grouper-ui_trunk which
look like GROUPER_TRUNK_SOURCE and GROUPER_TRUNK_CONF.  These should show
the grouper files in them when opened.  The ant task "dev" should be run once
and anytime you sync and jars change.  This copies jars to webapp/WEB-INF/lib.
Now you should be able to set your server to grouper-ui_trunk/webapp, and run 
the webapp.


 