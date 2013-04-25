package com.mdmp.android.feedback;

public abstract interface FeedbackSubmitListener
{
  public abstract void onSubmitSucceeded(Feedback paramMFFeedback);

  public abstract void onSubmitFailed();
}

