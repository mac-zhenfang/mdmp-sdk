package com.coder.android.update;

import android.app.Activity;

public abstract interface MFUpdateListener
{
  public abstract void onDetectedNewVersion(Activity paramActivity, int paramInt, String paramString1, String paramString2, String paramString3);

  public abstract void onPerformUpdate(Activity paramActivity, int paramInt, String paramString1, String paramString2, String paramString3);

  public abstract void onDetectedNothing(Activity paramActivity);

  public abstract void onWifiOff(Activity paramActivity);

  public abstract void onFailed(Activity paramActivity);
}
