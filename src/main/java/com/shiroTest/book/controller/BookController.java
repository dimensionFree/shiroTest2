package com.shiroTest.book.controller;


import com.shiroTest.book.model.Book;
import com.shiroTest.book.service.IBookService;
import com.shiroTest.book.service.impl.BookServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.shiroTest.base.BaseController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author freedom
 * @since 2023-09-04
 */
@RestController
@RequestMapping("/book")
public class BookController extends BaseController<Book, BookServiceImpl> {

}

