package com.zj.examsystem.controller;

import com.zj.examsystem.entity.KnowledgeFrame;
import com.zj.examsystem.service.KnowledgeFrameService;
import com.zj.examsystem.utils.response.BaseResponseEntity;
import com.zj.examsystem.utils.response.ResponseCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/knowledgeFrame")
public class KnowledgeFrameController {
    @Resource
    private KnowledgeFrameService knowledgeFrameService;

    @GetMapping("/loadKnowledgeBySubjectId")
    @ResponseBody
    public BaseResponseEntity<List<Map<String, Object>>> loadKnowledgeBySubjectId(Integer subjectId) {
        List<KnowledgeFrame> knowledgeFrames = knowledgeFrameService.loadKnowledgeBySubjectId(subjectId);
        List<Map<String, Object>> result = buildKnowledgeFrameTree(knowledgeFrames);
        return BaseResponseEntity.ok("", result);
    }

    private List<Map<String, Object>> buildKnowledgeFrameTree(List<KnowledgeFrame> frames) {
        Map<Integer, Map<String, Object>> nodeMap = new HashMap<>();
        List<Map<String, Object>> rootNodes = new ArrayList<>();

        for (KnowledgeFrame frame : frames) {
            Map<String, Object> node = convertFrameToMap(frame);
            nodeMap.put(frame.getChapterId(), node);
        }

        for (Map<String, Object> node : nodeMap.values()) {
            Integer parentId = (Integer) node.get("chapterParentId");
            if (parentId == 0) {
                rootNodes.add(node);
            } else {
                Map<String, Object> parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = parentNode.containsKey("children") ?
                        (List<Map<String, Object>>) parentNode.get("children") : new ArrayList<>();
                    parentNode.put("children", children);
                    children.add(node);
                }
            }
        }

        return rootNodes;
    }

    private Map<String, Object> convertFrameToMap(KnowledgeFrame frame) {
        Map<String, Object> map = new HashMap<>();
        map.put("chapterId", frame.getChapterId());
        map.put("chapterParentId", frame.getChapterParentId());
        map.put("content", frame.getContent());
        map.put("isKnowledge", frame.getIsKnowledge());
        map.put("subjectId", frame.getSubjectId());
        return map;
    }

    @GetMapping("/loadKnowledgeFrameBySubjectId")
    @ResponseBody
    public BaseResponseEntity<List<Map<String, Object>>> loadKnowledgeFrameBySubjectId(Integer subjectId) {
        return BaseResponseEntity.ok("", knowledgeFrameService.loadKnowledgeFrameBySubjectId(subjectId));
    }

    @PostMapping(value = "/save")
    @ResponseBody
    public BaseResponseEntity<Integer> save(String[] knowledgeFrames, Integer subjectId) {
        List<KnowledgeFrame> knowledgeFrameList = new ArrayList<>();
        for (String s : knowledgeFrames) {
            String[] str = s.split(",");
            knowledgeFrameList.add(new KnowledgeFrame("null".equals(str[0]) ? null : Integer.parseInt(str[0]), Integer.parseInt(str[1]), str[2],
                    Boolean.parseBoolean(str[3]) ? 1 : 0,
                    Integer.parseInt(str[4])));
        }
        int result = knowledgeFrameService.saveKnowledgeFrameList(knowledgeFrameList, subjectId);
        return result != 0 ? BaseResponseEntity.ok("修改成功", result) : BaseResponseEntity.error(ResponseCode.FAIL, "修改失败");
    }
}
