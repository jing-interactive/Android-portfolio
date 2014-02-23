package com.vnm.G9;

import java.util.ArrayList;

import oscP5.OscMessage;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

class AnimLayout {
	static final String kAnimSlotPrefix = "mov_";
	Config[] mConfigs = new Config[Config.kCount];
	Config mCurrentConfig;

	public ArrayList<Widget> mWidgets;

	private AnimSlot[] mAnimSlots = new AnimSlot[AnimSlot.kCount];
	AnimSlot mCurrentAnimSlot;
	AnimConfig mCurrentAnimConfig;

	TextView mLoopCountView;

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

	AnimLayout() {
		for (int i = 0; i < mConfigs.length; i++) {
			mConfigs[i] = new Config();
		}

	}

	public void loadConfig(SharedPreferences settings) {
		for (int i = 0; i < mConfigs.length; i++) {
			mConfigs[i].loadConfig(settings, AnimSlot.kConfigKey + i);
		}
	}

	public void saveConfig(SharedPreferences.Editor editor) {
		for (int i = 0; i < mConfigs.length; i++) {
			mConfigs[i].saveConfig(editor, AnimSlot.kConfigKey + i);
		}
	}

	public void createLayout() {

		if (mWidgets == null) {
			mWidgets = new ArrayList<Widget>();
			mWidgets = Widget.parseXML("PROGRAMME.xml");
		}

		mCurrentConfig = mConfigs[Config.mSelectedId];

		for (Widget widget : mWidgets) {

			String name = widget.name;
			if (MainAct.sInstance.applyTopButtons(widget, name)) {
				continue;
			}

			final int idOn = MainAct.sInstance
					.getDrawableByString(name + "_on");

			if (name.contains(kAnimSlotPrefix)) {
				final int slot = Integer.parseInt(name.substring(
						kAnimSlotPrefix.length(), name.length())) - 1;
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId,
						new View.OnClickListener() {

							public void onClick(View v) {
								mCurrentAnimSlot = mAnimSlots[slot];
								for (int i = 0; i < mAnimSlots.length; i++) {
									Integer resOff = (Integer) mAnimSlots[i].widget.view
											.getTag(R.id.TAG_RES_OFF);
									mAnimSlots[i].widget.view
											.setBackgroundResource(resOff
													.intValue());
								}
								updateLayoutFromConfig();
								v.setBackgroundResource(idOn);
							}
						});
				widget.view.setTag(R.id.TAG_RES_ON, Integer.valueOf(idOn));
				widget.view.setTag(R.id.TAG_RES_OFF,
						Integer.valueOf(widget.xmlResId));

				mAnimSlots[slot] = new AnimSlot(widget);
				mAnimSlots[slot].widget.userId = slot;
			} else if (name.equals("preview")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else if (name.equals("reset")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId,
						new View.OnClickListener() {
							public void onClick(View v) {
								onReset();
							}
						});
			} else if (name.equals("update")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId,
						new View.OnClickListener() {
							public void onClick(View v) {
								onUpdate();
							}
						});
			} else if (name.equals("loop_plus")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId,
						new View.OnClickListener() {
							public void onClick(View v) {
								int loopCount = Integer
										.valueOf((String) mLoopCountView
												.getText());
								setLoopCount(Math.min(loopCount + 1, 10));
							}
						});
			} else if (name.equals("loop_minus")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId,
						new View.OnClickListener() {
							public void onClick(View v) {
								int loopCount = Integer
										.valueOf((String) mLoopCountView
												.getText());
								setLoopCount(Math.max(loopCount - 1, 1));
							}
						});
			} else if (name.equals("loop_no1")) {
				mLoopCountView = new TextView(MainAct.sInstance);
				// mLoopCountView.setTextSize(mLoopCountView.getTextSize() *
				// 1.5);
				Rect rc = widget.xmlRect;
				mLoopCountView.setLayoutParams(new AbsoluteLayout.LayoutParams(
						rc.width() * 2, rc.height(), rc.left, rc.top));

				MainAct.sInstance.mMainLayout.addView(mLoopCountView);
			} else {
				widget.view = MainAct.sInstance.addImage(widget.xmlRect,
						widget.xmlResId);
			}
		}

		{
			mCurrentAnimSlot = mAnimSlots[0];

			updateLayoutFromConfig();
			Integer idOn = (Integer) mCurrentAnimSlot.widget.view
					.getTag(R.id.TAG_RES_ON);
			mCurrentAnimSlot.widget.view.setBackgroundResource(idOn.intValue());
		}
	}

	void onReset(){
		int currentAnimIdx = mCurrentAnimSlot.widget.userId;
		mCurrentConfig.animConfigs[currentAnimIdx] = new AnimConfig();
		updateLayoutFromConfig();
		onUpdate();
	}
	
	void onUpdate() {
		mCurrentConfig.sendOscMsg(Config.mSelectedId);
	}

	void setLoopCount(int loopCount) {
		mCurrentAnimConfig.loopCount = loopCount;
		mLoopCountView.setText(String.valueOf(mCurrentAnimConfig.loopCount));
	}

	void updateLayoutFromConfig() {
		int currentAnimIdx = mCurrentAnimSlot.widget.userId;
		mCurrentAnimConfig = mCurrentConfig.animConfigs[currentAnimIdx];
		setLoopCount(mCurrentAnimConfig.loopCount);
	}
}
