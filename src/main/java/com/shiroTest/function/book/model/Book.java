package com.shiroTest.function.book.model;

import com.shiroTest.function.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author freedom
 * @since 2024-04-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class Book extends BaseEntity {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    private String name;


}
