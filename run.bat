@echo off
if not exist bin\com\vishanth\sms\Main.class (
    echo [INFO] Binaries not found. Compiling first...
    if not exist bin mkdir bin
    javac -encoding UTF-8 -d bin src\com\vishanth\sms\model\*.java src\com\vishanth\sms\util\*.java src\com\vishanth\sms\service\*.java src\com\vishanth\sms\*.java src\com\vishanth\sms\test\*.java
)

java -cp bin com.vishanth.sms.Main
pause
