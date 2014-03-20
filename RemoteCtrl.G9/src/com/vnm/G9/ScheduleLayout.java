package com.vnm.G9;

import java.util.ArrayList;

import oscP5.OscMessage;

import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;

class ScheduleLayout {
	static final String kHourSlotPrefix = "fade_color_";

	public ArrayList<Widget> mWidgets;

	ScheduleSlot[] mProgramSlots = new ScheduleSlot[Config.kCount];
	ScheduleSlot mEraseProgramSlot;
	final String kProgramSlotPrefix = "color_button";

	View mWorldOn, mWorldOff, mWorldAuto;
	int mWorldStatus = 0; // 0, 1, 2

	// HourSlot
	public ScheduleSlot[] mHourSlots = new ScheduleSlot[ScheduleSlot.kCount];

	static public void updateHighlitSlots() {
		ScheduleSlot[][] scheduleSlots = {
				MainAct.sInstance.mScheduleLayout.mHourSlots,
				MainAct.sInstance.mScheduleLayout.mProgramSlots };

		for (ScheduleSlot slot : MainAct.sInstance.mScheduleLayout.mProgramSlots) {
			slot.mDeleteCount = 0;
		}

		for (ScheduleSlot[] slots : scheduleSlots) {
			for (ScheduleSlot slot : slots) {
				if (Config.mSelectedId != -1
						&& (slot.widget.userId == Config.mSelectedId)) {
					slot.setSelected(true);
				} else {
					slot.setSelected(false);
					slot.mDeleteCount = 0;
				}
			}
		}

		MainAct.sInstance.mProgrammeSceneBtn
				.setBackgroundResource(R.drawable.programme_off);
		MainAct.sInstance.mProgrammeSceneBtn
				.setVisibility(Config.mSelectedId == -1 ? View.INVISIBLE
						: View.VISIBLE);
	}

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

	// TODO: detect OSC feedback
	void onUpdate() {
		for (ScheduleSlot slot : mHourSlots) {
			slot.sendOscMsg();
		}
		MainAct.sInstance.sendAckMessage();
	}

	void setWorldStatus(int flag) {
		mWorldStatus = flag;
		if (mWorldStatus == 0) {
			MainAct.sInstance.showDarkLayer(false);
			mWorldOn.setBackgroundResource(R.drawable.world_on_on);
			mWorldOff.setBackgroundResource(R.drawable.world_off_off);
			mWorldAuto.setBackgroundResource(R.drawable.world_auto_off);

			MainAct.sInstance.sendCmd("/WORLD_VISIBLE", 1);
		} else if (mWorldStatus == 1) {
			MainAct.sInstance.showDarkLayer(true);

			mWorldOn.setBackgroundResource(R.drawable.world_on_off);
			mWorldOff.setBackgroundResource(R.drawable.world_off_on);
			mWorldAuto.setBackgroundResource(R.drawable.world_auto_off);

			MainAct.sInstance.sendCmd("/WORLD_VISIBLE", 0);
		} else {
			MainAct.sInstance.showDarkLayer(true);

			mWorldOn.setBackgroundResource(R.drawable.world_on_off);
			mWorldOff.setBackgroundResource(R.drawable.world_off_off);
			mWorldAuto.setBackgroundResource(R.drawable.world_auto_on);

			MainAct.sInstance.sendCmd("/WORLD_AUTO", 1);
		}
		mWorldOn.bringToFront();
		mWorldOff.bringToFront();
		mWorldAuto.bringToFront();
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

			final int idOn = MainAct.sInstance
					.getDrawableByString(name + "_on");

			if (name.equals("update")) {
				MainAct.sInstance.removeView(widget.view);
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId,
						new View.OnClickListener() {
							public void onClick(View v) {
								onUpdate();
							}
						});
			} else // RANDOM ON / OFF
			if (name.equals("world_on_on")) {
				mWorldOn = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, R.drawable.world_on_on,
						R.drawable.world_on_on, null);
				mWorldOn.setOnTouchListener(null);
				mWorldOn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (mWorldStatus != 0) {
							setWorldStatus(0);
						}
					}
				});
			} else if (name.equals("world_off_off")) {
				mWorldOff = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, R.drawable.world_off_off,
						R.drawable.world_off_off, null);
				mWorldOff.setOnTouchListener(null);
				mWorldOff.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (mWorldStatus != 1) {
							setWorldStatus(1);
						}
					}
				});
			} else if (name.equals("world_auto_off")) {
				mWorldAuto = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, R.drawable.world_auto_off,
						R.drawable.world_auto_off, null);
				mWorldAuto.setOnTouchListener(null);
				mWorldAuto.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (mWorldStatus != 2) {
							setWorldStatus(2);
						}
					}
				});
			}
			// create two types of slots
			else if (name.contains(kHourSlotPrefix)) {
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

		setWorldStatus(mWorldStatus);
	}
}
