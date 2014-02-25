package com.vnm.G9;

import oscP5.OscMessage;
import android.content.SharedPreferences;

class AnimConfig {
	public static final int kCount = 10;

	boolean isEnabled = true;
	int loopCount = 1;// bigger than 1
	float lightValue = 0.5f;
	float lightValue2 = 0; // if non-zero, then random light value from
							// (lightValue, lightValue2)

	public void loadConfig(SharedPreferences settings, String nameOfKey) {
		isEnabled = settings.getBoolean(nameOfKey + "/isEnabled", isEnabled);
		loopCount = settings.getInt(nameOfKey + "/loopCount", loopCount);
		lightValue = settings.getFloat(nameOfKey + "/lightValue", lightValue);
		lightValue2 = settings.getFloat(nameOfKey + "/lightValue2", lightValue2);
	}

	public void saveConfig(SharedPreferences.Editor editor, String nameOfKey) {
		editor.putBoolean(nameOfKey + "/isEnabled", isEnabled);
		editor.putInt(nameOfKey + "/loopCount", loopCount);
		editor.putFloat(nameOfKey + "/lightValue", lightValue);
		editor.putFloat(nameOfKey + "/lightValue2", lightValue2);
	}

	public void processOscMsg(OscMessage m) {
		m.add(isEnabled ? 1 : 0); // oscpack doesn't support bool args
		m.add(loopCount);
		m.add(lightValue);
		m.add(lightValue2);
	}
}

class Config {
	static final int kCount = 6;
	static int mSelectedId = -1;

	public boolean isKinectEnabled = false;
	AnimConfig[] animConfigs = new AnimConfig[AnimConfig.kCount];

	public Config() {
		for (int i = 0; i < AnimConfig.kCount; i++) {
			animConfigs[i] = new AnimConfig();
		}
	}

	public void loadConfig(SharedPreferences settings, String nameOfKey) {
		isKinectEnabled = settings.getBoolean(nameOfKey + "/isKinectEnabled",
				isKinectEnabled);
		for (int i = 0; i < AnimConfig.kCount; i++) {
			animConfigs[i].loadConfig(settings, nameOfKey + "/" + i);
		}
	}

	public void saveConfig(SharedPreferences.Editor editor, String nameOfKey) {
		editor.putBoolean(nameOfKey + "/isKinectEnabled", isKinectEnabled);
		for (int i = 0; i < AnimConfig.kCount; i++) {
			animConfigs[i].saveConfig(editor, nameOfKey + "/" + i);
		}
	}

	public void sendOscMsg(int index) {
		OscMessage m = new OscMessage("/anim");
		m.add(index);
		m.add(isKinectEnabled ? 1 : 0);
		for (int i = 0; i < AnimConfig.kCount; i++) {
			animConfigs[i].processOscMsg(m);
		}
		for (String ip : MainAct.sInstance.mRemoteIps)
			MainAct.sInstance.mOscServer.send(m, ip, MainAct.kLedPort);
	}
}
