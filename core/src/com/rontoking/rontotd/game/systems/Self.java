package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.Gdx;
import com.rontoking.rontotd.game.entities.Spawner;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.general.Point;

public class Self {
    public static int lives;
    public static int gold = 100;
    public static Point selectedTile;
    public static boolean isReady;

    public static void check(){
        if(GameState.phase == GameState.Phase.DEFEND && Networking.state != Networking.State.CLIENT && (Networking.state == Networking.State.SERVER || Gdx.input.isCursorCatched())) {
            if(alive())
                win();
            else
                loss();
        }
    }

    private static void win(){
        if(Level.enemies.size == 0 && Spawner.waveDone()) {
            Spawner.nextWave();
            if(Spawner.allWavesDone()) { // Next level.
                if (Assets.finalLevel >= Level.num + 1)
                    GameState.setToTransition(Level.num + 1, 60 * 2, true);
                else
                    GameState.setToTransition(0, 60 * 2, true); // All levels beaten. Game is won.
            }else{ // Next wave.
                GameState.phase = GameState.Phase.BUILD;
                Self.isReady = false;
                if(Networking.state == Networking.State.SERVER)
                    Networking.resetReady();
            }
        }
    }

    private static void loss(){
        if(Networking.state != Networking.State.SERVER)
            lives = 0;
        GameState.setToTransition(Level.num, 60 * 2, true);
    }

    private static boolean alive(){
        if(Networking.state == Networking.State.SERVER){
            for(int i = 0; i < Networking.playerFrontend.size; i++){
                if(Networking.playerFrontend.get(i).lives > 0)
                    return true;
                Networking.playerFrontend.get(i).lives = 0;
            }
            return false;
        }
        else return lives > 0;
    }
}
