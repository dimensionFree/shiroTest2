package com.shiroTest.function.book.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.shiroTest.function.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <p>
 * 
 * </p>
 *
 * @author freedom
 * @since 2023-09-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Table
@Entity
public class Book extends BaseEntity {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    @Id
    private String id;

    private String name;


}
