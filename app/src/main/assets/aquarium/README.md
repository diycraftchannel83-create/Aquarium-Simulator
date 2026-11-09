# AquaZen 3D Game Asset Directory

Place your custom aquarium game assets in these subfolders. The app automatically scans, loads, and displays static images and animated GIFs!

## Folder Structure

- `fish/`
  - Put animated fish GIFs or PNG/JPG fish textures here.
  - Recommended filenames: `<species_id>.gif` (e.g., `clownfish.gif`, `crystal_jelly.gif`, `blue_tang.gif`, `mandarin.gif`)
  - Transparent backgrounds (`xc:none` or alpha channel) look best.

- `decor/`
  - Put live animated plants, corals, chests, or decorations here.
  - Recommended filenames: `<decor_id>.gif` (e.g., `neon_anemone.gif`, `amazon_sword.gif`, `fire_coral.gif`)

- `backgrounds/`
  - Put high-resolution panoramic tank backgrounds and aquascape themes here.
  - Format: JPG / PNG (e.g., `sanctuary_panoramic.jpg`, `deep_abyss.jpg`)

- `effects/`
  - Animated live effects such as bubble columns, thermal vents, and caustics.
  - Format: GIF (e.g., `bubbles_stream.gif`, `water_caustics.gif`)

## Supported Formats
- Animated GIF (`.gif`) - Fully animated live playback via Coil GIF decoder.
- Static PNG (`.png`) & JPEG (`.jpg`, `.jpeg`)
- WebP (`.webp`)
