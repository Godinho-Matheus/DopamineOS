package com.lumeos.backend.identity.mapper;

import com.lumeos.backend.identity.User;
import com.lumeos.backend.identity.dto.UserResponse;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct de {@link User} para {@link UserResponse}. Mapeia apenas id/nome/email;
 * {@code passwordHash} nunca é incluído no response (REQ-001 critério 3, REQ-002).
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
