package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.game.entities.Enemy;
import com.rontoking.rontotd.game.entities.EnemyRenderable;
import com.rontoking.rontotd.game.entities.Tile;
import com.rontoking.rontotd.game.entities.Tower;
import com.rontoking.rontotd.game.systems.GameState.Phase;
import com.rontoking.rontotd.game.menu.Menu;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.packets.UpdatePacket;
import com.rontoking.rontotd.general.Utility;

public class Renderer {
	static int BOT_GUI_HEIGHT;
	static int TOP_GUI_HEIGHT;

	public static SpriteBatch gameBatch = new SpriteBatch();
	public static SpriteBatch guiBatch = new SpriteBatch();

	public static Color GUI_COLOR = new Color(0.1f, 0.1f, 0.1f, 1f);

	public static GlyphLayout glyphLayout = new GlyphLayout();

	public static void load(){
		BOT_GUI_HEIGHT = Main.DESKTOP_HEIGHT / 8;
		TOP_GUI_HEIGHT = Main.DESKTOP_HEIGHT / 16;
	}

	public static void render() {
		if(Networking.state == Networking.State.CLIENT && UpdatePacket.HAS_NEW_UPDATE_PACKET)
			UpdatePacket.clientUpdatePacket.load();
		gameBatch.setProjectionMatrix(Camera.getMatrix());
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		gameBatch.begin();
		if(Main.state == Main.State.GAME) {
			drawTileMap();
			drawShadows();
			drawOwned();
			drawSelection();
			drawStructures();
			drawSpawners();
			drawSorted();
			drawAnimEffects();
			drawMissiles();
			drawPlayers();
		}
		gameBatch.end();

		guiBatch.begin();
		if(Main.state == Main.State.MENU) {
			Menu.render();
		}else if(Main.state == Main.State.GAME){
			Chat.draw();
			drawTopGUI();
			drawBotGUI();
			drawEnemyDescription();
		}
		drawCursor();
		drawTransition();
		guiBatch.end();
	}
	
	public static void dispose() {
		gameBatch.dispose();
	}

	private static void drawEnemyDescription() {
		if (Settings.SHOW_TOOLTIPS) {
			if (GameState.phase == Phase.DEFEND) {
				Vector3 mouseGamePos = Camera.cursorGamePos();
				for (EnemyRenderable enemyRenderable : Level.enemyRenderables) {
					if (enemyRenderable.textureRect.contains(mouseGamePos.x, mouseGamePos.y)) {
						Renderer.drawText(enemyRenderable.getDescription(), Assets.descriptionFont, 20, BOT_GUI_HEIGHT + 20, Color.RED, false, true, 10, Shop.DESCRIPTION_COLOR);
						return;
					}
				}
			}
		}
	}

	private static void drawShadows(){
		for(Enemy e : Level.enemies)
			e.drawShadow();
	}

	private static void drawOwned(){
		if(GameState.phase == Phase.BUILD && (Networking.state == Networking.State.SERVER || Networking.state == Networking.State.CLIENT)) {
			for (Tower t : Level.towers) {
				if ((Networking.state == Networking.State.SERVER && t.ownerID == 0) || (Networking.state == Networking.State.CLIENT && t.ownerID == Networking.client.getID())){
					drawWorldTexture(Assets.owned, t.renderable.x * GameState.tileSize, t.renderable.y * GameState.tileSize, GameState.tileSize, GameState.tileSize);
				}
			}
		}
	}

	private static void drawPlayers(){
		if(Networking.state == Networking.State.SERVER || Networking.state == Networking.State.CLIENT){
			for(int i = 0; i < Networking.playerFrontend.size; i++){
				if((Networking.state != Networking.State.CLIENT || Networking.client.getID() != i) && (Networking.state != Networking.State.SERVER || i != 0))
						Networking.playerFrontend.get(i).render();
			}
		}
	}

	private static void drawCursor(){
		drawGUITexture(Assets.cursor, InputHandler.mousePos.x, InputHandler.mousePos.y, Assets.cursor.getWidth(), Assets.cursor.getHeight(), true);
	}

	private static void drawTileMap() {
		for(int y = 0; y < Level.tiles.length; y++) {
			for(int x = 0; x < Level.tiles[y].length; x++) {
				Tile.render(Level.tiles[y][x], x, y);
			}
		}
	}

	private static void drawStructures() {
		for(int i = 0; i < Level.structures.length; i++) {
			Level.structures[i].render();
		}
	}

	private static void drawSpawners() {
		if(Networking.state == Networking.State.CLIENT){
			for(int i = 0; i < Level.spawnerRenderable.length; i++) {
				Level.spawnerRenderable[i].render();
			}
		}else{
			for(int i = 0; i < Level.spawners.length; i++) {
				Level.spawners[i].render();
			}
		}
	}

	private static void drawSorted(){
		for(int i = 0; i < Level.sorted.size; i++)
				Level.sorted.get(i).render();
	}

	private static void drawAnimEffects(){
		if(Settings.SHOW_ANIM_EFFECTS) {
			for (int i = 0; i < Level.animEffects.size; i++)
				if (Level.animEffects.get(i).id == -1)
					Level.animEffects.get(i).render();
		}
	}

	private static void drawMissiles(){
		for(int i = 0; i < Level.missileRenderables.size; i++)
			Level.missileRenderables.get(i).render();
	}

	private static void drawSelection() {
		if (GameState.phase == Phase.BUILD) {
			if (Shop.SELECTED_TOWER_IN_GAME != -1)
				drawWorldTexture(Assets.upgradeSelection, Level.towers.get(Shop.SELECTED_TOWER_IN_GAME).renderable.x * GameState.tileSize, Level.towers.get(Shop.SELECTED_TOWER_IN_GAME).renderable.y * GameState.tileSize, GameState.tileSize, GameState.tileSize);
			else if (Self.selectedTile != null)
				drawWorldTexture(Assets.selection, Self.selectedTile.x * GameState.tileSize, Self.selectedTile.y * GameState.tileSize, GameState.tileSize, GameState.tileSize);
		}
	}

	private static void drawTopGUI() {
		guiBatch.setColor(GUI_COLOR);
		drawGUITexture(Assets.pixel, 0, 0, Main.DESKTOP_WIDTH, TOP_GUI_HEIGHT, true);
		guiBatch.setColor(Color.WHITE);

		if ((GameState.phase == Phase.BUILD || (GameState.phase == Phase.TRANSITION && GameState.transitionDelta < 0)) && !Self.isReady) {
			if (InputHandler.isButtonHovered(InputHandler.startRect, false))
				drawButton(Assets.start, (int) InputHandler.startRect.x, (int) InputHandler.startRect.y, 1, 2, (int) InputHandler.startRect.width, true);
			else
				drawButton(Assets.start, (int) InputHandler.startRect.x, (int) InputHandler.startRect.y, 0, 2, (int) InputHandler.startRect.width, true);
		}
		if (InputHandler.isButtonHovered(InputHandler.optionsRect, false))
			drawButton(Assets.options, (int) InputHandler.optionsRect.x, (int) InputHandler.optionsRect.y, 1, 2, (int) InputHandler.optionsRect.width, true);
		else
			drawButton(Assets.options, (int) InputHandler.optionsRect.x, (int) InputHandler.optionsRect.y, 0, 2, (int) InputHandler.optionsRect.width, true);
		drawText("Level: " + (Level.num + 1), Assets.topFont, Main.DESKTOP_WIDTH / 60, Main.DESKTOP_HEIGHT - (int) (TOP_GUI_HEIGHT * 0.7f), Color.WHITE, false);
		drawText("                   Lives: " + Self.lives, Assets.topFont, Main.DESKTOP_WIDTH / 60, Main.DESKTOP_HEIGHT - (int) (TOP_GUI_HEIGHT * 0.7f), Color.RED, false);
		drawText("                                        Gold: " + Self.gold, Assets.topFont, Main.DESKTOP_WIDTH / 60, Main.DESKTOP_HEIGHT - (int) (TOP_GUI_HEIGHT * 0.7f), Color.GOLD, false);
	}

	private static void drawBotGUI(){
		guiBatch.setColor(GUI_COLOR);
		drawGUITexture(Assets.pixel, 0, Main.DESKTOP_HEIGHT - BOT_GUI_HEIGHT, Main.DESKTOP_WIDTH, BOT_GUI_HEIGHT, true);
		guiBatch.setColor(Color.WHITE);
		Shop.render();
	}

	private static void drawTransition(){
		if(GameState.phase == Phase.TRANSITION){
			guiBatch.setColor(0, 0, 0, GameState.transitionProgress);
			guiBatch.draw(Assets.pixel, 0, 0, Main.DESKTOP_WIDTH, Main.DESKTOP_HEIGHT);
			guiBatch.setColor(Color.WHITE);
		}
	}
	
	private static void drawButton(Texture t, int x, int y, int frame, int frameNum, int width, boolean isOriginTopLeft) {
		if(isOriginTopLeft)
			guiBatch.draw(t, x, Main.DESKTOP_HEIGHT - y - t.getHeight() / frameNum * width / t.getWidth(), width, t.getHeight() / frameNum * width / t.getWidth(), 0, t.getHeight() / frameNum * frame, t.getWidth(), t.getHeight() / frameNum, false, false);
		else
			guiBatch.draw(t, x, y, width, t.getHeight() / frameNum * width / t.getWidth(), 0, t.getHeight() / frameNum * frame, t.getWidth(), t.getHeight() / frameNum, false, false);
	}

	public static void drawGUITexture(Texture t, int x, int y, int width, int height, boolean isOriginTopLeft){
		if(isOriginTopLeft)
			y = Main.DESKTOP_HEIGHT - y - height;
		guiBatch.draw(t, x, y, width, height);
	}

	public static void drawGUITextureRect(Texture t, int x, int y, int width, int height, int rectX, int rectY, int rectWidth, int rectHeight, boolean isOriginTopLeft){
		if(isOriginTopLeft)
			y = Main.DESKTOP_HEIGHT - y - height;
		guiBatch.draw(t, x, y, width, height, rectX, rectY, rectWidth, rectHeight, false, false);
	}

	public static void drawWorldTexture(Texture t, int x, int y, int width, int height) {
		gameBatch.draw(t, x, y, width, height, 0, 0, t.getWidth(), t.getHeight(), false, Camera.Y_DOWN);
	}

	public static void drawWorldTextureRect(Texture t, int x, int y, int width, int height, int rectX, int rectY, int rectWidth, int rectHeight) {
		gameBatch.draw(t, x, y, width, height, rectX, rectY, rectWidth, rectHeight, false, Camera.Y_DOWN);
	}

	public static void drawWorldTextureRectRotated(Texture t, int x, int y, int width, int height, int rectX, int rectY, int rectWidth, int rectHeight, float rotation) {
		gameBatch.draw(t, x - width / 2, y - height / 2, width / 2, height / 2, width, height, 1, 1, rotation, rectX, rectY, rectWidth, rectHeight, false, Camera.Y_DOWN);
	}

	public static void drawText(String text, BitmapFont font, int x, int y, Color color, boolean isOriginTopLeft) {
		drawText(text, font, x, y, color, isOriginTopLeft, false, 0, null);
	}

	public static void drawText(String text, BitmapFont font, int x, int y, Color color, boolean isOriginTopLeft, boolean drawBg, int bgMargin, Color bgColor) {
		glyphLayout.setText(font, text);
		font.setColor(color);
		if(drawBg){
			guiBatch.setColor(bgColor);
			drawGUITexture(Assets.pixel, x - bgMargin, y - bgMargin, (int)glyphLayout.width + 2*bgMargin, (int)glyphLayout.height + 2*bgMargin, isOriginTopLeft);
			guiBatch.setColor(Color.WHITE);
		}
		if(isOriginTopLeft)
			font.draw(guiBatch, text, x, Main.DESKTOP_HEIGHT - y - glyphLayout.height);
		else
			font.draw(guiBatch, text, x, y + glyphLayout.height);
	}

	public static void drawTextCentered(String text, BitmapFont font, int x, int y, Color color, boolean isOriginTopLeft) {
		glyphLayout.setText(font, text);
		font.setColor(color);
		if(isOriginTopLeft)
			font.draw(guiBatch, text, x - glyphLayout.width / 2, Main.DESKTOP_HEIGHT - y - glyphLayout.height);
		else
			font.draw(guiBatch, text, x - glyphLayout.width / 2, y + glyphLayout.height);
	}

	public static void drawTextCenteredY(String text, BitmapFont font, int x, int y, Color color, boolean isOriginTopLeft) {
		glyphLayout.setText(font, text);
		font.setColor(color);
		if(isOriginTopLeft)
			font.draw(guiBatch, text, x, Main.DESKTOP_HEIGHT - y - glyphLayout.height);
		else
			font.draw(guiBatch, text, x, y + glyphLayout.height / 2);
	}

	public static void drawTextCenterOfScreen(String text, BitmapFont font, int y, Color color, boolean isOriginTopLeft) {
		drawTextCenterOfScreen(text, font, y, color, isOriginTopLeft, false, null);
	}

	public static void drawTextCenterOfScreen(String text, BitmapFont font, int y, Color color, boolean isOriginTopLeft, boolean isOriginCentered) {
		drawTextCenterOfScreen(text, font, y, color, isOriginTopLeft, false, null, true);
	}

	public static void drawTextCenterOfScreen(String text, BitmapFont font, int y, Color color, boolean isOriginTopLeft, boolean drawBg, Color bgColor) {
		drawTextCenterOfScreen(text, font, y, color, isOriginTopLeft, drawBg, bgColor, false);
	}

	public static void drawTextCenterOfScreen(String text, BitmapFont font, int y, Color color, boolean isOriginTopLeft, boolean drawBg, Color bgColor, boolean isOriginCenter) {
		glyphLayout.setText(font, text);
		if(isOriginCenter)
			y = (int)(y - glyphLayout.height / 2);
		if(drawBg){
			guiBatch.setColor(bgColor);
			drawGUITexture(Assets.pixel, (int)(Main.DESKTOP_WIDTH / 2 - glyphLayout.width / 2), (int)(y - glyphLayout.height / 2), (int)glyphLayout.width, (int)glyphLayout.height, isOriginTopLeft);
			guiBatch.setColor(Color.WHITE);
		}
		font.setColor(color);
		if(isOriginTopLeft) {
			font.draw(guiBatch, text, Main.DESKTOP_WIDTH / 2 - glyphLayout.width / 2, Main.DESKTOP_HEIGHT - y - glyphLayout.height);
		}
		else {
			font.draw(guiBatch, text, Main.DESKTOP_WIDTH / 2 - glyphLayout.width / 2, y + glyphLayout.height);
		}
	}

	public static void drawWorldTextCentered(String text, BitmapFont font, int x, int y, Color color) {
		glyphLayout.setText(font, text);
		font.setColor(color);
		font.draw(gameBatch, text, x - glyphLayout.width / 2, y);
	}
}
