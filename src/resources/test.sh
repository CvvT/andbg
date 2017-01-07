#! /bin/sh

if [ $1 ] && [ $2 ]
then
    classpath="${2//\.//}"
    echo $classpath
    the_path="/data/data/${1}/files/smali/${classpath}.smali"
    target="/data/local/tmp/smali/${1}/out.smali"
    dir="/data/local/tmp/smali/${1}"
    adb shell <<EOF
    su
    mkdir $dir
    busybox cp -f $the_path $target
    chmod 777 $target
    exit
    exit
EOF
    if [ ! -d "smali" ]
    then
        mkdir smali
    fi
    if [ ! -d "$1" ]
    then
        mkdir "smali/${1}/"
    fi
    cd "smali/${1}"
    adb pull $target
    adb shell <<EOF
    su
    rm $target
    exit
    exit
EOF
    mv "out.smali" "${classpath}.smali"
    rm "out.smali"
fi


