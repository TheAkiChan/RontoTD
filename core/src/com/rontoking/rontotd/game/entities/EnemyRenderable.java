package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Renderer;
import com.rontoking.rontotd.game.systems.Settings;

public class EnemyRenderable {
    public enum Side{
        DOWN, LEFT, RIGHT, UP
    }

    public enum TransportType{
        Walking, Swimming, Flying
    }

    public static final int SIDE_NUM = Side.values().length;
    public static final Color HEALTH_BAR_COLOR = new Color(0, 0.5f, 0, 1);
    public static final int HEALTH_BAR_WIDTH = GameState.tileSize / 2;
    public static final int HEALTH_BAR_HEIGHT = GameState.tileSize / 16;
    public static final int HEALTH_BAR_OFFSET = GameState.tileSize / 4;

    public int index;
    public float x, y;
    public float health;
    public Rectangle textureRect;

    public String name;
    public float moveSpeed;
    public int gold;
    public TransportType transportType;
    public int armorType;
    public int damage;

    public Side side;
    public int frame;
    public int frameTimeLeft;

    public int frameNum;
    public int frameTime;

    public EnemyRenderable(){

    }

    public EnemyRenderable(String name, float health, float moveSpeed, int gold, TransportType transportType, int armorType, int frameNum, int frameTime, int damage){
        this.health = health;
        this.frameNum = frameNum;
        this.frameTime = frameTime;

        this.name = name;
        this.moveSpeed = moveSpeed;
        this.gold = gold;
        this.transportType = transportType;
        this.armorType = armorType;
        this.damage = damage;
    }

    public EnemyRenderable(int index, int tileX, int tileY){
        this.index = index;
        this.x = tileX * GameState.tileSize + GameState.tileSize / 2;
        this.y = tileY * GameState.tileSize + GameState.tileSize / 2;

        this.name = Enemy.enemies[index].renderable.name;
        this.moveSpeed = Enemy.enemies[index].renderable.moveSpeed;
        this.gold = Enemy.enemies[index].renderable.gold;
        this.transportType = Enemy.enemies[index].renderable.transportType;
        this.armorType =  Enemy.enemies[index].renderable.armorType;
        this.damage = Enemy.enemies[index].renderable.damage;

        this.health = Enemy.enemies[index].renderable.health;
        this.frameNum = Enemy.enemies[index].renderable.frameNum;
        this.frameTime = Enemy.enemies[index].renderable.frameTime;

        this.side = Side.DOWN;
        this.frame = 0;
        this.frameTimeLeft = 0;

        this.textureRect = textureRect();
    }

    public void render(){
        Renderer.drawWorldTextureRect(texture(),
                (int) textureRect.x,
                (int) textureRect.y,
                (int) textureRect.width,
                (int) textureRect.height,
                frame * texture().getWidth() / frameNum, side.ordinal() * texture().getHeight() / SIDE_NUM, texture().getWidth() / frameNum, texture().getHeight() / SIDE_NUM);
        renderHealthBar();
    }

    public String getDescription(){
        return "Name: " + name + "\n" +
                "Health: " + (int) health + "/" + (int) Enemy.enemies[index].renderable.health + " (" + (int) (health / Enemy.enemies[index].renderable.health * 100f) + "%)" + "\n" +
                "Movement Speed: " + moveSpeed + "\n" +
                "Gold Given: " + gold + "\n" +
                "Transport Type: " + transportType.name() + "\n" +
                "Armor Type: " + Enemy.armorTypes[armorType] + "\n" +
                "Damage: " + damage + abilityDescription();
    }

    private String abilityDescription(){
        if(Enemy.enemies[index].abilities.length > 0){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n\nAbilities: ");
            for(int i = 0; i < Enemy.enemies[index].abilities.length; i++){
                stringBuilder.append("\n" + Enemy.enemies[index].abilities[i].name());
            }
            return stringBuilder.toString();
        }
        return "";
    }

    private void renderHealthBar(){
        if(Settings.SHOW_HEALTH_BARS) {
            Renderer.gameBatch.setColor(Color.RED);
            Renderer.drawWorldTexture(Assets.pixel,
                    (int) x - HEALTH_BAR_OFFSET,
                    (int) y - texture().getHeight() * GameState.tileSize / texture().getWidth() + GameState.tileSize - HEALTH_BAR_HEIGHT,
                    HEALTH_BAR_WIDTH,
                    HEALTH_BAR_HEIGHT);
            Renderer.gameBatch.setColor(HEALTH_BAR_COLOR);
            Renderer.drawWorldTexture(Assets.pixel,
                    (int) x - HEALTH_BAR_OFFSET,
                    (int) y - texture().getHeight() * GameState.tileSize / texture().getWidth() + GameState.tileSize - HEALTH_BAR_HEIGHT,
                    (int) (HEALTH_BAR_WIDTH * hpRatio()),
                    HEALTH_BAR_HEIGHT);
            Renderer.gameBatch.setColor(Color.WHITE);
        }
    }

    private float hpRatio(){
        return health / Enemy.enemies[index].renderable.health;
    }

    public Texture texture(){
        return Assets.enemies[index];
    }

    public Rectangle textureRect(){
        return new Rectangle((int)x - width() / 2, (int)y - height() / 2, width(), height());
    }

    public int width(){
        return GameState.tileSize / 2;
    }

    public int height(){
        return texture().getHeight() / SIDE_NUM * (GameState.tileSize / 2) / (texture().getWidth() / frameNum);
    }
}
