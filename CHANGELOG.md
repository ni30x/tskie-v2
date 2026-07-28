# CHANGELOG

All notable changes to the **TSKIE** codebase are documented below.

---

## [1.0.0] - 2026-07-11

### Added
- **Clean Architecture & Package Structure**: Organized files into decoupled `domain`, `data`, `presentation`, `reminder`, `analytics`, and `ui` packages.
- **Domain Layer Entities & Use Cases**:
  - Models: `Task`, `Settings`, `Statistics`
  - Use Cases: `CreateTaskUseCase`, `CompleteTaskUseCase`, `DeleteTaskUseCase`, `RestoreTaskUseCase`
  - Repository interfaces: `TaskRepository`, `SettingsRepository`, `StatisticsRepository`
- **Data Layer Local Room Database**:
  - Initialized Room DB `tskie.db`.
  - Configured Entity Classes: `TaskEntity`, `SettingsEntity`, `StatisticsEntity`.
  - Created corresponding Data Access Objects (DAOs).
  - Implemented Data Mappers to transform raw Entities into rich Domain models.
- **2:00 AM Rollover & Date Utilities**:
  - Formulated custom `DateUtil` that handles the logical 2:00 AM rollover. Past tasks are archived, and tomorrow's planned tasks automatically shift into today.
- **Reminder & Alarm Managers**:
  - Implemented exact task alarms using `AlarmManager` and daily planning notifications using standard receivers.
- **Analytics Privacy Protection**:
  - Created `AnalyticsManager` checking setting triggers and preventing logging of confidential titles or descriptions.
- **Responsive Screens (Compose & M3)**:
  - `TodayScreen`: Interactive list of active items and expandable completed history.
  - `TomorrowScreen`: Planning visual, click-anywhere task creator, and full validation.
  - `CalendarScreen`: GitHub-style completion heatmap row for the last 14 days and detailed metrics grid.
  - `SettingsScreen`: Interactive switches and confirmation dialogs for storage actions.
- **Navigation Capsule**:
  - Implemented custom bottom capsule navigation bar for smooth screen shifting.
- **Testing Architecture**:
  - Unit tests verifying use cases.
  - Robolectric/Roborazzi screenshot visual regression test.

### Fixed
- Fixed an incorrect Compose import of `viewModel()` inside `MainActivity.kt`.
- Cleaned up pre-existing legacy classes (`MainContent`, legacy `TaskDao`, `TaskRepository`, and task entities) to avoid package redefinitions.
