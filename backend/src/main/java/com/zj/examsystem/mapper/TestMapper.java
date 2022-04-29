package com.zj.examsystem.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zj.examsystem.entity.Test;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;


public interface TestMapper extends BaseMapper<Test> {
    IPage<Test> selectPageWithClazzAndSubject(Page<Test> page, @Param(Constants.WRAPPER) QueryWrapper<Test> queryWrapper);
}
