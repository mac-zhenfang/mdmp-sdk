package com.coder.android.feedback;

import java.util.List;

public abstract interface FeedbackReplyListener
{
  public abstract void onDetectedNewReplies(List<Feedback> paramList);

  public abstract void onDetectedNothing();
}
