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
| 1 | Domain Layer - Модели и интерфейсы | DONE |
| 2 | Domain Layer - Use Cases | DONE |
| 3 | Data Layer - API и сетевой слой | DONE |
| 4 | Data Layer - Локальное хранилище | DONE |
| 5 | Data Layer - Repository Implementations | DONE |
| 6 | Presentation - Navigation и компоненты | DONE |
| 7 | Settings Screen (FR-01) | DONE |
| 8 | Auth Flow (FR-02) | DONE |
| 9 | Gallery Screen (FR-03, FR-04, FR-06) | DONE |
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
| 1.1 | Тест: MediaFile model | DONE |
| 1.2 | Реализация: MediaFile (id, name, path, type, size, dates, previewUrl, md5) | DONE |
| 1.3 | Тест: Folder model | DONE |
| 1.4 | Реализация: Folder (id, name, path, itemsCount) | DONE |
| 1.5 | Тест: DiskItem sealed class | DONE |
| 1.6 | Реализация: DiskItem sealed interface | DONE |
| 1.7 | Тест: UserSettings model | DONE |
| 1.8 | Реализация: UserSettings | DONE |
| 1.9 | Тест: AuthState model | DONE |
| 1.10 | Реализация: AuthState sealed class | DONE |
| 1.11 | Тест: PagedResult model | DONE |
| 1.12 | Реализация: PagedResult | DONE |
| 1.13 | Тест: DomainError sealed class | DONE |
| 1.14 | Реализация: DomainError | DONE |
| 1.15 | Реализация: Repository interfaces | DONE |
| 1.16 | Проверка: все тесты проходят | DONE |

**Ключевые файлы:**
- `domain/model/` - MediaFile, Folder, DiskItem, UserSettings, AuthState, PagedResult, DomainError
- `domain/repository/` - ISettingsRepository, IAuthRepository, IFilesRepository, ICacheRepository

---

## Фаза 2: Domain Layer - Use Cases

**Цель:** Реализовать бизнес-логику в виде Use Cases

**Задачи (TDD):**
| # | Задача | Статус |
|---|--------|--------|
| 2.1-2.2 | GetSettingsUseCase | DONE |
| 2.3-2.4 | SaveSettingsUseCase | DONE |
| 2.5-2.6 | ValidatePublicUrlUseCase | DONE |
| 2.7-2.8 | AuthorizeUseCase | DONE |
| 2.9-2.10 | LogoutUseCase | DONE |
| 2.11-2.12 | GetAuthStateUseCase | DONE |
| 2.13-2.14 | GetFilesUseCase | DONE |
| 2.15-2.16 | GetFolderContentsUseCase | DONE |
| 2.17-2.18 | GetMediaDownloadUrlUseCase | DONE |
| 2.19-2.20 | RefreshFilesUseCase | DONE |
| 2.21-2.22 | ClearCacheUseCase | DONE |
| 2.23-2.24 | GetCacheSizeUseCase | DONE |

---

## Фаза 3: Data Layer - API и сетевой слой

**Цель:** Реализовать Retrofit-интерфейсы для Яндекс.Диска API

**Задачи (TDD):**
| # | Задача | Статус |
|---|--------|--------|
| 3.1-3.2 | ResourceDto с @Serializable | DONE |
| 3.3-3.4 | FilesResponseDto | DONE |
| 3.5-3.6 | DownloadLinkDto | DONE |
| 3.7-3.8 | ErrorDto | DONE |
| 3.9-3.10 | Mappers (Dto -> Domain) | DONE |
| 3.11 | YandexDiskApi interface (Retrofit) | DONE |
| 3.12 | API integration tests (MockWebServer) | SKIPPED |
| 3.13 | NetworkModule (OkHttpClient, Retrofit) | DONE |
| 3.14-3.15 | AuthInterceptor | DONE |
| 3.16-3.17 | RetryInterceptor (exponential backoff) | DONE |
| 3.18 | NetworkMonitor | DONE |

---

## Фаза 4: Data Layer - Локальное хранилище

**Цель:** Реализовать Room для кеширования и DataStore для настроек

**Задачи (TDD):**
| # | Задача | Статус |
|---|--------|--------|
| 4.1-4.2 | MediaFileEntity | DONE |
| 4.3-4.4 | FolderEntity | DONE |
| 4.5-4.6 | CacheMetadataEntity | DONE |
| 4.7-4.8 | MediaDao | DONE |
| 4.9-4.10 | FolderDao + CacheMetadataDao | DONE |
| 4.11-4.12 | AppDatabase + DatabaseModule | DONE |
| 4.13-4.14 | SettingsDataStore | DONE |
| 4.15-4.16 | TokenStorage (EncryptedSharedPreferences) | DONE |
| 4.17 | DataStoreModule | DONE |

**Ключевые файлы:**
- `data/cache/entity/` - MediaFileEntity, FolderEntity, CacheMetadataEntity
- `data/cache/dao/` - MediaDao, FolderDao, CacheMetadataDao
- `data/cache/AppDatabase.kt`
- `data/datastore/` - SettingsDataStore, TokenStorage
- `di/DatabaseModule.kt`, `di/DataStoreModule.kt`

---

## Фаза 5: Data Layer - Repository Implementations

**Цель:** Реализовать все репозитории

**Задачи (TDD):**
| # | Задача | Статус |
|---|--------|--------|
| 5.1-5.2 | SettingsRepositoryImpl | DONE |
| 5.3-5.4 | AuthRepositoryImpl (TokenStorage) | DONE |
| 5.5-5.8 | FilesRepositoryImpl (API + Room + cache strategy) | DONE |
| 5.9-5.10 | CacheRepositoryImpl | DONE |
| 5.11 | RepositoryModule | DONE |

**Ключевые файлы:**
- `data/repository/` - SettingsRepositoryImpl, AuthRepositoryImpl, FilesRepositoryImpl, CacheRepositoryImpl
- `di/RepositoryModule.kt`

---

## Фаза 6: Presentation - Navigation и базовые компоненты

**Цель:** Настроить навигацию и создать переиспользуемые UI компоненты

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 6.1 | Создать Screen sealed class с маршрутами (Settings, Gallery, ImageViewer, VideoPlayer) | DONE |
| 6.2 | Создать NavGraph.kt с Compose Navigation | DONE |
| 6.3 | Создать Color.kt (Material 3 color scheme) | DONE |
| 6.4 | Создать Type.kt (типографика) | DONE |
| 6.5 | Создать Theme.kt (light/dark, dynamic colors Android 12+) | DONE |
| 6.6 | Создать LoadingIndicator composable | DONE |
| 6.7 | Создать ErrorView composable (с кнопкой retry) | DONE |
| 6.8 | Создать EmptyStateView composable | DONE |
| 6.9 | Создать SkeletonLoader composable (shimmer effect) | DONE |
| 6.10 | Создать Breadcrumbs composable | DONE |
| 6.11 | Создать MediaTypeIcon composable (иконки для image/video/folder) | DONE |
| 6.12 | Создать TopAppBar composable с навигацией | DONE |
| 6.13 | Создать BottomSheet base composable | DONE |
| 6.14 | Настроить MainActivity с NavHost | DONE |
| 6.15 | Проверка: сборка и preview компонентов | DONE |

**Ключевые файлы:**
- `presentation/navigation/Screen.kt` - sealed class маршрутов
- `presentation/navigation/NavGraph.kt` - граф навигации
- `presentation/theme/Color.kt`, `Type.kt`, `Theme.kt`
- `presentation/components/` - LoadingIndicator, ErrorView, EmptyStateView, etc.
- `MainActivity.kt` - точка входа с NavHost

---

## Фаза 7: Settings Screen (FR-01)

**Цель:** Реализовать экран настроек

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 7.1 | Тест: SettingsViewModel | DONE |
| 7.2 | Создать SettingsUiState data class | DONE |
| 7.3 | Создать SettingsViewModel с Hilt | DONE |
| 7.4 | Создать PublicUrlTextField composable с валидацией | DONE |
| 7.5 | Создать AuthStatusCard composable (статус подключения) | DONE |
| 7.6 | Создать YandexLoginButton composable | DONE |
| 7.7 | Создать RootFolderSelector composable | DONE |
| 7.8 | Создать ViewModeSelector composable (Grid/List) | DONE |
| 7.9 | Создать SortOrderSelector composable | DONE |
| 7.10 | Создать LogoutButton composable с подтверждением | DONE |
| 7.11 | Создать CacheInfoSection composable (размер кеша, очистка) | DONE |
| 7.12 | Собрать SettingsScreen composable | DONE |
| 7.13 | Интеграция с NavGraph | DONE |
| 7.14 | Проверка: сборка и тесты проходят | DONE |

**Ключевые файлы:**
- `presentation/settings/SettingsScreen.kt`
- `presentation/settings/SettingsViewModel.kt`
- `presentation/settings/SettingsUiState.kt`
- `presentation/settings/components/` - PublicUrlTextField, AuthStatusCard, etc.

---

## Фаза 8: Auth Flow (FR-02)

**Цель:** Реализовать OAuth авторизацию через Yandex Login SDK

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 8.1 | Добавить Yandex Login SDK в зависимости | DONE |
| 8.2 | Настроить AndroidManifest.xml (activity, intent-filter) | DONE |
| 8.3 | Создать YandexAuthManager wrapper class | DONE |
| 8.4 | Тест: AuthViewModel | DONE |
| 8.5 | Создать AuthUiState sealed class | DONE |
| 8.6 | Создать AuthViewModel | DONE |
| 8.7 | Создать AuthScreen composable | DONE |
| 8.8 | Реализовать handleAuthResult в MainActivity | DONE |
| 8.9 | Реализовать автообновление токена в AuthInterceptor | DONE |
| 8.10 | Тест: интеграция с Yandex SDK (manual) | SKIPPED |
| 8.11 | Проверка: полный auth flow | DONE |

**Ключевые файлы:**
- `presentation/auth/YandexAuthManager.kt` - wrapper для SDK
- `presentation/auth/AuthScreen.kt`
- `presentation/auth/AuthViewModel.kt`
- `presentation/auth/AuthUiState.kt`
- `data/api/interceptor/AuthInterceptor.kt` - обновление токена

---

## Фаза 9: Gallery Screen (FR-03, FR-04, FR-06)

**Цель:** Реализовать экран каталога с сеткой/списком и навигацией

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 9.1 | Тест: GalleryViewModel | DONE |
| 9.2 | Создать GalleryUiState data class | DONE |
| 9.3 | Создать GalleryViewModel с пагинацией | DONE |
| 9.4 | Создать MediaGridItem composable (thumbnail + type icon) | DONE |
| 9.5 | Создать FolderGridItem composable | DONE |
| 9.6 | Создать MediaListItem composable (детальный вид) | DONE |
| 9.7 | Создать FolderListItem composable | DONE |
| 9.8 | Создать GalleryGrid composable (LazyVerticalGrid 3 columns) | DONE |
| 9.9 | Создать GalleryList composable (LazyColumn) | DONE |
| 9.10 | Создать BreadcrumbsBar composable | DONE |
| 9.11 | Создать GalleryTopBar composable (view mode toggle, sort) | DONE |
| 9.12 | Реализовать Pull-to-Refresh (Material3 PullToRefreshBox) | DONE |
| 9.13 | Реализовать infinite scroll с пагинацией | DONE |
| 9.14 | Создать SkeletonGrid composable (placeholder при загрузке) | DONE |
| 9.15 | Собрать GalleryScreen composable | DONE |
| 9.16 | Настроить Coil для загрузки thumbnails | DONE |
| 9.17 | Интеграция с NavGraph (навигация в папки) | DONE |
| 9.18 | Проверка: lint и тесты проходят | DONE |

**Ключевые файлы:**
- `presentation/gallery/GalleryScreen.kt`
- `presentation/gallery/GalleryViewModel.kt`
- `presentation/gallery/GalleryUiState.kt`
- `presentation/gallery/components/MediaGridItem.kt`, `FolderGridItem.kt`
- `presentation/gallery/components/GalleryGrid.kt`, `GalleryList.kt`
- `presentation/gallery/components/BreadcrumbsBar.kt`, `GalleryTopBar.kt`

---

## Фаза 10: Image Viewer (FR-05.1-FR-05.7)

**Цель:** Реализовать полноэкранный просмотр изображений

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 10.1 | Тест: ImageViewerViewModel | NOT STARTED |
| 10.2 | Создать ImageViewerUiState data class | NOT STARTED |
| 10.3 | Создать ImageViewerViewModel | NOT STARTED |
| 10.4 | Создать ZoomableImage composable (pinch-to-zoom 1x-5x) | NOT STARTED |
| 10.5 | Реализовать double-tap для 2x zoom | NOT STARTED |
| 10.6 | Создать ImagePager composable (HorizontalPager для свайпов) | NOT STARTED |
| 10.7 | Реализовать immersive mode (скрытие system bars) | NOT STARTED |
| 10.8 | Реализовать загрузку оригинала при зуме >2x | NOT STARTED |
| 10.9 | Создать ImageLoadingIndicator composable | NOT STARTED |
| 10.10 | Создать ExifInfoSheet composable (BottomSheet с EXIF) | NOT STARTED |
| 10.11 | Реализовать чтение EXIF данных | NOT STARTED |
| 10.12 | Создать ImageViewerTopBar composable (share, info buttons) | NOT STARTED |
| 10.13 | Собрать ImageViewerScreen composable | NOT STARTED |
| 10.14 | Интеграция с NavGraph (аргументы: path, index) | NOT STARTED |
| 10.15 | Проверка: gesture тесты | NOT STARTED |

**Ключевые файлы:**
- `presentation/viewer/ImageViewerScreen.kt`
- `presentation/viewer/ImageViewerViewModel.kt`
- `presentation/viewer/ImageViewerUiState.kt`
- `presentation/viewer/components/ZoomableImage.kt`
- `presentation/viewer/components/ImagePager.kt`
- `presentation/viewer/components/ExifInfoSheet.kt`

---

## Фаза 11: Video Player (FR-05.8-FR-05.12)

**Цель:** Реализовать воспроизведение видео через ExoPlayer

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 11.1 | Тест: VideoPlayerViewModel | NOT STARTED |
| 11.2 | Создать VideoPlayerUiState data class | NOT STARTED |
| 11.3 | Создать VideoPlayerViewModel | NOT STARTED |
| 11.4 | Создать ExoPlayerWrapper class (lifecycle-aware) | NOT STARTED |
| 11.5 | Создать VideoPlayer composable (AndroidView с PlayerView) | NOT STARTED |
| 11.6 | Настроить стандартные контролы ExoPlayer | NOT STARTED |
| 11.7 | Реализовать streaming (без полной загрузки) | NOT STARTED |
| 11.8 | Реализовать сохранение позиции воспроизведения | NOT STARTED |
| 11.9 | Создать VideoLoadingOverlay composable | NOT STARTED |
| 11.10 | Создать VideoErrorOverlay composable | NOT STARTED |
| 11.11 | Собрать VideoPlayerScreen composable | NOT STARTED |
| 11.12 | Интеграция с NavGraph | NOT STARTED |
| 11.13 | Проверка: воспроизведение разных форматов | NOT STARTED |

**Ключевые файлы:**
- `presentation/viewer/VideoPlayerScreen.kt`
- `presentation/viewer/VideoPlayerViewModel.kt`
- `presentation/viewer/VideoPlayerUiState.kt`
- `presentation/viewer/components/ExoPlayerWrapper.kt`
- `presentation/viewer/components/VideoPlayer.kt`

---

## Фаза 12: Error Handling и Offline Mode (FR-07, FR-08)

**Цель:** Реализовать обработку ошибок и offline режим

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 12.1 | Создать ConnectivityObserver (NetworkCallback) | NOT STARTED |
| 12.2 | Создать OfflineState sealed class | NOT STARTED |
| 12.3 | Создать OfflineBanner composable | NOT STARTED |
| 12.4 | Интегрировать OfflineBanner в экраны | NOT STARTED |
| 12.5 | Создать ErrorMapper (API errors -> DomainError) | NOT STARTED |
| 12.6 | Создать RetryPolicy с exponential backoff | NOT STARTED |
| 12.7 | Обновить ErrorView с детализацией ошибок | NOT STARTED |
| 12.8 | Реализовать graceful degradation (показ кеша при offline) | NOT STARTED |
| 12.9 | Настроить Coil disk cache | NOT STARTED |
| 12.10 | Реализовать LRU eviction для Room cache | NOT STARTED |
| 12.11 | Тест: offline сценарии | NOT STARTED |
| 12.12 | Проверка: переключение online/offline | NOT STARTED |

**Ключевые файлы:**
- `data/network/ConnectivityObserver.kt`
- `data/api/ErrorMapper.kt`
- `data/api/RetryPolicy.kt`
- `presentation/components/OfflineBanner.kt`
- `presentation/components/ErrorView.kt` (обновление)

---

## Фаза 13: Performance и Accessibility (NFR)

**Цель:** Оптимизировать производительность и добавить доступность

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 13.1 | Профилирование cold start (Android Studio Profiler) | NOT STARTED |
| 13.2 | Оптимизация Application.onCreate (lazy init) | NOT STARTED |
| 13.3 | Добавить App Startup library для зависимостей | NOT STARTED |
| 13.4 | Профилирование scrolling (frame drops) | NOT STARTED |
| 13.5 | Оптимизация Compose recomposition (remember, derivedStateOf) | NOT STARTED |
| 13.6 | Оптимизация LazyGrid (contentType, key) | NOT STARTED |
| 13.7 | Профилирование memory (LeakCanary) | NOT STARTED |
| 13.8 | Оптимизация Coil memory cache | NOT STARTED |
| 13.9 | Добавить contentDescription ко всем интерактивным элементам | NOT STARTED |
| 13.10 | Добавить semantics для TalkBack | NOT STARTED |
| 13.11 | Увеличить touch targets до минимум 48dp | NOT STARTED |
| 13.12 | Тест: TalkBack navigation | NOT STARTED |
| 13.13 | Benchmark тесты (Macrobenchmark) | NOT STARTED |

**Ключевые файлы:**
- `YaDiskGalleryApp.kt` (оптимизация инициализации)
- Все composable (accessibility modifiers)
- `benchmark/` - модуль для Macrobenchmark

---

## Фаза 14: Security и Локализация (NFR)

**Цель:** Безопасность и i18n

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 14.1 | Добавить Certificate Pinning для cloud-api.yandex.net | NOT STARTED |
| 14.2 | Настроить network_security_config.xml | NOT STARTED |
| 14.3 | Проверить ProGuard/R8 rules | NOT STARTED |
| 14.4 | Добавить R8 rules для Retrofit, Room, Kotlinx Serialization | NOT STARTED |
| 14.5 | Создать strings.xml (RU - default) | NOT STARTED |
| 14.6 | Создать strings.xml (EN) | NOT STARTED |
| 14.7 | Экстрагировать все hardcoded строки | NOT STARTED |
| 14.8 | Добавить plurals для счетчиков | NOT STARTED |
| 14.9 | Проверка: release build с R8 | NOT STARTED |
| 14.10 | Проверка: EN локализация | NOT STARTED |

**Ключевые файлы:**
- `res/xml/network_security_config.xml`
- `proguard-rules.pro`
- `res/values/strings.xml` (RU)
- `res/values-en/strings.xml` (EN)

---

## Фаза 15: Integration и E2E тестирование

**Цель:** Добавить E2E тесты для критических сценариев

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 15.1 | Настроить androidTest dependencies (Espresso, Compose Testing) | NOT STARTED |
| 15.2 | Создать TestRunner с Hilt | NOT STARTED |
| 15.3 | Создать mock сервер для API (MockWebServer) | NOT STARTED |
| 15.4 | E2E тест: Public folder flow (ввод URL -> просмотр галереи) | NOT STARTED |
| 15.5 | E2E тест: Auth flow (login -> logout) | NOT STARTED |
| 15.6 | E2E тест: Gallery navigation (папки, breadcrumbs) | NOT STARTED |
| 15.7 | E2E тест: Image viewing (zoom, swipe) | NOT STARTED |
| 15.8 | E2E тест: Video playback (play, pause, seek) | NOT STARTED |
| 15.9 | E2E тест: Offline mode (показ кеша) | NOT STARTED |
| 15.10 | E2E тест: Error handling (retry) | NOT STARTED |
| 15.11 | Настроить CI для instrumented tests | NOT STARTED |
| 15.12 | Проверка: все E2E тесты проходят | NOT STARTED |

**Ключевые файлы:**
- `app/src/androidTest/` - все E2E тесты
- `app/src/androidTest/java/.../di/TestModule.kt`
- `.github/workflows/android.yml` (обновление для instrumented tests)

---

## Фаза 16: Финализация и Release

**Цель:** Подготовить к релизу

**Задачи:**
| # | Задача | Статус |
|---|--------|--------|
| 16.1 | Создать adaptive app icon (ic_launcher) | NOT STARTED |
| 16.2 | Создать ic_launcher_foreground.xml (vector) | NOT STARTED |
| 16.3 | Создать ic_launcher_background.xml | NOT STARTED |
| 16.4 | Настроить Splash Screen API (Android 12+) | NOT STARTED |
| 16.5 | Создать splash screen theme | NOT STARTED |
| 16.6 | Настроить версионирование (SemVer) в build.gradle.kts | NOT STARTED |
| 16.7 | Создать keystore для release signing | NOT STARTED |
| 16.8 | Настроить signingConfigs в build.gradle.kts | NOT STARTED |
| 16.9 | Создать GitHub Actions release workflow | NOT STARTED |
| 16.10 | Обновить README.md (описание, скриншоты, инструкции) | NOT STARTED |
| 16.11 | Создать CHANGELOG.md | NOT STARTED |
| 16.12 | Создать release notes template | NOT STARTED |
| 16.13 | Финальная сборка release APK | NOT STARTED |
| 16.14 | Проверка: установка и запуск release версии | NOT STARTED |

**Ключевые файлы:**
- `res/mipmap-*/ic_launcher*.xml`
- `res/drawable/ic_launcher_foreground.xml`
- `res/values/themes.xml` (splash)
- `build.gradle.kts` (versioning, signing)
- `.github/workflows/release.yml`
- `README.md`, `CHANGELOG.md`

---

## Диаграмма зависимостей фаз

```
Фаза 0 (Setup)
    │
    ├──────────────────┐
    ▼                  ▼
Фаза 1 (Domain)    Фаза 6 (Navigation/Theme)
    │                  │
    ▼                  │
Фаза 2 (Use Cases)     │
    │                  │
    ▼                  │
Фаза 3 (API)           │
    │                  │
    ▼                  │
Фаза 4 (Cache)         │
    │                  │
    ▼                  │
Фаза 5 (Repositories)  │
    │                  │
    └────────┬─────────┘
             │
    ┌────────┼────────┬────────┬────────┐
    ▼        ▼        ▼        ▼        ▼
Фаза 7   Фаза 8   Фаза 9   Фаза 10  Фаза 11
Settings  Auth    Gallery   Image    Video
    │        │        │        │        │
    └────────┴────────┴────────┴────────┘
                      │
                      ▼
              Фаза 12 (Error/Offline)
                      │
             ┌────────┴────────┐
             ▼                 ▼
       Фаза 13            Фаза 14
    (Performance)       (Security/i18n)
             │                 │
             └────────┬────────┘
                      ▼
               Фаза 15 (E2E Tests)
                      │
                      ▼
               Фаза 16 (Release)
```

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
