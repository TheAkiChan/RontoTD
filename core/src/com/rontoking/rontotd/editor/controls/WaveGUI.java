package com.rontoking.rontotd.editor.controls;

import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.editor.components.LevelEditor;
import com.rontoking.rontotd.game.entities.Spawn;
import com.rontoking.rontotd.game.entities.Wave;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class WaveGUI {
    private Separator separator;
    private Label waveLabel;
    private Button addSpawnButton, removeSelfButton;
    public VBox spawnBox;
    public SpawnGUI[] spawnNodes;

    public WaveGUI(final Wave wave, final int spawnerIndex, final int waveIndex){
        this.separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(15, 0, 10, 0));

        waveLabel = new Label("WAVE " + waveIndex);
        waveLabel.fontProperty().set(new Font(40));

        addSpawnButton = new Button("Add Spawn");
        addSpawnButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(LevelEditor.selectedSpawnerIndex > -1) {
                    wave.spawns.add(new Spawn());
                    LevelEditor.updateSpawnBox();
                    LevelEditor.autoSave();
                }
            }
        });

        removeSelfButton = new Button("Remove Wave");
        removeSelfButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                LevelEditor.spawners.get(spawnerIndex).waves.removeIndex(waveIndex);
                LevelEditor.updateSpawnBox();
                LevelEditor.autoSave();
            }
        });

        this.spawnBox = new VBox();
        spawnBox.getChildren().addAll(separator, waveLabel, addSpawnButton, removeSelfButton);

        spawnNodes = new SpawnGUI[wave.spawns.size];
        for(int i = 0; i < spawnNodes.length; i++){
            spawnNodes[i] = new SpawnGUI(wave.spawns.get(i), spawnerIndex, waveIndex, i);
            spawnBox.getChildren().add(spawnNodes[i].vBox);
        }
    }
}
