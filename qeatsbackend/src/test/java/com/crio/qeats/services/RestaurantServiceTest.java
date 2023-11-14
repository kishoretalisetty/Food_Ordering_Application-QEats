
/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crio.qeats.QEatsApplication;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.repositoryservices.RestaurantRepositoryService;
import com.crio.qeats.utils.FixtureHelpers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(classes = {QEatsApplication.class})
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DirtiesContext
@ActiveProfiles("test")
class RestaurantServiceTest {

  private static final String FIXTURES = "fixtures/exchanges";
  @InjectMocks
  private RestaurantServiceImpl restaurantService;
  @MockBean
  private RestaurantRepositoryService restaurantRepositoryServiceMock;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    MockitoAnnotations.initMocks(this);

    objectMapper = new ObjectMapper();
  }

  private String getServingRadius(List<Restaurant> restaurants, LocalTime timeOfService) {
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);

    GetRestaurantsResponse allRestaurantsCloseBy = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            timeOfService); //LocalTime.of(19,00));

    assertEquals(2, allRestaurantsCloseBy.getRestaurants().size());
    assertEquals("11", allRestaurantsCloseBy.getRestaurants().get(0).getRestaurantId());
    assertEquals("12", allRestaurantsCloseBy.getRestaurants().get(1).getRestaurantId());

    ArgumentCaptor<Double> servingRadiusInKms = ArgumentCaptor.forClass(Double.class);
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            servingRadiusInKms.capture());

    return servingRadiusInKms.getValue().toString();
  }

  private String getServingRadius2(List<Restaurant> restaurants, LocalTime timeOfService) {
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);

    GetRestaurantsResponse allRestaurantsCloseBy = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            timeOfService); //normal hours

           // System.out.println("()---->"+allRestaurantsCloseBy.getRestaurants().size());

    assertEquals(4, allRestaurantsCloseBy.getRestaurants().size());
    assertEquals("10", allRestaurantsCloseBy.getRestaurants().get(0).getRestaurantId());
    assertEquals("11", allRestaurantsCloseBy.getRestaurants().get(1).getRestaurantId());
    assertEquals("12", allRestaurantsCloseBy.getRestaurants().get(2).getRestaurantId());
    assertEquals("13", allRestaurantsCloseBy.getRestaurants().get(3).getRestaurantId());


    ArgumentCaptor<Double> servingRadiusInKms = ArgumentCaptor.forClass(Double.class);
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            servingRadiusInKms.capture());

           // System.out.println(" ...ANS = "+ servingRadiusInKms.getValue().toString());
    return servingRadiusInKms.getValue().toString();
  }


  private String getServingRadiusForNormalHours(List<Restaurant> restaurants, LocalTime timeOfService) {
 
    when(restaurantRepositoryServiceMock
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            any(Double.class)))
        .thenReturn(restaurants);

    GetRestaurantsResponse allRestaurantsCloseBy = restaurantService
        .findAllRestaurantsCloseBy(new GetRestaurantsRequest(20.0, 30.0),
            timeOfService); //LocalTime.of(19,00));

    assertEquals(4, allRestaurantsCloseBy.getRestaurants().size());
    assertEquals("10", allRestaurantsCloseBy.getRestaurants().get(0).getRestaurantId());
    assertEquals("11", allRestaurantsCloseBy.getRestaurants().get(1).getRestaurantId());

    ArgumentCaptor<Double> servingRadiusInKms = ArgumentCaptor.forClass(Double.class);
    verify(restaurantRepositoryServiceMock, times(1))
        .findAllRestaurantsCloseBy(any(Double.class), any(Double.class), any(LocalTime.class),
            servingRadiusInKms.capture());

    return servingRadiusInKms.getValue().toString();
  }


  @Test
  void peakHourServingRadiusOf3KmsAt7Pm() throws IOException {
   //  System.out.println("---->"+loadRestaurantsDuringPeakHours().size());
    assertEquals(getServingRadius(loadRestaurantsDuringPeakHours(), LocalTime.of(19, 0)), "3.0");
  }


  @Test
  void normalHourServingRadiusIs5Kms() throws IOException {

    // TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
    // We must ensure the API retrieves only restaurants that are closeby and are open
    // In short, we need to test:
    // 1. If the mocked service methods are being called
    // 2. If the expected restaurants are being returned
    // HINT: Use the `loadRestaurantsDuringNormalHours` utility method to speed things up
    // System.out.println("---->"+loadRestaurantsDuringNormalHours().size());
    
   List<Restaurant> res = loadRestaurantsDuringNormalHours();
    assertEquals(getServingRadiusForNormalHours(res, LocalTime.of(16, 0)), "5.0");
  
  }

  // @Test
  // void normalHourServingRadiusIs5Kms2ndTest() throws IOException {

  //   assertEquals(getServingRadius2(loadRestaurantsDuringNormalHours(), LocalTime.of(15, 1)), "5.0");

  // }



  
  private List<Restaurant> loadRestaurantsDuringNormalHours() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/normal_hours_list_of_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }

  private List<Restaurant> loadRestaurantsSearchedByAttributes() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/list_restaurants_searchedby_attributes.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }

  private List<Restaurant> loadRestaurantsDuringPeakHours() throws IOException {
    String fixture =
        FixtureHelpers.fixture(FIXTURES + "/peak_hours_list_of_restaurants.json");

    return objectMapper.readValue(fixture, new TypeReference<List<Restaurant>>() {
    });
  }
}
