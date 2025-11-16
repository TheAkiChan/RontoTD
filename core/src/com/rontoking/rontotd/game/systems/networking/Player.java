package com.rontoking.rontotd.game.systems.networking;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.game.systems.Level;

public class Player {
    public PlayerFrontend playerFrontend;

    public boolean isListenerAlive;
    public Array<Object> queue;
    public Object queueObject;

    public Player(){
        this.playerFrontend = new PlayerFrontend(0, 0, Level.gold, Level.lives, false);
        this.isListenerAlive = false;
        this.queue = new Array<Object>();
        this.queueObject = new Object();
    }
}
