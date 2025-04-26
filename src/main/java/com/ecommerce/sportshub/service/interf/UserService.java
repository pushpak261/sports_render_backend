package com.ecommerce.sportshub.service.interf;

import com.ecommerce.sportshub.dto.LoginRequest;
import com.ecommerce.sportshub.dto.Response;
import com.ecommerce.sportshub.dto.UserDto;
import com.ecommerce.sportshub.entity.User;

public interface UserService {
    Response registerUser(UserDto registrationRequest);
    Response loginUser(LoginRequest loginRequest);
    Response getAllUsers();
    User getLoginUser();
    Response getUserInfoAndOrderHistory();
}