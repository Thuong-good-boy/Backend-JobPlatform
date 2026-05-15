package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.LoginResponse;
import com.jobplatform.job_recruitment_system.dtos.request.RegisterRequest;
import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.models.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-16T05:57:45+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(RegisterRequest registerRequest) {
        if ( registerRequest == null ) {
            return null;
        }

        User user = new User();

        user.setEmail( registerRequest.getEmail() );
        user.setFullName( registerRequest.getFullName() );
        if ( registerRequest.getRole() != null ) {
            user.setRole( Enum.valueOf( Role.class, registerRequest.getRole() ) );
        }

        return user;
    }

    @Override
    public LoginResponse toLoginResponse(User user) {
        if ( user == null ) {
            return null;
        }

        LoginResponse loginResponse = new LoginResponse();

        return loginResponse;
    }
}
