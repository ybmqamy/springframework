package com.example.Jdbc;

import javax.annotation.Nullable;
import java.sql.Connection;

@FunctionalInterface
public interface ConnectionCallback<T>{
    @Nullable
    T doInConnection(Connection con);
}
