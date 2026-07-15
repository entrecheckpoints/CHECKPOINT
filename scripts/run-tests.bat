@ECHO OFF
SETLOCAL
CD /D "%~dp0\.."
CALL gradlew.bat --no-daemon :app:testDebugUnitTest --stacktrace
IF ERRORLEVEL 1 PAUSE
ENDLOCAL
