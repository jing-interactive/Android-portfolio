package com.vnm.G9;

import java.util.ArrayList;

import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscStatus;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainAct extends RemoteCtrl.BaseActivity {
	static MainAct sInstance;

	View mDarkLayer;
	View mUpdateSucess, mUpdateFail;

	protected String getAppAboutMe() {
		return "G9 Mobile Control.";
	}

	class LayoutID {
		static final int kSchedule = 0;
		static final int kAnim = 1;
		static final int kPreview = 2;
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

		mDarkLayer = addImage(0, 0, R.drawable.dark_black);
		mDarkLayer.setVisibility(View.INVISIBLE);
		mDarkLayer.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		mUpdateSucess = addImage(336, 315, R.drawable.update_successfully);
		mUpdateSucess.setVisibility(View.INVISIBLE);
		mUpdateSucess.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				showDarkLayer(false);
				return true;
			}
		});

		mUpdateFail = addImage(336, 315, R.drawable.update__failed);
		mUpdateFail.setVisibility(View.INVISIBLE);
		mUpdateFail.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				showDarkLayer(false);
				return true;
			}
		});

		mMainLayout.setBackgroundResource(R.drawable.bg);

		loadConfig();

		if (mCurrentLayout == LayoutID.kSchedule) {
			ScheduleLayout.updateHighlitSlots();
		}
	}

	public void showUpdate(boolean success) {
		showDarkLayer(true);
		if (success) {
			mUpdateSucess.bringToFront();
			mUpdateSucess.setVisibility(View.VISIBLE);
		} else {
			mUpdateFail.bringToFront();
			mUpdateFail.setVisibility(View.VISIBLE);
		}
	}

	public void showDarkLayer(boolean visible) {
		if (mDarkLayer == null) {
			return;
		}

		if (visible) {
			mDarkLayer.setVisibility(View.VISIBLE);
			mDarkLayer.bringToFront();
		} else {
			mDarkLayer.setVisibility(View.INVISIBLE);
			mUpdateSucess.setVisibility(View.INVISIBLE);
			mUpdateFail.setVisibility(View.INVISIBLE);
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

		mOscServer.addListener(new OscEventListener() {

			public void oscEvent(OscMessage m) {
				LOGI(" addr: " + m.addrPattern());
				if (m.checkAddrPattern("/msgBox")) {
					showUpdate(true);
				}
			}

			public void oscStatus(OscStatus theStatus) {
			}
		});
	}

	public ScheduleLayout mScheduleLayout = new ScheduleLayout();
	public AnimLayout mAnimLayout = new AnimLayout();

	ArrayList<Widget> mPreviewWidgets;

	public int getRemotePort() {
		return 4444;
	}

	public int getListenPort() {
		return 5555;
	}

	public final static int kLedPort = 4444;
	public final static String kConfigName = "CONFIG";

	public ImageView mScheduleSceneBtn, mProgrammeSceneBtn;

	int mCurrentLayout = -1;
}
