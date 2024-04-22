//package com.shiroTest.function.base;
//
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public class BaseService<M extends JpaRepository<T,String>,T extends BaseEntity>{
//    @Autowired
//    M mapper;
//
//    public List<T> list(){
//        return mapper.findAll();
//    }
//
//
//    public Optional<T> getById(String id) {
//        return mapper.findById(id);
//    }
//
//    public T save(T data) {
//        return mapper.save(data);
//    }
//
//    public <T extends BaseEntity> boolean update(UpdateWrapper<T> wrapper) {
//        mapper.
//    }
//}
