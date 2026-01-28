package com.example.excelrag.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CustomExecuteSqlMapper {

    @Select("${sql}")
    List<Map<String,Object>> execute(@Param("sql") String sql);

}
