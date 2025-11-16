package com.rontoking.rontotd.game.systems.networking;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.rontoking.rontotd.game.systems.networking.packets.FilePacket;

public class FileReceiver {
    private static byte[] data;
    private static int position = 0;
    private static String path;

    private static int senderId;

    public static void loadFilePacket(FilePacket filePacket, int sender) {
        data = new byte[filePacket.byteNum];
        path = filePacket.path;
        position = 0;

        if(Networking.state == Networking.State.SERVER)
            senderId = sender;
    }

    public static void recvBytes(byte[] bytes){
        for (int i = 0; i < bytes.length; i++) {
            data[position + i] = bytes[i];
        }
        position += bytes.length;
    }

    public static void writeFile(){
        Gdx.files.local(path).writeBytes(data, false);
        if (Networking.state == Networking.State.SERVER) {
            FileSender.sendFile(path, Networking.QueueType.EXCEPTION, senderId);
        }
    }
}
