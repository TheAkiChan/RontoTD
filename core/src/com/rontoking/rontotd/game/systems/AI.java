package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.game.entities.*;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.general.Point;
import com.rontoking.rontotd.general.Utility;

import static com.badlogic.gdx.math.MathUtils.random;

public class AI {
    private static final float CHANCE_TO_PLACE_TOWER = 0.5f;

    private static final int MIN_TILE_WIDTH = 10;
    private static final int MAX_TILE_WIDTH = 20;

    private static final int MIN_TILE_HEIGHT = 10;
    private static final int MAX_TILE_HEIGHT = 20;

    private static final int MIN_SPAWNER_NUM = 1;
    private static final int MAX_SPAWNER_NUM = 8;

    private static final int MIN_WAVE_NUM = 1;
    private static final int MAX_WAVE_NUM = 3;

    private static final int MIN_SPAWN_NUM = 1;
    private static final int MAX_SPAWN_NUM = 3;

    private static final int MIN_SPAWN_WAIT_FRAMES = 60;
    private static final int MAX_SPAWN_WAIT_FRAMES = 60;

    private static final int MIN_SPAWN_ENEMY_NUM = 5;
    private static final int MAX_SPAWN_ENEMY_NUM= 30;

    private static final int MIN_GOLD_NUM = 15;
    private static final int MAX_GOLD_NUM = 30;

    private static final int MIN_LIVES_NUM = 5;
    private static final int MAX_LIVES_NUM = 15;


    public static void update(){
        // TODO: Make good level-designing AI.
    }

    public static void findGoodLevel(){
        while(Self.lives != 1){
            generateRandomLevel();
            spendMoney();
            testLevel();
        }
    }

    public static void testLevel(){
        InputHandler.ready();
        while((Level.enemies.size > 0 || !Spawner.waveDone()) && Self.lives > 0){
            GameState.update();
        }
        if(Self.lives <= 0)
            GameState.setToTransition(Level.num, 0, true);
        Utility.print("Tested Level." + " - Lives: " + Self.lives);
    }

    public static void generateRandomLevel(){
        Level.clearLevel(true);
        generateRandomTiles();
        generateRandomStructures();
        generateRandomSpawners();
        Self.gold = random(MIN_GOLD_NUM, MAX_GOLD_NUM);
        Self.lives = random(MIN_LIVES_NUM, MAX_LIVES_NUM);
        Camera.setToDefault(1);
        GameState.phase = GameState.Phase.BUILD;
    }

    private static void generateRandomStructures(){
        Level.structures = new Structure[1];
        Level.structures[0] = getRandomStructure();
    }

    private static Structure getRandomStructure(){
        Point randomPos = getRandomLevelPos();
        if (Spawner.exists(randomPos.x, randomPos.y) || Structure.exists(randomPos.x, randomPos.y))
            return getRandomStructure();
        return new Structure(0, randomPos.x, randomPos.y);
    }

    private static void generateRandomSpawners(){
        Level.spawners = new Spawner[random(MIN_SPAWNER_NUM, MAX_SPAWNER_NUM)];
        for(int i = 0; i < Level.spawners.length; i++){
            Level.spawners[i] = getRandomSpawner();
        }
    }

    private static Spawner getRandomSpawner() {
        Point randomPos = getRandomLevelPos();
        if (Spawner.exists(randomPos.x, randomPos.y) || Structure.exists(randomPos.x, randomPos.y))
            return getRandomSpawner();
        Spawner spawner = new Spawner(randomPos.x, randomPos.y, new Array<Wave>());
        generateRandomWaves(spawner);
        return spawner;
    }

    private static void generateRandomWaves(Spawner spawner){
        for(int i = 0; i < random(MIN_WAVE_NUM, MAX_WAVE_NUM); i++){
            spawner.waves.add(new Wave(new Array<Spawn>()));
            generateRandomSpawns(spawner.waves.get(spawner.waves.size - 1));
        }
    }

    private static void generateRandomSpawns(Wave wave){
        for(int i = 0; i < random(MIN_SPAWN_NUM, MAX_SPAWN_NUM); i++){
            wave.spawns.add(new Spawn(random(MIN_SPAWN_WAIT_FRAMES, MAX_SPAWN_WAIT_FRAMES), getRandomEnemy(), random(MIN_SPAWN_ENEMY_NUM, MAX_SPAWN_ENEMY_NUM)));
        }
    }

    private static int getRandomEnemy(){
        return random(0, Enemy.enemies.length - 1);
    }

    private static void generateRandomTiles(){
        Level.tiles = new int[random(MIN_TILE_HEIGHT, MAX_TILE_HEIGHT)][random(MIN_TILE_WIDTH, MAX_TILE_WIDTH)];
        for(int y = 0; y < Level.tiles.length; y++)
            for(int x = 0; x < Level.tiles[y].length; x++)
                Level.tiles[y][x] = getRandomTile(x, y);
    }

    private static int getRandomTile(int x, int y){
        int chance;
        for(int i = 1; i < Tile.tiles.length; i++){
            chance = getSameTileNeighbourNum(i, x, y);
            chance *= chance;
            chance += 5;
            if(random(0, 100) < chance)
                return i;
        }
        return 0;
    }

    private static int getSameTileNeighbourNum(int type, int x, int y){
        int num = 0;
        for(int a = -1; a <= 1; a++){
            for(int b = -1; b <= 1; b++){
                if(a != 0 || b != 0){
                    if(isWithinLevel(x + a, y + b) && Level.tiles[y + b][x + a] == type)
                        num++;
                }
            }
        }
        return num;
    }

    private static boolean isWithinLevel(int x, int y){
        return x >= 0 && y >= 0 && x < Level.tiles[0].length && y < Level.tiles.length;
    }

    public static void spendMoney(){
        if(GameState.phase == GameState.Phase.BUILD)
            while (hasMoney())
                doRandomAction();
    }

    private static void doRandomAction() {
        if (random(0, 1f) < CHANCE_TO_PLACE_TOWER)
            placeRandomTower();
        else
            upgradeRandomTower();
    }

    private static void placeRandomTower(){
        selectRandomFreeTile();
        placeTower(random(0, Shop.basicTowers.size - 1));
    }

    private static void upgradeRandomTower(){
        if(Level.towers.size > 0) {
            int towerToUpgrade = random(0, Level.towers.size - 1);
            if (Level.towers.get(towerToUpgrade).upgrades.length > 0)
                upgradeTower(towerToUpgrade, random(0, Level.towers.get(towerToUpgrade).upgrades.length - 1));
        }
    }

    private static void placeTower(int shopIndex){
        Shop.buyTower(shopIndex);
    }

    private static void upgradeTower(int towerIndexInGame, int upgradeIndexInShop){
        if(Tower.towers[Level.towers.get(towerIndexInGame).upgrades[upgradeIndexInShop]].cost <= Self.gold) {
            Shop.getUpgradeTowers(towerIndexInGame, false);
            Shop.selectTowerInShop(upgradeIndexInShop);
            Shop.backToTowers();
        }
    }

    private static void selectRandomFreeTile(){
        Point randomPos = getRandomLevelPos();
        if(Tower.canPlaceTower(randomPos.x, randomPos.y, false))
            Self.selectedTile = randomPos;
        else
            selectRandomFreeTile();
    }

    private static Point getRandomLevelPos(){
        return new Point(random(0, Level.tiles[0].length - 1), random(0, Level.tiles.length - 1));
    }

    private static boolean hasMoney(){
        for(Tower tower : Shop.basicTowers)
            if(tower.cost <= Self.gold)
                return true;
        for(Tower tower : Level.towers)
            for(int i = 0; i < tower.upgrades.length; i++)
                if(Tower.towers[tower.upgrades[i]].cost <= Self.gold)
                    return true;
        return false;
    }
}
