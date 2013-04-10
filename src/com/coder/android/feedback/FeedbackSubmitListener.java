package com.coder.android.feedback;

public abstract interface FeedbackSubmitListener
{
  public abstract void onSubmitSucceeded(Feedback paramMFFeedback);

  public abstract void onSubmitFailed();
}

