package com.vnm.RemoteCtrl.G9;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import org.xmlpull.v1.XmlPullParser;

import android.os.Bundle;
import android.util.Xml;
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

		int osc_id = 1;

		switch (layoutId) {
		case LayoutID.kSchedule: {
			mMainLayout.setBackgroundResource(R.drawable.bg);

			for (Layer layer : mScheduleLayers) {
				// http://stackoverflow.com/questions/13351003/find-drawable-by-string
				int resId = getResources().getIdentifier(layer.name,
						"drawable", getPackageName());

				try {
					if (resId == R.drawable.schedule_button) {
						addButton("/", -1, layer.x, layer.y, resId,
								R.drawable.schedule, LayoutID.kSchedule);
					} else if (resId == R.drawable.programme_button) {
						addButton("/", -1, layer.x, layer.y, resId,
								R.drawable.programme, LayoutID.kProgramme);
					} else {
						addImage(layer.x, layer.y, resId);
					}
				} catch (Exception e) {
					LOGE(layer.name);
				}
			}
			break;
		}
		case LayoutID.kProgramme: {
			mMainLayout.setBackgroundResource(R.drawable.bg);

			for (Layer layer : mProgrammeLayers) {
				int resId = getResources().getIdentifier(layer.name,
						"drawable", getPackageName());

				try {
					if (resId == R.drawable.schedule) {
						addButton("/", -1, layer.x, layer.y,
								R.drawable.schedule_button,
								R.drawable.schedule, LayoutID.kSchedule);
					} else if (resId == R.drawable.programme) {
						addButton("/", -1, layer.x, layer.y,
								R.drawable.programme_button,
								R.drawable.programme, LayoutID.kProgramme);
					} else {
						addImage(layer.x, layer.y, resId);
					}
				} catch (Exception e) {
					LOGE(layer.name);
				}
			}
			break;
		}

		default:
			break;
		}
	}

	class Layer {
		String name = "";
		int x, y, w, h;
	}

	private ArrayList<Layer> parseXML(final String xmlName) {
		ArrayList<Layer> layers = null;
		try {
			InputStream in = getAssets().open(xmlName);
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in, null);

			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					layers = new ArrayList<Layer>();
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
						layers.add(aLayer);
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

		mScheduleLayers = parseXML("SCHEDULE.xml");
		mProgrammeLayers = parseXML("PROGRAMME.xml");
		mPreviewLayers = parseXML("PREVIEW.xml");

		setLayout(LayoutID.kSchedule);
	}

	private ArrayList<Layer> mScheduleLayers;
	private ArrayList<Layer> mProgrammeLayers;
	private ArrayList<Layer> mPreviewLayers;
}