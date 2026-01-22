# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-01-22

### Added

#### Core Features
- Browse Yandex.Disk folders and view photos/videos
- Support for public folders (no authentication required)
- OAuth 2.0 authentication via Yandex Login SDK
- Folder navigation with breadcrumbs
- Grid and list view modes
- Sorting by name, date, and size

#### Image Viewer
- Full-screen image viewing
- Pinch-to-zoom (1x to 5x)
- Double-tap for 2x zoom
- Swipe between images
- EXIF information display

#### Video Player
- Video playback with ExoPlayer (Media3)
- Custom playback controls
- Streaming support (no full download required)
- Playback position persistence

#### Offline & Caching
- Thumbnail caching with Coil
- Metadata caching with Room database
- Offline mode with cached content display
- Cache management in settings

#### Error Handling
- Network error handling with retry
- Graceful offline degradation
- User-friendly error messages

#### Accessibility
- TalkBack support
- Content descriptions for all interactive elements
- Minimum 48dp touch targets

#### Localization
- Russian (default)
- English

#### Security
- Certificate pinning for Yandex API
- Encrypted token storage
- ProGuard/R8 optimization

### Technical Details
- Kotlin 2.0 with Jetpack Compose
- Material 3 design with Dynamic Colors (Android 12+)
- Clean Architecture (Domain, Data, Presentation layers)
- Hilt dependency injection
- Unit tests with JUnit 5, MockK, Turbine
- E2E tests with Espresso and Compose Testing
- GitHub Actions CI/CD

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0 | 2026-01-22 | Initial release |

[Unreleased]: https://github.com/dnovichkov/yadisk-gallery/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/dnovichkov/yadisk-gallery/releases/tag/v1.0.0
