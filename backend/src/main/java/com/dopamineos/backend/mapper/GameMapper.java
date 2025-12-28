package com.dopamineos.backend.mapper;

import com.dopamineos.backend.dto.ProtocoloDTO;
import com.dopamineos.backend.dto.UsuarioDTO;
import com.dopamineos.backend.entity.Protocolo;
import com.dopamineos.backend.entity.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GameMapper {
    UsuarioDTO toUsuarioDTO(Usuario usuario);
    ProtocoloDTO toProtocoloDTO(Protocolo protocolo);
    Protocolo toProtocoloEntity(ProtocoloDTO dto);
}