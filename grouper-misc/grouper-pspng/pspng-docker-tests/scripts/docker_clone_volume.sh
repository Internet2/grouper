#!/bin/bash

# Reference: https://github.com/gdiepen/docker-convenience-scripts/blob/master/docker_clone_volume.sh
#Author: Guido Diepen

#Convenience script that can help me to easily create a clone of a given
#data volume. The script is mainly useful if you are using named volumes


#First check if the user provided all needed arguments
if [ "$1" = "" ]
then
        echo "Please provide a source volume name" 1>&2
        exit
fi

if [ "$2" = "" ] 
then
        echo "Please provide a destination volume name" 1>&2
        exit
fi


#Check if the source volume name does exist
docker volume inspect $1 > /dev/null 2>&1
if [ "$?" != "0" ]
then
        echo "The source volume \"$1\" does not exist" 1>&2
        exit
fi

#Now check if the destinatin volume name does not yet exist
docker volume inspect $2 > /dev/null 2>&1

if [ "$?" = "0" ]
then
        echo "The destination volume \"$2\" already exists" 1>&2
        exit
fi



echo "Creating destination volume \"$2\"..." 1>&2
docker volume create --name $2  
echo "Copying data from source volume \"$1\" to destination volume \"$2\"..." 1>&2
docker run --rm \
           -v $1:/from \
           -v $2:/to \
           alpine ash -c "cd /from ; cp -a . /to"

