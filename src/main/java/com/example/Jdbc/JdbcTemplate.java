package com.example.Jdbc;

import com.example.Utils.TransactionalUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/// 对数据进一步处理是mybatis的功能，而不是jdbctemplate的功能
public class JdbcTemplate {
    final DataSource dataSource;
    public JdbcTemplate(DataSource dataSource){
        this.dataSource=dataSource;
    }
    /// doInConnection 执行完（或抛异常）后会自动调 connection.close()，但因为是池化连接，这个 close() 并不是真正关掉底层
    ///   socket，而是把连接归还给连接池
    /// 这里通过回调函数方式，来处理sql的连接，方便数据库连接的打开与关闭，我们不用每次都自己new
    /// 这也是整个spring在用的模式
    /// 这里传入一个action，action需要传入一个连接对象，我这里先生成一个连接对象，直接返回给外界。我直接在外面就可以用这个连接对象
    /// 相当于我内部创建，然后暴露给外部

    /// 如果当前的connection不为空，那么就说明我应该使用当前的事务
    public <T> T execute(ConnectionCallback<T> action){
        Connection connection = TransactionalUtils.getcurrentConnection();
        if(connection!=null){
            try {
                return action.doInConnection(connection);
            }catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }
        }///等于空创建一个
        try(Connection con=dataSource.getConnection()) {
            return action.doInConnection(con);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    /// 回调套回调，我通过第一个execute回调，拿到connection对象，然后有了connection，我再传入另一个回调 PreparedStatementCallbac
    /// 然后这个里面我又拿到了PreparedStatement这个东西，最终相当于我要对这个操作
    /// 那PreparedStatement是干啥的呢，执行查询语句的,这样外界再使用就不用再创建了，直接回调就可以使用
    /// 这就体现了template封装的意义
    /// 通过try with resources方式创建需要的对象
    public <T> T execute(PreparedStatementCreator psc,PreparedStatementCallback action){
        return (T) execute((Connection con) -> {
            try (PreparedStatement ps = psc.createPreparedStatement(con)) {
                return action.doInPreparedStatement(ps);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    /// 回调套回调目的是把"打开/关闭连接、PS、结果集"这套样板代码收死在模板里，
    /// 业务方只写 sql 和 mapRow，不用碰任何资源管理。这正是 Spring 模板方法模式的核心。
    /// rowmapper把对应的结果集转换为java对象
    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(preparedStatementCreator(sql, args),
                (PreparedStatement ps) -> {
                    List<T> list = new ArrayList<>();
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(rowMapper.mapRow(rs, rs.getRow()));
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return list;
                }
        );
    }

    private PreparedStatementCreator preparedStatementCreator(String sql, Object... args) {
        return (Connection con) -> {
            var ps = con.prepareStatement(sql);
            bindArgs(ps, args);
            return ps;
        };
    }
    /// 负责把？全部转换为对应的参数，把参数填入
    private void bindArgs(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    public int updata(String sql, Object...args){
        return execute((Connection con) -> {
            try (PreparedStatement ps = preparedStatementCreator(sql, args).createPreparedStatement(con)) {
                int rows = ps.executeUpdate();
                return rows;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public int delete(String sql,Object...args){
        return execute((con )->{
        try (PreparedStatement ps = preparedStatementCreator(sql, args).createPreparedStatement(con)){
            int row = ps.executeUpdate();
            return row;
        }catch (Exception s){
            throw new RuntimeException(s);
        }
        });
    }
    public int insert(String sql,Object...args){
        return execute((con )->{
            try (PreparedStatement ps = preparedStatementCreator(sql, args).createPreparedStatement(con)){
                int row = ps.executeUpdate();
                return row;
            }catch (Exception s){
                throw new RuntimeException(s);
            }
        });
    }

}
