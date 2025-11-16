package com.rontoking.rontotd.editor.controls;

import com.rontoking.rontotd.editor.components.EnemyEditor;
import com.rontoking.rontotd.editor.components.LevelEditor;
import com.rontoking.rontotd.game.entities.Spawn;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class SpawnGUI {
    private Separator separator;
    private Label spawnLabel;

    private Button removeSelfButton;
    private Label frameWaitLabel, enemyTypeLabel, enemyNumLabel;

    public IntField frameWaitField, enemyNumField;
    public ComboBox enemyTypeBox;

    public VBox vBox;

    public SpawnGUI(final Spawn spawn, final int spawnerIndex, final int waveIndex, final int spawnIndex){
        this.separator = new Separator(Orientation.HORIZONTAL);
        this.separator.setPadding(new Insets(15, 0, 10, 0));

        this.spawnLabel = new Label("SPAWN " + spawnIndex);
        this.spawnLabel.fontProperty().set(new Font(20));

        this.removeSelfButton = new Button("Remove Spawn");
        this.removeSelfButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                LevelEditor.spawners.get(spawnerIndex).waves.get(waveIndex).spawns.removeIndex(spawnIndex);
                LevelEditor.updateSpawnBox();
                LevelEditor.autoSave();
            }
        });

        this.frameWaitLabel = new Label(" Cooldown: ");
        this.enemyTypeLabel = new Label(" Enemy State: ");
        this.enemyNumLabel = new Label(" Enemy Number: ");

        this.frameWaitField = new IntField(spawn.frameWait);
        this.frameWaitField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                spawn.frameWait = frameWaitField.num();
                LevelEditor.autoSave();
            }
        });
        this.enemyNumField = new IntField(spawn.enemyNum);
        this.enemyNumField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                spawn.enemyNum = enemyNumField.num();
                LevelEditor.autoSave();
            }
        });

        this.enemyTypeBox = new ComboBox(FXCollections.observableArrayList(EnemyEditor.listView.getItems()));
        this.enemyTypeBox.getSelectionModel().select(spawn.enemyType);
        this.enemyTypeBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                spawn.enemyType = enemyTypeBox.getSelectionModel().getSelectedIndex();
                LevelEditor.autoSave();
            }
        });

        this.vBox = new VBox();
        vBox.getChildren().addAll(separator, spawnLabel, removeSelfButton, frameWaitLabel, frameWaitField, enemyTypeLabel, enemyTypeBox, enemyNumLabel, enemyNumField);
    }
}
