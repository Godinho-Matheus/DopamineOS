package com.lumeos.backend.mapper;

import com.lumeos.backend.dto.LogAtividadeDTO;
import com.lumeos.backend.dto.ProtocoloDTO;
import com.lumeos.backend.dto.UsuarioDTO;
import com.lumeos.backend.entity.LogAtividade;
import com.lumeos.backend.entity.Protocolo;
import com.lumeos.backend.entity.Usuario;
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
