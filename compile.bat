@echo off
echo ===================================================
echo Compiling Student Management System (Java)...
echo ===================================================

if not exist bin mkdir bin

javac -encoding UTF-8 -d bin src\com\vishanth\sms\model\*.java src\com\vishanth\sms\util\*.java src\com\vishanth\sms\service\*.java src\com\vishanth\sms\*.java src\com\vishanth\sms\test\*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation complete. Classes placed in 'bin/' directory.
) else (
    echo [ERROR] Compilation failed.
)
pause
