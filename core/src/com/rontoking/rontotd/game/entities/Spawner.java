package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Level;

public class Spawner {
    public SpawnerRenderable spawnerRenderable;
    public Array<Wave> waves;

    public Spawner(){

    }

    public Spawner(int x, int y, Array<Wave> waves){
        spawnerRenderable = new SpawnerRenderable(x, y);
        this.waves = waves;
        getText();
    }

    public void update(){
        if(waves.size > 0 && waves.get(0).spawns.size > 0 && waves.get(0).spawns.get(0).update()){
            Enemy.spawn(waves.get(0).spawns.get(0).enemyType, spawnerRenderable.x, spawnerRenderable.y);
            if(waves.get(0).spawns.get(0).enemyNum <= 0) {
                waves.get(0).spawns.removeIndex(0);
            }
        }
    }

    public void render(){
        getText();
        spawnerRenderable.render();
    }

    public String getText(){
        if(waves.size > 0 && waves.get(0).spawns.size > 0)
                spawnerRenderable.text = waves.get(0).spawns.size + " - " + waves.get(0).spawns.get(0).enemyNum;
        else
            spawnerRenderable.text = "";
        return spawnerRenderable.text;
    }

    public static boolean waveDone(){
        for(Spawner s : Level.spawners)
            if(s.waves.size > 0 && s.waves.get(0).spawns.size > 0)
                return false;
        return true;
    }

    public static void nextWave(){
        for(Spawner s : Level.spawners)
            if(s.waves.size > 0)
                s.waves.removeIndex(0);
    }

    public static boolean allWavesDone(){
        for(Spawner s : Level.spawners)
            if(s.waves.size > 0)
                return false;
        return true;
    }

    public static boolean exists(int x, int y){
        for(int i = 0; i < Level.spawners.length; i++){
            if(Level.spawners[i] != null && Level.spawners[i].spawnerRenderable.x == x && Level.spawners[i].spawnerRenderable.y == y)
                return true;
        }
        return false;
    }
}
