#! /bin/sh

if [ $1 ]
then
	the_path="/data/data/${1}/files"
	target="/data/local/tmp/smali/${1}/"
	from="${target}smali/"
	echo $the_path
	adb shell <<EOF
	su
	cd $the_path
	mkdir $target
	busybox cp -r smali/ $target
	chmod -R 777 $target
	exit
	exit
EOF
	if [ ! -d "smali" ]
	then
	    mkdir smali
	fi
	mkdir "smali/${1}/"
	cd "smali/${1}"
	adb pull $from
	adb shell <<EOF
	su
	rm -r $target
	exit
	exit
EOF
#	cd ../../
#	java -jar smali-2.0.6.jar "smali/${1}/" -o "dex/classes.dex"
fi