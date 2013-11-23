package com.vnm.RemoteCtrl.template;

import android.os.Bundle;
import android.widget.AbsoluteLayout;

@SuppressWarnings("deprecation")
public class MainAct extends RemoteCtrl.BaseActivity {

	class LayoutID {
		static final int Scene = 0; // 首页
		static final int Menu = 1; // 交互主界面
	}

	protected int getClientCount() {
		return 1;
	}

	public void setLayout(int layoutId) {
		main_layout = new AbsoluteLayout(this);
		super.setContentView(main_layout);

		int osc_id = 1;

		switch (layoutId) {
		case LayoutID.Scene: {// 首页
			main_layout.setBackgroundResource(R.drawable.bg_intro);

//			int x0 = 388;
//			int dx = 353;
//			int y0 = 328;
//			String tag = "/scene";

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setLayout(LayoutID.Scene);
	}
}