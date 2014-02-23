package com.vnm.G9;

import oscP5.OscMessage;
import android.view.View;

class AnimConfig {
	int loopCount = 1;
	boolean isRandom = false;
	int temprature = 100;
	boolean enabled = true;
}

class AnimSlot {
	final static int kCount = 10;
	static public String kConfigKey = "kAnimConfigKey";
	
	static int mSelectedId = -1;

	public AnimSlot(Widget aWidget) {
		widget = aWidget;

		widget.view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSelectedId = widget.userId;
			}
		});
	}

	// TODO: kProgrammeColors -> userIds?
	public void setUserId(int id) {
		if (id <= -1)
			id = -1;
		if (id > kCount - 1)
			id = kCount - 1;
		widget.userId = id;
		
		{
			OscMessage m = new OscMessage("/anim");
			m.add(mOscId);
			m.add(id);
			for (String ip : MainAct.sInstance.mRemoteIps)
				MainAct.sInstance.mOscServer.send(m, ip, MainAct.sInstance.kLedPort);
		}
	}
	
	int mOscId;
	Widget widget;
}
