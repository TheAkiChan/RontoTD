package com.rontoking.rontotd.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.game.systems.*;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.Player;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class Menu {
    public static Texture singleplayerTexture, lanMultiplayerTexture, logoTexture, hostTexture, autoJoinTexture, manualJoinTexture, backToGameTexture, settingsTexture, exitToMenuTexture, quitGameTexture, backTexture, startTexture, audioTexture, graphicsTexture, checkboxTexture, markerTexture, toggleFullscreenTexture, newGameTexture, continueTexture;
    public static Sound clickSound;
    public static BitmapFont menuFont;
    public static MenuNode[] nodes;
    public static State state = State.MAIN;
    public static boolean IS_GAME_OPTIONS = false;
    private static int SELECTED_BUTTON_INDEX = -1;
    public static int SELECTED_TEXT_INPUT = -1;
    private static Color BUTTON_SELECTION_COLOR = Color.NAVY;
    private static boolean HAS_RELEASED = true;

    public enum State {
        MAIN, INTERNET, HOST, START_HOST, START_MANUAL, JOIN, START_AUTO, GAME_OPTIONS, SETTINGS, AUDIO, GRAPHICS, SINGLEPLAYER
    }

    public static void mainMenu(){
        state = State.MAIN;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode(singleplayerTexture, "Local"),
                new MenuNode(lanMultiplayerTexture, "Lan"),
                new MenuNode(settingsTexture, "Settings"),
                new MenuNode(quitGameTexture, "Quit Game")
        };
        HAS_RELEASED = false;
    }

    public static void singleplayerMenu(){
        state = State.SINGLEPLAYER;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode(continueTexture, "Continue"),
                new MenuNode(newGameTexture, "New Game"),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void startManual(){
        state = State.START_MANUAL;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode("Enter IP", Settings.IP + "", 0),
                new MenuNode("Enter Port", Settings.CLIENT_PORT + "", 1),
                new MenuNode(manualJoinTexture, "Start Manual"),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void startAuto(){
        state = State.START_AUTO;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode("Enter Port", Settings.CLIENT_PORT + "", 1),
                new MenuNode(autoJoinTexture, "Start Auto"),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void hostMenu(){
        state = State.HOST;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode("Enter Port", Settings.SERVER_PORT + "", 0),
                new MenuNode(hostTexture, "Start Host"),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void gameOptionsMenu(){
        state = State.GAME_OPTIONS;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode(backToGameTexture, "Back To Game"),
                new MenuNode(settingsTexture, "Settings"),
                new MenuNode(exitToMenuTexture, "Exit To Menu"),
                new MenuNode(quitGameTexture, "Quit Game")
        };
        HAS_RELEASED = false;
    }

    public static void graphicsMenu(){
        state = State.GRAPHICS;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode("Show Effects", Settings.SHOW_ANIM_EFFECTS, 0, 5),
                new MenuNode("Show Health Bars", Settings.SHOW_HEALTH_BARS, 1, 5),
                new MenuNode("Show Tooltips", Settings.SHOW_TOOLTIPS, 2, 5),
                new MenuNode(toggleFullscreenTexture, "Toggle Fullscreen"),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void audioMenu(){
        state = State.AUDIO;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode("Game Volume", 0, 100, (int)(Settings.GAME_VOLUME * 100)),
                new MenuNode("Music Volume", 0, 100, (int)(Settings.MUSIC_VOLUME * 100)),
                new MenuNode("Menu Volume", 0, 100, (int)(Settings.MENU_VOLUME * 100)),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void settingsMenu(){
        state = State.SETTINGS;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode(audioTexture, "Audio"),
                new MenuNode(graphicsTexture, "Graphics"),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void internetMenu(){
        state = State.INTERNET;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode(hostTexture, "Host"),
                new MenuNode(manualJoinTexture, "Manual"),
                new MenuNode(autoJoinTexture, "Auto"),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void joinMenu(boolean isLAN){
        state = State.JOIN;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
        if(!Networking.join(isLAN))
            stopClient();
    }

    public static void startHostMenu(){
        state = State.START_HOST;
        Networking.host();
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode(startTexture, "Start"),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void startManualMenu(){
        state = State.START_MANUAL;
        SELECTED_BUTTON_INDEX = -1;
        nodes = new MenuNode[]{
                new MenuNode(),
                new MenuNode(backTexture, "Back")
        };
        HAS_RELEASED = false;
    }

    public static void stopServer(){
        state = State.INTERNET;
        Networking.server.stop();
        Networking.state = Networking.State.NONE;
        internetMenu();
    }

    public static void stopClient(){
        state = State.INTERNET;
        Networking.client.stop();
        Networking.state = Networking.State.NONE;
        internetMenu();
    }

    public static void update() {
        if(!HAS_RELEASED && !Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            HAS_RELEASED = true;
        InputHandler.handleCursorCatching();
        InputHandler.handleEditor();
        if (Gdx.input.isCursorCatched()) {
            InputHandler.handleMouse();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                back();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                start();
            }
            getSelectedButton();
            handlePressingButton();
        }
    }

    private static void getSelectedButton(){
        if(HAS_RELEASED && (SELECTED_BUTTON_INDEX == -1 || nodes[SELECTED_BUTTON_INDEX].type != MenuNode.Type.SLIDER || !Gdx.input.isButtonPressed(Input.Buttons.LEFT))) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].containsY(Main.DESKTOP_HEIGHT - InputHandler.mousePos.y, i)) {
                    SELECTED_BUTTON_INDEX = i;
                    return;
                }
                SELECTED_BUTTON_INDEX = -1;
            }
        }
    }

    private static void handlePressingButton(){
        if(SELECTED_BUTTON_INDEX != -1) {
            switch (nodes[SELECTED_BUTTON_INDEX].type){
                case BUTTON:
                    if (Gdx.input.justTouched()) {
                        nodes[SELECTED_BUTTON_INDEX].press();
                        SELECTED_BUTTON_INDEX = -1;
                    }
                    break;
                case SLIDER:
                    if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                        nodes[SELECTED_BUTTON_INDEX].press();
                    }
                    break;
                case CHECKBOX:
                    if (Gdx.input.justTouched()) {
                        nodes[SELECTED_BUTTON_INDEX].press();
                        SELECTED_BUTTON_INDEX = -1;
                    }
                    break;
                case TEXT_INPUT:
                    if (Gdx.input.justTouched()) {
                        if(SELECTED_TEXT_INPUT == SELECTED_BUTTON_INDEX)
                            SELECTED_TEXT_INPUT = -1;
                        else
                            SELECTED_TEXT_INPUT = SELECTED_BUTTON_INDEX;
                        clickSound.play(Settings.MENU_VOLUME);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static void handleTypingInTextInput(char character){
        if(SELECTED_TEXT_INPUT != -1) {
            if ((Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) && Gdx.input.isKeyJustPressed(Input.Keys.V))) {
                try {
                    nodes[SELECTED_TEXT_INPUT].text += ((String) Toolkit.getDefaultToolkit()
                            .getSystemClipboard().getData(DataFlavor.stringFlavor)).replace("\n", "");
                    Menu.clickSound.play(Settings.MENU_VOLUME);
                } catch (UnsupportedFlavorException e) {
                    //e.printStackTrace();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            } else if (!Character.isISOControl(character)) {
                nodes[SELECTED_TEXT_INPUT].text += character;
                Menu.clickSound.play(Settings.MENU_VOLUME);
            } else if (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE)) {
                if (nodes[SELECTED_TEXT_INPUT].text.length() > 0)
                    nodes[SELECTED_TEXT_INPUT].text = nodes[SELECTED_TEXT_INPUT].text.substring(0, nodes[SELECTED_TEXT_INPUT].text.length() - 1);
                Menu.clickSound.play(Settings.MENU_VOLUME);
            }
            if (nodes[SELECTED_TEXT_INPUT].text.length() > MenuNode.MAX_TEXT_INPUT_LENGTH)
                nodes[SELECTED_TEXT_INPUT].text = nodes[SELECTED_TEXT_INPUT].text.substring(0, MenuNode.MAX_TEXT_INPUT_LENGTH);
            nodes[SELECTED_TEXT_INPUT].press();
        }
    }

    public static void start(){
        if (state == State.START_HOST) {
            for(Player player : Networking.players) // Make sure all files are sent.
                if(player.queue.size > 0)
                    return;
            Networking.begin();
        }
    }

    public static void back() {
        SELECTED_TEXT_INPUT = -1;
        if (state == State.INTERNET) {
            clickSound.play(Settings.MENU_VOLUME);
            mainMenu();
        } else if (state == State.HOST) {
            clickSound.play(Settings.MENU_VOLUME);
            internetMenu();
        } else if (state == State.START_HOST) {
            clickSound.play(Settings.MENU_VOLUME);
            stopServer();
        } else if (state == State.START_MANUAL) {
            clickSound.play(Settings.MENU_VOLUME);
            internetMenu();
        } else if (state == State.START_AUTO) {
            clickSound.play(Settings.MENU_VOLUME);
            internetMenu();
        } else if (state == State.JOIN) {
            clickSound.play(Settings.MENU_VOLUME);
            stopClient();
        } else if (state == State.MAIN) {
            Gdx.app.exit();
        } else if (state == State.GAME_OPTIONS) {
            clickSound.play(Settings.MENU_VOLUME);
            Main.state = Main.State.GAME;
        } else if (state == State.SETTINGS) {
            clickSound.play(Settings.MENU_VOLUME);
            if (IS_GAME_OPTIONS)
                gameOptionsMenu();
            else
                mainMenu();
        } else if (state == State.AUDIO || state == State.GRAPHICS) {
            clickSound.play(Settings.MENU_VOLUME);
            settingsMenu();
        } else if (state == State.SINGLEPLAYER) {
            clickSound.play(Settings.MENU_VOLUME);
            mainMenu();
        }
    }

    public static void render(){
        drawButtonSelection();
        drawNetworkingText();
        drawNodes();
        drawLogo();
    }

    private static void drawButtonSelection(){
        if(SELECTED_BUTTON_INDEX != -1){
            drawButtonSelection(SELECTED_BUTTON_INDEX);
        }if(SELECTED_TEXT_INPUT != -1){
            drawButtonSelection(SELECTED_TEXT_INPUT);
        }
    }

    private static void drawButtonSelection(int index){
        Renderer.guiBatch.setColor(BUTTON_SELECTION_COLOR);
        Renderer.drawGUITexture(Assets.pixel, 0, nodes[index].buttonY(index), Main.DESKTOP_WIDTH, nodes[index].buttonHeight(), false);
        Renderer.guiBatch.setColor(Color.WHITE);
    }

    private static void drawLogo(){
        if(state == State.MAIN)
            Renderer.guiBatch.draw(logoTexture, Main.DESKTOP_WIDTH / 2 - logoTexture.getWidth() / 2, Main.DESKTOP_HEIGHT - logoTexture.getHeight() - 10);
    }

    private static void drawNetworkingText(){
        if(state == State.JOIN && Networking.client != null){
            if(Networking.client.isConnected())
                Renderer.drawTextCenterOfScreen("Connected. Waiting for host to start.", menuFont, Main.DESKTOP_HEIGHT / 2 - 100, Color.WHITE, true, true);
            else
                Renderer.drawTextCenterOfScreen("Connecting to server...", menuFont, Main.DESKTOP_HEIGHT / 2 - 100, Color.WHITE, true, true);
        }else if(state == State.START_HOST && Networking.server != null && Networking.server.getConnections() != null){
            for(int i = 0; i < Networking.players.size; i++){
                if(Networking.players.get(i).queue.size > 0){
                    Renderer.drawText("Connecting #" + i + "\nPackets Left: " + Networking.players.get(i).queue.size, menuFont, 510, 365, Color.WHITE, false);
                    return;
                }

            }
            Renderer.drawTextCenterOfScreen("Connected: " + Networking.server.getConnections().length + "\nReady To Start", menuFont, Main.DESKTOP_HEIGHT / 2 - 25, Color.WHITE, false, true);
        }
    }

    private static void drawNodes(){
        for(int i = 0; i < nodes.length; i++){
            nodes[i].draw(i);
        }
    }

    public static void drawButton(Texture t, int heightFraction, int heightNum){
        drawNode(t, t.getWidth(), t.getHeight(), heightFraction, heightNum);
    }

    public static void drawNode(Texture t, int width, int height, int heightFraction, int heightNum){
        Renderer.guiBatch.draw(t, nodeX(width), nodeY(height, heightFraction, heightNum), width, height);
    }

    public static int nodeX(int width){
        return Main.DESKTOP_WIDTH / 2 - width / 2;
    }

    public static int nodeY(int height, int heightFraction, int heightNum){
        return Main.DESKTOP_HEIGHT / (heightFraction + 1) * (heightFraction - heightNum) - height;
    }
}
