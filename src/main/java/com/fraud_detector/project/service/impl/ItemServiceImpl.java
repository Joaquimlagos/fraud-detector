package com.fraud_detector.project.service.impl;

import com.fraud_detector.project.dto.ItemRequestDTO;
import com.fraud_detector.project.dto.ItemResponseDTO;
import com.fraud_detector.project.exception.ResourceNotFoundException;
import com.fraud_detector.project.mapper.ItemMapper;
import com.fraud_detector.project.model.Item;
import com.fraud_detector.project.repository.ItemRepository;
import com.fraud_detector.project.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemResponseDTO criar(ItemRequestDTO dto) {
        Item item = itemMapper.toEntity(dto);
        Item salvo = itemRepository.save(item);
        return itemMapper.toResponseDTO(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDTO buscarPorId(Long id) {
        Item item = buscarEntidadePorId(id);
        return itemMapper.toResponseDTO(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponseDTO> listarTodos() {
        return itemRepository.findAll()
                .stream()
                .map(itemMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public ItemResponseDTO atualizar(Long id, ItemRequestDTO dto) {
        Item item = buscarEntidadePorId(id);
        itemMapper.updateEntityFromDto(dto, item);
        Item atualizado = itemRepository.save(item);
        return itemMapper.toResponseDTO(atualizado);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        Item item = buscarEntidadePorId(id);
        itemRepository.delete(item);
    }

    private Item buscarEntidadePorId(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado com id: " + id));
    }
}
