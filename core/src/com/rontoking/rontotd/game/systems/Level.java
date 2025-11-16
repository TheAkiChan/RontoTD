package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.game.entities.*;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.general.Point;

public class Level {
	public static int[][] tiles;
	public static Structure[] structures;
	public static Array<Structure> auraStructures = new Array<Structure>();
	public static Spawner[] spawners;
	public static SpawnerRenderable[] spawnerRenderable;
	public static Array<Tower> towers = new Array<Tower>();
	public static Array<Tower> auraTowers = new Array<Tower>();
	public static Array<TowerRenderable> towerRenderables = new Array<TowerRenderable>();
	public static Array<Enemy> enemies = new Array<Enemy>(true, 0, Enemy.class);
	public static Array<Enemy> auraEnemies = new Array<Enemy>();
	public static Array<EnemyRenderable> enemyRenderables = new Array<EnemyRenderable>(true, 0, EnemyRenderable.class);
	public static Array<Sorted> sorted = new Array<Sorted>(true, 0, Sorted.class);
	public static Array<Missile> missiles = new Array<Missile>(true, 0, Missile.class);
	public static Array<MissileRenderable> missileRenderables = new Array<MissileRenderable>(true, 0, MissileRenderable.class);
	public static Array<AnimEffect> animEffects = new Array<AnimEffect>(true, 0, AnimEffect.class);
	public static int num = 0;
	public static int idCounter = 0;
	private static Array<Point> enemyGoals = new Array<Point>();

	public static int lives, gold;
	public static boolean sharedGold;
	public static float cameraZoom;

	public static void load(boolean shouldClearTowers) {
		clearLevel(shouldClearTowers);
		Assets.loadLevel();
		String[] fileSegments = Assets.level.split("\\.");
		loadTiles(fileSegments);
		loadStructures(fileSegments);
		loadSpawners(fileSegments);

		lives = Integer.parseInt(fileSegments[3]);
		gold = Integer.parseInt(fileSegments[4]);
		sharedGold = Boolean.parseBoolean(fileSegments[5]);
		cameraZoom = Float.parseFloat(fileSegments[6]);

		setLives();
		if(shouldClearTowers) {
			setGold();
		}

		Camera.setToDefault(cameraZoom);
	}

	public static void clearLevel(boolean shouldClearTowers){
		if(Assets.menuMusic.isPlaying())
			Assets.menuMusic.stop();
		if(Assets.defendMusic.isPlaying())
			Assets.defendMusic.stop();
		Assets.buildMusic.setVolume(Settings.MUSIC_VOLUME);
		Assets.buildMusic.setLooping(true);
		Assets.buildMusic.setPosition(0);
		Assets.buildMusic.play();
		Chat.reset();
		if(shouldClearTowers) {
			towers.clear();
			auraTowers.clear();
			towerRenderables.clear();
			idCounter = 0;
		}
		if(Networking.state != Networking.State.CLIENT) {
			enemies.clear();
			auraEnemies.clear();
			enemyRenderables.clear();
			for (int i = sorted.size - 1; i >= 0; i--) {
				if (sorted.get(i).type != Sorted.Type.TOWER || shouldClearTowers)
					sorted.removeIndex(i);
			}
			missiles.clear();
			missileRenderables.clear();
			animEffects.clear();
		}

		Shop.SELECTED_TOWER_IN_GAME = -1;
		Shop.SELECTED_TOWER_IN_SHOP = 0;

		Self.selectedTile = null;
		Self.isReady = false;
	}

	private static void setLives(){
		Self.lives = lives;
		if(Networking.state == Networking.State.SERVER){
			for(int i = 0; i < Networking.players.size; i++){
				Networking.playerFrontend.get(i).lives = lives;
			}
		}
	}

	private static void setGold(){
		if(Networking.state == Networking.State.SERVER){
			for(int i = 0; i < Networking.playerFrontend.size; i++){
				Networking.playerFrontend.get(i).gold = startGold(Networking.players.size);
			}
			Self.gold = startGold(Networking.players.size);
		}else{
			Self.gold = startGold(1);
		}
	}

	public static int startGold(int numOfPlayers){
		if(sharedGold)
			return gold / numOfPlayers;
		return gold;
	}

	private static void loadTiles(String[] fileSegments) {
		int fileHeight = fileSegments[0].split(";").length;
		String[] fileTiles = fileSegments[0].replace(";", ",").split(",");
		
		tiles = new int[fileTiles.length / fileHeight][fileHeight];
		for(int y = 0; y < fileHeight; y++) {
			for(int x = 0; x < fileTiles.length / fileHeight; x++) {
				tiles[y][x] = Integer.parseInt(fileTiles[y * fileHeight + x]);
			}
		}
	}
	
	private static void loadStructures(String[] fileSegments) {
		enemyGoals.clear();
		String[] fileStructures = fileSegments[1].split(",");
		
		structures = new Structure[fileStructures.length / 3];
		auraStructures.clear();
		for(int i = 0; i < structures.length; i++) {
			structures[i] = new Structure(Integer.parseInt(fileStructures[i * 3]), Integer.parseInt(fileStructures[i * 3 + 1]), Integer.parseInt(fileStructures[i * 3 + 2]));
			if(structures[i].isGoal){
				enemyGoals.add(new Point(structures[i].x, structures[i].y) );
			}
			if(Structure.hasAura(i)){
				auraStructures.add(structures[i]);
			}
		}
	}

	private static void loadSpawners(String[] fileSegments){
		String[] fileSpawners = fileSegments[2].split(";");
		spawners = new Spawner[fileSpawners.length];
		spawnerRenderable = new SpawnerRenderable[fileSpawners.length];
		for(int i = 0; i < spawners.length; i++){
			String[] fileWaves = fileSpawners[i].split("w");
			spawners[i] = new Spawner(Integer.parseInt(fileWaves[0].split(",")[0]), Integer.parseInt(fileWaves[0].split(",")[1]), new Array<Wave>());
			spawnerRenderable[i] = spawners[i].spawnerRenderable;
			for(int w = 1; w < fileWaves.length; w++){
				String[] fileSpawns = fileWaves[w].split(",");
				spawners[i].waves.add(new Wave(new Array<Spawn>()));
				for(int s = 0; s < fileSpawns.length / 3; s++){
					spawners[i].waves.get(w - 1).spawns.add(new Spawn(Integer.parseInt(fileSpawns[s * 3]), Integer.parseInt(fileSpawns[s * 3 + 1]), Integer.parseInt(fileSpawns[s * 3 + 2])));
				}
			}
		}
	}

	public static boolean isGoalPos(int x, int y){
		for(Point p : enemyGoals){
			if(p.isEqualTo(x, y))
				return true;
		}
		return false;
	}

	public static Point getClosestGoal(int x, int y){
		Point result = new Point();
		float distance = -1;
		for(Point p : enemyGoals){
			if((int)distance == -1 || Vector2.dst(x, y, p.x, p.y) < distance){
				distance = Vector2.dst(x, y, p.x, p.y);
				result.set(p);
			}
		}
		return result;
	}
}
