package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Renderer;

public class Tile {
    public static Tile[] tiles;

    public static void load() {
        String[] tileArray = Assets.getFile(Assets.tilePath + "/info").readString().replaceAll("\n","").split(";");
        String[] tile;
        tiles = new Tile[tileArray.length];
        for (int i = 0; i < tiles.length; i++) {
            tile = tileArray[i].split(",");
            tiles[i] = new Tile(tile[0].trim(), Boolean.parseBoolean(tile[1].trim()), Integer.parseInt(tile[2].trim()), Integer.parseInt(tile[3].trim()), Ability.parseStringToArray(tile[4]), Boolean.parseBoolean(tile[5].trim()), Boolean.parseBoolean(tile[6].trim()));
        }
    }

    public static void animate() {
        for (int i = 0; i < tiles.length; i++) {
            tiles[i].frameTimeLeft--;
            if (tiles[i].frameTimeLeft <= 0) {
                tiles[i].frameTimeLeft = tiles[i].frameTime;
                tiles[i].currentFrame++;
                if (tiles[i].currentFrame >= tiles[i].frameNum)
                    tiles[i].currentFrame = 0;
            }
        }
    }

    public String name;
    public boolean canHaveTower, canHaveEnemy, isWater;
    public int frameNum;
    public int frameTime;

    public int currentFrame;
    private int frameTimeLeft;

    public Ability[] abilities;

    public Tile(String name, boolean canHaveTower, int frameNum, int frameTime, Ability[] abilities, boolean canHaveEnemy, boolean isWater){
        this.name = name;
        this.canHaveTower = canHaveTower;
        this.frameNum = frameNum;
        this.frameTime = frameTime;
        this.canHaveEnemy = canHaveEnemy;
        this.isWater = isWater;

        this.currentFrame = 0;
        this.frameTimeLeft = this.frameTime;

        this.abilities = abilities;
    }

    public static void render(int index, int x, int y){
        if(index != -1)
            Renderer.drawWorldTextureRect(texture(index), x* GameState.tileSize, y*GameState.tileSize, GameState.tileSize, GameState.tileSize, 0, texture(index).getHeight() / tiles[index].frameNum * tiles[index].currentFrame, texture(index).getWidth(), texture(index).getHeight() / tiles[index].frameNum );
    }

    public static Texture texture(int index){
        return Assets.tiles[index];
    }

    public static int indexOf(String name){
        for(int i = 0; i < tiles.length; i++){
            if(tiles[i].name.equals(name))
                return i;
        }
        return -1;
    }
}
