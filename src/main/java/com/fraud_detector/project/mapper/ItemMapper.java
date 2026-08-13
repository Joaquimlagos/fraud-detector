package com.fraud_detector.project.mapper;

import com.fraud_detector.project.dto.ItemRequestDTO;
import com.fraud_detector.project.dto.ItemResponseDTO;
import com.fraud_detector.project.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toEntity(ItemRequestDTO dto);

    ItemResponseDTO toResponseDTO(Item entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ItemRequestDTO dto, @MappingTarget Item entity);
}
