package com.rontoking.rontotd.editor.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.editor.controls.*;
import com.rontoking.rontotd.game.entities.*;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.Camera;
import com.rontoking.rontotd.game.systems.GameState;
import com.rontoking.rontotd.game.systems.networking.FileSender;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.packets.SyncPacket;
import com.rontoking.rontotd.game.systems.networking.packets.EditorPacket;
import com.rontoking.rontotd.general.Utility;
import com.rontoking.rontotd.game.entities.Tile;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LevelEditor {
    private static int gridSize = 64;
    private static boolean AUTO_SAVE_ON = Editor.DEFAULT_AUTO_SAVE_SETTING;
    private static final String defaultLevel =
            "0.\n" +
            "\n" +
            "0, 0, 0.\n" +
            "\n" +
            ".\n" +
            "\n" +
            "10.\n" +
            "\n" +
            "12.\n" +
            "\n" +
            "true.\n" +
            "\n" +
            "1.";

    public static Tab tab;
    public static BorderPane pane;
    private static BorderPane listPane, hBoxPane;
    private static ScrollPane scrollPane, spawnerScrollPane;
    private static Canvas canvas;
    private static Array<Vector3> tiles = new Array<Vector3>();
    private static Array<Vector3> structures = new Array<Vector3>();
    public static Array<Spawner> spawners = new Array<com.rontoking.rontotd.game.entities.Spawner>();

    private static HBox levelHBox, playerHBox;
    private static VBox spawnerVBox, waveVBox;
    private static WaveGUI[] waveNodes;
    private static Label selectedSpawnerPosLabel;
    private static Button saveButton, addWaveButton, prevButton, nextButton, newButton, duplicateButton, deleteButton, swapButton, loadInEditorButton, loadInGameButton;
    private static CheckBox spawnerCheckBox, sharedGold, autoSave;

    private static LabeledIntField tileSize, levelWidth, levelHeight, lives, gold, tileEditSize, levelNum;
    private static IntField targetLevelNum;
    private static LabeledFloatField cameraZoom;

    private static ListView tileList, structureList;

    private static Image[] tileImages, structureImages;
    private static Image spawnerImage;
    private static int selectedIndex = 0;
    public static int selectedSpawnerIndex = -1;
    private static final int defaultSize = 20;
    private static boolean isTileSelected = true;

    private static boolean UP_PRESSED, DOWN_PRESSED, RIGHT_PRESSED, LEFT_PRESSED = false;

    public static void initPane(){
        initScrollPane();
        initHBoxPane();
        initListPane();
        initSpawnerBox();

        pane = new BorderPane();
        pane.setCenter(scrollPane);
        pane.setBottom(hBoxPane);
        pane.setLeft(listPane);
        pane.setRight(spawnerScrollPane);
    }

    private static void initScrollPane(){
        canvas = new Canvas(gridSize * defaultSize, gridSize * defaultSize);
        canvas.getGraphicsContext2D().setFont(new Font("impact", gridSize));

        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleMouseInput(event);
            }
        });
        canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleMouseInput(event);
            }
        });

        scrollPane = new ScrollPane(canvas);
        canvas.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                canvas.requestFocus();
            }
        });
        canvas.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.W)
                    UP_PRESSED = true;
                if (event.getCode() == KeyCode.A)
                    LEFT_PRESSED = true;
                if (event.getCode() == KeyCode.S)
                    DOWN_PRESSED = true;
                if (event.getCode() == KeyCode.D)
                    RIGHT_PRESSED = true;
            }
        });
        canvas.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.W)
                    UP_PRESSED = false;
                if (event.getCode() == KeyCode.A)
                    LEFT_PRESSED = false;
                if (event.getCode() == KeyCode.S)
                    DOWN_PRESSED = false;
                if (event.getCode() == KeyCode.D)
                    RIGHT_PRESSED = false;
            }
        });
        canvas.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                tileSize.field.set(tileSize.field.num() + (int)event.getDeltaY() / 10);
                if(tileSize.field.num() <= 0)
                    tileSize.field.set(1);
            }
        });
    }

    private static double levelEditorScrollValue(double currentValue, int direction){
        return currentValue +  direction * 0.8f / tileSize.field.num();
    }

    private static void handleMouseInput(MouseEvent event){
        if(event.getButton() == MouseButton.PRIMARY) {
            if(spawnerCheckBox.isSelected() && exists(Utility.floor((int)event.getX(), gridSize) / gridSize, Utility.floor((int)event.getY(), gridSize) / gridSize, isTileSelected)){
                selectedSpawnerIndex = indexOf(Utility.floor((int)event.getX(), gridSize) / gridSize, Utility.floor((int)event.getY(), gridSize) / gridSize, isTileSelected);
                updateSpawnBox();
            }else
                add(new Vector3(Utility.floor((int) event.getX(), gridSize) / gridSize, Utility.floor((int) event.getY(), gridSize) / gridSize, selectedIndex), isTileSelected);
        }
        else if(event.getButton() == MouseButton.SECONDARY)
            remove(new Vector2(Utility.floor((int)event.getX(), gridSize) / gridSize, Utility.floor((int)event.getY(), gridSize) / gridSize), isTileSelected);
    }

    public static void updateSpawnBox(){
        waveVBox.getChildren().clear();
        if(selectedSpawnerIndex != -1) {
            addWaveButton.setVisible(true);
            selectedSpawnerPosLabel.setVisible(true);
            selectedSpawnerPosLabel.setText("X: " + spawners.get(selectedSpawnerIndex).spawnerRenderable.x + ", Y: " + spawners.get(selectedSpawnerIndex).spawnerRenderable.y + " ");
            waveNodes = new WaveGUI[spawners.get(selectedSpawnerIndex).waves.size];
            for (int i = 0; i < waveNodes.length; i++) {
                waveNodes[i] = new WaveGUI(spawners.get(selectedSpawnerIndex).waves.get(i), selectedSpawnerIndex, i);
                waveVBox.getChildren().add(waveNodes[i].spawnBox);
            }
        }else {
            selectedSpawnerPosLabel.setText(" No spawner is selected. ");
            addWaveButton.setVisible(false);
            selectedSpawnerPosLabel.setVisible(false);
        }
        spawnerVBox.setPrefWidth(300);
    }

    private static void initHBoxPane(){
        lives = new LabeledIntField(" Lives: ", 10, 1);
        lives.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.LEVEL));
        gold = new LabeledIntField(" Gold: ", 50, 0);
        gold.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.LEVEL));
        sharedGold = new CheckBox("Shared Gold");
        sharedGold.setOnAction(Editor.autoSaveEventHandler(Editor.Type.LEVEL));
        cameraZoom = new LabeledFloatField(" Camera Zoom: ", 1, Camera.MIN_ZOOM, Camera.MAX_ZOOM);
        cameraZoom.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.LEVEL));
        playerHBox = new HBox();
        playerHBox.getChildren().addAll(lives, gold, cameraZoom, sharedGold);
        playerHBox.setAlignment(Pos.CENTER);

        tileEditSize = new LabeledIntField(" Edit Size: ", 1, 1);

        tileSize = new LabeledIntField(" Tile Size: ", gridSize, 1, 128);
        tileSize.field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                gridSize = tileSize.field.num();
                canvas.setWidth(gridSize * levelWidth.field.num());
                canvas.setHeight(gridSize * levelHeight.field.num());
                canvas.getGraphicsContext2D().setFont(new Font("impact", gridSize));
            }
        });
        levelWidth = new LabeledIntField(" Width: ", defaultSize, 1);
        levelWidth.field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                canvas.setWidth(gridSize * levelWidth.field.num());
                handleNewLevelBounds();
            }
        });

        levelHeight = new LabeledIntField(" Height: ", defaultSize, 1);
        levelHeight.field.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                canvas.setHeight(gridSize * levelHeight.field.num());
                handleNewLevelBounds();
            }
        });

        levelNum = new LabeledIntField(" Level: ", 0);
        levelNum.field.setEditable(false);
        saveButton = new Button("Save");
        saveButton.setDisable(Editor.DEFAULT_AUTO_SAVE_SETTING);
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveLevel();
            }
        });
        autoSave = new CheckBox("Auto Save");
        autoSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AUTO_SAVE_ON = autoSave.isSelected();
                saveButton.setDisable(AUTO_SAVE_ON);
            }
        });
        autoSave.setSelected(Editor.DEFAULT_AUTO_SAVE_SETTING);

        prevButton = new Button("<=");
        prevButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(levelNum.field.num() > 0){
                    levelNum.field.set(levelNum.field.num() - 1);
                    loadLevel();
                }
            }
        });

        nextButton = new Button("=>");
        nextButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(levelNum.field.num() + 1 <= Assets.finalLevel){
                    levelNum.field.set(levelNum.field.num() + 1);
                    loadLevel();
                }
            }
        });

        newButton = new Button("New");
        newButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newLevel(defaultLevel);
            }
        });

        duplicateButton = new Button("Duplicate");
        duplicateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newLevel(Gdx.files.local(Assets.levelPath + "/" + levelNum.field.getText()).readString());
            }
        });

        deleteButton = new Button("Delete");
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(levelNum.field.num() > 3) {
                    SyncPacket.sendDeleteSync(Assets.levelPath + "/" + levelNum.field.getText());
                    FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.LEVEL, false, true));

                    if (levelNum.field.num() == Assets.finalLevel)
                        Gdx.files.local(Assets.levelPath + "/" + levelNum.field.getText()).delete();
                    for (int i = levelNum.field.num() + 1; i <= Assets.finalLevel; i++) {
                        Gdx.files.local(Assets.levelPath + "/" + i).moveTo(Gdx.files.local(Assets.levelPath + "/" + (i - 1)));
                    }
                    update();
                }
            }
        });

        targetLevelNum = new IntField(Assets.finalLevel, 0);
        targetLevelNum.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(Integer.parseInt(newValue) > Assets.finalLevel){
                    targetLevelNum.set(Assets.finalLevel);
                }
            }
        });
        swapButton = new Button("Swap");
        swapButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SyncPacket.sendSwapSync(Assets.levelPath + "/" + targetLevelNum.getText(), Assets.levelPath + "/" + levelNum.field.getText());
                FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.LEVEL, false, true));

                Editor.swapFiles(Assets.levelPath + "/" + targetLevelNum.getText(), Assets.levelPath + "/" + levelNum.field.getText());
                loadLevel();
            }
        });

        loadInEditorButton = new Button("Load in Editor");
        loadInEditorButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                levelNum.field.set(targetLevelNum.num());
                loadLevel();
            }
        });

        loadInGameButton = new Button("Load in Game");
        loadInGameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(Main.state == Main.State.GAME && Networking.state != Networking.State.CLIENT){
                    GameState.setToTransition(targetLevelNum.num(), 0, true);
                }
            }
        });

        levelHBox = new HBox();
        levelHBox.getChildren().addAll(tileEditSize, tileSize, levelWidth, levelHeight, prevButton, levelNum, nextButton, newButton, duplicateButton, deleteButton, saveButton, autoSave, targetLevelNum, swapButton, loadInEditorButton, loadInGameButton);
        levelHBox.setAlignment(Pos.CENTER);

        hBoxPane = new BorderPane();
        hBoxPane.setLeft(playerHBox);
        hBoxPane.setRight(levelHBox);
    }

    private static void initListPane(){
        tileList = new ListView();
        tileList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selectedIndex = tileList.getSelectionModel().getSelectedIndex();
                isTileSelected = true;
            }
        });

        structureList = new ListView();
        structureList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selectedIndex = structureList.getSelectionModel().getSelectedIndex();
                isTileSelected = false;
            }
        });

        listPane = new BorderPane();
        listPane.setCenter(tileList);
        listPane.setBottom(structureList);

        updateListPane();
    }

    public static void updateListPane(){
        String[] tileArray = new String[Tile.tiles.length];
        tileImages = new Image[Tile.tiles.length];
        for(int i = 0; i < Tile.tiles.length; i++){
            tileArray[i] = Tile.tiles[i].name;
            tileImages[i] = Editor.getImageFromFileHandle(Gdx.files.local(Assets.tilePath + "/" + tileArray[i].toLowerCase() + ".png"));
        }
        tileList.setItems(FXCollections.observableArrayList(tileArray));
        if(tileList.getSelectionModel().getSelectedIndex() == -1 || tileList.getSelectionModel().getSelectedIndex() >= Tile.tiles.length) {
            tileList.getSelectionModel().select(tileArray.length - 1);
            if(isTileSelected)
                selectedIndex = tileArray.length - 1;
        }

        String[] structureArray = new String[Structure.structures.length];
        structureImages = new Image[Structure.structures.length];
        for(int i = 0; i < Structure.structures.length; i++){
            structureArray[i] = Structure.structures[i].name;
            structureImages[i] = Editor.getImageFromFileHandle(Gdx.files.local("structures/" + structureArray[i].toLowerCase() + ".png"));
        }
        structureList.setItems(FXCollections.observableArrayList(structureArray));
        if(structureList.getSelectionModel().getSelectedIndex() == -1 || structureList.getSelectionModel().getSelectedIndex() >= Structure.structures.length) {
            structureList.getSelectionModel().select(structureArray.length - 1);
            if(!isTileSelected)
                selectedIndex = structureArray.length - 1;
        }
    }

    private static void initSpawnerBox() {
        spawnerCheckBox = new CheckBox("Editing Spawners");
        spawnerCheckBox.setSelected(false);
        spawnerCheckBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                selectedSpawnerIndex = -1;
                updateSpawnBox();
            }
        });
        selectedSpawnerPosLabel = new Label(" No spawner is selected. ");
        spawnerImage = Editor.getImageFromFileHandle(Gdx.files.local(Assets.enemyPath + "/spawner.png"));
        addWaveButton = new Button("New Wave");
        addWaveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(selectedSpawnerIndex > -1) {
                    spawners.get(selectedSpawnerIndex).waves.add(new Wave(new Array<Spawn>()));
                    updateSpawnBox();
                }
            }
        });
        waveVBox = new VBox();
        spawnerVBox = new VBox();
        spawnerVBox.getChildren().addAll(spawnerCheckBox, selectedSpawnerPosLabel, addWaveButton, waveVBox);
        spawnerScrollPane = new ScrollPane();
        spawnerScrollPane.setContent(spawnerVBox);
    }

    private static void handleNewLevelBounds(){
        Rectangle rectangle = new Rectangle(0, 0, levelNum.field.num(), levelHeight.field.num());
        for(int i = spawners.size - 1; i >= 0; i--)
            if(!rectangle.contains(spawners.get(i).spawnerRenderable.x, spawners.get(i).spawnerRenderable.y))
                spawners.removeIndex(i);
        for(int i = structures.size - 1; i >= 0; i--)
            if(!rectangle.contains(structures.get(i).x, structures.get(i).y))
                structures.removeIndex(i);
        for(int i = tiles.size - 1; i >= 0; i--)
            if(!rectangle.contains(tiles.get(i).x, tiles.get(i).y))
                tiles.removeIndex(i);
        autoSave();
    }

    public static void initCanvasLoop(){
        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (UP_PRESSED)
                            scrollPane.setVvalue(levelEditorScrollValue(scrollPane.getVvalue(), -1));
                        if (LEFT_PRESSED)
                            scrollPane.setHvalue(levelEditorScrollValue(scrollPane.getHvalue(), -1));
                        if (DOWN_PRESSED)
                            scrollPane.setVvalue(levelEditorScrollValue(scrollPane.getVvalue(), 1));
                        if (RIGHT_PRESSED)
                            scrollPane.setHvalue(levelEditorScrollValue(scrollPane.getHvalue(), 1));

                        canvas.getGraphicsContext2D().setFill(Color.BLACK);
                        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        for(Vector3 tile : tiles){
                            if((int)tile.z != -1 && (int)tile.z < tileImages.length && tileImages[(int)tile.z] != null && Tile.texture((int)tile.z) != null)
                                canvas.getGraphicsContext2D().drawImage(tileImages[(int)tile.z],0, Tile.texture((int)tile.z).getHeight() / Tile.tiles[(int)tile.z].frameNum * Tile.tiles[(int)tile.z].currentFrame, Tile.texture((int)tile.z).getWidth(), Tile.texture((int)tile.z).getHeight() / Tile.tiles[(int)tile.z].frameNum, tile.x * gridSize, tile.y * gridSize, gridSize, gridSize);
                        }
                        for(Vector3 structure : structures){
                            if(structureImages[(int)structure.z] != null && Structure.texture((int)structure.z) != null)
                                canvas.getGraphicsContext2D().drawImage(structureImages[(int)structure.z],0, Structure.texture((int)structure.z).getHeight() / Structure.structures[(int)structure.z].frameNum * Structure.structures[(int)structure.z].currentFrame, Structure.texture((int)structure.z).getWidth(), Structure.texture((int)structure.z).getHeight() / Structure.structures[(int)structure.z].frameNum, structure.x * gridSize, structure.y * gridSize, gridSize, gridSize);
                        }
                        canvas.getGraphicsContext2D().setFill(Color.ORANGE);
                        for(Spawner spawner : spawners){
                            canvas.getGraphicsContext2D().drawImage(spawnerImage, spawner.spawnerRenderable.x * gridSize, spawner.spawnerRenderable.y * gridSize, gridSize, gridSize);
                            canvas.getGraphicsContext2D().fillText(spawner.getText(), spawner.spawnerRenderable.x * gridSize, spawner.spawnerRenderable.y * gridSize + gridSize, gridSize);
                        }
                    }
                });
            }
        }.start();
    }

    private static boolean exists(int x, int y, boolean isTile){
        if(spawnerCheckBox.isSelected()){
            for (int i = 0; i < spawners.size; i++)
                if (spawners.get(i).spawnerRenderable.x == x && spawners.get(i).spawnerRenderable.y == y)
                    return true;
            for (int i = 0; i < structures.size; i++)
                if (structures.get(i).x == x && structures.get(i).y == y)
                    return true;
        }
        else if(isTile) {
            for (int i = 0; i < tiles.size; i++)
                if (tiles.get(i).x == x && tiles.get(i).y == y)
                    return true;
        }
        else {
            for (int i = 0; i < structures.size; i++)
                if (structures.get(i).x == x && structures.get(i).y == y)
                    return true;
        }
        return false;
    }

    private static int indexOf(int x, int y, boolean isTile){
        if(spawnerCheckBox.isSelected()){
            for (int i = 0; i < spawners.size; i++)
                if (spawners.get(i).spawnerRenderable.x == x && spawners.get(i).spawnerRenderable.y == y)
                    return i;
        }
        else if(isTile) {
            for (int i = 0; i < tiles.size; i++)
                if (tiles.get(i).x == x && tiles.get(i).y == y)
                    return i;
        } else {
            for (int i = 0; i < structures.size; i++)
                if (structures.get(i).x == x && structures.get(i).y == y)
                    return i;
        }
        return -1;
    }

    private static int typeOf(int x, int y, boolean isTile){
        if(isTile) {
            for (int i = 0; i < tiles.size; i++)
                if (tiles.get(i).x == x && tiles.get(i).y == y)
                    return (int) tiles.get(i).z;
        }else {
            for (int i = 0; i < structures.size; i++)
                if (structures.get(i).x == x && structures.get(i).y == y)
                    return (int) structures.get(i).z;
        }
        return -1;
    }

    private static void add(Vector3 v, boolean isTile) {
        if (spawnerCheckBox.isSelected()) {
            spawners.add(new Spawner((int) v.x, (int) v.y, new Array<Wave>()));
            selectedSpawnerIndex = spawners.size - 1;
        }
        else if (isTile) {
            for (int x = 0; x < tileEditSize.field.num(); x++)
                for (int y = 0; y < tileEditSize.field.num(); y++)
                    if (!exists((int) v.x + x, (int) v.y + y, true))
                        tiles.add(new Vector3(v.x + x, v.y + y, v.z));
        } else if(indexOf((int)v.x, (int)v.y, isTile) == -1)
            structures.add(v);
        autoSave();
    }

    private static void remove(Vector2 v, boolean isTile){
        int i = indexOf((int)v.x, (int)v.y, isTile);
        if(spawnerCheckBox.isSelected() && i >= 0) {
            spawners.removeIndex(i);
            selectedSpawnerIndex = -1;
            updateSpawnBox();
        }
        else if(isTile) {
            for(int x = 0; x < tileEditSize.field.num(); x++) {
                for (int y = 0; y < tileEditSize.field.num(); y++){
                    i = indexOf((int)v.x + x, (int)v.y + y, true);
                    if(i != -1)
                        tiles.removeIndex(i);
                }
            }
        }
        else if(i >= 0)
            structures.removeIndex(i);
        autoSave();
    }

    private static void saveLevel(){
        Editor.SAFE_TO_SAVE = false;
        String save = "";
        for(int y = 0; y < levelHeight.field.num(); y++){
            for(int x = 0; x < levelWidth.field.num(); x++){
                save += typeOf(x, y, true);
                if(x < levelWidth.field.num() - 1)
                    save += ",";
            }
            if(y < levelHeight.field.num() - 1)
                save += ";\n";
        }
        save += ".";
        save += "\n\n";

        for(int i = 0; i < structures.size; i++){
            save += (int)structures.get(i).z + ", " + ((int)structures.get(i).x) + ", " + ((int)structures.get(i).y);
            if(i < structures.size - 1)
                save += ", \n";
        }
        save += ".\n\n";

        for(int i = 0; i < spawners.size; i++){
            save += spawners.get(i).spawnerRenderable.x + ", " + spawners.get(i).spawnerRenderable.y;
            for(int w = 0; w < spawners.get(i).waves.size; w++){
                save += "w";
                for(int s = 0; s < spawners.get(i).waves.get(w).spawns.size; s++){
                    save += spawners.get(i).waves.get(w).spawns.get(s).frameWait + ", " + spawners.get(i).waves.get(w).spawns.get(s).enemyType + ", " + spawners.get(i).waves.get(w).spawns.get(s).enemyNum;
                    if(s < spawners.get(i).waves.get(w).spawns.size - 1)
                        save += ", ";
                }
            }
            if(i < spawners.size - 1)
                save += ";\n";
        }
        save += ".\n\n";

        save += lives.field.getText() + ".\n\n" + gold.field.getText() + ".\n\n" + sharedGold.isSelected() + ".\n\n" + cameraZoom.field.getText() + ".";

        Gdx.files.local(Assets.levelPath + "/" + levelNum.field.getText()).writeString(save, false);
        FileSender.sendSyncFile(Assets.levelPath + "/" + levelNum.field.getText());
        FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.LEVEL, false, false));
        Editor.SAFE_TO_SAVE = true;
    }

    public static void loadLevel(){
        Editor.SAFE_TO_SAVE = false;
        tiles.clear();
        structures.clear();
        spawners.clear();
        String[] parts = Gdx.files.local(Assets.levelPath + "/" + levelNum.field.getText()).readString().replaceAll("\\s+","").split("\\.");
        levelHeight.field.set(parts[0].split(";").length );
        levelWidth.field.set(parts[0].split(";")[0].split(",").length );
        canvas.setWidth(gridSize * levelWidth.field.num());
        canvas.setHeight(gridSize * levelHeight.field.num());
        if(!parts[0].equals("")) {
            String[] tilePart = parts[0].replace(";", ",").split(",");
            for (int y = 0; y < tilePart.length / levelHeight.field.num(); y++)
                for (int x = 0; x < levelWidth.field.num(); x++) {
                    int tileType = Integer.parseInt(tilePart[y * levelHeight.field.num() + x]);
                    if(tileType > -1) {
                        if (tileType < Tile.tiles.length)
                            tiles.add(new Vector3(x, y, tileType));
                        else
                            tiles.add(new Vector3(x, y, Tile.tiles.length - 1));
                    }
                }
        }
        if(!parts[1].equals("")) {
            String[] structurePart = parts[1].split(",");
            for (int i = 0; i < structurePart.length / 3; i++) {
                int structureType = Integer.parseInt(structurePart[i * 3]);
                if (structureType < Structure.structures.length)
                    structures.add(new Vector3(Integer.parseInt(structurePart[i * 3 + 1]), Integer.parseInt(structurePart[i * 3 + 2]), structureType));
                else
                    structures.add(new Vector3(Integer.parseInt(structurePart[i * 3 + 1]), Integer.parseInt(structurePart[i * 3 + 2]), Structure.structures.length - 1));
            }
        }
        if(!parts[2].equals("")) {
            String[] spawnerPart = parts[2].split(";");
            for (int i = 0; i < spawnerPart.length; i++) {
                String[] wavePart = spawnerPart[i].split("w");
                spawners.add(new Spawner(Integer.parseInt(wavePart[0].split(",")[0]), Integer.parseInt(wavePart[0].split(",")[1]), new Array<Wave>()));
                for (int w = 1; w < wavePart.length; w++) {
                    spawners.get(i).waves.add(new Wave(new Array<Spawn>()));
                    String[] spawnPart = wavePart[w].split(",");
                    for (int s = 0; s < spawnPart.length / 3; s++) {
                        int enemyType = Integer.parseInt(spawnPart[s * 3 + 1]);
                        if(enemyType < Enemy.enemies.length)
                            spawners.get(i).waves.get(w - 1).spawns.add(new Spawn(Integer.parseInt(spawnPart[s * 3]), enemyType, Integer.parseInt(spawnPart[s * 3 + 2])));
                        else
                            spawners.get(i).waves.get(w - 1).spawns.add(new Spawn(Integer.parseInt(spawnPart[s * 3]), Enemy.enemies.length - 1, Integer.parseInt(spawnPart[s * 3 + 2])));
                    }
                }
            }
        }
        lives.field.setText(parts[3]);
        gold.field.setText(parts[4]);
        sharedGold.setSelected(Boolean.parseBoolean(parts[5]));
        cameraZoom.field.setText(parts[6]);
        Editor.SAFE_TO_SAVE = true;
    }

    private static void newLevel(String text){
        Assets.finalLevel++;
        Gdx.files.local(Assets.levelPath + "/" + Assets.finalLevel).writeString(text, false);
        levelNum.field.set(Assets.finalLevel);
        FileSender.sendSyncFile(Assets.levelPath + "/" + Assets.finalLevel);
        FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.LEVEL, false, true));
        loadLevel();
    }

    public static void update(){
        Assets.setFinalLevel();
        levelNum.field.set(Assets.finalLevel);
        if(targetLevelNum.num() > Assets.finalLevel)
            targetLevelNum.set(Assets.finalLevel);
        loadLevel();
    }

    public static void autoSave(){
        if(AUTO_SAVE_ON && Editor.SAFE_TO_SAVE)
            saveLevel();
    }

    public static void updateAllLevels(){
        int lvl_num = levelNum.field.num();
        for(int i = 0; i <= Assets.finalLevel; i++){
            levelNum.field.set(i);
            loadLevel();
            saveLevel();
        }
        levelNum.field.set(lvl_num);
        loadLevel();
    }
}