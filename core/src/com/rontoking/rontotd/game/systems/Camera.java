package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.game.entities.Spawner;
import com.rontoking.rontotd.game.entities.Structure;
import com.rontoking.rontotd.game.entities.Tower;
import com.rontoking.rontotd.general.Point;
import com.rontoking.rontotd.general.Utility;

public class Camera {
	private static OrthographicCamera camera;
	private static Viewport viewport;

	public static int speed = 8;
	public static int edgeSize = 10;

	public static final boolean Y_DOWN = true;
	public static final float MIN_ZOOM = 0.1f;
	public static final float MAX_ZOOM = 3f;

	public static void load(){
		camera = new OrthographicCamera(Main.DESKTOP_WIDTH, Main.DESKTOP_HEIGHT);
		camera.setToOrtho(Y_DOWN);
		viewport = new StretchViewport(Main.DESKTOP_WIDTH, Main.DESKTOP_HEIGHT, camera);
		//setToDefault(Level.cameraZoom);
	}

	public static void setToDefault(float zoom){
		camera.position.set(Level.tiles[0].length / 2 * GameState.tileSize + GameState.tileSize / 2, Level.tiles.length / 2 * GameState.tileSize + GameState.tileSize / 2, 0);
		setZoom(zoom);
	}

	public static void moveX(float delta){
		camera.position.x += delta;
	}

	public static void moveY(float delta){
		camera.position.y += delta;
	}

	public static void zoom(int amount){
		setZoom(camera.zoom + 0.1f * amount);
	}

	public static void setZoom(float zoom){
		camera.zoom = zoom;
		if (camera.zoom < MIN_ZOOM)
			camera.zoom = MIN_ZOOM;
		if (camera.zoom > MAX_ZOOM)
			camera.zoom = MAX_ZOOM;
		camera.update();
	}

	public static void resize(int width, int height){
		viewport.update(width, height, true);
	}

	public static void update(){
		camera.update();
	}

	public static Vector3 cursorGamePos(){
		return camera.unproject(new Vector3(InputHandler.mousePos.x / ((float)Main.DESKTOP_WIDTH / (float)Gdx.graphics.getWidth()), InputHandler.mousePos.y / ((float)Main.DESKTOP_HEIGHT / (float)Gdx.graphics.getHeight()), 0));
	}

	public static Point cursorTile(){
		Vector3 gamePos = cursorGamePos();
		Vector3 snappedGamePos = new Vector3(Utility.floor((int)gamePos.x, GameState.tileSize), Utility.floor((int)gamePos.y, GameState.tileSize), 0);
		Point indexedPos = new Point((int)snappedGamePos.x / GameState.tileSize, (int)snappedGamePos.y / GameState.tileSize);
		return indexedPos;
	}

	public static Matrix4 getMatrix(){
		return camera.combined;
	}
}