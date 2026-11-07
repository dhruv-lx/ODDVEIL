@echo off
title The Marble - Launcher
echo ===================================================
echo             THE MARBLE - DESKTOP GUI
echo ===================================================
echo Compiling Java application...
if not exist "bin" mkdir bin
javac -d bin src\TheMarbleGUI.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. Please make sure Java JDK 11+ is installed.
    pause
    exit /b %errorlevel%
)
echo Launching The Marble...
start javaw -cp bin TheMarbleGUI
exit

