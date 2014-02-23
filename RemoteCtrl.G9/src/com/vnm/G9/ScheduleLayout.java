package com.vnm.G9;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.view.View;

class ScheduleLayout {
	static final String kHourSlotPrefix = "fade_color_";

	public ArrayList<Widget> mWidgets;

	ScheduleSlot[] mProgramSlots = new ScheduleSlot[Config.kCount];
	ScheduleSlot mEraseProgramSlot;
	final String kProgramSlotPrefix = "color_button";

	// HourSlot
	public ScheduleSlot[] mHourSlots = new ScheduleSlot[ScheduleSlot.kCount];

	public static ScheduleSlot getHitSlot(int x, int y, ScheduleSlot[] slots) {
		ScheduleSlot hit = null;
		for (int i = 0; i < slots.length; i++) {
			if (slots[i].widget.xmlRect.contains(x, y)) {
				hit = slots[i];
				break;
			}
		}
		return hit;
	}

	public void loadConfig(SharedPreferences settings) {
		for (int i = 0; i < ScheduleSlot.kCount; i++) {
			int id = settings.getInt(ScheduleSlot.kConfigKey + i, -1);
			mHourSlots[i].setUserId(id);
		}
		for (int i = 0; i < Config.kCount; i++) {
			mProgramSlots[i].setUserId(i);
		}
	}

	public void saveConfig(SharedPreferences.Editor editor) {
		for (int i = 0; i < ScheduleSlot.kCount; i++) {
			editor.putInt(ScheduleSlot.kConfigKey + i,
					mHourSlots[i].widget.userId);
		}
	}

	public void createLayout() {

		if (mWidgets == null) {
			mWidgets = new ArrayList<Widget>();
			mWidgets = Widget.parseXML("SCHEDULE.xml");
		}
		
		for (Widget widget : mWidgets) {

			String name = widget.name;

			if (MainAct.sInstance.applyTopButtons(widget, name)) {
				continue;
			}

			try {
				widget.view = MainAct.sInstance.addImage(widget.xmlRect,
						widget.xmlResId);
			} catch (Exception e) {
				MainAct.LOGE(widget.name);
			}

			// create two types of slots
			if (name.contains(kHourSlotPrefix)) {
				int slot = Integer.parseInt(name.substring(
						kHourSlotPrefix.length(), name.length()));
				// fade_color_1 -> hour: 10
				// ...
				// fade_color_15 -> hour: 0
				// fade_color_16 -> hour: 1
				mHourSlots[slot - 1] = new ScheduleSlot(widget, (slot + 9) % 24);
			} else if (name.contains(kProgramSlotPrefix)) {
				int slot = Integer.parseInt(name.substring(
						kProgramSlotPrefix.length(), name.length()));
				// MOV_06 -> slot: 5
				mProgramSlots[slot - 1] = new ScheduleSlot(widget, -1);
				mProgramSlots[slot - 1].widget.userId = slot - 1;
			} else if (name.equals("delet_button_hl")) {
				// -1 means invalid progId
				mEraseProgramSlot = new ScheduleSlot(widget, -1);
				mEraseProgramSlot.widget.userId = -1;
			}
		}

		for (ScheduleSlot slot : mProgramSlots) {
			slot.widget.view.bringToFront();
		}

		MainAct.sInstance.mProgrammeSceneBtn.setVisibility(View.INVISIBLE);
	}
}
