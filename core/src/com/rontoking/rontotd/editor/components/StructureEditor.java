package com.rontoking.rontotd.editor.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.editor.controls.LabeledFloatField;
import com.rontoking.rontotd.editor.controls.LabeledIntField;
import com.rontoking.rontotd.editor.controls.LabeledTextField;
import com.rontoking.rontotd.game.entities.Ability;
import com.rontoking.rontotd.game.entities.Structure;
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

public class StructureEditor {
    public static Tab tab;
    public static BorderPane pane;

    private static boolean AUTO_SAVE_ON = Editor.DEFAULT_AUTO_SAVE_SETTING;

    private static HBox box;
    private static VBox mainBox, abilityBox;
    public static ListView listView, abilityListView;
    private static Label spriteLabel, abilityLabel;
    private static LabeledTextField name;
    private static CheckBox isGoal, autoSave, isBuildableOn, isPathable;
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

    private static void initListView() {
        listView = new ListView();
        refreshListView();
        listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                refreshBox(true);
            }
        });
    }

    private static void refreshListView() {
        int selIndex = listView.getSelectionModel().getSelectedIndex();
        String[] structureNames = new String[Structure.structures.length];
        for (int i = 0; i < structureNames.length; i++)
            structureNames[i] = Structure.structures[i].name;
        listView.setItems(FXCollections.observableArrayList(structureNames));
        if(selIndex < 0 || selIndex >= structureNames.length)
            listView.getSelectionModel().select(structureNames.length - 1);
        else
            listView.getSelectionModel().select(selIndex);
    }

    private static void refreshBox(boolean refreshAssets) {
        Editor.SAFE_TO_SAVE = false;
        name.field.setText(selectedStructure().name);
        name.field.positionCaret(name.field.getLength());
        isGoal.setSelected(selectedStructure().isGoal);
        isBuildableOn.setSelected(selectedStructure().isBuildableOn);
        isPathable.setSelected(selectedStructure().isPathable);
        frameNum.field.set(selectedStructure().frameNum);
        frameTime.field.set(selectedStructure().frameTime);

        int selIndex = abilityListView.getSelectionModel().getSelectedIndex();
        String[] abilityNames = new String[selectedStructure().abilities.length];
        for (int i = 0; i < abilityNames.length; i++)
            abilityNames[i] = selectedStructure().abilities[i].name();
        abilityListView.setItems(FXCollections.observableArrayList(abilityNames));
        if (selIndex < 0 || selIndex >= abilityNames.length)
            abilityListView.getSelectionModel().select(abilityNames.length - 1);
        else
            abilityListView.getSelectionModel().select(selIndex);

        refreshAbilityBox();

        if(refreshAssets) {
            spriteFile = new File(Gdx.files.local(Assets.structurePath + "/" + name.field.getText().toLowerCase() + ".png").path());
            sprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(spriteFile.getPath())));
        }
        Editor.SAFE_TO_SAVE = true;
    }

    private static Structure selectedStructure(){
        return Structure.structures[listView.getSelectionModel().getSelectedIndex()];
    }


    private static void initBox() {
        spriteLabel = new Label(" Structure Texture: ");
        abilityLabel = new Label("Abilities:");

        name = new LabeledTextField(" Name: ", selectedStructure().name);
        name.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.STRUCTURE, true));
        isGoal = new CheckBox("Is Goal");
        isGoal.setSelected(selectedStructure().isGoal);
        isGoal.setOnAction(Editor.autoSaveEventHandler(Editor.Type.STRUCTURE));
        isBuildableOn = new CheckBox("Is Buildable On");
        isBuildableOn.setSelected(selectedStructure().isBuildableOn);
        isBuildableOn.setOnAction(Editor.autoSaveEventHandler(Editor.Type.STRUCTURE));
        isPathable = new CheckBox("Is Pathable");
        isPathable.setSelected(selectedStructure().isPathable);
        isPathable.setOnAction(Editor.autoSaveEventHandler(Editor.Type.STRUCTURE));
        frameNum = new LabeledIntField(" Number of Frames: ", selectedStructure().frameNum, 1);
        frameNum.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.STRUCTURE, false));
        frameTime = new LabeledIntField(" Frame Duration: ", selectedStructure().frameTime, 0);
        frameTime.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.STRUCTURE, false));

        sprite = new ImageView(Editor.getImageFromFileHandle(Gdx.files.local(Assets.structurePath + "/" + name.field.getText().toLowerCase() + ".png")));
        sprite.setPreserveRatio(true);
        sprite.setFitHeight(128);

        spriteFile = new File(Gdx.files.local(Assets.structurePath + "/" + name.field.getText().toLowerCase() + ".png").path());

        fileChooser = new FileChooser();
        spriteButton = new Button("Choose Texture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        spriteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.setTitle("Choose Texture");
                fileChooser.setInitialDirectory(new File(Gdx.files.getLocalStoragePath() + Assets.structurePath));
                spriteFile = fileChooser.showOpenDialog(Editor.primaryStage);
                if (spriteFile != null)
                    sprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(spriteFile.getPath())));
                autoSave(true);
            }
        });
        duplicateButton = new Button("Duplicate");
        duplicateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!Gdx.files.local(Assets.structurePath + "/" + selectedStructure().name.toLowerCase() + " copy.png").exists()) {
                    Editor.copyFile(Gdx.files.local(Assets.structurePath + "/" + selectedStructure().name.toLowerCase() + " copy.png"), Gdx.files.local(Assets.structurePath + "/" + selectedStructure().name.toLowerCase() + ".png"));
                    Gdx.files.local(Assets.structurePath + "/info").writeString(selectedStructure().name + " Copy, " + selectedStructure().isGoal + ", " + selectedStructure().frameNum + ", " + selectedStructure().frameTime + ", " + abilitiesToString(false) + ", " + selectedStructure().isBuildableOn + ", " + selectedStructure().isPathable + ";\n", true);

                    FileSender.sendSyncFile(Assets.structurePath + "/" + selectedStructure().name.toLowerCase() + " copy.png");
                    FileSender.sendSyncFile(Assets.structurePath + "/info");
                    FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.STRUCTURE, false, true));

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
                if (Structure.structures.length > 5) { // Can't delete base structures.
                    SyncPacket.sendDeleteSync(Assets.structurePath + "/" + selectedStructure().name.toLowerCase() + ".png");

                    Gdx.files.local(Assets.structurePath + "/" + selectedStructure().name.toLowerCase() + ".png").delete();

                    Array<String> structureArray = new Array<String>(Gdx.files.local(Assets.structurePath + "/info").readString().replace("\n", "").split(";"));
                    structureArray.removeIndex(listView.getSelectionModel().getSelectedIndex());
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < structureArray.size; i++) {
                        builder.append(structureArray.get(i));
                        builder.append(";\n");
                    }
                    Gdx.files.local(Assets.structurePath + "/info").writeString(builder.toString(), false);

                    FileSender.sendSyncFile(Assets.structurePath + "/info");
                    FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.STRUCTURE, true, true));

                    update(false, true, true);
                }
            }
        });

        abilityType = new ComboBox(Editor.getObservableListFromEnum(Ability.Type.values(), Ability.STRUCTURE_START, Ability.STRUCTURE_END));
        abilityType.getSelectionModel().select(0);
        abilityType.setOnAction(Editor.autoSaveEventHandler(Editor.Type.STRUCTURE, false));
        abilityStat = new ComboBox(Editor.getObservableListFromEnum(Ability.EnemyStat.values()));
        abilityStat.getSelectionModel().select(0);
        abilityStat.setOnAction(Editor.autoSaveEventHandler(Editor.Type.STRUCTURE, false));
        abilityTarget = new ComboBox(Editor.getObservableListFromEnum(Ability.Target.values()));
        abilityTarget.getSelectionModel().select(0);
        abilityTarget.setOnAction(Editor.autoSaveEventHandler(Editor.Type.STRUCTURE, false));
        abilityValue1 = new LabeledFloatField(" Value 1: ", 0);
        abilityValue1.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.STRUCTURE, false));
        abilityValue2 = new LabeledFloatField(" Value 2: ", 0);
        abilityValue2.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.STRUCTURE, false));
        abilityMath = new ComboBox(Editor.getObservableListFromEnum(Ability.Math.values()));
        abilityMath.getSelectionModel().select(0);
        abilityMath.setOnAction(Editor.autoSaveEventHandler(Editor.Type.STRUCTURE, false));

        String[] abilityNames = new String[selectedStructure().abilities.length];
        for(int i = 0; i < abilityNames.length; i++){
            abilityNames[i] = selectedStructure().abilities[i].name();
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
                    Ability[] newAbilities = new Ability[selectedStructure().abilities.length - 1];
                    for(int i = 0; i < newAbilities.length; i++){
                        if(i < abilityListView.getSelectionModel().getSelectedIndex())
                            newAbilities[i] = new Ability(selectedStructure().abilities[i]);
                        else if(i >= abilityListView.getSelectionModel().getSelectedIndex())
                            newAbilities[i] =  new Ability(selectedStructure().abilities[i + 1]);
                    }
                    selectedStructure().abilities = newAbilities;
                    abilityListView.getSelectionModel().clearSelection();
                    save(false);
                }
            }
        });
        addAbilityButton = new Button("Add Ability");
        addAbilityButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Ability[] newAbilities = new Ability[selectedStructure().abilities.length + 1];
                for(int i = 0; i < newAbilities.length - 1; i++){
                    newAbilities[i] = new Ability(selectedStructure().abilities[i]);
                }
                newAbilities[newAbilities.length - 1] = new Ability();
                newAbilities[newAbilities.length - 1].type = Ability.Type.values()[abilityType.getSelectionModel().getSelectedIndex() + Ability.STRUCTURE_START];
                newAbilities[newAbilities.length - 1].stat = 0;
                newAbilities[newAbilities.length - 1].target = Ability.Target.Both_Equal;
                newAbilities[newAbilities.length - 1].value1 = 0;
                newAbilities[newAbilities.length - 1].value2 = 0;
                newAbilities[newAbilities.length - 1].math = Ability.Math.Set;
                selectedStructure().abilities = newAbilities;
                abilityListView.getItems().add(selectedStructure().abilities[selectedStructure().abilities.length - 1].name());
                abilityListView.getSelectionModel().select(selectedStructure().abilities.length - 1);
                save(false);
            }
        });
        
        mainBox = new VBox();
        mainBox.setAlignment(Pos.CENTER);
        mainBox.getChildren().addAll(spriteLabel, sprite, spriteButton, frameNum, frameTime, name, isGoal, isBuildableOn, isPathable, duplicateButton, saveButton, autoSave, deleteButton);

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
        if(selectedStructure().abilities.length > 0) {
            abilityType.getSelectionModel().select(selectedAbility().type.ordinal() - Ability.STRUCTURE_START);
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
                    setAbilityNodeVisibility(abilityTarget, true);
                    abilityValue1.field.set(selectedAbility().value1);
                    abilityValue1.label.setText(" Power: ");
                    setAbilityNodeVisibility(abilityValue1, true);
                    abilityValue2.field.set(selectedAbility().value2);
                    abilityValue2.label.setText(" Duration: ");
                    setAbilityNodeVisibility(abilityValue2, true);
                    abilityMath.getSelectionModel().select(selectedAbility().math.ordinal());
                    setAbilityNodeVisibility(abilityMath, true);
                    break;
                case Enemy_Aura:
                    abilityStat.setItems(Editor.getObservableListFromEnum(Ability.EnemyStat.values()));
                    abilityStat.getSelectionModel().select(selectedAbility().stat);
                    setAbilityNodeVisibility(abilityStat, true);
                    abilityTarget.getSelectionModel().select(selectedAbility().target.ordinal());
                    setAbilityNodeVisibility(abilityTarget, true);
                    abilityValue1.field.set(selectedAbility().value1);
                    abilityValue1.label.setText(" Power: ");
                    setAbilityNodeVisibility(abilityValue1, true);
                    abilityValue2.field.set(selectedAbility().value2);
                    abilityValue2.label.setText(" Range: ");
                    setAbilityNodeVisibility(abilityValue2, true);
                    abilityMath.getSelectionModel().select(selectedAbility().math.ordinal());
                    setAbilityNodeVisibility(abilityMath, true);
                    break;
                case Tower_Aura:
                    abilityStat.setItems(Editor.getObservableListFromEnum(Ability.TowerStat.values()));
                    abilityStat.getSelectionModel().select(selectedAbility().stat);
                    setAbilityNodeVisibility(abilityStat, true);
                    abilityTarget.getSelectionModel().select(selectedAbility().target.ordinal());
                    setAbilityNodeVisibility(abilityTarget, true);
                    abilityValue1.field.set(selectedAbility().value1);
                    abilityValue1.label.setText(" Power: ");
                    setAbilityNodeVisibility(abilityValue1, true);
                    abilityValue2.field.set(selectedAbility().value2);
                    abilityValue2.label.setText(" Range: ");
                    setAbilityNodeVisibility(abilityValue2, true);
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
        return selectedStructure().abilities[abilityListView.getSelectionModel().getSelectedIndex()];
    }

    private static String abilityToString(int abilityIndex, boolean setSelected){
        if(setSelected && abilityIndex == abilityListView.getSelectionModel().getSelectedIndex())
            return ":" + (abilityType.getSelectionModel().getSelectedIndex() + Ability.STRUCTURE_START) + "/" + abilityStat.getSelectionModel().getSelectedIndex() + "/" + abilityValue1.field.num() + "/" + abilityValue2.field.num() + "/" + abilityMath.getSelectionModel().getSelectedIndex() + "/" + abilityTarget.getSelectionModel().getSelectedIndex();
        return ":" + selectedStructure().abilities[abilityIndex].type.ordinal() + "/" + selectedStructure().abilities[abilityIndex].stat + "/" + selectedStructure().abilities[abilityIndex].value1 + "/" + selectedStructure().abilities[abilityIndex].value2 + "/" + selectedStructure().abilities[abilityIndex].math.ordinal() + "/" + selectedStructure().abilities[abilityIndex].target.ordinal();
    }

    private static String abilitiesToString(boolean setSelected){
        StringBuilder abilityBuilder = new StringBuilder();
        if(selectedStructure().abilities.length > 0){
            abilityBuilder.append(abilityToString(0, setSelected).substring(1));
            for(int i = 1; i < selectedStructure().abilities.length; i++){
                abilityBuilder.append(abilityToString(i, setSelected));
            }
        }else{
            abilityBuilder.append("-1");
        }
        return abilityBuilder.toString();
    }
    
    public static void update(final boolean selectLast, final boolean updateLevels, final boolean updateAssets) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Structure.load();
                if(updateAssets) {
                    Assets.loadStructures();
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshListView();
                        if (selectLast)
                            listView.getSelectionModel().select(Structure.structures.length - 1);
                        refreshBox(updateAssets);
                        Editor.SAFE_TO_SAVE = false;
                        LevelEditor.updateListPane();
                        if (updateLevels)
                            LevelEditor.updateAllLevels();
                        Editor.SAFE_TO_SAVE = true;
                    }
                });
            }
        });
    }

    private static void save(boolean saveAssets) {
        name.field.setText(name.field.getText().trim());
        if ((Structure.indexOf(name.field.getText()) == -1 || Structure.indexOf(name.field.getText()) == listView.getSelectionModel().getSelectedIndex()) && Editor.SAFE_TO_SAVE) {
            Editor.SAFE_TO_SAVE = false;
            if(saveAssets) {
                Editor.replaceImage(Assets.structurePath, spriteFile, name.field.getText(), selectedStructure().name);
            }

            String[] structureArray = Gdx.files.local(Assets.structurePath + "/info").readString().replace("\n", "").split(";");
            String structureStr = name.field.getText() + ", " + isGoal.isSelected() + ", " + frameNum.field.getText() + ", " + frameTime.field.getText() + ", " + abilitiesToString(true) + ", " + isBuildableOn.isSelected() + ", " + isPathable.isSelected();
            structureArray[listView.getSelectionModel().getSelectedIndex()] = structureStr;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < structureArray.length; i++) {
                builder.append(structureArray[i]);
                builder.append(";\n");
            }
            Gdx.files.local(Assets.structurePath + "/info").writeString(builder.toString(), false);

            if(saveAssets) {
                SyncPacket.sendDeleteSync(Assets.structurePath + "/" + Structure.structures[listView.getSelectionModel().getSelectedIndex()].name.toLowerCase() + ".png");

                FileSender.sendSyncFile(Assets.structurePath + "/" + name.field.getText().toLowerCase() + ".png");
            }
            FileSender.sendSyncFile(Assets.structurePath + "/info");
            FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.STRUCTURE, false, saveAssets));
            update(false, false, saveAssets);
        }
    }

    public static void autoSave(boolean saveAssets){
        if(AUTO_SAVE_ON && Editor.SAFE_TO_SAVE)
            save(saveAssets);
    }

}
