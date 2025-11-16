package com.rontoking.rontotd.game.systems.networking.packets;

import com.rontoking.rontotd.game.entities.*;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Level;
import com.rontoking.rontotd.game.systems.Self;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.PlayerFrontend;

public class UpdatePacket {
    public static int serverEnemyDeathIndex = -1;
    public static boolean BOOM_SOUND_PLAYED = false;
    private static int CURRENT_ORDER_ID = Integer.MIN_VALUE;
    private static final int ORDER_RESET_DIF = 1000000000;

    public static UpdatePacket clientUpdatePacket; // Used for thread-safe purposes.
    public static boolean HAS_NEW_UPDATE_PACKET = false;

    public PlayerFrontend[] playerFrontend;
    public EnemyRenderable[] enemyRenderables;
    public MissileRenderable[] missileRenderables;
    public Sorted[] sorted;
    public AnimEffect[] animEffects;
    public SpawnerRenderable[] spawnerRenderables;

    public GameState.Phase phase;

    public int enemyDeathIndex;
    public int orderId;

    public boolean boomSoundPlayed;

    public UpdatePacket(){

    }

    public static UpdatePacket newUpdatePacket(){
        UpdatePacket updatePacket = new UpdatePacket();
        updatePacket.playerFrontend = Networking.playerFrontend.items;
        updatePacket.enemyRenderables = Level.enemyRenderables.items;
        updatePacket.missileRenderables = Level.missileRenderables.items;
        updatePacket.sorted = Level.sorted.items;
        updatePacket.animEffects = Level.animEffects.items;
        updatePacket.spawnerRenderables = Level.spawnerRenderable;

        updatePacket.phase = GameState.phase;
        updatePacket.orderId = CURRENT_ORDER_ID;
        CURRENT_ORDER_ID++;

        updatePacket.enemyDeathIndex = serverEnemyDeathIndex;
        serverEnemyDeathIndex = -1;
        updatePacket.boomSoundPlayed = BOOM_SOUND_PLAYED;
        BOOM_SOUND_PLAYED = false;

        return updatePacket;
    }

    public void load(){
        HAS_NEW_UPDATE_PACKET = false;
        if(orderId >= CURRENT_ORDER_ID || CURRENT_ORDER_ID - orderId >= ORDER_RESET_DIF) {
            CURRENT_ORDER_ID = orderId;
            Networking.playerFrontend.clear();
            for(int i = 0; i < playerFrontend.length; i++){
                if(playerFrontend[i] != null)
                    Networking.playerFrontend.add(playerFrontend[i]);
            }

            Self.isReady = Networking.playerFrontend.get(Networking.client.getID()).isReady;
            Self.lives = Networking.playerFrontend.get(Networking.client.getID()).lives;
            Self.gold = Networking.playerFrontend.get(Networking.client.getID()).gold;


            Level.enemyRenderables.clear();
            for(int i = 0; i < enemyRenderables.length; i++){
                if(enemyRenderables[i] != null)
                    Level.enemyRenderables.add(enemyRenderables[i]);
            }
            Level.missileRenderables.clear();
            for(int i = 0; i < missileRenderables.length; i++){
                if(missileRenderables[i] != null)
                    Level.missileRenderables.add(missileRenderables[i]);
            }
            Level.sorted.clear();
            for(int i = 0; i < sorted.length; i++){
                if(sorted[i] != null)
                    Level.sorted.add(sorted[i]);
            }
            Level.animEffects.clear();
            for(int i = 0; i < animEffects.length; i++){
                if(animEffects[i] != null)
                    Level.animEffects.add(animEffects[i]);
            }
            Level.spawnerRenderable = spawnerRenderables;

            GameState.phase = phase;

            Enemy.playDeathSound(enemyDeathIndex);
            if(boomSoundPlayed)
                Tower.playBoomSound();
        }
    }
}
