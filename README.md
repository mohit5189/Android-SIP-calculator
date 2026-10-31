# SIP Calculator

The standalone Android app and embedded hosts share `app/src/main/java`.
`SIPCalculatorApp` exposes the original SIP, SWP, goal and additional calculator UI.
Its optional factory, analytics and promotion parameters support embedded hosts
without changing the calculation implementations.

Standalone Activity/Application, AdMob and Firebase analytics implementation are
in `app/src/standalone/java`, included only by this repository's application build.
The standalone manifest and Firebase configuration must not be merged into a host.
Optional calculation events use `LocalCalculationHandler`; currency can be supplied
through `LocalCurrencyManager`. An embedded host must inject its own owner-scoped
GoalDatabase and currency DataStore instead of using the standalone singletons.

For Paisa integration, read the parent repository's `AGENTS.md` and `docs/`.
Its `calculator` library consumes this repository as a pinned submodule, without
ads or calculator telemetry, and supplies account isolation and a persistent
return button. Calculator schema v3 is exported; host databases must use explicit
migrations rather than the legacy standalone destructive-migration configuration.

Standalone compilation:

```sh
export ANDROID_HOME=/path/to/Android/sdk
./gradlew :app:compileDebugKotlin
```
