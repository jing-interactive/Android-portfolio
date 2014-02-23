package com.vnm.G9;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;

import android.graphics.Rect;
import android.util.Xml;
import android.widget.ImageView;

class Widget {

	ImageView view;
	String name = "";

	int userId = 0; // can be anything
	int xmlResId = -1;
	Rect xmlRect = new Rect();
	int savedZOrder = 0;
	
	static public ArrayList<Widget> parseXML(final String xmlName) {
		ArrayList<Widget> widgets = null;
		try {
			InputStream in = MainAct.sInstance.getAssets().open(xmlName);
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
						if (widget.name.contains(ScheduleLayout.kHourSlotPrefix)) {
							// HACK: fix bugs in layout xml
							x += 3;
							y += 4;
							w -= 5;
							h -= 6;
						}
						widget.xmlRect.set(x, y, x + w, y + h);
						widget.xmlResId = MainAct.sInstance.getDrawableByString(widget.name);

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

}
