(function () {
    'use strict';

    var ZONE_NAME = 'space-shooter';
    var APP_NAME = 'space-shooter';
    var WEB_SOCKET_URL = 'ws://' + (location.hostname || 'localhost') + ':2208/ws';

    var KIND_SHIP = 1;
    var KIND_ASTEROID = 2;
    var KIND_POWERUP = 4;

    var ALTITUDE_LIFT = 0.35;
    var ALTITUDE_DIM_DISTANCE = 70;

    var canvas = document.getElementById('game');
    var ctx = canvas.getContext('2d');
    var loginPanel = document.getElementById('login');
    var nameInput = document.getElementById('player-name');
    var playButton = document.getElementById('play-button');
    var loginStatus = document.getElementById('login-status');

    var images = {};
    var sounds = {};
    var music = null;
    var musicEnabled = false;

    var config = null;
    var app = null;
    var joined = false;
    var me = null;
    var myId = null;
    var entities = {};
    var bullets = {};
    var particles = [];
    var leaderboard = [];
    var stats = {total: 0, ships: 0, tick: 0, nearby: 0, cx: 0, cy: 0, cz: 0};
    var camera = {x: 0, y: 0};
    var showGrid = true;
    var stars = [];
    var stateCount = 0;
    var statesPerSecond = 0;
    var fps = 0;
    var frameCount = 0;
    var lastSecond = performance.now();

    var input = {up: false, down: false, left: false, right: false, rise: false, sink: false, fire: false, aim: 0};
    var mouse = {x: 0, y: 0};
    var lastSentInput = '';
    var lastSentAt = 0;

    function loadImage(name) {
        var image = new Image();
        image.src = 'assets/' + name;
        images[name] = image;
    }

    function loadSound(name, volume) {
        var audio = new Audio('assets/' + name);
        audio.volume = volume;
        audio.preload = 'auto';
        sounds[name] = audio;
    }

    [
        'SpaceFlier_sm_1.png',
        'enemy_spaceship.png',
        'asteroid.png',
        'laserbeam_blue.png',
        'laserbeam_red.png',
        'powerup.png',
        'bg_galaxy.png',
        'bg_planetsunrise.png',
        'bg_spacialanomaly.png',
        'bg_spacialanomaly2.png',
        'bg_front_spacedust.png'
    ].forEach(loadImage);
    loadSound('laser_ship.wav', 0.25);
    loadSound('explosion_large.wav', 0.5);

    function playSound(name, x, y) {
        var base = sounds[name];
        if (!base || !me) {
            return;
        }
        var dx = x - me.x;
        var dy = y - me.y;
        var distance = Math.sqrt(dx * dx + dy * dy);
        var volume = Math.max(0, 1 - distance / 1400) * base.volume;
        if (volume <= 0.01) {
            return;
        }
        var audio = base.cloneNode();
        audio.volume = volume;
        audio.play().catch(function () {});
    }

    function toggleMusic() {
        musicEnabled = !musicEnabled;
        if (!music) {
            music = new Audio('assets/SpaceGame.wav');
            music.loop = true;
            music.volume = 0.35;
        }
        if (musicEnabled) {
            music.play().catch(function () {});
        } else {
            music.pause();
        }
    }

    function createStars() {
        stars = [];
        for (var layer = 0; layer < 3; ++layer) {
            for (var i = 0; i < 120; ++i) {
                stars.push({
                    x: Math.random() * 2048,
                    y: Math.random() * 2048,
                    size: 0.5 + layer * 0.6 + Math.random() * 0.6,
                    parallax: 0.15 + layer * 0.25,
                    alpha: 0.35 + Math.random() * 0.5
                });
            }
        }
    }

    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }

    function sanitizeName(value) {
        var name = (value || '').replace(/[^A-Za-z0-9_]/g, '');
        if (name.length === 0) {
            name = 'pilot';
        }
        name = name.substring(0, 16);
        return name + '_' + Math.floor(100 + Math.random() * 900);
    }

    function connect() {
        var username = sanitizeName(nameInput.value);
        playButton.disabled = true;
        loginStatus.textContent = 'connecting to ' + WEB_SOCKET_URL + ' ...';

        EzyLogger.debug = false;
        var clientConfig = new EzyClientConfig();
        clientConfig.zoneName = ZONE_NAME;
        clientConfig.reconnect.enable = false;
        var client = EzyClients.getInstance().newDefaultClient(clientConfig);

        var handshakeHandler = new EzyHandshakeHandler();
        handshakeHandler.getLoginRequest = function () {
            return [ZONE_NAME, username, '', []];
        };

        var loginSuccessHandler = new EzyLoginSuccessHandler();
        loginSuccessHandler.handleLoginSuccess = function () {
            loginStatus.textContent = 'logged in, accessing app ...';
            this.client.sendRequest(EzyCommand.APP_ACCESS, [APP_NAME, []]);
        };

        var loginErrorHandler = new EzyLoginErrorHandler();
        loginErrorHandler.handleLoginError = function (data) {
            loginStatus.textContent = 'login error: ' + JSON.stringify(data);
            playButton.disabled = false;
        };

        var appAccessHandler = new EzyAppAccessHandler();
        appAccessHandler.postHandle = function (accessedApp) {
            app = accessedApp;
            app.send('join', {});
        };

        var disconnectionHandler = new EzyDisconnectionHandler();
        disconnectionHandler.preHandle = function () {
            joined = false;
            app = null;
            loginPanel.style.display = 'flex';
            playButton.disabled = false;
            loginStatus.textContent = 'disconnected';
        };

        var connectionFailureHandler = new EzyConnectionFailureHandler();
        connectionFailureHandler.onConnectionFailed = function () {
            playButton.disabled = false;
            loginStatus.textContent = 'can not connect to ' + WEB_SOCKET_URL;
        };

        var setup = client.setup;
        setup.addEventHandler(EzyEventType.CONNECTION_FAILURE, connectionFailureHandler);
        setup.addEventHandler(EzyEventType.DISCONNECTION, disconnectionHandler);
        setup.addDataHandler(EzyCommand.HANDSHAKE, handshakeHandler);
        setup.addDataHandler(EzyCommand.LOGIN, loginSuccessHandler);
        setup.addDataHandler(EzyCommand.LOGIN_ERROR, loginErrorHandler);
        setup.addDataHandler(EzyCommand.APP_ACCESS, appAccessHandler);

        var setupApp = setup.setupApp(APP_NAME);
        setupApp.addDataHandler('joined', onJoined);
        setupApp.addDataHandler('state', onState);

        client.connect(WEB_SOCKET_URL);
    }

    function onJoined(accessedApp, data) {
        config = data;
        myId = data.id;
        me = null;
        joined = true;
        entities = {};
        bullets = {};
        particles = [];
        loginPanel.style.display = 'none';
        canvas.focus();
    }

    function onState(accessedApp, data) {
        stateCount += 1;
        var m = data.me;
        if (!me) {
            me = {x: m.x, y: m.y, z: m.z, a: m.a / 1000};
            camera.x = m.x;
            camera.y = m.y;
        }
        me.tx = m.x;
        me.ty = m.y;
        me.tz = m.z;
        me.ta = m.a / 1000;
        me.hp = m.hp;
        me.score = m.score;
        me.inv = m.inv === 1;
        var respawned = me.respawn > 0 && m.respawn <= 0;
        me.respawn = m.respawn;
        if (m.respawn > 0 || respawned) {
            me.x = m.x;
            me.y = m.y;
            me.z = m.z;
            me.a = me.ta;
        }
        if (respawned) {
            camera.x = m.x;
            camera.y = m.y;
        }
        stats.tick = data.tick;
        stats.total = data.total;
        stats.ships = data.ships;
        stats.nearby = m.nearby;
        stats.cx = m.cx;
        stats.cy = m.cy;
        stats.cz = m.cz;
        leaderboard = data.lb;

        var seen = {};
        var list = data.es;
        for (var i = 0; i < list.length; ++i) {
            var item = list[i];
            var id = item[0];
            seen[id] = true;
            var entity = entities[id];
            if (!entity) {
                entity = {
                    id: id,
                    kind: item[1],
                    x: item[2],
                    y: item[3],
                    z: item[4],
                    a: item[5] / 1000
                };
                entities[id] = entity;
            }
            entity.tx = item[2];
            entity.ty = item[3];
            entity.tz = item[4];
            entity.ta = item[5] / 1000;
            entity.r = item[6];
            entity.hp = item[7];
            if (entity.kind === KIND_SHIP) {
                entity.label = item[8];
                entity.inv = item[9] === 1;
            }
        }
        for (var key in entities) {
            if (!seen[key]) {
                delete entities[key];
            }
        }

        var laserPlayed = 0;
        var events = data.ev;
        for (var k = 0; k < events.length; ++k) {
            var event = events[k];
            if (event[0] === 'x') {
                spawnExplosion(event[1], event[2], event[3], event[4]);
                playSound('explosion_large.wav', event[1], event[2]);
            } else if (event[0] === 'p') {
                spawnPickup(event[1], event[2], event[3]);
            } else if (event[0] === 'b') {
                spawnBullet(event, data.tick);
                if (laserPlayed < 3) {
                    laserPlayed += 1;
                    playSound('laser_ship.wav', event[1], event[2]);
                }
            } else if (event[0] === 'h') {
                delete bullets[event[4]];
                spawnHit(event[1], event[2], event[3]);
            }
        }
    }

    function spawnExplosion(x, y, z, size) {
        var count = Math.min(40, 10 + size / 2);
        for (var i = 0; i < count; ++i) {
            var angle = Math.random() * Math.PI * 2;
            var speed = 40 + Math.random() * (size * 4);
            particles.push({
                x: x,
                y: y,
                z: z,
                vx: Math.cos(angle) * speed,
                vy: Math.sin(angle) * speed,
                life: 0.5 + Math.random() * 0.6,
                maxLife: 1.1,
                size: 2 + Math.random() * 3,
                color: Math.random() < 0.5 ? '255,170,60' : '255,230,150'
            });
        }
        particles.push({x: x, y: y, z: z, vx: 0, vy: 0, life: 0.25, maxLife: 0.25, size: size * 1.4, color: '255,240,200', flash: true});
    }

    function spawnBullet(event, stateTick) {
        var vx = event[5];
        var vy = event[6];
        var age = Math.max(0, stateTick - event[9] + 1) * config.tickMillis / 1000;
        var ttl = event[7] / 1000 - age;
        if (ttl <= 0) {
            return;
        }
        bullets[event[4]] = {
            x: event[1] + vx * age,
            y: event[2] + vy * age,
            z: event[3],
            vx: vx,
            vy: vy,
            a: Math.atan2(vy, vx),
            ttl: ttl,
            mine: event[8] === myId
        };
    }

    function spawnHit(x, y, z) {
        for (var i = 0; i < 6; ++i) {
            var angle = Math.random() * Math.PI * 2;
            particles.push({
                x: x,
                y: y,
                z: z,
                vx: Math.cos(angle) * 120,
                vy: Math.sin(angle) * 120,
                life: 0.2,
                maxLife: 0.2,
                size: 2,
                color: '255,220,160'
            });
        }
    }

    function spawnPickup(x, y, z) {
        for (var i = 0; i < 16; ++i) {
            var angle = (i / 16) * Math.PI * 2;
            particles.push({
                x: x,
                y: y,
                z: z,
                vx: Math.cos(angle) * 160,
                vy: Math.sin(angle) * 160,
                life: 0.5,
                maxLife: 0.5,
                size: 3,
                color: '120,255,160'
            });
        }
    }

    function lerpAngle(from, to, t) {
        var diff = to - from;
        while (diff > Math.PI) {
            diff -= Math.PI * 2;
        }
        while (diff < -Math.PI) {
            diff += Math.PI * 2;
        }
        return from + diff * t;
    }

    function updateInterpolation(dt) {
        var t = Math.min(1, dt * 12);
        if (me && me.tx !== undefined) {
            me.x += (me.tx - me.x) * t;
            me.y += (me.ty - me.y) * t;
            me.z += (me.tz - me.z) * t;
            me.a = lerpAngle(me.a, me.ta, t);
            camera.x += (me.x - camera.x) * Math.min(1, dt * 10);
            camera.y += (me.y - camera.y) * Math.min(1, dt * 10);
        }
        for (var key in entities) {
            var entity = entities[key];
            entity.x += (entity.tx - entity.x) * t;
            entity.y += (entity.ty - entity.y) * t;
            entity.z += (entity.tz - entity.z) * t;
            entity.a = lerpAngle(entity.a, entity.ta, t);
        }
        for (var id in bullets) {
            var bullet = bullets[id];
            bullet.ttl -= dt;
            if (bullet.ttl <= 0) {
                delete bullets[id];
                continue;
            }
            bullet.x += bullet.vx * dt;
            bullet.y += bullet.vy * dt;
        }
        for (var i = particles.length - 1; i >= 0; --i) {
            var particle = particles[i];
            particle.life -= dt;
            if (particle.life <= 0) {
                particles.splice(i, 1);
                continue;
            }
            particle.x += particle.vx * dt;
            particle.y += particle.vy * dt;
            particle.vx *= 0.96;
            particle.vy *= 0.96;
        }
    }

    function worldToScreenX(x) {
        return x - camera.x + canvas.width / 2;
    }

    function worldToScreenY(y) {
        return y - camera.y + canvas.height / 2;
    }

    function elevatedScreenY(y, z) {
        return worldToScreenY(y) - z * ALTITUDE_LIFT;
    }

    function altitudeScale(z) {
        var depth = config ? config.worldDepth : 1;
        return 0.8 + 0.4 * (z / depth);
    }

    function altitudeAlpha(z) {
        if (!me) {
            return 1;
        }
        return Math.abs(z - me.z) > ALTITUDE_DIM_DISTANCE ? 0.4 : 1;
    }

    function drawBackground() {
        ctx.fillStyle = '#02030a';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        for (var i = 0; i < stars.length; ++i) {
            var star = stars[i];
            var sx = ((star.x - camera.x * star.parallax) % 2048 + 2048) % 2048;
            var sy = ((star.y - camera.y * star.parallax) % 2048 + 2048) % 2048;
            for (var ox = sx; ox < canvas.width; ox += 2048) {
                for (var oy = sy; oy < canvas.height; oy += 2048) {
                    ctx.globalAlpha = star.alpha;
                    ctx.fillStyle = '#ffffff';
                    ctx.fillRect(ox, oy, star.size, star.size);
                }
            }
        }
        ctx.globalAlpha = 1;

        drawParallaxImage('bg_galaxy.png', 900, 700, 0.3, 1.2);
        drawParallaxImage('bg_planetsunrise.png', 2600, 1400, 0.35, 1);
        drawParallaxImage('bg_spacialanomaly.png', 1500, 2600, 0.4, 1.2);
        drawParallaxImage('bg_spacialanomaly2.png', 3200, 3100, 0.4, 1);
        drawParallaxImage('bg_galaxy.png', 600, 3300, 0.3, 0.8);

        var dust = images['bg_front_spacedust.png'];
        if (dust.complete && dust.width > 0) {
            var parallax = 0.8;
            var w = dust.width;
            var h = dust.height;
            var startX = -((camera.x * parallax) % w + w) % w;
            var startY = -((camera.y * parallax) % (h * 2) + h * 2) % (h * 2);
            ctx.globalAlpha = 0.18;
            for (var dx = startX; dx < canvas.width; dx += w) {
                for (var dy = startY; dy < canvas.height; dy += h * 2) {
                    ctx.drawImage(dust, dx, dy);
                }
            }
            ctx.globalAlpha = 1;
        }
    }

    function drawParallaxImage(name, worldX, worldY, parallax, scale) {
        var image = images[name];
        if (!image.complete || image.width === 0) {
            return;
        }
        var x = worldX * parallax - camera.x * parallax + canvas.width / 2;
        var y = worldY * parallax - camera.y * parallax + canvas.height / 2;
        var w = image.width * scale;
        var h = image.height * scale;
        if (x + w < 0 || y + h < 0 || x > canvas.width || y > canvas.height) {
            return;
        }
        ctx.globalAlpha = 0.85;
        ctx.drawImage(image, x - w / 2, y - h / 2, w, h);
        ctx.globalAlpha = 1;
    }

    function drawGrid() {
        if (!config) {
            return;
        }
        var cellSize = config.cellSize;
        var maxCellX = Math.ceil(config.worldWidth / cellSize);
        var maxCellY = Math.ceil(config.worldHeight / cellSize);

        if (showGrid) {
            var startX = Math.max(0, Math.floor((camera.x - canvas.width / 2) / cellSize));
            var endX = Math.min(maxCellX, Math.ceil((camera.x + canvas.width / 2) / cellSize));
            var startY = Math.max(0, Math.floor((camera.y - canvas.height / 2) / cellSize));
            var endY = Math.min(maxCellY, Math.ceil((camera.y + canvas.height / 2) / cellSize));

            ctx.strokeStyle = 'rgba(80, 140, 255, 0.18)';
            ctx.lineWidth = 1;
            ctx.beginPath();
            for (var gx = startX; gx <= endX; ++gx) {
                var lineX = worldToScreenX(gx * cellSize);
                ctx.moveTo(lineX, worldToScreenY(startY * cellSize));
                ctx.lineTo(lineX, worldToScreenY(endY * cellSize));
            }
            for (var gy = startY; gy <= endY; ++gy) {
                var lineY = worldToScreenY(gy * cellSize);
                ctx.moveTo(worldToScreenX(startX * cellSize), lineY);
                ctx.lineTo(worldToScreenX(endX * cellSize), lineY);
            }
            ctx.stroke();

            ctx.fillStyle = 'rgba(255, 220, 90, 0.12)';
            ctx.fillRect(worldToScreenX(stats.cx * cellSize), worldToScreenY(stats.cy * cellSize), cellSize, cellSize);

            ctx.fillStyle = 'rgba(120, 170, 255, 0.35)';
            ctx.font = '10px Menlo, monospace';
            for (var lx = startX; lx < endX; ++lx) {
                for (var ly = startY; ly < endY; ++ly) {
                    ctx.fillText(lx + ',' + ly, worldToScreenX(lx * cellSize) + 4, worldToScreenY(ly * cellSize) + 12);
                }
            }

            if (me) {
                var centerX = worldToScreenX(me.x);
                var centerY = worldToScreenY(me.y);
                var radius = config.interestRadius;
                ctx.fillStyle = 'rgba(0, 0, 0, 0.35)';
                ctx.beginPath();
                ctx.rect(0, 0, canvas.width, canvas.height);
                ctx.arc(centerX, centerY, radius, 0, Math.PI * 2, true);
                ctx.fill('evenodd');
                ctx.strokeStyle = 'rgba(90, 200, 255, 0.6)';
                ctx.lineWidth = 2;
                ctx.beginPath();
                ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
                ctx.stroke();
            }
        }

        ctx.strokeStyle = 'rgba(255, 70, 70, 0.8)';
        ctx.lineWidth = 3;
        ctx.strokeRect(worldToScreenX(0), worldToScreenY(0), config.worldWidth, config.worldHeight);
    }

    function drawShadow(x, y, radius) {
        ctx.fillStyle = 'rgba(0, 0, 0, 0.35)';
        ctx.beginPath();
        ctx.ellipse(worldToScreenX(x), worldToScreenY(y), radius, radius * 0.45, 0, 0, Math.PI * 2);
        ctx.fill();
    }

    function drawSprite(name, x, y, z, width, height, angle) {
        var image = images[name];
        if (!image.complete || image.width === 0) {
            return;
        }
        var scale = altitudeScale(z);
        ctx.save();
        ctx.translate(worldToScreenX(x), elevatedScreenY(y, z));
        ctx.rotate(angle);
        ctx.drawImage(image, -width * scale / 2, -height * scale / 2, width * scale, height * scale);
        ctx.restore();
    }

    function isOnScreen(x, y, margin) {
        var sx = worldToScreenX(x);
        var sy = worldToScreenY(y);
        return sx > -margin && sy > -margin && sx < canvas.width + margin && sy < canvas.height + margin;
    }

    function drawShipInfo(x, y, z, label, hp, maxHp, color) {
        var sx = worldToScreenX(x);
        var sy = elevatedScreenY(y, z);
        ctx.font = '12px Menlo, monospace';
        ctx.textAlign = 'center';
        ctx.fillStyle = color;
        ctx.fillText(label + ' [' + Math.round(z) + ']', sx, sy - 42);
        ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';
        ctx.fillRect(sx - 25, sy - 36, 50, 5);
        ctx.fillStyle = hp > maxHp * 0.3 ? '#4cff7a' : '#ff5050';
        ctx.fillRect(sx - 25, sy - 36, 50 * Math.max(0, hp) / maxHp, 5);
        ctx.textAlign = 'left';
    }

    function drawEntity(entity, now, maxHp) {
        if (entity.kind === KIND_ASTEROID) {
            var size = entity.r * 2.3;
            drawSprite('asteroid.png', entity.x, entity.y, entity.z, size, size, entity.a);
        } else if (entity.kind === KIND_POWERUP) {
            var pulse = 1 + Math.sin(now / 150) * 0.12;
            drawSprite('powerup.png', entity.x, entity.y, entity.z, 58 * pulse, 32 * pulse, Math.sin(entity.a) * 0.3);
        } else if (entity.kind === KIND_SHIP) {
            if (entity.inv && Math.floor(now / 120) % 2 === 0) {
                ctx.globalAlpha *= 0.35;
            }
            drawSprite('enemy_spaceship.png', entity.x, entity.y, entity.z, 88, 38, Math.sin(now / 300 + entity.x) * 0.08);
            ctx.strokeStyle = 'rgba(255, 90, 90, 0.8)';
            ctx.lineWidth = 2;
            ctx.beginPath();
            var ax = worldToScreenX(entity.x) + Math.cos(entity.a) * 34;
            var ay = elevatedScreenY(entity.y, entity.z) + Math.sin(entity.a) * 34;
            ctx.moveTo(ax, ay);
            ctx.lineTo(ax - Math.cos(entity.a - 0.5) * 10, ay - Math.sin(entity.a - 0.5) * 10);
            ctx.moveTo(ax, ay);
            ctx.lineTo(ax - Math.cos(entity.a + 0.5) * 10, ay - Math.sin(entity.a + 0.5) * 10);
            ctx.stroke();
            drawShipInfo(entity.x, entity.y, entity.z, entity.label, entity.hp, maxHp, '#ff9a9a');
        } else if (entity.kind === 'me') {
            if (me.inv && Math.floor(now / 120) % 2 === 0) {
                ctx.globalAlpha *= 0.35;
            }
            drawSprite('SpaceFlier_sm_1.png', me.x, me.y, me.z, 78, 53, me.a);
            drawShipInfo(me.x, me.y, me.z, config ? config.name : '', me.hp, maxHp, '#9fd1ff');
        } else if (entity.kind === 'bullet') {
            if (entity.mine) {
                drawSprite('laserbeam_blue.png', entity.x, entity.y, entity.z, 36, 9, entity.a);
            } else {
                drawSprite('laserbeam_red.png', entity.x, entity.y, entity.z, 30, 12, entity.a);
            }
        }
    }

    function drawEntities(now) {
        var maxHp = config ? config.maxHp : 100;
        var list = [];
        for (var key in entities) {
            var entity = entities[key];
            if (isOnScreen(entity.x, entity.y, 160)) {
                list.push(entity);
            }
        }
        for (var id in bullets) {
            var bullet = bullets[id];
            if (isOnScreen(bullet.x, bullet.y, 60)) {
                bullet.kind = 'bullet';
                list.push(bullet);
            }
        }
        if (me && me.respawn <= 0) {
            list.push({kind: 'me', x: me.x, y: me.y, z: me.z});
        }
        list.sort(function (a, b) {
            return a.z - b.z;
        });

        for (var s = 0; s < list.length; ++s) {
            var shadowEntity = list[s];
            if (shadowEntity.kind !== 'bullet') {
                var shadowRadius = shadowEntity.kind === 'me' || shadowEntity.kind === KIND_SHIP
                    ? 26
                    : (shadowEntity.r || 20);
                drawShadow(shadowEntity.x, shadowEntity.y, shadowRadius);
            }
        }

        for (var i = 0; i < list.length; ++i) {
            var item = list[i];
            ctx.globalAlpha = item.kind === 'me' ? 1 : altitudeAlpha(item.z);
            drawEntity(item, now, maxHp);
            ctx.globalAlpha = 1;
        }

        for (var p = 0; p < particles.length; ++p) {
            var particle = particles[p];
            var alpha = particle.life / particle.maxLife;
            var px = worldToScreenX(particle.x);
            var py = elevatedScreenY(particle.y, particle.z);
            if (particle.flash) {
                var gradient = ctx.createRadialGradient(px, py, 0, px, py, particle.size);
                gradient.addColorStop(0, 'rgba(' + particle.color + ',' + alpha + ')');
                gradient.addColorStop(1, 'rgba(' + particle.color + ',0)');
                ctx.fillStyle = gradient;
                ctx.beginPath();
                ctx.arc(px, py, particle.size, 0, Math.PI * 2);
                ctx.fill();
            } else {
                ctx.fillStyle = 'rgba(' + particle.color + ',' + alpha + ')';
                ctx.fillRect(px - particle.size / 2, py - particle.size / 2, particle.size, particle.size);
            }
        }
    }

    function drawAltitudeBar() {
        var height = 200;
        var x = 24;
        var y = canvas.height - height - 50;
        ctx.fillStyle = 'rgba(5, 10, 30, 0.7)';
        ctx.fillRect(x - 10, y - 24, 64, height + 44);
        ctx.strokeStyle = 'rgba(120, 160, 255, 0.6)';
        ctx.lineWidth = 1;
        ctx.strokeRect(x, y, 12, height);
        var depth = config.worldDepth;
        for (var key in entities) {
            var entity = entities[key];
            if (entity.kind === KIND_SHIP) {
                ctx.fillStyle = 'rgba(255, 96, 96, 0.8)';
                ctx.fillRect(x + 14, y + height - entity.z / depth * height - 1, 6, 2);
            }
        }
        var markerY = y + height - me.z / depth * height;
        ctx.fillStyle = '#9fd1ff';
        ctx.fillRect(x - 3, markerY - 2, 18, 4);
        ctx.font = '11px Menlo, monospace';
        ctx.fillStyle = '#cfe0ff';
        ctx.fillText('ALT', x - 4, y - 8);
        ctx.fillText(String(Math.round(me.z)), x + 18, markerY + 4);
        ctx.fillText('Q/E', x - 4, y + height + 16);
    }

    function drawHud() {
        if (!me || !config) {
            return;
        }
        var rendered = Object.keys(entities).length;
        var lines = [
            'SCORE    ' + me.score,
            'HP       ' + Math.max(0, me.hp) + ' / ' + config.maxHp,
            'POSITION ' + Math.round(me.tx) + ', ' + Math.round(me.ty) + ', ' + Math.round(me.tz),
            'CELL     ' + stats.cx + ', ' + stats.cy + ', ' + stats.cz + '  (size ' + config.cellSize + ', radius ' + config.interestRadius + ')',
            'NEARBY   ' + stats.nearby + '  (rendered ' + rendered + ', bullets ' + Object.keys(bullets).length + ')',
            'WORLD    ' + stats.total + ' entities, ' + stats.ships + ' ships',
            'TICK     ' + stats.tick + '  (' + statesPerSecond + ' states/s, ' + fps + ' fps)'
        ];
        ctx.fillStyle = 'rgba(5, 10, 30, 0.7)';
        ctx.fillRect(10, 10, 480, lines.length * 18 + 14);
        ctx.font = '13px Menlo, monospace';
        ctx.fillStyle = '#cfe0ff';
        for (var i = 0; i < lines.length; ++i) {
            ctx.fillText(lines[i], 20, 30 + i * 18);
        }

        var boardWidth = 260;
        var boardX = canvas.width - boardWidth - 10;
        ctx.fillStyle = 'rgba(5, 10, 30, 0.7)';
        ctx.fillRect(boardX, 10, boardWidth, leaderboard.length * 18 + 34);
        ctx.fillStyle = '#ffd35a';
        ctx.fillText('LEADERBOARD   score  k/d', boardX + 10, 30);
        for (var k = 0; k < leaderboard.length; ++k) {
            var row = leaderboard[k];
            ctx.fillStyle = row[0] === config.name ? '#9fd1ff' : '#cfe0ff';
            var name = (row[0] + '              ').substring(0, 14);
            ctx.fillText((k + 1) + '. ' + name + ' ' + row[1] + '  ' + row[2] + '/' + row[3], boardX + 10, 50 + k * 18);
        }

        drawMinimap();
        drawAltitudeBar();

        if (me.respawn > 0) {
            ctx.fillStyle = 'rgba(0, 0, 0, 0.45)';
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            ctx.font = 'bold 32px Menlo, monospace';
            ctx.textAlign = 'center';
            ctx.fillStyle = '#ff7070';
            ctx.fillText('DESTROYED', canvas.width / 2, canvas.height / 2 - 10);
            ctx.font = '16px Menlo, monospace';
            ctx.fillStyle = '#ffffff';
            ctx.fillText('respawn in ' + (me.respawn / 1000).toFixed(1) + 's', canvas.width / 2, canvas.height / 2 + 20);
            ctx.textAlign = 'left';
        }

        ctx.font = '11px Menlo, monospace';
        ctx.fillStyle = 'rgba(200, 220, 255, 0.6)';
        ctx.fillText('G: grid ' + (showGrid ? 'on' : 'off') + '   M: music ' + (musicEnabled ? 'on' : 'off'), 90, canvas.height - 16);
    }

    function drawMinimap() {
        var size = 180;
        var x = canvas.width - size - 14;
        var y = canvas.height - size - 14;
        var scale = size / Math.max(config.worldWidth, config.worldHeight);
        ctx.fillStyle = 'rgba(5, 10, 30, 0.75)';
        ctx.fillRect(x, y, size, size);
        ctx.strokeStyle = 'rgba(120, 160, 255, 0.6)';
        ctx.lineWidth = 1;
        ctx.strokeRect(x, y, size, size);

        ctx.fillStyle = 'rgba(90, 200, 255, 0.15)';
        ctx.beginPath();
        ctx.arc(x + me.x * scale, y + me.y * scale, config.interestRadius * scale, 0, Math.PI * 2);
        ctx.fill();

        for (var key in entities) {
            var entity = entities[key];
            ctx.fillStyle = entity.kind === KIND_SHIP ? '#ff6060' : (entity.kind === KIND_POWERUP ? '#60ff90' : '#8a8a8a');
            var dotSize = entity.kind === KIND_SHIP ? 3 : 2;
            ctx.fillRect(x + entity.x * scale - 1, y + entity.y * scale - 1, dotSize, dotSize);
        }
        ctx.fillStyle = '#9fd1ff';
        ctx.fillRect(x + me.x * scale - 2, y + me.y * scale - 2, 4, 4);
    }

    function sendInput(now) {
        if (!app || !joined || !me) {
            return;
        }
        var screenX = worldToScreenX(me.x);
        var screenY = elevatedScreenY(me.y, me.z);
        input.aim = Math.atan2(mouse.y - screenY, mouse.x - screenX);
        var payload = [
            input.up ? 1 : 0,
            input.down ? 1 : 0,
            input.left ? 1 : 0,
            input.right ? 1 : 0,
            Math.round(input.aim * 1000),
            input.fire ? 1 : 0,
            input.rise ? 1 : 0,
            input.sink ? 1 : 0
        ];
        var serialized = payload.join(',');
        var changed = serialized !== lastSentInput;
        if ((changed && now - lastSentAt >= 50) || now - lastSentAt >= 500) {
            app.send('input', payload);
            lastSentInput = serialized;
            lastSentAt = now;
        }
    }

    var lastFrame = performance.now();

    function frame(now) {
        var dt = Math.min(0.1, (now - lastFrame) / 1000);
        lastFrame = now;
        frameCount += 1;
        if (now - lastSecond >= 1000) {
            fps = frameCount;
            statesPerSecond = stateCount;
            frameCount = 0;
            stateCount = 0;
            lastSecond = now;
        }
        updateInterpolation(dt);
        drawBackground();
        drawGrid();
        drawEntities(now);
        drawHud();
        sendInput(now);
        requestAnimationFrame(frame);
    }

    function setKey(code, pressed) {
        switch (code) {
            case 'KeyW':
            case 'ArrowUp':
                input.up = pressed;
                return true;
            case 'KeyS':
            case 'ArrowDown':
                input.down = pressed;
                return true;
            case 'KeyA':
            case 'ArrowLeft':
                input.left = pressed;
                return true;
            case 'KeyD':
            case 'ArrowRight':
                input.right = pressed;
                return true;
            case 'KeyQ':
                input.rise = pressed;
                return true;
            case 'KeyE':
                input.sink = pressed;
                return true;
            case 'Space':
                input.fire = pressed;
                return true;
            default:
                return false;
        }
    }

    window.addEventListener('keydown', function (event) {
        if (!joined) {
            if (event.code === 'Enter' && !playButton.disabled) {
                connect();
            }
            return;
        }
        if (setKey(event.code, true)) {
            event.preventDefault();
        } else if (event.code === 'KeyG') {
            showGrid = !showGrid;
        } else if (event.code === 'KeyM') {
            toggleMusic();
        }
    });
    window.addEventListener('keyup', function (event) {
        if (joined && setKey(event.code, false)) {
            event.preventDefault();
        }
    });
    window.addEventListener('blur', function () {
        input.up = input.down = input.left = input.right = input.rise = input.sink = input.fire = false;
    });
    canvas.addEventListener('mousemove', function (event) {
        mouse.x = event.clientX;
        mouse.y = event.clientY;
    });
    canvas.addEventListener('mousedown', function (event) {
        if (event.button === 0) {
            input.fire = true;
        }
    });
    window.addEventListener('mouseup', function (event) {
        if (event.button === 0) {
            input.fire = false;
        }
    });
    canvas.addEventListener('contextmenu', function (event) {
        event.preventDefault();
    });
    playButton.addEventListener('click', connect);
    window.addEventListener('resize', resize);

    resize();
    createStars();
    mouse.x = canvas.width / 2 + 100;
    mouse.y = canvas.height / 2;
    nameInput.focus();
    requestAnimationFrame(frame);
})();
