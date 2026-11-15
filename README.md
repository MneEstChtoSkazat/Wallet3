# Wallet3

«Android-приложение хранение цифровой валюты и поиска пунктов обмена валюты»

## Стек

- Язык: Kotlin
- UI: Jetpack Compose
- Сборка: Gradle (Android Gradle Plugin)


## Требования для сборки

- [Android Studio](https://developer.android.com/studio) (актуальная стабильная версия)
- JDK 17 (встроен в Android Studio)
- Android SDK с платформой <compileSdk из app/build.gradle(.kts)>
- Эмулятор или устройство с Android <minSdk> и выше

## Сборка и запуск

### Через Android Studio

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/MneEstChtoSkazat/Wallet2.git
   ```
2. Откройте Android Studio → **File → Open** → выберите папку `Wallet2`.
3. Дождитесь завершения синхронизации Gradle.
4. Выберите эмулятор или подключённое устройство (с включённой отладкой по USB).
5. Нажмите **Run ▶** (`Shift+F10`).

### Через командную строку

Из корня проекта:

```bash
# Linux / macOS
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

Готовый APK появится в `app/build/outputs/apk/debug/app-debug.apk`.

Установка на подключённое устройство:

```bash
./gradlew installDebug
```

### Релизная сборка

1. Создайте ключ подписи (**Build → Generate Signed Bundle / APK**) или через `keytool`.
2. Соберите подписанный APK или AAB:
   ```bash
   ./gradlew assembleRelease   # APK
   ./gradlew bundleRelease     # AAB для Google Play
   ```
3. Результат: `app/build/outputs/apk/release/` или `app/build/outputs/bundle/release/`.
