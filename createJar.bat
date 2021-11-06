mkdir mybin
javac -classpath .;.\lib\json-20190722.jar;.\lib\ptts.sdk.2.6.11.N1.18.jar src\com\pachira\tts\demo\*.java -encoding UTF8 -d .\mybin
echo Main-Class: com.pachira.tts.demo.Main>>MANIFEST.MF
cd mybin
jar xvf ..\lib\json-20190722.jar
jar xvf ..\lib\ptts.sdk.2.6.11.N1.18.jar
rd /s /q META-INF
xcopy ..\src\conf.txt .
jar cvfm ..\ttsReq.jar ..\MANIFEST.MF *
cd ..\
del MANIFEST.MF
rd /s /q mybin