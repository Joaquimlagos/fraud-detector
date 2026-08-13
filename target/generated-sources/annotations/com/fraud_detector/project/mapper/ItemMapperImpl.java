package com.fraud_detector.project.mapper;

import com.fraud_detector.project.dto.ItemRequestDTO;
import com.fraud_detector.project.dto.ItemResponseDTO;
import com.fraud_detector.project.model.Item;
import java.time.Instant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-12T22:15:18-0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.12 (Amazon.com Inc.)"
)
@Component
public class ItemMapperImpl implements ItemMapper {

    @Override
    public Item toEntity(ItemRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Item.ItemBuilder item = Item.builder();

        item.nome( dto.nome() );
        item.descricao( dto.descricao() );
        item.quantidade( dto.quantidade() );

        return item.build();
    }

    @Override
    public ItemResponseDTO toResponseDTO(Item entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String nome = null;
        String descricao = null;
        Integer quantidade = null;
        Instant criadoEm = null;
        Instant atualizadoEm = null;

        id = entity.getId();
        nome = entity.getNome();
        descricao = entity.getDescricao();
        quantidade = entity.getQuantidade();
        criadoEm = entity.getCriadoEm();
        atualizadoEm = entity.getAtualizadoEm();

        ItemResponseDTO itemResponseDTO = new ItemResponseDTO( id, nome, descricao, quantidade, criadoEm, atualizadoEm );

        return itemResponseDTO;
    }

    @Override
    public void updateEntityFromDto(ItemRequestDTO dto, Item entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.nome() != null ) {
            entity.setNome( dto.nome() );
        }
        if ( dto.descricao() != null ) {
            entity.setDescricao( dto.descricao() );
        }
        if ( dto.quantidade() != null ) {
            entity.setQuantidade( dto.quantidade() );
        }
    }
}
