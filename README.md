# wadachi

English | [日本語](README.ja.md)

> A couples app that shares your plans and memories through the calendars and albums you already use.

> **Status: Concept stage.** The app is only a skeleton that launches. This document is a working design draft.

---

## 1. Why

Between has a calendar, but **it cannot import events from Google Calendar or TimeTree**. Everyday plans live in another calendar, so they have to be re-entered by hand in Between, and people stop using it.

wadachi is **built around importing**. It reads the calendars and albums you already use and lets the two of you see them overlaid.

## 2. Design Principles

| Principle | Meaning |
| --- | --- |
| **Serverless** | No backend of our own. Data lives on each person's device and in each person's Google account |
| **Existing services are the source of truth** | Google Calendar holds the plans; each person's photo library or cloud holds the photos. wadachi only connects them |
| **Pair in person** | Partners link by showing each other their screens and exchanging codes. No sign-up or invitation email |
| **Share only what you choose** | Each person decides what the partner sees: full event details, or just "busy" |

## 3. Scope

### Features to strengthen

1. **Google Calendar integration**
2. **TimeTree integration**
3. **Album integration**

### Low priority

- **Messenger**: It can likely be built serverless over P2P, but other apps already cover it, so it comes later.

### Platforms and distribution

- iPhone (iOS) and Android
- Published by the author on the App Store and Google Play

## 4. How the Integrations Work (Draft)

### Google Calendar

To share without a backend, wadachi uses Google Calendar's own sharing as the sync layer.

- When pairing, wadachi creates **a shared "Us" calendar** in one partner's Google account and grants the other partner write access (Calendar API ACL).
- Each person shares their personal calendars with the partner as **free/busy only (freeBusyReader)** or **full details**, as they choose.
- The app overlays both. New shared plans are written to the "Us" calendar.
- Google handles sync, so wadachi needs no server.

### TimeTree

TimeTree offers few ways to get data out (as researched in September 2026).

| Route | Status |
| --- | --- |
| Public API (TimeTree Connect) | Shut down on December 22, 2023 |
| Bulk export (ICS or similar) | No official feature |
| OS calendar → TimeTree display | Yes (shown automatically under "All Calendars") |
| OS calendar → TimeTree import | Yes ("Copy OS calendar events"; one-time, later changes are not reflected) |
| TimeTree → OS calendar copy | Yes (manual, per event; the destination OS calendar must be enabled in settings) |
| Unofficial tools (TimeTree-Exporter, etc.) | Scrape the web app and log in with email and password. May break without notice; not used in a store app |

So the plan is:

- **wadachi → TimeTree**: Because the "Us" calendar lives in Google, it also appears under TimeTree's "All Calendars."
- **TimeTree → wadachi (import)**: Two routes:
  1. **Via the OS calendar**: Guide users to pick the "Us" calendar (a Google calendar added to the device) as the destination of TimeTree's event copy. wadachi reads it through EventKit / CalendarContract.
  2. **Screenshot import**: Recognize text on-device (Vision / ML Kit) in screenshots of TimeTree's list view and create event candidates, which the user confirms before saving. No server involved.

### Album

Since the 2025 Google Photos Library API changes, apps can no longer access shared albums or albums created by other apps. iCloud Shared Albums cannot be created through PhotoKit either. To support mixed iPhone and Android couples, the options are:

| Option | How it works | Pros | Cons |
| --- | --- | --- | --- |
| A. Shared Google Drive folder | The app creates a folder, shares it with the partner, and stores photos there | No server; works across OSes | Uses Google Drive storage |
| B. P2P transfer + local storage | Send directly when nearby or both online | No cloud involved | Only arrives when both are online at once |
| C. References only | Photos stay in each library; only links between photos and events are shared | Lightweight | Can't see the partner's photos |

The current front-runner is **A, with photos chosen through each OS's picker (Photos Picker / Google Photos Picker API)**.

## 5. Pairing

1. Both partners open wadachi in the same place
2. One screen shows a QR code (or a short code) and the other scans it, then they swap roles
3. Exchanged data: device public key, Google account email (used to share the calendar and folder), pair ID
4. Pairing completes when both screens show the same confirmation number

Because the exchange happens face to face, impersonation is unlikely and no server is needed to verify keys. Either partner can unpair, which also revokes sharing.

## 6. Feature Ideas (Under Discussion)

Nothing here is decided. Priorities are provisional.

### Calendar

| Idea | Description | Priority |
| --- | --- | --- |
| Mutual free-day highlight | Highlight days and time slots when both are free | High |
| Date proposals | Send candidate dates; when the partner accepts, the plan is added to the "Us" calendar | High |
| Automatic anniversaries | Add 100 days, 1 year, birthdays, and similar dates from the day you started dating | Medium |
| Per-event visibility | Choose per event: show details / show as busy / hide | Medium |
| Home screen widget | "N days until we meet," today's plans for both | Medium |
| Travel time awareness | Work out when you can meet from event locations | Low |

### Album

| Idea | Description | Priority |
| --- | --- | --- |
| Auto-link photos to events | Suggest photos matching a date's time and place and attach them to the event | High |
| Memory timeline | Browse events and photos together as a shared history | Medium |
| "On this day" | Notify about past events and photos from the same date | Low |

### Other

| Idea | Description | Priority |
| --- | --- | --- |
| P2P messenger | One-to-one messages without a server | Low |
| Quick signals | One-tap messages such as "heading home now" | Low |

## 7. Open Questions

- **iCloud Calendar support**: Whether to also support people without a Google account through the device calendar (EventKit / CalendarContract)
- **Album storage**: Which of options A–C, or a combination
- **Notifications**: How to push notifications to the partner without a server (whether Google Calendar notifications are enough)

## 8. Development

### Tech Stack

**Kotlin Multiplatform + Compose Multiplatform**, sharing both logic and UI. OS APIs (EventKit / CalendarContract, Vision / ML Kit, photo pickers, widgets) are handled through expect/actual or native code on each OS.

| Directory | Contents |
| --- | --- |
| `shared/` | Shared code (UI and logic): `commonMain` / `androidMain` / `iosMain` |
| `androidApp/` | Android app |
| `iosApp/` | iOS app. The Xcode project is generated from `project.yml` with XcodeGen |

### Requirements

- JDK 21 (the one bundled with Android Studio works)
- Android SDK Platform 37
- Xcode 26 and XcodeGen (iOS only)

### Build

```sh
# Android
./gradlew :androidApp:assembleDebug

# Tests
./gradlew :shared:allTests

# iOS (generate the Xcode project, then open it in Xcode)
cd iosApp && xcodegen generate && open iosApp.xcodeproj
```

Xcode builds the Kotlin framework through Gradle. If `JAVA_HOME` is not set, the JDK bundled with Android Studio is used.

## License

TBD
