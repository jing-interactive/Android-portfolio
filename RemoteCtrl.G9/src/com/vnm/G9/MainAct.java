package com.vnm.G9;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressWarnings("deprecation")
public class MainAct extends RemoteCtrl.BaseActivity {

	class LayoutID {
		static final int kSchedule = 0;
		static final int kProgramme = 1;
		static final int kPreview = 2;
	}

	protected int getClientCount() {
		return 1;
	}

	public void setLayout(int layoutId) {
		if (mCurrentLayout == layoutId)
			return;

		mMainLayout = new AbsoluteLayout(this);
		super.setContentView(mMainLayout);

		mCurrentLayout = layoutId;

		switch (layoutId) {
		case LayoutID.kSchedule: {
			createSchduleLayout();
			break;
		}
		case LayoutID.kProgramme: {
			createProgrammeLayout();
			break;
		}

		default:
			break;
		}

		mMainLayout.setBackgroundResource(R.drawable.bg);

		setButtonPair(mScheduleBtn, mScheduleOffBtn, LayoutID.kSchedule,
				mProgrammeBtn, mProgrammeOffBtn, LayoutID.kProgramme);

		switch (layoutId) {
		case LayoutID.kSchedule: {
			mScheduleOffBtn.setVisibility(View.INVISIBLE);
			break;
		}
		case LayoutID.kProgramme: {
			mProgrammeOffBtn.setVisibility(View.INVISIBLE);
			break;
		}
		}

		loadConfig();
	}

	private void setButtonPair(final ImageView btn1, final ImageView btn1_off,
			final int scene1, final ImageView btn2, final ImageView btn2_off,
			final int scene2) {

		btn1.setVisibility(View.INVISIBLE);
		btn2.setVisibility(View.INVISIBLE);

		btn1_off.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (scene1 >= 0) {
					saveConfig();
					setLayout(scene1);
				}
				btn1_off.setVisibility(View.INVISIBLE);
				btn2_off.setVisibility(View.VISIBLE);

				btn1.setVisibility(View.VISIBLE);
				btn2.setVisibility(View.INVISIBLE);
			}
		});
		btn2_off.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (scene2 >= 0) {
					saveConfig();
					setLayout(scene2);
				}
				btn2_off.setVisibility(View.INVISIBLE);
				btn1_off.setVisibility(View.VISIBLE);

				btn2.setVisibility(View.VISIBLE);
				btn1.setVisibility(View.INVISIBLE);
			}
		});
	}

	private void createProgrammeLayout() {

		for (Widget widget : mProgrammeWidgets) {

			String name = widget.name;
			if (mInvisibleWidgets.contains(name)) {
				continue;
			}

			try {
				widget.view = addImage(widget.savedRect, widget.savedResId);
			} catch (Exception e) {
				LOGE(widget.name);
			}
			if (name.contains(kHourSlotPrefix)) {
				int slot = Integer.parseInt(name.substring(
						kHourSlotPrefix.length(), name.length()));
				mHourSlots[slot - 1] = new ScheduleSlot(widget);
			} else if (name.contains(kProgrammeSlotPrefix)) {
				int slot = Integer.parseInt(name.substring(
						kProgrammeSlotPrefix.length(), name.length()));
				mProgrammeSlots[slot - 1] = new ScheduleSlot(widget);
				mProgrammeSlots[slot - 1].widget.userId = slot - 1;
			} else if (name.equals("delet_button_hl")) {
				mProgrammeSlots[kProgrammeSlotCount - 1] = new ScheduleSlot(
						widget);
				mProgrammeSlots[kProgrammeSlotCount - 1].widget.userId = kProgrammeSlotCount - 1;
			} else {
				applyTopButtons(widget, name);
			}
		}
	}

	/**
	 * @param widget
	 * @param name
	 */
	private void applyTopButtons(Widget widget, String name) {
		if (name.equals("schedule")) {
			mScheduleBtn = widget.view;
		} else if (name.equals("programme")) {
			mProgrammeBtn = widget.view;
		}
		if (name.equals("schedule_off")) {
			mScheduleOffBtn = widget.view;
		} else if (name.equals("programme_off")) {
			mProgrammeOffBtn = widget.view;
		}
	}

	private void createSchduleLayout() {

		for (Widget widget : mScheduleWidgets) {

			String name = widget.name;
			if (mInvisibleWidgets.contains(name)) {
				continue;
			}

			try {
				widget.view = addImage(widget.savedRect, widget.savedResId);
			} catch (Exception e) {
				LOGE(widget.name);
			}

			// create two types of slots
			if (name.contains(kHourSlotPrefix)) {
				int slot = Integer.parseInt(name.substring(
						kHourSlotPrefix.length(), name.length()));
				mHourSlots[slot - 1] = new ScheduleSlot(widget);
			} else if (name.contains(kProgrammeSlotPrefix)) {
				int slot = Integer.parseInt(name.substring(
						kProgrammeSlotPrefix.length(), name.length()));
				mProgrammeSlots[slot - 1] = new ScheduleSlot(widget);
				mProgrammeSlots[slot - 1].widget.userId = slot - 1;
			} else if (name.equals("delet_button_hl")) {
				mProgrammeSlots[kProgrammeSlotCount - 1] = new ScheduleSlot(
						widget);
				mProgrammeSlots[kProgrammeSlotCount - 1].widget.userId = kProgrammeSlotCount - 1;
			} else {
				applyTopButtons(widget, name);
			}
		}

		for (ScheduleSlot slot : mProgrammeSlots) {
			slot.widget.view.bringToFront();
		}
	}

	private void loadConfig() {
		SharedPreferences settings = getSharedPreferences(kConfigName,
				MODE_PRIVATE);
		for (int i = 0; i < kHourSlotCount; i++) {
			int id = settings.getInt(kScheduleTimeProgrammeKey + i,
					kProgrammeSlotCount - 1);
			mHourSlots[i].widget.setUserId(id);
		}
		for (int i = 0; i < kProgrammeSlotCount - 1; i++) {
			mProgrammeSlots[i].widget.setUserId(i);
		}
	}

	class Widget {
		// TODO: kProgrammeColors -> userIds?
		public void setUserId(int id) {
			userId = id;
			isSelected = true;
			setSelected(false);
		}

		public void setSelected(boolean selected) {
			if (isSelected != selected) {
				isSelected = selected;
				int clr = kProgrammeColors[userId];
				if (!selected) {
					clr = (150 << 24) | (clr & 0x00ffffff);
				}
				view.setImageDrawable(new ColorDrawable(clr));
			}
		}

		boolean isSelected;

		ImageView view;
		String name = "";
		String imageName = "";

		int userId = 0;
		int savedResId = -1;
		Rect savedRect = new Rect();
		int savedZOrder = 0;
	}

	private ArrayList<Widget> parseXML(final String xmlName) {
		ArrayList<Widget> widgets = null;
		try {
			InputStream in = getAssets().open(xmlName);
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in, null);

			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					widgets = new ArrayList<Widget>();
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equals("layer")) {
						Widget widget = new Widget();
						widget.name = parser.getAttributeValue(null, "name")
								.toLowerCase(Locale.ENGLISH);
						String line = parser
								.getAttributeValue(null, "position");
						String[] sublines = line.split(", ");
						int x = Integer.parseInt(sublines[0]);
						int y = Integer.parseInt(sublines[1]);
						int w = Integer.parseInt(parser.getAttributeValue(null,
								"layerwidth"));
						int h = Integer.parseInt(parser.getAttributeValue(null,
								"layerheight"));
						if (widget.name.contains(kHourSlotPrefix)) {
							// HACK: fix bugs in layout xml
							x += 3;
							y += 4;
							w -= 5;
							h -= 6;
						}
						widget.savedRect.set(x, y, x + w, y + h);

						// http://stackoverflow.com/questions/13351003/find-drawable-by-string
						widget.savedResId = getResources().getIdentifier(
								widget.name, "drawable", getPackageName());

						widgets.add(widget);
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return widgets;
	}

	protected void onPause() {
		super.onPause();
		saveConfig();
	}

	/**
	 * 
	 */
	private void saveConfig() {
		SharedPreferences settings = getSharedPreferences(kConfigName,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		for (int i = 0; i < kHourSlotCount; i++) {
			editor.putInt(kScheduleTimeProgrammeKey + i,
					mHourSlots[i].widget.userId);
		}
		editor.commit();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		String[] invisibleArray = { "bg", "bg_copy" };
		mInvisibleWidgets = new HashSet<String>(Arrays.asList(invisibleArray));

		mScheduleWidgets = parseXML("SCHEDULE.xml");
		mProgrammeWidgets = parseXML("PROGRAMME.xml");
		mPreviewWidgets = parseXML("PREVIEW.xml");

		setLayout(LayoutID.kSchedule);
	}

	private ArrayList<Widget> mScheduleWidgets;
	private ArrayList<Widget> mProgrammeWidgets;
	private ArrayList<Widget> mPreviewWidgets;

	private Set<String> mInvisibleWidgets;

	// HourSlot
	final int kHourSlotCount = 16;

	private ScheduleSlot[] mHourSlots = new ScheduleSlot[kHourSlotCount];
	final String kHourSlotPrefix = "fade_color_";

	// ProgrammeSlot
	final int kProgrammeSlotCount = 7;

	int[] kProgrammeColors = { Color.rgb(255, 114, 0), Color.rgb(255, 6, 123),
			Color.rgb(193, 6, 255), Color.rgb(123, 13, 247),
			Color.rgb(63, 13, 247), Color.rgb(3, 115, 253),
			Color.rgb(40, 13, 126) };

	class ProgrammeSettings {
		boolean isInteractive;
		boolean isRandomAnimation;
	}

	class ScheduleSlot {
		public ScheduleSlot(Widget aWidget) {
			widget = aWidget;

			widget.view.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					int x = (int) event.getRawX();
					int y = (int) event.getRawY();

					switch (event.getActionMasked()) {
					case MotionEvent.ACTION_DOWN: {
						isMoving = true;
						widget.view.bringToFront();

						mOffsetx = widget.savedRect.left - x;
						mOffsety = widget.savedRect.top - y;

						return true;
					}
					case MotionEvent.ACTION_UP: {
						if (isMoving) {
							widget.view.setX(widget.savedRect.left);
							widget.view.setY(widget.savedRect.top);

							ScheduleSlot hitHourSlot = getHitSlot(x, y,
									mHourSlots);
							if (hitHourSlot != null) {
								hitHourSlot.widget.setUserId(widget.userId);
							}

							isMoving = false;
							
							for (ScheduleSlot slot : mHourSlots) {
								if (widget.userId != kProgrammeSlotCount -1 && (slot.widget.userId == widget.userId)) {
									slot.widget.setSelected(true);
								} else {
									slot.widget.setSelected(false);
								}
							}
							
							// TODO: combine them
							for (ScheduleSlot slot : mProgrammeSlots) {
								if (widget.userId != kProgrammeSlotCount -1 && (slot.widget.userId == widget.userId)) {
									slot.widget.setSelected(true);
								} else {
									slot.widget.setSelected(false);
								}
							}
							
							// TODO: highlight 
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

				/**
				 * @param x
				 * @param y
				 * @param hitHourSlot
				 * @return
				 */
				private ScheduleSlot getHitSlot(int x, int y,
						ScheduleSlot[] slots) {
					ScheduleSlot hit = null;
					for (int i = 0; i < slots.length; i++) {
						if (slots[i].widget.savedRect.contains(x, y)) {
							hit = mHourSlots[i];
							break;
						}
					}
					return hit;
				}
			});
		}

		boolean isMoving; // user is moving the slot
		Widget widget;
	}

	float mOffsetx, mOffsety;

	private ScheduleSlot[] mProgrammeSlots = new ScheduleSlot[kProgrammeSlotCount];
	final String kProgrammeSlotPrefix = "color_button";

	// Save/Load
	private String kConfigName = "CONFIG";
	private String kScheduleTimeProgrammeKey = "kScheduleTimeProgrammeKey";

	private ImageView mScheduleBtn, mProgrammeBtn;
	private ImageView mScheduleOffBtn, mProgrammeOffBtn;

	private int mCurrentLayout = -1;
}