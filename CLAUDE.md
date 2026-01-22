# CLAUDE.md - Правила проекта YaDisk Gallery

## Обзор проекта

Android-приложение для просмотра фотографий и видео из каталогов Яндекс.Диска.

**Репозиторий:** git@github.com:dnovichkov/yadisk-gallery.git
**Package:** com.dnovichkov.yadiskgallery

## Правила разработки

### 1. План проекта
- План хранится в `docs/PROJECT_PLAN.md`
- План актуализируется после завершения каждой задачи
- Отмечайте выполненные задачи в плане

### 2. TDD подход
Порядок разработки для каждой функциональности:
1. **Тест** - написать failing тест
2. **Реализация** - написать минимальную реализацию
3. **Проверка** - убедиться что тесты проходят
4. **Рефакторинг** - улучшить код при необходимости

### 3. Сборка и тесты
После каждой фазы разработки:
- `./gradlew assembleDebug` - должна проходить успешно
- `./gradlew test` - все тесты должны проходить
- GitHub Actions должен быть зелёным

### 3.1 Проверки перед коммитом (ОБЯЗАТЕЛЬНО!)
**Перед каждым коммитом выполнить:**
```bash
./gradlew lintDebug      # Lint проверка (включая NewApi, ModifierParameter)
./gradlew ktlintCheck    # Проверка форматирования
./gradlew test           # Unit тесты
```

Или одной командой:
```bash
./gradlew lintDebug ktlintCheck test
```

⚠️ **Не коммитить если lint не проходит!** GitHub CI будет падать.

### 3.2 Коммиты после каждой фазы (ОБЯЗАТЕЛЬНО!)
После завершения каждой фазы разработки:
1. Выполнить все проверки (`./gradlew lintDebug ktlintCheck test`)
2. Обновить `docs/PROJECT_PLAN.md` - отметить задачи как DONE
3. Создать коммит с описанием фазы
4. **Не пушить** - только локальный коммит

Формат коммита:
```
feat(<область>): <краткое описание> (Phase N)

- Детали реализации
- Созданные файлы
```

### 4. Покрытие тестами
- Минимум 80% покрытия для domain layer (бизнес-логика)
- Unit-тесты для всех Use Cases
- UI-тесты для ключевых сценариев

### 5. Архитектура
Clean Architecture с разделением на слои:
- `domain/` - бизнес-логика, не зависит от Android
- `data/` - работа с данными (API, БД, хранилище)
- `presentation/` - UI (Compose) и ViewModels
- `di/` - Hilt модули

### 6. Стиль кода
- Kotlin coding conventions
- ktlint для форматирования
- KDoc для публичных API

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

## Структура пакетов

```
com.dnovichkov.yadiskgallery/
├── data/
│   ├── api/           # Retrofit interfaces
│   ├── repository/    # Repository implementations
│   ├── cache/         # Room database
│   ├── datastore/     # DataStore, TokenStorage
│   └── model/         # DTO models
├── domain/
│   ├── model/         # Domain entities
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Use cases
├── presentation/
│   ├── auth/          # Auth screens
│   ├── gallery/       # Gallery screens
│   ├── viewer/        # Media viewer
│   ├── settings/      # Settings screens
│   ├── navigation/    # Navigation
│   ├── theme/         # Material 3 theme
│   └── components/    # Reusable components
└── di/                # Hilt modules
```

## Полезные команды

```bash
# Сборка
./gradlew assembleDebug
./gradlew assembleRelease

# Тесты
./gradlew test                    # Unit тесты
./gradlew connectedAndroidTest    # Instrumented тесты

# Lint (ОБЯЗАТЕЛЬНО перед коммитом!)
./gradlew lintDebug               # Проверка lint ошибок

# Форматирование
./gradlew ktlintCheck             # Проверка стиля кода
./gradlew ktlintFormat            # Автоисправление стиля

# Полная проверка перед коммитом
./gradlew lintDebug ktlintCheck test

# Полная проверка (включая все варианты сборки)
./gradlew check
```

## Документация

- `docs/SRS_YaDisk_Gallery.md` - Спецификация требований
- `docs/specs.md` - Техническая спецификация API
- `docs/PROJECT_PLAN.md` - План разработки

## API Яндекс.Диска

Base URL: `https://cloud-api.yandex.net/v1/disk/`

Ключевые эндпоинты:
- `GET /resources` - содержимое папки (авторизованный)
- `GET /public/resources` - публичная папка (без авторизации)
- `GET /resources/download` - ссылка на скачивание
