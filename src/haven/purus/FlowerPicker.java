package haven.purus;

import haven.*;
import haven.automation.GobSelectCallback;
import haven.automation.WItemDestroyCallback;
import haven.purus.pbot.PBotUtils;

import java.awt.*;
import java.util.ArrayList;

public class FlowerPicker implements Runnable, ItemClickCallback, WItemDestroyCallback {

	private GameUI gui;
	private String itemName;
	private boolean itemDestroyed = false;

	public FlowerPicker(GameUI gui) {
		this.gui = gui;
	}

	@Override
	public void run() {
		PBotUtils.sysMsg("Click an item in inventory");
		synchronized (GobSelectCallback.class) {
			BotUtils.gui.registerItemCallback(this);
		}
		while(itemName == null)
			PBotUtils.sleep(5);
		ArrayList<WItem> itmList = new ArrayList<>();
		synchronized(gui.ui) {
			for(Widget w : gui.ui.widgets.values()) {
				if(w instanceof Inventory) {
					for(Widget witm = w.child; witm != null; witm = witm.next) {
						synchronized(witm) {
							if(witm instanceof WItem && ((WItem) witm).item.getname().equals(itemName)) {
								itmList.add((WItem)witm);
							}
						}
					}
				}
			}
		}
		for(WItem itm:itmList) {
			if(itm.parent != null && ((Inventory)itm.parent).wmap.containsKey(itm.item)) {
				itm.item.wdgmsg("iact", itm.c, 3);
				int timeout = 1000/5; // 1 second
				int retries = 0;
				while(gui.ui.root.findchild(FlowerMenu.class) == null) {
					if(retries == timeout) {
						break;
					}
					BotUtils.sleep(5);
					retries++;
				}
				itm.registerDestroyCallback(this);
				itemDestroyed = false;
				synchronized(gui.ui) {
					FlowerMenu menu = gui.ui.root.findchild(FlowerMenu.class);
					if(menu != null) {
						menu.choose(menu.opts[0]);
					} else {
						continue;
					}
				}
				while(gui.ui.root.findchild(FlowerMenu.class) != null && !itemDestroyed) {
					BotUtils.sleep(5);
				}
				synchronized(gui.ui) {
					FlowerMenu menu = gui.ui.root.findchild(FlowerMenu.class);
					if(menu != null)
						menu.destroy();
				}
			}
		}
		BotUtils.sysMsg("FlowerPicker done!", Color.GREEN);
	}

	@Override
	public void itemClick(WItem item) {
		synchronized (GobSelectCallback.class) {
			BotUtils.gui.unregisterItemCallback();
		}
		itemName = item.item.getname();

	}

	@Override
	public void notifyDestroy() {
		itemDestroyed = true;
	}
}
