package com.example.Jdbc;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface PreparedStatementCallback<T>{
    T doInPreparedStatement(PreparedStatement ps);
}
