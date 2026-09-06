package com.example.Manager;

import java.sql.Connection;
/// 表示当前事务的状态，可以扩展其他的传播模式
public class TransactionStatus {
    final Connection connection;

    public TransactionStatus(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
}
