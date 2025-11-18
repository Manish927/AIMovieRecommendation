package com.spring5.movieservice.domain.service;

import com.spring5.movieservice.domain.entity.TheaterMovieEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface TheaterMovieMapper {
    TheaterMovie entityToApi(TheaterMovieEntity entity);

    @Mappings({@Mapping(target = "id", ignore = true)})
    TheaterMovieEntity apiToEntity(TheaterMovie api);
}


