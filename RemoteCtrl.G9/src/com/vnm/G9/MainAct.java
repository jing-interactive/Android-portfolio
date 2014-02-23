package com.vnm.G9;

import java.util.ArrayList;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainAct extends RemoteCtrl.BaseActivity {
	static MainAct sInstance;

	protected String getAppAboutMe() {
		return "G9 Mobile Control.";
	}

	class LayoutID {
		static final int kSchedule = 0;
		static final int kAnim = 1;
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
			mScheduleLayout.createLayout();
			break;
		}
		case LayoutID.kAnim: {
			mAnimLayout.createLayout();
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

	public boolean applyTopButtons(Widget widget, String name) {
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
							setLayout(LayoutID.kAnim);
						}
					});
			return true;
		}

		return false;
	}

	protected void onPause() {
		super.onPause();
		saveConfig();
	}

	void loadConfig() {
		SharedPreferences settings = getSharedPreferences(kConfigName,
				MODE_PRIVATE);
		mScheduleLayout.loadConfig(settings);
		mAnimLayout.loadConfig(settings);
	}

	void saveConfig() {
		SharedPreferences settings = getSharedPreferences(kConfigName,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		mScheduleLayout.saveConfig(editor);
		mAnimLayout.saveConfig(editor);

		editor.commit();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		sInstance = this;

		super.onCreate(savedInstanceState);

		mPreviewWidgets = Widget.parseXML("PREVIEW.xml");

		setLayout(LayoutID.kSchedule);
	}

	public ScheduleLayout mScheduleLayout = new ScheduleLayout();
	public AnimLayout mAnimLayout = new AnimLayout();

	ArrayList<Widget> mPreviewWidgets;

	public final static int kLedPort = 4444;
	public final static String kConfigName = "CONFIG";

	public ImageView mScheduleSceneBtn, mProgrammeSceneBtn;

	int mCurrentLayout = -1;
}
