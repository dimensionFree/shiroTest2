package com.shiroTest.function.base;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shiroTest.common.Result;
import com.shiroTest.function.role.model.Authority;
import com.shiroTest.function.user.model.User4Display;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

public class BaseController<T extends BaseAuditableEntity, S extends IService<T>> {
    public static final String SELF_AUTH_SUFFIX = "_SELF";
    @Autowired
    S service;

    Class<T> classOfT;

    public BaseController() {
    }

    public BaseController(Class<T> classOfT) {
        this.classOfT = classOfT;
    }

    public S getService() {
        return service;
    }

    private String getUrl() {
        return "";
    }

    public Class<T> getClassOfT(){
        return classOfT;
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
        return beforeReturn(Result.success(service.getById(id))) ;
    }

    protected void checkSelfAuth(String resourceId,String authStr) {
        User4Display loginInfo = (User4Display) SecurityUtils.getSubject().getPrincipal();
        Authority bigAuth = getAuth(authStr);
        Set<Authority> haveAuthorities = loginInfo.getRole().getAuthorities();
        if (!haveAuthorities.contains(bigAuth)) {
            if (haveAuthorities.contains(getAuth(authStr+ SELF_AUTH_SUFFIX))){
                T byId = service.getById(resourceId);
                String creatorId = byId.getCreatedBy();
                if (!loginInfo.getId().equals(creatorId)){
                    throw new AuthenticationException("只有self权限，无法访问其他人的资源");
                }
            }
        }

    }

    private Authority getAuth(String authStr) {
        return Authority.valueOf(classOfT.getSimpleName().toUpperCase()+ authStr);
    }
    private Authority getReadAuth() {
        return getAuth("_READ");
    }
    private Authority getReadSelfAuth() {
        return getAuth("_READ_SELF");
    }
    private Authority getEditAuth() {
        return getAuth("_EDIT");
    }
    private Authority getEditSelfAuth() {
        return getAuth("_EDIT_SELF");
    }

    protected Result beforeReturn(Result success) {
        return success;
    }


    @PostMapping("/create")
    public Result create(@RequestBody T data){
        boolean save = service.save(data);
        return Result.success(save);
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


    @PutMapping("update/{id}")
    public Result putUserById(@PathVariable("id") String id,@RequestBody T data){
        checkSelfAuth(id,"_READ");

        UpdateWrapper<T> wrapper=new UpdateWrapper<>();
        wrapper.eq("id",id);
        boolean update = service.update(data, wrapper);
        return Result.success(update);
    }
    @DeleteMapping("delete/{id}")
    public Result deleteById(@PathVariable("id") String id){
        checkSelfAuth(id,"_EDIT");
        boolean delete = service.removeById(id);
        return Result.success(delete);
    }

}
