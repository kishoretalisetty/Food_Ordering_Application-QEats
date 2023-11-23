/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositories;

import com.crio.qeats.models.RestaurantEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends MongoRepository<RestaurantEntity, String> {

    List<RestaurantEntity> findAll();

    @Query("{'name':{$regex: '^?0$', $options: 'i'}}")
    Optional<List<RestaurantEntity>> findRestaurantsByNameExact(String name);
}

