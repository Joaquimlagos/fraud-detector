package com.fraud_detector.project.controller;

import com.fraud_detector.project.dto.ItemRequestDTO;
import com.fraud_detector.project.dto.ItemResponseDTO;
import com.fraud_detector.project.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Operações relacionadas a itens")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @Operation(summary = "Cria um novo item")
    public ResponseEntity<ItemResponseDTO> criar(@Valid @RequestBody ItemRequestDTO dto) {
        ItemResponseDTO criado = itemService.criar(dto);
        return ResponseEntity.created(URI.create("/api/v1/items/" + criado.id())).body(criado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um item pelo id")
    public ResponseEntity<ItemResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista todos os itens")
    public ResponseEntity<List<ItemResponseDTO>> listarTodos() {
        return ResponseEntity.ok(itemService.listarTodos());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um item existente")
    public ResponseEntity<ItemResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ItemRequestDTO dto) {
        return ResponseEntity.ok(itemService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um item")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        itemService.deletar(id);
    }
}
