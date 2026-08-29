# Attendance Manager

A complete, offline-first Android app for tracking attendance of ~110 members, built with
Kotlin, Jetpack Compose, and Room. No internet connection is required at any point.

## How to open and build

1. Unzip this project.
2. Open the unzipped `AttendanceManager` folder directly in **Android Studio** (Hedgehog/Iguana
   or newer) via *File > Open*. Let Gradle sync — it will download the Compose, Room, and
   Navigation dependencies automatically the first time (internet is only needed for this
   one-time Gradle sync, not for using the app itself).
3. To build an installable APK:
   - **Android Studio UI:** *Build > Build Bundle(s)/APK(s) > Build APK(s)*. The APK will be
     under `app/build/outputs/apk/debug/app-debug.apk`.
   - **Command line:** run `./gradlew assembleDebug` from the project root (or `gradlew.bat
     assembleDebug` on Windows). Android Studio will generate the Gradle wrapper files
     automatically on first sync if they're missing.
4. Install the APK on the phone (enable "Install from unknown sources" if sideloading, or just
   run it straight from Android Studio with the phone connected via USB debugging).

Minimum Android version supported: **Android 8.0 (API 26)**.

## What's implemented

- **Member management** — add, edit, search by name/ID, unique Member ID, optional mobile
  number, joining/leaving dates, active/inactive toggle. Members are never hard-deleted, so
  historical attendance always stays intact.
- **Daily attendance** — shows only members eligible on the selected date (joined by then,
  not yet left), defaults everyone to Present, one-tap "Mark All Present", per-member
  Present/Absent/Leave selector, saved keyed by Member ID + Date (re-saving a date edits it).
- **Holidays** — set/remove a holiday for a date; holiday dates are excluded from
  attendance entirely (no absences are ever generated for them).
- **Dashboard (Home tab)** — active member count, present/absent/leave counts for the
  selected day, and holiday status.
- **Today's Absentees** — member ID, name, mobile (if present), and total absentee count,
  shown right on the Home tab.
- **WhatsApp sharing** — "Share" button on the Home tab builds a message with the date,
  absentee list, and total count, then opens Android's standard share sheet
  (`ACTION_SEND`) so you choose WhatsApp (or any other app) yourself. Nothing is ever sent
  automatically, and no WhatsApp Business API is used.
- **Reports tab** — monthly Present/Absent/Leave/Holiday totals, overall attendance
  percentage, and a per-member breakdown; tap a member to open their full attendance
  history (Members tab also links to the same history screen).
- **Backup & Restore** (More tab) — exports all members, attendance records, and holidays
  to a single JSON file via Android's file picker (Storage Access Framework); restoring
  reads a previously exported file the same way and replaces the current data with it.
- **Sample data** — the app seeds 10 sample members with a few days of sample attendance
  the first time it runs, purely for demo/testing. Nothing is hard-coded for 110 members —
  add real members through the Members tab as needed.

## Project structure

```
app/src/main/java/com/attendancemanager/app/
├── data/               Room entities, DAOs, database, type converters
├── repository/         Repository layer wrapping the DAOs
├── backup/             JSON export/import (BackupManager)
├── util/                DateUtils, WhatsAppShare, SampleData
├── ui/
│   ├── navigation/      Bottom nav items + NavHost graph
│   ├── theme/            Compose color scheme/theme
│   ├── viewmodel/       One ViewModel per feature area + a shared ViewModelFactory
│   └── screens/          Home, Attendance, Members, Add/Edit Member, Member History,
│                         Reports, More — plus a reusable DatePickerField
├── AttendanceApp.kt      Application class / simple service locator for DB + repos
└── MainActivity.kt        Single-activity entry point hosting the Compose UI
```

## Notes on data rules

- Member ID is the Room primary key (`@PrimaryKey val memberId: String`), matching the
  requirement to use it as the database identifier.
- A member is "eligible" for a date when `joiningDate <= date` and
  (`leavingDate` is null or `leavingDate >= date`) — this is what drives which members show
  up in Daily Attendance and monthly reports for a given time window.
- Marking a member "Inactive" does not delete them or their records; it only affects default
  visibility going forward and is intended to be paired with setting a leaving date.
