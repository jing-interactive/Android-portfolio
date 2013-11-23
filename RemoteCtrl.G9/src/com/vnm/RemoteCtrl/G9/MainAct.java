package com.vnm.RemoteCtrl.G9;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import org.xmlpull.v1.XmlPullParser;

import android.graphics.Rect;
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
				int resId = getResources().getIdentifier(layer.name, "drawable", getPackageName());
				
				try {
					addButton("/", -1, layer.rect.left, layer.rect.top, resId, resId);					
				} catch (Exception e) {
					LOGE(layer.name);
				}
			}

			// int x0 = 388;
			// int dx = 353;
			// int y0 = 328;
			// String tag = "/scene";

			// addButton(tag, osc_id++, x0, y0, R.drawable.dj1,
			// R.drawable.dj1b);// 欢迎画面
			// addButton(tag, osc_id++, x0 += dx, y0, R.drawable.dj3,
			// R.drawable.dj3b, LayoutID.Menu);// 交互系统

			break;
		}

		default:
			break;
		}
	}

	class Layer {
		String name = "";
		Rect rect = new Rect();
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
						aLayer.name = parser.getAttributeValue(null, "name").toLowerCase(Locale.ENGLISH);
						String line = parser.getAttributeValue(null, "position");
						String[] sublines = line.split(", ");
						int x = Integer.parseInt(sublines[0]);
						int y = Integer.parseInt(sublines[1]);
						int w = Integer.parseInt(parser.getAttributeValue(null, "layerwidth"));
						int h = Integer.parseInt(parser.getAttributeValue(null, "layerheight"));
						aLayer.rect.set(x, y, x + w, y + h);
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