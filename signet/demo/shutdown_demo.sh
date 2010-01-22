#!/bin/sh

# $Header: /home/hagleyj/i2mi/signet/demo/shutdown_demo.sh,v 1.1 2007-04-22 01:17:36 ddonn Exp $

# Script to shutdown the Signet QuickStart Demo

../tomcat/bin/shutdown.sh
./shutdown_hsqldb.sh
