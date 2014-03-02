package com.vnm.G9;

import oscP5.OscMessage;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;

class ScheduleSlot {
	static int kCount = 16;

	float mOffsetx, mOffsety;
	static public String kConfigKey = "kScheduleTimeProgrammeKey";
	int mOscId = -1;
	int[] kProgrammeColors = { Color.rgb(40, 13, 126), Color.rgb(255, 114, 0),
			Color.rgb(255, 6, 123), Color.rgb(193, 6, 255),
			Color.rgb(123, 13, 247), Color.rgb(63, 13, 247),
			Color.rgb(3, 115, 253), };

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

					mOffsetx = widget.xmlRect.left - x;
					mOffsety = widget.xmlRect.top - y;

					return true;
				}
				case MotionEvent.ACTION_UP: {
					if (isMoving) {
						widget.view.setX(widget.xmlRect.left);
						widget.view.setY(widget.xmlRect.top);

						ScheduleSlot hitHourSlot = ScheduleLayout
								.getHitSlot(
										x,
										y,
										MainAct.sInstance.mScheduleLayout.mHourSlots);
						if (hitHourSlot != null) {
							hitHourSlot.setUserId(widget.userId);
						}

						isMoving = false;

						// this value is visible to all the three layouts
						Config.mSelectedId = widget.userId;

						if (widget.userId != -1) {
							MainAct.sInstance.mProgrammeSceneBtn
									.setVisibility(View.VISIBLE);
						} else {
							MainAct.sInstance.mProgrammeSceneBtn
									.setVisibility(View.INVISIBLE);
						}

						ScheduleLayout.updateHighlitSlots();
					}
					return true;
				}
				case MotionEvent.ACTION_MOVE: {
					if (isMoving) {
						widget.view.setX(mOffsetx + x);
						widget.view.setY(mOffsety + y);
					}

					return true;
				}
				}
				return true;
			}
		});
	}

	// TODO: kProgrammeColors -> userIds?
	public void setUserId(int id) {
		id = Math.max(id, -1);
		id = Math.min(id, Config.kCount - 1);

		widget.userId = id;
		setSelected(false);
	}

	public void setSelected(boolean selected) {
		int clr = kProgrammeColors[widget.userId + 1]; // HACK: -1 = ?
		if (!selected) {
			clr = (150 << 24) | (clr & 0x00ffffff);
		}
		widget.view.setImageDrawable(new ColorDrawable(clr));
	}

	boolean isSelected;
	boolean isMoving; // user is moving the slot
	Widget widget;

	public void sendOscMsg() {
		OscMessage m = new OscMessage("/schedule");
		m.add(mOscId);
		m.add(widget.userId);
		for (String ip : MainAct.sInstance.mRemoteIps)
			MainAct.sInstance.mOscServer.send(m, ip, MainAct.kLedPort);
	}
}
