# AGENTS.md — working agreement for Hermex

This checkout contains Hermex's native SwiftUI iPhone app (Xcode target/scheme
`HermesMobile`, App Store name `Hermex`) and a native Kotlin/Jetpack Compose Android
port under `android/`. The Android port is maintained in this repository fork and
talks to the same self-hosted `hermes-webui` server. `PROJECT_SPEC.md` is the shared
product/API source of truth; Android-specific setup and build guidance lives in
`android/README.md` and `.github/workflows/android-ci.yml`. If a request conflicts
with the shared API contract, stop and ask.
Read by every agent (Codex, Claude Code, …); keep it tool-agnostic.

## Session start & wrap-up
- Read `CURRENT.md` first if it exists — it holds the latest resumable state. It is
  local-only (gitignored), never committed; a fresh clone won't have one.
- Read only the `PROJECT_SPEC.md` sections named in CURRENT.md's **Spec Read** field;
  never the whole ~850-line spec unless told to.
- Active work lives in GitHub Issues. Implement only the issue the human selects, one
  labeled `ready-for-agent`, or one named in CURRENT.md — not every open issue.
- On "wrap up": verify repo/build/test state, overwrite `CURRENT.md` with the new
  state (it stays uncommitted), then commit the code.
  History lives in `git log` and merged PRs; there is no append-only log.

## How work flows
- One issue → one short `issue/<n>-slug` branch → one PR (branches with no issue use
  `chore/` or `fix/`). Issue/triage/domain conventions live in `docs/agents/`.
- `master` is the protected release-candidate branch (the source for internal
  TestFlight builds): keep it buildable, never do feature work on it.
- Pushing a branch, opening/updating a PR, or merging needs explicit human approval.
  Triage bot/review comments before accepting them.

## Hard rules
1. **Never invent API endpoints or JSON shapes.** Verify in this precedence order:
   (a) `curl` your own running server — final arbiter; (b) the official API docs at
   https://get-hermes.ai/api-docs/ — best for endpoint intent, auth contract, SSE
   event vocabulary, and conventions (no version pin; tracks the latest release);
   (c) the pinned upstream copy at `.codex-tmp/hermes-webui/api/routes.py` — ground
   truth for exact JSON shapes, but may lag the release the docs describe (clone it
   if missing: `git clone https://github.com/nesquena/hermes-webui .codex-tmp/hermes-webui`).
   That upstream copy is read-only — never modify it (refreshing via `git pull` is fine).
2. **No new third-party dependencies** beyond the approved project dependency lists
   without approval. For Android, versions are managed in
   `android/gradle/libs.versions.toml`.
3. **Tolerant decoding:** Swift `Codable` models use optionals for fields upstream
   might add or rename. Android JSON decoding must preserve the existing
   `ignoreUnknownKeys` behavior in `HermesJson`; never crash on unknown fields.
4. **No destructive commands** (`rm -rf`, `git push --force`, anything touching
   `~/Library/LaunchAgents/` or restarting Mac services). Suggest them; let the human run them.
5. **Don't commit broken builds.** If a build or test fails, fix it before writing more code.

## Tooling
- The maintainer works in Agentic Development Environments (Codex, Claude Code), not the Xcode UI — prefer terminal validation;
  ask to open Xcode only when the terminal can't answer.
- Use **XcodeBuildMCP** for simulator build/test/run/log; fall back to raw
  `xcodebuild`/`xcrun simctl` for release/archive or low-level diagnosis. Defaults live
  in `.xcodebuildmcp/config.yaml` (scheme `HermesMobile`, sim **iPhone 17**); if that
  sim is missing, pick a nearby iPhone and say which.
- **Simulator installs must be signed.** Never install a `CODE_SIGNING_ALLOWED=NO`
  build on the simulator for manual testing — that flag is for compile-only checks
  (see `TESTFLIGHT.md`) and strips entitlements, so Keychain writes fail with
  `errSecMissingEntitlement` and login breaks. Put the app on the sim via XcodeBuildMCP
  `build_run_sim` or a plain signed Debug build (no signing-disabling flags), then install/launch.
- Android builds use the project Gradle Wrapper from `android/`, Java 17, and Android
  SDK platform 36; do not install a global Gradle. Follow `android/README.md` and run
  `./gradlew testDebugUnitTest lintDebug assembleDebug` from `android/`. Android CI
  additionally runs managed-device tests on API 27, 35, and 36.
- Before asking for review or committing code changes, run the full test suite for the
  affected platform (the full XCTest suite for iOS; the Android Gradle checks above
  for Android). For UI changes, build and launch the affected app in its simulator or
  emulator for the human's manual test when available; if the required toolchain is
  unavailable, state the blocker and do not claim the change was validated.

## App identity
- iOS (resolved via xcconfig): bundle ID `com.uzairansar.hermesmobile` · tests
  `….tests` · Team `6GYD9C9N6R` · SKU `hermes-mobile-ios`.
- Android: application ID `com.uzairans.hermex`, defined in
  `android/app/build.gradle.kts`.

## iOS "push to branch testflight" (maintainer-only)
Upload the current branch to the side-by-side **Hermex Branch** internal TestFlight app
(`com.uzairansar.hermesmobile.branch`) — a TestFlight upload, **not** a git push.
Requires the maintainer's App Store Connect access; contributors never need this. Use a
unique `CURRENT_PROJECT_VERSION` (e.g. `YYYYMMDDHHMM`) each time. Full commands + branch
identity: `DEVELOPMENT.md`. Never touch the production `com.uzairansar.hermesmobile` app
unless explicitly asked.

## Working with the human
- Surface tradeoffs in plain English before non-obvious choices; when in doubt, ask.
- Ask before touching anything under the spec's "Open questions."
- After each slice, report: (1) files changed (2) build/test command run (3) result
  (4) next suggested step — plus a short manual simulator/emulator test plan when UI changed.

## Keep this file honest
If something here surprises you or contradicts the project, tell the developer and
**propose** an AGENTS.md edit — don't silently edit it. This file is a Band-Aid for what
can't be fixed in code/tests/tooling; your proposed edits are also a signal of what to fix structurally.
