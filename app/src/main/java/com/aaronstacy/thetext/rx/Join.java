package com.aaronstacy.thetext.rx;

import rx.Observable;
import rx.Subscriber;

public final class Join implements Observable.Operator<String, String> {
  private final String delimiter;

  private Join(String delimiter) {
    this.delimiter = delimiter;
  }

  public static Join with(String delimiter) {
    return new Join(delimiter);
  }

  @Override public Subscriber<? super String> call(final Subscriber<? super String> subscriber) {
    return new Subscriber<String>(subscriber) {
      String last = null;

      @Override public void onCompleted() {
        subscriber.onNext(last);
        subscriber.onCompleted();
      }

      @Override public void onError(Throwable e) {
        subscriber.onError(e);
      }

      @Override public void onNext(String next) {
        if (last != null) {
          subscriber.onNext(last);
          subscriber.onNext(delimiter);
        }
        last = next;
      }
    };
  }
}
