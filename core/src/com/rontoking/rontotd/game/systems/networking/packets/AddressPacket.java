package com.rontoking.rontotd.game.systems.networking.packets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryonet.Connection;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.Settings;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.Receiver;

public class AddressPacket {
    public String ip;
    public String path;

    public AddressPacket(){

    }

    public static AddressPacket newAddressPacket(Connection connection){
        AddressPacket addressPacket = new AddressPacket();
        addressPacket.ip = connection.getRemoteAddressTCP().getAddress().getHostAddress();
        addressPacket.path = Gdx.files.getLocalStoragePath();
        return addressPacket;
    }

    public void load() {
        Receiver.IS_SAME_DIR = false;
        if ((ip.equals(Settings.IP) || ip.equals("127.0.0.1") || ip.equals("localhost")) && path.equals(Gdx.files.getLocalStoragePath())) {
            Receiver.IS_SAME_DIR = true;
        }
        if(!Receiver.IS_SAME_DIR)
            deleteAllGameFiles();
    }

    private static void deleteAllGameFiles(){
        Gdx.files.local(Assets.fontPath).emptyDirectory();
        Gdx.files.local(Assets.levelPath).emptyDirectory();
        Gdx.files.local(Assets.enemyPath).emptyDirectory();
        Gdx.files.local(Assets.missilePath).emptyDirectory();
        Gdx.files.local(Assets.missileAnimEffectPath).emptyDirectory();
        Gdx.files.local(Assets.structurePath).emptyDirectory();
        Gdx.files.local(Assets.tilePath).emptyDirectory();
        Gdx.files.local(Assets.towerPath).emptyDirectory();
        Gdx.files.local(Assets.animEffectPath).emptyDirectory();
        Gdx.files.local(Assets.towerIconPath).emptyDirectory();
    }
}
