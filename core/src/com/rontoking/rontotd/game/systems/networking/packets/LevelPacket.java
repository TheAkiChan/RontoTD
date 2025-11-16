package com.rontoking.rontotd.game.systems.networking.packets;

import com.rontoking.rontotd.game.systems.GameState;

public class LevelPacket {
    public int num;
    public boolean clearTowers;

    public LevelPacket(){

    }

    public static LevelPacket newLevelPacket(int num, boolean clearTowers){
        LevelPacket levelPacket = new LevelPacket();
        levelPacket.num = num;
        levelPacket.clearTowers = clearTowers;
        return levelPacket;
    }

    public void load(){
        GameState.setToTransition(num, 2 * 60, clearTowers);
    }
}
