package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Level;
import com.rontoking.rontotd.game.systems.Renderer;

public class Structure {
	public static Structure[] structures;

	public static void load() {
		String[] structureArray = Assets.getFile(Assets.structurePath + "/info").readString().replaceAll("\n","").split(";");
		String[] structure;
		structures = new Structure[structureArray.length];
		for (int i = 0; i < structures.length; i++) {
			structure = structureArray[i].split(",");
			structures[i] = new Structure(structure[0].trim(), Boolean.parseBoolean(structure[1].trim()), Integer.parseInt(structure[2].trim()), Integer.parseInt(structure[3].trim()), Ability.parseStringToArray(structure[4]), Boolean.parseBoolean(structure[5].trim()), Boolean.parseBoolean(structure[6].trim()));
		}
	}

	public static void animate() {
		for (int i = 0; i < structures.length; i++) {
			structures[i].frameTimeLeft--;
			if (structures[i].frameTimeLeft <= 0) {
				structures[i].frameTimeLeft = structures[i].frameTime;
				structures[i].currentFrame++;
				if (structures[i].currentFrame >= structures[i].frameNum)
					structures[i].currentFrame = 0;
			}
		}
	}

	public String name;
	public boolean isGoal, isBuildableOn, isPathable;
	public int frameNum;
	public int frameTime;

	public int currentFrame;
	private int frameTimeLeft;

	public Ability[] abilities;

	private int index;
	public int x, y;

	public Structure(String name, boolean isGoal, int frameNum, int frameTime, Ability[] abilities, boolean isBuildableOn, boolean isPathable) {
		this.name = name;
		this.isGoal = isGoal;
		this.frameNum = frameNum;
		this.frameTime = frameTime;
		this.isBuildableOn = isBuildableOn;
		this.isPathable = isPathable;

		this.abilities = abilities;

		this.currentFrame = 0;
		this.frameTimeLeft = this.frameTime;
	}

	public Structure(int index, int x, int y){
		this.index = index;
		this.x = x;
		this.y = y;

		this.name = structures[index].name;
		this.isGoal = structures[index].isGoal;
		this.frameNum = structures[index].frameNum;
		this.frameTime = structures[index].frameTime;
		this.abilities = structures[index].abilities;
		this.isBuildableOn = structures[index].isBuildableOn;
		this.isPathable = structures[index].isPathable;
	}

	public void render(){
		Renderer.drawWorldTextureRect(texture(index), x* GameState.tileSize, y*GameState.tileSize, GameState.tileSize, GameState.tileSize, 0, texture(index).getHeight() / frameNum * structures[index].currentFrame, texture(index).getWidth(), texture(index).getHeight() / frameNum );
	}

	public static boolean hasAura(int index){
		for(Ability ability : Level.structures[index].abilities){
			if(ability.type == Ability.Type.Enemy_Aura || ability.type == Ability.Type.Tower_Aura)
				return true;
		}
		return false;
	}

	public static Texture texture(int index){
		return Assets.structures[index];
	}

	public static boolean blocksEnemy(int x, int y){
		for(int i = 0; i < Level.structures.length; i++){
			if(Level.structures[i].x == x && Level.structures[i].y == y && !Level.structures[i].isPathable)
				return true;
		}
		return false;
	}

	public static boolean blocksTower(int x, int y){
		for(int i = 0; i < Level.structures.length; i++){
			if(Level.structures[i].x == x && Level.structures[i].y == y && !Level.structures[i].isBuildableOn)
				return true;
		}
		return false;
	}

	public static boolean exists(int x, int y){
		for(int i = 0; i < Level.structures.length; i++){
			if(Level.structures[i] != null && Level.structures[i].x == x && Level.structures[i].y == y)
				return true;
		}
		return false;
	}

	public static int indexOf(String name){
		for(int i = 0; i < structures.length; i++){
			if(structures[i].name.equals(name))
				return i;
		}
		return -1;
	}
}
