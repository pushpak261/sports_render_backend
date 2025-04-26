package com.ecommerce.sportshub.service.interf;

import com.ecommerce.sportshub.dto.AddressDto;
import com.ecommerce.sportshub.dto.Response;

public interface AddressService {
    Response saveAndUpdateAddress(AddressDto addressDto);
}
