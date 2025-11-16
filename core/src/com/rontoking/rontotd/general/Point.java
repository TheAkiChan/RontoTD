package com.rontoking.rontotd.general;

import com.badlogic.gdx.math.Vector3;

public class Point {
	public int x, y;
	
	public Point() {
		this.x = 0;
		this.y = 0;
	}

	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}

	public Point(Vector3 v) {
		this.x = (int)v.x;
		this.y = (int)v.y;
	}

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(Point p) {
		this.x = p.x;
		this.y = p.y;
	}

	public boolean isEqualTo(int x, int y){
		return this.x == x && this.y == y;
	}

	public boolean isEqualTo(Point p){
		return this.x == p.x && this.y == p.y;
	}

	public String string(){
		return this.x + ", " + this.y;
	}

	public void add(int x, int y){
		this.x += x;
		this.y += y;
	}

	public void add(Point p){
		this.x += p.x;
		this.y += p.y;
	}

	public Vector3 getVector3(){
		return new Vector3(x, y, 0);
	}
}
