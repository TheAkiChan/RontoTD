package com.rontoking.rontotd.editor.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.editor.controls.EmptySelectionModel;
import com.rontoking.rontotd.editor.controls.LabeledFloatField;
import com.rontoking.rontotd.editor.controls.LabeledIntField;
import com.rontoking.rontotd.editor.controls.LabeledTextField;
import com.rontoking.rontotd.game.entities.*;
import com.rontoking.rontotd.game.systems.Assets;
import com.rontoking.rontotd.game.systems.networking.FileSender;
import com.rontoking.rontotd.game.systems.networking.packets.SyncPacket;
import com.rontoking.rontotd.game.systems.networking.packets.EditorPacket;
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

public class TowerEditor {
    public static Tab tab;
    public static BorderPane pane;

    private static boolean AUTO_SAVE_ON = Editor.DEFAULT_AUTO_SAVE_SETTING;

    private static HBox box;
    private static VBox towerBox, missileBox, attackTypeBox, abilityBox, upgradeBox;
    private static ListView listView, attackTypeListView, armorTypeListView, upgradeListView, abilityListView;
    private static Label iconLabel, spriteLabel, missileSpriteLabel, armorTypeLabel, towerLabel, missileLabel, attackTypeLabel, abilityLabel, upgradeLabel;
    private static LabeledTextField name, attackTypeName;
    private static Button iconButton, spriteButton, missileSpriteButton, missileAnimEffectButton, saveButton, duplicateButton, deleteButton, removeAttackTypeButton, addAttackTypeButton, upgradeButton, removeAbilityButton, addAbilityButton;
    private static LabeledFloatField range, missileSpeed, missileDMG, attackTypeMultiplier, abilityValue1, abilityValue2, missileYOffset;
    private static LabeledIntField cost, cd, missileHeight, missileRadius, missileSplashRadius, missileAnimEffectFrameNum, missileAnimEffectFrameTime, missileFrameNum, missileFrameTime, frameNum, frameTime;
    private static FileChooser fileChooser;
    private static ImageView icon, sprite, missileSprite, missileAnimEffect;
    private static File iconFile, spriteFile, missileSpriteFile, missileAnimEffectFile;
    private static CheckBox missileHasAnimEffect, inShop, autoSave;
    private static ComboBox missileType, missileAttackType, upgrade, abilityType, abilityStat, abilityTarget, abilityMath;

    public static void initPane(){
        initListView();
        initBox();

        pane = new BorderPane();
        pane.setLeft(listView);
        pane.setCenter(box);
    }

    private static void refreshBox(boolean refreshAssets){
        Editor.SAFE_TO_SAVE = false;
        name.field.setText(selectedTower().name);
        name.field.positionCaret(name.field.getLength());
        cost.field.set(selectedTower().cost);
        cost.field.positionCaret(cost.field.getLength());
        range.field.set(selectedTower().range);
        range.field.positionCaret(range.field.getLength());
        cd.field.set(selectedTower().fireCooldown);
        cd.field.positionCaret(cd.field.getLength());
        missileSpeed.field.set(selectedTower().missile.moveSpeed);
        missileSpeed.field.positionCaret(missileSpeed.field.getLength());
        missileDMG.field.set(selectedTower().missile.maxDamage);
        missileDMG.field.positionCaret(missileDMG.field.getLength());
        missileYOffset.field.set(selectedTower().missileYOffset);
        missileYOffset.field.positionCaret(missileYOffset.field.getLength());
        missileHeight.field.set(selectedTower().missile.renderable.height);
        missileHeight.field.positionCaret(missileHeight.field.getLength());
        missileHeight.label.setText(" Height: ");
        missileType.getSelectionModel().select(selectedTower().missile.type.ordinal());
        missileRadius.field.set(selectedTower().missile.hitRadius);
        missileRadius.field.positionCaret(missileRadius.field.getLength());
        missileSplashRadius.field.set(selectedTower().missile.splashRadius);
        missileSplashRadius.field.positionCaret(missileSplashRadius.field.getLength());
        missileAnimEffectFrameNum.field.set(selectedTower().missile.animEffectFrameNum);
        missileAnimEffectFrameNum.field.positionCaret(missileAnimEffectFrameNum.field.getLength());
        missileAnimEffectFrameTime.field.set(selectedTower().missile.animEffectFrameTime);
        missileAnimEffectFrameTime.field.positionCaret(missileAnimEffectFrameTime.field.getLength());
        missileHasAnimEffect.setSelected(selectedTower().missile.hasAnimEffect);
        missileFrameNum.field.set(selectedTower().missile.renderable.frameNum);
        missileFrameNum.field.positionCaret(missileFrameNum.field.getLength());
        missileFrameTime.field.set(selectedTower().missile.frameTime);
        missileFrameTime.field.positionCaret(missileFrameTime.field.getLength());

        frameNum.field.set(selectedTower().renderable.frameNum);
        frameNum.field.positionCaret(frameNum.field.getLength());
        frameTime.field.set(selectedTower().frameTime);
        frameTime.field.positionCaret(frameTime.field.getLength());

        inShop.setSelected(selectedTower().inShop);

        int selIndex = armorTypeListView.getSelectionModel().getSelectedIndex();
        armorTypeListView.setItems(FXCollections.observableArrayList(Enemy.armorTypes));
        if (selIndex < 0 || selIndex >= Enemy.armorTypes.length)
            armorTypeListView.getSelectionModel().select(Enemy.armorTypes.length - 1);
        else
            armorTypeListView.getSelectionModel().select(selIndex);

        selIndex = attackTypeListView.getSelectionModel().getSelectedIndex();
        String[] attackTypeNames = new String[AttackType.attackTypes.length];
        for (int i = 0; i < attackTypeNames.length; i++)
            attackTypeNames[i] = AttackType.attackTypes[i].name;
        attackTypeListView.setItems(FXCollections.observableArrayList(attackTypeNames));
        missileAttackType.setItems(FXCollections.observableArrayList(attackTypeNames));
        if (selIndex < 0 || selIndex >= attackTypeNames.length)
            attackTypeListView.getSelectionModel().select(attackTypeNames.length - 1);
        else
            attackTypeListView.getSelectionModel().select(selIndex);
        attackTypeName.field.setText(AttackType.attackTypes[attackTypeListView.getSelectionModel().getSelectedIndex()].name);
        attackTypeName.field.positionCaret(attackTypeName.field.getLength());
        attackTypeMultiplier.field.set(AttackType.attackTypes[attackTypeListView.getSelectionModel().getSelectedIndex()].multipliers[armorTypeListView.getSelectionModel().getSelectedIndex()]);
        attackTypeMultiplier.field.positionCaret(attackTypeMultiplier.field.getLength());

        missileAttackType.getSelectionModel().select(selectedTower().missile.attackType);

        selIndex = upgradeListView.getSelectionModel().getSelectedIndex();
        String[] upgradeNames = new String[selectedTower().upgrades.length];
        for(int i = 0; i < upgradeNames.length; i++){
            upgradeNames[i] =  Tower.towers[selectedTower().upgrades[i]].name;
        }
        upgradeListView.setItems(FXCollections.observableArrayList(upgradeNames));
        if (selIndex < 0 || selIndex >= upgradeNames.length)
            upgradeListView.getSelectionModel().select(upgradeNames.length - 1);
        else
            upgradeListView.getSelectionModel().select(selIndex);

        selIndex = upgrade.getSelectionModel().getSelectedIndex();
        String[] towerNames = new String[Tower.towers.length];
        for (int i = 0; i < towerNames.length; i++)
            towerNames[i] = Tower.towers[i].name;
        upgrade.setItems(FXCollections.observableArrayList(towerNames));
        if (selIndex < 0 || selIndex >= towerNames.length)
            upgrade.getSelectionModel().select(towerNames.length - 1);
        else
            upgrade.getSelectionModel().select(selIndex);

        selIndex = abilityListView.getSelectionModel().getSelectedIndex();
        String[] abilityNames = new String[selectedTower().abilities.length];
        for (int i = 0; i < abilityNames.length; i++)
            abilityNames[i] = selectedTower().abilities[i].name();
        abilityListView.setItems(FXCollections.observableArrayList(abilityNames));
        if (selIndex < 0 || selIndex >= abilityNames.length)
            abilityListView.getSelectionModel().select(abilityNames.length - 1);
        else
            abilityListView.getSelectionModel().select(selIndex);

        refreshAbilityBox();

        if(refreshAssets) {
            iconFile = new File(Gdx.files.local(Assets.towerIconPath + "/" + name.field.getText().toLowerCase() + ".png").path());
            icon.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(iconFile.getPath())));

            spriteFile = new File(Gdx.files.local(Assets.towerPath + "/" + name.field.getText().toLowerCase() + ".png").path());
            sprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(spriteFile.getPath())));

            missileSpriteFile = new File(Gdx.files.local(Assets.missilePath + "/" + name.field.getText().toLowerCase() + ".png").path());
            missileSprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(missileSpriteFile.getPath())));

            if( selectedTower().missile.hasAnimEffect) {
                missileAnimEffectFile = new File(Gdx.files.local(Assets.missileAnimEffectPath + "/" + name.field.getText().toLowerCase() + ".png").path());
                missileAnimEffect.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(missileAnimEffectFile.getPath())));
            }else{
                missileAnimEffectFile = null;
                missileAnimEffect.setImage(null);
            }
        }
        missileAnimEffectEnabled();
        Editor.SAFE_TO_SAVE = true;
    }

    private static void missileAnimEffectEnabled(){
        boolean isDisabled = !selectedTower().missile.hasAnimEffect;
        missileAnimEffectButton.setDisable(isDisabled);
        missileAnimEffectFrameNum.setDisable(isDisabled);
        missileAnimEffectFrameTime.setDisable(isDisabled);
    }

    private static void refreshListView() {
        int selIndex = listView.getSelectionModel().getSelectedIndex();
        String[] towerNames = new String[Tower.towers.length];
        for (int i = 0; i < towerNames.length; i++)
            towerNames[i] = Tower.towers[i].name;
        listView.setItems(FXCollections.observableArrayList(towerNames));
        if (selIndex < 0 || selIndex >= towerNames.length)
            listView.getSelectionModel().select(towerNames.length - 1);
        else
            listView.getSelectionModel().select(selIndex);
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

    private static void initBox(){
        towerLabel = new Label("Tower:");
        missileLabel = new Label("Missile:");
        armorTypeLabel = new Label("Armor Types:");
        attackTypeLabel = new Label("Attack Types:");
        abilityLabel = new Label("Abilities:");
        upgradeLabel = new Label("Upgrades:");

        iconLabel = new Label(" Shop Icon: ");
        spriteLabel = new Label(" Tower Texture: ");
        missileSpriteLabel = new Label(" Texture: ");
        name = new LabeledTextField(" Name: ", selectedTower().name);
        name.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER, true));
        cost = new LabeledIntField(" Cost: ", selectedTower().cost);
        cost.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        range = new LabeledFloatField(" Range: ", selectedTower().range, 0);
        range.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        cd = new LabeledIntField(" Fire Cooldown: ", selectedTower().fireCooldown, 0);
        cd.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileSpeed = new LabeledFloatField(" Speed: ", selectedTower().missile.moveSpeed);
        missileSpeed.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileDMG = new LabeledFloatField(" Damage: ", selectedTower().missile.maxDamage);
        missileDMG.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileYOffset = new LabeledFloatField(" Missile Y Offset: ", selectedTower().missileYOffset);
        missileYOffset.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileHeight = new LabeledIntField(" Height: ", selectedTower().missile.renderable.height, 0);
        missileHeight.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileType = new ComboBox(FXCollections.observableArrayList(Missile.Type.values()));
        missileType.getSelectionModel().select(selectedTower().missile.type.ordinal());
        missileType.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TOWER));
        missileRadius = new LabeledIntField(" Hit Radius: ", selectedTower().missile.hitRadius, 0);
        missileRadius.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileSplashRadius = new LabeledIntField(" Splash Radius: ", selectedTower().missile.splashRadius, 0);
        missileSplashRadius.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileAnimEffectFrameNum = new LabeledIntField(" Number of Frames: ", selectedTower().missile.animEffectFrameNum, 1);
        missileAnimEffectFrameNum.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileAnimEffectFrameTime = new LabeledIntField(" Frame Duration: ", selectedTower().missile.animEffectFrameTime, 0);
        missileAnimEffectFrameTime.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileHasAnimEffect = new CheckBox("Has Effect");
        missileHasAnimEffect.setSelected(selectedTower().missile.hasAnimEffect);
        missileHasAnimEffect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(missileHasAnimEffect.isSelected()){
                    missileAnimEffect.setImage(Editor.getImageFromFileHandle(Gdx.files.local(Assets.animEffectPath + "/spawn.png")));
                    missileAnimEffectFile = Gdx.files.local(Assets.animEffectPath + "/spawn.png").file();
                    missileAnimEffectFrameNum.field.set(5);
                    missileAnimEffectFrameTime.field.set(10);
                }else{
                    SyncPacket.sendDeleteSync(Assets.missileAnimEffectPath + "/" + selectedTower().name.toLowerCase() + ".png");
                    Gdx.files.local(Assets.missileAnimEffectPath + "/" + selectedTower().name.toLowerCase() + ".png").delete();
                    missileAnimEffect.setImage(null);
                    missileAnimEffectFile = null;
                }
                autoSave(true);
            }
        });
        missileFrameNum = new LabeledIntField(" Number of Frames: ", selectedTower().missile.renderable.frameNum, 1);
        missileFrameNum.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        missileFrameTime = new LabeledIntField(" Frame Duration: ", selectedTower().missile.frameTime, 0);
        missileFrameTime.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));

        String[] attackTypeNames = new String[AttackType.attackTypes.length];
        for(int i = 0; i < attackTypeNames.length; i++){
            attackTypeNames[i] = AttackType.attackTypes[i].name;
        }
        missileAttackType = new ComboBox(FXCollections.observableArrayList(attackTypeNames));
        missileAttackType.setVisibleRowCount(2);
        missileAttackType.getSelectionModel().select(selectedTower().missile.attackType);
        missileAttackType.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TOWER));

        frameNum = new LabeledIntField(" Number of Frames: ", selectedTower().renderable.frameNum, 1);
        frameNum.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));
        frameTime = new LabeledIntField(" Frame Duration: ", selectedTower().frameTime, 0);
        frameTime.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER));

        inShop = new CheckBox("In Shop");
        inShop.setSelected(selectedTower().inShop);
        inShop.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TOWER, false));

        icon = new ImageView(Editor.getImageFromFileHandle(Gdx.files.local(Assets.towerIconPath + "/" + name.field.getText().toLowerCase() + ".png")));
        icon.setPreserveRatio(true);
        icon.setFitHeight(32);

        sprite = new ImageView(Editor.getImageFromFileHandle(Gdx.files.local(Assets.towerPath + "/" + name.field.getText().toLowerCase() + ".png")));
        sprite.setPreserveRatio(true);
        sprite.setFitHeight(128);

        missileSprite = new ImageView(Editor.getImageFromFileHandle(Gdx.files.local(Assets.missilePath + "/" + name.field.getText().toLowerCase() + ".png")));
        missileSprite.setPreserveRatio(true);
        missileSprite.setFitHeight(32);

        if( selectedTower().missile.hasAnimEffect)
            missileAnimEffect = new ImageView(Editor.getImageFromFileHandle(Gdx.files.local(Assets.missileAnimEffectPath + "/" + name.field.getText().toLowerCase() + ".png")));
        else
            missileAnimEffect = new ImageView();
        missileAnimEffect.setPreserveRatio(true);
        missileAnimEffect.setFitHeight(64);

        iconFile = new File(Gdx.files.local(Assets.towerIconPath + "/" + name.field.getText().toLowerCase() + ".png").path());
        spriteFile = new File(Gdx.files.local(Assets.towerPath + "/" + name.field.getText().toLowerCase() + ".png").path());
        missileSpriteFile = new File(Gdx.files.local(Assets.missilePath + "/" + name.field.getText().toLowerCase() + ".png").path());
        if( selectedTower().missile.hasAnimEffect)
            missileAnimEffectFile = new File(Gdx.files.local(Assets.missileAnimEffectPath + "/" + name.field.getText().toLowerCase() + ".png").path());

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        iconButton = new Button("Choose Icon");
        iconButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.setTitle("Choose Shop Icon");
                fileChooser.setInitialDirectory(new File(Gdx.files.getLocalStoragePath() + Assets.towerIconPath));
                iconFile = fileChooser.showOpenDialog(Editor.primaryStage);
                if(iconFile != null) {
                    icon.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(iconFile.getPath())));
                    autoSave(true);
                }
            }
        });
        spriteButton = new Button("Choose Texture");
        spriteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.setTitle("Choose Texture");
                fileChooser.setInitialDirectory(new File(Gdx.files.getLocalStoragePath() + Assets.towerPath));
                spriteFile = fileChooser.showOpenDialog(Editor.primaryStage);
                if(spriteFile != null) {
                    sprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(spriteFile.getPath())));
                    autoSave(true);
                }
            }
        });
        missileSpriteButton = new Button("Choose Texture");
        missileSpriteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.setTitle("Choose Texture");
                fileChooser.setInitialDirectory(new File(Gdx.files.getLocalStoragePath() + Assets.missilePath));
                missileSpriteFile = fileChooser.showOpenDialog(Editor.primaryStage);
                if(missileSpriteFile != null) {
                    missileSprite.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(missileSpriteFile.getPath())));
                    autoSave(true);
                }
            }
        });
        missileAnimEffectButton = new Button("Choose Texture");
        missileAnimEffectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                fileChooser.setTitle("Choose Texture");
                fileChooser.setInitialDirectory(new File(Gdx.files.getLocalStoragePath() + Assets.missileAnimEffectPath));
                missileAnimEffectFile = fileChooser.showOpenDialog(Editor.primaryStage);
                if(missileAnimEffectFile != null && selectedTower().missile.hasAnimEffect){
                    missileAnimEffect.setImage(Editor.getImageFromFileHandle(Gdx.files.absolute(missileAnimEffectFile.getPath())));
                    autoSave(true);
                }
            }
        });
        duplicateButton = new Button("Duplicate");
        duplicateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!Gdx.files.local(Assets.towerPath + "/" + selectedTower().name.toLowerCase() + " copy.png").exists()) {
                    Editor.copyFile(Gdx.files.local(Assets.towerPath + "/" + selectedTower().name.toLowerCase() + " copy.png"), Gdx.files.local(Assets.towerPath + "/" + selectedTower().name.toLowerCase() + ".png"));
                    Editor.copyFile(Gdx.files.local(Assets.towerIconPath + "/" + selectedTower().name.toLowerCase() + " copy.png"), Gdx.files.local(Assets.towerIconPath + "/" + selectedTower().name.toLowerCase() + ".png"));
                    Editor.copyFile(Gdx.files.local(Assets.missilePath + "/" + selectedTower().name.toLowerCase() + " copy.png"), Gdx.files.local(Assets.missilePath + "/" + selectedTower().name.toLowerCase() + ".png"));
                    if( selectedTower().missile.hasAnimEffect)
                        Editor.copyFile(Gdx.files.local(Assets.missileAnimEffectPath + "/" + selectedTower().name.toLowerCase() + " copy.png"), Gdx.files.local(Assets.missileAnimEffectPath + "/" + selectedTower().name.toLowerCase() + ".png"));

                    StringBuilder upgradeBuilder = new StringBuilder();
                    if(selectedTower().upgrades.length > 0) {
                        upgradeBuilder.append(selectedTower().upgrades[0]);
                        for(int i = 1; i < selectedTower().upgrades.length; i++){
                            upgradeBuilder.append(":" + selectedTower().upgrades[i]);
                        }
                    }else{
                        upgradeBuilder.append("-1");
                    }
                    Gdx.files.local(Assets.towerPath + "/info").writeString(selectedTower().name + " Copy, " + selectedTower().cost + ", " + selectedTower().range + ", " + selectedTower().fireCooldown + ", " + selectedTower().missile.moveSpeed + ", " + selectedTower().missile.maxDamage + ", " + selectedTower().missile.renderable.height  + ", " + selectedTower().missile.type.ordinal()  + ", " + selectedTower().missile.hitRadius + ", " + selectedTower().missile.splashRadius   + ", " + selectedTower().missile.animEffectFrameNum + ", " + selectedTower().missile.animEffectFrameTime + ", " + selectedTower().missile.hasAnimEffect + ", " + selectedTower().missile.renderable.frameNum + ", " + selectedTower().missile.frameTime + ", " + selectedTower().missile.attackType + ", " + selectedTower().renderable.frameNum + ", " + selectedTower().frameTime + ", " + upgradeBuilder.toString() + ", " + selectedTower().inShop + ", " + abilitiesToString(false) + ", " + selectedTower().missileYOffset + ";\n", true);

                    FileSender.sendSyncFile(Assets.towerPath + "/" + selectedTower().name.toLowerCase() + " copy.png");
                    FileSender.sendSyncFile(Assets.towerIconPath + "/" + selectedTower().name.toLowerCase() + " copy.png");
                    FileSender.sendSyncFile(Assets.missilePath + "/" + selectedTower().name.toLowerCase() + " copy.png");
                    if( selectedTower().missile.hasAnimEffect)
                        FileSender.sendSyncFile(Assets.missileAnimEffectPath + "/" + selectedTower().name.toLowerCase() + " copy.png");
                    FileSender.sendSyncFile(Assets.towerPath + "/info");
                    FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.TOWER, false, true));

                    update(true, true);
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
                if(Tower.towers.length > 4) {
                    SyncPacket.sendDeleteSync(Assets.towerPath + "/" + selectedTower().name.toLowerCase() + ".png");
                    SyncPacket.sendDeleteSync(Assets.towerIconPath + "/" + selectedTower().name.toLowerCase() + ".png");
                    SyncPacket.sendDeleteSync(Assets.missilePath + "/" + selectedTower().name.toLowerCase() + ".png");
                    if( selectedTower().missile.hasAnimEffect)
                        SyncPacket.sendDeleteSync(Assets.missileAnimEffectPath + "/" + selectedTower().name.toLowerCase() + ".png");

                    Gdx.files.local(Assets.towerPath + "/" + selectedTower().name.toLowerCase() + ".png").delete();
                    Gdx.files.local(Assets.towerIconPath + "/" + selectedTower().name.toLowerCase() + ".png").delete();
                    Gdx.files.local(Assets.missilePath + "/" + selectedTower().name.toLowerCase() + ".png").delete();
                    if( selectedTower().missile.hasAnimEffect)
                        Gdx.files.local(Assets.missileAnimEffectPath + "/" + selectedTower().name.toLowerCase() + ".png").delete();

                    String[] towerParts = Gdx.files.local(Assets.towerPath + "/info").readString().replace("\n", "").split(":", 2);
                    Array<String> towerArray = new Array<String>(towerParts[1].split(";"));
                    towerArray.removeIndex(listView.getSelectionModel().getSelectedIndex());
                    StringBuilder builder = new StringBuilder();
                    builder.append(towerParts[0]);
                    builder.append(":\n");
                    for (int i = 0; i < towerArray.size; i++) {
                        builder.append(towerArray.get(i));
                        builder.append(";\n");
                    }
                    Gdx.files.local(Assets.towerPath + "/info").writeString(builder.toString(), false);

                    FileSender.sendSyncFile(Assets.towerPath + "/info");
                    FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.TOWER, true, true));

                    update(false, true);
                }
            }
        });



        attackTypeListView = new ListView(FXCollections.observableArrayList(attackTypeNames));
        attackTypeListView.getSelectionModel().select(0);
        attackTypeListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Editor.SAFE_TO_SAVE = false;
                attackTypeName.field.setText(AttackType.attackTypes[attackTypeListView.getSelectionModel().getSelectedIndex()].name);
                Editor.SAFE_TO_SAVE = true;
                attackTypeMultiplier.field.set(AttackType.attackTypes[attackTypeListView.getSelectionModel().getSelectedIndex()].multipliers[armorTypeListView.getSelectionModel().getSelectedIndex()]);
            }
        });

        armorTypeListView = new ListView(FXCollections.observableArrayList(Enemy.armorTypes));
        armorTypeListView.getSelectionModel().select(0);
        armorTypeListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Editor.SAFE_TO_SAVE = false;
                attackTypeMultiplier.field.set(AttackType.attackTypes[attackTypeListView.getSelectionModel().getSelectedIndex()].multipliers[armorTypeListView.getSelectionModel().getSelectedIndex()]);
                Editor.SAFE_TO_SAVE = true;
            }
        });

        attackTypeMultiplier = new LabeledFloatField(" Multiplier: ", AttackType.attackTypes[attackTypeListView.getSelectionModel().getSelectedIndex()].multipliers[armorTypeListView.getSelectionModel().getSelectedIndex()]);
        attackTypeMultiplier.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER, false));

        removeAttackTypeButton = new Button("Remove Attack Type");
        removeAttackTypeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(attackTypeListView.getSelectionModel().getSelectedIndex() >= 2) {
                    AttackType[] newAttackTypes = new AttackType[AttackType.attackTypes.length - 1];
                    for(int i = 0; i < newAttackTypes.length; i++){
                        if(i < attackTypeListView.getSelectionModel().getSelectedIndex())
                            newAttackTypes[i] = new AttackType(AttackType.attackTypes[i]);
                        else if(i >= attackTypeListView.getSelectionModel().getSelectedIndex())
                            newAttackTypes[i] =  new AttackType(AttackType.attackTypes[i + 1]);
                    }
                    AttackType.attackTypes = newAttackTypes;
                    save(false);
                }
            }
        });

        attackTypeName = new LabeledTextField(" Attack Type Name: ", AttackType.attackTypes[attackTypeListView.getSelectionModel().getSelectedIndex()].name);
        attackTypeName.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER, false));

        addAttackTypeButton = new Button("Add Attack Type");
        addAttackTypeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AttackType[] newAttackTypes = new AttackType[AttackType.attackTypes.length + 1];
                for(int i = 0; i < AttackType.attackTypes.length; i++){
                    newAttackTypes[i] = new AttackType(AttackType.attackTypes[i]);
                }
                newAttackTypes[newAttackTypes.length - 1] = new AttackType(attackTypeName.field.getText() + " Copy");
                attackTypeListView.getSelectionModel().clearSelection();
                for(int i = 0; i < Enemy.armorTypes.length; i++){
                    newAttackTypes[newAttackTypes.length - 1].multipliers[i] = 1;
                }
                AttackType.attackTypes = newAttackTypes;
                save(false, false, true);
            }
        });

        String[] upgradeNames = new String[selectedTower().upgrades.length];
        for(int i = 0; i < upgradeNames.length; i++){
            upgradeNames[i] =  Tower.towers[selectedTower().upgrades[i]].name;
        }
        upgradeListView = new ListView(FXCollections.observableArrayList(upgradeNames));
        upgradeListView.setSelectionModel(new EmptySelectionModel());
        if(upgradeNames.length > 0)
            upgradeListView.getSelectionModel().select(0);

        String[] towerNames = new String[Tower.towers.length];
        for (int i = 0; i < towerNames.length; i++)
            towerNames[i] = Tower.towers[i].name;
        upgrade = new ComboBox(FXCollections.observableArrayList(towerNames));
        upgrade.setVisibleRowCount(3);
        upgrade.getSelectionModel().select(0);

        upgradeButton = new Button("Toggle Upgrade");
        upgradeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for(int i = 0; i < upgradeListView.getItems().size(); i++){
                    if(upgrade.getSelectionModel().getSelectedIndex() == Tower.indexOf((String)upgradeListView.getItems().get(i))){ // Remove upgrade.
                        upgradeListView.getItems().remove(i);
                        save(false);
                        return;
                    }
                }
                upgradeListView.getItems().add(Tower.towers[upgrade.getSelectionModel().getSelectedIndex()].name); // Add upgrade.
                save(false);
            }
        });

        abilityType = new ComboBox(Editor.getObservableListFromEnum(Ability.Type.values(), Ability.TOWER_START, Ability.TOWER_END));
        abilityType.getSelectionModel().select(0);
        abilityType.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TOWER, false));
        abilityStat = new ComboBox(Editor.getObservableListFromEnum(Ability.EnemyStat.values()));
        abilityStat.getSelectionModel().select(0);
        abilityStat.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TOWER, false));
        abilityTarget = new ComboBox(Editor.getObservableListFromEnum(Ability.Target.values()));
        abilityTarget.getSelectionModel().select(0);
        abilityTarget.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TOWER, false));
        abilityValue1 = new LabeledFloatField(" Value 1: ", 0);
        abilityValue1.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER, false));
        abilityValue2 = new LabeledFloatField(" Value 2: ", 0);
        abilityValue2.field.textProperty().addListener(Editor.autoSaveChangeListener(Editor.Type.TOWER, false));
        abilityMath = new ComboBox(Editor.getObservableListFromEnum(Ability.Math.values()));
        abilityMath.getSelectionModel().select(0);
        abilityMath.setOnAction(Editor.autoSaveEventHandler(Editor.Type.TOWER, false));

        String[] abilityNames = new String[selectedTower().abilities.length];
        for(int i = 0; i < abilityNames.length; i++){
            abilityNames[i] = selectedTower().abilities[i].name();
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
                    Ability[] newAbilities = new Ability[selectedTower().abilities.length - 1];
                    for(int i = 0; i < newAbilities.length; i++){
                        if(i < abilityListView.getSelectionModel().getSelectedIndex())
                            newAbilities[i] = new Ability(selectedTower().abilities[i]);
                        else if(i >= abilityListView.getSelectionModel().getSelectedIndex())
                            newAbilities[i] =  new Ability(selectedTower().abilities[i + 1]);
                    }
                    selectedTower().abilities = newAbilities;
                    abilityListView.getSelectionModel().clearSelection();
                    save(false);
                }
            }
        });
        addAbilityButton = new Button("Add Ability");
        addAbilityButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Ability[] newAbilities = new Ability[selectedTower().abilities.length + 1];
                for(int i = 0; i < newAbilities.length - 1; i++){
                    newAbilities[i] = new Ability(selectedTower().abilities[i]);
                }
                newAbilities[newAbilities.length - 1] = new Ability();
                newAbilities[newAbilities.length - 1].type = Ability.Type.values()[abilityType.getSelectionModel().getSelectedIndex() + Ability.TOWER_START];
                newAbilities[newAbilities.length - 1].stat = 0;
                newAbilities[newAbilities.length - 1].target = Ability.Target.Both_Equal;
                newAbilities[newAbilities.length - 1].value1 = 0;
                newAbilities[newAbilities.length - 1].value2 = 0;
                newAbilities[newAbilities.length - 1].math = Ability.Math.Set;
                selectedTower().abilities = newAbilities;
                abilityListView.getItems().add(selectedTower().abilities[selectedTower().abilities.length - 1].name());
                abilityListView.getSelectionModel().select(selectedTower().abilities.length - 1);
                save(false);
            }
        });

        towerBox = new VBox();
        towerBox.setAlignment(Pos.CENTER);
        towerBox.getChildren().addAll(towerLabel, name, cost, range, cd, inShop, missileYOffset, iconLabel, icon, iconButton, spriteLabel, sprite, spriteButton, frameNum, frameTime, duplicateButton, saveButton, autoSave, deleteButton);

        missileBox = new VBox();
        missileBox.setAlignment(Pos.CENTER);
        missileBox.getChildren().addAll(missileLabel, missileType, missileHeight, missileRadius, missileSplashRadius, missileSpeed, missileDMG, missileAttackType, missileSpriteLabel, missileSprite, missileSpriteButton, missileFrameNum, missileFrameTime, missileHasAnimEffect, missileAnimEffect, missileAnimEffectButton, missileAnimEffectFrameNum, missileAnimEffectFrameTime);

        attackTypeBox = new VBox();
        attackTypeBox.setAlignment(Pos.CENTER);
        attackTypeBox.getChildren().addAll(attackTypeLabel, attackTypeListView, armorTypeLabel, armorTypeListView, attackTypeMultiplier, removeAttackTypeButton, attackTypeName, addAttackTypeButton);

        abilityBox = new VBox();
        abilityBox.setAlignment(Pos.CENTER);
        abilityBox.getChildren().addAll(abilityLabel, abilityListView, removeAbilityButton, addAbilityButton, abilityType, abilityStat, abilityTarget, abilityMath, abilityValue1, abilityValue2);

        upgradeBox = new VBox();
        upgradeBox.setAlignment(Pos.CENTER);
        upgradeBox.getChildren().addAll(upgradeLabel, upgradeListView, upgrade, upgradeButton);

        box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(towerBox, missileBox, attackTypeBox, abilityBox, upgradeBox);

        missileAnimEffectEnabled();
        refreshAbilityBox();
    }

    private static void refreshAbilityBox(){
        Editor.SAFE_TO_SAVE = false;
        if(selectedTower().abilities.length > 0) {
            abilityType.getSelectionModel().select(selectedAbility().type.ordinal() - Ability.TOWER_START);
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
        return selectedTower().abilities[abilityListView.getSelectionModel().getSelectedIndex()];
    }

    public static void update(final boolean selectLast, final boolean updateAssets){
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Tower.load();
                if(updateAssets) {
                    Assets.loadTowers();
                    Assets.loadShopTowers();
                    Assets.loadMissiles();
                    Assets.loadMissileAnimEffects();
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refreshListView();
                        if(selectLast){
                            listView.getSelectionModel().select(Tower.towers.length - 1);

                        }
                        refreshBox(updateAssets);
                        if(selectLast){
                            attackTypeListView.getSelectionModel().select(attackTypeListView.getItems().size() - 1);

                        }
                        Editor.SAFE_TO_SAVE = true;
                    }
                });
            }
        });
    }

    public static void save(boolean saveAssets){
        save(saveAssets, false);
    }

    private static Tower selectedTower(){
        return Tower.towers[listView.getSelectionModel().getSelectedIndex()];
    }

    private static String abilityToString(int abilityIndex, boolean setSelected){
        if(setSelected && abilityIndex == abilityListView.getSelectionModel().getSelectedIndex())
            return ":" + (abilityType.getSelectionModel().getSelectedIndex() + Ability.TOWER_START) + "/" + abilityStat.getSelectionModel().getSelectedIndex() + "/" + abilityValue1.field.num() + "/" + abilityValue2.field.num() + "/" + abilityMath.getSelectionModel().getSelectedIndex() + "/" + abilityTarget.getSelectionModel().getSelectedIndex();
        return ":" + selectedTower().abilities[abilityIndex].type.ordinal() + "/" + selectedTower().abilities[abilityIndex].stat + "/" + selectedTower().abilities[abilityIndex].value1 + "/" + selectedTower().abilities[abilityIndex].value2 + "/" + selectedTower().abilities[abilityIndex].math.ordinal() + "/" + selectedTower().abilities[abilityIndex].target.ordinal();
    }

    private static String abilitiesToString(boolean setSelected){
        StringBuilder abilityBuilder = new StringBuilder();
        if(selectedTower().abilities.length > 0){
            abilityBuilder.append(abilityToString(0, setSelected).substring(1));
            for(int i = 1; i < selectedTower().abilities.length; i++){
                abilityBuilder.append(abilityToString(i, setSelected));
            }
        }else{
            abilityBuilder.append("-1");
        }
        return abilityBuilder.toString();
    }

    public static void save(boolean saveAssets, boolean ignoreSafeToSave){
        save(saveAssets, ignoreSafeToSave, false);
    }

    private static void save(boolean saveAssets, boolean ignoreSafeToSave, boolean selectLast) {
        name.field.setText(name.field.getText().trim());
        if ((Tower.indexOf(name.field.getText()) == -1 || Tower.indexOf(name.field.getText()) == listView.getSelectionModel().getSelectedIndex()) && (Editor.SAFE_TO_SAVE || ignoreSafeToSave)) {
            Editor.SAFE_TO_SAVE = false;
            if (saveAssets) {
                Editor.replaceImage(Assets.towerIconPath, iconFile, name.field.getText(), selectedTower().name);
                Editor.replaceImage(Assets.towerPath, spriteFile, name.field.getText(), selectedTower().name);
                Editor.replaceImage(Assets.missilePath, missileSpriteFile, name.field.getText(), selectedTower().name);
                if (missileHasAnimEffect.isSelected()) {
                    Editor.replaceImage(Assets.missileAnimEffectPath, missileAnimEffectFile, name.field.getText(), selectedTower().name);
                }
            }
            String[] towerParts = Gdx.files.local(Assets.towerPath + "/info").readString().replace("\n", "").split(":", 2);
            String[] towerArray = towerParts[1].split(";");

            StringBuilder upgradeBuilder = new StringBuilder();
            if(upgradeListView.getItems().size() > 0) {
                upgradeBuilder.append(Tower.indexOf((String)upgradeListView.getItems().get(0)));
                for(int i = 1; i < upgradeListView.getItems().size(); i++){
                    upgradeBuilder.append(":" + Tower.indexOf((String)upgradeListView.getItems().get(i)));
                }
            }else{
                upgradeBuilder.append("-1");
            }

            String towerStr = name.field.getText() + ", " + cost.field.getText() + ", " + range.field.getText() + ", " + cd.field.getText() + ", " + missileSpeed.field.getText() + ", " + missileDMG.field.getText() + ", " + missileHeight.field.getText() + ", " + missileType.getSelectionModel().getSelectedIndex() + ", " + missileRadius.field.getText() + ", " + missileSplashRadius.field.getText() + ", " + missileAnimEffectFrameNum.field.getText() + ", " + missileAnimEffectFrameTime.field.getText() + ", " + missileHasAnimEffect.isSelected() + ", " + missileFrameNum.field.getText() + ", " + missileFrameTime.field.getText() + ", " + missileAttackType.getSelectionModel().getSelectedIndex() + ", " + frameNum.field.getText() + ", " + frameTime.field.getText() + ", " + upgradeBuilder.toString() + ", " + inShop.isSelected() + ", " + abilitiesToString(true) + ", " + missileYOffset.field.getText();
            towerArray[listView.getSelectionModel().getSelectedIndex()] = towerStr;
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < AttackType.attackTypes.length; i++){
                if(i == attackTypeListView.getSelectionModel().getSelectedIndex()){
                    builder.append(attackTypeName.field.getText());
                    for(int m = 0; m < AttackType.attackTypes[i].multipliers.length; m++){
                        if(m == armorTypeListView.getSelectionModel().getSelectedIndex())
                            builder.append(", " + attackTypeMultiplier.field.getText());
                        else
                            builder.append(", " + AttackType.attackTypes[i].multipliers[m]);
                    }
                }else {
                    builder.append(AttackType.attackTypes[i].name);
                    for (int m = 0; m < AttackType.attackTypes[i].multipliers.length; m++) {
                        builder.append(", " + AttackType.attackTypes[i].multipliers[m]);
                    }
                }
                if(i < AttackType.attackTypes.length - 1)
                    builder.append(";\n");
            }
            builder.append(":\n");
            for (int i = 0; i < towerArray.length; i++) {
                builder.append(towerArray[i]);
                builder.append(";\n");
            }
            Gdx.files.local(Assets.towerPath + "/info").writeString(builder.toString(), false);

            // Delete old files if tower has been renamed.
            if (saveAssets) {
                SyncPacket.sendDeleteSync(Assets.towerPath + "/" + selectedTower().name.toLowerCase() + ".png");
                SyncPacket.sendDeleteSync(Assets.towerIconPath + "/" + selectedTower().name.toLowerCase() + ".png");
                SyncPacket.sendDeleteSync(Assets.missilePath + "/" + selectedTower().name.toLowerCase() + ".png");
                SyncPacket.sendDeleteSync(Assets.missileAnimEffectPath + "/" + selectedTower().name.toLowerCase() + ".png");

                FileSender.sendSyncFile(Assets.towerPath + "/" + name.field.getText().toLowerCase() + ".png");
                FileSender.sendSyncFile(Assets.towerIconPath + "/" + name.field.getText().toLowerCase() + ".png");
                FileSender.sendSyncFile(Assets.missilePath + "/" + name.field.getText().toLowerCase() + ".png");
                if (missileHasAnimEffect.isSelected()) {
                    FileSender.sendSyncFile(Assets.missileAnimEffectPath + "/" + name.field.getText().toLowerCase() + ".png");
                }
            }
            FileSender.sendSyncFile(Assets.towerPath + "/info");
            FileSender.queueException(EditorPacket.newSyncEditorPacket(Editor.Type.TOWER, false, saveAssets));

            update(selectLast, saveAssets);
        }
    }

    public static void autoSave(boolean saveAssets){
        if(AUTO_SAVE_ON && Editor.SAFE_TO_SAVE)
            save(saveAssets);
    }
}
