package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.rontoking.rontotd.game.entities.AnimEffect;
import com.rontoking.rontotd.game.entities.Enemy;
import com.rontoking.rontotd.game.entities.Structure;
import com.rontoking.rontotd.game.entities.Tile;
import com.rontoking.rontotd.game.entities.Tower;
import com.rontoking.rontotd.game.menu.Menu;

public class Assets {
	public static Texture[] tiles, structures, towers, shopTowers, enemies, missiles, missileAnimEffects, animEffects;
	public static Sound[] enemySounds;
	public static Music menuMusic, buildMusic, defendMusic;
	public static Texture pixel, start, options, selection, back, remove, spawner, shadow, arrow, cursor, owned, shopSelection, upgradeSelection;
	public static Sound placeSound, boomSound;

	private static FreeTypeFontGenerator generator;
	private static FreeTypeFontGenerator.FreeTypeFontParameter parameter;
	public static BitmapFont topFont, shopFont, nameFont, descriptionFont, spawnerFont, chatFont;
	public static BitmapFont[] animEffectFonts;

	public static String level;
	public static int finalLevel;

	public static final String animEffectPath = "animation effects";
	public static final String enemyPath = "enemies";
	public static final String fontPath = "fonts";
	public static final String levelPath = "levels";
	public static final String missilePath = "missiles";
	public static final String missileAnimEffectPath = "animation effects/missiles";
	public static final String structurePath = "structures";
	public static final String tilePath = "tiles";
	public static final String towerIconPath = "tower icons";
	public static final String towerPath = "towers";

	public static final String iconPath = "icons";
	public static final String gameGuiPath = "gui";
	public static final String menuPath = "menu";
	public static final String shopPath = "shop";
	public static final String musicPath = "music";

	public static void load() {
		setFinalLevel();
		loadGUIAssets();
		loadGameAssets();
		loadMusic();
	}

	public static void setFinalLevel(){
		finalLevel = Gdx.files.local(levelPath).list().length - 1;
	}

	public static void loadGameAssets(){
		loadTiles();
		loadStructures();
		loadTowers();
		loadShopTowers();
		loadEnemies();
		loadMissiles();
		loadMissileAnimEffects();
		loadAnimEffects();
	}

	private static void loadGUIAssets(){
		loadMenu();
		loadGameGUI();
		loadFonts();
	}

	private static void loadMusic(){
		menuMusic = newMusic(musicPath + "/menu");
		buildMusic = newMusic(musicPath + "/build");
		defendMusic = newMusic(musicPath + "/defend");
	}

	public static void dispose() {
		Menu.singleplayerTexture.dispose();
		Menu.lanMultiplayerTexture.dispose();
		Menu.logoTexture.dispose();
		Menu.hostTexture.dispose();
		Menu.autoJoinTexture.dispose();
		Menu.manualJoinTexture.dispose();

		Menu.backToGameTexture.dispose();
		Menu.settingsTexture.dispose();
		Menu.exitToMenuTexture.dispose();
		Menu.quitGameTexture.dispose();
		Menu.backTexture.dispose();
		Menu.startTexture.dispose();
		Menu.audioTexture.dispose();
		Menu.graphicsTexture.dispose();
		Menu.checkboxTexture.dispose();
		Menu.toggleFullscreenTexture.dispose();
		Menu.newGameTexture.dispose();
		Menu.continueTexture.dispose();

		for(Texture t : tiles)
			t.dispose();
		for(Texture t : structures)
			t.dispose();
		for(Texture t : towers)
			t.dispose();
		for(Texture t : shopTowers)
			t.dispose();
		for(Texture t : enemies)
			t.dispose();
		for(Texture t : missiles)
			t.dispose();
		for(Texture t : missileAnimEffects)
			if(t != null)
				t.dispose();
		for(Texture t : animEffects)
			t.dispose();
		for(Sound s : enemySounds)
			s.dispose();
		spawner.dispose();
		shadow.dispose();
		pixel.dispose();
		start.dispose();
		options.dispose();
		selection.dispose();

		back.dispose();
		remove.dispose();
		arrow.dispose();
		shopSelection.dispose();

		cursor.dispose();
		owned.dispose();
		upgradeSelection.dispose();

		placeSound.dispose();
		boomSound.dispose();
		Menu.clickSound.dispose();

		topFont.dispose();
		shopFont.dispose();
		nameFont.dispose();
		descriptionFont.dispose();
		spawnerFont.dispose();
		chatFont.dispose();
		for(BitmapFont f : animEffectFonts)
			f.dispose();
		Menu.menuFont.dispose();

		menuMusic.dispose();
		buildMusic.dispose();
		defendMusic.dispose();
	}

	private static void loadMenu(){
		Menu.singleplayerTexture = newTexture(menuPath + "/singleplayerButton");
		Menu.lanMultiplayerTexture = newTexture(menuPath + "/onlineMultiplayerButton");
		Menu.logoTexture = newTexture(menuPath + "/logo");
		Menu.hostTexture = newTexture(menuPath + "/hostButton");
		Menu.autoJoinTexture = newTexture(menuPath + "/autoJoinButton");
		Menu.manualJoinTexture = newTexture(menuPath + "/manualJoinButton");

		Menu.backToGameTexture = newTexture(menuPath + "/backToGameButton");
		Menu.settingsTexture = newTexture(menuPath + "/settingsButton");
		Menu.exitToMenuTexture = newTexture(menuPath + "/exitToMenuButton");
		Menu.quitGameTexture = newTexture(menuPath + "/quitGameButton");
		Menu.backTexture = newTexture(menuPath + "/backButton");
		Menu.startTexture = newTexture(menuPath + "/startButton");
		Menu.audioTexture = newTexture(menuPath + "/audioButton");
		Menu.graphicsTexture = newTexture(menuPath + "/graphicsButton");
		Menu.checkboxTexture = newTexture(menuPath + "/checkbox");
		Menu.markerTexture = newTexture(menuPath + "/marker");
		Menu.toggleFullscreenTexture = newTexture(menuPath + "/toggleFullscreenButton");
		Menu.newGameTexture = newTexture(menuPath + "/newGameButton");
		Menu.continueTexture = newTexture(menuPath + "/continueButton");

		Menu.clickSound = newSound(menuPath + "/click");
	}
	
	public static void loadLevel() {
		level = getFile(levelPath + "/" + Level.num).readString().replaceAll("\\s+","");
	}

	private static void loadGameGUI() {
		pixel = newTexture(gameGuiPath + "/pixel");
		start = newTexture(gameGuiPath + "/start");
		options = newTexture(gameGuiPath + "/options");
		selection = newTexture(gameGuiPath + "/selection");
		cursor = newTexture(gameGuiPath + "/cursor");
		owned = newTexture(gameGuiPath + "/owned");
		upgradeSelection = newTexture(gameGuiPath + "/upgrade");

		back = newTexture(shopPath + "/back");
		remove = newTexture(shopPath + "/remove");
		arrow = newTexture(shopPath + "/arrow");
		shopSelection = newTexture(shopPath + "/selection");
	}

	public static void loadTiles() {
		tiles = new Texture[Tile.tiles.length];
		for(int i = 0; i < tiles.length; i++) {
			tiles[i] = newTexture(tilePath + "/" + Tile.tiles[i].name);
		}
	}

	public static void loadStructures() {
		structures = new Texture[Structure.structures.length];
		for(int i = 0; i < structures.length; i++) {
			structures[i] = newTexture(structurePath + "/" + Structure.structures[i].name);
		}
	}

	public static void loadTowers(){
		towers = new Texture[Tower.towers.length];
		for(int i = 0; i < towers.length; i++) {
			towers[i] = newTexture(towerPath + "/" + Tower.towers[i].name);
		}

		placeSound = newSound(towerPath + "/place");
		boomSound = newSound(towerPath + "/boom");
	}

	public static void loadEnemies(){
		spawner = newTexture(enemyPath + "/spawner");
		shadow = newTexture(enemyPath + "/shadow");
		enemies = new Texture[Enemy.enemies.length];
		enemySounds = new Sound[Enemy.enemies.length];
		for(int i = 0; i < enemies.length; i++) {
			enemies[i] = newTexture(enemyPath + "/" + Enemy.enemies[i].renderable.name);
			enemySounds[i] = newSound(enemyPath + "/" + Enemy.enemies[i].renderable.name);
		}
	}

	public static void loadMissiles(){
		missiles = new Texture[Tower.towers.length];
		for(int i = 0; i < missiles.length; i++) {
			missiles[i] = newTexture(missilePath + "/" + Tower.towers[i].name);
		}
	}

	public static void loadMissileAnimEffects(){
		missileAnimEffects = new Texture[Tower.towers.length];
		for(int i = 0; i < missileAnimEffects.length; i++) {
			if(Tower.towers[i].missile.hasAnimEffect)
				missileAnimEffects[i] = newTexture(missileAnimEffectPath + "/" + Tower.towers[i].name);
		}
	}

	private static void loadAnimEffects(){
		animEffects = new Texture[AnimEffect.Special.values().length];
		for(int i = 0; i < animEffects.length; i++){
			animEffects[i] = newTexture(animEffectPath + "/" + AnimEffect.Special.values()[i].name());
		}
	}

	public static void loadShopTowers(){
		shopTowers = new Texture[Tower.towers.length];
		for(int i = 0; i < shopTowers.length; i++) {
			shopTowers[i] = newTexture(towerIconPath + "/" + Tower.towers[i].name);
		}
	}

	private static void loadFonts() {
		generator = new FreeTypeFontGenerator(getFile(fontPath + "/cavestory.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.magFilter = Texture.TextureFilter.Nearest;
		parameter.minFilter = Texture.TextureFilter.Nearest;
        parameter.color = Color.WHITE;

		parameter.size = Renderer.TOP_GUI_HEIGHT;
		topFont = generator.generateFont(parameter);

		parameter.size = (int)(Renderer.BOT_GUI_HEIGHT / 2.5f);
		shopFont = generator.generateFont(parameter);

		parameter.size = (int)(Renderer.BOT_GUI_HEIGHT / 5f);
		nameFont = generator.generateFont(parameter);

		parameter.size = Renderer.BOT_GUI_HEIGHT;
		Menu.menuFont = generator.generateFont(parameter);

		parameter.size = (int)(Renderer.BOT_GUI_HEIGHT / 4f);
		descriptionFont = generator.generateFont(parameter);

		parameter.size = (int)(Renderer.BOT_GUI_HEIGHT / 4f);
		parameter.flip = Camera.Y_DOWN;
		spawnerFont = generator.generateFont(parameter);

		animEffectFonts = new BitmapFont[2];

		parameter.size = (int)(Renderer.BOT_GUI_HEIGHT / 4f);
		animEffectFonts[0] = generator.generateFont(parameter);

		parameter.size = (int)(Renderer.BOT_GUI_HEIGHT / 5f);
		animEffectFonts[1] = generator.generateFont(parameter);

		parameter.flip = !Camera.Y_DOWN;
		parameter.size = (int)(Renderer.BOT_GUI_HEIGHT / 2.5f);
		parameter.shadowColor = Color.BLACK;
		parameter.shadowOffsetX = 3;
		parameter.shadowOffsetY = 3;
		chatFont = generator.generateFont(parameter);

		generator.dispose();
	}

	private static Texture newTexture(String name) {
		return new Texture(getFile(name.toLowerCase() + ".png"));
	}
	private static Sound newSound(String name){
		return Gdx.audio.newSound(getFile(name.toLowerCase() + ".ogg"));
	}
	private static Music newMusic(String name){
		return Gdx.audio.newMusic(getFile(name.toLowerCase() + ".ogg"));
	}
	public static FileHandle getFile(String path){
		if(Gdx.app.getType() == Application.ApplicationType.Desktop)
			return  Gdx.files.local(path);
		return  Gdx.files.internal(path);
	}
}
