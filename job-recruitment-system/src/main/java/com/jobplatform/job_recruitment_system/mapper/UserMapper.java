package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.LoginResponse;
import com.jobplatform.job_recruitment_system.dtos.request.RegisterRequest;
import com.jobplatform.job_recruitment_system.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password",ignore = true)
    @Mapping(target = "active",ignore = true)
    User toEntity(RegisterRequest registerRequest);

    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    LoginResponse toLoginResponse(User user);



}
