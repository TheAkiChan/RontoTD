package com.rontoking.rontotd.game.systems.networking.packets;

import com.badlogic.gdx.math.Vector3;
import com.rontoking.rontotd.game.systems.Camera;

public class CursorPacket {
    public int x, y;

    public CursorPacket(){

    }

    public static CursorPacket newCursorPacket(){
        CursorPacket cursorPacket = new CursorPacket();
        Vector3 gamePos = Camera.cursorGamePos();
        cursorPacket.x = (int)gamePos.x;
        cursorPacket.y = (int)gamePos.y;
        return cursorPacket;
    }
}
