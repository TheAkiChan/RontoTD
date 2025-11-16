package com.rontoking.rontotd.game.systems.networking.packets;

import com.rontoking.rontotd.game.entities.Tower;
import com.rontoking.rontotd.game.systems.Level;
import com.rontoking.rontotd.game.systems.Self;
import com.rontoking.rontotd.game.systems.Shop;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.Sender;

public class TowerPacket {
    public int type; // -1 means it's sold.
    public int ownerID;
    public int x, y;

    public TowerPacket(){

    }

    public static TowerPacket newTowerPacket(int type, int ownerID, int x, int y){
        TowerPacket towerPacket = new TowerPacket();
        towerPacket.type = type;
        towerPacket.ownerID = ownerID;
        towerPacket.x = x;
        towerPacket.y = y;
        return towerPacket;
    }

    public void loadForServer(int senderID){
        if(type == -1 && Tower.towers.length > type && Tower.exists(x, y) && Tower.get(x, y).ownerID == senderID){ // Removing tower.
            Networking.playerFrontend.get(senderID).gold += Tower.get(x, y).cost;
            Tower.remove(x, y, true);
            Sender.sendTower(type, senderID, x, y);
        }else if(type != -1 && Tower.towers.length > type && Networking.playerFrontend.get(senderID).gold >= Tower.towers[type].cost) {
            if (Tower.canPlaceTower(x, y, false)) { // Placing tower.
                Networking.playerFrontend.get(senderID).gold -= Tower.towers[type].cost;
                Tower.place(type, x, y, senderID, true);
                Sender.sendTower(type, ownerID, x, y);
            }else if(Tower.exists(x, y)){ // Upgrading tower.
                for(int i = 0; i < Level.towers.get(Tower.indexOf(x, y)).upgrades.length; i++){
                    if(Level.towers.get(Tower.indexOf(x, y)).upgrades[i] == type){
                        Networking.playerFrontend.get(senderID).gold -= Tower.towers[type].cost;
                        Tower.upgrade(type, Tower.indexOf(x, y), true);
                        Sender.sendTower(type, ownerID, x, y);
                        return;
                    }
                }
            }
        }
    }

    public void loadForClient(){
        if(type == -1){ // Removing tower.
            if(ownerID == Networking.client.getID())
                Self.gold += Tower.get(x, y).cost;
            Tower.remove(x, y, true);
        }else{
            if(ownerID == Networking.client.getID())
                Self.gold -= Tower.towers[type].cost;
            if(Tower.exists(x, y)) { // Upgrading tower.
                Tower.upgrade(type, Tower.indexOf(x, y), true);
                if(ownerID == Networking.client.getID())
                    Shop.getUpgradeTowers(Shop.SELECTED_TOWER_IN_GAME, true);
            }
            else // Placing tower.
                Tower.place(type, x, y, ownerID, true);
        }
    }
}
