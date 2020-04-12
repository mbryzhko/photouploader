@echo off

if not "%JAVA_HOME%"=="" goto OkJHome
for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
goto checkJCmd

:OkJHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

:checkJCmd
if exist "%JAVACMD%" goto checkPuHome

echo The JAVA_HOME environment variable is not defined correctly >&2
echo This environment variable is needed to run this program >&2
echo NB: JAVA_HOME should point to a JDK not a JRE >&2
goto error

:checkPuHome
if not "%PU_HOME%"=="" goto runPu

echo The PU_HOME environment variable is not defined correctly >&2
echo This environment variable is needed to run this program >&2
goto error

:runPu
set "JAR_FILE=%PU_HOME%\bin\pu.jar"
set "CONFIG_FILE=%PU_HOME%\bin\pu.yaml"

"%JAVACMD%" -jar %JAR_FILE% --spring.config.additional-location=file:%CONFIG_FILE%
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

exit /B %ERROR_CODE%