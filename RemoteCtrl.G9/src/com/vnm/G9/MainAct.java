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
			mScheduleSceneBtn.setBackgroundResource(R.drawable.schedule_on);
			mProgrammeSceneBtn.setVisibility(View.INVISIBLE);
			break;
		}
		case LayoutID.kAnim: {
			mAnimLayout.createLayout();
			mScheduleSceneBtn.setBackgroundResource(R.drawable.schedule_off);
			mProgrammeSceneBtn.setBackgroundResource(R.drawable.programme_on);
			mProgrammeSceneBtn.setVisibility(View.VISIBLE);
			break;
		}

		default:
			break;
		}

		mMainLayout.setBackgroundResource(R.drawable.bg);

		loadConfig();

		if (mCurrentLayout == LayoutID.kSchedule) {
			ScheduleLayout.updateHighlitSlots();
		}
	}

	public boolean applyTopButtons(Widget widget, String name) {
		if (name.equals("schedule")) {
			mScheduleSceneBtn = addButton(widget.xmlRect.left,
					widget.xmlRect.top, R.drawable.schedule_off,
					R.drawable.schedule_on, null);
			mScheduleSceneBtn.setOnTouchListener(null);
			mScheduleSceneBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (mCurrentLayout != LayoutID.kSchedule) {
						v.setBackgroundResource(R.drawable.schedule_on);
						saveConfig();
						setLayout(LayoutID.kSchedule);
					}
				}
			});
			return true;
		} else if (name.equals("programme")) {
			mProgrammeSceneBtn = addButton(widget.xmlRect.left,
					widget.xmlRect.top, R.drawable.programme_off,
					R.drawable.programme_on, null);
			mProgrammeSceneBtn.setVisibility(View.INVISIBLE);
			mProgrammeSceneBtn.setOnTouchListener(null);
			mProgrammeSceneBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (mCurrentLayout != LayoutID.kAnim) {
						saveConfig();
						setLayout(LayoutID.kAnim);
					}
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
