package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Renderer;

public class TowerRenderable {
    public int index, frameNum, x, y;

    public TowerRenderable(){

    }

    public TowerRenderable(int index, int frameNum){
        this.index = index;
        this.frameNum = frameNum;
    }

    public TowerRenderable(int index, int x, int y){
        this.index = index;
        this.x = x;
        this.y = y;

        this.frameNum = Tower.towers[index].renderable.frameNum;
    }

    public void render(){
        Renderer.drawWorldTextureRect(texture(), x* GameState.tileSize,
                y * GameState.tileSize - texture().getHeight() / frameNum * GameState.tileSize / texture().getWidth() + GameState.tileSize,
                GameState.tileSize,
                texture().getHeight() / frameNum * GameState.tileSize / texture().getWidth(),
                0, texture().getHeight() / frameNum * Tower.towers[index].currentFrame, texture().getWidth(), texture().getHeight() / frameNum);
    }

    public Texture texture(){
        return Assets.towers[index];
    }
}
