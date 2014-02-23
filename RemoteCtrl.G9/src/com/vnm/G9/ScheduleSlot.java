package com.vnm.G9;

import oscP5.OscMessage;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;

class ScheduleSlot {
	static float sOffsetx, sOffsety;

	int mOscId;

	public ScheduleSlot(Widget aWidget, int oscId) {
		widget = aWidget;
		mOscId = oscId;

		widget.view.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				int x = (int) event.getRawX();
				int y = (int) event.getRawY();

				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN: {
					isMoving = true;
					widget.view.bringToFront();

					sOffsetx = widget.xmlRect.left - x;
					sOffsety = widget.xmlRect.top - y;

					return true;
				}
				case MotionEvent.ACTION_UP: {
					if (isMoving) {
						widget.view.setX(widget.xmlRect.left);
						widget.view.setY(widget.xmlRect.top);

						ScheduleSlot hitHourSlot = getHitSlot(x, y, MainAct.sInstance.mHourSlots);
						if (hitHourSlot != null) {
							hitHourSlot.setProgramId(widget.userId);
						}

						isMoving = false;

						// this value is visible to all the three layouts
						HourlyProgram.mSelectedId = widget.userId;

						if (widget.userId != -1) {
							MainAct.sInstance.mProgrammeSceneBtn.setVisibility(View.VISIBLE);
						} else {
							MainAct.sInstance.mProgrammeSceneBtn.setVisibility(View.INVISIBLE);
						}
						for (ScheduleSlot slot : MainAct.sInstance.mHourSlots) {
							if (widget.userId != -1
									&& (slot.widget.userId == widget.userId)) {
								slot.setSelected(true);
							} else {
								slot.setSelected(false);
							}
						}

						// TODO: combine them
						for (ScheduleSlot slot : MainAct.sInstance.mProgrammeSlots) {
							if (widget.userId != -1
									&& (slot.widget.userId == widget.userId)) {
								slot.setSelected(true);
							} else {
								slot.setSelected(false);
							}
						}

						// TODO: highlight
					}
					return true;
				}
				case MotionEvent.ACTION_MOVE: {
					if (isMoving) {
						widget.view.setX(sOffsetx + x);
						widget.view.setY(sOffsety + y);
					}

					return true;
				}
				}
				return true;
			}

			/**
			 * @param x
			 * @param y
			 * @param hitHourSlot
			 * @return
			 */
			private ScheduleSlot getHitSlot(int x, int y, ScheduleSlot[] slots) {
				ScheduleSlot hit = null;
				for (int i = 0; i < slots.length; i++) {
					if (slots[i].widget.xmlRect.contains(x, y)) {
						hit = MainAct.sInstance.mHourSlots[i];
						break;
					}
				}
				return hit;
			}
		});
	}

	final int kLedPort = 4444;

	// TODO: kProgrammeColors -> userIds?
	public void setProgramId(int id) {
		if (id <= -1)
			id = -1;
		if (id > MainAct.sInstance.kProgramCount - 1)
			id = MainAct.sInstance.kProgramCount - 1;
		widget.userId = id;
		isSelected = true;
		setSelected(false);
		OscMessage m = new OscMessage("/schedule");
		m.add(mOscId);
		m.add(id);
		for (String ip : MainAct.sInstance.mRemoteIps)
			MainAct.sInstance.mOscServer.send(m, ip, kLedPort);
	}

	public void setSelected(boolean selected) {
		if (isSelected != selected) {
			isSelected = selected;
			int clr = MainAct.sInstance.kProgrammeColors[widget.userId + 1]; // HACK: -1 = ?
			if (!selected) {
				clr = (150 << 24) | (clr & 0x00ffffff);
			}
			widget.view.setImageDrawable(new ColorDrawable(clr));
		}
	}

	boolean isSelected;
	boolean isMoving; // user is moving the slot
	Widget widget;
}


class HourlyProgram {
	static int mSelectedId = -1;

	public HourlyProgram() {
		isInteractive = false;
		isRandomAnimation = false;
		animConfigs = new AnimConfig[kAnimCount];
		for (int i = 0; i < kAnimCount; i++) {
			animConfigs[i] = new AnimConfig();
		}
	}

	final int kAnimCount = 10;

	boolean isInteractive;
	boolean isRandomAnimation;

	class AnimConfig {
		int loopCount = 1;
		boolean isRandom = false;
		int temprature = 100;
		boolean enabled = true;
	}

	AnimConfig[] animConfigs;
}
