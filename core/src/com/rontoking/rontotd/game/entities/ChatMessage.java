package com.rontoking.rontotd.game.entities;

import com.badlogic.gdx.graphics.Color;

public class ChatMessage {
    public String sender, text, senderSpaces;
    public Color senderColor;
    public int framesLeft;

    public ChatMessage(String sender, Color senderColor, String text){
        this.sender = sender;
        this.senderSpaces = "  ";
        for(int i = 0; i < sender.length(); i++)
            senderSpaces += "  ";
        this.senderColor = senderColor;
        this.text = text;
        this.framesLeft = 32 * 5 * text.trim().replaceAll(" +", " ").split(" ").length;
    }
}
