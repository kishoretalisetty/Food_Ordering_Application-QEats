package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;



@Service
 @Primary
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Autowired
  private RestaurantRepository restaurantRepository;


  @Autowired
  private RedisConfiguration redisConfiguration;

  

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

        if(redisConfiguration.isCacheAvailable()){
          return findAllRestaurantsCloseByFromCache(latitude, longitude, currentTime, servingRadiusInKms);
        }
        else{
          return findAllRestaurantsMongo(latitude, longitude, currentTime, servingRadiusInKms);
        }

    //     List<RestaurantEntity> restaurantEntities=restaurantRepository.findAll();

    // List<Restaurant> restaurants = new ArrayList<>();
      
    //  ModelMapper modelMapper=modelMapperProvider.get();
    
    // for(RestaurantEntity entity: restaurantEntities){
    //   if(isRestaurantCloseByAndOpen(entity, currentTime, latitude, longitude, servingRadiusInKms)){
    //     restaurants.add(modelMapper.map(entity, Restaurant.class));
    //   }
    // }

    //   //CHECKSTYLE:OFF
    //   //CHECKSTYLE:ON


    // return restaurants;
  }



  private List<Restaurant> findAllRestaurantsCloseByFromCache(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms) {

        List<Restaurant> restaurants = new ArrayList<Restaurant>();
        GeoLocation geoLocation = new GeoLocation(latitude,longitude);
        GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(),
            geoLocation.getLongitude(), 7);
    
        Jedis jedis = redisConfiguration.getJedisPool().getResource();
    
        if (!jedis.exists(geoHash.toBase32())) {
          return findAllRestaurantsMongo(latitude, longitude, currentTime, servingRadiusInKms);
        }
    
        String restaurantString = "";
    
        ObjectMapper objectMapper = new ObjectMapper();
    
        try {
          restaurantString = jedis.get(geoHash.toBase32());
          restaurants = objectMapper.readValue(restaurantString,
              new TypeReference<List<Restaurant>>() {});
        } catch (IOException e) {
          e.printStackTrace();
        }
    
       return restaurants;
      }
  
  public List<Restaurant> findAllRestaurantsMongo(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {
    List<Restaurant> restaurants = new ArrayList<Restaurant>();
    ObjectMapper objectMapper = new ObjectMapper();

    List<RestaurantEntity> allRestaurants = restaurantRepository.findAll();
      
    for (RestaurantEntity restaurantEntity : allRestaurants) {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime,
          latitude, longitude, servingRadiusInKms)) {
        Restaurant restaurant = modelMapperProvider.get().map(restaurantEntity,Restaurant.class);
        restaurants.add(restaurant);
      }
    }

    String restaurantDbString = "";
    redisConfiguration.initCache();
    try {
      restaurantDbString = objectMapper.writeValueAsString(restaurants);
    } catch (IOException e) {
      e.printStackTrace();
    }
  

    GeoLocation geoLocation = new GeoLocation(latitude,longitude);
    GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(),
        geoLocation.getLongitude(),7);
    Jedis jedis = redisConfiguration.getJedisPool().getResource();
    jedis.set(geoHash.toBase32(),restaurantDbString);
    return restaurants;
  }

   
  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose names have an exact or partial match with the search query.

  @Override
  public List<Restaurant> findRestaurantsByName(Double latitude, Double longitude,
                                                String searchString, LocalTime currentTime,
                                                Double servingRadiusInKms) {
    final String regexExact = String.format("^%s$", searchString);
    final String regexAll = String.format(".*%s.*", searchString);

    LinkedHashSet<RestaurantEntity> restaurantEntityLinkedHashSet = new LinkedHashSet<>();

    Query queryExact = new Query(Criteria.where("name").regex(regexExact, "i"));
    Query queryRestMatches = new Query(Criteria.where("name").regex(regexAll, "i"));


    restaurantEntityLinkedHashSet.addAll(mongoTemplate.find(queryExact, RestaurantEntity.class));
    restaurantEntityLinkedHashSet.addAll(mongoTemplate.find(queryRestMatches,
        RestaurantEntity.class));

    List<Restaurant> restaurants = new ArrayList<>();
    restaurantEntityLinkedHashSet.forEach(restaurantEntity -> {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
          servingRadiusInKms)) {
        restaurants.add(modelMapperProvider.get().map(restaurantEntity, Restaurant.class));
      }
    });
    return restaurants;
  }
  // @Override
  // public List<Restaurant> findRestaurantsByName(Double latitude, Double longitude,
  //     String searchString, LocalTime currentTime, Double servingRadiusInKms) {

  //       List<Restaurant> restaurants =
  //       findAllRestaurantsCloseBy(latitude, longitude, currentTime, servingRadiusInKms);
  //   List<Restaurant> res = new ArrayList<>();
  //   for (Restaurant r : restaurants) {
  //     if (r.getName().toLowerCase().contains(searchString.toLowerCase())) {
  //       res.add(r);
  //     }
  //   }
  //   return res;
  // }


  // Objective:
  // Find restaurants whose attributes (cuisines) intersect with the search query.

  @Override
  public List<Restaurant> findRestaurantsByAttributes(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
    final String regexAll = String.format(".*%s.*", searchString);
    Query queryRestMatches = new Query();
    queryRestMatches.addCriteria(Criteria.where("attributes").in(regexAll, "i"));
    List<RestaurantEntity> restaurantEntities =
        new ArrayList<>(mongoTemplate.find(queryRestMatches, RestaurantEntity.class));

        List<Restaurant> restaurants = new ArrayList<>();
    restaurantEntities.forEach(restaurantEntity -> {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
          servingRadiusInKms)) {
        restaurants.add(modelMapperProvider.get().map(restaurantEntity, Restaurant.class));
      }
    });
    return restaurants;
  }


  // @Override
  // public List<Restaurant> findRestaurantsByAttributes(
  //     Double latitude, Double longitude,
  //     String searchString, LocalTime currentTime, Double servingRadiusInKms) {

  //       List<Restaurant> restaurants =
  //       findAllRestaurantsCloseBy(latitude, longitude, currentTime, servingRadiusInKms);

  //       List<Restaurant> res = new ArrayList<>();
  //       for (Restaurant r : restaurants) {
  //         if (r.getAttributes().contains(searchString)) {
  //           res.add(r);
  //         }
  //       }
  //       return res;
  // }



  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose names form a complete or partial match
  // with the search query.

  @Override
  public List<Restaurant> findRestaurantsByItemName(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {


     return new ArrayList();
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose attributes intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemAttributes(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {

     return new ArrayList();
  }


  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }



}

  // List<Restaurant> restaurantList = new ArrayList<>();

    // GeoLocation geoLocation = new GeoLocation(latitude, longitude);
    // GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(), geoLocation.getLongitude(), 7);

    // try (Jedis jedis = redisConfiguration.getJedisPool().getResource()) {
    //   String jsonStringFromCache = jedis.get(geoHash.toBase32());

    //   if (jsonStringFromCache == null) {
    //     // Cache needs to be updated.
    //     String createdJsonString = "";
    //     try {
    //       restaurantList = findAllRestaurantsCloseFromDb(geoLocation.getLatitude(), geoLocation.getLongitude(),
    //           currentTime, servingRadiusInKms);
    //       createdJsonString = new ObjectMapper().writeValueAsString(restaurantList);
    //     } catch (JsonProcessingException e) {
    //       e.printStackTrace();
    //     }

    //     // Do operations with jedis resource
    //     jedis.setex(geoHash.toBase32(), GlobalConstants.REDIS_ENTRY_EXPIRY_IN_SECONDS, createdJsonString);
    //   } else {
    //     try {
    //       restaurantList = new ObjectMapper().readValue(jsonStringFromCache, new TypeReference<List<Restaurant>>(){});

    //     } catch (IOException e) {
    //       e.printStackTrace();
    //     }
    //   }
    // }

    // return restaurantList;

  //}

  // private List<Restaurant>findAllRestaurantsCloseFromDb(Double latitude, Double longitude, LocalTime currentTime,
  //     Double servingRadiusInKms) {
  //   ModelMapper modelMapper = modelMapperProvider.get();
  //   List<RestaurantEntity>restaurantEntities = restaurantRepository.findAll();
  //   List<Restaurant> restaurants = new ArrayList<Restaurant>();
  //   for (RestaurantEntity restaurantEntity : restaurantEntities) {
  //     if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms)) {
  //       restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
  //     }
  //   }
  //   return restaurants;
  // }


