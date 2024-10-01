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


public class Article extends BaseAuditableEntity {

    private static final long serialVersionUID=1L;



    private String title="";

    private String preface="";

    private String content="";


    // 去除常见 Markdown 标记的方法
    public static String removeMarkdownSyntax(String text) {
        // 正则表达式用于去除 Markdown 语法（#、*、-、>、等）
        return text.replaceAll("(?m)^#+\\s*", "")  // 移除标题标记，例如 ## 或 ###
                .replaceAll("\\*+|_+|`+", "")  // 移除加粗、斜体和代码块符号
                .replaceAll("^>\\s*", "")      // 移除引用符号 ">"
                .replaceAll("-{3,}", "");      // 移除水平分割线 "---"
    }

    public String getPreface() {
        if (StringUtils.isNotEmpty( preface)){
            return preface;
        }
        if (StringUtils.isEmpty( content)){
            return "empty preface";
        }

        // 先移除 Markdown 语法
        String cleanedText = removeMarkdownSyntax(content);

        // 正则表达式：匹配以句号、问号、感叹号结尾的句子，支持换行
        Pattern sentencePattern = Pattern.compile("[^.。!?！？\\n]+[.。!?！？\\n]");

        // 使用 Matcher 查找匹配的句子
        Matcher matcher = sentencePattern.matcher(cleanedText);

        if (matcher.find()) {
            // 找到第一个句子并返回
            return matcher.group(0).trim();
        } else {
            // 如果没有标点符号，则返回第一行
            int firstNewlineIndex = cleanedText.indexOf("\n");
            if (firstNewlineIndex != -1) {
                return cleanedText.substring(0, firstNewlineIndex).trim();
            } else {
                // 如果没有换行符，则返回整个文本
                String firstSentence = cleanedText.trim();
                return StringUtils.isEmpty(firstSentence)? "No sentence found":firstSentence;
            }
        }
    }
}
