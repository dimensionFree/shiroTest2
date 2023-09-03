package com.shiroTest.book.service.impl;

import com.shiroTest.book.model.Book;
import com.shiroTest.book.dao.BookMapper;
import com.shiroTest.book.service.IBookService;
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
