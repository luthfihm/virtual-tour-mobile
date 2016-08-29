package com.luthfihm.virtualtour.apiclient;

import com.luthfihm.virtualtour.model.ObjectModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by luthfi on 8/14/16.
 */
public interface ObjectModelAPI {
    String ENDPOINT = "http://45.114.118.154:3000/api/";

    @GET("objects")
    Call<List<ObjectModel>> getAllObjects();

    @GET("objects/{id}")
    Call<ObjectModel> getObject(@Path("id") String objectId);

    @GET("objects?filter[where][geoLocation][near]={location}&filter[where][geoLocation][maxDistance]={distance}&filter[where][geoLocation][unit]=meters")
    Call<List<ObjectModel>> getAllObjectsNear(@Path("location") String location, @Path("distance") int distance);
}
