package com.shiroTest.function.base;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shiroTest.common.Result;
import com.shiroTest.function.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

public class BaseController<T extends BaseEntity, S extends IService<T>> {
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
    public Result getById(@PathVariable("id") String id){
        return Result.success(service.getById(id));
    }


    @PostMapping
    public Result create(@RequestBody T data){
        return Result.success(service.save(data));
    }


    @PatchMapping("/{id}")
    public Result patchUserById(@PathVariable("id") String id,@RequestBody Map<String,Object> patchMap){
        UpdateWrapper<T> wrapper=new UpdateWrapper<>();
        wrapper.eq("id",id);
        for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
            wrapper.set(entry.getKey(),entry.getValue());
        }
        boolean update = service.update(wrapper);
        return Result.success(update);
    }


    @PutMapping("/{id}")
    public Result putUserById(@PathVariable("id") String id,@RequestBody T data){
        UpdateWrapper<T> wrapper=new UpdateWrapper<>();
        wrapper.eq("id",id);
        boolean update = service.update(data, wrapper);
        return Result.success(update);
    }
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable("id") String id){
        boolean delete = service.removeById(id);
        return Result.success(delete);
    }







}
