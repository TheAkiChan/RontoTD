package com.rontoking.rontotd.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rontoking.rontotd.editor.components.*;
import com.rontoking.rontotd.game.entities.Structure;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.networking.packets.EditorPacket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Editor extends Application {
    private static final int WIDTH = 1600;
    private static final int HEIGHT = 900;
    private static final String TITLE = "Editor";

    public static final boolean DEFAULT_AUTO_SAVE_SETTING = true;
    public static boolean SAFE_TO_SAVE = true;


    public static Stage primaryStage;
    private static TabPane tabPane;

    public enum State{
        NOT_LOADED, LOADING, LOADED
    }

    public enum Type{
        LEVEL, TILE, STRUCTURE, TOWER, ENEMY
    }

    public static State state = State.NOT_LOADED;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stg) {
        initTabPane();
        initStage(stg);
        LevelEditor.update();
        LevelEditor.initCanvasLoop();
        state = State.LOADED;
    }

    private static void initTabPane(){
        LevelEditor.initPane();
        TileEditor.initPane();
        StructureEditor.initPane();
        TowerEditor.initPane();
        EnemyEditor.initPane();

        LevelEditor.tab = new Tab("Levels");
        LevelEditor.tab.setContent(LevelEditor.pane);

        TileEditor.tab = new Tab("Tiles");
        TileEditor.tab.setContent(TileEditor.pane);

        StructureEditor.tab = new Tab("Structures");
        StructureEditor.tab.setContent(StructureEditor.pane);

        TowerEditor.tab = new Tab("Towers");
        TowerEditor.tab.setContent(TowerEditor.pane);

        EnemyEditor.tab = new Tab("Enemies");
        EnemyEditor.tab.setContent(EnemyEditor.pane);

        tabPane = new TabPane();
        tabPane.getTabs().addAll(LevelEditor.tab, TileEditor.tab, StructureEditor.tab, TowerEditor.tab, EnemyEditor.tab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    }

    private static void initStage(Stage stg){
        primaryStage = stg;
        primaryStage.setTitle(TITLE);
        primaryStage.getIcons().add(getImageFromFileHandle(Gdx.files.local(Assets.iconPath + "/icon512.png")));
        primaryStage.getIcons().add(getImageFromFileHandle(Gdx.files.local(Assets.iconPath + "/icon256.png")));
        primaryStage.getIcons().add(getImageFromFileHandle(Gdx.files.local(Assets.iconPath + "/icon128.png")));
        primaryStage.getIcons().add(getImageFromFileHandle(Gdx.files.local(Assets.iconPath + "/icon32.png")));
        primaryStage.getIcons().add(getImageFromFileHandle(Gdx.files.local(Assets.iconPath + "/icon16.png")));
        primaryStage.setResizable(true);
        Platform.setImplicitExit(false);

        Scene scene = new Scene(tabPane, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public static void replaceImage(String folderOfOldImage, File fileOfNewImage, String nameOfNewImage, String nameOfOldImage){
        replaceFile(folderOfOldImage, fileOfNewImage, nameOfNewImage, nameOfOldImage, "png");
    }

    public static void replaceSound(String folderOfOldSound, File fileOfNewSound, String nameOfNewSound, String nameOfOldSound) {
        replaceFile(folderOfOldSound, fileOfNewSound, nameOfNewSound, nameOfOldSound, "ogg");
    }

    private static void replaceFile(String folderOfOldFile, File fileOfNewFile, String nameOfNewFile, String nameOfOldFile, String fileExtension){
        if(fileOfNewFile != null) {
            nameOfNewFile = nameOfNewFile.toLowerCase();
            nameOfOldFile = nameOfOldFile.toLowerCase();
            FileHandle oldFileHandle = Gdx.files.local(folderOfOldFile + "/" + nameOfOldFile + "." + fileExtension);
            if (!oldFileHandle.file().getAbsolutePath().equals(fileOfNewFile.getAbsolutePath())) {
                InputStream inputStream = Gdx.files.absolute(fileOfNewFile.getPath()).read();
                oldFileHandle.write(inputStream, false);
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!nameOfNewFile.equals(nameOfOldFile)) {
                oldFileHandle.moveTo(oldFileHandle.parent().child(nameOfNewFile + "." + fileExtension));
            }
        }
    }

    public static Image getImageFromFileHandle(FileHandle fileHandle){
        InputStream inputStream = fileHandle.read();
        Image image = new Image(inputStream);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static void copyFile(FileHandle targetHandle, FileHandle handleToCopy){
        InputStream inputStream = handleToCopy.read();
        targetHandle.write(inputStream, false);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void swapFiles(String path1, String path2){
        Gdx.files.local(path1).moveTo(Gdx.files.local(path1).parent().child("temp"));
        Gdx.files.local(path2).moveTo(Gdx.files.local(path1));
        Gdx.files.local(path1).parent().child("temp").moveTo(Gdx.files.local(path2));
    }

    public static ChangeListener<String> autoSaveChangeListener(final Type type){
        return new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                autoSave(type, false);
            }
        };
    }

    public static EventHandler<ActionEvent> autoSaveEventHandler(final Type type){
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                autoSave(type, false);
            }
        };
    }

    public static ChangeListener<String> autoSaveChangeListener(final Type type, final boolean saveAssets){
        return new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                autoSave(type, saveAssets);
            }
        };
    }

    public static EventHandler<ActionEvent> autoSaveEventHandler(final Type type, final boolean saveAssets){
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                autoSave(type, saveAssets);
            }
        };
    }

    private static void autoSave(Type type, boolean saveAssets){
        switch (type) {
            case LEVEL:
                LevelEditor.autoSave();
                break;
            case TILE:
                TileEditor.autoSave(saveAssets);
                break;
            case STRUCTURE:
                StructureEditor.autoSave(saveAssets);
                break;
            case TOWER:
                TowerEditor.autoSave(saveAssets);
                break;
            case ENEMY:
                EnemyEditor.autoSave(saveAssets);
                break;
            default:
                break;
        }
    }

    public static ObservableList getObservableListFromEnum(Enum[] e){
        String[] list = new String[e.length];
        for (int i = 0; i < list.length; i++) {
            list[i] = e[i].name().replace("_", " ");
        }
        return FXCollections.observableArrayList(list);
    }

    public static ObservableList getObservableListFromEnum(Enum[] e, int start){
        return getObservableListFromEnum(e, start, e.length);
    }

    public static ObservableList getObservableListFromEnum(Enum[] e, int start, int end){
        String[] list = new String[end - start];
        for (int i = start; i < end; i++) {
            list[i - start] = e[i].name().replace("_", " ");
        }
        return FXCollections.observableArrayList(list);
    }

    public static void open(){
        Gdx.input.setCursorCatched(false);
        if(state == State.LOADED){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    primaryStage.show();
                }
            });
        }else if(state == State.NOT_LOADED){
            state = State.LOADING;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    launch(Editor.class);
                }
            }).start();
        }
    }
}
