package com.vnm.G9;

class HourlyProgram {
	static final int kCount = 6;
	static int mSelectedId = -1;

	public HourlyProgram() {
		isInteractive = false;
		isRandomAnimation = false;
		animConfigs = new AnimConfig[kAnimCount];
		for (int i = 0; i < kAnimCount; i++) {
			animConfigs[i] = new AnimConfig();
		}
	}

	final int kAnimCount = 10;

	boolean isInteractive;
	boolean isRandomAnimation;

	AnimConfig[] animConfigs;
}
