#! /bin/sh

# 1 for path to smali and 2 for package name
if [ $1 ] && [ $2 ]
then
    if [ ! -d "dex" ]
    then
        mkdir dex
    fi
    java -jar $2 "smali/${1}/" -o "dex/classes.dex"
fi
