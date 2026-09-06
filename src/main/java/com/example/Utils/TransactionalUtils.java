package com.example.Utils;

import com.example.Manager.DataSourceTransactionManager;
import com.example.Manager.TransactionStatus;

import javax.annotation.Nullable;
import java.sql.Connection;
/// 拿到当前的连接
public class TransactionalUtils {
    @Nullable
    public static Connection getcurrentConnection(){
        TransactionStatus transactionStatus = DataSourceTransactionManager.transactionStatus.get();
        return transactionStatus==null?null:transactionStatus.getConnection();
    }
}
