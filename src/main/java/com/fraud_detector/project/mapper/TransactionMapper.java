package com.fraud_detector.project.mapper;

import com.fraud_detector.project.dto.response.TransactionItemDTO;
import com.fraud_detector.project.model.Transaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionItemDTO toItemDTO(Transaction model);

    List<TransactionItemDTO> toItemDTOList(List<Transaction> models);
}
