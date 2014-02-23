package com.vnm.G9;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.view.View;

class AnimLayout {
	static final String kAnimSlotPrefix = "mov_";

	public ArrayList<Widget> mWidgets;
	final int kAnimCount = 10;
	private AnimSlot[] mAnimSlots = new AnimSlot[kAnimCount];

	public static AnimSlot getHitSlot(int x, int y, AnimSlot[] slots) {
		AnimSlot hit = null;
		for (int i = 0; i < slots.length; i++) {
			if (slots[i].widget.xmlRect.contains(x, y)) {
				hit = slots[i];
				break;
			}
		}
		return hit;
	}

	public void loadConfig(SharedPreferences settings) {
		for (int i = 0; i < AnimSlot.kCount; i++) {
			int id = settings.getInt(AnimSlot.kConfigKey + i, -1);
			mAnimSlots[i].setUserId(id);
		}
	}

	public void saveConfig(SharedPreferences.Editor editor) {
		for (int i = 0; i < AnimSlot.kCount; i++) {
			editor.putInt(AnimSlot.kConfigKey + i, mAnimSlots[i].widget.userId);
		}
	}

	public void createLayout() {

		if (mWidgets.size() == 0) {
			mWidgets = Widget.parseXML("PROGRAMME.xml");
		}
		for (Widget widget : mWidgets) {

			String name = widget.name;
			if (MainAct.sInstance.applyTopButtons(widget, name)) {
				continue;
			}

			int idOn = MainAct.sInstance.getDrawableByString(name + "_on");

			if (name.contains(kAnimSlotPrefix)) {
				int slot = Integer.parseInt(name.substring(
						kAnimSlotPrefix.length(), name.length())) - 1;
				widget.view = MainAct.sInstance.addButton("/debug/Anim", slot,
						widget.xmlRect.left, widget.xmlRect.top, idOn,
						widget.xmlResId);
				mAnimSlots[slot] = new AnimSlot(widget);
				mAnimSlots[slot].widget.userId = slot;
			} else if (name.equals("preview")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else if (name.equals("reset")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else if (name.equals("update")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else if (name.equals("loop_plus")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else if (name.equals("loop_minus")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else {
				widget.view = MainAct.sInstance.addImage(widget.xmlRect,
						widget.xmlResId);
			}
		}
	}
}
