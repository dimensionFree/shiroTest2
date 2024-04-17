package com.shiroTest.function.book.service.impl;

import com.shiroTest.function.book.model.Book;
import com.shiroTest.function.book.dao.BookMapper;
import com.shiroTest.function.book.service.IBookService;
import com.shiroTest.function.base.BaseService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author freedom
 * @since 2024-04-17
 */
@Service
public class BookServiceImpl extends BaseService<BookMapper, Book> implements IBookService {

}
