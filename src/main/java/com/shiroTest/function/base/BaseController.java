package com.shiroTest.function.base;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shiroTest.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class BaseController<T extends BaseEntity,S extends IService<T>  > {
    @Autowired
    S service;

    public S getService() {
        return service;
    }

    private String getUrl() {
        return "";
    }


    @GetMapping
    public List<T> getAll(){
        return service.list();
    }

    @GetMapping("/{id}")
    public T getById(@PathVariable("id") String id){
        return service.getById(id);
    }


    @PostMapping
    public boolean create(@RequestBody T data){
        return service.save(data);
    }


//    @PatchMapping("/{id}")
//    public Result patchUser(){
//    }





}
