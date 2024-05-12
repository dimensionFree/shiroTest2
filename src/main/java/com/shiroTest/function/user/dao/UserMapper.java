package com.shiroTest.function.user.dao;

import com.shiroTest.function.user.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author freedom
 * @since 2023-11-12
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 总结：
     * #{}：相当于JDBC中的PreparedStatement，是经过预编译的，是安全的；
     * ${}：是未经过预编译的，仅仅是取变量的值，非安全的，存在SQL注入。  要用的话，要加'' ,如 #{username} -> '${username}'
     * 在编写MyBatis的映射语句时，尽量采用“#{xxx}”这样的格式来做参数的占位。
     * 但涉及到[动态表名]和[列名]时，只能使用“${xxx}”这样的参数格式占位，这种不得不使用的情况下，我们要手工做过滤工作，这样才能有效防止SQL注入攻击。
     * @param username
     * @return
     */
    @Select("select * from User where username=#{username}")
    public User getByUsername(String username);

    @Override
    @Insert("insert into User(id,password,role,state,username,email) values(#{id},#{password},#{role},#{state},#{username},#{email})")
    int insert(User entity);

//    @Insert("insert into user_quick_menu_items ")
    int cascadeInsert(User entity);

    @Override
    User selectById(Serializable id);
}
