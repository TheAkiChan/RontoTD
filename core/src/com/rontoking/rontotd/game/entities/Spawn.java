package com.rontoking.rontotd.game.entities;

public class Spawn {
    public int frameWait;
    private int framesLeft;
    public int enemyType;
    public int enemyNum;

    public Spawn(int frameWait, int enemyType, int enemyNum){
        this.frameWait = frameWait;
        this.framesLeft = frameWait;
        this.enemyType = enemyType;
        this.enemyNum = enemyNum;
    }

    public Spawn(){
        this.frameWait = 60;
        this.framesLeft = frameWait;
        this.enemyType = 0;
        this.enemyNum = 10;
    }

    public boolean update(){
        framesLeft--;
        if(framesLeft <= 0){
            framesLeft = frameWait;
            enemyNum--;
            return true;
        }
        return false;
    }
}
