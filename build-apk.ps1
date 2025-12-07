# Скрипт для сборки APK
# Использование: .\build-apk.ps1

Write-Host "Проверка проекта..." -ForegroundColor Cyan

# Проверка наличия gradle wrapper
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "ОШИБКА: gradlew.bat не найден!" -ForegroundColor Red
    exit 1
}

Write-Host "Сборка debug APK..." -ForegroundColor Green

# Сборка APK
& .\gradlew.bat assembleDebug

if ($LASTEXITCODE -eq 0) {
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        Write-Host "`n✅ APK успешно создан!" -ForegroundColor Green
        Write-Host "Путь: $apkPath" -ForegroundColor Yellow
        $fileInfo = Get-Item $apkPath
        Write-Host "Размер: $([math]::Round($fileInfo.Length / 1MB, 2)) MB" -ForegroundColor Yellow
    } else {
        Write-Host "`n⚠️ Сборка завершена, но APK не найден по пути: $apkPath" -ForegroundColor Yellow
    }
} else {
    Write-Host "`n❌ Ошибка при сборке APK!" -ForegroundColor Red
    exit 1
}

