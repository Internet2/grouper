#!/bin/bash

# go to the right dir
cd /opt/grouper/2.0.0

# get the latest build, assumes that you built it on the remote server
rm /opt/grouper/2.0.0/grouper.apiBinary-2.0.0.tar.gz
rm /opt/grouper/2.0.0/grouper.ws-2.0.0.tar.gz
rm /opt/grouper/2.0.0/grouper.ui-2.0.0.tar.gz
/usr/bin/scp -B i2mibuild:/home/mchyzer/tmp/grouperAll/build_mchyzer/grouper.apiBinary-2.0.0.tar.gz /opt/grouper/2.0.0
/usr/bin/scp -B i2mibuild:/home/mchyzer/tmp/grouperAll/build_mchyzer/grouper.ws-2.0.0.tar.gz /opt/grouper/2.0.0
/usr/bin/scp -B i2mibuild:/home/mchyzer/tmp/grouperAll/build_mchyzer/grouper.ui-2.0.0.tar.gz /opt/grouper/2.0.0

# remove the old code
rm -rf /opt/grouper/2.0.0/grouper.apiBinary-2.0.0
rm -rf /opt/grouper/2.0.0/grouper.ui-2.0.0
rm -rf /opt/grouper/2.0.0/grouper.ws-2.0.0

# unzip
tar xzf /opt/grouper/2.0.0/grouper.apiBinary-2.0.0.tar.gz -C /opt/grouper/2.0.0
tar xzf /opt/grouper/2.0.0/grouper.ui-2.0.0.tar.gz -C /opt/grouper/2.0.0
tar xzf /opt/grouper/2.0.0/grouper.ws-2.0.0.tar.gz -C /opt/grouper/2.0.0

# customizations against source
cp -Rv /opt/grouper/2.0.0/filesGrouperUi/* /opt/grouper/2.0.0/grouper.ui-2.0.0/
cp -Rv /opt/grouper/2.0.0/filesGrouperWs/* /opt/grouper/2.0.0/grouper.ws-2.0.0/
cp -Rv /opt/grouper/2.0.0/filesGrouper/* /opt/grouper/2.0.0/grouper.apiBinary-2.0.0/

# delete webapps
/sbin/service tomcat_e stop
/sbin/service tomcat_g stop

rm -rf /opt/tomcats/tomcat_e/webapps/*
rm -rf /opt/tomcats/tomcat_g/webapps/*
rm -rf /opt/tomcats/tomcat_e/logs/*
rm -rf /opt/tomcats/tomcat_g/logs/*

# build the UI and WS
cd /opt/grouper/2.0.0/grouper.ui-2.0.0
/opt/ant/bin/ant default

cd /opt/grouper/2.0.0/grouper.ws-2.0.0/grouper-ws
/opt/ant/bin/ant dist

# customize the deployment (logging)
cp -Rv /opt/grouper/2.0.0/filesGrouperUiTomcat/* /opt/tomcats/tomcat_e/webapps/grouper_v2_0_0/
cp -Rv /opt/grouper/2.0.0/filesGrouperWsTomcat/* /opt/tomcats/tomcat_g/webapps/grouper-ws_v2_0_0/

/sbin/service tomcat_e start
/sbin/service tomcat_g start

cd /opt/grouper/2.0.0
