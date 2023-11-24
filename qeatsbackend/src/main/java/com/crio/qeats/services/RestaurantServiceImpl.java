
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
       
       
        Double servingRadiusInKms =
        isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;

    List<Restaurant> restaurantsCloseBy = restaurantRepositoryService.findAllRestaurantsCloseBy(
        getRestaurantsRequest.getLatitude(), getRestaurantsRequest.getLongitude(),
        currentTime, servingRadiusInKms);
 
    return new GetRestaurantsResponse(restaurantsCloseBy);

      }


  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  @Override
  public GetRestaurantsResponse findRestaurantsBySearchQuery(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {
    if (!getRestaurantsRequest.getSearchFor().isEmpty()) {
      return findAllRestaurantsCloseBy(getRestaurantsRequest, currentTime);
    }

    List<Restaurant> restaurants = new ArrayList<>();
    final Double latitude = getRestaurantsRequest.getLatitude();
    final Double longitude = getRestaurantsRequest.getLongitude();
    final String searchQuery = getRestaurantsRequest.getSearchFor();
   
    Double servingRadiusInKms =
    isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;

    final Double finalServingRadiusInKms = servingRadiusInKms;


    // Restaurants by Name
    CompletableFuture<List<Restaurant>> completableFuture1 =
        getRestaurantsByName(currentTime, latitude, longitude, searchQuery,
            finalServingRadiusInKms);

    // Restaurants by Cuisines (Attributes)
    CompletableFuture<List<Restaurant>> completableFuture2 =
        getRestaurantsByAttributes(currentTime, latitude, longitude, searchQuery,
            finalServingRadiusInKms);

    // Restaurants by Food Item
    CompletableFuture<List<Restaurant>> completableFuture3 =
        getRestaurantsByItemName(currentTime, latitude, longitude, searchQuery,
            finalServingRadiusInKms);

    // Restaurants by Food Item Attributes
    CompletableFuture<List<Restaurant>> completableFuture4 =
        getRestaurantsByItemAttributes(currentTime, latitude, longitude, searchQuery,
            finalServingRadiusInKms);

    CompletableFuture.allOf(completableFuture1, completableFuture2, completableFuture3,
        completableFuture4);

    try {
      restaurants.addAll(completableFuture1.get());
      restaurants.addAll(completableFuture2.get());
      restaurants.addAll(completableFuture3.get());
      restaurants.addAll(completableFuture4.get());
    } catch (InterruptedException | ExecutionException e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }

    // restaurants.forEach(restaurant -> {
    //   restaurant.setName(StringUtils.stripAccents(restaurant.getName()));
    // });
    return new GetRestaurantsResponse(restaurants);
  }

  @Async
  private CompletableFuture<List<Restaurant>> getRestaurantsByItemAttributes(
      LocalTime currentTime, Double latitude, Double longitude, String searchQuery,
      Double finalServingRadiusInKms) {
    return CompletableFuture.completedFuture(restaurantRepositoryService
        .findRestaurantsByItemAttributes(latitude, longitude, searchQuery, currentTime,
            finalServingRadiusInKms));
  }

  @Async
  private CompletableFuture<List<Restaurant>> getRestaurantsByItemName(
      LocalTime currentTime, Double latitude, Double longitude, String searchQuery,
      Double finalServingRadiusInKms) {
    return CompletableFuture.completedFuture(restaurantRepositoryService
        .findRestaurantsByItemName(latitude, longitude, searchQuery, currentTime,
            finalServingRadiusInKms));
  }

  @Async
  private CompletableFuture<List<Restaurant>> getRestaurantsByAttributes(
      LocalTime currentTime, Double latitude, Double longitude, String searchQuery,
      Double finalServingRadiusInKms) {
    return CompletableFuture.completedFuture(restaurantRepositoryService
        .findRestaurantsByAttributes(latitude, longitude, searchQuery, currentTime,
            finalServingRadiusInKms));
  }

  @Async
  private CompletableFuture<List<Restaurant>> getRestaurantsByName(
      LocalTime currentTime, Double latitude, Double longitude, String searchQuery,
      Double finalServingRadiusInKms) {
    return CompletableFuture.completedFuture(restaurantRepositoryService
        .findRestaurantsByName(latitude, longitude, searchQuery, currentTime,
            finalServingRadiusInKms));
  }


  
  // COMPLETED: CRIO_TASK_MODULE_MULTITHREADING: Implement multi-threaded version of
  // RestaurantSearch.
  // Implement variant of findRestaurantsBySearchQuery which is at least 1.5x time faster than
  // findRestaurantsBySearchQuery.
  @Override
  @Async("restaurantExecutor")
  public GetRestaurantsResponse findRestaurantsBySearchQueryMt(
      GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

    if (!getRestaurantsRequest.getSearchFor().isEmpty()) {
      return findAllRestaurantsCloseBy(getRestaurantsRequest, currentTime);
    }

    List<Restaurant> restaurants = new ArrayList<>();
    final Double latitude = getRestaurantsRequest.getLatitude();
    final Double longitude = getRestaurantsRequest.getLongitude();
    final String searchQuery = getRestaurantsRequest.getSearchFor();
    Double servingRadiusInKms = normalHoursServingRadiusInKms;
    if (isPeakHour(currentTime)) {
      servingRadiusInKms = peakHoursServingRadiusInKms;
    }

    // Restaurants by Name
    restaurants.addAll(restaurantRepositoryService.findRestaurantsByName(latitude, longitude,
        searchQuery, currentTime, servingRadiusInKms));

    // Restaurants by Cuisines (Attributes)
    restaurants.addAll(restaurantRepositoryService.findRestaurantsByAttributes(latitude,
        longitude, searchQuery, currentTime, servingRadiusInKms));

    // Restaurants by Food Item
    restaurants.addAll(restaurantRepositoryService.findRestaurantsByItemName(latitude,
        longitude, searchQuery, currentTime, servingRadiusInKms));

    // Restaurants by Food Item Attributes
    restaurants.addAll(restaurantRepositoryService.findRestaurantsByItemAttributes(latitude,
        longitude, searchQuery, currentTime, servingRadiusInKms));
    return new GetRestaurantsResponse(restaurants);
  }



}

  // TODO: CRIO_TASK_MODULE_MULTITHREADING
  // Implement multi-threaded version of RestaurantSearch.
  // Implement variant of findRestaurantsBySearchQuery which is at least 1.5x time faster than
  // findRestaurantsBySearchQuery.
  // @Override
  // public GetRestaurantsResponse findRestaurantsBySearchQueryMt(
  //     GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

  //    return null;
  // }

  
      // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Implement findRestaurantsBySearchQuery. The request object has the search string.
  // We have to combine results from multiple sources:
  // 1. Restaurants by name (exact and inexact)
  // 2. Restaurants by cuisines (also called attributes)
  // 3. Restaurants by food items it serves
  // 4. Restaurants by food item attributes (spicy, sweet, etc)
  // Remember, a restaurant must be present only once in the resulting list.
  // Check RestaurantService.java file for the interface contract.
  // @Override
  // public GetRestaurantsResponse findRestaurantsBySearchQuery(
  //     GetRestaurantsRequest getRestaurantsRequest, LocalTime currentTime) {

  //       String searchby=getRestaurantsRequest.getSearchFor();
        
  //       Double servingRadiusInKms =
  //       isPeakHour(currentTime) ? peakHoursServingRadiusInKms : normalHoursServingRadiusInKms;

        
  //       List<List<Restaurant>> listsOfRestaurants = new  ArrayList<>();
  //       Set<Restaurant> setOfRestaurants=new HashSet<>();
  //       List<Restaurant> ans=new ArrayList<>();

  //       if(!searchby.isEmpty()){

  //      listsOfRestaurants.add(restaurantRepositoryService.findRestaurantsByItemName(getRestaurantsRequest.getLatitude(),
  //        getRestaurantsRequest.getLongitude(), searchby, currentTime, servingRadiusInKms));

  //        listsOfRestaurants.add(restaurantRepositoryService.findRestaurantsByName(getRestaurantsRequest.getLatitude(),
  //        getRestaurantsRequest.getLongitude(), searchby, currentTime, servingRadiusInKms));

  //        listsOfRestaurants.add(restaurantRepositoryService.findRestaurantsByAttributes(getRestaurantsRequest.getLatitude(),
  //        getRestaurantsRequest.getLongitude(), searchby, currentTime, servingRadiusInKms));

  //        listsOfRestaurants.add(restaurantRepositoryService.findRestaurantsByItemName(getRestaurantsRequest.getLatitude(),
  //        getRestaurantsRequest.getLongitude(), searchby, currentTime, servingRadiusInKms));

  //       }
       
  //       for(List<Restaurant> list : listsOfRestaurants){
  //         for(Restaurant restaurant: list){
  //           if(!setOfRestaurants.contains(restaurant)){
  //             ans.add(restaurant);
  //             setOfRestaurants.add(restaurant);
  //           }
  //         }
  //       }

  //       return new GetRestaurantsResponse(ans);
  // }
  //=======

