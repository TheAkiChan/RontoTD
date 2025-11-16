package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.editor.components.LevelEditor;
import com.rontoking.rontotd.game.entities.Tower;
import com.rontoking.rontotd.game.menu.Menu;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.Sender;
import com.rontoking.rontotd.general.Point;
import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.general.Utility;
import javafx.application.Platform;

public class InputHandler implements InputProcessor {
	public static boolean touched = false;
	
	public static Point beginTouch = new Point();
	public static Point endTouch = new Point();
	public static Point mousePos = new Point(Gdx.input.getX(), Gdx.input.getY());

	public static Rectangle startRect, optionsRect;

	public static int CAM_UP_KEY = Input.Keys.W;
	public static int CAM_DOWN_KEY = Input.Keys.S;
	public static int CAM_RIGHT_KEY = Input.Keys.A;
	public static int CAM_LEFT_KEY = Input.Keys.D;

	public static void load(){
		startRect = buttonRect(Assets.start, Main.DESKTOP_WIDTH - 160 - 10, Main.DESKTOP_HEIGHT - Renderer.TOP_GUI_HEIGHT, 160, 2);
		optionsRect = buttonRect(Assets.options, Main.DESKTOP_WIDTH - 160 - 10 - 30 - 10, Main.DESKTOP_HEIGHT - Renderer.TOP_GUI_HEIGHT + Renderer.TOP_GUI_HEIGHT / 2 - 15, 30, 2);
	}

	public static void handle() {
		if(Gdx.input.isCursorCatched()){
			handlePressingOptions();
			handleMouse();
			handleCamera();
			setTouch();
			if(GameState.phase == GameState.Phase.BUILD) {
				handleSelectingTile();
				handlePressingStart();

				Shop.handleInput();
			}
			if(Gdx.app.getType() == Application.ApplicationType.Desktop)
				handleDesktopInput();
		}
		else {
			if(GameState.phase == GameState.Phase.BUILD)
				handleSelectingTile();
			handleCursorCatching();
			handleEditor();
		}
	}

	public static void handleMouse(){
		if(Gdx.input.isCursorCatched()) {
			mousePos.x += Gdx.input.getDeltaX() * ((float)Main.DESKTOP_WIDTH / (float)Gdx.graphics.getWidth());
			mousePos.y += Gdx.input.getDeltaY() * ((float)Main.DESKTOP_HEIGHT / (float)Gdx.graphics.getHeight());
			if (mousePos.x < 0)
				mousePos.x = 0;
			else if (mousePos.x > Main.DESKTOP_WIDTH)
				mousePos.x = Main.DESKTOP_WIDTH;
			if (mousePos.y < 0)
				mousePos.y = 0;
			else if (mousePos.y > Main.DESKTOP_HEIGHT)
				mousePos.y = Main.DESKTOP_HEIGHT;
		}
	}

	private static void handleCamera(){
		if(Gdx.input.isCursorCatched()) {
			if (mousePos.x < Camera.edgeSize)
				Camera.moveX(-Camera.speed);
			else if (mousePos.x > Main.DESKTOP_WIDTH - Camera.edgeSize)
				Camera.moveX(Camera.speed);
			if (mousePos.y < Camera.edgeSize)
				Camera.moveY(-Camera.speed);
			else if (mousePos.y > Main.DESKTOP_HEIGHT - Camera.edgeSize)
				Camera.moveY(Camera.speed);
		}

		if(!Chat.IS_CHAT_OPEN) {
			if (Gdx.input.isKeyPressed(CAM_RIGHT_KEY))
				Camera.moveX(-Camera.speed);
			if (Gdx.input.isKeyPressed(CAM_LEFT_KEY))
				Camera.moveX(Camera.speed);
			if (Gdx.input.isKeyPressed(CAM_UP_KEY))
				Camera.moveY(-Camera.speed);
			if (Gdx.input.isKeyPressed(CAM_DOWN_KEY))
				Camera.moveY(Camera.speed);
		}

		Camera.update();
	}

	public static void handleSelectingTile(){
		if(Shop.SELECTED_TOWER_IN_GAME == -1 && !Shop.isCursorOnGUI()) {
			Point selectedTile = Camera.cursorTile();
			if(selectedTile.y >= 0 && selectedTile.x >= 0 && Level.tiles.length > selectedTile.y && Level.tiles[selectedTile.y].length > selectedTile.x && Tower.canPlaceTower(selectedTile.x, selectedTile.y, true))
				Self.selectedTile = selectedTile;
			else
				Self.selectedTile = null;
		}
		else
			Self.selectedTile = null;
	}

	private static void handlePressingStart() {
		if(isButtonPressed(startRect, false)) {
			ready();
		}
	}

	private static void handlePressingOptions() {
		if(isButtonPressed(optionsRect, false)) {
			GameState.gameOptions();
		}
	}

	public static void ready(){
		if(!Self.isReady) {
			if (Networking.state == Networking.State.SERVER) {
				Self.isReady = true;
				Networking.playerFrontend.get(0).isReady = true;
				Networking.playerReady();
			} else if (Networking.state == Networking.State.CLIENT) {
				Sender.sendReady();
			} else {
				Self.isReady = true;
				GameState.phase = GameState.Phase.DEFEND;
			}
		}
	}
	
	private static Rectangle buttonRect(Texture t, int x, int y, int width, int frameNum) {
		return new Rectangle(x, Main.DESKTOP_HEIGHT - y - t.getHeight() / frameNum * width / t.getWidth(), width, t.getHeight() / frameNum * width / t.getWidth());
	}
	
	private static void setTouch() {
		if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && endTouch != null){
			beginTouch = null;
			endTouch = null;
		}
		if(Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !touched){
			beginTouch = new Point(mousePos);
			endTouch = null;
			touched = true;
		}else if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && touched){
			endTouch = new Point(mousePos);
			touched = false;
		}
	}

	public static boolean isButtonPressed(Rectangle rect, boolean mouseFlipped){
		if(endTouch != null){
			if(mouseFlipped)
				return rect.contains(beginTouch.x, Main.DESKTOP_HEIGHT - beginTouch.y) && rect.contains(endTouch.x, Main.DESKTOP_HEIGHT - endTouch.y);
			else
				return rect.contains(beginTouch.x, beginTouch.y) && rect.contains(endTouch.x, endTouch.y);
		}
		return false;
	}

	public static boolean isButtonHovered(Rectangle rect, boolean mouseFlipped){
		if(beginTouch == null)
			return false;
		if(mouseFlipped)
			return rect.contains(beginTouch.x, Main.DESKTOP_HEIGHT - beginTouch.y) && rect.contains(mousePos.x, Main.DESKTOP_HEIGHT - mousePos.y);
		else
			return rect.contains(beginTouch.x, beginTouch.y) && rect.contains(mousePos.x, mousePos.y);
	}

	public static void handleEditor(){
		//if(!Chat.IS_CHAT_OPEN && Main.IS_EDITOR_ENABLED && Gdx.input.isKeyJustPressed(Input.Keys.E)){
		//	Editor.open();
		//}
	}

	public static void getReady(){
		if(GameState.phase == GameState.Phase.BUILD)
			ready();
		else if(Networking.state != Networking.State.CLIENT)
			GameState.setToTransition(Level.num, 0, true);
	}

	public static void handleCursorCatching(){
		if(!Chat.IS_CHAT_OPEN && (Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT))){
			Gdx.input.setCursorCatched(!Gdx.input.isCursorCatched());
		}
	}

	private static void handleDesktopInput(){
		handleEditor();
		handleCursorCatching();
		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			if(Chat.IS_CHAT_OPEN){
				Chat.IS_CHAT_OPEN = false;
			}else {
				GameState.gameOptions();
			}
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT) && !Chat.IS_CHAT_OPEN){
			Camera.setToDefault(Level.cameraZoom);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !Chat.IS_CHAT_OPEN){
			//getReady();
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		if(Main.state == Main.State.MENU)
			Menu.handleTypingInTextInput(character);
		else if(Main.state == Main.State.GAME)
			Chat.type(character);
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(Main.state == Main.State.GAME && Gdx.input.isCursorCatched()) {
			GameState.movedMouseAfterMenu = true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(Main.state == Main.State.GAME && Gdx.input.isCursorCatched()) {
			GameState.movedMouseAfterMenu = true;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		if(Main.state == Main.State.GAME && Gdx.input.isCursorCatched()) {
			GameState.movedMouseAfterMenu = true;
		}
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		if(Main.state == Main.State.GAME && Gdx.input.isCursorCatched()) {
			Camera.zoom(amount);
		}
		return true;
	}
}
