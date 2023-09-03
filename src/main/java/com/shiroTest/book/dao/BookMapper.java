package com.shiroTest.book.dao;

import com.shiroTest.book.model.Book;
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
