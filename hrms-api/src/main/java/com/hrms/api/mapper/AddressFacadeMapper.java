package com.hrms.api.mapper;

import com.hrms.api.request.CreateAddressRequest;
import com.hrms.api.request.UpdateAddressRequest;
import com.hrms.api.response.AddressResponse;
import com.hrms.core.model.dto.AddressDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", implementationName = "AddressFacadeMapperImpl")
public interface AddressFacadeMapper {
    
    @Mapping(target = "id", ignore = true)
    AddressDTO toDTO(CreateAddressRequest request);
    
    @Mapping(target = "id", ignore = true)
    AddressDTO toDTO(UpdateAddressRequest request);
    
    AddressResponse toResponse(AddressDTO dto);
    
    List<AddressResponse> toResponseList(List<AddressDTO> dtos);
}

