package com.rontoking.rontotd.game.systems.networking.packets;

import com.rontoking.rontotd.game.systems.networking.Networking;

public class FilePacket {
    public int byteNum;
    public String path;

    public FilePacket(){

    }

    public static FilePacket newFilePacket(int byteNum, String path){
        FilePacket filePacket = new FilePacket();
        filePacket.byteNum = byteNum;
        filePacket.path = path;
        return filePacket;
    }
}
