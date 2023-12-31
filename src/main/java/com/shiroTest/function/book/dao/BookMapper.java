package com.shiroTest.function.book.dao;

import com.shiroTest.function.book.model.Book;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author freedom
 * @since 2023-09-04
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

}
