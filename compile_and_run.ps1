Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " Building and Launching CodeAlpha Stock Trading Platform " -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan

if (-not (Test-Path -Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling Java source files..." -ForegroundColor Yellow
javac -d bin -sourcepath src src/com/codealpha/stocktrading/Main.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] Compilation completed successfully!" -ForegroundColor Green
    Write-Host "[2/2] Launching GUI application..." -ForegroundColor Yellow
    java -cp bin com.codealpha.stocktrading.Main
} else {
    Write-Host "[ERROR] Compilation failed. Please check javac configuration." -ForegroundColor Red
}
