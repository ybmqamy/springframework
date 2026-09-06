package com.example.Manager;

import com.example.Exception.TransactionException;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

public class DataSourceTransactionManager implements platformTransactionManager, InvocationHandler {
    /// 上下文传递当前事务的状态信息
    public static final ThreadLocal<TransactionStatus> transactionStatus=new ThreadLocal<>();
    final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    /// required事务模式，没有事务的时候，创建一个事务，有事务的话就加入当前事务
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TransactionStatus transactionStatus1 = transactionStatus.get();
        /// 如果等于空，说明当前无事务，创建一个事务
        System.out.println("走到了这里，事务实现了");
        if(transactionStatus1==null){
            try(Connection connection=dataSource.getConnection()){
                final boolean autoCommit = connection.getAutoCommit();
                ///如果当前手动提交，改为手动提交
                if(autoCommit){
                    connection.setAutoCommit(false);
                }
                try{
                    TransactionStatus transactionStatus2 = new TransactionStatus(connection);
                    transactionStatus.set(transactionStatus2);
                    Object invoke = method.invoke(proxy, args); ///调用业务方法，
                    System.out.println("事务正常执行");
                    connection.commit();
                    return invoke;
                }catch (InvocationTargetException e){
                    TransactionException transactionException = new TransactionException(e.getMessage());
                    System.err.println("事务出错了，需要回滚");
                    try {
                        connection.rollback();
                    }catch (Exception ee){
                        transactionException.addSuppressed(ee);
                    }
                    throw e.getCause();  /// 把业务异常抛 出去，而不是吞掉后返回 null
                }finally {
                    transactionStatus.remove();  ///事务结束，移除该事务的状态
                    if(autoCommit) {
                        connection.setAutoCommit(true);
                    }
                }
            }
        }
        else{
            return method.invoke(proxy,args); ///如果不为空，说明直接加入这个事务
        }
    }
}
