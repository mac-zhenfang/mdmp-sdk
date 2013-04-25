package com.mdmp.android.config;

import android.content.Context;
import java.util.Map;

public abstract interface ConfigListener
{
  public abstract void onReceived(Context paramContext, Map<String, String> paramMap);

  public abstract void onChanged(Context paramContext, Map<String, String> paramMap);

  public abstract void onFailed(Context paramContext);
}
