package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Level;
import com.rontoking.rontotd.game.systems.Renderer;
import com.rontoking.rontotd.game.systems.networking.Networking;

public class AnimEffect {
    private Type type;

    public int x, y, totalFrames, frameTime, playerID;
    public boolean isFinished;
    private int index, currentFrame, frameTimeLeft;

    private String text;
    private int fontIndex;
    private Color color, outlineColor;
    private int speed;

    public int id;

    public enum Special {
        Spawn, Explosion // Explosion - 21 frames
    }

    public enum Type{
        Special, Text, Missile
    }

    public AnimEffect() {

    }

    public AnimEffect(Special special, int x, int y, int totalFrames, int frameTime, boolean isSorted) {
        this.type = Type.Special;
        this.index = special.ordinal();
        this.x = x;
        this.y = y;
        this.totalFrames = totalFrames;
        this.frameTime = frameTime;
        this.playerID = -1;

        this.currentFrame = 0;
        this.isFinished = false;
        this.frameTimeLeft = this.frameTime;

        if(isSorted) {
            this.id = Level.idCounter;
            Level.idCounter++;
            Sorted.addAnimEffect(this);
        }else{
            this.id = -1;
        }
    }

    public AnimEffect(String text, int fontIndex, Color color, int x, int y, int totalFrames, int speed, int playerID) {
        this.type = Type.Text;
        this.text = text;
        this.fontIndex = fontIndex;
        this.color = new Color(color);
        this.outlineColor = new Color(Color.BLACK);
        this.index = -1;
        if(Networking.state == Networking.State.SINGLEPLAYER)
            this.playerID = -1;
        else
            this.playerID = playerID;

        this.x = x;
        this.y = y;
        this.totalFrames = totalFrames;
        this.speed = speed;

        this.frameTimeLeft = totalFrames;
        this.isFinished = false;

        this.id = -1;
    }

    public AnimEffect(Missile missile) {
        this.type = Type.Missile;
        this.index = missile.renderable.index;
        this.x = (int)missile.collisionCenter.x;
        this.y = (int)missile.collisionCenter.y;
        this.totalFrames = missile.animEffectFrameNum;
        this.frameTime = missile.animEffectFrameTime;
        this.playerID = -1;

        this.currentFrame = 0;
        this.isFinished = false;
        this.frameTimeLeft = this.frameTime;

        this.id = Level.idCounter;
        Level.idCounter++;
        Sorted.addAnimEffect(this);
    }

    public void update() {
        if (index == -1) {
            frameTimeLeft--;
            y -= speed;
            color.a = (float)frameTimeLeft*5f / (float)totalFrames;
            if(color.a > 1)
                color.a = 1;
            outlineColor.a = color.a;
            if (frameTimeLeft <= 0) {
                isFinished = true;
            }
        } else {
            frameTimeLeft--;
            if (frameTimeLeft <= 0) {
                frameTimeLeft = frameTime;
                currentFrame++;
                if (currentFrame >= totalFrames)
                    isFinished = true;
            }
        }
    }

    private int width(){
        return GameState.tileSize;
    }

    private int height(){
        return GameState.tileSize;
    }

    public void render() {
        if(playerID == -1 || (Networking.state == Networking.State.SERVER && playerID == 0) || (Networking.state == Networking.State.CLIENT && playerID == Networking.client.getID())) {
            switch (type){
                case Special:
                    drawTexturedEffect(Assets.animEffects[index]);
                    break;
                case Text:
                    Renderer.drawWorldTextCentered(text, Assets.animEffectFonts[fontIndex], x - 1, y - 1, outlineColor);
                    Renderer.drawWorldTextCentered(text, Assets.animEffectFonts[fontIndex], x, y, color);
                    break;
                case Missile:
                    drawTexturedEffect(Assets.missileAnimEffects[index]);
                    break;
                default:
                    break;
            }
        }
    }

    private void drawTexturedEffect(Texture texture){
            Renderer.drawWorldTextureRect(texture, x - width() / 2, y - height() / 2,
                    width(), height(),
                    0, texture.getHeight() / totalFrames * currentFrame, texture.getWidth(), texture.getHeight() / totalFrames);
    }
}
