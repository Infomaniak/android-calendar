# Copilot Coding Agent Onboarding — android-calendar

> **Read `AGENTS.md` and `app/AGENTS.md` first** for architecture and conventions. This file covers build, CI, and validation.

## PR Review Instructions

- Ensure strings are localized via `strings.xml` resources. App strings live under `app/src/main/res/values*/*`; any strings used by `CalendarComponents/*` must go in `CalendarComponents/Resources/src/main/res/values*/*` (and translations in the corresponding `values-<lang>` folders). Do not add strings to other `CalendarComponents/*` modules.
- Make sure fdroid is not forgotten when adding new behavior.
- When adding/removing a runtime dependency, update `LICENSES.md` at the repo root.
- Always make sure that when calendar core version is updated, the KMP submodule is also updated to the same version. And vice versa.
