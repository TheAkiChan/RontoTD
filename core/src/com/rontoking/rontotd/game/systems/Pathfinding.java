package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.utils.BinaryHeap;
import com.badlogic.gdx.utils.IntArray;
import com.rontoking.rontotd.game.entities.*;
import com.rontoking.rontotd.general.Point;

public  class Pathfinding {
    private final int width, height;
    private final BinaryHeap<PathNode> open;
    private final PathNode[] nodes;
    int runID;
    private final IntArray path = new IntArray();
    private int targetX, targetY;

    public static Pathfinding pathfinding;
    public static IntArray intArray;
    public static Point[] ret;

    public Pathfinding(int width, int height) {
        this.width = width;
        this.height = height;
        open = new BinaryHeap(width * 4, false);
        nodes = new PathNode[width * height];
    }

    /**
     * Returns x,y pairs that are the path from the target to the start.
     */
    public static Point[] path(Point pos, Point goal, final EnemyRenderable.TransportType transportType, final boolean isEnraged){
        pathfinding = new Pathfinding(Level.tiles[0].length, Level.tiles.length){
            protected boolean isValid (int x, int y, int prevX, int prevY){
                return Pathfinding.canPath(x, y, transportType, isEnraged, prevX, prevY);
            }
        };
        intArray = pathfinding.getPath(pos.x, pos.y, goal.x, goal.y);
        ret = new Point[intArray.size / 2];
        for (int i = 0; i < intArray.size; i += 2)
            ret[ret.length - 1 - i / 2] = new Point( intArray.get(i), intArray.get(i + 1));

        return ret;
    }

    private static boolean canPath(int x, int y, EnemyRenderable.TransportType transportType, final boolean isEnraged, int prevX, int prevY){
        return isWalkable(x, y, transportType, isEnraged) && !isCuttingCorners(x, y, transportType, isEnraged, prevX, prevY);
    }

    private static boolean isWalkable(int x, int y, EnemyRenderable.TransportType transportType, final boolean isEnraged){
        switch (transportType){
            case Walking:
                return (Tile.tiles[Level.tiles[y][x]].canHaveEnemy && !Tower.exists(x, y) || isEnraged) && !Structure.blocksEnemy(x, y) && !Tile.tiles[Level.tiles[y][x]].isWater;
            case Swimming:
                return (Tile.tiles[Level.tiles[y][x]].canHaveEnemy && !Tower.exists(x, y) || isEnraged) && !Structure.blocksEnemy(x, y) && Tile.tiles[Level.tiles[y][x]].isWater;
            case Flying:
                return true;
            default:
                return false;
        }
    }

    private static boolean isCuttingCorners(int x, int y, EnemyRenderable.TransportType transportType, final boolean isEnraged, int prevX, int prevY){
        if(transportType == EnemyRenderable.TransportType.Flying)
            return false;
        if(x != prevX && y != prevY){ // Diagonal movement.
            return !isWalkable(prevX, y, transportType, isEnraged) || !isWalkable(x, prevY, transportType, isEnraged);
        }
        return false;
    }

    public IntArray getPath(int startX, int startY, int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;

        path.clear();
        open.clear();

        runID++;
        if (runID < 0) runID = 1;

        int index = startY * width + startX;
        PathNode root = nodes[index];
        if (root == null) {
            root = new PathNode(0);
            root.x = startX;
            root.y = startY;
            nodes[index] = root;
        }
        root.parent = null;
        root.pathCost = 0;
        open.add(root, 0);

        int lastColumn = width - 1, lastRow = height - 1;
        while (open.size > 0) {
            PathNode node = open.pop();
            if (node.x == targetX && node.y == targetY) {
                while (node != root) {
                    path.add(node.x);
                    path.add(node.y);
                    node = node.parent;
                }
                break;
            }
            node.closedID = runID;
            int x = node.x;
            int y = node.y;
            if (x < lastColumn) {
                addNode(node, x + 1, y, 10);
                if (y < lastRow) addNode(node, x + 1, y + 1, 14); // Diagonals cost more, roughly equivalent to sqrt(2).
                if (y > 0) addNode(node, x + 1, y - 1, 14);
            }
            if (x > 0) {
                addNode(node, x - 1, y, 10);
                if (y < lastRow) addNode(node, x - 1, y + 1, 14);
                if (y > 0) addNode(node, x - 1, y - 1, 14);
            }
            if (y < lastRow) addNode(node, x, y + 1, 10);
            if (y > 0) addNode(node, x, y - 1, 10);
        }
        return path;
    }

    private void addNode(PathNode parent, int x, int y, int cost) {
        if(parent == null) {
            if (!isValid(x, y, targetX, targetY)) return;
        }
        else {
            if (!isValid(x, y, parent.x, parent.y)) return;
        }

        int pathCost = parent.pathCost + cost;
        float score = pathCost + Math.abs(x - targetX) + Math.abs(y - targetY);

        int index = y * width + x;
        PathNode node = nodes[index];
        if (node != null && node.runID == runID) { // Node already encountered for this run.
            if (node.closedID != runID && pathCost < node.pathCost) { // Node isn't closed and new cost is lower.
                // Update the existing node.
                open.setValue(node, score);
                node.parent = parent;
                node.pathCost = pathCost;
            }
        } else {
            // Use node from the cache or create a new one.
            if (node == null) {
                node = new PathNode(0);
                node.x = x;
                node.y = y;
                nodes[index] = node;
            }
            open.add(node, score);
            node.runID = runID;
            node.parent = parent;
            node.pathCost = pathCost;
        }
    }

    protected boolean isValid(int x, int y, int prevX, int prevY) {
        return true;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    static private class PathNode extends BinaryHeap.Node {
        int runID, closedID, x, y, pathCost;
        PathNode parent;

        public PathNode(float value) {
            super(value);
        }
    }
}
