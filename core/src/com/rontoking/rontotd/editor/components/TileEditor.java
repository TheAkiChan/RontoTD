package com.rontoking.rontotd.editor.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.editor.controls.LabeledFloatField;
import com.rontoking.rontotd.editor.controls.LabeledIntField;
import com.rontoking.rontotd.editor.controls.LabeledTextField;
import com.rontoking.rontotd.game.entities.Ability;
import com.rontoking.rontotd.game.entities.Tile;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.networking.FileSender;
import com.rontoking.rontotd.game.systems.networking.packets.EditorPacket;
import com.rontoking.rontotd.game.systems.networking.packets.SyncPacket;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class TileEditor {
    public static Tab tab;
    public static BorderPane pane;

    private static boolean AUTO_SAVE_ON = Editor.DEFAULT_AUTO_SAVE_SETTING;

    private static HBox box;
    private static VBox mainBox, abilityBox;
    public static ListView listView, abilityListView;
    private static Label spriteLabel, abilityLabel;
    private static LabeledTextField name;
    private static CheckBox canHaveTower, autoSave, canHaveEnemy, isWater;
    private static Button spriteButton, saveButton, duplicateButton, deleteButton, removeAbilityButton, addAbilityButton;
    private static LabeledIntField frameNum, frameTime;

    private static FileChooser fileChooser;
    private static ImageView sprite;
    private static File spriteFile;

    private static ComboBox abilityType, abilityStat, abilityTarget, abilityMath;
    private static LabeledFloatField abilityValue1, abilityValue2;

    public static void initPane() {
        initListView();
        initBox();

        pane = new BorderPane();
        pane.setLeft(listView);
        pane.setCenter(box);
    }

    private static void initListView(){
        listView = new ListView();
        refreshListView();
        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                refreshBox(true);
            }
        });
    }

    private static void refreshListView(){
        int selIndex = listView.getSelectionModel().getSelectedIndex();
        String[] tileNames = new String[Tile.tiles.length];
        for(int i = 0; i < tileNames.length; i++)
            tileNames[i] = Tile.tiles[i].name;
        listView.setItems(FXCollections.observableArrayList(tileNames));
        if(selIndex < 0 || selIndex >= tileNames.length)
            listView.getSelectionModel().select(tileNames.length - 1);
        else
            listView.getSelectionModel().select(selIndex);
    }

    private static void refreshBox(boolean refreshAssets){
        Editor.SAFE_TO_SAVE = false;
        name.field.setText(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name);
        name.field.positionCaret(name.field.getLength());
        canHaveTower.setSelected(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].canHaveTower);
        canHaveEnemy.setSelected(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].canHaveEnemy);
        isWater.setSelected(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].isWater);
        frameNum.field.set(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].frameNum);
        frameTime.field.set(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].frameTime);

        int selIndex = abilityListView.getSelectionModel().getSelectedIndex();
        String[] abilityNames = new String[selectedTile().abilities.length];
        for (int i = 0; i < abilityNames.length; i++)
            abilityNames[i] = selectedTile().abilities[i].name();
        abilityListView.setItems(FXCollections.observableArrayList(abilityNames));
        if (selIndex < 0 || selIndex >= abilityNames.length)
            abilityListView.getSelectionModel().select(abilityNames.length - 1);
        else
            abilityListView.getSelectionModel().select(selIndex);

        refreshAbilityBox();

        if(refreshAssets) {
            spriteFile = new File(Gdx.files.local(Assets.tilePath + "/" + name.field.getText().toLowerCase() + ".png").path());
            sprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(spriteFile.getPath())));
        }
        Editor.SAFE_TO_SAVE = true;
    }

    private static Tile selectedTile(){
        return Tile.tiles[listView.getSelectionModel().getSelectedIndex()];
    }

    private static void initBox(){
        spriteLabel = new Label(" Tile Texture: ");
        abilityLabel = new Label("Abilities:");

        name = new LabeledTextField(" Name: ", Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name);
        name.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TILE, true));
        canHaveTower = new CheckBox("Is Buildable On");
        canHaveTower.setSelected(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].canHaveTower);
        canHaveTower.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TILE));
        canHaveEnemy = new CheckBox("Is Pathable");
        canHaveEnemy.setSelected(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].canHaveEnemy);
        canHaveEnemy.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TILE));
        isWater = new CheckBox("Is Water");
        isWater.setSelected(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].isWater);
        isWater.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TILE));
        frameNum = new LabeledIntField(" Number of Frames: ", Tile.tiles[listView.getSelectionModel().getSelectedIndex()].frameNum, 1);
        frameNum.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TILE, false));
        frameTime = new LabeledIntField(" Frame Duration: ", Tile.tiles[listView.getSelectionModel().getSelectedIndex()].frameTime, 0);
        frameTime.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TILE, false));

        sprite = new ImageView(Editor.getImageFromFileHandle(Gdx.files.local(Assets.tilePath + "/" + name.field.getText().toLowerCase() + ".png")));
        sprite.setPreserveRatio(true);
        sprite.setFitHeight(128);

        spriteFile = new File(Gdx.files.local(Assets.tilePath + "/" + name.field.getText().toLowerCase() + ".png").path());

        fileChooser = new FileChooser();
        spriteButton = new Button("Choose Texture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        spriteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.setTitle("Choose Texture");
                fileChooser.setInitialDirectory(new File(Gdx.files.getLocalStoragePath() + Assets.tilePath));
                spriteFile = fileChooser.showOpenDialog(Editor.primaryStage);
                if(spriteFile != null)
                    sprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(spriteFile.getPath())));
                autoSave(true);
            }
        });
        duplicateButton = new Button("Duplicate");
        duplicateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!Gdx.files.local(Assets.tilePath + "/" + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name.toLowerCase() + " copy.png").exists()) {
                    Editor.copyFile(Gdx.files.local(Assets.tilePath + "/" + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name.toLowerCase() + " copy.png"), Gdx.files.local(Assets.tilePath + "/" + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name.toLowerCase() + ".png"));
                    Gdx.files.local(Assets.tilePath + "/info").writeString(Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name + " Copy, " + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].canHaveTower + ", " + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].frameNum + ", " + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].frameTime + ", " + abilitiesToString(false) + ", " + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].canHaveEnemy + ", " + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].isWater + ";\n", true);

                    FileSender.sendSyncFile(Assets.tilePath + "/" + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name.toLowerCase() + " copy.png");
                    FileSender.sendSyncFile(Assets.tilePath + "/info");
                    FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.TILE, false, true));

                    update(true, false, true);
                }
            }
        });
        saveButton = new Button("Save Changes");
        saveButton.setDisable(Editor.DEFAULT_AUTO_SAVE_SETTING);
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                save(true);
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
        deleteButton = new Button("Delete");
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(Tile.tiles.length > 5) { // Can't delete base tiles.
                    SyncPacket.sendDeleteSync(Assets.tilePath + "/" + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name.toLowerCase() + ".png");

                    Gdx.files.local(Assets.tilePath + "/" + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name.toLowerCase() + ".png").delete();

                    Array<String> tileArray = new Array<String>(Gdx.files.local(Assets.tilePath + "/info").readString().replace("\n", "").split(";"));
                    tileArray.removeIndex(listView.getSelectionModel().getSelectedIndex());
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < tileArray.size; i++) {
                        builder.append(tileArray.get(i));
                        builder.append(";\n");
                    }
                    Gdx.files.local(Assets.tilePath + "/info").writeString(builder.toString(), false);

                    FileSender.sendSyncFile(Assets.tilePath + "/info");
                    FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.TILE, true, true));

                    update(false, true, true);
                }
            }
        });

        abilityType = new ComboBox(Editor.getObservableListFromEnum(Ability.Type.values(), Ability.TILE_START, Ability.TILE_END));
        abilityType.getSelectionModel().select(0);
        abilityType.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TILE, false));
        abilityStat = new ComboBox(Editor.getObservableListFromEnum(Ability.EnemyStat.values()));
        abilityStat.getSelectionModel().select(0);
        abilityStat.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TILE, false));
        abilityTarget = new ComboBox(Editor.getObservableListFromEnum(Ability.Target.values()));
        abilityTarget.getSelectionModel().select(0);
        abilityTarget.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TILE, false));
        abilityValue1 = new LabeledFloatField(" Value 1: ", 0);
        abilityValue1.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TILE, false));
        abilityValue2 = new LabeledFloatField(" Value 2: ", 0);
        abilityValue2.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TILE, false));
        abilityMath = new ComboBox(Editor.getObservableListFromEnum(Ability.Math.values()));
        abilityMath.getSelectionModel().select(0);
        abilityMath.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TILE, false));

        String[] abilityNames = new String[selectedTile().abilities.length];
        for(int i = 0; i < abilityNames.length; i++){
            abilityNames[i] = selectedTile().abilities[i].name();
        }
        abilityListView = new ListView(FXCollections.observableArrayList(abilityNames));
        if(abilityListView.getItems().size() > 0)
            abilityListView.getSelectionModel().select(0);
        abilityListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                refreshAbilityBox();
            }
        });

        removeAbilityButton = new Button("Remove Ability");
        removeAbilityButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(abilityListView.getSelectionModel().getSelectedIndex() >= 0) {
                    Ability[] newAbilities = new Ability[selectedTile().abilities.length - 1];
                    for(int i = 0; i < newAbilities.length; i++){
                        if(i < abilityListView.getSelectionModel().getSelectedIndex())
                            newAbilities[i] = new Ability(selectedTile().abilities[i]);
                        else if(i >= abilityListView.getSelectionModel().getSelectedIndex())
                            newAbilities[i] =  new Ability(selectedTile().abilities[i + 1]);
                    }
                    selectedTile().abilities = newAbilities;
                    abilityListView.getSelectionModel().clearSelection();
                    save(false);
                }
            }
        });
        addAbilityButton = new Button("Add Ability");
        addAbilityButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Ability[] newAbilities = new Ability[selectedTile().abilities.length + 1];
                for(int i = 0; i < newAbilities.length - 1; i++){
                    newAbilities[i] = new Ability(selectedTile().abilities[i]);
                }
                newAbilities[newAbilities.length - 1] = new Ability();
                newAbilities[newAbilities.length - 1].type = Ability.Type.values()[abilityType.getSelectionModel().getSelectedIndex() + Ability.TILE_START];
                newAbilities[newAbilities.length - 1].stat = 0;
                newAbilities[newAbilities.length - 1].target = Ability.Target.Both_Equal;
                newAbilities[newAbilities.length - 1].value1 = 0;
                newAbilities[newAbilities.length - 1].value2 = 0;
                newAbilities[newAbilities.length - 1].math = Ability.Math.Set;
                selectedTile().abilities = newAbilities;
                abilityListView.getItems().add(selectedTile().abilities[selectedTile().abilities.length - 1].name());
                abilityListView.getSelectionModel().select(selectedTile().abilities.length - 1);
                save(false);
            }
        });

        mainBox = new VBox();
        mainBox.setAlignment(Pos.CENTER);
        mainBox.getChildren().addAll(spriteLabel, sprite, spriteButton, frameNum, frameTime, name, canHaveTower, canHaveEnemy, isWater, duplicateButton, saveButton, autoSave, deleteButton);

        abilityBox = new VBox();
        abilityBox.setAlignment(Pos.CENTER);
        abilityBox.getChildren().addAll(abilityLabel, abilityListView, removeAbilityButton, addAbilityButton, abilityType, abilityStat, abilityTarget, abilityMath, abilityValue1, abilityValue2);

        box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(mainBox, abilityBox);

        refreshAbilityBox();
    }

    private static void refreshAbilityBox(){
        Editor.SAFE_TO_SAVE = false;
        if(selectedTile().abilities.length > 0) {
            abilityType.getSelectionModel().select(selectedAbility().type.ordinal() - Ability.TILE_START);
            switch (selectedAbility().type) {
                case Multi_Shot:
                    setAbilityNodeVisibility(abilityStat, false);
                    setAbilityNodeVisibility(abilityTarget, false);
                    abilityValue1.field.set(selectedAbility().value1);
                    abilityValue1.label.setText(" Max Missiles: ");
                    setAbilityNodeVisibility(abilityValue1, true);
                    setAbilityNodeVisibility(abilityValue2, false);
                    setAbilityNodeVisibility(abilityMath, false);
                    break;
                case Hit_Effect:
                    abilityStat.setItems(Editor.getObservableListFromEnum(Ability.EnemyStat.values()));
                    abilityStat.getSelectionModel().select(selectedAbility().stat);
                    setAbilityNodeVisibility(abilityStat, true);
                    abilityTarget.getSelectionModel().select(selectedAbility().target.ordinal());
                    setAbilityNodeVisibility(abilityTarget, false);
                    abilityValue1.field.set(selectedAbility().value1);
                    abilityValue1.label.setText(" Power: ");
                    setAbilityNodeVisibility(abilityValue1, true);
                    abilityValue2.field.set(selectedAbility().value2);
                    abilityValue2.label.setText(" Duration: ");
                    setAbilityNodeVisibility(abilityValue2, false);
                    abilityMath.getSelectionModel().select(selectedAbility().math.ordinal());
                    setAbilityNodeVisibility(abilityMath, true);
                    break;
                case Enemy_Aura:
                    abilityStat.setItems(Editor.getObservableListFromEnum(Ability.EnemyStat.values()));
                    abilityStat.getSelectionModel().select(selectedAbility().stat);
                    setAbilityNodeVisibility(abilityStat, true);
                    abilityTarget.getSelectionModel().select(selectedAbility().target.ordinal());
                    setAbilityNodeVisibility(abilityTarget, false);
                    abilityValue1.field.set(selectedAbility().value1);
                    abilityValue1.label.setText(" Power: ");
                    setAbilityNodeVisibility(abilityValue1, true);
                    abilityValue2.field.set(selectedAbility().value2);
                    abilityValue2.label.setText(" Range: ");
                    setAbilityNodeVisibility(abilityValue2, false);
                    abilityMath.getSelectionModel().select(selectedAbility().math.ordinal());
                    setAbilityNodeVisibility(abilityMath, true);
                    break;
                case Tower_Aura:
                    abilityStat.setItems(Editor.getObservableListFromEnum(Ability.TowerStat.values()));
                    abilityStat.getSelectionModel().select(selectedAbility().stat);
                    setAbilityNodeVisibility(abilityStat, true);
                    abilityTarget.getSelectionModel().select(selectedAbility().target.ordinal());
                    setAbilityNodeVisibility(abilityTarget, false);
                    abilityValue1.field.set(selectedAbility().value1);
                    abilityValue1.label.setText(" Power: ");
                    setAbilityNodeVisibility(abilityValue1, true);
                    abilityValue2.field.set(selectedAbility().value2);
                    abilityValue2.label.setText(" Range: ");
                    setAbilityNodeVisibility(abilityValue2, false);
                    abilityMath.getSelectionModel().select(selectedAbility().math.ordinal());
                    setAbilityNodeVisibility(abilityMath, true);
                    break;
                default:
                    break;
            }
        }else{
            setAbilityNodeVisibility(abilityStat, false);
            setAbilityNodeVisibility(abilityTarget, false);
            setAbilityNodeVisibility(abilityValue1, false);
            setAbilityNodeVisibility(abilityValue2, false);
            setAbilityNodeVisibility(abilityMath, false);
        }
        abilityValue1.field.positionCaret(abilityValue1.field.getLength());
        abilityValue2.field.positionCaret(abilityValue2.field.getLength());
        Editor.SAFE_TO_SAVE = true;
    }

    private static void setAbilityNodeVisibility(Node node, boolean isVisible){
        if(isVisible && !abilityBox.getChildren().contains(node)){
            abilityBox.getChildren().add(node);
        }else if(!isVisible && abilityBox.getChildren().contains(node)){
            abilityBox.getChildren().remove(node);
        }
    }

    private static Ability selectedAbility(){
        return selectedTile().abilities[abilityListView.getSelectionModel().getSelectedIndex()];
    }

    private static String abilityToString(int abilityIndex, boolean setSelected){
        if(setSelected && abilityIndex == abilityListView.getSelectionModel().getSelectedIndex())
            return ":" + (abilityType.getSelectionModel().getSelectedIndex() + Ability.TILE_START) + "/" + abilityStat.getSelectionModel().getSelectedIndex() + "/" + abilityValue1.field.num() + "/" + abilityValue2.field.num() + "/" + abilityMath.getSelectionModel().getSelectedIndex() + "/" + abilityTarget.getSelectionModel().getSelectedIndex();
        return ":" + selectedTile().abilities[abilityIndex].type.ordinal() + "/" + selectedTile().abilities[abilityIndex].stat + "/" + selectedTile().abilities[abilityIndex].value1 + "/" + selectedTile().abilities[abilityIndex].value2 + "/" + selectedTile().abilities[abilityIndex].math.ordinal() + "/" + selectedTile().abilities[abilityIndex].target.ordinal();
    }

    private static String abilitiesToString(boolean setSelected){
        StringBuilder abilityBuilder = new StringBuilder();
        if(selectedTile().abilities.length > 0){
            abilityBuilder.append(abilityToString(0, setSelected).substring(1));
            for(int i = 1; i < selectedTile().abilities.length; i++){
                abilityBuilder.append(abilityToString(i, setSelected));
            }
        }else{
            abilityBuilder.append("-1");
        }
        return abilityBuilder.toString();
    }

    public static void update(final boolean selectLast, final boolean updateLevels, final boolean updateAssets){
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Tile.load();
                if(updateAssets) {
                    Assets.loadTiles();
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshListView();
                        if(selectLast)
                            listView.getSelectionModel().select(Tile.tiles.length - 1);
                        refreshBox(updateAssets);
                        Editor.SAFE_TO_SAVE = false;
                        LevelEditor.updateListPane();
                        if(updateLevels)
                            LevelEditor.updateAllLevels();
                        Editor.SAFE_TO_SAVE = true;
                    }
                });
            }
        });
    }

    private static void save(boolean saveAssets){
        name.field.setText(name.field.getText().trim());
        if((Tile.indexOf(name.field.getText()) == -1 || Tile.indexOf(name.field.getText()) == listView.getSelectionModel().getSelectedIndex()) && Editor.SAFE_TO_SAVE) {
            Editor.SAFE_TO_SAVE = false;
            if(saveAssets) {
                Editor.replaceImage(Assets.tilePath, spriteFile, name.field.getText(), Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name);
            }

            String[] tileArray = Gdx.files.local(Assets.tilePath + "/info").readString().replace("\n", "").split(";");
            String tileStr = name.field.getText() + ", " + canHaveTower.isSelected() + ", " + frameNum.field.getText() + ", " + frameTime.field.getText() + ", " + abilitiesToString(true) + ", " + canHaveEnemy.isSelected() + ", " + isWater.isSelected();
            tileArray[listView.getSelectionModel().getSelectedIndex()] = tileStr;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < tileArray.length; i++) {
                builder.append(tileArray[i]);
                builder.append(";\n");
            }
            Gdx.files.local(Assets.tilePath + "/info").writeString(builder.toString(), false);

            if(saveAssets) {
                SyncPacket.sendDeleteSync(Assets.tilePath + "/" + Tile.tiles[listView.getSelectionModel().getSelectedIndex()].name.toLowerCase() + ".png");

                FileSender.sendSyncFile(Assets.tilePath + "/" + name.field.getText().toLowerCase() + ".png");
            }
            FileSender.sendSyncFile(Assets.tilePath + "/info");
            FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.TILE, false, saveAssets));
            update(false, false, saveAssets);
        }
    }

    public static void autoSave(){
        if(AUTO_SAVE_ON && Editor.SAFE_TO_SAVE)
            save(false);
    }

    public static void autoSave(boolean saveAssets){
        if(AUTO_SAVE_ON && Editor.SAFE_TO_SAVE)
            save(saveAssets);
    }
}
