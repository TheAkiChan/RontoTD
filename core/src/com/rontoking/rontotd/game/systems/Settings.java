package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Settings {
    public static float GAME_VOLUME = 1;
    public static float MUSIC_VOLUME = 1;
    public static float MENU_VOLUME = 1;
    public static boolean SHOW_ANIM_EFFECTS = true;
    public static boolean SHOW_HEALTH_BARS = true;
    public static boolean SHOW_TOOLTIPS = true;
    public static boolean IS_FULLSCREEN = false;
    public static int SERVER_PORT = 25565;
    public static int CLIENT_PORT = 25565;
    public static String IP = "localhost";
    public static int LEVEL = 0;

    public static Preferences preferences;

    public static void load(){
        preferences = Gdx.app.getPreferences("RontoTD");
        GAME_VOLUME = preferences.getFloat("game volume", 1);
        MUSIC_VOLUME = preferences.getFloat("music volume", 1);
        MENU_VOLUME = preferences.getFloat("menu volume", 1);

        SHOW_ANIM_EFFECTS = preferences.getBoolean("show effects", true);
        SHOW_HEALTH_BARS = preferences.getBoolean("show health bars", true);
        SHOW_TOOLTIPS = preferences.getBoolean("show tooltips", true);
        IS_FULLSCREEN = preferences.getBoolean("is fullscreen", false);

        SERVER_PORT = preferences.getInteger("server port", 25565);
        CLIENT_PORT = preferences.getInteger("client port", 25565);
        IP = preferences.getString("ip", "localhost");

        LEVEL = preferences.getInteger("level", 0);
    }
}
