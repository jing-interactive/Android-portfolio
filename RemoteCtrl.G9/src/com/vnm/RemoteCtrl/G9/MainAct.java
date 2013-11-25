package com.vnm.RemoteCtrl.G9;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;

import android.os.Bundle;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsoluteLayout;

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
					LOGW(event.toString());
					return true;
				}
			});

			for (HashMap.Entry<String, Layer> entry : mScheduleLayers
					.entrySet()) {

				String name = entry.getKey();
				if (mInvisibleLayers.contains(name)) {
					continue;
				}

				Layer layer = entry.getValue();
				if (name.contains(kTimeSlotPrefix)) {
					int slot = Integer.parseInt(name.substring(
							kTimeSlotPrefix.length(), name.length()));
					mTimeSlots[slot - 1] = new TimeSlot(layer);
				} else if (name.contains(kThemeSlotPrefix)) {
					int slot = Integer.parseInt(name.substring(
							kThemeSlotPrefix.length(), name.length()));
					mThemeSlots[slot - 1] = new ThemeSlot(layer);
				}

				try {
					addImage(layer.x, layer.y, layer.resId);
				} catch (Exception e) {
					LOGE(layer.name);
				}
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

	class Layer {
		String name = "";
		String imageName = "";
		int resId = -1;
		int x, y, w, h;
	}

	private Map<String, Layer> parseXML(final String xmlName) {
		Map<String, Layer> layers = null;
		try {
			InputStream in = getAssets().open(xmlName);
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in, null);

			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					layers = new HashMap<String, Layer>();
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equals("layer")) {
						Layer aLayer = new Layer();
						aLayer.name = parser.getAttributeValue(null, "name")
								.toLowerCase(Locale.ENGLISH);
						String line = parser
								.getAttributeValue(null, "position");
						String[] sublines = line.split(", ");
						aLayer.x = Integer.parseInt(sublines[0]);
						aLayer.y = Integer.parseInt(sublines[1]);
						aLayer.w = Integer.parseInt(parser.getAttributeValue(
								null, "layerwidth"));
						aLayer.h = Integer.parseInt(parser.getAttributeValue(
								null, "layerheight"));

						// http://stackoverflow.com/questions/13351003/find-drawable-by-string
						aLayer.resId = getResources().getIdentifier(
								aLayer.name, "drawable", getPackageName());

						layers.put(aLayer.name, aLayer);
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return layers;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		String[] invisibleArray = { "bg", "bg_copy" };
		mInvisibleLayers = new HashSet<String>(Arrays.asList(invisibleArray));

		mScheduleLayers = parseXML("SCHEDULE.xml");
		mProgrammeLayers = parseXML("PROGRAMME.xml");
		mPreviewLayers = parseXML("PREVIEW.xml");

		setLayout(LayoutID.kSchedule);
	}

	private Map<String, Layer> mScheduleLayers;
	private Map<String, Layer> mProgrammeLayers;
	private Map<String, Layer> mPreviewLayers;

	private Set<String> mInvisibleLayers;

	//
	final int kTimeSlotCount = 16;

	class TimeSlot {
		public TimeSlot(Layer aLayer) {
			layer = aLayer;
		}

		Layer layer;
		int themeId;
	}

	private TimeSlot[] mTimeSlots = new TimeSlot[kTimeSlotCount];
	final String kTimeSlotPrefix = "fade_color_";

	//
	final int kThemeSlotCount = 6;

	class ThemeSlot {
		public ThemeSlot(Layer aLayer) {
			layer = aLayer;
		}

		boolean isInteractive;
		boolean isRandomAnimation;
		Layer layer;
		int id;
	}

	private ThemeSlot[] mThemeSlots = new ThemeSlot[kThemeSlotCount];
	final String kThemeSlotPrefix = "color_button";
}