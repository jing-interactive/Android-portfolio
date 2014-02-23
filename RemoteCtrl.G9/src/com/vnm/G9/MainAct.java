package com.vnm.G9;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressWarnings("deprecation")
public class MainAct extends RemoteCtrl.BaseActivity {
	static MainAct sInstance;

	protected String getAppAboutMe() {
		return "G9 Mobile Control.";
	}

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
			createScheduleLayout();
			break;
		}
		case LayoutID.kProgramme: {
			try {
				createProgrammeLayout();
			} catch (Exception e) {
				LOGE("<createProgrammeLayout> " + e.toString());
			}
			break;
		}

		default:
			break;
		}

		mMainLayout.setBackgroundResource(R.drawable.bg);

		// setButtonPair(mScheduleBtn, mScheduleSceneBtn, LayoutID.kSchedule,
		// mProgrammeBtn, mProgrammeSceneBtn, LayoutID.kProgramme);

		// mScheduleSceneBtn.setVisibility(View.INVISIBLE);
		// mProgrammeSceneBtn.setVisibility(View.INVISIBLE);

		// switch (layoutId) {
		// case LayoutID.kSchedule: {
		// mScheduleSceneBtn.setVisibility(View.INVISIBLE);
		// break;
		// }
		// case LayoutID.kProgramme: {
		// mProgrammeSceneBtn.setVisibility(View.INVISIBLE);
		// break;
		// }
		// }

		loadConfig();
	}

	private void createProgrammeLayout() {

		for (Widget widget : mProgramWidgets) {

			String name = widget.name;

			if (applyTopButtons(widget, name)) {
				continue;
			}

			int idOn = getDrawableByString(name + "_on");

			if (name.contains(kAnimSlotPrefix)) {
				int slot = Integer.parseInt(name.substring(
						kAnimSlotPrefix.length(), name.length())) - 1;
				widget.view = addButton("/debug/Anim", slot,
						widget.xmlRect.left, widget.xmlRect.top, idOn,
						widget.xmlResId);
				mAnimSlots[slot] = new AnimSlot(widget);
				mAnimSlots[slot].widget.userId = slot;
			} else if (name.equals("preview")) {
				widget.view = addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else if (name.equals("reset")) {
				widget.view = addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else if (name.equals("update")) {
				widget.view = addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else if (name.equals("loop_plus")) {
				widget.view = addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else if (name.equals("loop_minus")) {
				widget.view = addButton(widget.xmlRect.left,
						widget.xmlRect.top, idOn, widget.xmlResId, null);
			} else {
				widget.view = addImage(widget.xmlRect, widget.xmlResId);
			}
		}
	}

	/**
	 * @param widget
	 * @param name
	 */
	private boolean applyTopButtons(Widget widget, String name) {
		// if (name.equals("schedule")) {
		// mScheduleBtn = widget.view;
		// } else if (name.equals("programme")) {
		// mProgrammeBtn = widget.view;
		// }
		if (name.equals("schedule_off")) {
			mScheduleSceneBtn = widget.view = addToggleButton("", -1, -1,
					widget.xmlRect.left, widget.xmlRect.top,
					R.drawable.schedule_off, R.drawable.schedule_on,
					new View.OnClickListener() {
						public void onClick(View v) {
							saveConfig();
							setLayout(LayoutID.kSchedule);
						}
					});
			return true;
		} else if (name.equals("programme_off")) {
			mProgrammeSceneBtn = widget.view = addToggleButton("", -1, -1,
					widget.xmlRect.left, widget.xmlRect.top,
					R.drawable.programme_off, R.drawable.programme_on,
					new View.OnClickListener() {
						public void onClick(View v) {
							saveConfig();
							setLayout(LayoutID.kProgramme);
						}
					});
			return true;
		}

		return false;
	}

	private void createScheduleLayout() {

		for (Widget widget : mScheduleWidgets) {

			String name = widget.name;

			if (applyTopButtons(widget, name)) {
				continue;
			}

			try {
				widget.view = addImage(widget.xmlRect, widget.xmlResId);
			} catch (Exception e) {
				LOGE(widget.name);
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
			} else if (name.contains(kProgrammeSlotPrefix)) {
				int slot = Integer.parseInt(name.substring(
						kProgrammeSlotPrefix.length(), name.length()));
				// MOV_06 -> slot: 5
				mProgrammeSlots[slot - 1] = new ScheduleSlot(widget, slot - 1);
				mProgrammeSlots[slot - 1].widget.userId = slot - 1;
			} else if (name.equals("delet_button_hl")) {
				// -1 means invalid progId
				mEraseProgramSlot = new ScheduleSlot(widget, -1);
				mEraseProgramSlot.widget.userId = -1;
			}
		}

		for (ScheduleSlot slot : mProgrammeSlots) {
			slot.widget.view.bringToFront();
		}

		mProgrammeSceneBtn.setVisibility(View.INVISIBLE);
	}

	private void loadConfig() {
		SharedPreferences settings = getSharedPreferences(kConfigName,
				MODE_PRIVATE);
		for (int i = 0; i < kHourSlotCount; i++) {
			int id = settings.getInt(kScheduleTimeProgrammeKey + i, -1);
			mHourSlots[i].setProgramId(id);
		}
		for (int i = 0; i < kProgramCount; i++) {
			mProgrammeSlots[i].setProgramId(i);
		}
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
						widget.xmlRect.set(x, y, x + w, y + h);
						widget.xmlResId = getDrawableByString(widget.name);

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

		sInstance = this;
		
		super.onCreate(savedInstanceState);

		mScheduleWidgets = parseXML("SCHEDULE.xml");
		mProgramWidgets = parseXML("PROGRAMME.xml");
		mPreviewWidgets = parseXML("PREVIEW.xml");

		setLayout(LayoutID.kSchedule);
	}

	private ArrayList<Widget> mScheduleWidgets;
	private ArrayList<Widget> mProgramWidgets;
	private ArrayList<Widget> mPreviewWidgets;

	// HourSlot
	final int kHourSlotCount = 16;

	public ScheduleSlot[] mHourSlots = new ScheduleSlot[kHourSlotCount];
	final String kHourSlotPrefix = "fade_color_";

	// ProgrammeSlot
	final int kProgramCount = 6;

	int[] kProgrammeColors = { Color.rgb(40, 13, 126), Color.rgb(255, 114, 0),
			Color.rgb(255, 6, 123), Color.rgb(193, 6, 255),
			Color.rgb(123, 13, 247), Color.rgb(63, 13, 247),
			Color.rgb(3, 115, 253), };


	private HourlyProgram[] mPrograms = new HourlyProgram[kProgramCount];
	ScheduleSlot[] mProgrammeSlots = new ScheduleSlot[kProgramCount];
	ScheduleSlot mEraseProgramSlot;
	final String kProgrammeSlotPrefix = "color_button";

	// Save/Load
	private String kConfigName = "CONFIG";
	private String kScheduleTimeProgrammeKey = "kScheduleTimeProgrammeKey";

	public ImageView mScheduleSceneBtn, mProgrammeSceneBtn;
	final String kAnimSlotPrefix = "mov_";

	final int kAnimCount = 10;
	private AnimSlot[] mAnimSlots = new AnimSlot[kAnimCount];

	private int mCurrentLayout = -1;
}
