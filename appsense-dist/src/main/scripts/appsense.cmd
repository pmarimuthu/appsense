@echo off
setlocal
set "APP_HOME=%~dp0"
if defined JAVA_HOME (
    set "JAVA=%JAVA_HOME%\bin\javaw.exe"
) else (
    set "JAVA=javaw.exe"
)
start "" "%JAVA%" -Xmx300m -jar "%APP_HOME%appsense-gui.jar" %*
endlocal
