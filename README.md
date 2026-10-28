<div align="center">

  <h1>Tube Transport System: Reforged (1.20.1)</h1>

</div>

A 1.20.1 Forge port of [Tube Transport System](https://www.curseforge.com/minecraft/mc-mods/tube-transport-system)
by Alz454 (polyrobot), built from SrNadien's 1.21.1 port of the
[1.12.2 version](https://github.com/haunterdev/TubeTransportSystem-Reforged-1.12.2/tree/main).

Client and server side mod.

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
- **Seven tube items.** `tts:tube` takes its direction from the face you place it on, and
  `tts:tube_down`, `_up`, `_north`, `_south`, `_east` and `_west` are named after the direction the
  tube carries you. They share one block, and a broken or pick-blocked tube gives `tts:tube`.
- **Mining.** Tubes and stations are stone: they need a pickaxe to drop and mine at pickaxe speed.

## Recipes

- **Transport Tube (16):** stone and glass in a ring around an ender pearl.
- **Tube Station (1):** a stone ring with a slab top and bottom, hollow centre. Either
  `smooth_stone_slab` or `stone_slab` works.
- **Setting a direction:** a shapeless craft cycles a tube stack to the next direction, on 1, 4, or 9
  tubes at a time. The chain is undirected, down, up, north, south, east, west, then back to
  undirected.

## Changes from the original

- The undirected tube has its own greyed out item texture so it is not mistaken for a directed one,
  and its tooltip explains that it faces the side you place it on.
- No walking head bob and no footstep sounds while riding.
- Tubes and stations render in the cutout pass, so they no longer draw over other mods' sky and
  cloud geometry.
- Two tubes of different directions meeting no longer z-fight on the shared face.
- The selection outline follows the tube and station walls rather than the full cube. Looking
  through an open tube end still targets whatever is behind it.
- The 1.7.10 in game update notifier was dropped.
- The held item ghost preview (the faint tube outline shown before placing) was not carried over.

## Config

`config/tts-common.toml`

- `MaxTubeSpeed` (default 0.5, range 0.0 to 10.0). The speed cap for anything travelling through a
  tube. Raise it for long distance networks. High values will outrun chunk loading.

## Requirements

- Minecraft 1.20.1
- Forge 47.4 or newer

## Installation

1. Install Forge for 1.20.1.
2. Put `tts-1.20.1-0.6.jar` into your `mods` folder.
3. Launch the game.

## Building

1. Clone the repository and check out the `1.20.1` branch.
2. Run `./gradlew build` with JDK 17 or newer.
3. The jar lands in `build/libs/tts-1.20.1-0.6.jar`.

`./gradlew runGameTestServer` runs the headless tests in `src/main/java/tubeTransportSystem/gametest`.

## Credits

- Alz454 (polyrobot), the original Tube Transport System.
- SrNadien, the 1.21.1 port this version is built from.

## License

[LGPL-3.0](LICENSE). The original mod is LGPLv3, so this port keeps the same license.
