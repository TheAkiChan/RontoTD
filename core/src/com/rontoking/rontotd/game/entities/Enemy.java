package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.game.systems.*;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.packets.UpdatePacket;
import com.rontoking.rontotd.general.Point;
import com.rontoking.rontotd.general.Utility;

public class Enemy{
    public static final float DIAGONAL_CONSTANT = MathUtils.sinDeg(45);

    public static Enemy[] enemies;
    public static String[] armorTypes;
    public static int lastFrame = 3;

    public static void load() {
        String[] enemyParts = Assets.getFile("enemies/info").readString().replaceAll("\n","").split("\\.", 2);
        armorTypes = enemyParts[0].split(",");
        for(int i = 0; i < armorTypes.length; i++){
            armorTypes[i] = armorTypes[i].trim();
        }
        String[] enemyArray = enemyParts[1].split(";");
        String[] enemy;
        enemies = new Enemy[enemyArray.length];
        for (int i = 0; i < enemies.length; i++) {
            enemy = enemyArray[i].split(",");
            int enemyArmorType = Integer.parseInt(enemy[5].trim());
            if(enemyArmorType >= armorTypes.length)
                enemyArmorType = armorTypes.length - 1;
            enemies[i] = new Enemy(enemy[0].trim(), Float.parseFloat(enemy[1].trim()), Float.parseFloat(enemy[2].trim()), Integer.parseInt(enemy[3].trim()), EnemyRenderable.TransportType.values()[Integer.parseInt(enemy[4].trim())], enemyArmorType, Integer.parseInt(enemy[6].trim()), Integer.parseInt(enemy[7].trim()), Ability.parseStringToArray(enemy[8]), Integer.parseInt(enemy[9].trim()));
        }
    }

    public boolean isAlive;
    public boolean isEnraged; // Enabled when an enemy does not have a way to get to any goal. Will pass through basicTowers, destroying them instantly.
    public Point tilePos, collisionPos;
    public Point direction;

    public int id;
    public EnemyRenderable renderable;

    public Array<Ability> hitEffects;

    public Ability[] abilities;
    public boolean isSilenced;

    public Enemy(String name, float health, float moveSpeed, int gold, EnemyRenderable.TransportType transportType, int armorType, int frameNum, int frameTime, Ability[] abilities, int damage){
        this.renderable = new EnemyRenderable(name, health, moveSpeed, gold, transportType, armorType, frameNum, frameTime, damage);
        this.abilities = abilities;
    }

    public Enemy(int index, int tileX, int tileY){
        this.renderable = new EnemyRenderable(index, tileX, tileY);
        this.abilities = enemies[index].abilities;

        this.isAlive = true;
        this.isEnraged = false;
        this.tilePos = new Point(tileX, tileY);
        this.collisionPos = new Point();
        updateCollisionPos();

        this.direction = new Point();

        this.id = Level.idCounter;
        Level.idCounter++;

        this.hitEffects = new Array<Ability>();
        this.isSilenced = false;

        updatePath();
    }

    private void updateStats(){
        Ability.updateAllFor(this);
    }

    private void updateCollisionPos(){
        collisionPos.set((int)renderable.x, (int)(renderable.y + renderable.height() / 2 - collisionRadius() / 4));
    }

    public void update(){
        updateStats();
        updateDirection();
        move();
        updateFrame();
        renderable.textureRect = renderable.textureRect();
    }

    public int collisionRadius(){
        return renderable.width() / 4;
    }

    public void drawShadow(){
        Renderer.drawWorldTexture(Assets.shadow, collisionPos.x - collisionRadius(), collisionPos.y - collisionRadius(), collisionRadius() * 2, collisionRadius());
    }

    public Texture texture(){
        return Assets.enemies[renderable.index];
    }

    private void updateDirection(){
        if(Vector2.dst(renderable.x, renderable.y, (tilePos.x + direction.x) * GameState.tileSize + GameState.tileSize / 2, (tilePos.y + direction.y) * GameState.tileSize + GameState.tileSize / 2) < renderable.moveSpeed){
            tilePos.add(direction);
            rage();
            renderable.x = tilePos.x * GameState.tileSize + GameState.tileSize / 2;
            renderable.y = tilePos.y * GameState.tileSize + GameState.tileSize / 2;
            updateCollisionPos();
            if(Level.isGoalPos(tilePos.x, tilePos.y))
                attack();
            else
                updatePath();
        }
    }

    private void rage(){
        if(isEnraged && Tower.exists(tilePos.x, tilePos.y)){
            Tower.remove(tilePos.x, tilePos.y, true);
            Level.animEffects.add(new AnimEffect(AnimEffect.Special.Explosion, tilePos.x * GameState.tileSize + GameState.tileSize / 2, tilePos.y * GameState.tileSize + GameState.tileSize / 2, 21, 2, true));
            Tower.playBoomSound();
        }
    }

    private void attack(){
        Self.lives -= renderable.damage;
        if(Networking.state == Networking.State.SERVER){
            for(int i = 0; i < Networking.playerFrontend.size; i++){
                Networking.playerFrontend.get(i).lives -= renderable.damage;
            }
        }
        if(renderable.damage == 1)
            Level.animEffects.add(new AnimEffect("-" + renderable.damage + " Life", 0, Color.RED, (int) renderable.x, (int) renderable.y, 50, 1, -1));
        else if(renderable.damage > 1)
            Level.animEffects.add(new AnimEffect("-" + renderable.damage + " Lives", 0, Color.RED, (int) renderable.x, (int) renderable.y, 50, 1, -1));
        die(null);
    }

    private void updatePath(){
        Point[] path = Pathfinding.path(tilePos, Level.getClosestGoal(tilePos.x, tilePos.y), renderable.transportType, isEnraged);
        if(path.length > 0) {
            Point nextTile = path[0];
            direction.x = Utility.getSign(nextTile.x - tilePos.x);
            direction.y = Utility.getSign(nextTile.y - tilePos.y);
        }else if(!isEnraged){ // No way to get to any goal.
            isEnraged = true;
            updatePath();
        }
    }

    private void move(){
        if(direction.x != 0 && direction.y != 0){ // Diagonal movement must be slower.
            renderable.x += direction.x * renderable.moveSpeed * DIAGONAL_CONSTANT;
            renderable.y += direction.y * renderable.moveSpeed * DIAGONAL_CONSTANT;
        }else {
            renderable.x += direction.x * renderable.moveSpeed;
            renderable.y += direction.y * renderable.moveSpeed;
        }
        updateCollisionPos();
        if(direction.y != 0)
            sort();
    }

    private void sort(){
        int sorted = Sorted.indexOf(id);
        Level.sorted.get(sorted).sortValue = (int)renderable.y;
        if(renderable.transportType == EnemyRenderable.TransportType.Flying)
            Level.sorted.get(sorted).sortValue += Sorted.FLIGHT_SORT_BONUS;
        if(sorted + 1 < Level.sorted.size && Level.sorted.get(sorted).sortValue > Level.sorted.get(sorted + 1).sortValue){
            while(sorted + 1 < Level.sorted.size && Level.sorted.get(sorted).sortValue > Level.sorted.get(sorted + 1).sortValue){
                Level.sorted.swap(sorted, sorted + 1);
                sorted++;
            }
        }else if (sorted - 1 >= 0 && Level.sorted.get(sorted).sortValue < Level.sorted.get(sorted - 1).sortValue) {
            while (sorted - 1 >= 0 && Level.sorted.get(sorted).sortValue < Level.sorted.get(sorted - 1).sortValue) {
                Level.sorted.swap(sorted, sorted - 1);
                sorted--;
            }
        }
    }

    public static void playDeathSound(int i){
        if(i >= 0) {
            Assets.enemySounds[i].play(Settings.GAME_VOLUME, MathUtils.random(0.75f, 1.25f), 0);
            if(Networking.state == Networking.State.SERVER)
                UpdatePacket.serverEnemyDeathIndex = i;
        }
    }

    private void updateFrame(){
        renderable.frameTimeLeft--;
        if(renderable.frameTimeLeft <= 0){
            renderable.frameTimeLeft = renderable.frameTime;
            renderable.frame++;
            if(renderable.frame > lastFrame){
                renderable.frame = 0;
            }
        }
        if(direction.x == 1)
            renderable.side = EnemyRenderable.Side.RIGHT;
        else if (direction.x == -1)
            renderable.side = EnemyRenderable.Side.LEFT;
        else if(direction.y == 1)
            renderable.side = EnemyRenderable.Side.DOWN;
        else if (direction.y == -1)
            renderable.side = EnemyRenderable.Side.UP;
    }

    public void die(Tower killer){
        isAlive = false;
        if(Level.auraEnemies.contains(this, true)){
            Level.auraEnemies.removeValue(this, true);
        }
        if(killer != null) {
            for (Ability ability : abilities) {
                if (ability.type == Ability.Type.Spawn) {
                    if (ability.value1 < 0)
                        ability.value1 = 0;
                    else if (ability.value1 > enemies.length - 1)
                        ability.value1 = enemies.length - 1;
                    for (int i = 0; i < (int) ability.value2; i++) {
                        spawn((int) ability.value1, (int) (this.renderable.x - GameState.tileSize / 2) / GameState.tileSize, (int) (this.renderable.y - GameState.tileSize / 2) / GameState.tileSize);
                        for (int m = 0; m < i * 30; m++) {
                            Level.enemies.get(Level.enemies.size - 1).move();
                        }
                    }
                }
            }
        }
        if(killer != null && renderable.gold > 0){
            if(Networking.state == Networking.State.SERVER){
                Networking.playerFrontend.get(killer.ownerID).gold += renderable.gold;
                if(killer.ownerID == 0){
                    Self.gold += renderable.gold;
                }
            }else{
                Self.gold += renderable.gold;
            }
            Level.animEffects.add(new AnimEffect("+" + renderable.gold + " Gold", 0, Color.GOLD, (int) renderable.x, (int) renderable.y, 50, 1, killer.ownerID));
        }
        Level.animEffects.add(new AnimEffect(AnimEffect.Special.Spawn, (int)renderable.x, (int)renderable.y, 5, 4, true));
        playDeathSound(renderable.index);
    }

    public static boolean exists(int x, int y){
        for(Enemy e : Level.enemies)
            if(e.tilePos.x == x && e.tilePos.y ==  y)
                return true;
        return false;
    }

    public static int indexOf(String name){
        for(int i = 0; i < enemies.length; i++){
            if(enemies[i].renderable.name.equals(name))
                return i;
        }
        return -1;
    }

    public static void kill(Enemy targetEnemy, Tower tower){
        for (int e = 0; e < Level.enemies.size; e++)
            if (Level.enemies.get(e).id == targetEnemy.id) {
                Level.enemies.get(e).die(tower);
                Level.enemyRenderables.removeIndex(e);
                Level.enemies.removeIndex(e);
            }
        Sorted.remove(targetEnemy.id);
    }

    public static void spawn(int type, int x, int y){
        Level.enemies.add(new Enemy(type, x, y));
        Sorted.addEnemy(Level.enemies.get(Level.enemies.size - 1));
        Level.enemyRenderables.add(Level.enemies.get(Level.enemies.size - 1).renderable);
        Level.animEffects.add(new AnimEffect(AnimEffect.Special.Spawn, x * GameState.tileSize + GameState.tileSize / 2, y * GameState.tileSize + GameState.tileSize / 2, 5, 4, true));
    }
}
