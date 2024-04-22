//package com.shiroTest.handler.typeHandler;
//
//import com.shiroTest.function.quickMenu.MenuItem;
//import org.apache.ibatis.type.BaseTypeHandler;
//import org.apache.ibatis.type.JdbcType;
//import java.sql.CallableStatement;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashSet;
//import java.util.Set;
//
//public class MenuItemSetTypeHandler extends BaseTypeHandler<Set<MenuItem>> {
//
//    @Override
//    public void setNonNullParameter(PreparedStatement ps, int i, Set<MenuItem> parameter, JdbcType jdbcType)
//            throws SQLException {
//        // 在这里将 Set<MenuItem> 转换为适当的数据库格式并设置参数
//        // 例如，可以将 MenuItem 转换为字符串，并将其以特定的格式存储在数据库中
//        // 例如，可以使用逗号分隔的字符串存储 MenuItem 的序数值
//        // ps.setString(i, convertMenuItemSetToString(parameter));
//    }
//
//    @Override
//    public Set<MenuItem> getNullableResult(ResultSet rs, String columnName) throws SQLException {
//        // 在这里从 ResultSet 中获取数据，并将其转换为 Set<MenuItem> 类型
//        // 例如，可以从逗号分隔的字符串中解析出 MenuItem 的序数值，并构建 Set<MenuItem> 对象
//        // return convertStringToMenuItemSet(rs.getString(columnName));
//        return null;
//    }
//
//    @Override
//    public Set<MenuItem> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
//        // 与上面相似，只是这里使用列索引而不是列名
//        return null;
//    }
//
//    @Override
//    public Set<MenuItem> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
//        // 与上面相似，只是这里使用 CallableStatement 而不是 ResultSet
//        return null;
//    }
//}
