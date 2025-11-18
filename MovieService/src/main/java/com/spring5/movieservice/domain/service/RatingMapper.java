package com.spring5.movieservice.domain.service;

import com.spring5.movieservice.domain.entity.RatingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface RatingMapper {
    Rating entityToApi(RatingEntity entity);
    
    @Mappings({@Mapping(target = "ratingId", ignore = true), @Mapping(target = "createdAt", ignore = true)})
    RatingEntity apiToEntity(Rating api);
}


