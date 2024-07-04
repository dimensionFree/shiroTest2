package com.shiroTest.function.user.model;

import com.shiroTest.function.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 
 * </p>
 *
 * @author freedom
 * @since 2023-11-12
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserLoginInfo extends BaseEntity {
    User4Display user4Display=null;

    String token="";
}
