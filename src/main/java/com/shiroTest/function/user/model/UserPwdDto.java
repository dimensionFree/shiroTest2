package com.shiroTest.function.user.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data

public class UserPwdDto {
    @NotBlank(groups = {Register.class},message = "username cant be blank")
    @Length(min = 4,max = 10,message = "username length error",groups = Register.class)
    String username;
    @NotBlank(groups = {Register.class},message = "pwd cant be blank")
    @Length(min = 4,max = 10,message = "pwd length error",groups = Register.class)
    String password;
    @NotBlank(groups = {Register.class},message = "email cant be blank")
    @Email
    String email;

    public UserPwdDto() {
    }

    @NotBlank(groups = {Register.class},message = "verificationCode cant be blank")
    String verificationCode;

    public UserPwdDto(String username, String password,String email,String verificationCode) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.verificationCode = verificationCode;
    }


    public interface Register {

    }
    public interface Login {

    }


}
