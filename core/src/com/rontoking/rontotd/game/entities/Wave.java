package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.utils.Array;

public class Wave {
    public Array<Spawn> spawns;

    public Wave(){
        this.spawns = new Array<Spawn>();
    }

    public Wave(Array<Spawn> spawns){
        this.spawns = spawns;
    }
}
