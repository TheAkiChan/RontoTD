package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.game.systems.*;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.packets.UpdatePacket;
import com.rontoking.rontotd.general.Utility;

public class Tower{
    public static Tower[] towers;
    private static Array<Integer> multiShotTargets = new Array<Integer>();
    private static float TEMP_TARGET_HEALTH, TARGET_HEALTH, TARGET_DISTANCE, TEMP_TARGET_DISTANCE;
    private static int TARGET_ENEMY;

    public String name;
    public int id, cost, fireCooldown, fireFramesLeft, ownerID, frameTime, currentFrame, frameTimeLeft;
    public float range, missileYOffset;
    public Missile missile;
    public int[] upgrades;
    public boolean inShop;
    public Ability[] abilities;
    public boolean isSilenced;

    private boolean IS_MULTI_SHOT;
    private int MULTI_SHOT_NUM;

    public TowerRenderable renderable;

    public static void load() {
        String[] towerParts = Gdx.files.local(Assets.towerPath + "/info").readString().replace("\n", "").split(":", 2);
        AttackType.load(towerParts[0].split(";"));
        String[] towerArray = towerParts[1].split(";");
        String[] tower;
        towers = new Tower[towerArray.length];
        Shop.basicTowers.clear();
        for (int i = 0; i < towers.length; i++) {
            tower = towerArray[i].split(",");

            int attackType = Integer.parseInt(tower[15].trim());
            if(attackType > AttackType.attackTypes.length - 1)
                attackType = AttackType.attackTypes.length - 1;

            int[] upgrades;
            if(tower[18].trim().equals("-1")){
                upgrades = new int[0];
            }else {
                String[] upgradesArray = tower[18].split(":");
                upgrades = new int[upgradesArray.length];
                for (int u = 0; u < upgrades.length; u++) {
                    upgrades[u] = Integer.parseInt(upgradesArray[u].trim());
                }
            }

            towers[i] = new Tower(i, tower[0].trim(), Integer.parseInt(tower[1].trim()), Float.parseFloat(tower[2].trim()), Integer.parseInt(tower[3].trim()), new Missile(Float.parseFloat(tower[4].trim()), Float.parseFloat(tower[5].trim()), Integer.parseInt(tower[6].trim()), Missile.Type.values()[Integer.parseInt(tower[7].trim())], Integer.parseInt(tower[8].trim()), Integer.parseInt(tower[9].trim()), Integer.parseInt(tower[10].trim()), Integer.parseInt(tower[11].trim()), Boolean.parseBoolean(tower[12].trim()), Integer.parseInt(tower[13].trim()), Integer.parseInt(tower[14].trim()), attackType), Integer.parseInt(tower[16].trim()), Integer.parseInt(tower[17].trim()), upgrades, Boolean.parseBoolean(tower[19].trim()), Ability.parseStringToArray(tower[20]), Float.parseFloat(tower[21].trim()));
            if(towers[i].inShop)
                Shop.basicTowers.add(towers[i]);
        }
        for(int i = 0; i < Level.towers.size; i++){
            if(Level.towers.get(i).renderable.index > towers.length - 1)
                Level.towers.set(i, new Tower(towers.length - 1, Level.towers.get(i).renderable.x, Level.towers.get(i).renderable.y, Level.towers.get(i).ownerID));
        }
    }

    public static void animate() {
        for (int i = 0; i < towers.length; i++) {
            towers[i].frameTimeLeft--;
            if (towers[i].frameTimeLeft <= 0) {
                towers[i].frameTimeLeft = towers[i].frameTime;
                towers[i].currentFrame++;
                if (towers[i].currentFrame >= towers[i].renderable.frameNum)
                    towers[i].currentFrame = 0;
            }
        }
    }

    public Tower(){

    }

    public Tower(int index, String name, int cost, float range, int fireCooldown, Missile missile, int frameNum, int frameTime, int[] upgrades, boolean inShop, Ability[] abilities, float missileYOffset){
        this.renderable = new TowerRenderable(index, frameNum);

        this.name = name;
        this.cost = cost;
        this.range = range;
        this.fireCooldown = fireCooldown;
        this.missile = new Missile(missile);
        this.frameTime = frameTime;
        this.upgrades = upgrades;
        this.inShop = inShop;
        this.abilities = abilities;
        this.missileYOffset = missileYOffset;

        this.currentFrame = 0;
        this.frameTimeLeft = frameTime;
    }

    public Tower(int index, int x, int y, int ownerID){
        this.renderable = new TowerRenderable(index, x, y);

        this.name = towers[index].name;
        this.cost = towers[index].cost;
        this.range = towers[index].range;
        this.fireCooldown = towers[index].fireCooldown;
        this.missile = new Missile(towers[index].missile);
        this.frameTime = towers[index].frameTime;
        this.upgrades = towers[index].upgrades;
        this.inShop = towers[index].inShop;
        this.abilities = towers[index].abilities;
        this.missileYOffset = towers[index].missileYOffset;

        this.isSilenced = false;

        checkForMultiShot();

        this.fireFramesLeft = 0;

        this.ownerID = ownerID;

        this.id = Level.idCounter;
        Level.idCounter++;
        Sorted.addTower(this);
    }

    private void checkForMultiShot(){
        for(Ability ability : abilities){
            if(ability.type == Ability.Type.Multi_Shot){
                IS_MULTI_SHOT = true;
                if(ability.value1 > MULTI_SHOT_NUM){
                    MULTI_SHOT_NUM = (int)ability.value1;
                }
            }
        }
    }

    public void setID(int newId){
        Level.sorted.get(Sorted.indexOf(id)).id = newId;
        id = newId;
    }

    public void update(){
        updateStats();
        fireFramesLeft--;
        if(fireFramesLeft <= 0){
            chooseTarget();
            if(TARGET_ENEMY != -1 || (IS_MULTI_SHOT && multiShotTargets.size > 0)){
                fire();
            }
        }
    }

    private void updateStats(){
        Ability.resetStats(this);
        for (int i = 0; i < Ability.Math.values().length; i++) {
            Ability.updateAurasFor(this, Ability.Math.values()[i]);
        }
    }

    private void fire(){
        fireFramesLeft = fireCooldown;
        if(IS_MULTI_SHOT){
            for(int i = 0; i < multiShotTargets.size && i < MULTI_SHOT_NUM; i++){
                Level.missiles.add(new Missile(renderable.index, renderable.x, renderable.y, Level.enemies.get(multiShotTargets.get(i)), this));
                Level.missileRenderables.add(Level.missiles.get(Level.missiles.size - 1).renderable);
            }
        }else {
            Level.missiles.add(new Missile(renderable.index, renderable.x, renderable.y, Level.enemies.get(TARGET_ENEMY), this));
            Level.missileRenderables.add(Level.missiles.get(Level.missiles.size - 1).renderable);
        }
    }

    private void insertMultiShot(int index){
        for (int e = 0; e < multiShotTargets.size && e < MULTI_SHOT_NUM; e++) {
            if (TEMP_TARGET_HEALTH < Level.enemies.get(e).renderable.health || (TEMP_TARGET_HEALTH == Level.enemies.get(e).renderable.health && TEMP_TARGET_DISTANCE < Utility.dstBetweenTowerAndEnemy(this, Level.enemies.get(e)))) {
                multiShotTargets.insert(e, index);
                return;
            }
        }
    }

    private void chooseTarget(){
        if(IS_MULTI_SHOT){
            multiShotTargets.clear();
            for (int i = 0; i < Level.enemies.size; i++) {
                TEMP_TARGET_HEALTH = Level.enemies.get(i).renderable.health;
                TEMP_TARGET_DISTANCE = Utility.dstBetweenTowerAndEnemy(this, Level.enemies.get(i));
                if (TEMP_TARGET_DISTANCE <= range * GameState.tileSize && TEMP_TARGET_HEALTH > 0) {
                    if(multiShotTargets.size == 0 || TEMP_TARGET_HEALTH > Level.enemies.get(multiShotTargets.get(multiShotTargets.size - 1)).renderable.health)
                        multiShotTargets.add(i);
                    else {
                        insertMultiShot(i);
                    }
                }
            }
        }else {
            TARGET_HEALTH = -1;
            TARGET_DISTANCE = -1;
            TARGET_ENEMY = -1;
            for (int i = 0; i < Level.enemies.size; i++) {
                TEMP_TARGET_HEALTH = Level.enemies.get(i).renderable.health;
                TEMP_TARGET_DISTANCE = Utility.dstBetweenTowerAndEnemy(this, Level.enemies.get(i));
                if (TEMP_TARGET_DISTANCE <= range * GameState.tileSize && TEMP_TARGET_HEALTH > 0 && (TARGET_HEALTH == -1 || TEMP_TARGET_HEALTH < TARGET_HEALTH || (TEMP_TARGET_HEALTH == TARGET_HEALTH && TEMP_TARGET_DISTANCE < TARGET_DISTANCE))) {
                    TARGET_ENEMY = i;
                    TARGET_HEALTH = TEMP_TARGET_HEALTH;
                    TARGET_DISTANCE = TEMP_TARGET_DISTANCE;
                }
            }
        }
    }

    public String getDescription(){
        return "Name: " + name + "\n" +
                "Cost: " + cost + "\n" +
                "Range: " + range + "\n" +
                "Fire Cooldown: " + fireCooldown + "\n" +
                "Missile Type: " + missile.type.name() + "\n" +
                "Missile Hit Radius: " + missile.hitRadius + "\n" +
                "Missile Splash Radius: " + missile.splashRadius + "\n" +
                "Missile Speed: " + missile.moveSpeed + "\n" +
                "Missile Direct Damage: " + missile.maxDamage + "\n" +
                "Missile Attack Type: " + AttackType.attackTypes[missile.attackType].name + abilityDescription();
    }

    private String abilityDescription(){
        if(abilities.length > 0){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n\nAbilities: ");
            for(int i = 0; i < abilities.length; i++){
                stringBuilder.append("\n" + abilities[i].name());
            }
            return stringBuilder.toString();
        }
        return "";
    }

    public static boolean exists(int x, int y){
        for(int i = 0; i < Level.towers.size; i++){
            if(Level.towers.get(i).renderable.x == x && Level.towers.get(i).renderable.y == y)
                return true;
        }
        return false;
    }

    public static Tower get(int x, int y){
        for(int i = 0; i < Level.towers.size; i++){
            if(Level.towers.get(i).renderable.x == x && Level.towers.get(i).renderable.y == y) {
                return Level.towers.get(i);
            }
        }
        return null;
    }

    public static void playBoomSound(){
        Assets.boomSound.play(Settings.GAME_VOLUME, MathUtils.random(0.75f, 1.25f), 0);
        if(Networking.state == Networking.State.SERVER)
            UpdatePacket.BOOM_SOUND_PLAYED = true;
    }

    public static int indexOf(int x, int y){
        for(int i = 0; i < Level.towers.size; i++){
            if(Level.towers.get(i).renderable.x == x && Level.towers.get(i).renderable.y == y) {
                return i;

            }
        }
        return -1;
    }

    public static int indexOf(String name){
        for(int i = 0; i < towers.length; i++){
            if(towers[i].name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean canPlaceTower(int x, int y, boolean ignoreTowers){
        return (!Tower.exists(x, y) || ignoreTowers) && !Structure.blocksTower(x, y) && !Spawner.exists(x, y) && Level.tiles[y][x] > -1 && Tile.tiles[Level.tiles[y][x]].canHaveTower;
    }

    public static void remove(int x, int y, boolean playSound){
        Sorted.remove(Level.towers.get(indexOf(x, y)).id);
        if(indexOf(x, y) == Shop.SELECTED_TOWER_IN_GAME)
            Shop.SELECTED_TOWER_IN_GAME = -1;
        else if(indexOf(x, y) < Shop.SELECTED_TOWER_IN_GAME)
            Shop.SELECTED_TOWER_IN_GAME--;
        if(Level.auraTowers.contains(Level.towers.get(indexOf(x, y)), true)){
            Level.auraTowers.removeValue(Level.towers.get(indexOf(x, y)), true);
        }
        Level.towerRenderables.removeIndex(indexOf(x, y));
        Level.towers.removeIndex(indexOf(x, y));
        if(playSound)
            Assets.placeSound.play(Settings.GAME_VOLUME, MathUtils.random(0.75f, 1.25f), 0);
    }

    public static void place(int type, int x, int y, int ownerID, boolean showEffect){
        Level.towers.add(new Tower(type, x, y, ownerID));
        Level.towerRenderables.add(Level.towers.get(Level.towers.size - 1).renderable);
        Assets.placeSound.play(Settings.GAME_VOLUME, MathUtils.random(0.75f, 1.25f), 0);
        if(showEffect)
            Level.animEffects.add(new AnimEffect(AnimEffect.Special.Spawn, x * GameState.tileSize + GameState.tileSize / 2, y * GameState.tileSize + GameState.tileSize / 2, 5, 4, false));
        for(Ability a : Level.towers.get(Level.towers.size - 1).abilities){
            if(a.type == Ability.Type.Enemy_Aura || a.type == Ability.Type.Tower_Aura){
                Level.auraTowers.add(Level.towers.get(Level.towers.size - 1));
                return;
            }
        }
    }

    public static void upgrade(int type, int upgradedTowerIndex, boolean showEffect){
        int x = Level.towers.get(upgradedTowerIndex).renderable.x;
        int y = Level.towers.get(upgradedTowerIndex).renderable.y;
        int ownerID = Level.towers.get(upgradedTowerIndex).ownerID;
        int id = Level.towers.get(upgradedTowerIndex).id;
        remove(x, y, false);
        if(Networking.state == Networking.State.SINGLEPLAYER || (Networking.state == Networking.State.SERVER && ownerID == 0) || (Networking.state == Networking.State.CLIENT && ownerID == Networking.client.getID()))
            Shop.SELECTED_TOWER_IN_GAME = upgradedTowerIndex;
        Level.towers.insert(upgradedTowerIndex, new Tower(type, x, y, ownerID));
        Level.towerRenderables.insert(upgradedTowerIndex, Level.towers.get(upgradedTowerIndex).renderable);
        Level.towers.get(upgradedTowerIndex).setID(id);
        Assets.placeSound.play(Settings.GAME_VOLUME, MathUtils.random(0.75f, 1.25f), 0);
        if(showEffect)
            Level.animEffects.add(new AnimEffect(AnimEffect.Special.Spawn, Level.towers.get(upgradedTowerIndex).renderable.x * GameState.tileSize + GameState.tileSize / 2, Level.towers.get(upgradedTowerIndex).renderable.y * GameState.tileSize + GameState.tileSize / 2, 5, 4, false));
        for(Ability a : Level.towers.get(upgradedTowerIndex).abilities){
            if(a.type == Ability.Type.Enemy_Aura || a.type == Ability.Type.Tower_Aura){
                Level.auraTowers.add(Level.towers.get(upgradedTowerIndex));
                return;
            }
        }
    }
}
