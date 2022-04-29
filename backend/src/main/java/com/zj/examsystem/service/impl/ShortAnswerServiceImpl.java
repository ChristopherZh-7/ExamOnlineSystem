package com.zj.examsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.zj.examsystem.entity.ShortAnswer;
import com.zj.examsystem.mapper.ShortAnswerMapper;
import com.zj.examsystem.service.ShortAnswerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("all")
public class ShortAnswerServiceImpl extends ServiceImpl<ShortAnswerMapper, ShortAnswer> implements ShortAnswerService {
    @Autowired
    private ShortAnswerMapper shortAnswerMapper;

}
