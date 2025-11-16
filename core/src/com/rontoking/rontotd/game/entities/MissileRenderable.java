package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Renderer;

public class MissileRenderable {
    public int index, frameNum, currentFrame, height;
    public Vector2 texturePosition, direction;

    public MissileRenderable(){

    }

    public MissileRenderable(int height, int frameNum){
        this.height = height;
        this.frameNum = frameNum;
    }

    public MissileRenderable(int index, int x, int y, Tower tower){
        this.index = index;
        this.height =  Tower.towers[this.index].missile.renderable.height;
        this.frameNum =  Tower.towers[this.index].missile.renderable.frameNum;

        this.currentFrame = 0;
        this.direction = new Vector2();
        this.texturePosition = new Vector2(x * GameState.tileSize + GameState.tileSize / 2, y * GameState.tileSize + tower.missileYOffset * GameState.tileSize);
    }

    public void render(){
        Renderer.drawWorldTextureRectRotated(texture(), (int) texturePosition.x, (int) texturePosition.y, width(), height, 0, texture().getHeight() / frameNum * currentFrame, texture().getWidth(), texture().getHeight() / frameNum, direction.angle());
    }

    public Texture texture(){
        return Assets.missiles[index];
    }

    public int width(){
        return texture().getWidth() * height / texture().getHeight() * frameNum;
    }
}
