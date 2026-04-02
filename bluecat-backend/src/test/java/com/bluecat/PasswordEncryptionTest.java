package com.bluecat;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码加密测试工具
 * 用于生成 BCrypt 加密后的密码和可直接执行的 SQL 语句
 *
 * @author BlueCat
 * @since 2026-04-02
 */
public class PasswordEncryptionTest {

    /**
     * 主方法
     * 修改下面的用户名和密码，运行即可生成对应的加密密码和 SQL 语句
     */
    public static void main(String[] args) {
        // ========== 在这里修改用户名和密码 ==========
        String username = "admin";
        String password = "123456";
        String realName = "管理员";
        String phone = "13800138000";
        String email = "admin@bluecat.com";
        // ===========================================

        // 使用 BCrypt 加密密码
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // 打印结果
        System.out.println("========== 加密结果 ==========");
        System.out.println("用户名：" + username);
        System.out.println("原始密码：" + password);
        System.out.println("加密后密码：" + hashedPassword);
        System.out.println();

        // 生成 INSERT SQL 语句
        System.out.println("========== INSERT SQL ==========");
        String insertSql = String.format(
                "INSERT INTO sys_user (username, password, real_name, phone, email, status, create_time, update_time) " +
                        "VALUES ('%s', '%s', '%s', '%s', '%s', 1, NOW(), NOW());",
                username, hashedPassword, realName, phone, email
        );
        System.out.println(insertSql);
        System.out.println();

        // 生成 UPDATE SQL 语句（如果用户已存在）
        System.out.println("========== UPDATE SQL ==========");
        String updateSql = String.format(
                "UPDATE sys_user SET password = '%s', real_name = '%s', phone = '%s', email = '%s', update_time = NOW() " +
                        "WHERE username = '%s';",
                hashedPassword, realName, phone, email, username
        );
        System.out.println(updateSql);
        System.out.println();

        // 验证加密
        System.out.println("========== 验证加密 ==========");
        boolean check1 = BCrypt.checkpw(password, hashedPassword);
        boolean check2 = BCrypt.checkpw("wrong_password", hashedPassword);
        System.out.println("验证原始密码：" + check1 + " (应为 true)");
        System.out.println("验证错误密码：" + check2 + " (应为 false)");
        System.out.println();

        // 生成更多测试用户的 SQL
        System.out.println("========== 批量插入示例 SQL ==========");
        generateBatchInsertSQL();
    }

    /**
     * 生成批量插入的示例 SQL
     */
    private static void generateBatchInsertSQL() {
        String[][] users = {
                {"admin", "admin123", "超级管理员", "13800138000", "admin@bluecat.com"},
                {"user1", "user123", "普通用户 1", "13800138001", "user1@bluecat.com"},
                {"user2", "user123", "普通用户 2", "13800138002", "user2@bluecat.com"},
                {"test", "test123", "测试用户", "13800138003", "test@bluecat.com"}
        };

        StringBuilder sql = new StringBuilder("INSERT INTO sys_user (username, password, real_name, phone, email, status, create_time, update_time) VALUES\n");
        for (int i = 0; i < users.length; i++) {
            String[] user = users[i];
            String hashedPwd = BCrypt.hashpw(user[1], BCrypt.gensalt());
            sql.append(String.format(
                    "('%s', '%s', '%s', '%s', '%s', 1, NOW(), NOW())",
                    user[0], hashedPwd, user[2], user[3], user[4]
            ));
            if (i < users.length - 1) {
                sql.append(",\n");
            } else {
                sql.append(";");
            }
        }
        System.out.println(sql);
    }
}
