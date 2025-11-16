package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Level;
import com.rontoking.rontotd.general.Point;

public class Missile {
    public enum Type{ // Homing goes for target enemy, landing lands on a tile. straight goes until it hits an enemy or exits the level.
        Homing, Landing, Straight
    }

    private static float RATIO = 1;

    public Vector2 collisionCenter;
    public float moveSpeed;
    public float maxDamage;
    private Enemy target;
    private Point targetPoint; // For landing-type missiles.
    public boolean isAlive, hasAnimEffect;

    public Type type;
    public int hitRadius, splashRadius, animEffectFrameNum, animEffectFrameTime, frameTime, attackType;
    public Tower tower;

    private float damage;
    private int frameTimeLeft;

    public MissileRenderable renderable;

    public Missile(float moveSpeed, float maxDamage, int height, Type type, int hitRadius, int splashRadius, int animEffectFrameNum, int animEffectFrameTime, boolean hasAnimEffect, int frameNum, int frameTime, int attackType){
        this.renderable = new MissileRenderable(height, frameNum);

        this.moveSpeed = moveSpeed;
        this.maxDamage = maxDamage;
        this.type = type;
        this.hitRadius = hitRadius;
        this.splashRadius = splashRadius;
        this.animEffectFrameNum = animEffectFrameNum;
        this.animEffectFrameTime = animEffectFrameTime;
        this.hasAnimEffect = hasAnimEffect;
        this.frameTime = frameTime;
        this.attackType = attackType;
    }

    public Missile(Missile missile){
        this.renderable = new MissileRenderable(missile.renderable.height, missile.renderable.frameNum);

        this.moveSpeed = missile.moveSpeed;
        this.maxDamage = missile.maxDamage;
        this.type = missile.type;
        this.hitRadius = missile.hitRadius;
        this.splashRadius = missile.splashRadius;
        this.animEffectFrameNum = missile.animEffectFrameNum;
        this.animEffectFrameTime = missile.animEffectFrameTime;
        this.hasAnimEffect = missile.hasAnimEffect;
        this.frameTime = missile.frameTime;
        this.attackType = missile.attackType;
    }

    public Missile(int index, int x, int y, Enemy target, Tower tower){
        this.renderable = new MissileRenderable(index, x, y, tower);

        this.target = target;
        this.tower = tower;

        this.moveSpeed = Tower.towers[this.renderable.index].missile.moveSpeed;
        this.maxDamage = Tower.towers[this.renderable.index].missile.maxDamage;
        this.type = Tower.towers[this.renderable.index].missile.type;
        this.hitRadius = Tower.towers[this.renderable.index].missile.hitRadius;
        this.splashRadius = Tower.towers[this.renderable.index].missile.splashRadius;
        this.animEffectFrameNum = Tower.towers[this.renderable.index].missile.animEffectFrameNum;
        this.animEffectFrameTime = Tower.towers[this.renderable.index].missile.animEffectFrameTime;
        this.hasAnimEffect = Tower.towers[this.renderable.index].missile.hasAnimEffect;
        this.frameTime = Tower.towers[this.renderable.index].missile.frameTime;
        this.attackType =  Tower.towers[this.renderable.index].missile.attackType;

        this.frameTimeLeft = frameTime;
        this.isAlive = true;
        this.collisionCenter = new Vector2();

        this.collisionCenter.set(renderable.texturePosition.x + renderable.direction.x * (renderable.width() / 2 - hitRadius), renderable.texturePosition.y + renderable.direction.y * (renderable.width() / 2 - hitRadius));
        aim();
        travel(renderable.width() / 2);
    }

    private void aim() {
        switch (type){
            case Homing:
                renderable.direction.set(target.renderable.x - collisionCenter.x, target.renderable.y - collisionCenter.y);
                renderable.direction = renderable.direction.nor();
                break;
            case Landing:
                targetPoint = new Point(target.collisionPos.x, (int)target.collisionPos.y);
                renderable.direction.set(targetPoint.x - collisionCenter.x, targetPoint.y - collisionCenter.y);
                renderable.direction = renderable.direction.nor();
                break;
            case Straight:
                renderable.direction.set(target.collisionPos.x - collisionCenter.x, target.collisionPos.y - collisionCenter.y);
                renderable.direction = renderable.direction.nor();
                break;
            default:
                break;
        }
    }

    public void update(){
        animate();
        if(target == null && type == Type.Homing)
            die(false);
        else{
            checkForCollision();
            travel();
        }
    }

    private void animate(){
        frameTimeLeft--;
        if (frameTimeLeft <= 0) {
            frameTimeLeft = frameTime;
            renderable.currentFrame++;
            if (renderable.currentFrame >= renderable.frameNum)
                renderable.currentFrame = 0;
        }
    }

    private void travel(){
        travel(moveSpeed);
    }

    private void travel(float amount) {
        if (type == Type.Homing)
            aim();
        renderable.direction.set(renderable.direction.x * amount, renderable.direction.y * amount);
        collisionCenter.set(collisionCenter.x + renderable.direction.x, collisionCenter.y + renderable.direction.y);
        updateTexturePosition();
        checkIfOutsideLevel();
    }

    private void checkIfOutsideLevel(){
        if(type == Type.Straight && (collisionCenter.x + hitRadius < 0 || collisionCenter.x - hitRadius > Level.tiles[0].length * GameState.tileSize || collisionCenter.y + hitRadius < 0 || collisionCenter.y - hitRadius > Level.tiles.length * GameState.tileSize)){
            die(false);
        }
    }

    private void checkForCollision(){
        switch (type){
            case Homing:
                if (distanceFrom(target.renderable.x, target.renderable.y) < moveSpeed) { // Enemy is hit. Missile disappears.
                    hit(target);
                }
                break;
            case Landing:
                if (distanceFrom(targetPoint.x, targetPoint.y) < moveSpeed) { // Enemy is hit. Missile disappears.
                    hit(null);
                }
                break;
            case Straight:
                for(Enemy e : Level.enemies){
                    if(intersectingEnemy(e)) {
                        hit(e);
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void hit(Enemy targetEnemy){
        die(true);
        dealDirectDamage(targetEnemy);
        dealSplashDamage(targetEnemy);
    }

    private void dealDirectDamage(Enemy targetEnemy){
        if(targetEnemy != null){ // Hit target for full damage.
            dealDamageTo(targetEnemy, true);
        }
    }

    private void dealSplashDamage(Enemy targetEnemy){
        if(splashRadius > 0) {
            for (Enemy e : Level.enemies) {
                if (targetEnemy == null || e != targetEnemy)
                    dealDamageTo(e, false);
            }
        }
    }

    private void dealDamageTo(Enemy targetEnemy, boolean isMax){
        if(isMax) {
            RATIO = 1;
        }
        else {
            RATIO = 1f - distanceFromEnemy(targetEnemy) / (float) splashRadius;
        }
        damage = maxDamage * RATIO;
        for(Ability ability : tower.abilities){
            if(ability.type == Ability.Type.Hit_Effect && (ability.target != Ability.Target.Direct || isMax) && ((ability.target != Ability.Target.Indirect_Equal && ability.target != Ability.Target.Indirect_Unequal) || !isMax))
                ability.useOn(targetEnemy, RATIO, tower);
        }
        if(damage > 0) {
            damage *= AttackType.attackTypes[attackType].multipliers[targetEnemy.renderable.armorType];
            targetEnemy.renderable.health -= damage;
            Level.animEffects.add(new AnimEffect("-" + (int)damage, 1, Color.RED, (int) targetEnemy.renderable.x, (int) targetEnemy.renderable.y, 20, 2, tower.ownerID));
            if (targetEnemy.renderable.health <= 0) {
                Enemy.kill(targetEnemy, tower);
            }
        }
    }

    private float distanceFrom(float targetX, float targetY){
        return collisionCenter.dst(targetX, targetY);
    }

    private float distanceFromEnemy(Enemy targetEnemy){
        return distanceFrom(targetEnemy.collisionPos.x, targetEnemy.collisionPos.y) - targetEnemy.collisionRadius();
    }

    private boolean intersectingEnemy(Enemy targetEnemy){
        return distanceFromEnemy(targetEnemy) <= hitRadius;
    }

    private void die(boolean addEffect){
        if(addEffect && hasAnimEffect)
            Level.animEffects.add(new AnimEffect(this));
        isAlive = false;
    }

    private void updateTexturePosition(){
        renderable.direction = renderable.direction.nor();
        renderable.texturePosition.set(collisionCenter.x - renderable.direction.x * (renderable.width() / 2 - hitRadius), collisionCenter.y - renderable.direction.y * (renderable.width() / 2 - hitRadius));
    }
}
