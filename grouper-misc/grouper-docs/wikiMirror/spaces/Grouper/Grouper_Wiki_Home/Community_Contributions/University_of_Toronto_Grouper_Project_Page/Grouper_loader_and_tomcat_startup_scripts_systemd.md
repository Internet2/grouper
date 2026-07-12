---
title: "Grouper loader and tomcat startup scripts systemd"
space: Grouper
pageId: 28543985
version: 2
lastUpdated: 2018-05-04T15:28:38.440Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543985/Grouper+loader+and+tomcat+startup+scripts+systemd
---

I've cobbled together a couple of Linux systemd service description files for Tomcat as well as the Grouper loader that I'm happy to contribute / document; this simplifies the automatic startup for both of these. Since we have shibd and Apache as a reverse proxy (Shibboleth service provider) in front of Tomcat, as well as a local PostgreSQL, the machine startup / service restart dependencies are more easily handled this way.

(The one caveat is that Unix scripts used for gsh -- gsh, gsh.sh – won't run this way as they don't have the #!/bin/bash "splat-bang" header line that sets out the interpreter, easily fixed manually but it would be more useful if these files were changed in the source code assuming that it wouldn't cause issues elsewhere)

- Peter St. Onge - University of Toronto

grouper-loader.service

```
# For more info about custom unit files, see 
# http://fedoraproject.org/wiki/Systemd
#
# Look for #How_do_I_customize_a_unit_file.2F_add_a_custom_unit_file.3F
#
# This is a unit file for systemd, to allow the grouper loader daemon to
# be started when the server starts, and shutdown as needed.
#
# this is an early attempt, and will likely fail in interesting and novel manners.
#
# This file should live as /etc/systemd/system/grouper-loader.service
#
# The gsh.sh file in
# /opt/grouper/grouper-2.3.0/grouper.apiBinary-2.3.0/bin/ should have
# the splat-bang line (#!/bin/bash) added to the top of the file.
#
# Remember to run 'systemctl enable grouper-loader.service' once this is
# done so that the loader comes up at boot time.
#
#
[Unit]
Description=Grouper Loader daemon for Grouper 2.3.0
After=syslog.target
After=network.target
After=postgresql-9.6.service
# If the LDAP server is on the same box, you'll probably want to
# reference that here as an 'After' stanza

[Service]

Environment=GROUPER_HOME=/opt/grouper/grouper-2.3.0/grouper.apiBinary-2.3.0
#ExecStart=/opt/grouper/grouper-2.3.0/grouper.apiBinary-2.3.0/bin/gsh.sh -loader
ExecStart=/usr/bin/env ${GROUPER_HOME}/bin/gsh.sh -loader

[Install]
WantedBy=multi-user.target

```

grouper-tomcat.service

```
# For more info about custom unit files, see 
# http://fedoraproject.org/wiki/Systemd
#
# Look for #How_do_I_customize_a_unit_file.2F_add_a_custom_unit_file.3F
#
# This is a unit file for systemd, to allow the tomcat server (that runs
# the Grouper web UI) to be started when the server starts, and shutdown
# as needed.
#
# this is an early attempt, and will likely fail in interesting and novel manners.
#
# This file should live as /etc/systemd/system/grouper-tomcat.service
#
# Remember to run 'systemctl enable grouper-tomcat.service' once this is
# done so that the loader comes up at boot time.
#
#

[Unit]
Description=Apache Tomcat Web Application Container for Grouper 2.3.0
After=network.target
After=shibd.service
After=httpd.service
After=postgresql-9.6.service
Requires=shibd.service
Requires=httpd.service
Requires=postgresql-9.6.service
PartOf=shibd.service
PartOf=httpd.service

[Service]
Type=forking
ExecStart=/opt/grouper/grouper-2.3.0/apache-tomcat-6.0.35/bin/startup.sh
ExecStop=/opt/grouper/grouper-2.3.0/apache-tomcat-6.0.35/bin/shutdown.sh

[Install]
WantedBy=multi-user.target

```
