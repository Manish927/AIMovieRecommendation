package com.spring5.movieservice.domain.service;

import com.spring5.movieservice.domain.entity.MovieEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface MovieMapper {
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Movie entityToApi(MovieEntity entity);

    @Mappings({@Mapping(target = "movieId", ignore = true)})
    MovieEntity apiToEntity(Movie api);
}


