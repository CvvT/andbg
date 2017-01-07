#! /bin/sh

if [ $1 ]
then
	adb shell <<EOF
	su
	cd data/data/${1}/files
	rm -r smali/
	exit
	exit
EOF
fi
