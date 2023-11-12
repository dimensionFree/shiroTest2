package com.shiroTest.function.book.service.impl;

import com.shiroTest.function.book.model.Book;
import com.shiroTest.function.book.dao.BookMapper;
import com.shiroTest.function.book.service.IBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author freedom
 * @since 2023-09-04
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements IBookService {

}
