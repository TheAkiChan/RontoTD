package com.rontoking.rontotd.game.systems.networking.packets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.networking.FileSender;
import com.rontoking.rontotd.game.systems.networking.Networking;

public class SyncPacket {
    public String path1, path2;
    public Type type;

    public enum Type{
        DELETE, SWAP
    }

    public SyncPacket(){

    }

    public static SyncPacket newDeletePacket(String path){
        SyncPacket syncPacket = new SyncPacket();
        syncPacket.type = Type.DELETE;
        syncPacket.path1 = path;
        return syncPacket;
    }

    public static SyncPacket newSwapPacket(String path1, String path2){
        SyncPacket syncPacket = new SyncPacket();
        syncPacket.type = Type.SWAP;
        syncPacket.path1 = path1;
        syncPacket.path2 = path2;
        return syncPacket;
    }

    public static void sendDeleteSync(String path){
        if(Networking.state == Networking.State.SERVER)
            FileSender.queue(newDeletePacket(path), Networking.QueueType.EXCEPTION, -1);
        else if(Networking.state == Networking.State.CLIENT)
            FileSender.queue(newDeletePacket(path), Networking.QueueType.EXCEPTION, Networking.client.getID());
    }

    public static void sendSwapSync(String path1, String path2){
        if(Networking.state == Networking.State.SERVER)
            FileSender.queue(newSwapPacket(path1, path2), Networking.QueueType.EXCEPTION, -1);
        else if(Networking.state == Networking.State.CLIENT)
            FileSender.queue(newSwapPacket(path1, path2), Networking.QueueType.EXCEPTION, Networking.client.getID());
    }

    public void load(int sender){
        if(Networking.state == Networking.State.SERVER)
            FileSender.queue(this, Networking.QueueType.EXCEPTION, sender);
        if(type == Type.DELETE) {
            final FileHandle path = Gdx.files.local(path1);
            path.delete();
            if(path.parent().equals(Assets.levelPath)){ // Deleting a level.
                int levelNum = Integer.parseInt(path.nameWithoutExtension());
                for(int i = levelNum + 1; i <= Assets.finalLevel; i++){
                    Gdx.files.local(Assets.levelPath + "/" + i).moveTo(Gdx.files.local(Assets.levelPath + "/" + (i - 1)));
                }
            }
        }else if(type == Type.SWAP){
            Editor.swapFiles(path1, path2);
        }
    }
}
