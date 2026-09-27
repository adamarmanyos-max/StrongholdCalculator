# Stronghold Finder

A client-side Fabric mod for Minecraft **1.16.1** that works out where the
stronghold is from your Eye of Ender throws. It uses the same maths as
[Ninjabrain Bot](https://github.com/Ninjabrain1/Ninjabrain-Bot), running inside
the game, so it works on iOS (Amethyst) where Ninjabrain Bot can't run.

Built to pair with **Toolscreen Mobile** (the `Toolscreen` repo), whose Eye
Measure mode gives the magnified view needed to find the stronghold with **one
eye** ("boat eye"). Stronghold Finder works without it, too.

Not for verified speedruns: speedrun.com only allows mods on its approved list.

## Install

1. Get the jars from the latest green run on each repo's **Actions** tab
   (artifacts `stronghold-finder` and `toolscreen-mobile`).
2. Put both in Amethyst's `.minecraft/mods/` with Fabric Loader for 1.16.1.
   Fabric API is not needed.

## One-eye (boat eye) walkthrough

1. Press `.` for settings and check **Boat eye** is `on` and **Boat eye aim
   error** is `0.0010 deg`.
2. Throw an eye and **stand still**. Pushing into the corner of a block stops
   you exactly and makes your position exact.
3. Press `↑` to switch Toolscreen Mobile into **Eye Measure**. The zoom panel
   with the ruler appears on one side; Stronghold Finder's results appear on
   the other.
4. Put the crosshair on the eye as closely as you can and press **F3+C**.
5. In the zoom panel, count how many ruler cells the middle of the eye is from
   the centre line. Press `→` once per cell if it is to the right, `←` once per
   cell if it is to the left.
6. Read the top row: stronghold coordinates, certainty, distance and Nether
   coordinates. At about 95% or more, go. If lower, the panel says where to
   walk for a second throw.
7. Press `↑` to leave Eye Measure, and `B` to clear for the next stronghold.

The throw is labelled `boat` (green) only if the view really was zoomed in:
one pixel at most 0.005°. Otherwise it is labelled `aim`, uses your normal
aim error, and the notice tells you to press `↑`.

### Why there's no actual boat

In Ninjabrain Bot, the boat is a trick for getting around F3+C: F3+C only
gives the angle to two decimal places (about 0.01°). Entering a boat sets
your angle to a known value, and every mouse movement after that changes it
by a fixed amount, so Ninjabrain can work out the missing decimals.

This mod reads the exact angle straight from the game, so it never loses
those decimals and the boat step adds nothing. What gives boat-eye accuracy
is the other half of the technique: lining the eye up on a magnified view
and correcting to the pixel. That is what Eye Measure and the arrow keys do.

## Keys

| Key | Action |
| --- | --- |
| F3+C | Add a throw (don't hold it: F3+C held for 10 s crashes the game on purpose) |
| `←` `→` | Move the last throw by one pixel |
| `↑` | Eye Measure on/off (Toolscreen Mobile), or tall window on desktop |
| `B` | Clear all throws |
| `N` | Hide/show the panel |
| `.` | Settings |
| `M` | Switch between measuring (FOV 30, slow mouse) and travel settings |
| `K` | Calibrate normal aim error: stand in the stronghold's chunk after a find |
| `P` | Teleport to the top prediction (creative, for practice) |
| `[` `]` | Panel opacity |
| `-` `=` | Panel size |

These are raw keys, so they also work as Amethyst on-screen buttons that send
the same key codes.

## Settings

- **Aim error**: how accurate normal (unzoomed) throws are. `K` calibrates
  it. Typical (from Ninjabrain Bot): 0.05 to 0.2 at a wide FOV, 0.02 to 0.04 at FOV 30.
- **Measure from**: `my crosshair`, or `eye flight`, which reads the flying
  eye's real direction from the game and is exact. Practice only.
- **Boat eye**: allow boat-eye throws when zoomed in.
- **Boat eye aim error**: 0.0005 to 0.005°. Ninjabrain Bot's default is 0.001.
  Never taken as less than 0.6 of a pixel.
- **Measuring trainer**: shows the rendered FOV, render height, degrees per
  pixel, whether you're zoomed in enough for boat eye, and whether you're on
  a block corner.
- **Blind practice**: drops you somewhere random (needs cheats).

## How it fits with Toolscreen Mobile

The two mods don't depend on each other at build time. Stronghold Finder looks
for Toolscreen Mobile's `ToolscreenApi` by reflection when it starts:

- Degrees per pixel comes from the real render: the framebuffer height, which
  Eye Measure makes about 8× taller than the screen, and the FOV from
  `GameRenderer.getFov`, which Eye Measure halves. So a ruler cell and one
  arrow press are always the same angle.
- In any Toolscreen mode, the results panel is drawn beside the strip, because
  the normal HUD position is cropped out of the tall render.
- `↑` switches Eye Measure.

## Building

CI builds every push. Locally, with JDK 17: `./gradlew build`. The jar is in
`build/libs/`. Gradle is pinned to 8.7 for Loom 1.6.

This source was recovered from the 6.1.0 jar (decompiled and translated to
Yarn names), because the original source wasn't in this repository.
