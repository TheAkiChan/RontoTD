package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Renderer;

public class SpawnerRenderable {
    public int x, y;
    public String text;

    public SpawnerRenderable(){

    }

    public SpawnerRenderable(int x, int y){
        this.x = x;
        this.y = y;
        this.text = "";
    }

    public void render(){
        Renderer.drawWorldTexture(Assets.spawner, x* GameState.tileSize, y*GameState.tileSize, GameState.tileSize, GameState.tileSize);
        Renderer.drawWorldTextCentered(text, Assets.spawnerFont, x*GameState.tileSize + GameState.tileSize / 2, y*GameState.tileSize + GameState.tileSize / 2, Color.RED);
    }
}
