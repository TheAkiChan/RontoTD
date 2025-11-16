package com.rontoking.rontotd.game.systems.networking.packets;

import com.esotericsoftware.kryonet.Connection;
import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.editor.components.*;
import com.rontoking.rontotd.game.systems.networking.FileSender;
import com.rontoking.rontotd.game.systems.networking.Networking;
import javafx.application.Platform;

public class EditorPacket {
    public Editor.Type type;
    public boolean updateLevels;
    public boolean updateAssets;

    public EditorPacket(){

    }

    public static EditorPacket newSyncEditorPacket(Editor.Type type, boolean updateLevels, boolean updateAssets){
        EditorPacket editorPacket = new EditorPacket();
        editorPacket.type = type;
        editorPacket.updateLevels = updateLevels;
        editorPacket.updateAssets = updateAssets;
        return editorPacket;
    }

    public void load(int sender){
        if(Networking.state == Networking.State.SERVER)
            FileSender.queue(this, Networking.QueueType.EXCEPTION, sender);
        if (Editor.state == Editor.State.LOADED) {
            switch (type) {
                case LEVEL:
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            LevelEditor.update();
                        }
                    });
                    break;
                case TILE:
                    TileEditor.update(false, updateLevels, updateAssets);
                    break;
                case STRUCTURE:
                    StructureEditor.update(false, updateLevels, updateAssets);
                    break;
                case TOWER:
                    TowerEditor.update(false, updateAssets);
                    break;
                case ENEMY:
                    EnemyEditor.update(false, updateLevels, updateAssets);
                    break;
                default:
                    break;
            }
        }
    }
}
