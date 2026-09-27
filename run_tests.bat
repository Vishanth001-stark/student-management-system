@echo off
echo ===================================================
echo Running Student Management System Automated Tests...
echo ===================================================

if not exist bin\com\vishanth\sms\test\StudentSystemTest.class (
    echo [INFO] Compiling classes...
    if not exist bin mkdir bin
    javac -encoding UTF-8 -d bin src\com\vishanth\sms\model\*.java src\com\vishanth\sms\util\*.java src\com\vishanth\sms\service\*.java src\com\vishanth\sms\*.java src\com\vishanth\sms\test\*.java
)

java -cp bin com.vishanth.sms.test.StudentSystemTest
pause
