@echo off
echo Starting CodeAlpha Stock Trading Platform (GUI Mode)...
if not exist bin\com\codealpha\stocktrading\Main.class (
    echo Binaries not found. Compiling first...
    call compile.bat
)
java -cp bin com.codealpha.stocktrading.Main
