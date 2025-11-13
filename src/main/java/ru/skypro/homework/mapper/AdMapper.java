package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.skypro.homework.model.dto.Ad;
import ru.skypro.homework.model.dto.CreateOrUpdateAd;
import ru.skypro.homework.model.dto.ExtendedAd;
import ru.skypro.homework.model.entity.AdEntity;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AdMapper {

    @Mapping(target = "author", source = "author.id")
    @Mapping(target = "image", source = "imagePath")
    @Mapping(target = "pk", source = "id")
    Ad entityToAdDto(AdEntity entity);

    @Mapping(target = "pk", source = "id")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorLastName", source = "author.lastName")
    @Mapping(target = "email", source = "author.email")
    @Mapping(target = "phone", source = "author.phone")
    @Mapping(target = "image", source = "imagePath")
    ExtendedAd entityToExtendedAdDto(AdEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "comments", ignore = true)
    AdEntity createOrUpdateAdToEntity(CreateOrUpdateAd dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateEntityFromDto(CreateOrUpdateAd dto, @org.mapstruct.MappingTarget AdEntity entity);

}
