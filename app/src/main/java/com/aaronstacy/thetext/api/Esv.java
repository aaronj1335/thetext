package com.aaronstacy.thetext.api;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public final class Esv {
  private static final String BASE_URL = "http://www.esvapi.org";
  private static final Service service = new Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .build()
      .create(Service.class);

  public interface Service {
    @GET("/v2/rest/passageQuery?key=IP")
    Observable<ResponseBody> lookup(@Query("passage") String passage);
  }

  public static Service service() {
    return service;
  }
}
