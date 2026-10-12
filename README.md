# game-box <img src="https://github.com/youngmonkeys/game-box/blob/master/logo.png" width="72" />

* [Features](#features)
* [Introduction](#introduction)
* [Get started](#get-started)
* [Documentation](#documentation)
* [Demos](#demos)
* [Tutorials](#tutorials)
* [Performance testing](#performance-testing)
* [Contact us](#contact-us)

# Features

Provide `Entities, Managers, Handlers, Maths` classes for many types of online games (Turn-based (Located), MMO,
Normal):

* **Entities**: MMOPlayer, LocatedPlayer, MMORoom, LocatedRoom, NormalRoom, MMOVirtualWorld
* **Managers**: MMORoomManager, LocatedRoomManager, MMOPlayerManager, LocatedPlayerManager
* **Handlers**: Synchronized Position Handler
* **Maths**: Rect, Vec2, Vec3

# Introduction

To rapidly build online games, developers often have to use a game server engine like SmartFoxServer or Photon, but
unfortunately, they come with really high price, especially when more and more users are engaging in our products.
Therefore, we develop `game-box engine` powered by the [EzyFox Ecosystem](https://github.com/youngmonkeys/ezyfox-server)
aiming to make everything free and open for everyone who are keen on building scalable multi-players games.

EzyFox Ecosystem supports a wide range of most important components to develop an enterprise product including TCP, UDP,
WebSocket protocols with SSL encryption, HTTP RESTful API, Remote procedure call RPC protocol, Database interaction,
Memory caching, Message Queue.

# Get Started

[https://youngmonkeys.org/game-box-getting-started/](https://youngmonkeys.org/game-box-getting-started/)

# Documentation

[https://youngmonkeys.org/game-box](https://youngmonkeys.org/game-box/)

# Demos

[EzySmashers (MMO Game built with game-box and Unity)](https://github.com/vu-luong/EzySmashers)

# Tutorials:

[https://youngmonkeys.org/game-box/](https://youngmonkeys.org/game-box/)

# Performance testing

The test sources contain a multiplayer space shooter (ships shoot asteroids and each other) that runs on
`MMOVirtualWorld` and logs performance metrics, so you can load test the MMO rooms with many players.
There are three versions, one for each room type:

| Version | Main class (in `src/test/java`) | Room | Web client |
|---|---|---|---|
| 2D | `com.tvd12.gamebox.testing.spaceshooter.SpaceShooterServer` | `MMOGridRoom` | `src/test/resources/space-shooter` |
| 2.5D | `com.tvd12.gamebox.testing.shooter25d.SpaceShooterServer25D` | `MMOUniformGridRoom` | `src/test/resources/space-shooter-25d` |
| 3D | `com.tvd12.gamebox.testing.shooter3d.SpaceShooterServer3D` | `MMOOcTreeRoom` | `src/test/resources/space-shooter-3d` |

Each server starts an embedded EzyFox server (WebSocket) and an EzyHTTP server for the web client. The web client
uses the [ezyfox-server-js-client](https://github.com/youngmonkeys/ezyfox-server-js-client). All versions use the
same ports, so run only one at a time.

## Run

1. Run the `main` method of one of the classes above from your IDE, with the project root as the working directory.
   Or run it with Maven from the project root:

   ```bash
   mvn test-compile exec:java \
     -Dexec.classpathScope=test \
     -Dexec.mainClass=com.tvd12.gamebox.testing.shooter3d.SpaceShooterServer3D \
     -Dbots=200
   ```

2. Open [http://localhost:8080](http://localhost:8080), enter a name and play.

| System property | Default | Description |
|---|---|---|
| `bots` | `200` | Number of bot ships. Bots move, aim and shoot on their own. |
| `botState` | `true` | Build a state packet for every bot each time one is due, as if the bots were real players, but don't send it. This simulates the cost of `bots` connected players. |
| `httpPort` | `8080` | Port of the web client. |
| `webSocketPort` | `2208` | Port of the EzyFox WebSocket server. |

Controls:

- `W` `A` `S` `D` / arrow keys: move
- Mouse: aim. Left click / `Space`: fire
- `Q` / `E` (2.5D, 3D): climb / descend
- Mouse wheel (3D): gun pitch
- `G`: toggle the grid / area of interest overlay
- `M`: toggle music

The HUD shows your cell, the number of nearby entities, the total number of entities in the world and the
number of states received per second.

## Read the metrics

Logs are written to the console and to `logs/space-shooter.log`. The log file is overwritten on each run.
Every 5 seconds the server writes one metrics line, prefixed with `perf` (2D), `perf25d` (2.5D) or `perf3d` (3D):

- `ticks`: room updates in the period. The target is 20 per second (50 ms per tick).
- `tick avg/max`: time spent in one tick. Keep it well below 50 ms. `interval max` is the longest time between
  two ticks.
- `phase avg`: time spent in each part of the tick:
  - `nearby` (2.5D, 3D only): recompute the nearby players of every player whose interest is enabled;
  - `update`: move entities and run the bot AI;
  - `collision`, `bullets`: collision checks for ships and bullets;
  - `broadcast`: build and send the state packets.
- `grid ops/s` and `avg(us)`: number of `setPlayerPosition` (move), `addPlayer` and `removePlayer` calls per
  second on the room, and their average time in microseconds.
- `entities`, `messages`, `entities per message`: world size and the data sent to each player.
- `heap`, `gc count`, `gc time` (2.5D, 3D only): memory usage and garbage collection in the period.

The `MMORoomGroup` logs a warning `Room group update took: ...ms, exceeded time tick` whenever a tick takes longer
than the time tick. A few of these warnings while the JVM warms up are expected.

The game uses these settings, which are a good starting point for your own games:

- Asteroids and power-ups don't need to know what is around them, so they are created with
  `interestEnabled = false`. Only ships keep a nearby list.
- Bullets aren't room players. The game moves them itself and checks collisions with `searchPlayers(...)`.
- The 2.5D and 3D rooms use `updateNearbyOnTick(true)` and `nearbyUpdateIntervalTicks(2)`: nearby lists are
  recomputed once per tick for half of the players, instead of on every move.
- Each player gets a state packet every 2 ticks (10 per second), and the state of an entity is built once per tick
  and shared between the packets.

# Contact us

- Get in touch with us on [Facebook](https://www.facebook.com/youngmonkeys.org)
- Ask us on [stackask.com](https://stackask.com)
- Email us at [contact@youngmonkeys.org](mailto:contact@youngmonkeys.org)
- Chat with us on [Discord](https://discord.gg/hKV2cbaT5h)

# Help us by donation

Currently, our operating budget is fully from on our own salaries, and all product developments are still based on
voluntary contributions from a few members of the organization. Apparently, the low budget would cause many considerable
difficulties for us. Therefore, with a clear roadmap and an ambitious goal to provide intellectual products for the
community, we really appreciate your support if you can provide a donation to take us further steps. Thanks in advance
for your meaningful contributions!

[https://youngmonkeys.org/donate/](https://youngmonkeys.org/donate/)

# License

- Apache License, Version 2.0
