package com.shiroTest.function.book.controller;


import com.shiroTest.function.book.model.Book;
import com.shiroTest.function.book.service.impl.BookServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.shiroTest.function.base.BaseController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author freedom
 * @since 2024-04-22
 */
@RestController
@RequestMapping("/book")
public class BookController extends BaseController<Book, BookServiceImpl> {

}

