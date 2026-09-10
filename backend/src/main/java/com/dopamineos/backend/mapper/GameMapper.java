package com.dopamineos.backend.mapper;

import com.dopamineos.backend.dto.LogAtividadeDTO;
import com.dopamineos.backend.dto.ProtocoloDTO;
import com.dopamineos.backend.dto.UsuarioDTO;
import com.dopamineos.backend.entity.LogAtividade;
import com.dopamineos.backend.entity.Protocolo;
import com.dopamineos.backend.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GameMapper {
    UsuarioDTO toUsuarioDTO(Usuario usuario);

    ProtocoloDTO toProtocoloDTO(Protocolo protocolo);

    /** Ignora o campo usuario ao converter DTO → entidade (definido no controller). */
    @Mapping(target = "usuario", ignore = true)
    Protocolo toProtocoloEntity(ProtocoloDTO dto);

    /** Mapeia LogAtividade para DTO sem expor o usuário relacionado. */
    LogAtividadeDTO toLogAtividadeDTO(LogAtividade logAtividade);
}
