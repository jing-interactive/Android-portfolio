package com.vnm.G9;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

class AnimLayout {
	static final String kAnimSlotPrefix = "mov_";
	static final String kAnimEnablePrefix = "move_on_";
	Config[] mConfigs = new Config[Config.kCount];
	Config mCurrentConfig;

	public ArrayList<Widget> mWidgets;

	private AnimSlot[] mAnimSlots = new AnimSlot[AnimSlot.kCount];
	AnimSlot mCurrentAnimSlot;
	AnimConfig mCurrentAnimConfig;

	// TODO: replace fields with HashMap<String, View>
	TextView mLoopCountView;
	Widget mSliderButton;

	View mMovieText, mInteractionText;

	View mRandomOn, mRandomOff;
	View mEnabledOn, mEnabledOff;

	View mLoopPlus, mLoopMinus;

	private View[] mEnableFlagViews = new View[AnimSlot.kCount];

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

	int mSliderStartX = 40;
	int mSliderEndX = 40 + 610;

	float mOffsetX;

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

			// RANDOM ON / OFF
			if (name.equals("random_on")) {
				mRandomOn = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, R.drawable.on_on, R.drawable.on_on,
						null);
				mRandomOn.setOnTouchListener(null);
				mRandomOn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (!mCurrentAnimConfig.isRandom) {
							setIsRandom(true);
						}
					}
				});
			} else if (name.equals("random_off")) {
				mRandomOff = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, R.drawable.off_on,
						R.drawable.off_off, null);
				mRandomOff.setOnTouchListener(null);
				mRandomOff.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (mCurrentAnimConfig.isRandom) {
							setIsRandom(false);
						}
					}
				});
			}
			// MOVIE ON / OFF
			else if (name.equals("movie_on")) {
				mEnabledOn = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, R.drawable.on_on, R.drawable.on_on,
						null);
				mEnabledOn.setOnTouchListener(null);
				mEnabledOn.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (!mCurrentAnimConfig.isEnabled) {
							setIsEnabled(true);
						}
					}
				});
			} else if (name.equals("movie_off")) {
				mEnabledOff = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top, R.drawable.off_on,
						R.drawable.off_off, null);
				mEnabledOff.setOnTouchListener(null);
				mEnabledOff.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (mCurrentAnimConfig.isEnabled) {
							setIsEnabled(false);
						}
					}
				});
			} else if (name.equals("lighting_triangle")) {
				widget.view = MainAct.sInstance.addButton(widget.xmlRect.left,
						widget.xmlRect.top - 75, R.drawable.light_slider,
						R.drawable.light_slider, null);
				mSliderButton = widget;
				widget.view.bringToFront();
				widget.view.setOnTouchListener(new View.OnTouchListener() {
					public boolean onTouch(View v, MotionEvent event) {
						int x = (int) event.getRawX();
						switch (event.getActionMasked()) {
						case MotionEvent.ACTION_DOWN: {
							mOffsetX = mSliderButton.xmlRect.left - x;
						}
						case MotionEvent.ACTION_MOVE: {
							if (x >= mSliderStartX && x <= mSliderEndX) {
								setSlider((x - mSliderStartX)
										/ (float) (mSliderEndX - mSliderStartX));
							}
							return true;
						}
						}

						return false;
					}
				});
			} else if (name.contains(kAnimSlotPrefix)
					|| name.equals("interaction_fade")) {
				final int slot = name.equals("interaction_fade") ? AnimSlot.kKinectSlot
						: Integer.parseInt(name.substring(
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
				mLoopPlus = widget.view;
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
				mLoopMinus = widget.view;
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

				if (name.contains(kAnimEnablePrefix)
						|| name.equals("interaction_on")) {
					final int slot = name.equals("interaction_on") ? AnimSlot.kKinectSlot
							: Integer.parseInt(name.substring(
									kAnimEnablePrefix.length(), name.length())) - 1;
					mEnableFlagViews[slot] = widget.view;
					widget.view.setVisibility(View.INVISIBLE);
				} else if (name.equals("movie")) {
					mMovieText = widget.view;
				} else if (name.equals("interaction")) {
					mInteractionText = widget.view;
					mInteractionText.setVisibility(View.INVISIBLE);
				}
			}
		}

		{
			mCurrentAnimSlot = mAnimSlots[0];

			updateLayoutFromConfig();
			Integer idOn = (Integer) mCurrentAnimSlot.widget.view
					.getTag(R.id.TAG_RES_ON);
			mCurrentAnimSlot.widget.view.setBackgroundResource(idOn.intValue());
		}

		{
			for (int i = 0; i < AnimSlot.kCount; i++) {
				mEnableFlagViews[i]
						.setVisibility(mCurrentConfig.animConfigs[i].isEnabled ? View.VISIBLE
								: View.INVISIBLE);

			}
		}
	}

	// TODO: pop up YES_NO dialog box
	void onReset() {
		int currentAnimIdx = mCurrentAnimSlot.widget.userId;
		mCurrentConfig.animConfigs[currentAnimIdx] = new AnimConfig();
		updateLayoutFromConfig();
		onUpdate();
	}

	// TODO: detect OSC feedback
	void onUpdate() {
		mCurrentConfig.sendOscMsg(Config.mSelectedId);
	}

	void showLoopGroup(boolean visible) {
		int vis = visible ? View.VISIBLE : View.INVISIBLE;
		mLoopCountView.setVisibility(vis);
		mLoopPlus.setVisibility(vis);
		mLoopMinus.setVisibility(vis);
	}

	void setLoopCount(int loopCount) {
		mCurrentAnimConfig.loopCount = loopCount;
		mLoopCountView.setText(String.valueOf(mCurrentAnimConfig.loopCount));
	}

	void setSlider(float ratio) {
		mCurrentAnimConfig.lightValue = ratio;
		mSliderButton.view.setX(ratio * (mSliderEndX - mSliderStartX)
				+ mSliderStartX);
		// TODO: send real-time slider value??
		// mCurrentConfig.sendOscMsg(Config.mSelectedId);
	}

	void setIsEnabled(boolean flag) {
		mCurrentAnimConfig.isEnabled = flag;
		mEnabledOn.setBackgroundResource(flag ? R.drawable.on_on
				: R.drawable.on_off);
		mEnabledOff.setBackgroundResource(flag ? R.drawable.off_off
				: R.drawable.off_on);
		mEnabledOn.bringToFront();
		mEnabledOff.bringToFront();

		int currentAnimIdx = mCurrentAnimSlot.widget.userId;
		mEnableFlagViews[currentAnimIdx].setVisibility(flag ? View.VISIBLE
				: View.INVISIBLE);
	}

	void setIsRandom(boolean flag) {
		mCurrentAnimConfig.isRandom = flag;
		mRandomOn.setBackgroundResource(flag ? R.drawable.on_on
				: R.drawable.on_off);
		mRandomOff.setBackgroundResource(flag ? R.drawable.off_off
				: R.drawable.off_on);
		mRandomOn.bringToFront();
		mRandomOff.bringToFront();
	}

	void updateLayoutFromConfig() {
		int currentAnimIdx = mCurrentAnimSlot.widget.userId;
		if (currentAnimIdx == AnimSlot.kKinectSlot) {
			mInteractionText.setVisibility(View.VISIBLE);
			mMovieText.setVisibility(View.INVISIBLE);
		} else {
			mMovieText.setVisibility(View.VISIBLE);
			mInteractionText.setVisibility(View.INVISIBLE);
		}
		mCurrentAnimConfig = mCurrentConfig.animConfigs[currentAnimIdx];
		setLoopCount(mCurrentAnimConfig.loopCount);
		setSlider(mCurrentAnimConfig.lightValue);
		setIsRandom(mCurrentAnimConfig.isRandom);
		setIsEnabled(mCurrentAnimConfig.isEnabled);
	}
}
