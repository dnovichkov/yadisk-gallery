# План разработки Android-приложения "Просмотр фотогалереи Яндекс.Диска"

## Обзор проекта

**Репозиторий:** git@github.com:dnovichkov/yadisk-gallery.git
**Документация:** `docs/`
**Ключевые документы:** SRS_YaDisk_Gallery.md, specs.md

## Правила проекта

1. **План проекта** хранится в `docs/PROJECT_PLAN.md` и актуализируется после каждой задачи
2. **TDD подход:** тесты -> реализация -> проверка тестов
3. **После каждой фазы:** сборка проекта и выполнение тестов должны проходить успешно
4. **GitHub Actions:** автоматическая сборка и тестирование при каждом push/PR
5. **Покрытие тестами:** минимум 80% для бизнес-логики (domain layer)

---

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

---

## Прогресс по фазам

| Фаза | Название | Статус |
|------|----------|--------|
| 0 | Инициализация проекта и CI/CD | DONE |
| 1 | Domain Layer - Модели и интерфейсы | NOT STARTED |
| 2 | Domain Layer - Use Cases | NOT STARTED |
| 3 | Data Layer - API и сетевой слой | NOT STARTED |
| 4 | Data Layer - Локальное хранилище | NOT STARTED |
| 5 | Data Layer - Repository Implementations | NOT STARTED |
| 6 | Presentation - Navigation и компоненты | NOT STARTED |
| 7 | Settings Screen (FR-01) | NOT STARTED |
| 8 | Auth Flow (FR-02) | NOT STARTED |
| 9 | Gallery Screen (FR-03, FR-04, FR-06) | NOT STARTED |
| 10 | Image Viewer (FR-05.1-FR-05.7) | NOT STARTED |
| 11 | Video Player (FR-05.8-FR-05.12) | NOT STARTED |
| 12 | Error Handling и Offline Mode | NOT STARTED |
| 13 | Performance и Accessibility | NOT STARTED |
| 14 | Security и Локализация | NOT STARTED |
| 15 | Integration и E2E тестирование | NOT STARTED |
| 16 | Финализация и Release | NOT STARTED |

---

## Фаза 0: Инициализация проекта и CI/CD

**Цель:** Создать базовую структуру Android-проекта с настроенным CI/CD

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 0.1 | Создать Android проект (Empty Compose Activity, package `com.dnovichkov.yadiskgallery`, minSdk 24, targetSdk 35) | DONE |
| 0.2 | Настроить Gradle (libs.versions.toml) со всеми зависимостями | DONE |
| 0.3 | Настроить build.gradle.kts (app) - plugins, android config, dependencies | DONE |
| 0.4 | Создать структуру пакетов (data/, domain/, presentation/, di/) | DONE |
| 0.5 | Настроить Hilt Application (`@HiltAndroidApp`) | DONE |
| 0.6 | Создать базовые тестовые конфигурации (JUnit 5) | DONE |
| 0.7 | Написать smoke-тест | DONE |
| 0.8 | Создать GitHub Actions workflow (`.github/workflows/android.yml`) | DONE |
| 0.9 | Настроить ktlint | DONE |
| 0.10 | Добавить .gitignore, README, CLAUDE.md с правилами проекта | DONE |

**Критерии завершения:**
- [x] `./gradlew assembleDebug` - успешно
- [x] `./gradlew test` - успешно
- [ ] GitHub Actions build + test - успешно (нужен push в репозиторий)

---

## Фаза 1: Domain Layer - Модели и интерфейсы

**Цель:** Определить все доменные сущности и интерфейсы репозиториев

**Задачи (TDD):**
| # | Задача | Статус |
|---|--------|--------|
| 1.1 | Тест: MediaFile model | NOT STARTED |
| 1.2 | Реализация: MediaFile (id, name, path, type, size, dates, previewUrl, md5) | NOT STARTED |
| 1.3 | Тест: Folder model | NOT STARTED |
| 1.4 | Реализация: Folder (id, name, path, itemsCount) | NOT STARTED |
| 1.5 | Тест: DiskItem sealed class | NOT STARTED |
| 1.6 | Реализация: DiskItem sealed interface | NOT STARTED |
| 1.7 | Тест: UserSettings model | NOT STARTED |
| 1.8 | Реализация: UserSettings | NOT STARTED |
| 1.9 | Тест: AuthState model | NOT STARTED |
| 1.10 | Реализация: AuthState sealed class | NOT STARTED |
| 1.11 | Тест: PagedResult model | NOT STARTED |
| 1.12 | Реализация: PagedResult | NOT STARTED |
| 1.13 | Тест: DomainError sealed class | NOT STARTED |
| 1.14 | Реализация: DomainError | NOT STARTED |
| 1.15 | Реализация: Repository interfaces | NOT STARTED |
| 1.16 | Проверка: все тесты проходят | NOT STARTED |

**Ключевые файлы:**
- `domain/model/` - MediaFile, Folder, DiskItem, UserSettings, AuthState, PagedResult, DomainError
- `domain/repository/` - ISettingsRepository, IAuthRepository, IFilesRepository, ICacheRepository

---

## Фаза 2: Domain Layer - Use Cases

**Цель:** Реализовать бизнес-логику в виде Use Cases

**Задачи (TDD):**
| # | Задача | Статус |
|---|--------|--------|
| 2.1-2.2 | GetSettingsUseCase | NOT STARTED |
| 2.3-2.4 | SaveSettingsUseCase | NOT STARTED |
| 2.5-2.6 | ValidatePublicUrlUseCase | NOT STARTED |
| 2.7-2.8 | AuthorizeUseCase | NOT STARTED |
| 2.9-2.10 | LogoutUseCase | NOT STARTED |
| 2.11-2.12 | GetAuthStateUseCase | NOT STARTED |
| 2.13-2.14 | GetFilesUseCase | NOT STARTED |
| 2.15-2.16 | GetFolderContentsUseCase | NOT STARTED |
| 2.17-2.18 | GetMediaDownloadUrlUseCase | NOT STARTED |
| 2.19-2.20 | RefreshFilesUseCase | NOT STARTED |
| 2.21-2.22 | ClearCacheUseCase | NOT STARTED |
| 2.23-2.24 | GetCacheSizeUseCase | NOT STARTED |

---

## Фаза 3: Data Layer - API и сетевой слой

**Цель:** Реализовать Retrofit-интерфейсы для Яндекс.Диска API

**Задачи (TDD):**
| # | Задача | Статус |
|---|--------|--------|
| 3.1-3.2 | ResourceDto с @Serializable | NOT STARTED |
| 3.3-3.4 | FilesResponseDto | NOT STARTED |
| 3.5-3.6 | DownloadLinkDto | NOT STARTED |
| 3.7-3.8 | ErrorDto | NOT STARTED |
| 3.9-3.10 | Mappers (Dto -> Domain) | NOT STARTED |
| 3.11 | YandexDiskApi interface (Retrofit) | NOT STARTED |
| 3.12 | API integration tests (MockWebServer) | NOT STARTED |
| 3.13 | NetworkModule (OkHttpClient, Retrofit) | NOT STARTED |
| 3.14-3.15 | AuthInterceptor | NOT STARTED |
| 3.16-3.17 | RetryInterceptor (exponential backoff) | NOT STARTED |
| 3.18 | NetworkMonitor | NOT STARTED |

---

## Фазы 4-16

_(Детальная разбивка будет добавлена при переходе к соответствующим фазам)_

---

## GitHub Actions Workflow

```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run ktlint
        run: ./gradlew ktlintCheck

      - name: Run unit tests
        run: ./gradlew test

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: app/build/reports/tests/

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/
```

---

## Технологический стек

| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin 1.9+ |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM + Clean Architecture |
| DI | Hilt |
| Networking | Retrofit 2 + OkHttp 4 + Kotlinx Serialization |
| Images | Coil |
| Video | ExoPlayer (Media3) |
| Async | Kotlin Coroutines + Flow |
| Storage | DataStore + Room |
| Auth | Yandex Login SDK 3.x |
| Testing | JUnit 5, MockK, Turbine, Espresso |
