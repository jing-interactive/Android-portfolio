package com.vnm.G9;

import android.view.View;

class AnimSlot {
	static final int kCount = 11;
	static final int kKinectSlot = kCount - 1;
	static public String kConfigKey = "kAnimConfig";

	static int mSelectedId = -1;

	public AnimSlot(Widget aWidget) {
		widget = aWidget;

		widget.view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mSelectedId = widget.userId;
			}
		});
	}

	Widget widget;
}
