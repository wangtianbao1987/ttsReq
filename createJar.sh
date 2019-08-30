#!/bin/sh

mkdir mybin

javac -classpath .:./lib/json-20190722.jar src/com/pachira/tts/demo/*.java -encoding UTF-8 -d ./mybin

echo Main-Class: com.pachira.tts.demo.Main > MANIFEST.MF

cd mybin

jar xvf ../lib/json-20190722.jar

rm -rf META-INF

jar cvfm ../ttsReq.jar ../MANIFEST.MF *

cd ../

rm -rf MANIFEST.MF

rm -rf mybin
