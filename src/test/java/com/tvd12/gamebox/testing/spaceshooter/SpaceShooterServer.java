package com.tvd12.gamebox.testing.spaceshooter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.tvd12.ezyfox.entity.EzyArray;
import com.tvd12.ezyfox.entity.EzyObject;
import com.tvd12.ezyfox.factory.EzyEntityFactory;
import com.tvd12.ezyfoxserver.app.EzyAppRequestController;
import com.tvd12.ezyfoxserver.command.EzyAppResponse;
import com.tvd12.ezyfoxserver.command.EzyAppSetup;
import com.tvd12.ezyfoxserver.constant.EzyEventType;
import com.tvd12.ezyfoxserver.constant.EzyMaxRequestPerSecondAction;
import com.tvd12.ezyfoxserver.context.EzyAppContext;
import com.tvd12.ezyfoxserver.controller.EzyAppEventController;
import com.tvd12.ezyfoxserver.embedded.EzyEmbeddedServer;
import com.tvd12.ezyfoxserver.entity.EzyUser;
import com.tvd12.ezyfoxserver.event.EzyUserRemovedEvent;
import com.tvd12.ezyfoxserver.event.EzyUserRequestAppEvent;
import com.tvd12.ezyfoxserver.ext.EzyAbstractAppEntry;
import com.tvd12.ezyfoxserver.ext.EzyAbstractAppEntryLoader;
import com.tvd12.ezyfoxserver.ext.EzyAppEntry;
import com.tvd12.ezyfoxserver.setting.EzyAppSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzySessionManagementSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzySessionManagementSettingBuilder.EzyMaxRequestPerSecondBuilder;
import com.tvd12.ezyfoxserver.setting.EzySettingsBuilder;
import com.tvd12.ezyfoxserver.setting.EzySimpleSettings;
import com.tvd12.ezyfoxserver.setting.EzySocketSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyUserManagementSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyWebSocketSettingBuilder;
import com.tvd12.ezyfoxserver.setting.EzyZoneSettingBuilder;
import com.tvd12.gamebox.entity.MMOGridRoom;
import com.tvd12.gamebox.entity.MMOPlayer;
import com.tvd12.gamebox.entity.MMORoom;
import com.tvd12.gamebox.entity.MMOVirtualWorld;
import com.tvd12.gamebox.math.Vec3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class SpaceShooterServer {

    public static final String ZONE_NAME = "space-shooter";
    public static final String APP_NAME = "space-shooter";

    private static final float WORLD_WIDTH = 4000;
    private static final float WORLD_HEIGHT = 4000;
    private static final float CELL_SIZE = 400;
    private static final int CELL_RANGE_OF_INTEREST = 2;
    private static final int TIME_TICK_MILLIS = 50;
    private static final int ASTEROID_COUNT = 160;
    private static final int MAX_ASTEROID_SPAWN_PER_TICK = 3;

    private static final int KIND_SHIP = 1;
    private static final int KIND_ASTEROID = 2;
    private static final int KIND_BULLET = 3;
    private static final int KIND_POWERUP = 4;

    private static final int SHIP_MAX_HP = 100;
    private static final float SHIP_RADIUS = 24;
    private static final float SHIP_SPEED = 380;
    private static final float SHIP_INVULNERABLE_SECONDS = 2.5f;
    private static final float SHIP_RESPAWN_SECONDS = 3;
    private static final float FIRE_INTERVAL_SECONDS = 0.18f;
    private static final float BULLET_SPEED = 950;
    private static final float BULLET_RADIUS = 6;
    private static final float BULLET_TTL_SECONDS = 1.1f;
    private static final int BULLET_DAMAGE = 10;
    private static final float POWERUP_RADIUS = 20;
    private static final float POWERUP_TTL_SECONDS = 20;
    private static final int POWERUP_HEAL = 40;
    private static final float POWERUP_DROP_RATE = 0.08f;
    private static final float EVENT_DISTANCE = 1600;

    private static final String WEB_ROOT = "space-shooter";
    private static final File WEB_ROOT_DIR = new File("src/test/resources/" + WEB_ROOT);

    private final int httpPort;
    private final int webSocketPort;
    private final MMOVirtualWorld world;
    private final MMOGridRoom room;
    private final Map<String, GameEntity> entities = new HashMap<>();
    private final Map<String, Ship> shipByUsername = new HashMap<>();
    private final List<Ship> ships = new ArrayList<>();
    private final List<GameEntity> removedEntities = new ArrayList<>();
    private final List<GameEvent> events = new ArrayList<>();
    private final List<String> nearbyBuffer = new ArrayList<>();
    private final Queue<Runnable> commands = new ConcurrentLinkedQueue<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final Random random = new Random();
    private final Vec3 positionBuffer = new Vec3();
    private volatile EzyAppContext appContext;
    private int asteroidCount;
    private long tick;
    private long lastTickNanos;

    public SpaceShooterServer(int httpPort, int webSocketPort, int botCount) {
        this.httpPort = httpPort;
        this.webSocketPort = webSocketPort;
        this.world = MMOVirtualWorld.builder()
            .roomGroupCount(1)
            .timeTickMillis(TIME_TICK_MILLIS)
            .build();
        this.room = (MMOGridRoom) MMOGridRoom.builder()
            .maxX(WORLD_WIDTH)
            .maxY(WORLD_HEIGHT)
            .maxZ(0)
            .cellSize(CELL_SIZE)
            .distanceOfInterest(CELL_RANGE_OF_INTEREST)
            .maxPlayer(100000)
            .addRoomUpdatedHandler(this::onRoomUpdated)
            .name("space-shooter")
            .build();
        for (int i = 0; i < ASTEROID_COUNT; ++i) {
            spawnAsteroid(
                3,
                randomRange(100, WORLD_WIDTH - 100),
                randomRange(100, WORLD_HEIGHT - 100)
            );
        }
        for (int i = 1; i <= botCount; ++i) {
            createShip(null, "bot-" + i);
        }
    }

    public static void main(String[] args) throws Exception {
        new SpaceShooterServer(
            Integer.getInteger("httpPort", 8080),
            Integer.getInteger("webSocketPort", 2208),
            Integer.getInteger("bots", 10)
        ).start();
    }

    public void start() throws Exception {
        EzyEmbeddedServer ezyServer = EzyEmbeddedServer.builder()
            .settings(createSettings())
            .build();
        ezyServer.start();
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
        httpServer.createContext("/", this::serveFile);
        httpServer.start();
        world.addRoom(room);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            world.destroy();
            httpServer.stop(0);
            ezyServer.stop();
        }));
        System.out.println("space shooter started at: http://localhost:" + httpPort);
    }

    private EzySimpleSettings createSettings() {
        return new EzySettingsBuilder()
            .zone(
                new EzyZoneSettingBuilder()
                    .name(ZONE_NAME)
                    .application(
                        new EzyAppSettingBuilder()
                            .name(APP_NAME)
                            .entryLoader(SpaceShooterAppEntryLoader.class)
                            .entryLoaderArgs(new Object[] {this})
                            .build()
                    )
                    .userManagement(
                        new EzyUserManagementSettingBuilder()
                            .userNamePattern("^[a-zA-Z0-9_]{3,24}$")
                            .maxSessionPerUser(1)
                            .build()
                    )
                    .build()
            )
            .socket(
                new EzySocketSettingBuilder()
                    .active(false)
                    .build()
            )
            .websocket(
                new EzyWebSocketSettingBuilder()
                    .active(true)
                    .port(webSocketPort)
                    .maxFrameSize(32768)
                    .build()
            )
            .sessionManagement(
                new EzySessionManagementSettingBuilder()
                    .sessionMaxRequestPerSecond(
                        new EzyMaxRequestPerSecondBuilder()
                            .value(60)
                            .action(EzyMaxRequestPerSecondAction.DROP_REQUEST)
                            .build()
                    )
                    .build()
            )
            .build();
    }

    private void configApp(EzyAppContext context) {
        this.appContext = context;
        EzyAppSetup setup = context.get(EzyAppSetup.class);
        setup.setRequestController((EzyAppRequestController) this::onUserRequest);
        setup.addEventController(
            EzyEventType.USER_REMOVED,
            (EzyAppEventController<EzyUserRemovedEvent>) (ctx, event) ->
                commands.add(() -> removeShip(event.getUser().getName()))
        );
    }

    private void onUserRequest(EzyAppContext context, EzyUserRequestAppEvent event) {
        EzyArray data = event.getData();
        String command = data.get(0, String.class);
        EzyUser user = event.getUser();
        if ("join".equals(command)) {
            commands.add(() -> joinShip(user));
        } else if ("input".equals(command)) {
            EzyArray params = data.get(1, EzyArray.class);
            Ship ship = findShip(user.getName());
            if (ship == null || params == null || params.size() < 6) {
                return;
            }
            ship.up = intAt(params, 0) == 1;
            ship.down = intAt(params, 1) == 1;
            ship.left = intAt(params, 2) == 1;
            ship.right = intAt(params, 3) == 1;
            ship.aim = intAt(params, 4) / 1000f;
            ship.fire = intAt(params, 5) == 1;
        }
    }

    private static int intAt(EzyArray array, int index) {
        Object value = array.get(index);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private Ship findShip(String username) {
        synchronized (shipByUsername) {
            return shipByUsername.get(username);
        }
    }

    private void joinShip(EzyUser user) {
        Ship ship = findShip(user.getName());
        if (ship == null) {
            ship = createShip(user, user.getName());
            synchronized (shipByUsername) {
                shipByUsername.put(user.getName(), ship);
            }
        }
        EzyObject joined = EzyEntityFactory.newObjectBuilder()
            .append("id", ship.getName())
            .append("name", ship.displayName)
            .append("worldWidth", WORLD_WIDTH)
            .append("worldHeight", WORLD_HEIGHT)
            .append("cellSize", CELL_SIZE)
            .append("cellRange", CELL_RANGE_OF_INTEREST)
            .append("tickMillis", TIME_TICK_MILLIS)
            .append("maxHp", SHIP_MAX_HP)
            .build();
        send(user, "joined", joined);
    }

    private void removeShip(String username) {
        Ship ship;
        synchronized (shipByUsername) {
            ship = shipByUsername.remove(username);
        }
        if (ship == null || ship.removed) {
            return;
        }
        ship.removed = true;
        ships.remove(ship);
        entities.remove(ship.getName());
        room.removePlayer(ship);
    }

    private void send(EzyUser user, String command, EzyObject params) {
        EzyAppContext context = appContext;
        if (context == null) {
            return;
        }
        context.cmd(EzyAppResponse.class)
            .command(command)
            .params(params)
            .user(user)
            .execute();
    }

    private void onRoomUpdated(MMORoom updatedRoom, List<MMOPlayer> players) {
        long now = System.nanoTime();
        float dt = lastTickNanos == 0
            ? TIME_TICK_MILLIS / 1000f
            : Math.min(0.2f, (now - lastTickNanos) / 1_000_000_000f);
        lastTickNanos = now;
        ++tick;

        Runnable command;
        while ((command = commands.poll()) != null) {
            command.run();
        }
        for (MMOPlayer player : players) {
            GameEntity entity = (GameEntity) player;
            if (isActive(entity)) {
                updateEntity(entity, dt);
            }
        }
        for (MMOPlayer player : players) {
            GameEntity entity = (GameEntity) player;
            if (!isActive(entity)) {
                continue;
            }
            if (entity.kind == KIND_BULLET) {
                handleBulletCollisions(entity);
            } else if (entity.kind == KIND_SHIP) {
                handleShipCollisions((Ship) entity);
            }
        }
        for (GameEntity entity : removedEntities) {
            room.removePlayer(entity);
            entities.remove(entity.getName());
        }
        removedEntities.clear();
        maintainAsteroids();
        broadcastState();
        events.clear();
    }

    private void updateEntity(GameEntity entity, float dt) {
        switch (entity.kind) {
            case KIND_SHIP:
                updateShip((Ship) entity, dt);
                break;
            case KIND_ASTEROID:
                updateAsteroid(entity, dt);
                break;
            case KIND_BULLET:
                updateBullet(entity, dt);
                break;
            default:
                updatePowerup(entity, dt);
                break;
        }
    }

    private void updateShip(Ship ship, float dt) {
        if (ship.respawnIn > 0) {
            ship.respawnIn -= dt;
            if (ship.respawnIn <= 0) {
                respawnShip(ship);
            }
            return;
        }
        if (ship.user == null) {
            updateBot(ship, dt);
        }
        ship.invulnerableIn = Math.max(0, ship.invulnerableIn - dt);
        float inputX = (ship.right ? 1 : 0) - (ship.left ? 1 : 0);
        float inputY = (ship.down ? 1 : 0) - (ship.up ? 1 : 0);
        float inputLength = (float) Math.sqrt(inputX * inputX + inputY * inputY);
        if (inputLength > 0) {
            inputX /= inputLength;
            inputY /= inputLength;
        }
        float factor = Math.min(1f, dt * 8);
        ship.vx += (inputX * SHIP_SPEED - ship.vx) * factor;
        ship.vy += (inputY * SHIP_SPEED - ship.vy) * factor;
        ship.x = clamp(ship.x + ship.vx * dt, SHIP_RADIUS, WORLD_WIDTH - SHIP_RADIUS);
        ship.y = clamp(ship.y + ship.vy * dt, SHIP_RADIUS, WORLD_HEIGHT - SHIP_RADIUS);
        ship.angle = ship.aim;
        ship.fireCooldown -= dt;
        if (ship.fire && ship.fireCooldown <= 0) {
            ship.fireCooldown = FIRE_INTERVAL_SECONDS;
            spawnBullet(ship);
        }
        moveEntity(ship);
    }

    private void updateBot(Ship bot, float dt) {
        bot.thinkIn -= dt;
        if (bot.thinkIn <= 0) {
            bot.thinkIn = randomRange(1, 3);
            bot.left = random.nextFloat() < 0.4f;
            bot.right = !bot.left && random.nextFloat() < 0.6f;
            bot.up = random.nextFloat() < 0.4f;
            bot.down = !bot.up && random.nextFloat() < 0.6f;
            if (bot.x < 300) {
                bot.left = false;
                bot.right = true;
            } else if (bot.x > WORLD_WIDTH - 300) {
                bot.right = false;
                bot.left = true;
            }
            if (bot.y < 300) {
                bot.up = false;
                bot.down = true;
            } else if (bot.y > WORLD_HEIGHT - 300) {
                bot.down = false;
                bot.up = true;
            }
        }
        GameEntity target = findNearestTarget(bot, 650);
        if (target != null) {
            bot.aim = (float) Math.atan2(target.y - bot.y, target.x - bot.x);
            bot.fire = true;
        } else {
            bot.fire = false;
            if (bot.vx * bot.vx + bot.vy * bot.vy > 100) {
                bot.aim = (float) Math.atan2(bot.vy, bot.vx);
            }
        }
    }

    private GameEntity findNearestTarget(Ship bot, float maxDistance) {
        GameEntity nearest = null;
        float nearestDistance = maxDistance * maxDistance;
        nearbyBuffer.clear();
        bot.getNearbyPlayerNames(nearbyBuffer);
        for (String name : nearbyBuffer) {
            GameEntity other = entities.get(name);
            if (other == null
                || other == bot
                || !isActive(other)
                || (other.kind != KIND_ASTEROID && other.kind != KIND_SHIP)
            ) {
                continue;
            }
            float distance = distanceSquare(bot, other);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = other;
            }
        }
        return nearest;
    }

    private void updateAsteroid(GameEntity asteroid, float dt) {
        asteroid.x += asteroid.vx * dt;
        asteroid.y += asteroid.vy * dt;
        if (asteroid.x < asteroid.radius || asteroid.x > WORLD_WIDTH - asteroid.radius) {
            asteroid.vx = -asteroid.vx;
            asteroid.x = clamp(asteroid.x, asteroid.radius, WORLD_WIDTH - asteroid.radius);
        }
        if (asteroid.y < asteroid.radius || asteroid.y > WORLD_HEIGHT - asteroid.radius) {
            asteroid.vy = -asteroid.vy;
            asteroid.y = clamp(asteroid.y, asteroid.radius, WORLD_HEIGHT - asteroid.radius);
        }
        asteroid.angle += asteroid.spin * dt;
        moveEntity(asteroid);
    }

    private void updateBullet(GameEntity bullet, float dt) {
        bullet.ttl -= dt;
        bullet.x += bullet.vx * dt;
        bullet.y += bullet.vy * dt;
        if (bullet.ttl <= 0
            || bullet.x < 0 || bullet.x > WORLD_WIDTH
            || bullet.y < 0 || bullet.y > WORLD_HEIGHT
        ) {
            removeEntity(bullet);
            return;
        }
        moveEntity(bullet);
    }

    private void updatePowerup(GameEntity powerup, float dt) {
        powerup.ttl -= dt;
        powerup.angle += powerup.spin * dt;
        if (powerup.ttl <= 0) {
            removeEntity(powerup);
        }
    }

    private void handleBulletCollisions(GameEntity bullet) {
        nearbyBuffer.clear();
        bullet.getNearbyPlayerNames(nearbyBuffer);
        for (String name : nearbyBuffer) {
            GameEntity other = entities.get(name);
            if (other == null || other == bullet || !isActive(other) || !isColliding(bullet, other)) {
                continue;
            }
            if (other.kind == KIND_ASTEROID) {
                removeEntity(bullet);
                damageAsteroid(other, bullet.owner);
                return;
            }
            if (other.kind == KIND_SHIP && other != bullet.owner) {
                removeEntity(bullet);
                Ship ship = (Ship) other;
                if (ship.invulnerableIn <= 0) {
                    damageShip(ship, BULLET_DAMAGE, bullet.owner);
                }
                return;
            }
        }
    }

    private void handleShipCollisions(Ship ship) {
        nearbyBuffer.clear();
        ship.getNearbyPlayerNames(nearbyBuffer);
        for (String name : nearbyBuffer) {
            GameEntity other = entities.get(name);
            if (other == null || other == ship || !isActive(other) || !isColliding(ship, other)) {
                continue;
            }
            if (other.kind == KIND_ASTEROID) {
                destroyAsteroid(other, ship);
                if (ship.invulnerableIn <= 0) {
                    damageShip(ship, 10 * other.level, null);
                }
            } else if (other.kind == KIND_POWERUP) {
                removeEntity(other);
                ship.hp = Math.min(SHIP_MAX_HP, ship.hp + POWERUP_HEAL);
                addEvent("p", other.x, other.y, other.radius);
            }
            if (ship.respawnIn > 0) {
                return;
            }
        }
    }

    private void damageAsteroid(GameEntity asteroid, Ship attacker) {
        asteroid.hp -= 1;
        if (asteroid.hp <= 0) {
            destroyAsteroid(asteroid, attacker);
        }
    }

    private void destroyAsteroid(GameEntity asteroid, Ship attacker) {
        removeEntity(asteroid);
        addEvent("x", asteroid.x, asteroid.y, asteroid.radius);
        if (attacker != null && !attacker.removed) {
            attacker.score += 10 * asteroid.level;
        }
        if (asteroid.level > 1) {
            for (int i = 0; i < 2; ++i) {
                spawnAsteroid(asteroid.level - 1, asteroid.x, asteroid.y);
            }
        }
        if (random.nextFloat() < POWERUP_DROP_RATE) {
            spawnPowerup(asteroid.x, asteroid.y);
        }
    }

    private void damageShip(Ship ship, int damage, Ship attacker) {
        ship.hp -= damage;
        if (ship.hp > 0) {
            return;
        }
        ship.hp = 0;
        ship.respawnIn = SHIP_RESPAWN_SECONDS;
        ship.vx = 0;
        ship.vy = 0;
        ship.deaths += 1;
        addEvent("x", ship.x, ship.y, 60);
        if (attacker != null && attacker != ship && !attacker.removed) {
            attacker.score += 50;
            attacker.kills += 1;
        }
    }

    private void respawnShip(Ship ship) {
        float[] position = randomSafePosition();
        ship.x = position[0];
        ship.y = position[1];
        ship.hp = SHIP_MAX_HP;
        ship.respawnIn = 0;
        ship.invulnerableIn = SHIP_INVULNERABLE_SECONDS;
        moveEntity(ship);
    }

    private void maintainAsteroids() {
        for (int i = 0; i < MAX_ASTEROID_SPAWN_PER_TICK && asteroidCount < ASTEROID_COUNT; ++i) {
            float x = 0;
            float y = 0;
            for (int k = 0; k < 10; ++k) {
                x = randomRange(100, WORLD_WIDTH - 100);
                y = randomRange(100, WORLD_HEIGHT - 100);
                if (isFarFromShips(x, y, 600)) {
                    break;
                }
            }
            spawnAsteroid(3, x, y);
        }
    }

    private boolean isFarFromShips(float x, float y, float distance) {
        for (Ship ship : ships) {
            float dx = ship.x - x;
            float dy = ship.y - y;
            if (dx * dx + dy * dy < distance * distance) {
                return false;
            }
        }
        return true;
    }

    private float[] randomSafePosition() {
        float x = 0;
        float y = 0;
        for (int i = 0; i < 20; ++i) {
            x = randomRange(200, WORLD_WIDTH - 200);
            y = randomRange(200, WORLD_HEIGHT - 200);
            boolean safe = true;
            for (GameEntity entity : entities.values()) {
                if (entity.kind == KIND_ASTEROID) {
                    float dx = entity.x - x;
                    float dy = entity.y - y;
                    if (dx * dx + dy * dy < 250 * 250) {
                        safe = false;
                        break;
                    }
                }
            }
            if (safe) {
                break;
            }
        }
        return new float[] {x, y};
    }

    private Ship createShip(EzyUser user, String displayName) {
        Ship ship = new Ship("s" + idGenerator.incrementAndGet(), user, displayName);
        float[] position = randomSafePosition();
        ship.x = position[0];
        ship.y = position[1];
        ship.radius = SHIP_RADIUS;
        ship.hp = SHIP_MAX_HP;
        ship.invulnerableIn = SHIP_INVULNERABLE_SECONDS;
        ships.add(ship);
        addEntity(ship);
        return ship;
    }

    private void spawnAsteroid(int level, float x, float y) {
        GameEntity asteroid = new GameEntity("a" + idGenerator.incrementAndGet(), KIND_ASTEROID);
        asteroid.level = level;
        asteroid.hp = level;
        asteroid.radius = level == 3 ? 56 : (level == 2 ? 36 : 20);
        asteroid.x = clamp(x, asteroid.radius, WORLD_WIDTH - asteroid.radius);
        asteroid.y = clamp(y, asteroid.radius, WORLD_HEIGHT - asteroid.radius);
        float speed = randomRange(30, 70) * (4 - level);
        float direction = randomRange(0, (float) (Math.PI * 2));
        asteroid.vx = (float) Math.cos(direction) * speed;
        asteroid.vy = (float) Math.sin(direction) * speed;
        asteroid.angle = randomRange(0, (float) (Math.PI * 2));
        asteroid.spin = randomRange(-1.5f, 1.5f);
        asteroidCount += 1;
        addEntity(asteroid);
    }

    private void spawnBullet(Ship ship) {
        GameEntity bullet = new GameEntity("b" + idGenerator.incrementAndGet(), KIND_BULLET);
        float cos = (float) Math.cos(ship.angle);
        float sin = (float) Math.sin(ship.angle);
        bullet.owner = ship;
        bullet.radius = BULLET_RADIUS;
        bullet.ttl = BULLET_TTL_SECONDS;
        bullet.angle = ship.angle;
        bullet.x = clamp(ship.x + cos * (SHIP_RADIUS + 10), 0, WORLD_WIDTH);
        bullet.y = clamp(ship.y + sin * (SHIP_RADIUS + 10), 0, WORLD_HEIGHT);
        bullet.vx = cos * BULLET_SPEED + ship.vx * 0.3f;
        bullet.vy = sin * BULLET_SPEED + ship.vy * 0.3f;
        addEntity(bullet);
        addEvent("f", ship.x, ship.y, 0);
    }

    private void spawnPowerup(float x, float y) {
        GameEntity powerup = new GameEntity("p" + idGenerator.incrementAndGet(), KIND_POWERUP);
        powerup.radius = POWERUP_RADIUS;
        powerup.ttl = POWERUP_TTL_SECONDS;
        powerup.spin = 2;
        powerup.x = clamp(x, POWERUP_RADIUS, WORLD_WIDTH - POWERUP_RADIUS);
        powerup.y = clamp(y, POWERUP_RADIUS, WORLD_HEIGHT - POWERUP_RADIUS);
        addEntity(powerup);
    }

    private void addEntity(GameEntity entity) {
        entity.setPosition(entity.x, entity.y, 0);
        entities.put(entity.getName(), entity);
        room.addPlayer(entity);
    }

    private void removeEntity(GameEntity entity) {
        if (entity.removed) {
            return;
        }
        entity.removed = true;
        if (entity.kind == KIND_ASTEROID) {
            asteroidCount -= 1;
        }
        removedEntities.add(entity);
    }

    private void moveEntity(GameEntity entity) {
        positionBuffer.set(entity.x, entity.y, 0);
        room.setPlayerPosition(entity, positionBuffer);
    }

    private void addEvent(String type, float x, float y, float size) {
        events.add(new GameEvent(type, x, y, size));
    }

    private boolean isActive(GameEntity entity) {
        if (entity.removed) {
            return false;
        }
        return entity.kind != KIND_SHIP || ((Ship) entity).respawnIn <= 0;
    }

    private void broadcastState() {
        EzyArray leaderboard = buildLeaderboard();
        for (Ship ship : ships) {
            if (ship.user != null) {
                send(ship.user, "state", buildState(ship, leaderboard));
            }
        }
    }

    private EzyArray buildLeaderboard() {
        List<Ship> ranking = new ArrayList<>(ships);
        ranking.sort((a, b) -> Integer.compare(b.score, a.score));
        EzyArray leaderboard = EzyEntityFactory.newArray();
        for (int i = 0; i < Math.min(5, ranking.size()); ++i) {
            Ship ship = ranking.get(i);
            leaderboard.add(
                EzyEntityFactory.newArrayBuilder()
                    .append(ship.displayName)
                    .append(ship.score)
                    .append(ship.kills)
                    .append(ship.deaths)
                    .build()
            );
        }
        return leaderboard;
    }

    private EzyObject buildState(Ship ship, EzyArray leaderboard) {
        nearbyBuffer.clear();
        ship.getNearbyPlayerNames(nearbyBuffer);
        EzyArray entityArray = EzyEntityFactory.newArray();
        for (String name : nearbyBuffer) {
            GameEntity other = entities.get(name);
            if (other == null || other == ship || !isActive(other)) {
                continue;
            }
            EzyArray item = EzyEntityFactory.newArray();
            item.add(
                other.getName(),
                other.kind,
                Math.round(other.x),
                Math.round(other.y),
                Math.round(other.angle * 1000),
                Math.round(other.radius),
                other.hp
            );
            if (other.kind == KIND_SHIP) {
                Ship otherShip = (Ship) other;
                item.add(otherShip.displayName, otherShip.invulnerableIn > 0 ? 1 : 0);
            }
            entityArray.add(item);
        }
        EzyArray eventArray = EzyEntityFactory.newArray();
        for (GameEvent event : events) {
            float dx = event.x - ship.x;
            float dy = event.y - ship.y;
            if (dx * dx + dy * dy <= EVENT_DISTANCE * EVENT_DISTANCE) {
                EzyArray item = EzyEntityFactory.newArray();
                item.add(
                    event.type,
                    Math.round(event.x),
                    Math.round(event.y),
                    Math.round(event.size)
                );
                eventArray.add(item);
            }
        }
        EzyObject me = EzyEntityFactory.newObjectBuilder()
            .append("x", Math.round(ship.x))
            .append("y", Math.round(ship.y))
            .append("a", Math.round(ship.angle * 1000))
            .append("hp", ship.hp)
            .append("score", ship.score)
            .append("inv", ship.invulnerableIn > 0 ? 1 : 0)
            .append("respawn", Math.max(0, Math.round(ship.respawnIn * 1000)))
            .append("cx", toCellIndex(ship.x, WORLD_WIDTH))
            .append("cy", toCellIndex(ship.y, WORLD_HEIGHT))
            .append("nearby", Math.max(0, nearbyBuffer.size() - 1))
            .build();
        return EzyEntityFactory.newObjectBuilder()
            .append("tick", tick)
            .append("me", me)
            .append("total", entities.size())
            .append("ships", ships.size())
            .append("es", entityArray)
            .append("ev", eventArray)
            .append("lb", leaderboard)
            .build();
    }

    private int toCellIndex(float value, float max) {
        int maxCell = Math.max(1, (int) Math.ceil(max / CELL_SIZE));
        return Math.min(maxCell - 1, (int) (value / CELL_SIZE));
    }

    private void serveFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/".equals(path)) {
            path = "/index.html";
        }
        byte[] body = path.contains("..") ? null : readWebResource(path.substring(1));
        try (OutputStream outputStream = exchange.getResponseBody()) {
            if (body == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            exchange.getResponseHeaders().set("Content-Type", contentTypeOf(path));
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, body.length);
            outputStream.write(body);
        }
    }

    private static byte[] readWebResource(String path) throws IOException {
        File file = new File(WEB_ROOT_DIR, path);
        if (file.isFile()) {
            return Files.readAllBytes(file.toPath());
        }
        try (InputStream inputStream = SpaceShooterServer.class
            .getClassLoader()
            .getResourceAsStream(WEB_ROOT + "/" + path)
        ) {
            if (inputStream == null) {
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        }
    }

    private static String contentTypeOf(String path) {
        if (path.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (path.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (path.endsWith(".png")) {
            return "image/png";
        }
        if (path.endsWith(".wav")) {
            return "audio/wav";
        }
        return "application/octet-stream";
    }

    private float randomRange(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float distanceSquare(GameEntity a, GameEntity b) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return dx * dx + dy * dy;
    }

    private static boolean isColliding(GameEntity a, GameEntity b) {
        float radius = a.radius + b.radius;
        return distanceSquare(a, b) <= radius * radius;
    }

    public static class SpaceShooterAppEntryLoader extends EzyAbstractAppEntryLoader {

        private final SpaceShooterServer server;

        public SpaceShooterAppEntryLoader(SpaceShooterServer server) {
            this.server = server;
        }

        @Override
        public EzyAppEntry load() {
            return new SpaceShooterAppEntry(server);
        }
    }

    public static class SpaceShooterAppEntry extends EzyAbstractAppEntry {

        private final SpaceShooterServer server;

        public SpaceShooterAppEntry(SpaceShooterServer server) {
            this.server = server;
        }

        @Override
        public void config(EzyAppContext context) {
            server.configApp(context);
        }
    }

    private static class GameEntity extends MMOPlayer {
        final int kind;
        float x;
        float y;
        float vx;
        float vy;
        float angle;
        float spin;
        float radius;
        float ttl;
        int hp;
        int level;
        Ship owner;
        boolean removed;

        GameEntity(String name, int kind) {
            super(name);
            this.kind = kind;
        }
    }

    private static class Ship extends GameEntity {
        final EzyUser user;
        final String displayName;
        volatile boolean up;
        volatile boolean down;
        volatile boolean left;
        volatile boolean right;
        volatile boolean fire;
        volatile float aim;
        float fireCooldown;
        float invulnerableIn;
        float respawnIn;
        float thinkIn;
        int score;
        int kills;
        int deaths;

        Ship(String name, EzyUser user, String displayName) {
            super(name, KIND_SHIP);
            this.user = user;
            this.displayName = displayName;
        }
    }

    private static class GameEvent {
        final String type;
        final float x;
        final float y;
        final float size;

        GameEvent(String type, float x, float y, float size) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.size = size;
        }
    }
}
