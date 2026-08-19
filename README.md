<div align="center">

  <h1>Tube Transport System: Reforged (1.21.1)</h1>

</div>

A 1.21.1 NeoForge port of [Tube Transport System](https://www.curseforge.com/minecraft/mc-mods/tube-transport-system)
by Alz454 (polyrobot), built from the [0.6 source](https://github.com/enhancedportals/TubeTransportSystem)
by way of the 1.12.2 Reforged port.

## Features

- **Transport Tube.** A directional glass tube. Anything that walks in accelerates along the tube
  axis until it hits the speed cap, so long runs are fast and short hops are gentle. Players, mobs,
  and animals all travel. Fall damage is cancelled while you are inside, so vertical shafts can be
  as deep as you like.
- **Connected textures.** Tubes joining tubes close up into one continuous corridor, and the inner
  skin stays visible while you ride.
- **Tube Station.** Two blocks tall and hollow. It is where you get in and out, and it sits flush
  against the tube above or below it. Standing on a station over an upward tube lifts you into the
  network.
- **Sneak to stop.** Place stations in series along a run and sneak to get off at an intermediate
  one instead of riding to the end.
- **Horizontal Tube Station.** The sideways variant, two blocks long, for entry and exit on a level run.

## Recipes

- **Transport Tube (16):** stone and glass in a ring around an ender pearl.
- **Tube Station (1):** a stone ring with a slab top and bottom, hollow centre. Either
  `smooth_stone_slab` (what the 1.12.2 stone slab flattened into) or `stone_slab` works.
- **Setting a direction:** a shapeless craft cycles a tube stack to the next direction, on 1, 4, or 9
  tubes at a time. The chain is undirected → down → up → north → south → east → west → undirected.
  The undirected tube takes its direction from the face you place it on.

## Changes from the 1.7.10 original

- The undirected tube has its own greyed out item texture so it is not mistaken for a directed one,
  and its tooltip explains that it faces the side you place it on.
- No walking head bob and no footstep sounds while riding.
- Tubes and stations render in the cutout pass, so they no longer draw over other mods' sky and
  cloud geometry.
- Two tubes of different directions meeting no longer z-fight on the shared face.
- The 1.7.10 in game update notifier was dropped.
- The held item ghost preview (the faint tube outline shown before placing) was not carried over.

## Notes on the 1.21.1 port

Behaviour is a straight port of the 1.12.2 version, including its inherited quirks (an "east" tube
carries you east even though it is stored as the west facing, and breaking the *upper* half of a
station still destroys the pair without dropping the item). What had to change:

- **Item metadata is gone**, so the seven tube stacks are now seven items: `tts:tube` (undirected)
  and `tts:tube_down` / `_up` / `_north` / `_south` / `_east` / `_west`, named after the direction
  the tube carries you, which is what the original tooltip showed. They share one block, one name,
  and the same cycle recipes. A broken tube still drops the undirected item.
- **Block metadata is gone**, so the state is `facing` on the tube, `facing` + `top` on the station,
  and `facing` + `front` on the horizontal station. Every ported table still runs off the old
  metadata number, rebuilt from those properties.
- **Rendering** uses a custom baked model registered through a NeoForge geometry loader. The
  neighbour sampling the 1.12.2 port did in `getExtendedState` now happens in
  `BakedModel#getModelData`, which is the modern equivalent and still runs per chunk rebuild.
  The 123 sprites need no registration: everything under `textures/block` is stitched automatically.
- **Recipes and drops are data** (`data/tts/recipe`, `data/tts/loot_table`) instead of code.
- **The selection outline** follows the tube and station walls rather than the full cube, because a
  single shape now drives both the outline and the ray trace. Looking through an open tube end still
  targets whatever is behind it, as before.

## Config

`config/tts-common.toml`, or the Config button on the mod list screen.

- `MaxTubeSpeed` (default 0.5, range 0.0 to 10.0). The speed cap for anything travelling through a
  tube. Raise it for long distance networks. High values will outrun chunk loading.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.240 or newer

No dependencies, no core mods, no mixins. Nothing in vanilla is patched. Needs to be installed on
both client and server.

## Installation

1. Install NeoForge for 1.21.1.
2. Drop `tts-1.21.1-0.6.jar` into your `mods` folder.
3. Launch the game.

## Building

1. Clone the repository.
2. Run `./gradlew build` with JDK 21.
3. The jar lands in `build/libs/tts-1.21.1-0.6.jar`.

## License

[LGPL-3.0](LICENSE). The original mod is LGPLv3, so this port keeps the same license.
