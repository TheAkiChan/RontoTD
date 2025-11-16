package com.rontoking.rontotd.game.systems.networking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.util.TcpIdleSender;
import com.rontoking.rontotd.game.systems.networking.packets.AddressPacket;
import com.rontoking.rontotd.game.systems.networking.packets.FilePacket;

public class FileSender {
    private static int MAX_DATA_SIZE = 10000;
    private static Array<Object> serverQueue = new Array<Object>();
    private static Object serverObject;
    private static byte[] data;
    private static byte[] part;
    private static int roundNum;
    private static int position;

    private static boolean isServerListenerAlive = false;
    public static Connection serverConnection;

    private static final boolean USING_LISTENERS = false;

    public static void reset(){
        serverQueue.clear();
    }

    public static void sendFile(String path, Networking.QueueType queueType, int id){
        data = Gdx.files.local(path).readBytes();
        queue(FilePacket.newFilePacket(data.length, path), queueType, id);

        roundNum = (int)Math.ceil((float)data.length / (float)MAX_DATA_SIZE);
        position = 0;
        for(int i = 0; i < roundNum; i++){
            queueBytes(queueType, id);
        }
        queue(Networking.FILE_DONE_MSG, queueType, id);
    }

    public static void sendSyncFile(String path){
        if(Networking.state == Networking.State.SERVER)
            sendFile(path, Networking.QueueType.EXCEPTION, -1);
        else if(Networking.state == Networking.State.CLIENT)
            sendFile(path, Networking.QueueType.EXCEPTION, Networking.client.getID());
    }

    public static void sendAllFiles(Connection connection, Networking.QueueType queueType, int id){
        queue(AddressPacket.newAddressPacket(connection), queueType, id);
        for(FileHandle fileHandle : new FileHandle(Gdx.files.getLocalStoragePath()).list()){
            sendAllFilesInDir(fileHandle.name(), queueType, id);
        }
        queue(Networking.ALL_FILES_SENT, queueType, id);
    }

    public static void sendAllFilesInDir(String path, Networking.QueueType queueType, int id){
        for(FileHandle fileHandle : Gdx.files.local(path).list()){
            if(fileHandle.isDirectory()){
                sendAllFilesInDir(fileHandle.path(), queueType, id);
            }else{
                sendFile(fileHandle.path(), queueType, id);
            }
        }
    }

    public static void addListenerToConnection(final Connection connection){
        if(Networking.state == Networking.State.SERVER)
            Networking.players.get(connection.getID()).isListenerAlive = true;
        else if(Networking.state == Networking.State.CLIENT)
            isServerListenerAlive = true;
        connection.addListener(new TcpIdleSender() {
            @Override
            protected Object next() {
                if(Networking.state == Networking.State.SERVER){
                    if(Networking.players.get(connection.getID()).queue.size > 0) {
                        Networking.players.get(connection.getID()).queueObject = Networking.players.get(connection.getID()).queue.get(0);
                        Networking.players.get(connection.getID()).queue.removeIndex(0);
                        return Networking.players.get(connection.getID()).queueObject;
                    }
                    Networking.players.get(connection.getID()).isListenerAlive = false;
                }
                else if(Networking.state == Networking.State.CLIENT) {
                    if(serverQueue.size > 0) {
                        serverObject = serverQueue.get(0);
                        serverQueue.removeIndex(0);
                        return serverObject;
                    }
                    isServerListenerAlive = false;
                }
                return null;
            }
        });
    }

    private static void queueBytes(Networking.QueueType queueType, int id){
        if(data.length - position < MAX_DATA_SIZE)
            part = new byte[data.length - position];
        else
            part = new byte[MAX_DATA_SIZE];
        for(int i = 0; i < part.length; i++){
            part[i] = data[position + i];
        }
        queue(part, queueType, id);
        position += MAX_DATA_SIZE;
    }

    public static void sendQueued(){
        if(!USING_LISTENERS) {
            if (Networking.state == Networking.State.CLIENT) {
                if (serverQueue.size > 0) {
                    Networking.client.sendTCP(serverQueue.get(0));
                    serverQueue.removeIndex(0);
                }
            } else if (Networking.state == Networking.State.SERVER) {
                for (int i = 1; i < Networking.players.size; i++) {
                    if (Networking.players.get(i).queue.size > 0) {
                        Networking.server.sendToTCP(i, Networking.players.get(i).queue.get(0));
                        Networking.players.get(i).queue.removeIndex(0);
                    }
                }
            }
        }
    }

    public static void queueException(Object object){
        if(Networking.state == Networking.State.SERVER)
            queue(object, Networking.QueueType.EXCEPTION, -1);
        else if(Networking.state == Networking.State.CLIENT)
            queue(object, Networking.QueueType.EXCEPTION, Networking.client.getID());
    }

    public static void queue(Object object, Networking.QueueType queueType, int id){
        if(Networking.state == Networking.State.SERVER){
            if(queueType == Networking.QueueType.TARGET){
                Networking.players.get(id).queue.add(object);
                if(USING_LISTENERS && !Networking.players.get(id).isListenerAlive)
                    addListenerToConnection(Networking.server.getConnections()[id - 1]);
            }else if(queueType == Networking.QueueType.EXCEPTION){
                for (int i = 1; i < Networking.players.size; i++) {
                    if(i != id) {
                        Networking.players.get(i).queue.add(object);
                        if (USING_LISTENERS && !Networking.players.get(i).isListenerAlive)
                            addListenerToConnection(Networking.server.getConnections()[i - 1]);
                    }
                }
            }
        }else if(Networking.state == Networking.State.CLIENT){
            serverQueue.add(object);
            if(USING_LISTENERS && !isServerListenerAlive)
                addListenerToConnection(serverConnection);
        }
    }
}
