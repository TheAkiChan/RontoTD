package com.rontoking.rontotd.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.rontoking.rontotd.game.systems.*;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.game.systems.networking.Networking;

public class MenuNode {
    public Type type;
    public Texture texture;
    public String name, text, prevText;
    public static float bonusButtonHeight = 0.25f;
    public int min, max, marker, height, index;
    public Rectangle checkboxRect;
    public boolean isChecked;

    private static final int MARKER_WIDTH = 32;
    private static final int MARKER_HEIGHT = 32;
    private static final int TEXT_INPUT_HEIGHT = 48;
    public static final int MAX_TEXT_INPUT_LENGTH = 20;

    public enum Type{
        EMPTY, BUTTON, SLIDER, CHECKBOX, TEXT_INPUT
    }

    public MenuNode(){
        this.type = Type.EMPTY;
    }

    public MenuNode(Texture texture, String name){
        this.type = Type.BUTTON;
        this.texture = texture;
        this.name = name;
    }

    public MenuNode(String name, int min, int max, int marker){
        this.type = Type.SLIDER;
        this.name = name;
        this.min = min;
        this.max = max;
        this.marker = marker;
    }

    public MenuNode(String name, boolean isChecked, int index, int numOfNodes){
        this.type = Type.CHECKBOX;
        this.name = name;
        this.isChecked = isChecked;

        Renderer.glyphLayout.setText(Menu.menuFont, name);
        this.height = (int)Renderer.glyphLayout.height;
        this.checkboxRect = new Rectangle(Main.DESKTOP_WIDTH / 2 + Renderer.glyphLayout.width / 2 + 50, Menu.nodeY(height, numOfNodes, index) - 32, 64, 64);
    }

    public MenuNode(String name, String text, int index){
        this.type = Type.TEXT_INPUT;
        this.name = name;
        this.text = text;
        this.prevText = text;
        this.index = index;
    }

    public void draw(int index){
        switch (type){
            case EMPTY:
                break;
            case BUTTON:
                Menu.drawButton(this.texture, Menu.nodes.length, index);
                break;
            case SLIDER:
                Renderer.drawTextCenterOfScreen(name, Menu.menuFont, Menu.nodeY(10, Menu.nodes.length, index) - 60, Color.WHITE, false);
                Renderer.drawTextCenterOfScreen((int)((float)marker / (float)max * 100) + "%", Assets.topFont, Menu.nodeY(10, Menu.nodes.length, index) + 20, Color.WHITE, false);
                Menu.drawNode(Assets.pixel, 500, 10, Menu.nodes.length, index);
                Renderer.guiBatch.setColor(Color.GOLD);
                Renderer.drawGUITexture(Menu.markerTexture, (int)((float)Menu.nodeX(500) - MARKER_WIDTH / 2 + (float)marker * 500f / (float)max), Menu.nodeY(10, Menu.nodes.length, index) - (MARKER_HEIGHT - 10) / 2, MARKER_WIDTH, MARKER_HEIGHT, false);
                Renderer.guiBatch.setColor(Color.WHITE);
                break;
            case CHECKBOX:
                Renderer.drawTextCenterOfScreen(name, Menu.menuFont, Menu.nodeY(height, Menu.nodes.length, index), Color.WHITE, false, true);
                if(isChecked)
                    Renderer.drawGUITextureRect(Menu.checkboxTexture, (int)checkboxRect.x, (int)checkboxRect.y, (int)checkboxRect.width, (int)checkboxRect.height, 0, 64, 64, 64, false);
                else
                    Renderer.drawGUITextureRect(Menu.checkboxTexture, (int)checkboxRect.x, (int)checkboxRect.y, (int)checkboxRect.width, (int)checkboxRect.height, 0, 0, 64, 64, false);
                break;
            case TEXT_INPUT:
                Menu.drawNode(Assets.pixel, 500, TEXT_INPUT_HEIGHT, Menu.nodes.length, index);
                Renderer.drawTextCenterOfScreen(name + ":", Assets.chatFont, Menu.nodeY(TEXT_INPUT_HEIGHT, Menu.nodes.length, index) + TEXT_INPUT_HEIGHT + 10, Color.GOLD, false);
                if(Menu.SELECTED_TEXT_INPUT == index)
                    Renderer.drawTextCenteredY(text + "|", Assets.chatFont, Menu.nodeX(500) + 10, Menu.nodeY(TEXT_INPUT_HEIGHT, Menu.nodes.length, index) + TEXT_INPUT_HEIGHT / 2, Color.RED, false);
                else
                    Renderer.drawTextCenteredY(text, Assets.chatFont, Menu.nodeX(500) + 10, Menu.nodeY(TEXT_INPUT_HEIGHT, Menu.nodes.length, index) + TEXT_INPUT_HEIGHT / 2, Color.RED, false);
                break;
            default:
                break;
        }
    }

    public boolean containsY(float y, int index){
        switch(type){
            case BUTTON:
                return Main.DESKTOP_HEIGHT / (Menu.nodes.length + 1) * (Menu.nodes.length - index) - this.texture.getHeight() * (1 + bonusButtonHeight)  < y && y < Main.DESKTOP_HEIGHT / (Menu.nodes.length + 1) * (Menu.nodes.length - index) + this.texture.getHeight() * bonusButtonHeight;
            case SLIDER:
                return Main.DESKTOP_HEIGHT / (Menu.nodes.length + 1) * (Menu.nodes.length - index) - 10 - (MARKER_HEIGHT - 10) / 2  < y && y < Main.DESKTOP_HEIGHT / (Menu.nodes.length + 1) * (Menu.nodes.length - index) + (MARKER_HEIGHT - 10) / 2;
            case CHECKBOX:
                return y > checkboxRect.y && y < checkboxRect.y + checkboxRect.height;
            case TEXT_INPUT:
                return Main.DESKTOP_HEIGHT / (Menu.nodes.length + 1) * (Menu.nodes.length - index) - TEXT_INPUT_HEIGHT  < y && y < Main.DESKTOP_HEIGHT / (Menu.nodes.length + 1) * (Menu.nodes.length - index);
            default:
                return false;
        }
    }

    public int buttonHeight(){
        switch (type){
            case BUTTON:
                return (int)(this.texture.getHeight() * ( 2 * bonusButtonHeight + 1));
            case SLIDER:
                return MARKER_HEIGHT;
            case CHECKBOX:
                return (int)checkboxRect.height;
            case TEXT_INPUT:
                return TEXT_INPUT_HEIGHT;
            default:
                return 0;
        }
    }

    public int buttonY(int index){
        switch (type){
            case BUTTON:
                return (int)(Main.DESKTOP_HEIGHT / (Menu.nodes.length + 1) * (Menu.nodes.length - index) - this.texture.getHeight() * (bonusButtonHeight + 1));
            case SLIDER:
                return Main.DESKTOP_HEIGHT / (Menu.nodes.length + 1) * (Menu.nodes.length - index) - 10 - (MARKER_HEIGHT - 10) / 2;
            case CHECKBOX:
                return (int)checkboxRect.y;
            case TEXT_INPUT:
                return Main.DESKTOP_HEIGHT / (Menu.nodes.length + 1) * (Menu.nodes.length - index) - TEXT_INPUT_HEIGHT;
            default:
                return 0;
        }
    }

    public void press(){
        if(this.name.equals("Local")) {
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.singleplayerMenu();
        }
        else if(this.name.equals("Continue")){
            Level.num = Settings.LEVEL;
            Networking.state = Networking.State.SINGLEPLAYER;
            Menu.clickSound.play(Settings.MENU_VOLUME);
            GameState.setToTransition(Level.num, 30, true);
        }
        else if(this.name.equals("New Game")){
            Level.num = 0;
            Networking.state = Networking.State.SINGLEPLAYER;
            Menu.clickSound.play(Settings.MENU_VOLUME);
            GameState.setToTransition(Level.num, 30, true);
        }
        else if(this.name.equals("Lan")) {
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.internetMenu();
        }
        else if (this.name.equals("Host")) {
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.hostMenu();
        }
        else if (this.name.equals("Auto")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.startAuto();
        }
        else if (this.name.equals("Manual")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.startManual();
        }else if (this.name.equals("Back To Game")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Main.state = Main.State.GAME;
        }
        else if (this.name.equals("Exit To Menu")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            GameState.quitGame();
        }
        else if(this.name.equals("Quit Game")){
            Gdx.app.exit();
        }
        else if(this.name.equals("Settings")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.settingsMenu();
        }
        else if(this.name.equals("Back")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.back();
        }
        else if(this.name.equals("Start")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.start();
        }
        else if(this.name.equals("Game Volume")){
            setMarker();
            setGameVolume((float)marker / (float)max);
        }
        else if(this.name.equals("Music Volume")){
            setMarker();
            setMusicVolume((float)marker / (float)max);

        }else if(this.name.equals("Menu Volume")){
            setMarker();
            setMenuVolume((float)marker / (float)max);
        }else if(this.name.equals("Audio")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.audioMenu();
        }
        else if(this.name.equals("Graphics")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.graphicsMenu();
        }else if(this.name.equals("Show Effects")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            isChecked = !isChecked;
            Settings.SHOW_ANIM_EFFECTS = isChecked;
            Settings.preferences.putBoolean("show effects", isChecked);
            Settings.preferences.flush();
        }else if(this.name.equals("Show Health Bars")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            isChecked = !isChecked;
            Settings.SHOW_HEALTH_BARS = isChecked;
            Settings.preferences.putBoolean("show health bars", isChecked);
            Settings.preferences.flush();
        }else if(this.name.equals("Show Tooltips")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            isChecked = !isChecked;
            Settings.SHOW_TOOLTIPS = isChecked;
            Settings.preferences.putBoolean("show tooltips", isChecked);
            Settings.preferences.flush();
        }else if(this.name.equals("Toggle Fullscreen")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Settings.IS_FULLSCREEN = !Gdx.graphics.isFullscreen();
            Settings.preferences.putBoolean("is fullscreen", Settings.IS_FULLSCREEN);
            Settings.preferences.flush();

            if(Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(Main.windowWidth, Main.windowHeight);
            }
            else {
                Main.windowWidth = Gdx.graphics.getWidth();
                Main.windowHeight = Gdx.graphics.getHeight();
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }else if(this.name.equals("Start Host")) {
            Menu.startHostMenu();
        } else if(this.name.equals("Enter Port")) {
            int port;
            if(Menu.state == Menu.State.HOST)
                port = Settings.SERVER_PORT;
            else
                port = Settings.CLIENT_PORT;

            try {
                if (!text.equals("")) {
                    port = Integer.parseInt(text);
                    if(port < 0)
                        port = 0;
                    else if(port > 65535)
                        port = 65535;
                    text = port + "";
                }
                prevText = text;

            } catch (Exception e) {
                text = prevText;
                if(!text.equals(""))
                    port = Integer.parseInt(text);
            }
            if (!text.equals("")) {
                if(Menu.state == Menu.State.HOST) {
                    Settings.preferences.putInteger("server port", port);
                    Settings.preferences.flush();
                    Settings.SERVER_PORT = port;
                }
                else {
                    Settings.preferences.putInteger("client port", port);
                    Settings.preferences.flush();
                    Settings.CLIENT_PORT = port;
                }
            }
        }else if(this.name.equals("Enter IP")){
            Settings.preferences.putString("ip", text);
            Settings.preferences.flush();
            Settings.IP = text;
        }else if(this.name.equals("Start Manual")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.joinMenu(false);
        }
        else if(this.name.equals("Start Auto")){
            Menu.clickSound.play(Settings.MENU_VOLUME);
            Menu.joinMenu(true);
        }
    }

    private void setMarker(){
        if(InputHandler.mousePos.x <= Menu.nodeX(500))
            marker = min;
        else if(InputHandler.mousePos.x >= Menu.nodeX(500) + 500)
            marker = max;
        else
            marker = (int)((InputHandler.mousePos.x - (float)Menu.nodeX(500)) / 500f * (float)max);
    }

    private static void setGameVolume(float volume){
        Settings.GAME_VOLUME = volume;
        Settings.preferences.putFloat("game volume", volume);
        Settings.preferences.flush();
    }

    private static void setMusicVolume(float volume){
        Settings.MUSIC_VOLUME = volume;
        Settings.preferences.putFloat("music volume", volume);
        Settings.preferences.flush();

        Assets.menuMusic.setVolume(Settings.MUSIC_VOLUME);
        Assets.buildMusic.setVolume(Settings.MUSIC_VOLUME);
        Assets.defendMusic.setVolume(Settings.MUSIC_VOLUME);
    }

    private static void setMenuVolume(float volume){
        Settings.MENU_VOLUME = volume;
        Settings.preferences.putFloat("menu volume", volume);
        Settings.preferences.flush();
    }
}
