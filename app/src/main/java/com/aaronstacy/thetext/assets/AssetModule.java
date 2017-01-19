package com.aaronstacy.thetext.assets;

import android.app.Application;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@Module
public final class AssetModule {
  @Provides @Singleton Observable<String> chapterCss(Application app) {
    return Observable.just(app)
        .observeOn(Schedulers.io())
        .map(new Func1<Application, String>() {
          @Override public String call(Application app) {
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder result = new StringBuilder();
            InputStream inputStream;
            try {
              inputStream = app.getAssets().open("chapter.css");
            } catch (IOException exception) {
              throw Exceptions.propagate(exception);
            }
            Reader reader;
            try {
              reader = new InputStreamReader(inputStream, "utf-8");
            } catch (UnsupportedEncodingException exception) {
              throw Exceptions.propagate(exception);
            }
            //noinspection LoopStatementThatDoesntLoop
            for (;;) {
              int size;
              try {
                size = reader.read(buffer, 0, buffer.length);
              } catch (IOException exception) {
                throw Exceptions.propagate(exception);
              }
              if (size < 0) {
                break;
              }
              result.append(buffer, 0, size);
            }
            return result.toString();
          }
        });
  }
}
