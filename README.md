# YaDisk Gallery

[![Android CI](https://github.com/dnovichkov/yadisk-gallery/actions/workflows/android.yml/badge.svg)](https://github.com/dnovichkov/yadisk-gallery/actions/workflows/android.yml)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-Proprietary-blue.svg)](LICENSE)

Android-приложение для просмотра фотографий и видео из каталогов Яндекс.Диска.

## Возможности

- Просмотр фотографий и видео из Яндекс.Диска
- Поддержка публичных папок (без авторизации)
- Авторизация через OAuth 2.0 (Yandex Login SDK)
- Навигация по вложенным папкам с хлебными крошками
- Полноэкранный просмотр с масштабированием (pinch-to-zoom, double-tap)
- Воспроизведение видео через ExoPlayer
- Переключение между сеткой и списком
- Сортировка по имени, дате, размеру
- Кеширование для офлайн-доступа
- Поддержка тёмной темы и Dynamic Colors (Android 12+)
- Локализация: русский и английский языки
- Доступность: поддержка TalkBack

## Скриншоты

*Coming soon*

## Требования

- Android 7.0 (API 24) и выше
- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17

## Установка

### Из релиза

1. Скачайте APK из [Releases](https://github.com/dnovichkov/yadisk-gallery/releases)
2. Установите на устройство

### Сборка из исходников

```bash
# Клонирование репозитория
git clone https://github.com/dnovichkov/yadisk-gallery.git
cd yadisk-gallery

# Debug сборка
./gradlew assembleDebug

# Release сборка (требуется настройка signing)
./gradlew assembleRelease

# Запуск тестов
./gradlew test

# Проверка стиля кода
./gradlew ktlintCheck

# Полная проверка перед коммитом
./gradlew lintDebug ktlintCheck test
```

## Настройка

### Yandex OAuth

Для работы авторизации необходимо:

1. Зарегистрировать приложение на [Yandex OAuth](https://oauth.yandex.ru/)
2. Получить Client ID
3. Заменить `your_client_id_here` в `app/build.gradle.kts`:

```kotlin
manifestPlaceholders["YANDEX_CLIENT_ID"] = "your_actual_client_id"
```

## Технологический стек

| Компонент | Технология |
|-----------|------------|
| **Язык** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material 3 |
| **Архитектура** | MVVM + Clean Architecture |
| **DI** | Hilt |
| **Сеть** | Retrofit 2 + OkHttp 4 + Kotlinx Serialization |
| **Изображения** | Coil |
| **Видео** | ExoPlayer (Media3) |
| **Асинхронность** | Kotlin Coroutines + Flow |
| **Хранилище** | DataStore + Room |
| **Авторизация** | Yandex Login SDK 3.x |
| **Тестирование** | JUnit 5, MockK, Turbine, Espresso |
| **CI/CD** | GitHub Actions |

## Архитектура

```
+-------------------------------------------------------------------+
|                      Presentation Layer                            |
|  +----------+  +----------+  +----------+  +------------------+   |
|  | Settings |  |  Auth    |  | Gallery  |  |   MediaViewer    |   |
|  |  Screen  |  |  Screen  |  |  Screen  |  | (Photo + Video)  |   |
|  +----+-----+  +----+-----+  +----+-----+  +--------+---------+   |
|       |             |             |                 |              |
|  +----+-----+  +----+-----+  +----+-----+  +--------+---------+   |
|  | ViewModel |  | ViewModel|  | ViewModel|  |    ViewModel     |  |
|  +----------+  +----------+  +----------+  +------------------+   |
+-------------------------------------------------------------------+
                              |
+-----------------------------+-------------------------------------+
|                        Domain Layer                                |
|  Use Cases | Repository Interfaces | Domain Models                |
+-------------------------------------------------------------------+
                              |
+-----------------------------+-------------------------------------+
|                         Data Layer                                 |
|  API (Retrofit) | Cache (Room) | DataStore | Repository Impl      |
+-------------------------------------------------------------------+
```

## Структура проекта

```
com.dnovichkov.yadiskgallery/
├── data/           # Data Layer
│   ├── api/        # Retrofit interfaces, DTOs
│   ├── repository/ # Repository implementations
│   ├── cache/      # Room database, entities, DAOs
│   ├── datastore/  # DataStore, TokenStorage
│   └── network/    # Network monitoring
├── domain/         # Domain Layer
│   ├── model/      # Domain entities
│   ├── repository/ # Repository interfaces
│   └── usecase/    # Use cases
├── presentation/   # Presentation Layer
│   ├── auth/       # Auth flow
│   ├── gallery/    # Gallery screens
│   ├── viewer/     # Image/Video viewer
│   ├── settings/   # Settings screen
│   ├── navigation/ # Navigation
│   ├── theme/      # Material 3 theme
│   └── components/ # Reusable components
└── di/             # Hilt modules
```

## Документация

- [Спецификация требований](docs/SRS_YaDisk_Gallery.md)
- [Техническая спецификация API](docs/specs.md)
- [План разработки](docs/PROJECT_PLAN.md)
- [Changelog](CHANGELOG.md)

## Участие в разработке

1. Fork репозитория
2. Создайте feature branch (`git checkout -b feature/amazing-feature`)
3. Commit изменения (`git commit -m 'Add amazing feature'`)
4. Push в branch (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

### Правила

- Следуйте [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Запускайте `./gradlew lintDebug ktlintCheck test` перед коммитом
- Пишите тесты для новой функциональности

## Лицензия

Copyright 2024-2026 Dmitriy Novichkov. All rights reserved.
