package com.spring5.movieservice.domain.service;

import com.spring5.movieservice.domain.entity.TheaterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface TheaterMapper {
    @Mappings({@Mapping(target = "serviceAddress", ignore = true)})
    Theater entityToApi(TheaterEntity entity);

    @Mappings({@Mapping(target = "theaterId", ignore = true)})
    TheaterEntity apiToEntity(Theater api);
}


