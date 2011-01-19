#!/bin/bash

# go to the right dir
cd /opt/grouper/2.0.0a

# get the latest build, assumes that you built it on the remote server
rm /opt/grouper/2.0.0a/grouper.apiBinary-2.0.0.tar.gz
rm /opt/grouper/2.0.0a/grouper.ws-2.0.0.tar.gz
rm /opt/grouper/2.0.0a/grouper.ui-2.0.0.tar.gz
/usr/bin/scp -B i2mibuild:/home/mchyzer/tmp/grouperAll/build_mchyzer/grouper.apiBinary-2.0.0.tar.gz /opt/grouper/2.0.0a
/usr/bin/scp -B i2mibuild:/home/mchyzer/tmp/grouperAll/build_mchyzer/grouper.ws-2.0.0.tar.gz /opt/grouper/2.0.0a
/usr/bin/scp -B i2mibuild:/home/mchyzer/tmp/grouperAll/build_mchyzer/grouper.ui-2.0.0.tar.gz /opt/grouper/2.0.0a

# remove the old code
rm -rf /opt/grouper/2.0.0a/grouper.apiBinary-2.0.0
rm -rf /opt/grouper/2.0.0a/grouper.ui-2.0.0
rm -rf /opt/grouper/2.0.0a/grouper.ws-2.0.0

# unzip
tar xzf /opt/grouper/2.0.0a/grouper.apiBinary-2.0.0.tar.gz -C /opt/grouper/2.0.0a
tar xzf /opt/grouper/2.0.0a/grouper.ui-2.0.0.tar.gz -C /opt/grouper/2.0.0a
tar xzf /opt/grouper/2.0.0a/grouper.ws-2.0.0.tar.gz -C /opt/grouper/2.0.0a

# customizations against source
cp -Rv /opt/grouper/2.0.0a/filesGrouperUi/* /opt/grouper/2.0.0a/grouper.ui-2.0.0/
cp -Rv /opt/grouper/2.0.0a/filesGrouperWs/* /opt/grouper/2.0.0a/grouper.ws-2.0.0/
cp -Rv /opt/grouper/2.0.0a/filesGrouper/* /opt/grouper/2.0.0a/grouper.apiBinary-2.0.0/

# delete webapps
/sbin/service tomcat_j stop
/sbin/service tomcat_k stop

rm -rf /opt/tomcats/tomcat_j/webapps/*
rm -rf /opt/tomcats/tomcat_k/webapps/*
rm -rf /opt/tomcats/tomcat_j/logs/*
rm -rf /opt/tomcats/tomcat_k/logs/*

# build the UI and WS
cd /opt/grouper/2.0.0a/grouper.ui-2.0.0
/opt/ant/bin/ant default

cd /opt/grouper/2.0.0a/grouper.ws-2.0.0/grouper-ws
/opt/ant/bin/ant dist

# customize the deployment (logging)
cp -Rv /opt/grouper/2.0.0a/filesGrouperUiTomcat/* /opt/tomcats/tomcat_j/webapps/grouper_v2_0_0a/
cp -Rv /opt/grouper/2.0.0a/filesGrouperWsTomcat/* /opt/tomcats/tomcat_k/webapps/grouper-ws_v2_0_0a/

/sbin/service tomcat_j start
/sbin/service tomcat_k start

cd /opt/grouper/2.0.0a
