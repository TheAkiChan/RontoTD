package com.rontoking.rontotd.game.systems;

import com.rontoking.rontotd.editor.Editor;
import com.rontoking.rontotd.game.entities.ChatMessage;

public class Commands {
    private static String HELP_MSG = "/help /editor /ready /reset /zoom [number] /ai lvl /ai spend /ai test";

    public static boolean tryParse(String command){
        if(command.toLowerCase().equals("/help")){
            Chat.addSystemMsg(HELP_MSG);
            return true;
        }
        if(command.toLowerCase().equals("/editor")){
            Editor.open();
            return true;
        }
        if(command.toLowerCase().equals("/ready")){
            InputHandler.getReady();
            return true;
        }
        if(command.toLowerCase().equals("/reset")){
            Camera.setToDefault(1);
            return true;
        }
        if(isCommand(command, "/zoom ")){
            try {
                Camera.setZoom(Float.parseFloat(command.split(" ", 2)[1].trim()));
            }catch (Exception ex){
                Chat.addSystemMsg("\"" + command.split(" ", 2)[1].trim() + "\" is not a valid number.");
            }
            return true;
        }
        //if(command.toLowerCase().equals("/ai search") || command.toLowerCase().equals("-ai find")){
        //    AI.findGoodLevel();
        //    return true;
        //}
        if(command.toLowerCase().equals("/ai lvl")){
            AI.generateRandomLevel();
            return true;
        }
        if(command.toLowerCase().equals("/ai spend")){
            AI.spendMoney();
            return true;
        }
        if(command.toLowerCase().equals("/ai test")){
            AI.testLevel();
            return true;
        }
        return false;
    }

    private static boolean isCommand(String str, String command){
        return str.length() >= command.length() && str.toLowerCase().substring(0, command.length()).equals(command);
    }
}
