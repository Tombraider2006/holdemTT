# Инструкция по сборке APK

## Требования
- Android Studio Hedgehog или новее
- JDK 17
- Android SDK (API 24+)

## Сборка через Android Studio

1. Откройте проект в Android Studio
2. Дождитесь синхронизации Gradle
3. Выберите Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK будет создан в `app/build/outputs/apk/debug/app-debug.apk`

## Сборка через командную строку

### Windows:
```bash
gradlew.bat assembleDebug
```

### Linux/Mac:
```bash
./gradlew assembleDebug
```

APK будет создан в `app/build/outputs/apk/debug/app-debug.apk`

## Сборка release версии

Для release версии нужно:
1. Настроить signing config в `app/build.gradle.kts`
2. Выполнить: `gradlew assembleRelease`

## Проверка кода

Перед сборкой проверьте:
- ✅ Нет ошибок компиляции
- ✅ Все импорты корректны
- ✅ Логика игры работает правильно

