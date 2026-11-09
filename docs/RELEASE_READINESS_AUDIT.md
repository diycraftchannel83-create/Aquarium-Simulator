# Aquarium Simulator — Technical Audit & Release Roadmap

Audit date: 2026-11-09
Audited branch: main
Working branch: release-readiness/phase-1

## Executive assessment

The repository is a native Android/Kotlin + Jetpack Compose game prototype with an aquarium management loop. It is not yet a production-ready 3D aquarium game. The current renderer is a custom Compose Canvas drawing scene with depth-style scaling and 2D paths; fish with configured assets are animated GIFs rendered as images. No GLB/glTF fish meshes, skeletal rigs, PBR materials, or real-time 3D renderer were found in the repository tree.

Do not market the current renderer as true 3D until the renderer and asset pipeline are upgraded and validated on devices.

## Findings

### Critical — monetization is simulated, not Google Play Billing

`AquaStoreSheet.kt` invokes `purchaseIAPPackage()` directly. That method increments local coins/pearls without launching a Play purchase flow, verifying a purchase token, acknowledging a purchase, or handling refunds/restores. This must not ship as a paid store. Integrate Google Play Billing and server-side purchase verification before enabling paid products.

### Critical — cloud sync and leaderboard are placeholders

`performCloudSync()` only changes local status/timestamp. `leaderboards` is a hard-coded sample list. These features must be connected to a real backend or clearly labelled as unavailable/local-only until implemented.

### High — simulation persisted too frequently (fixed on this branch)

The physics loop runs at about 30 updates per second and previously serialized the full game state into SharedPreferences on every tick. Phase 1 now updates the in-memory StateFlow on each tick and persists the latest simulation snapshot every five seconds. Explicit player actions still use immediate persistence. The latest snapshot is also persisted when the ViewModel is cleared.

### High — feeding ignored the food position (fixed on this branch)

The canvas now passes normalized tap coordinates, and feeding targets only the nearest fish within a bounded interaction radius. Taps far from fish no longer feed every fish or award challenge progress/experience.

### High — renderer and assets are not real 3D

Current fish are either hand-drawn 2D vector shapes or animated GIFs. The asset directory documents GIF/PNG/JPG/WebP support, not 3D model loading. Introduce a dedicated real-time 3D renderer (SceneView/Filament is a candidate) and a versioned glTF 2.0/GLB asset pipeline. Each fish asset should include a correctly proportioned mesh, UVs, PBR textures, rig/skeleton, and swim animation. Use species-specific fin/tail motion, depth-aware lighting, and LOD variants.

### High — release identity and signing need decisions

The current application ID is `com.aistudio.aquazen3d.kqvxz`, namespace is `com.example`, and version is `1.0 (1)`. Choose the permanent Play Store application ID and branding before the first production release; changing the ID after publishing creates a different app. Release signing currently expects an upload keystore and environment-provided passwords. Configure these as protected GitHub Actions secrets and use Play App Signing.

### High — build/release automation is missing

No GitHub Actions workflow, Gradle wrapper script, or Gradle wrapper JAR was present in the repository tree at audit time. Phase 1 adds CI that installs a pinned Gradle distribution and runs unit tests plus a debug APK build. A reproducible wrapper should be committed before handing the project to external developers or relying on local builds.

### Medium — gameplay integrity and lifecycle

Review fish reproduction completion, daily reward claim validation, economy balancing, save migration/versioning, and simulation lifecycle. The local-only economy is currently easy to modify and is not suitable for competitive leaderboards or purchases without server authority.

### Medium — Play policy and privacy readiness

Before publishing, complete a Data safety declaration, privacy policy, content rating, target API verification, ads declaration (if ads are added), account deletion flow (if accounts are added), and testing tracks. Request only permissions needed by shipped features. Verify all third-party asset licenses and retain attribution where required.

## Phase 1 changes on this branch

- Avoid full JSON/SharedPreferences serialization on every 33 ms simulation tick; persist the simulation snapshot every five seconds.
- Persist the current in-memory snapshot when the ViewModel is cleared.
- Normalize aquarium tap positions and feed only a nearby fish.
- Add an Android CI workflow for tests and debug APK assembly.
- Add this audit and staged release plan.

## Recommended delivery phases

### Phase 1 — stability and build foundation
- Keep simulation rendering and persistence decoupled.
- Fix gameplay interaction and save-state edge cases.
- Add CI, deterministic builds, lint/static checks, and automated tests.
- Resolve package ID, namespace, signing, versioning, and release configuration.

### Phase 2 — true 3D rendering and asset production
- Integrate SceneView/Filament after a small device compatibility spike.
- Replace the 2D fish renderer with GLB/glTF models.
- Produce licensed original fish models: clownfish, blue tang, angelfish, discus, mandarin, lionfish, and jellyfish as a separate non-fish creature.
- Add PBR maps, skeletal swim cycles, fin deformation, species-accurate proportions, natural eye placement, and transparent/translucent materials where appropriate.
- Build realistic tank glass, water volume, caustics, particles, aquatic plants, coral, gravel, and environment lighting.
- Provide low/medium/high quality presets and LOD/texture compression for a broad Android device range.

### Phase 3 — game design and economy
- Make fish respond to food, tank conditions, nearby fish, obstacles, and player interaction.
- Validate species compatibility, breeding, growth, hunger, oxygen, temperature, and water cleanliness.
- Balance progression and virtual currency; prevent duplicate rewards and invalid claims.
- Add accessible onboarding, settings, localization, save migration, and offline behavior.

### Phase 4 — commercial services
- Integrate Google Play Billing with product IDs, purchase verification, acknowledgement, restore, pending purchase, refund/revocation handling, and clear purchase UI.
- Either implement real cloud save/leaderboard backend with authentication and anti-cheat, or remove/label those features as local-only.
- Add analytics/diagnostics only with appropriate disclosure and consent.

### Phase 5 — release candidate
- Run automated unit/instrumentation tests and device tests across low/mid/high-tier Android hardware.
- Profile frame time, memory, thermal behavior, battery drain, startup time, and app size.
- Validate TalkBack, font scaling, screen sizes, offline mode, crash-free sessions, and data safety disclosures.
- Produce signed AAB, mapping/native symbols as applicable, store listing, screenshots, feature graphic, privacy policy, and staged rollout plan.

## Acceptance gates

Do not call the game Play Store ready until:
1. The app builds reproducibly in CI and a signed AAB is generated.
2. No fake purchase, fake cloud sync, or fake leaderboard is exposed as a real service.
3. The main fish are actual animated 3D models, not GIFs or vector drawings.
4. Save/load survives process death and app upgrades.
5. Gameplay, economy, and daily reward logic have automated tests.
6. Performance is measured on representative physical Android devices.
7. All asset licenses, privacy disclosures, store listing declarations, and Play Console requirements are checked.
