package com.rontoking.rontotd.game.systems.networking;

import com.badlogic.gdx.math.Vector3;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.Camera;
import com.rontoking.rontotd.game.systems.Renderer;

public class PlayerFrontend {
    public int cursorX;
    public int cursorY;
    public int gold;
    public int lives;
    public boolean isReady;

    public PlayerFrontend(){

    }

    public PlayerFrontend(int cursorX, int cursorY, int gold, int lives, boolean isReady){
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.gold = gold;
        this.lives = lives;
        this.isReady = isReady;
    }

    public void render(){
        Renderer.drawWorldTexture(Assets.cursor, cursorX, cursorY, Assets.cursor.getWidth(), Assets.cursor.getHeight());
    }

    public void updateCursorPos(){
        Vector3 gamePos = Camera.cursorGamePos();
        cursorX = (int)gamePos.x;
        cursorY = (int)gamePos.y;
    }
}
