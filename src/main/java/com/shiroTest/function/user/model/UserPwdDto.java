package com.shiroTest.function.user.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class UserPwdDto {
    @NotBlank(groups = {Save.class,Update.class},message = "username cant be blank")
    @Length(min = 4,max = 10,message = "username length error",groups = Save.class)
    String username;
    @NotBlank(groups = {Save.class,Update.class},message = "username cant be blank")
    @Length(min = 4,max = 10,message = "pwd length error",groups = Save.class)
    String password;


    public interface Save{

    }
    public interface Update{

    }


}
