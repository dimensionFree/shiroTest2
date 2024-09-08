package com.shiroTest.function.article.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.shiroTest.function.base.BaseAuditableEntity;
import com.shiroTest.function.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class Article extends BaseAuditableEntity {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.UUID)
    @Id
    private String id;

    private String title;

    private String preface;

    private String content;


    public String getPreface() {
        if (StringUtils.isNotEmpty( preface)){
            return preface;
        }
        if (StringUtils.isEmpty( content)){
            return "empty preface";
        }
        // 使用正则表达式匹配句子，保留标点符号
        Pattern pattern = Pattern.compile("([^.。!！?？]+[.。!！?？])");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1); // 返回第一个匹配的句子
        } else {
            return "No sentence found";
        }
    }
}
