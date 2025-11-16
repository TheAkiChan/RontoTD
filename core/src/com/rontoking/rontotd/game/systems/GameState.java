package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.Gdx;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.game.entities.*;
import com.rontoking.rontotd.game.menu.Menu;
import com.rontoking.rontotd.game.systems.networking.Networking;

public class GameState {
	public enum Phase{
		BUILD, DEFEND, TRANSITION
	}

	public static Phase phase = Phase.BUILD;
	public static int tileSize = 64;
	public static boolean movedMouseAfterMenu = true;

	public static float transitionProgress = 0;
	private static float transitionFramesLeft = 0;
	public static float transitionDelta = 0;
	private static boolean shouldClearTowers = false;

	public static void update() {
		updateMusic();
		if(phase == Phase.BUILD && Networking.state != Networking.State.CLIENT && (Networking.state == Networking.State.SERVER || Gdx.input.isCursorCatched()))
			updateBuildPhase();
		else if(phase == Phase.DEFEND && Networking.state != Networking.State.CLIENT && (Networking.state == Networking.State.SERVER || Gdx.input.isCursorCatched()))
			updateDefendPhase();
	}

	private static void updateMusic(){
		if(phase == Phase.BUILD && !Assets.buildMusic.isPlaying()){
			if(Assets.menuMusic.isPlaying())
				Assets.menuMusic.stop();
			if(Assets.defendMusic.isPlaying())
				Assets.defendMusic.pause();
			Assets.buildMusic.setVolume(Settings.MUSIC_VOLUME);
			Assets.buildMusic.setLooping(true);
			Assets.buildMusic.play();
		}else if(phase == Phase.DEFEND && !Assets.defendMusic.isPlaying()){
			if(Assets.menuMusic.isPlaying())
				Assets.menuMusic.stop();
			if(Assets.buildMusic.isPlaying())
				Assets.buildMusic.pause();
			Assets.defendMusic.setVolume(Settings.MUSIC_VOLUME);
			Assets.defendMusic.setLooping(true);
			Assets.defendMusic.play();
		}
	}

	private static void updateBuildPhase(){
		updateAnimEffects();
		updateMissiles();
	}

	private static void updateDefendPhase(){
		updateAnimEffects();
		updateTowers();
		updateEnemies();
		updateMissiles();
		updateSpawners();
	}

	private static void updateAnimEffects(){
		for(int i = Level.animEffects.size - 1; i >= 0; i--){
			Level.animEffects.get(i).update();
			if(Level.animEffects.get(i).isFinished) {
				if(Level.animEffects.get(i).id != -1)
					Sorted.remove(Level.animEffects.get(i).id);
				Level.animEffects.removeIndex(i);
			}
		}
	}

	private static void updateTowers(){
		if(Shop.SELECTED_TOWER_IN_GAME != -1) {
			Shop.SELECTED_TOWER_IN_GAME = -1;
			Shop.upgradeTowers.clear();
		}
		for(int i = 0; i < Level.towers.size; i++){
			Level.towers.get(i).update();
		}
	}

	private static void updateEnemies(){
		for(int i = Level.enemies.size - 1; i >= 0; i--){
			Level.enemies.get(i).update();
			if(!Level.enemies.get(i).isAlive){
				Level.enemies.get(i).die(null);
				Sorted.remove(Level.enemies.get(i).id);
				Level.enemyRenderables.removeIndex(i);
				Level.enemies.removeIndex(i);
			}
		}
	}
	private static void updateSpawners(){
		for(int i = 0; i < Level.spawners.length; i++){
			Level.spawners[i].update();
		}
	}

	private static void updateMissiles(){
		for(int i = Level.missiles.size - 1; i >= 0; i--){
			Level.missiles.get(i).update();
			if(!Level.missiles.get(i).isAlive) {
				Level.missileRenderables.removeIndex(i);
				Level.missiles.removeIndex(i);
			}
		}
	}

	public static void setToTransition(int levelNum, int frameNum, boolean clearTowers){
		Level.num = levelNum;
		if(Networking.state == Networking.State.SINGLEPLAYER) {
			Settings.LEVEL = levelNum;
			Settings.preferences.putInteger("level", Settings.LEVEL);
		}
		phase = Phase.TRANSITION;
		transitionProgress = 0;
		transitionFramesLeft = frameNum;
		transitionDelta = 2f / frameNum;
		shouldClearTowers = clearTowers;
		Networking.transition(levelNum, clearTowers);
	}

	public static void updateTransitionPhase(){
		transitionFramesLeft--;
		transitionProgress += transitionDelta;
		if(transitionProgress >= 1){
			transitionProgress = 1;
			transitionDelta *= -1;
			Main.state = Main.State.GAME;
			Level.load(shouldClearTowers);
		}
		if(transitionFramesLeft <= 0)
			phase = Phase.BUILD;
	}

	public static void quitGame(){
		if(Assets.buildMusic.isPlaying())
			Assets.buildMusic.stop();
		if(Assets.defendMusic.isPlaying())
			Assets.defendMusic.stop();
		Assets.menuMusic.setVolume(Settings.MUSIC_VOLUME);
		Assets.menuMusic.setLooping(true);
		Assets.menuMusic.play();
		Level.num = 0;
		Main.state = Main.State.MENU;
		Menu.IS_GAME_OPTIONS = false;
		Menu.mainMenu();
		if(Networking.state == Networking.State.SERVER)
			Networking.server.stop();
		else if(Networking.state == Networking.State.CLIENT)
			Networking.client.stop();
		Networking.state = Networking.State.NONE;
	}

	public static void gameOptions(){
		Main.state = Main.State.MENU;
		Menu.IS_GAME_OPTIONS = true;
		movedMouseAfterMenu = false;
		Menu.gameOptionsMenu();
		Menu.clickSound.play(Settings.MENU_VOLUME);

		InputHandler.beginTouch = null;
		InputHandler.endTouch = null;
		InputHandler.touched = false;

		if(Assets.buildMusic.isPlaying())
			Assets.buildMusic.pause();
		if(Assets.defendMusic.isPlaying())
			Assets.defendMusic.pause();
		Assets.menuMusic.setVolume(Settings.MUSIC_VOLUME);
		Assets.menuMusic.setLooping(true);
		Assets.menuMusic.play();
	}
}
