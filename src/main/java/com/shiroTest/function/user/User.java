package com.shiroTest.function.user;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class User {
    @Id
    public Integer id;
    public String username;
    public String password;
    public String role;
    public String state;
}
