# Animated Fish Assets

These transparent looping GIFs are runtime display assets generated from the visual identities of the static GLB starter meshes in `assets/models/fish`.

| Runtime GIF | GLB source | Aquarium species ID |
|---|---|---|
| `clownfish.gif` | `clownfish.glb` | `clownfish` |
| `blue_tang.gif` | `blue_tang.glb` | `blue_tang` |
| `angelfish.gif` | `angelfish.glb` | `flame_angelfish` |
| `neon_tetra.gif` | `neon_tetra.glb` | `neon_tetra` |

Each GIF has 12 frames, transparent background, and a looping tail/fin swim cycle. The GLB files remain static starter meshes; these GIFs are 2D animated runtime assets, not animated GLB exports or a real-time 3D model renderer.

Regenerate with:

```bash
python -m pip install Pillow
python tools/generate_fish_gifs.py
```
