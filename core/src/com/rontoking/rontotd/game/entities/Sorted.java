package com.rontoking.rontotd.game.entities;

import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Level;
import com.rontoking.rontotd.game.systems.Settings;

public class Sorted {
    public enum Type{
        TOWER, ENEMY, ANIM_EFFECT
    }

    public static final int FLIGHT_SORT_BONUS = 1000000;

    public TowerRenderable towerRenderable;
    public EnemyRenderable enemyRenderable;
    public AnimEffect animEffect;
    public Type type;
    public int sortValue, id;

    public Sorted(){

    }

    public Sorted(Tower tower){
        this.type = Type.TOWER;
        this.towerRenderable = tower.renderable;
        this.enemyRenderable = null;
        this.animEffect = null;

        this.sortValue = this.towerRenderable.y * GameState.tileSize + GameState.tileSize / 2;
        this.id = tower.id;
    }

    public Sorted(Enemy enemy){
        this.type = Type.ENEMY;
        this.towerRenderable = null;
        this.enemyRenderable = enemy.renderable;
        this.animEffect = null;

        this.sortValue = (int)this.enemyRenderable.y;
        if(enemy.renderable.transportType == EnemyRenderable.TransportType.Flying)
            this.sortValue += FLIGHT_SORT_BONUS;
        this.id = enemy.id;
    }

    public Sorted(AnimEffect animEffect){
        this.type = Type.ANIM_EFFECT;
        this.enemyRenderable = null;
        this.towerRenderable = null;
        this.animEffect = animEffect;

        this.sortValue = this.animEffect.y;
        this.id = this.animEffect.id;
    }

    public void render(){
        if(type == Type.TOWER)
            towerRenderable.render();
        else if(type == Type.ENEMY)
            enemyRenderable.render();
        else if(type == Type.ANIM_EFFECT)
            if(Settings.SHOW_ANIM_EFFECTS)
                animEffect.render();
    }

    public static void addTower(Tower tower){
        int i = 0;
        Sorted sorted = new Sorted(tower);
        while(i < Level.sorted.size && sorted.sortValue > Level.sorted.get(i).sortValue){
            i++;
        }
        Level.sorted.insert(i, sorted);
    }

    public static void addEnemy(Enemy enemy){
        int i = 0;
        Sorted sorted = new Sorted(enemy);
        while(i < Level.sorted.size && sorted.sortValue > Level.sorted.get(i).sortValue){
            i++;
        }
        Level.sorted.insert(i, sorted);
    }

    public static void addAnimEffect(AnimEffect animEffect){
        int i = 0;
        Sorted sorted = new Sorted(animEffect);
        while(i < Level.sorted.size && sorted.sortValue > Level.sorted.get(i).sortValue){
            i++;
        }
        Level.sorted.insert(i, sorted);
    }

    public static void remove(int id){
        for(int i = 0; i < Level.sorted.size; i++){
            if(Level.sorted.get(i).id == id){
                Level.sorted.removeIndex(i);
                return;
            }
        }
    }

    public static int indexOf(int id){
        for(int i = 0; i < Level.sorted.size; i++){
            if(Level.sorted.get(i).id == id){
                return i;
            }
        }
        return -1;
    }
}