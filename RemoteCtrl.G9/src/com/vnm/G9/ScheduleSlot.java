package com.vnm.G9;

import oscP5.OscMessage;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

class ScheduleSlot {
	static int kCount = 16;

	float mOffsetx, mOffsety;
	static public String kConfigKey = "kScheduleTimeProgrammeKey";
	int mOscId = -1;
	int[] kProgrammeColors = { Color.rgb(40, 13, 126), Color.rgb(255, 114, 0),
			Color.rgb(255, 6, 123), Color.rgb(193, 6, 255),
			Color.rgb(123, 13, 247), Color.rgb(63, 13, 247),
			Color.rgb(3, 115, 253), };

	ImageView mBonusLayer;
	int mDeleteCount = 0;

	public ScheduleSlot(Widget aWidget, int oscId) {
		widget = aWidget;
		mOscId = oscId;

		mBonusLayer = MainAct.sInstance.addImage(aWidget.xmlRect, 0);
		mBonusLayer.setVisibility(View.INVISIBLE);

		widget.view.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				int x = (int) event.getRawX();
				int y = (int) event.getRawY();

				ScheduleSlot hitHourSlot = ScheduleLayout.getHitSlot(x, y,
						MainAct.sInstance.mScheduleLayout.mHourSlots);

				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN: {

					if (mDeleteCount == 3) {
						setUserId(-1);
					} else if (mDeleteCount == 2) {
						mBonusLayer
								.setBackgroundResource(R.drawable.color_delete);
					}

					for (ScheduleSlot slot : MainAct.sInstance.mScheduleLayout.mHourSlots) {
						if (slot.mOscId != mOscId) {
							slot.mDeleteCount = 0;
						}
					}

					if (hitHourSlot != null && hitHourSlot.mOscId == mOscId) {
						mDeleteCount++;
					}

					isMoving = widget.userId != -1;
					widget.view.bringToFront();
					mBonusLayer.bringToFront();

					mOffsetx = widget.xmlRect.left - x;
					mOffsety = widget.xmlRect.top - y;

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

					return true;
				}
				case MotionEvent.ACTION_UP: {
					if (isMoving) {
						isMoving = false;

						widget.view.setX(widget.xmlRect.left);
						widget.view.setY(widget.xmlRect.top);
						mBonusLayer.setX(widget.view.getX());
						mBonusLayer.setY(widget.view.getY());

						if (hitHourSlot != null) {
							hitHourSlot.setUserId(widget.userId);
						}
						ScheduleLayout.updateHighlitSlots();
					}
					return true;
				}
				case MotionEvent.ACTION_MOVE: {
					if (isMoving) {
						widget.view.setX(mOffsetx + x);
						widget.view.setY(mOffsety + y);
						mBonusLayer.setX(widget.view.getX());
						mBonusLayer.setY(widget.view.getY());
					}

					return true;
				}
				}
				return true;
			}
		});
	}

	public void setUserId(int id) {
		id = Math.max(id, -1);
		id = Math.min(id, Config.kCount - 1);

		widget.userId = id;
		setSelected(false);
	}

	public void setSelected(boolean selected) {
		int clr = kProgrammeColors[widget.userId + 1]; // HACK: -1 = ?
		isSelected = selected;
		if (isSelected) {
			mBonusLayer.setVisibility(View.VISIBLE);
			mBonusLayer
					.setBackgroundResource(mDeleteCount == 3 ? R.drawable.color_delete
							: R.drawable.color_select);
			mBonusLayer.bringToFront();
		} else {
			mBonusLayer.setVisibility(View.INVISIBLE);
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
			MainAct.sInstance.mOscServer.send(m, ip,
					MainAct.sInstance.getRemotePort());
	}
}
