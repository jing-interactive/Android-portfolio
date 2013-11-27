package com.vnm.RemoteCtrl.G9;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

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
		mMainLayout = new AbsoluteLayout(this);
		super.setContentView(mMainLayout);

		switch (layoutId) {
		case LayoutID.kSchedule: {
			mMainLayout.setBackgroundResource(R.drawable.bg);
			mMainLayout.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					int x = (int) event.getX();
					int y = (int) event.getY();
					switch (event.getActionMasked()) {
					case MotionEvent.ACTION_DOWN: {
						// theme hit
						mDraggingThemeSlot = null;
						for (int i = 0; i < mThemeSlots.length; i++) {
							if (mThemeSlots[i].widget.rect.contains(x, y)) {
								mDraggingThemeSlot = mThemeSlots[i];
								mDraggingThemeSlot.widget.view.bringToFront();
								mHitTimeSlot = null;
								break;
							}
						}

						// time hit
						if (mDraggingThemeSlot == null) {
							for (int i = 0; i < mTimeSlots.length; i++) {
								if (mTimeSlots[i].widget.rect.contains(x, y)) {
									mHitTimeSlot = mTimeSlots[i];
									mHitTimeSlot.widget.view.setSelected(true);
									break;
								}
							}
						}
						mSourceX = x;
						mSourceY = y;
						break;
					}
					case MotionEvent.ACTION_UP: {
						if (mDraggingThemeSlot != null) {
							mDraggingThemeSlot.widget.view
									.setX(mDraggingThemeSlot.widget.rect.left);
							mDraggingThemeSlot.widget.view
									.setY(mDraggingThemeSlot.widget.rect.top);

							if (mHitTimeSlot != null) {
								if (mDraggingThemeSlot.widget.resId == R.drawable.delet_button_hl) {
									mHitTimeSlot.widget.view
											.setImageResource(mHitTimeSlot.widget.resId);
								} else {
									mHitTimeSlot.widget.view
											.setImageDrawable(new ColorDrawable(
													kThemeColors[mDraggingThemeSlot.id]));
								}
							}

							mDraggingThemeSlot = null;
						}
						break;
					}
					case MotionEvent.ACTION_MOVE: {
						for (int i = 0; i < mTimeSlots.length; i++) {
							if (mTimeSlots[i].widget.rect.contains(x, y)) {
								mHitTimeSlot = mTimeSlots[i];
								break;
							}
						}
						if (mDraggingThemeSlot != null) {
							float dx = mDraggingThemeSlot.widget.rect.left
									- mSourceX;
							float dy = mDraggingThemeSlot.widget.rect.top
									- mSourceY;

							mDraggingThemeSlot.widget.view.setX(dx + x);
							mDraggingThemeSlot.widget.view.setY(dy + y);
						}

						mTargetX = x;
						mTargetY = y;
						break;
					}
					}
					return true;
				}
			});

			for (Widget widget : mScheduleWidgets) {

				String name = widget.name;
				if (mInvisibleWidgets.contains(name)) {
					continue;
				}

				try {
					widget.view = addImage(widget.rect.left, widget.rect.top,
							widget.resId);
				} catch (Exception e) {
					LOGE(widget.name);
				}
				if (name.contains(kTimeSlotPrefix)) {
					int slot = Integer.parseInt(name.substring(
							kTimeSlotPrefix.length(), name.length()));
					mTimeSlots[slot - 1] = new TimeSlot(widget);
				} else if (name.contains(kThemeSlotPrefix)) {
					int slot = Integer.parseInt(name.substring(
							kThemeSlotPrefix.length(), name.length()));
					mThemeSlots[slot - 1] = new ThemeSlot(widget);
					mThemeSlots[slot - 1].id = slot - 1;
				} else if (name.equals("delet_button_hl")) {
					mThemeSlots[kThemeSlotCount - 1] = new ThemeSlot(widget);
					mThemeSlots[kThemeSlotCount - 1].id = kThemeSlotCount - 1;
				} else if (name.equals("SCHEDULE_BUTTON")) {

				} else if (name.equals("PROGRAMME_BUTTON")) {

				}
			}

			for (ThemeSlot slot : mThemeSlots) {
				slot.widget.view.bringToFront();
			}

			break;
		}
		case LayoutID.kProgramme: {
			mMainLayout.setBackgroundResource(R.drawable.bg);

			break;
		}

		default:
			break;
		}
	}

	class Widget {
		ImageView view;
		String name = "";
		String imageName = "";
		int resId = -1;
		Rect rect = new Rect();
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
						widget.rect.set(x, y, x + w, y + h);

						// http://stackoverflow.com/questions/13351003/find-drawable-by-string
						widget.resId = getResources().getIdentifier(
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

	// TimeSlot
	final int kTimeSlotCount = 16;
	private TimeSlot mHitTimeSlot;

	class TimeSlot {
		public TimeSlot(Widget aWidget) {
			widget = aWidget;
		}

		Widget widget;
		int themeId;
	}

	private TimeSlot[] mTimeSlots = new TimeSlot[kTimeSlotCount];
	final String kTimeSlotPrefix = "fade_color_";

	// ThemeSlot
	final int kThemeSlotCount = 7;
	private ThemeSlot mDraggingThemeSlot;

	int[] kThemeColors = { Color.rgb(255, 114, 0), Color.rgb(255, 6, 123),
			Color.rgb(193, 6, 255), Color.rgb(123, 13, 247),
			Color.rgb(63, 13, 247), Color.rgb(3, 115, 253), Color.BLACK };

	class ThemeSlot {
		public ThemeSlot(Widget aWidget) {
			widget = aWidget;
		}

		boolean isInteractive;
		boolean isRandomAnimation;
		Widget widget;
		int id;
	}

	private ThemeSlot[] mThemeSlots = new ThemeSlot[kThemeSlotCount];
	final String kThemeSlotPrefix = "color_button";

	// Line
	private float mSourceX, mSourceY;
	private float mTargetX, mTargetY;
}