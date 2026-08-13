package com.fraud_detector.project.service;

import com.fraud_detector.project.dto.ItemRequestDTO;
import com.fraud_detector.project.dto.ItemResponseDTO;

import java.util.List;

public interface ItemService {

    ItemResponseDTO criar(ItemRequestDTO dto);

    ItemResponseDTO buscarPorId(Long id);

    List<ItemResponseDTO> listarTodos();

    ItemResponseDTO atualizar(Long id, ItemRequestDTO dto);

    void deletar(Long id);
}
