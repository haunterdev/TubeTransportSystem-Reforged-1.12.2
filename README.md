<div align="center">

  <h1>Tube Transport System Redux (1.12.2 Forge)</h1>

</div>

A 1.12.2 Forge port of [Tube Transport System](https://www.curseforge.com/minecraft/mc-mods/tube-transport-system)
by Alz454 (polyrobot), originally for 1.7.10. Ported line for line from the
[0.6 source](https://github.com/enhancedportals/TubeTransportSystem), so the blocks, recipes,
speeds, and look all match the original. Where 1.12.2 forced a change, the code says so.

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
- **Tube Station (1):** a stone ring with a stone slab top and bottom, hollow centre.
- **Setting a direction:** a shapeless craft cycles a tube stack to the next direction, on 1, 4, or 9
  tubes at a time. Cycling past the last direction returns the tubes to their undirected form, which
  takes its direction from the face you place it on.

## Changes from the 1.7.10 original

Everything below is deliberate. Anything not listed here behaves as it did in 0.6.

- The undirected tube has its own greyed out item texture so it is not mistaken for a directed one,
  and its tooltip explains that it faces the side you place it on.
- No walking head bob and no footstep sounds while riding.
- Tubes and stations render in the cutout pass, so they no longer draw over other mods' sky and
  cloud geometry.
- Two tubes of different directions meeting no longer z-fight on the shared face.
- The 1.7.10 in game update notifier was dropped.
- The held item ghost preview (the faint tube outline shown before placing) was not carried over.

## Config

`config/TubeTransportSystem.cfg`

- `MaxTubeSpeed` (default 0.5, range 0.0 to 10.0). The speed cap for anything travelling through a
  tube. Raise it for long distance networks. High values will outrun chunk loading.

## Requirements

- Minecraft 1.12.2
- Minecraft Forge 14.23.5.2860 or newer

No dependencies, no core mods, no mixins. Nothing in vanilla is patched. Needs to be installed on
both client and server.

## Installation

1. Install Minecraft Forge for 1.12.2.
2. Drop `tts-0.6.jar` into your `mods` folder.
3. Launch the game.

## Building

1. Clone the repository.
2. Run `./gradlew build` (JDK 8 is fetched by the toolchain; Gradle itself needs JDK 17).
3. The jar lands in `build/libs/tts-0.6.jar`.

## License

[LGPL-3.0](LICENSE). The original mod is LGPLv3, so this port keeps the same license.
