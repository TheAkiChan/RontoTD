package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.math.MathUtils;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Level;
import com.rontoking.rontotd.general.Utility;

public class Ability {
    public enum Type{
        Multi_Shot, Hit_Effect, Enemy_Aura, Tower_Aura, Spawn
    }

    public enum Target{
        Both_Equal, Both_Unequal, Direct, Indirect_Equal, Indirect_Unequal
    }

    public enum EnemyStat{
        Health, Movement_Speed, Gold, Armor_Type, Silence, Damage
    }

    public enum TowerStat{
        Fire_Cooldown, Range, Missile_Speed, Missile_Damage, Missile_Hit_Radius, Missile_Splash_Radius, Missile_Attack_Type, Silence
    }

    public enum Math{
        Set, Additive, Multiplicative
    }

    public static final int TOWER_START = 0;
    public static final int TOWER_END = 4;
    public static final int ENEMY_START = 2;
    public static final int TILE_START = 2;
    public static final int TILE_END = 4;
    public static final int STRUCTURE_START = 2;
    public static final int STRUCTURE_END = 4;

    public Type type;
    public Target target;
    public int stat;

    public float value1, value2;
    public Math math;

    private float ratio;
    private Tower user;

    public static void updateAurasFor(Tower targetTower, Math math){
        for(Tower tower : Level.auraTowers){
            if(!tower.isSilenced) {
                for (Ability ability : tower.abilities) {
                    if (ability.type == Type.Tower_Aura && (ability.target != Target.Direct || tower == targetTower) && ((ability.target != Target.Indirect_Unequal && ability.target != Target.Indirect_Equal) || tower != targetTower) && ability.math == math && Utility.dstBetweenTowerAndTower(tower, targetTower) <= auraRange(ability)) {
                        ability.update(targetTower, Utility.dstBetweenTowerAndTower(tower, targetTower) / auraRange(ability));
                    }
                }
            }
        }
        for(Enemy enemy : Level.auraEnemies){
            if(!enemy.isSilenced) {
                for (Ability ability : enemy.abilities) {
                    if (ability.type == Type.Tower_Aura && ability.math == math && Utility.dstBetweenTowerAndEnemy(targetTower, enemy) <= auraRange(ability)) {
                        ability.update(targetTower, Utility.dstBetweenTowerAndEnemy(targetTower, enemy) / auraRange(ability));
                    }
                }
            }
        }
        for(Structure structure : Level.auraStructures){
            for(Ability ability : structure.abilities){
                if(ability.type == Type.Tower_Aura && ability.math == math && Utility.dstBetweenTowerAndStructure(targetTower, structure) <= auraRange(ability)){
                    ability.update(targetTower, Utility.dstBetweenTowerAndStructure(targetTower, structure) / auraRange(ability));
                }
            }
        }
        updateTileAuraFor(targetTower, math);
    }

    public static void updateAurasFor(Enemy targetEnemy, Math math){
        for(Tower tower : Level.auraTowers) {
            if (!tower.isSilenced) {
                for (Ability ability : tower.abilities) {
                    if (ability.type == Type.Enemy_Aura && ability.math == math && Utility.dstBetweenTowerAndEnemy(tower, targetEnemy) <= auraRange(ability)) {
                        ability.update(targetEnemy, Utility.dstBetweenTowerAndEnemy(tower, targetEnemy) / auraRange(ability));
                    }
                }
            }
        }
        for(Enemy enemy : Level.auraEnemies){
            if(!enemy.isSilenced) {
                for (Ability ability : enemy.abilities) {
                    if (ability.type == Type.Enemy_Aura && (ability.target != Target.Direct || enemy == targetEnemy) && ((ability.target != Target.Indirect_Unequal && ability.target != Target.Indirect_Equal) || enemy != targetEnemy) && ability.math == math && Utility.dstBetweenEnemyAndEnemy(enemy, targetEnemy) <= auraRange(ability)) {
                        ability.update(targetEnemy, Utility.dstBetweenEnemyAndEnemy(enemy, targetEnemy) / auraRange(ability));
                    }
                }
            }
        }
        for(Structure structure : Level.auraStructures){
            for(Ability ability : structure.abilities){
                if(ability.type == Type.Enemy_Aura && ability.math == math && Utility.dstBetweenEnemyAndStructure(targetEnemy, structure) <= auraRange(ability)){
                    ability.update(targetEnemy, Utility.dstBetweenEnemyAndStructure(targetEnemy, structure) / auraRange(ability));
                }
            }
        }
        updateTileAuraFor(targetEnemy, math);
    }

    private static void updateTileAuraFor(Enemy enemy, Math math){
        for (Ability ability : Tile.tiles[Level.tiles[MathUtils.floor((float) enemy.collisionPos.y / (float)GameState.tileSize)][MathUtils.floor((float) enemy.collisionPos.x / (float)GameState.tileSize)]].abilities){
            if(ability.type == Type.Enemy_Aura && ability.math == math){
                ability.update(enemy, 1);
            }
        }
    }

    private static void updateTileAuraFor(Tower tower, Math math){
        for (Ability ability : Tile.tiles[Level.tiles[tower.renderable.y][tower.renderable.x]].abilities){
            if(ability.type == Type.Tower_Aura && ability.math == math){
                ability.update(tower, 1);
            }
        }
    }

    private static float auraRange(Ability ability){
        return ability.value2 * GameState.tileSize;
    }

    public static void updateHitEffectsFor(Enemy enemy, Math math){
        if(enemy.hitEffects.size > 0) {
            for (int a = 0; a < enemy.hitEffects.size; a++) {
                if (enemy.hitEffects.get(a).math == math) { // Buffs are calculated in order.
                    enemy.hitEffects.get(a).update(enemy);
                    if (enemy.hitEffects.get(a).type == Type.Hit_Effect) { // If it's a hit effect, shorten its remaining duration.
                        enemy.hitEffects.get(a).value2--;
                        if (enemy.hitEffects.get(a).value2 == 0) {
                            enemy.hitEffects.removeIndex(a);
                        }
                    }
                }
            }
        }
    }

    public static void updateAllFor(Enemy enemy){
        resetStats(enemy);
        for (int i = 0; i < Math.values().length; i++) {
            updateHitEffectsFor(enemy, Math.values()[i]);
            updateAurasFor(enemy, Math.values()[i]);
        }
    }

    private static void resetStats(Enemy enemy){
        enemy.renderable.moveSpeed = Enemy.enemies[enemy.renderable.index].renderable.moveSpeed;
        enemy.renderable.gold = Enemy.enemies[enemy.renderable.index].renderable.gold;
        enemy.renderable.armorType = Enemy.enemies[enemy.renderable.index].renderable.armorType;
        enemy.isSilenced = false;
    }

    public static void resetStats(Tower tower){
        tower.fireCooldown = Tower.towers[tower.renderable.index].fireCooldown;
        tower.range = Tower.towers[tower.renderable.index].range;
        tower.missile.moveSpeed = Tower.towers[tower.renderable.index].missile.moveSpeed;
        tower.missile.maxDamage = Tower.towers[tower.renderable.index].missile.maxDamage;
        tower.missile.hitRadius = Tower.towers[tower.renderable.index].missile.hitRadius;
        tower.missile.splashRadius = Tower.towers[tower.renderable.index].missile.splashRadius;
        tower.missile.attackType = Tower.towers[tower.renderable.index].missile.attackType;
        tower.isSilenced = false;
    }

    public static Ability[] parseStringToArray(String string){
        Ability[] abilities;
        if(string.trim().equals("-1")) {
            abilities = new Ability[0];
        }else{
            String[] abilitiesArray = string.split(":");
            abilities = new Ability[abilitiesArray.length];
            for(int a = 0; a < abilities.length; a++){
                abilities[a] = parseString(abilitiesArray[a]);
            }
        }
        return abilities;
    }

    private static Ability parseString(String string){
        String[] abilityParts = string.split("/");
        Ability ability = new Ability();
        ability.type = Type.values()[Integer.parseInt(abilityParts[0].trim())];
        ability.stat = Integer.parseInt(abilityParts[1].trim());
        ability.value1 = Float.parseFloat(abilityParts[2].trim());
        ability.value2 = Float.parseFloat(abilityParts[3].trim());
        ability.math = Math.values()[Integer.parseInt(abilityParts[4].trim())];
        ability.target = Target.values()[Integer.parseInt(abilityParts[5].trim())];
        return ability;
    }

    public Ability(){

    }

    public Ability(Ability ability){
        this.type = ability.type;
        this.target = ability.target;
        this.stat = ability.stat;
        this.value1 = ability.value1;
        this.value2 = ability.value2;
        this.math = ability.math;
    }

    public String name(){
        String name = "";
        switch (type) {
            case Multi_Shot:
                name += "Multi-Shot (" + (int)value1 + " Missiles)";
                break;
            case Hit_Effect:
                name += EnemyStat.values()[stat].name().replace("_", " ") + " Hit Effect (Duration: " + (int)value2 + ")";
                break;
            case Enemy_Aura:
                name += EnemyStat.values()[stat].name().replace("_", " ") + " Enemy Aura (Range: " + value2 + ")";
                break;
            case Tower_Aura:
                name += TowerStat.values()[stat].name().replace("_", " ") + " Tower Aura (Range: " + value2 + ")";
                break;
            case Spawn:
                name += "Spawn " + (int)value2 + " of " + Enemy.enemies[(int)value1].renderable.name + " on Death";
                break;
            default:
                break;
        }
        return name;
    }

    public static Ability multiShot(int missileNum){
        Ability ability = new Ability();
        ability.type = Type.Multi_Shot;
        ability.value1 = missileNum;
        return ability;
    }

    public static Ability hitEffect(EnemyStat enemyStat, float duration, float value, Math math, Target target){
        Ability ability = new Ability();
        ability.type = Type.Hit_Effect;
        ability.value1 = value;
        ability.value2 = duration;
        ability.math = math;
        ability.target = target;
        return ability;
    }

    public static Ability auraEffect(EnemyStat enemyStat, float range, float value, Math math, Target target){
        Ability ability = new Ability();
        ability.type = Type.Enemy_Aura;
        ability.stat = enemyStat.ordinal();
        ability.value1 = value;
        ability.value2 = range;
        ability.math = math;
        ability.target = target;
        return ability;
    }

    public static Ability auraEffect(TowerStat towerStat, float range, float value, Math math, Target target){
        Ability ability = new Ability();
        ability.type = Type.Tower_Aura;
        ability.stat = towerStat.ordinal();
        ability.value1 = value;
        ability.value2 = range;
        ability.math = math;
        ability.target = target;
        return ability;
    }
    private float fixedRatio(float ratio){
        if(target == Target.Both_Equal || target == Target.Direct || target == Target.Indirect_Equal)
            return 1;
        return ratio;
    }

    public void useOn(Enemy enemy, float ratio, Tower tower){
        enemy.hitEffects.add(new Ability(this));
        enemy.hitEffects.get(enemy.hitEffects.size - 1).ratio = fixedRatio(ratio);
        enemy.hitEffects.get(enemy.hitEffects.size - 1).user = tower;
    }

    public void update(Enemy enemy, float auraRatio){
        ratio = auraRatio;
        update(enemy);
    }

    public void update(Tower tower, float auraRatio){
        ratio = auraRatio;
        update(tower);
    }

    public void update(Enemy enemy){
        ratio = fixedRatio(ratio);
        switch (EnemyStat.values()[stat]){
            case Health:
                if(math == Math.Set)
                    enemy.renderable.health = value1 * ratio;
                else if(math == Math.Additive)
                    enemy.renderable.health -= value1 * ratio;
                else if(math == Math.Multiplicative)
                    enemy.renderable.health /= value1 * ratio;
                if(enemy.renderable.health <= 0)
                    enemy.die(user);
                break;
            case Movement_Speed:
                if(math == Math.Set)
                    enemy.renderable.moveSpeed = value1 * ratio;
                else if(math == Math.Additive)
                    enemy.renderable.moveSpeed -= value1 * ratio;
                else if(math == Math.Multiplicative)
                    enemy.renderable.moveSpeed /= value1 * ratio;
                break;
            case Gold:
                if(math == Math.Set)
                    enemy.renderable.gold = (int)(value1 * ratio);
                else if(math == Math.Additive)
                    enemy.renderable.gold += value1 * ratio;
                else if(math == Math.Multiplicative)
                    enemy.renderable.gold *= value1 * ratio;
                break;
            case Armor_Type:
                if(math == Math.Set)
                    enemy.renderable.armorType = (int)(value1 * ratio);
                else if(math == Math.Additive)
                    enemy.renderable.armorType += value1 * ratio;
                else if(math == Math.Multiplicative)
                    enemy.renderable.armorType *= value1 * ratio;
                if (enemy.renderable.armorType < 0)
                    enemy.renderable.armorType = 0;
                else if (enemy.renderable.armorType > Enemy.armorTypes.length - 1)
                    enemy.renderable.armorType = Enemy.armorTypes.length - 1;
                break;
            case Silence:
                enemy.isSilenced = true;
                break;
            case Damage:
                if(math == Math.Set)
                    enemy.renderable.damage = (int)(value1 * ratio);
                else if(math == Math.Additive)
                    enemy.renderable.damage -= value1 * ratio;
                else if(math == Math.Multiplicative)
                    enemy.renderable.damage /= value1 * ratio;
                break;
            default:
                break;
        }
    }

    public void update(Tower tower){
        ratio = fixedRatio(ratio);
        switch (TowerStat.values()[stat]) {
            case Fire_Cooldown:
                if (math == Math.Set)
                    tower.fireCooldown = (int) (value1 * ratio);
                else if (math == Math.Additive)
                    tower.fireCooldown -= value1 * ratio;
                else if (math == Math.Multiplicative)
                    tower.fireCooldown /= value1 * ratio;
                break;
            case Range:
                if (math == Math.Set)
                    tower.range = value1 * ratio;
                else if (math == Math.Additive)
                    tower.range += value1 * ratio;
                else if (math == Math.Multiplicative)
                    tower.range *= value1 * ratio;
                break;
            case Missile_Speed:
                if (math == Math.Set)
                    tower.missile.moveSpeed = value1 * ratio;
                else if (math == Math.Additive)
                    tower.missile.moveSpeed += value1 * ratio;
                else if (math == Math.Multiplicative)
                    tower.missile.moveSpeed *= value1 * ratio;
                break;
            case Missile_Damage:
                if (math == Math.Set)
                    tower.missile.maxDamage = value1 * ratio;
                else if (math == Math.Additive)
                    tower.missile.maxDamage += value1 * ratio;
                else if (math == Math.Multiplicative)
                    tower.missile.maxDamage *= value1 * ratio;
                break;
            case Missile_Hit_Radius:
                if (math == Math.Set)
                    tower.missile.hitRadius = (int) (value1 * ratio);
                else if (math == Math.Additive)
                    tower.missile.hitRadius += value1 * ratio;
                else if (math == Math.Multiplicative)
                    tower.missile.hitRadius *= value1 * ratio;
                break;
            case Missile_Splash_Radius:
                if (math == Math.Set)
                    tower.missile.splashRadius = (int) (value1 * ratio);
                else if (math == Math.Additive)
                    tower.missile.splashRadius += value1 * ratio;
                else if (math == Math.Multiplicative)
                    tower.missile.splashRadius *= value1 * ratio;
                break;
            case Missile_Attack_Type:
                if (math == Math.Set)
                    tower.missile.attackType = (int) (value1 * ratio);
                else if (math == Math.Additive)
                    tower.missile.attackType += value1 * ratio;
                else if (math == Math.Multiplicative)
                    tower.missile.attackType *= value1 * ratio;
                if (tower.missile.attackType < 0)
                    tower.missile.attackType = 0;
                else if (tower.missile.attackType > AttackType.attackTypes.length - 1)
                    tower.missile.attackType = AttackType.attackTypes.length - 1;
                break;
            case Silence:
                tower.isSilenced = true;
                break;
            default:
                break;
        }
    }
}
