package com.rontoking.rontotd.general;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.rontoking.rontotd.game.entities.Enemy;
import com.rontoking.rontotd.game.entities.Structure;
import com.rontoking.rontotd.game.entities.Tower;
import com.rontoking.rontotd.game.systems.GameState;

public class Utility {
	public static int floor(int numToRound, int rounder) {
		return MathUtils.floor((float)numToRound / (float)rounder) * rounder;
	}
	
	public static void print(String s) {
		System.out.println(s);
	}

	public static int getSign(float num){
		return (int)(num / Math.abs(num));
	}

	public static float dstBetweenEnemyAndStructure(Enemy enemy, Structure structure){
		return  Vector2.dst(enemy.collisionPos.x, enemy.collisionPos.y, structure.x * GameState.tileSize + GameState.tileSize / 2, structure.y * GameState.tileSize + GameState.tileSize / 2) - enemy.collisionRadius() - GameState.tileSize;
	}

	public static float dstBetweenEnemyAndEnemy(Enemy enemy1, Enemy enemy2){
		return  Vector2.dst(enemy1.collisionPos.x, enemy1.collisionPos.y, enemy2.collisionPos.x, enemy2.collisionPos.y) - enemy1.collisionRadius() - enemy2.collisionRadius();
	}

	public static float dstBetweenTowerAndEnemy(Tower tower, Enemy enemy){
		return  Vector2.dst(tower.renderable.x * GameState.tileSize + GameState.tileSize / 2, tower.renderable.y * GameState.tileSize + GameState.tileSize / 2, enemy.collisionPos.x, enemy.collisionPos.y) - GameState.tileSize / 2 - enemy.collisionRadius();
	}

	public static float dstBetweenTowerAndTower(Tower tower1, Tower tower2){
		return  Vector2.dst(tower1.renderable.x * GameState.tileSize + GameState.tileSize / 2, tower1.renderable.y * GameState.tileSize + GameState.tileSize / 2, tower2.renderable.x * GameState.tileSize + GameState.tileSize / 2, tower2.renderable.y * GameState.tileSize + GameState.tileSize / 2) - GameState.tileSize;
	}

	public static float dstBetweenTowerAndStructure(Tower tower, Structure structure){
		return  Vector2.dst(tower.renderable.x * GameState.tileSize + GameState.tileSize / 2, tower.renderable.y * GameState.tileSize + GameState.tileSize / 2, structure.x * GameState.tileSize + GameState.tileSize / 2, structure.y * GameState.tileSize + GameState.tileSize / 2) - GameState.tileSize * 3 / 2;
	}
}
