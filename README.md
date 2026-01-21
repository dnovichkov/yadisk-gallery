# YaDisk Gallery

Android-приложение для просмотра фотографий и видео из каталогов Яндекс.Диска.

## Возможности

- Просмотр фотографий и видео из Яндекс.Диска
- Поддержка публичных папок (без авторизации)
- Авторизация через OAuth 2.0 (Yandex Login SDK)
- Навигация по вложенным папкам
- Полноэкранный просмотр с масштабированием
- Воспроизведение видео через ExoPlayer
- Кеширование для офлайн-доступа
- Поддержка тёмной темы и Dynamic Colors

## Требования

- Android 7.0 (API 24) и выше
- Android Studio Hedgehog или новее
- JDK 17

## Сборка

```bash
# Debug сборка
./gradlew assembleDebug

# Release сборка
./gradlew assembleRelease

# Запуск тестов
./gradlew test

# Проверка стиля кода
./gradlew ktlintCheck
```

## Технологический стек

- **Язык:** Kotlin 1.9+
- **UI:** Jetpack Compose + Material 3
- **Архитектура:** MVVM + Clean Architecture
- **DI:** Hilt
- **Сеть:** Retrofit 2 + OkHttp 4 + Kotlinx Serialization
- **Изображения:** Coil
- **Видео:** ExoPlayer (Media3)
- **Асинхронность:** Kotlin Coroutines + Flow
- **Хранилище:** DataStore + Room
- **Авторизация:** Yandex Login SDK 3.x
- **Тестирование:** JUnit 5, MockK, Turbine, Espresso

## Структура проекта

```
com.dnovichkov.yadiskgallery/
├── data/           # Data Layer
│   ├── api/        # Retrofit interfaces
│   ├── repository/ # Repository implementations
│   ├── cache/      # Room database
│   ├── datastore/  # DataStore, TokenStorage
│   └── model/      # DTO models
├── domain/         # Domain Layer
│   ├── model/      # Domain entities
│   ├── repository/ # Repository interfaces
│   └── usecase/    # Use cases
├── presentation/   # Presentation Layer
│   ├── auth/       # Auth screens
│   ├── gallery/    # Gallery screens
│   ├── viewer/     # Media viewer
│   ├── settings/   # Settings screens
│   ├── navigation/ # Navigation
│   ├── theme/      # Material 3 theme
│   └── components/ # Reusable components
└── di/             # Hilt modules
```

## Документация

- [Спецификация требований](docs/SRS_YaDisk_Gallery.md)
- [Техническая спецификация API](docs/specs.md)
- [План разработки](docs/PROJECT_PLAN.md)

## Лицензия

Copyright 2026 Dmitriy Novichkov
