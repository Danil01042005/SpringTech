package ru.danil.springtest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.danil.springtest.dto.UserDTO;
import ru.danil.springtest.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDTO toUserDTO(User user);
    User toUser(UserDTO userDTO);
    void updateUser(UserDTO updatedUserDTO, @MappingTarget User user);
}
