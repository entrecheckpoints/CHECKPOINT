@ECHO OFF
SETLOCAL
CD /D "%~dp0\.."
ECHO.
ECHO ===============================================
ECHO   CHECKPOINT - GENERAR APK DE DEPURACION
ECHO ===============================================
ECHO.
CALL gradlew.bat --no-daemon :app:assembleDebug --stacktrace
IF ERRORLEVEL 1 (
  ECHO.
  ECHO La compilacion fallo. Copia desde "FAILURE: Build failed" hasta "BUILD FAILED".
  PAUSE
  EXIT /B %ERRORLEVEL%
)
ECHO.
ECHO APK GENERADO CORRECTAMENTE:
ECHO %CD%\app\build\outputs\apk\debug\app-debug.apk
ECHO.
PAUSE
ENDLOCAL
