package com.rontoking.rontotd.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.rontoking.rontotd.Main;
import com.rontoking.rontotd.game.systems.Assets;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = Main.DESKTOP_WIDTH;
		config.height = Main.DESKTOP_HEIGHT;
		config.title = Main.DESKTOP_TITLE;

		config.addIcon(Assets.iconPath + "/icon512.png", Files.FileType.Local);
		config.addIcon(Assets.iconPath + "/icon256.png", Files.FileType.Local);
		config.addIcon(Assets.iconPath + "/icon128.png", Files.FileType.Local);
		config.addIcon(Assets.iconPath + "/icon32.png", Files.FileType.Local);
		config.addIcon(Assets.iconPath + "/icon16.png", Files.FileType.Local);
		config.resizable = Main.IS_RESIZABLE;

		new LwjglApplication(new Main(), config);
	}
}

