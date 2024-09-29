package com.shiroTest.function.base;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shiroTest.common.Result;
import com.shiroTest.function.role.model.Authority;
import com.shiroTest.function.user.model.User4Display;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public Result getAll(@RequestParam(defaultValue = "1") int currentPage,
                         @RequestParam(defaultValue = "10") int pageSize){
        // 开始分页
        PageHelper.startPage(currentPage, pageSize);
        List<T> list = service.list();

        // 获取分页信息
        PageInfo<T> pageInfo = new PageInfo<>(list);

        // 在分页信息基础上进行处理
        List beforeReturnList = beforeReturnList(list);
        pageInfo.setList(beforeReturnList);

//        List beforeReturnList = beforeReturnList(list);
//        // 获取分页信息
//        PageInfo<T> pageInfo = new PageInfo<>(beforeReturnList);
        return Result.success(pageInfo);
    }

    protected List beforeReturnList(List<T> datas) {
        return datas;
    }

    @GetMapping("/find/{id}")
    public Result getById(@PathVariable("id") String id){
        T byId = service.getById(id);
        var t = beforeReturn(byId);
        return  Result.success(t);
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

    protected Object beforeReturn(T success) {
        return success;
    }


    @PostMapping("/create")
    public Result create(@RequestBody T data){
        boolean saveSuccess = service.save(data);
        if (saveSuccess){
            return Result.success(data.getId());
        }
        return Result.fail("save failed");
    }


    @PatchMapping("patch/{id}")
    public Result patchUserById(@PathVariable("id") String id,@RequestBody Map<String,Object> patchMap){
        checkSelfAuth(id,"_READ");

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
        checkSelfAuth(id,"_EDIT");

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
