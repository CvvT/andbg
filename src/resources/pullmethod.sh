#! /bin/sh

#! /bin/sh

# $1 for package name, and $2 for class name, $3 for method name and $4 for method's descriptor
if [ $1 ] && [ $2 ] && [ $3 ] && [ $4 ]
then
    classpath="${2//\.//}"
    echo $classpath
    the_path="/data/data/${1}/files/smali/${3}.dat"
    target="/data/local/tmp/smali/${1}/${3}.dat"
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
    # on Mac os, on linux we use sed -i '/\.method.*dowhile(II)I/, /\.end method/d' MainActivity.smali
    sed -i "" '/\.method.*${3}${4}/, /\.end method/d' "${classpath}.smali"
    echo '' >> "${classpath}.smali"
    cat part.dat >> "${classpath}.smali"
    rm part.dat
fi
