@echo off
echo Stopping Java processes...
taskkill //F //IM java.exe 2>nul
if errorlevel 1 (
    echo Could not stop Java process. Please press Ctrl+C in the running terminal and try again.
    pause
    exit /b 1
)
echo Building project...
call mvn clean install -DskipTests
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)
echo.
echo Starting the server...
call mvn spring-boot:run
