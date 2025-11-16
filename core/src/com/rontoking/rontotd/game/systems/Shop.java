package com.rontoking.rontotd.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.game.entities.AttackType;
import com.rontoking.rontotd.game.entities.Tower;
import com.rontoking.rontotd.game.systems.networking.Networking;
import com.rontoking.rontotd.game.systems.networking.Sender;
import com.rontoking.rontotd.general.Point;

import static com.rontoking.rontotd.game.systems.Renderer.TOP_GUI_HEIGHT;
import static com.rontoking.rontotd.game.systems.Renderer.guiBatch;
import static com.rontoking.rontotd.game.systems.Renderer.BOT_GUI_HEIGHT;

public class Shop {
    public static Array<Tower> basicTowers = new Array<Tower>();
    public static Array<Tower> upgradeTowers = new Array<Tower>();

    public static int SELECTED_TOWER_IN_SHOP = -1;
    public static int SELECTED_TOWER_IN_GAME = -1;
    public static int PREV_SELECTED_TOWER_IN_SHOP = -1;

    private static int buttonSize;
    private static int towerScrollOffset = 0;
    private static final int MAX_TOWERS_VISIBLE = 18;

    public static Color DESCRIPTION_COLOR = new Color(0, 0, 0.1f, 0.8f);
    private static Color DEFAULT_SEL_COLOR = Color.ORANGE;
    private static Color SEL_COLOR = new Color(DEFAULT_SEL_COLOR);
    private static float MAX_SEL_COLOR_DIFF = 45;
    private static float SEL_COLOR_DIFF = 0;
    private static float SEL_COLOR_DELTA = 1;

    private static int towerOffsetNum = 0;

    public static int buttonY;

    public static void load(){
        buttonSize = (int)(BOT_GUI_HEIGHT * 0.5f);
        buttonY = 30;
    }

    public static void handleInput(){
        handleSelectingTowerInShopWithCursor();
        handleSelectingTowerInShopWithNumber();
        handleSelectingTowerInGame();
        onPress();
        onBack();
    }

    public static void handleSelectingTowerInGame() {
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && !Shop.isCursorOnGUI()) {
            Point mouseTilePos = Camera.cursorTile();
            for (int i = 0; i < Level.towers.size; i++) {
                if (Networking.state == Networking.State.SINGLEPLAYER || (Networking.state == Networking.State.SERVER && Level.towers.get(i).ownerID == 0) || (Networking.state == Networking.State.CLIENT && Level.towers.get(i).ownerID == Networking.client.getID())) {
                    if (mouseTilePos.isEqualTo(Level.towers.get(i).renderable.x, Level.towers.get(i).renderable.y)) {
                        if (SELECTED_TOWER_IN_GAME == i) {
                            if (Gdx.input.justTouched())
                                backToTowers();
                        } else {
                            if(Gdx.input.justTouched() || SELECTED_TOWER_IN_GAME != -1)
                            getUpgradeTowers(i, false);
                        }
                        return;
                    }
                }
            }
        }
    }

    public static void getUpgradeTowers(int index, boolean isChainUpgrade){
        SELECTED_TOWER_IN_GAME = index;
        if(SELECTED_TOWER_IN_SHOP == -1 || isChainUpgrade)
            SELECTED_TOWER_IN_SHOP = PREV_SELECTED_TOWER_IN_SHOP;
        else
            PREV_SELECTED_TOWER_IN_SHOP = SELECTED_TOWER_IN_SHOP;
        SELECTED_TOWER_IN_SHOP = -1;
        upgradeTowers.clear();
        for(int i = 0; i < Level.towers.get(index).upgrades.length; i++){
            upgradeTowers.add(Tower.towers[Level.towers.get(index).upgrades[i]]);
        }
    }

    private static Array<Tower> towersInShop(){
        if(SELECTED_TOWER_IN_GAME == -1)
            return basicTowers;
        else
            return upgradeTowers;
    }

    public static boolean isCursorOnGUI(){
        return new Rectangle(0, 0,Main.DESKTOP_WIDTH, TOP_GUI_HEIGHT).contains(InputHandler.mousePos.x, InputHandler.mousePos.y) || new Rectangle(0, Main.DESKTOP_HEIGHT - BOT_GUI_HEIGHT, Main.DESKTOP_WIDTH, BOT_GUI_HEIGHT).contains(InputHandler.mousePos.x, InputHandler.mousePos.y);
    }

    private static void onPress() {
        if (GameState.movedMouseAfterMenu && !isCursorOnGUI() && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && SELECTED_TOWER_IN_SHOP != -1 && Self.selectedTile != null && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            buyTower(SELECTED_TOWER_IN_SHOP);
        }
        if (GameState.movedMouseAfterMenu && !isCursorOnGUI() && Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && Self.selectedTile != null) {
            sellTower();
        } else if (canScrollPrev() && InputHandler.isButtonPressed(prevRect(), true)) {
            towerScrollOffset--;
        } else if (canScrollNext() && InputHandler.isButtonPressed(nextRect(), true)) {
            towerScrollOffset++;
        }
    }

    public static void render() {
        if(GameState.phase == GameState.Phase.BUILD || (GameState.phase == GameState.Phase.TRANSITION && GameState.transitionDelta < 0)) {
            drawButtons();
            drawText();
            drawSelection();
            changeSelColor();
        }
    }

    private static void drawTowerDescription(int index) {
        if(Settings.SHOW_TOOLTIPS) {
            Renderer.drawText(
                    towersInShop().get(index).getDescription(), Assets.descriptionFont, 20, BOT_GUI_HEIGHT + 20, Color.YELLOW, false, true, 10, DESCRIPTION_COLOR);
        }
    }

    private static void changeSelColor(){
        if(SEL_COLOR_DIFF > 0 || SEL_COLOR_DIFF < -MAX_SEL_COLOR_DIFF){
            SEL_COLOR_DELTA *= -1;
        }
        SEL_COLOR_DIFF += SEL_COLOR_DELTA;
        SEL_COLOR.set(DEFAULT_SEL_COLOR.r + 1f / 255f * SEL_COLOR_DIFF, DEFAULT_SEL_COLOR.g + 1f / 255f * SEL_COLOR_DIFF, DEFAULT_SEL_COLOR.b + 1f / 255f * SEL_COLOR_DIFF, DEFAULT_SEL_COLOR.a);
    }

    private static void drawSelection(){
        if(SELECTED_TOWER_IN_SHOP != -1){
            guiBatch.setColor(SEL_COLOR);
            Renderer.guiBatch.draw(Assets.shopSelection, buttonRect(SELECTED_TOWER_IN_SHOP).x, buttonRect(SELECTED_TOWER_IN_SHOP).y, buttonRect(SELECTED_TOWER_IN_SHOP).width, buttonRect(SELECTED_TOWER_IN_SHOP).height);
            guiBatch.setColor(Color.WHITE);
        }
    }

    private static void onBack(){
        if(!Chat.IS_CHAT_OPEN && SELECTED_TOWER_IN_GAME != -1 && Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)){
            backToTowers();
        }
    }

    private static void handleSelectingTowerInShopWithCursor(){
        for(int i = towerScrollOffset; i < towersInShop().size; i++){
            if (i < towerScrollOffset + MAX_TOWERS_VISIBLE && InputHandler.isButtonPressed(buttonRect(i + towerOffsetNum - towerScrollOffset), true)) {
                selectTowerInShop(i);
                return;
            }
        }
    }

    public static void selectTowerInShop(int i){
        if(SELECTED_TOWER_IN_SHOP == i)
            SELECTED_TOWER_IN_SHOP = -1;
        else {
            SELECTED_TOWER_IN_SHOP = i;
            if(SELECTED_TOWER_IN_GAME != -1 && SELECTED_TOWER_IN_SHOP != -1){ // Upgrading tower.
                if(Self.gold >= towersInShop().get(SELECTED_TOWER_IN_SHOP).cost) {
                    if (Networking.state == Networking.State.CLIENT) {
                        Sender.sendTower(towersInShop().get(SELECTED_TOWER_IN_SHOP).renderable.index, Networking.client.getID(), Level.towers.get(SELECTED_TOWER_IN_GAME).renderable.x, Level.towers.get(SELECTED_TOWER_IN_GAME).renderable.y);
                    } else {
                        if (Networking.state == Networking.State.SERVER) {
                            Networking.playerFrontend.get(0).gold -= towersInShop().get(SELECTED_TOWER_IN_SHOP).cost;
                            Sender.sendTower(towersInShop().get(SELECTED_TOWER_IN_SHOP).renderable.index, 0, Level.towers.get(SELECTED_TOWER_IN_GAME).renderable.x, Level.towers.get(SELECTED_TOWER_IN_GAME).renderable.y);
                        }
                        Self.gold -= towersInShop().get(SELECTED_TOWER_IN_SHOP).cost;
                        Tower.upgrade(towersInShop().get(SELECTED_TOWER_IN_SHOP).renderable.index, SELECTED_TOWER_IN_GAME, true);
                        getUpgradeTowers(SELECTED_TOWER_IN_GAME, true);
                        //backToTowers();
                    }
                }else{
                    SELECTED_TOWER_IN_SHOP = -1;
                }
            }
        }
    }

    private static void handleSelectingTowerInShopWithNumber(){
        if(!Chat.IS_CHAT_OPEN) {
            for (int i = 8; i <= 16; i++) { // Selecting a tower in the shop.
                if (Gdx.input.isKeyJustPressed(i)) {
                    int selectedIndex = i - 8;
                    if (selectedIndex < towersInShop().size + towerScrollOffset) {
                        selectTowerInShop(selectedIndex);
                    }
                    return;
                }
            }
        }
    }

    public static void buyTower(int shopIndex){
        if(Networking.state == Networking.State.CLIENT){
            Sender.sendTower(towersInShop().get(shopIndex).renderable.index, Networking.client.getID(), Self.selectedTile.x, Self.selectedTile.y);
        }
        else if(Self.gold >= towersInShop().get(shopIndex).cost) {
            if(Tower.canPlaceTower(Self.selectedTile.x, Self.selectedTile.y, false)){ // Placing tower.
                if(Networking.state == Networking.State.SERVER){
                    Networking.playerFrontend.get(0).gold -= towersInShop().get(shopIndex).cost;
                    Sender.sendTower(towersInShop().get(shopIndex).renderable.index, 0, Self.selectedTile.x, Self.selectedTile.y);
                }
                Self.gold -= towersInShop().get(shopIndex).cost;
                Tower.place(towersInShop().get(shopIndex).renderable.index, Self.selectedTile.x, Self.selectedTile.y, 0, true);
            }
        }
    }

    public static void backToTowers(){
        if(SELECTED_TOWER_IN_GAME != -1) {
            SELECTED_TOWER_IN_GAME = -1;
            SELECTED_TOWER_IN_SHOP = PREV_SELECTED_TOWER_IN_SHOP;
            upgradeTowers.clear();
            InputHandler.handleSelectingTile();
        }
    }

    public static void sellTower(){
        if(Networking.state == Networking.State.CLIENT){
            Sender.sendTower(-1, Networking.client.getID(), Self.selectedTile.x, Self.selectedTile.y);
        }
        else if(Self.selectedTile != null && Tower.exists(Self.selectedTile.x, Self.selectedTile.y) && Tower.get(Self.selectedTile.x, Self.selectedTile.y).ownerID == 0){
            if(Networking.state == Networking.State.SERVER){
                Networking.playerFrontend.get(0).gold += Tower.get(Self.selectedTile.x, Self.selectedTile.y).cost;
                Sender.sendTower(-1, 0, Self.selectedTile.x, Self.selectedTile.y);
            }
            Self.gold += Tower.get(Self.selectedTile.x, Self.selectedTile.y).cost;
            Tower.remove(Self.selectedTile.x, Self.selectedTile.y, true);
        }
    }

    private static void drawButtons(){
            for(int i = towerScrollOffset; i < towersInShop().size; i++){
                if(i < towerScrollOffset + MAX_TOWERS_VISIBLE)
                    drawTower(i);
            }
            if(canScrollPrev()){
                if(InputHandler.isButtonHovered(prevRect(), true)){
                    guiBatch.setColor(Color.GRAY);
                    guiBatch.draw(Assets.arrow, prevRect().x, prevRect().y, prevRect().width, prevRect().height, 0, 0, Assets.arrow.getWidth(), Assets.arrow.getHeight(), true, false);
                    guiBatch.setColor(Color.WHITE);
                }else
                    guiBatch.draw(Assets.arrow, prevRect().x, prevRect().y, prevRect().width, prevRect().height, 0, 0, Assets.arrow.getWidth(), Assets.arrow.getHeight(), true, false);
            }
            if(canScrollNext()){
                if(InputHandler.isButtonHovered(nextRect(), true)){
                    guiBatch.setColor(Color.GRAY);
                    guiBatch.draw(Assets.arrow, nextRect().x, nextRect().y, nextRect().width, nextRect().height);
                    guiBatch.setColor(Color.WHITE);
                }else
                    guiBatch.draw(Assets.arrow, nextRect().x, nextRect().y, nextRect().width, nextRect().height);
            }
    }

    private static void drawText(){
        if(SELECTED_TOWER_IN_GAME == -1) {
            Renderer.drawTextCenterOfScreen("Towers", Assets.shopFont, (int) (BOT_GUI_HEIGHT * 0.80f), Color.GOLD, false, true, Renderer.GUI_COLOR);
        }
        else
            Renderer.drawTextCenterOfScreen("Upgrades", Assets.shopFont, (int)(BOT_GUI_HEIGHT * 0.80f), Color.GOLD, false, true, Renderer.GUI_COLOR);
    }

    private static void drawTower(int i){
        if(SELECTED_TOWER_IN_SHOP == i)
            drawButton(Assets.shopTowers[towersInShop().get(i).renderable.index], i + towerOffsetNum - towerScrollOffset, towersInShop().get(i).name, SEL_COLOR);
        else
            drawButton(Assets.shopTowers[towersInShop().get(i).renderable.index], i + towerOffsetNum - towerScrollOffset, towersInShop().get(i).name);
        Renderer.drawTextCentered("Cost: " + towersInShop().get(i).cost, Assets.nameFont, (int)buttonRect(i + towerOffsetNum - towerScrollOffset).x + buttonSize / 2, Main.DESKTOP_HEIGHT - buttonY + 5, Color.GOLD, true);
    }

    private static void drawButton(Texture t, int i, String name){
        drawButton(t, i, name, Color.WHITE);
    }

    private static void drawButton(Texture t, int i, String name, Color textColor){
        if(buttonRect(i).contains(InputHandler.mousePos.x, Main.DESKTOP_HEIGHT - InputHandler.mousePos.y)) {
            drawTowerDescription(i);
            if (InputHandler.isButtonHovered(buttonRect(i), true)) {
                Renderer.guiBatch.setColor(Color.GRAY);
                Renderer.guiBatch.draw(t, buttonRect(i).x, buttonRect(i).y, buttonRect(i).width, buttonRect(i).height);
                Renderer.guiBatch.setColor(Color.WHITE);
                Renderer.drawTextCentered(name, Assets.nameFont, (int) buttonRect(i).x + buttonSize / 2, Main.DESKTOP_HEIGHT - buttonY - 7, Color.GRAY, true);
                return;
            }
        }
        Renderer.guiBatch.draw(t, buttonRect(i).x, buttonRect(i).y, buttonRect(i).width, buttonRect(i).height);
        Renderer.drawTextCentered(name, Assets.nameFont, (int) buttonRect(i).x + buttonSize / 2, Main.DESKTOP_HEIGHT - buttonY - 7, textColor, true);
    }

    private static Rectangle buttonRect(int i){
        return new Rectangle(buttonSize * i + (buttonSize / 2) * (i + towerOffsetNum) + buttonSize, buttonY, buttonSize, buttonSize);
    }

    private static Rectangle nextRect(){
        return new Rectangle(Main.DESKTOP_WIDTH - buttonRect(0).width / 2, buttonRect(0).y, buttonRect(0).width / 2, buttonRect(0).height);
    }

    private static Rectangle prevRect(){
        return new Rectangle(0, buttonRect(0).y, buttonRect(0).width / 2, buttonRect(0).height);
    }

    private static boolean canScrollNext(){
        return towersInShop().size > MAX_TOWERS_VISIBLE + towerScrollOffset;
    }

    private static boolean canScrollPrev(){
        return towerScrollOffset > 0;
    }
}
