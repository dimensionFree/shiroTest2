package com.shiroTest.function.base;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shiroTest.common.Result;
import com.shiroTest.function.user.model.User;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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


    @GetMapping("/findAll")
    public Result getAll(){
        return beforeReturnList(Result.success(service.list()));
    }

    protected Result beforeReturnList(Result success) {
        return success;
    }

    @GetMapping("/find/{id}")
    public Result getById(@PathVariable("id") String id){
        checkAuth(id);
        return beforeReturn(Result.success(service.getById(id))) ;
    }

    protected void checkAuth(String id) {

    }

    protected Result beforeReturn(Result success) {
        return success;
    }


    @PostMapping("/create")
    public Result create(@RequestBody T data){

        return Result.success(service.save(data));
    }


    @PatchMapping("patch/{id}")
    public Result patchUserById(@PathVariable("id") String id,@RequestBody Map<String,Object> patchMap){
        UpdateWrapper<T> wrapper=new UpdateWrapper<>();
        wrapper.eq("id",id);
        for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
            wrapper.set(entry.getKey(),entry.getValue());
        }
        boolean update = service.update(wrapper);
        return Result.success(update);
    }


    @PutMapping("put/{id}")
    public Result putUserById(@PathVariable("id") String id,@RequestBody T data){
        UpdateWrapper<T> wrapper=new UpdateWrapper<>();
        wrapper.eq("id",id);
        boolean update = service.update(data, wrapper);
        return Result.success(update);
    }
    @DeleteMapping("delete/{id}")
    public Result deleteById(@PathVariable("id") String id){
        boolean delete = service.removeById(id);
        return Result.success(delete);
    }



    @GetMapping("/needUserEdit")
    public Result test(){
        return Result.success(true);
    }




}
