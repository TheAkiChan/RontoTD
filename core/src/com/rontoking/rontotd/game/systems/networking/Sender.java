package com.rontoking.rontotd.game.systems.networking;

import com.rontoking.rontotd.game.systems.Self;
import com.rontoking.rontotd.game.systems.networking.packets.CursorPacket;
import com.rontoking.rontotd.game.systems.networking.packets.LevelPacket;
import com.rontoking.rontotd.game.systems.networking.packets.TowerPacket;
import com.rontoking.rontotd.game.systems.networking.packets.UpdatePacket;

public class Sender {

    public static void sendLevel(int levelNum, boolean clearTowers){
        Networking.server.sendToAllTCP(LevelPacket.newLevelPacket(levelNum, clearTowers));
    }

    public static void sendCursor(){
        Networking.client.sendUDP(CursorPacket.newCursorPacket());
    }

    public static void sendServerUpdate(){
        Networking.server.sendToAllUDP(UpdatePacket.newUpdatePacket());
    }

    public static void sendClientUpdate(){
        sendCursor();
    }

    public static void sendTower(int towerIndex, int ownerID, int x, int y){
        sendTCP(TowerPacket.newTowerPacket(towerIndex, ownerID, x, y));
    }

    public static void sendReady(){
        sendTCP(Networking.READY_MSG);
    }

    public static void sendTCP(Object object){
        if(Networking.state == Networking.State.SERVER)
            Networking.server.sendToAllTCP(object);
        else
            Networking.client.sendTCP(object);
    }
}
