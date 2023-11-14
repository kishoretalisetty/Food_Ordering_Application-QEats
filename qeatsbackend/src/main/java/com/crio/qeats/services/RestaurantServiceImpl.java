
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RestaurantServiceImpl implements RestaurantService {

  private final Double peakHoursServingRadiusInKms = 3.0;
  private final Double normalHoursServingRadiusInKms = 5.0;
  
  @Autowired
  private RestaurantRepositoryService restaurantRepositoryService;

  private boolean isTimeWithInRange(LocalTime timeNow,
  LocalTime startTime, LocalTime endTime) {
  return timeNow.isAfter(startTime) && timeNow.isBefore(endTime);
  }

   public boolean isPeakHour(LocalTime timeNow) {
   return isTimeWithInRange(timeNow, LocalTime.of(7, 59, 59), LocalTime.of(10, 00, 01))
    || isTimeWithInRange(timeNow, LocalTime.of(12, 59, 59), LocalTime.of(14, 00, 01))
    || isTimeWithInRange(timeNow, LocalTime.of(18, 59, 59), LocalTime.of(21, 00, 01));
   }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement findAllRestaurantsCloseby.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findAllRestaurantsCloseBy(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
       
        // double latitude=getRestaurantsRequest.getLatitude();
        // double longitude=getRestaurantsRequest.getLongitude();
        // String searchFor=getRestaurantsRequest.getSearchFor();

        // // For peak hours: 8AM - 10AM, 1PM-2PM, 7PM-9PM
        //  if((currentTime.isAfter(LocalTime.of(7,59,59)) && currentTime.isBefore(LocalTime.of(10,00,01))) || 
        //  (currentTime.isAfter(LocalTime.of(12,59,59)) && currentTime.isBefore(LocalTime.of(14,00,01)))    ||
        //  (currentTime.isAfter(LocalTime.of(18,59,59)) && currentTime.isBefore(LocalTime.of(21,00,01)))){
        //  List<Restaurant> restaurantsradius3km =  restaurantRepositoryService.
        // findAllRestaurantsCloseBy(latitude, longitude, currentTime, peakHoursServingRadiusInKms);
        //  return new GetRestaurantsResponse(restaurantsradius3km);
        //  }
        //  else{
        //  List<Restaurant> restaurantsradius5km = restaurantRepositoryService.
        // findAllRestaurantsCloseBy(latitude, longitude, currentTime, normalHoursServingRadiusInKms);
        // return new GetRestaurantsResponse(restaurantsradius5km);

        Double servingRadiusInKms =
        isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;

    List<Restaurant> restaurantsCloseBy = restaurantRepositoryService.findAllRestaurantsCloseBy(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        currentTime, servingRadiusInKms);
 
    return new GetRestaurantsResponse(restaurantsCloseBy);

      }


}

