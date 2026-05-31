package ru.danil.medicine.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.danil.medicine.dto.PolicyDTO;
import ru.danil.medicine.model.Policy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
                         unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PolicyMapper {
    Policy toPolicy(PolicyDTO policyDTO);
    PolicyDTO toPolicyDTO(Policy policy);
}
