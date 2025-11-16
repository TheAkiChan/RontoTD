package com.rontoking.rontotd.game.systems.networking;

import com.badlogic.gdx.Gdx;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.networking.packets.*;

public class Receiver {
    public static boolean IS_SAME_DIR = false;

    public static void serverRecv(int id, Object object) {
        if (object instanceof CursorPacket) {
            CursorPacket cursorPacket = (CursorPacket) object;
            Networking.playerFrontend.get(id).cursorX = cursorPacket.x;
            Networking.playerFrontend.get(id).cursorY = cursorPacket.y;
        } else if (object instanceof TowerPacket) {
            ((TowerPacket) object).loadForServer(id);
        } else if (object instanceof String) {
            recvStringForServer((String) object, id);
        } else if (object instanceof byte[]) {
            recvBytes((byte[]) object);
        } else if (object instanceof FilePacket) {
            FileReceiver.loadFilePacket((FilePacket) object, id);
        } else if (object instanceof EditorPacket) {
            ((EditorPacket) object).load(id);
        } else if (object instanceof SyncPacket) {
            ((SyncPacket) object).load(id);
        } else if (object instanceof ChatPacket) {
            ((ChatPacket) object).loadForServer(id);
        }
    }

    public static void clientRecv(Object object) {
        if (object instanceof AddressPacket) {
            ((AddressPacket) object).load();
        } else if (object instanceof LevelPacket) {
            ((LevelPacket) object).load();
        } else if (object instanceof UpdatePacket) {
            UpdatePacket.clientUpdatePacket = (UpdatePacket)object;
            UpdatePacket.HAS_NEW_UPDATE_PACKET = true;
        } else if (object instanceof TowerPacket) {
            ((TowerPacket) object).loadForClient();
        } else if (object instanceof String) {
            recvStringForClient((String) object);
        } else if (object instanceof byte[]) {
            recvBytes((byte[]) object);
        }else if (object instanceof EditorPacket) {
            ((EditorPacket) object).load(-1);
        } else if (object instanceof FilePacket) {
            FileReceiver.loadFilePacket((FilePacket) object, -1);
        }else if (object instanceof SyncPacket) {
            ((SyncPacket) object).load(-1);
        } else if (object instanceof ChatPacket) {
            ((ChatPacket) object).loadForClient();
        }
    }

    private static void recvStringForServer(String msg, int senderID){
        if(msg.equals(Networking.READY_MSG)){
            if(GameState.phase == GameState.Phase.BUILD && !Networking.playerFrontend.get(senderID).isReady) {
                Networking.playerFrontend.get(senderID).isReady = true;
                Networking.playerReady();
            }
        }else if(msg.equals(Networking.FILE_DONE_MSG)) {
            FileReceiver.writeFile();
        }
    }

    private static void recvStringForClient(String msg){
        if(msg.equals(Networking.FILE_DONE_MSG)){
            FileReceiver.writeFile();
        }else if(msg.equals(Networking.ALL_FILES_SENT)){
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Main.loadGameFiles();
                }
            });
        }
    }

    private static void recvBytes(byte[] bytes){
        FileReceiver.recvBytes(bytes);
    }
}
