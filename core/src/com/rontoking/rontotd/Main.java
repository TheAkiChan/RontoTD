package com.rontoking.rontotd;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.minlog.Log;
import com.rontoking.rontotd.game.entities.Enemy;
import com.rontoking.rontotd.game.entities.Structure;
import com.rontoking.rontotd.game.entities.Tile;
import com.rontoking.rontotd.game.entities.Tower;
import com.rontoking.rontotd.game.menu.Menu;
import com.rontoking.rontotd.game.systems.*;
import com.rontoking.rontotd.game.systems.networking.Networking;

public class Main extends ApplicationAdapter {
	public static int DESKTOP_WIDTH = 1600;
	public static int DESKTOP_HEIGHT = 900;
	public static final String DESKTOP_TITLE = "RontoTD";
	public static final boolean IS_RESIZABLE = true;

	public static boolean IS_EDITOR_ENABLED = true;
	public static int windowWidth = -1;
	public static int windowHeight = -1;

	public enum State{
		MENU, GAME
	}

	public static State state = State.MENU;

	@Override
	public void create () {
		//Log.set(Log.LEVEL_DEBUG);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
		Gdx.input.setCursorCatched(true);
		Gdx.input.setInputProcessor(new InputHandler());

		Settings.load();
		Camera.load();
		Renderer.load();
		Chat.load();
		Shop.load();
		Tile.load();
		Structure.load();
		Enemy.load();
		Tower.load();
		Assets.load();
		InputHandler.load();
		Menu.mainMenu();
		if(Settings.IS_FULLSCREEN){
			windowWidth = Gdx.graphics.getWidth();
			windowHeight = Gdx.graphics.getHeight();
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		}
		Assets.menuMusic.setVolume(Settings.MUSIC_VOLUME);
		Assets.menuMusic.setLooping(true);
		Assets.menuMusic.play();
	}

	public static void loadGameFiles(){
		Tower.load();
		Enemy.load();
		Assets.loadGameAssets();
		Level.load(true);
	}

	@Override
	public void render () {
		animate();
		if(state == State.MENU)
			Menu.update();
		else if(state == State.GAME) {
			InputHandler.handle();
			if(state == State.GAME) {
				AI.update();
				Chat.update();
				GameState.update();
				Self.check();
			}
		}
		Networking.update();
		if(GameState.phase == GameState.Phase.TRANSITION){
			GameState.updateTransitionPhase();
		}
		Renderer.render();
	}

	private static void animate(){
		Tile.animate();
		Structure.animate();
		Tower.animate();
	}

	@Override
	public void dispose () {
		Assets.dispose();
		Renderer.dispose();
	}

	@Override
	public void resize(int width, int height) {
		Camera.resize(width, height);
	}
}
