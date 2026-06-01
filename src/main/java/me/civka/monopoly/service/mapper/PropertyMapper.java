package me.civka.monopoly.service.mapper;

import me.civka.monopoly.dto.property.PropertyDto;
import me.civka.monopoly.repository.entity.Property;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = MemberMapper.class)
public interface PropertyMapper {

  PropertyDto toPropertyDto(Property property);
}
