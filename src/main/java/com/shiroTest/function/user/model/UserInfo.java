package com.shiroTest.function.user.model;

import com.shiroTest.function.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
public class UserInfo extends BaseEntity {
    User user=null;

    String token="";
}
