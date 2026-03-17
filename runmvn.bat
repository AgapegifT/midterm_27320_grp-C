@echo off
cd /d "%~dp0"
call mvnw.cmd -v > mvn_out.txt 2>&1
