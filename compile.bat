@echo off
echo =========================================================
echo  Compiling CodeAlpha Stock Trading Platform...
echo =========================================================

if not exist bin mkdir bin

javac -d bin -sourcepath src src/com/codealpha/stocktrading/Main.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation finished without errors.
    echo To start the GUI application, run: run.bat
    echo To start the CLI application, run: run-cli.bat
) else (
    echo [ERROR] Compilation failed. Please verify Java JDK installation.
)
pause
