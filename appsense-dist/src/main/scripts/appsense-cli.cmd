@echo off
setlocal
set "APP_HOME=%~dp0"
if defined JAVA_HOME (
    set "JAVA=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA=java.exe"
)
"%JAVA%" -Xmx300m -jar "%APP_HOME%appsense-cli.jar" %*
endlocal
