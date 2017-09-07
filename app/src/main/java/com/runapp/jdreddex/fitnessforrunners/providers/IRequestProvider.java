package com.runapp.jdreddex.fitnessforrunners.providers;

import com.runapp.jdreddex.fitnessforrunners.models.AfterLoginRequestData;
import com.runapp.jdreddex.fitnessforrunners.models.AfterPointsRequest;
import com.runapp.jdreddex.fitnessforrunners.models.AfterRegisterToken;
import com.runapp.jdreddex.fitnessforrunners.models.AfterSaveRequest;
import com.runapp.jdreddex.fitnessforrunners.models.AfterTracksRequest;
import com.runapp.jdreddex.fitnessforrunners.models.LoginRequestData;
import com.runapp.jdreddex.fitnessforrunners.models.PointsRequestData;
import com.runapp.jdreddex.fitnessforrunners.models.RegisterRequestData;
import com.runapp.jdreddex.fitnessforrunners.models.SaveRequestData;
import com.runapp.jdreddex.fitnessforrunners.models.TrackToSaveRequest;
import com.runapp.jdreddex.fitnessforrunners.models.TracksRequestData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by JDReddex on 25.07.2016.
 */
public interface IRequestProvider {
    @POST("senla-training-addition/lesson-26.php?method=register")
    Call<AfterRegisterToken> userRegister(@Body RegisterRequestData register);

    @POST("senla-training-addition/lesson-26.php?method=login")
    Call<AfterLoginRequestData> userLogin(@Body LoginRequestData login);

    @POST("senla-training-addition/lesson-26.php?method=tracks")
    Call<AfterTracksRequest> getTracks(@Body TracksRequestData track);

    @POST("senla-training-addition/lesson-26.php?method=points")
    Call<AfterPointsRequest> getPoints(@Body PointsRequestData points);

    @POST("senla-training-addition/lesson-26.php?method=save")
    Call<AfterSaveRequest> saveTrack(@Body SaveRequestData save);
}
