package com.rontoking.rontotd.editor.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.editor.controls.LabeledFloatField;
import com.rontoking.rontotd.editor.controls.LabeledIntField;
import com.rontoking.rontotd.editor.controls.LabeledTextField;
import com.rontoking.rontotd.game.entities.Ability;
import com.rontoking.rontotd.game.entities.AttackType;
import com.rontoking.rontotd.game.entities.Enemy;
import com.rontoking.rontotd.game.entities.EnemyRenderable;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.Level;
import com.rontoking.rontotd.game.systems.networking.FileSender;
import com.rontoking.rontotd.game.systems.networking.Networking;
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

public class EnemyEditor {
    public static Tab tab;
    public static BorderPane pane;

    private static boolean AUTO_SAVE_ON = Editor.DEFAULT_AUTO_SAVE_SETTING;

    private static HBox box;
    private static VBox enemyBox, armorBox, abilityBox;
    public static ListView listView, armorTypeListView, abilityListView;
    private static Label spriteLabel, armorTypeLabel, abilityLabel;
    private static LabeledTextField name, armorTypeName;
    private static Button spriteButton, deathButton, playDeathButton, saveButton, duplicateButton, deleteButton, removeArmorType, addArmorType, removeAbilityButton, addAbilityButton;
    private static LabeledFloatField hp, speed, abilityValue1, abilityValue2;
    private static LabeledIntField gold, frameNum, frameTime, damage;
    private static FileChooser fileChooser;
    private static ImageView sprite;
    private static File spriteFile, deathFile;
    private static ComboBox transportType, armorType, abilityType, abilityStat, abilityTarget, abilityMath;
    private static CheckBox autoSave;

    public static void initPane(){
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
        String[] enemyNames = new String[Enemy.enemies.length];
        for(int i = 0; i < enemyNames.length; i++)
            enemyNames[i] = Enemy.enemies[i].renderable.name;
        listView.setItems(FXCollections.observableArrayList(enemyNames));
        if(selIndex < 0 || selIndex >= enemyNames.length)
            listView.getSelectionModel().select(enemyNames.length - 1);
        else
            listView.getSelectionModel().select(selIndex);
        LevelEditor.updateSpawnBox();
    }

    private static void refreshBox(boolean refreshAssets){
        Editor.SAFE_TO_SAVE = false;
        name.field.setText(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name);
        name.field.positionCaret(name.field.getLength());
        hp.field.set(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.health);
        hp.field.positionCaret(hp.field.getLength());
        speed.field.set(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.moveSpeed);
        speed.field.positionCaret(speed.field.getLength());
        gold.field.set(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.gold);
        gold.field.positionCaret(gold.field.getLength());
        transportType.getSelectionModel().select(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.transportType.ordinal());

        int selIndex = armorTypeListView.getSelectionModel().getSelectedIndex();
        armorTypeListView.setItems(FXCollections.observableArrayList(Enemy.armorTypes));
        if (selIndex < 0 || selIndex >= Enemy.armorTypes.length)
            armorTypeListView.getSelectionModel().select(Enemy.armorTypes.length - 1);
        else
            armorTypeListView.getSelectionModel().select(selIndex);

        armorType.setItems(FXCollections.observableArrayList(Enemy.armorTypes));
        armorType.getSelectionModel().select(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.armorType);
        armorTypeName.field.setText(Enemy.armorTypes[armorTypeListView.getSelectionModel().getSelectedIndex()]);
        armorTypeName.field.positionCaret(armorTypeName.field.getLength());
        TowerEditor.save(false, true);

        frameNum.field.set(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.frameNum);
        frameNum.field.positionCaret(frameNum.field.getLength());
        frameTime.field.set(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.frameTime);
        frameTime.field.positionCaret(frameTime.field.getLength());
        damage.field.set(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.damage);
        damage.field.positionCaret(damage.field.getLength());

        selIndex = abilityListView.getSelectionModel().getSelectedIndex();
        String[] abilityNames = new String[selectedEnemy().abilities.length];
        for (int i = 0; i < abilityNames.length; i++)
            abilityNames[i] = selectedEnemy().abilities[i].name();
        abilityListView.setItems(FXCollections.observableArrayList(abilityNames));
        if (selIndex < 0 || selIndex >= abilityNames.length)
            abilityListView.getSelectionModel().select(abilityNames.length - 1);
        else
            abilityListView.getSelectionModel().select(selIndex);

        refreshAbilityBox();

        if(refreshAssets) {
            spriteFile = new File(Gdx.files.local(Assets.enemyPath + "/" + name.field.getText().toLowerCase() + ".png").path());
            sprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(spriteFile.getPath())));

            deathFile = new File(Gdx.files.local(Assets.enemyPath + "/" + name.field.getText().toLowerCase() + ".ogg").path());
        }
        Editor.SAFE_TO_SAVE = true;
    }

    private static Enemy selectedEnemy(){
        return Enemy.enemies[listView.getSelectionModel().getSelectedIndex()];
    }

    private static void initBox(){
        spriteLabel = new Label(" Enemy Texture: ");
        armorTypeLabel = new Label("Armor Types:");
        abilityLabel = new Label("Abilities:");

        name = new LabeledTextField(" Name: ", Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name);
        name.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY, true));
        hp = new LabeledFloatField(" Health: ", Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.health, 0);
        hp.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY));
        speed = new LabeledFloatField(" Movement Speed: ", Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.moveSpeed);
        speed.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY));
        gold = new LabeledIntField(" Gold Rewarded: ", Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.gold);
        gold.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY));
        frameNum = new LabeledIntField(" Number of Frames: ", Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.frameNum, 1);
        frameNum.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY));
        frameTime = new LabeledIntField(" Frame Duration: ", Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.frameTime, 0);
        frameTime.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY));
        damage = new LabeledIntField(" Damage: ", Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.damage, 0);
        damage.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY));

        transportType = new ComboBox();
        transportType.setItems(FXCollections.observableArrayList(EnemyRenderable.TransportType.values()));
        transportType.getSelectionModel().select(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.transportType.ordinal());
        transportType.setOnAction(Editor.autoSaveEventHandler(Editor.Type.ENEMY));

        armorTypeListView = new ListView(FXCollections.observableArrayList(Enemy.armorTypes));
        armorTypeListView.getSelectionModel().select(0);
        armorTypeListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                armorTypeName.field.setText(Enemy.armorTypes[armorTypeListView.getSelectionModel().getSelectedIndex()]);
            }
        });

        armorType = new ComboBox(FXCollections.observableArrayList(Enemy.armorTypes));
        armorType.setVisibleRowCount(2);
        armorType.getSelectionModel().select(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.armorType);
        armorType.setOnAction(Editor.autoSaveEventHandler(Editor.Type.ENEMY, false));

        removeArmorType = new Button("Remove Armor Type");
        removeArmorType.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(armorTypeListView.getSelectionModel().getSelectedIndex() >= 2) {
                    String[] newArmorTypes = new String[Enemy.armorTypes.length - 1];
                    for(int i = 0; i < newArmorTypes.length; i++){
                        if(i < armorTypeListView.getSelectionModel().getSelectedIndex())
                            newArmorTypes[i] = Enemy.armorTypes[i];
                        else if(i >= armorTypeListView.getSelectionModel().getSelectedIndex())
                            newArmorTypes[i] = Enemy.armorTypes[i + 1];
                    }
                    Enemy.armorTypes = newArmorTypes;

                    for(int i = 0; i < AttackType.attackTypes.length; i++){
                        float[] newMultipliers = new float[Enemy.armorTypes.length];
                        for(int m = 0; m < Enemy.armorTypes.length; m++){
                            if(m < armorTypeListView.getSelectionModel().getSelectedIndex())
                                newMultipliers[m] = AttackType.attackTypes[i].multipliers[m];
                            else if(m >= armorTypeListView.getSelectionModel().getSelectedIndex())
                                newMultipliers[m] = AttackType.attackTypes[i].multipliers[m + 1];
                        }
                        AttackType.attackTypes[i].multipliers = newMultipliers;
                    }

                    save(false);
                }
            }
        });
        armorTypeName = new LabeledTextField(" Armor Type Name: ", Enemy.armorTypes[armorType.getSelectionModel().getSelectedIndex()]);
        armorTypeName.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY, false));
        addArmorType = new Button("Add Armor Type");
        addArmorType.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String[] newArmorTypes = new String[Enemy.armorTypes.length + 1];
                for(int i = 0; i < Enemy.armorTypes.length; i++){
                    newArmorTypes[i] = Enemy.armorTypes[i];
                }
                newArmorTypes[newArmorTypes.length - 1] = armorTypeName.field.getText() + " Copy";
                armorTypeListView.getSelectionModel().clearSelection();
                Enemy.armorTypes = newArmorTypes;

                for(int i = 0; i < AttackType.attackTypes.length; i++){
                    float[] newMultipliers = new float[Enemy.armorTypes.length];
                    for(int m = 0; m < AttackType.attackTypes[i].multipliers.length; m++){
                        newMultipliers[m] = AttackType.attackTypes[i].multipliers[m];
                    }
                    newMultipliers[newMultipliers.length - 1] = 1;
                    AttackType.attackTypes[i].multipliers = newMultipliers;
                }

                save(false, true);
            }
        });

        sprite = new ImageView(Editor.getImageFromFileHandle((Gdx.files.local(Assets.enemyPath + "/" + name.field.getText().toLowerCase() + ".png"))));
        sprite.setPreserveRatio(true);
        sprite.setFitHeight(128);

        spriteFile = new File(Gdx.files.local(Assets.enemyPath + "/" + name.field.getText().toLowerCase() + ".png").path());
        deathFile = new File(Gdx.files.local(Assets.enemyPath + "/" + name.field.getText().toLowerCase() + ".ogg").path());

        fileChooser = new FileChooser();
        spriteButton = new Button("Choose Texture");
        spriteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.getExtensionFilters().clear();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
                fileChooser.setTitle("Choose Texture");
                fileChooser.setInitialDirectory(new File(Gdx.files.getLocalStoragePath() + Assets.enemyPath));
                spriteFile = fileChooser.showOpenDialog(Editor.primaryStage);
                if(spriteFile != null) {
                    sprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(spriteFile.getPath())));
                    autoSave(true);
                }
            }
        });
        deathButton = new Button("Choose Sound");
        deathButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.getExtensionFilters().clear();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("OGG", "*.ogg"));
                fileChooser.setTitle("Choose Sound");
                fileChooser.setInitialDirectory(new File(Gdx.files.getLocalStoragePath() + Assets.enemyPath));
                deathFile = fileChooser.showOpenDialog(com.rontoking.rontotd.editor.Editor.primaryStage);
                autoSave(true);
            }
        });
        playDeathButton = new Button("Play Sound");
        playDeathButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(deathFile != null && deathFile.exists())
                    Gdx.audio.newSound(new FileHandle(deathFile)).play();
                else
                    Assets.enemySounds[listView.getSelectionModel().getSelectedIndex()].play();
            }
        });
        duplicateButton = new Button("Duplicate");
        duplicateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!Gdx.files.local(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + " copy.png").exists()) {
                    Editor.copyFile(Gdx.files.local(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + " copy.png"), Gdx.files.local(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + ".png"));
                    Editor.copyFile(Gdx.files.local(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + " copy.ogg"), Gdx.files.local(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + ".ogg"));
                    Gdx.files.local(Assets.enemyPath + "/info").writeString(Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name + " Copy, " + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.health + ", " + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.moveSpeed + ", " + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.gold + ", " + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.transportType.ordinal() + ", " + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.armorType + ", " + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.frameNum + ", " + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.frameTime + ", " + abilitiesToString(false) + ", " + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.damage + ";\n", true);

                    FileSender.sendSyncFile(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + " copy.png");
                    FileSender.sendSyncFile(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + " copy.ogg");
                    FileSender.sendSyncFile(Assets.enemyPath + "/info");
                    FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.ENEMY, false, true));

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
                if(Enemy.enemies.length > 2) {
                    SyncPacket.sendDeleteSync(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + ".png");
                    SyncPacket.sendDeleteSync(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + ".ogg");

                    Gdx.files.local(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + ".png").delete();
                    Gdx.files.local(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + ".ogg").delete();

                    String[] enemyParts = Gdx.files.local(Assets.enemyPath + "/info").readString().replace("\n", "").split("\\.", 2);
                    Array<String> enemyArray = new Array<String>(enemyParts[1].split(";"));
                    enemyArray.removeIndex(listView.getSelectionModel().getSelectedIndex());
                    StringBuilder builder = new StringBuilder();
                    builder.append(enemyParts[0]);
                    builder.append(".\n");
                    for (int i = 0; i < enemyArray.size; i++) {
                        builder.append(enemyArray.get(i));
                        builder.append(";\n");
                    }
                    Gdx.files.local(Assets.enemyPath + "/info").writeString(builder.toString(), false);

                    FileSender.sendSyncFile(Assets.enemyPath + "/info");
                    FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.ENEMY, true, true));

                    update(false, true, true);
                }
            }
        });

        abilityType = new ComboBox(Editor.getObservableListFromEnum(Ability.Type.values(), Ability.ENEMY_START));
        abilityType.getSelectionModel().select(0);
        abilityType.setOnAction(Editor.autoSaveEventHandler(Editor.Type.ENEMY, false));
        abilityStat = new ComboBox(Editor.getObservableListFromEnum(Ability.EnemyStat.values()));
        abilityStat.getSelectionModel().select(0);
        abilityStat.setOnAction(Editor.autoSaveEventHandler(Editor.Type.ENEMY, false));
        abilityTarget = new ComboBox(Editor.getObservableListFromEnum(Ability.Target.values()));
        abilityTarget.getSelectionModel().select(0);
        abilityTarget.setOnAction(Editor.autoSaveEventHandler(Editor.Type.ENEMY, false));
        abilityValue1 = new LabeledFloatField(" Value 1: ", 0);
        abilityValue1.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY, false));
        abilityValue2 = new LabeledFloatField(" Value 2: ", 0);
        abilityValue2.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.ENEMY, false));
        abilityMath = new ComboBox(Editor.getObservableListFromEnum(Ability.Math.values()));
        abilityMath.getSelectionModel().select(0);
        abilityMath.setOnAction(Editor.autoSaveEventHandler(Editor.Type.ENEMY, false));

        String[] abilityNames = new String[selectedEnemy().abilities.length];
        for(int i = 0; i < abilityNames.length; i++){
            abilityNames[i] = selectedEnemy().abilities[i].name();
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
                    Ability[] newAbilities = new Ability[selectedEnemy().abilities.length - 1];
                    for(int i = 0; i < newAbilities.length; i++){
                        if(i < abilityListView.getSelectionModel().getSelectedIndex())
                            newAbilities[i] = new Ability(selectedEnemy().abilities[i]);
                        else if(i >= abilityListView.getSelectionModel().getSelectedIndex())
                            newAbilities[i] =  new Ability(selectedEnemy().abilities[i + 1]);
                    }
                    selectedEnemy().abilities = newAbilities;
                    abilityListView.getSelectionModel().clearSelection();
                    save(false);
                }
            }
        });
        addAbilityButton = new Button("Add Ability");
        addAbilityButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Ability[] newAbilities = new Ability[selectedEnemy().abilities.length + 1];
                for(int i = 0; i < newAbilities.length - 1; i++){
                    newAbilities[i] = new Ability(selectedEnemy().abilities[i]);
                }
                newAbilities[newAbilities.length - 1] = new Ability();
                newAbilities[newAbilities.length - 1].type = Ability.Type.values()[abilityType.getSelectionModel().getSelectedIndex() + Ability.ENEMY_START];
                newAbilities[newAbilities.length - 1].stat = 0;
                newAbilities[newAbilities.length - 1].target = Ability.Target.Both_Equal;
                newAbilities[newAbilities.length - 1].value1 = 0;
                newAbilities[newAbilities.length - 1].value2 = 0;
                newAbilities[newAbilities.length - 1].math = Ability.Math.Set;
                selectedEnemy().abilities = newAbilities;
                abilityListView.getItems().add(selectedEnemy().abilities[selectedEnemy().abilities.length - 1].name());
                abilityListView.getSelectionModel().select(selectedEnemy().abilities.length - 1);
                save(false);
            }
        });

        enemyBox = new VBox();
        enemyBox.setAlignment(Pos.CENTER);
        enemyBox.getChildren().addAll(spriteLabel, sprite, spriteButton, frameNum, frameTime, deathButton, playDeathButton, name, hp, speed, damage, gold, transportType, armorType, duplicateButton, saveButton, autoSave, deleteButton);

        armorBox = new VBox();
        armorBox.setAlignment(Pos.CENTER);
        armorBox.getChildren().addAll(armorTypeLabel, armorTypeListView, removeArmorType, armorTypeName, addArmorType);

        abilityBox = new VBox();
        abilityBox.setAlignment(Pos.CENTER);
        abilityBox.getChildren().addAll(abilityLabel, abilityListView, removeAbilityButton, addAbilityButton, abilityType, abilityStat, abilityTarget, abilityMath, abilityValue1, abilityValue2);

        box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(enemyBox, armorBox, abilityBox);

        refreshAbilityBox();
    }

    private static void refreshAbilityBox() {
        Editor.SAFE_TO_SAVE = false;
        if (selectedEnemy().abilities.length > 0) {
            abilityType.getSelectionModel().select(selectedAbility().type.ordinal() - Ability.ENEMY_START);
            switch (selectedAbility().type) {
                case Spawn:
                    setAbilityNodeVisibility(abilityStat, false);
                    setAbilityNodeVisibility(abilityTarget, false);
                    abilityValue1.field.set(selectedAbility().value1);
                    abilityValue2.field.set(selectedAbility().value2);
                    abilityValue1.label.setText(" Enemy Type: ");
                    abilityValue2.label.setText(" Amount Spawned: ");
                    setAbilityNodeVisibility(abilityValue1, true);
                    setAbilityNodeVisibility(abilityValue2, true);
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
        } else {
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
        return selectedEnemy().abilities[abilityListView.getSelectionModel().getSelectedIndex()];
    }

    private static String abilityToString(int abilityIndex, boolean setSelected){
        if(setSelected && abilityIndex == abilityListView.getSelectionModel().getSelectedIndex())
            return ":" + (abilityType.getSelectionModel().getSelectedIndex() + Ability.ENEMY_START) + "/" + abilityStat.getSelectionModel().getSelectedIndex() + "/" + abilityValue1.field.num() + "/" + abilityValue2.field.num() + "/" + abilityMath.getSelectionModel().getSelectedIndex() + "/" + abilityTarget.getSelectionModel().getSelectedIndex();
        return ":" + selectedEnemy().abilities[abilityIndex].type.ordinal() + "/" + selectedEnemy().abilities[abilityIndex].stat + "/" + selectedEnemy().abilities[abilityIndex].value1 + "/" + selectedEnemy().abilities[abilityIndex].value2 + "/" + selectedEnemy().abilities[abilityIndex].math.ordinal() + "/" + selectedEnemy().abilities[abilityIndex].target.ordinal();
    }

    private static String abilitiesToString(boolean setSelected){
        StringBuilder abilityBuilder = new StringBuilder();
        if(selectedEnemy().abilities.length > 0){
            abilityBuilder.append(abilityToString(0, setSelected).substring(1));
            for(int i = 1; i < selectedEnemy().abilities.length; i++){
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
                Enemy.load();
                if(updateAssets) {
                    Assets.loadEnemies();
                }
                if(Networking.state == Networking.State.SERVER || Networking.state == Networking.State.CLIENT){
                    Networking.transition(Level.num, true);
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshListView();
                        if(selectLast)
                            listView.getSelectionModel().select(Enemy.enemies.length - 1);
                        refreshBox(updateAssets);
                        if(selectLast)
                            armorTypeListView.getSelectionModel().select(armorTypeListView.getItems().size() - 1);
                        Editor.SAFE_TO_SAVE = false;
                        if(updateLevels)
                            LevelEditor.updateAllLevels();
                        LevelEditor.updateSpawnBox();
                        Editor.SAFE_TO_SAVE = true;
                    }
                });
            }
        });
    }

    private static void save(boolean saveAssets){
        save(saveAssets, false);
    }

    private static void save(boolean saveAssets, boolean selectLast){
        name.field.setText(name.field.getText().trim());
        if((Enemy.indexOf(name.field.getText()) == -1 || Enemy.indexOf(name.field.getText()) == listView.getSelectionModel().getSelectedIndex()) && Editor.SAFE_TO_SAVE) {
            Editor.SAFE_TO_SAVE = false;
            if(saveAssets) {
                Editor.replaceImage(Assets.enemyPath, spriteFile, name.field.getText(), Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name);
                Editor.replaceSound(Assets.enemyPath, deathFile, name.field.getText(), Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name);
            }
            String[] enemyParts = Gdx.files.local(Assets.enemyPath + "/info").readString().replace("\n", "").split("\\.", 2);
            String[] enemyArray = enemyParts[1].split(";");

            String enemyStr = name.field.getText() + ", " + hp.field.getText() + ", " + speed.field.getText() + ", " + gold.field.getText() + ", " + transportType.getSelectionModel().getSelectedIndex() + ", " + armorType.getSelectionModel().getSelectedIndex() + ", " + frameNum.field.getText() + ", " + frameTime.field.getText() + ", " + abilitiesToString(true) + ", " + damage.field.getText();
            enemyArray[listView.getSelectionModel().getSelectedIndex()] = enemyStr;
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < Enemy.armorTypes.length; i++){
                if(i == armorTypeListView.getSelectionModel().getSelectedIndex())
                    builder.append(armorTypeName.field.getText());
                else
                    builder.append(Enemy.armorTypes[i]);
                if(i < Enemy.armorTypes.length - 1)
                    builder.append(", ");
            }
            builder.append(".\n");
            for (int i = 0; i < enemyArray.length; i++) {
                builder.append(enemyArray[i]);
                builder.append(";\n");
            }
            Gdx.files.local(Assets.enemyPath + "/info").writeString(builder.toString(), false);

            if(saveAssets) {
                SyncPacket.sendDeleteSync(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + ".png");
                SyncPacket.sendDeleteSync(Assets.enemyPath + "/" + Enemy.enemies[listView.getSelectionModel().getSelectedIndex()].renderable.name.toLowerCase() + ".ogg");

                FileSender.sendSyncFile(Assets.enemyPath + "/" + name.field.getText().toLowerCase() + ".png");
                FileSender.sendSyncFile(Assets.enemyPath + "/" + name.field.getText().toLowerCase() + ".ogg");
            }
            FileSender.sendSyncFile(Assets.enemyPath + "/info");
            FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.ENEMY, false, saveAssets));

            update(selectLast, false, saveAssets);
        }
    }

    public static void autoSave(boolean saveAssets){
        if(AUTO_SAVE_ON && Editor.SAFE_TO_SAVE)
            save(saveAssets);
    }
}
