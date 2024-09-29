package com.shiroTest.function.base;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
@Data
public class BaseEntity implements Serializable {
    @TableId(value = "id", type = IdType.UUID)
    private String id;
}
