package com.rontoking.rontotd.game.systems.networking;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.game.entities.*;
import com.rontoking.rontotd.game.menu.Menu;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.Self;
import com.rontoking.rontotd.game.systems.Settings;
import com.rontoking.rontotd.game.systems.networking.packets.*;
import com.rontoking.rontotd.general.Point;

import java.io.IOException;
import java.net.InetAddress;

public class Networking {
    public enum State {
        SINGLEPLAYER, CLIENT, SERVER, NONE
    }

    public enum QueueType{
        EXCEPTION, TARGET
    }

    public static State state = State.NONE;
    public static Server server;
    public static Client client;

    public static final int WRITE_BUFFER_SIZE = 32000;
    public static final int OBJECT_BUFFER_SIZE = 32000;

    public static Array<Player> players = new Array<Player>();
    public static Array<PlayerFrontend> playerFrontend = new Array<PlayerFrontend>(true, 0, PlayerFrontend.class);
    public static int readyPlayerCount = 0;

    public static final String FILE_DONE_MSG = "File Done";
    public static final String READY_MSG = "Ready";
    public static final String ALL_FILES_SENT = "All Files Sent";

    public static void host(){
        FileSender.reset();
        players.clear();
        playerFrontend.clear();
        players.add(new Player()); // The server is the first player.
        playerFrontend.add(players.get(players.size - 1).playerFrontend);
        server = new Server(WRITE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
        registerClasses(server.getKryo());
        server.addListener(new Listener(){
            @Override
            public void connected(Connection connection) {
                if(Main.state == Main.State.MENU) {
                    players.add(new Player());
                    playerFrontend.add(players.get(players.size - 1).playerFrontend);
                    FileSender.sendAllFiles(connection, QueueType.TARGET, connection.getID());
                }
                else
                    connection.close();
            }

            @Override
            public void received(Connection connection, Object object) {
                Receiver.serverRecv(connection.getID(), object);
            }

            @Override
            public void disconnected(Connection connection) {
                if(Main.state == Main.State.GAME)
                    GameState.quitGame();
                else if(Main.state == Main.State.MENU){
                    Menu.stopServer();
                }
            }
        });
        try {
            server.bind(Settings.SERVER_PORT, Settings.SERVER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.start();
        state = State.SERVER;
    }

    public static void begin(){
        GameState.setToTransition(0, 60 * 2, true);
    }

    public static boolean join(boolean isLAN){
        FileSender.reset();
        try {
            players.clear();
            playerFrontend.clear();
            UpdatePacket.clientUpdatePacket = null;
            UpdatePacket.HAS_NEW_UPDATE_PACKET = false;
            client = new Client(WRITE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
            registerClasses(client.getKryo());
            client.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    FileSender.serverConnection = connection;
                }

                @Override
                public void received(Connection connection, Object object) {
                    Receiver.clientRecv(object);
                }

                @Override
                public void disconnected(Connection connection) {
                    if (Main.state == Main.State.GAME) {
                        GameState.quitGame();
                    }else if(Main.state == Main.State.MENU){
                        Menu.stopClient();
                    }
                }
            });
            client.start();
            if (isLAN) {
                InetAddress host = client.discoverHost(Settings.CLIENT_PORT, 5000);
                client.connect(5000, host, Settings.CLIENT_PORT, Settings.CLIENT_PORT);
            } else {
                client.connect(5000, Settings.IP, Settings.CLIENT_PORT, Settings.CLIENT_PORT);
            }
            state = State.CLIENT;
            return true;
        }catch(Exception ex){
            return false;
        }
    }

    public static void update(){
        FileSender.sendQueued();
        if((Networking.state == State.SERVER || Networking.state == State.CLIENT) && Main.state == Main.State.MENU && Menu.IS_GAME_OPTIONS){
            GameState.update();
            Self.check();
        }
        if(Main.state == Main.State.GAME || (Main.state == Main.State.MENU && Menu.IS_GAME_OPTIONS)) {
            if (state == State.SERVER) {
                playerFrontend.get(0).updateCursorPos();
                Sender.sendServerUpdate();
            } else if (state == State.CLIENT) {
                Sender.sendClientUpdate();
            }
        }
    }

    private static void registerClasses(Kryo kryo){
        kryo.register(Object[].class);
        kryo.register(byte.class);
        kryo.register(byte[].class);
        kryo.register(boolean.class);
        kryo.register(int.class);
        kryo.register(int[].class);
        kryo.register(float.class);
        kryo.register(float[].class);
        kryo.register(String.class);
        kryo.register(Rectangle.class);
        kryo.register(Ability.Type.class);
        kryo.register(Ability.EnemyStat.class);
        kryo.register(Ability.TowerStat.class);
        kryo.register(Ability.Target.class);
        kryo.register(Ability.Math.class);
        kryo.register(Ability.class);
        kryo.register(Ability[].class);
        kryo.register(LevelPacket.class);
        kryo.register(CursorPacket.class);
        kryo.register(PlayerFrontend.class);
        kryo.register(PlayerFrontend[].class);
        kryo.register(TowerPacket.class);
        kryo.register(Point.class);
        kryo.register(EnemyRenderable.Side.class);
        kryo.register(AttackType.class);
        kryo.register(Vector2.class);
        kryo.register(Sorted.Type.class);
        kryo.register(Color.class);
        kryo.register(AnimEffect.Type.class);
        kryo.register(AnimEffect.class);
        kryo.register(AnimEffect[].class);
        kryo.register(EnemyRenderable.Side.class);
        kryo.register(EnemyRenderable.TransportType.class);
        kryo.register(EnemyRenderable.class);
        kryo.register(EnemyRenderable[].class);
        kryo.register(MissileRenderable.class);
        kryo.register(MissileRenderable[].class);
        kryo.register(TowerRenderable.class);
        kryo.register(Sorted.class);
        kryo.register(Sorted[].class);
        kryo.register(GameState.Phase.class);
        kryo.register(SpawnerRenderable.class);
        kryo.register(SpawnerRenderable[].class);
        kryo.register(FilePacket.class);
        kryo.register(AddressPacket.class);
        kryo.register(Editor.Type.class);
        kryo.register(EditorPacket.class);
        kryo.register(SyncPacket.Type.class);
        kryo.register(SyncPacket.class);
        kryo.register(UpdatePacket.class);
        kryo.register(ChatPacket.class);
    }

    public static void playerReady(){
        Networking.readyPlayerCount++;
        if(Networking.readyPlayerCount >= Networking.players.size){
            GameState.phase = GameState.Phase.DEFEND;
        }
    }

    public static void resetReady(){
        readyPlayerCount = 0;
        for(int i = 0; i < players.size; i++){
            playerFrontend.get(i).isReady = false;
        }
    }

    public static void transition(int levelNum, boolean clearTowers){
        if(state == State.SERVER) {
            resetReady();
            Sender.sendLevel(levelNum, clearTowers);
        }
    }
}
