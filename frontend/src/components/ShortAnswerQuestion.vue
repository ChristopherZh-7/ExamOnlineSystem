<template>
  <div>
    <div class="topic">
      <span class="number">{{ number }}</span>
      <div v-if="isCodeContent(title)" class="title-code">
        <code-highlight 
          :code="formatCode(title)" 
          :language="detectLanguage(title)"
        ></code-highlight>
      </div>
      <div v-else>{{ title }}</div>
    </div>
    
    <div v-if="!isable" class="answer-section">
      <el-input
        v-model="text"
        type="textarea"
        :autosize="{ minRows: 10 }"
        maxlength="50000"
        show-word-limit
        @blur="sendAnswer(text)"
        placeholder="请输入答案..."
      />
    </div>
    
    <div v-else class="text-content">{{ text }}</div>
  </div>
</template>

<script>
import CodeHighlight from './CodeHighlight.vue';

export default {
  components: {
    CodeHighlight
  },
  props: {
    number: Number,
    title: String,
    content: String,
    questionId: Number,
    isable: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      text: "",
    };
  },
  created() {
    if (this.content != undefined) {
      this.text = this.content.split(" ")[1];
    } else {
      this.text = "";
    }
  },
  methods: {
    sendAnswer(val) {
      this.$emit("returnAnswer", this.questionId + " " + val);
    },
    isCodeContent(text) {
      if (!text) return false;
      
      // 检查是否包含代码特征
      const codePatterns = [
        // 检查是否有连续的缩进行
        /(?:\n\s{2,}.*){2,}/,
        // 检查常见的代码开始标记
        /^(public\s+class|import\s+|def\s+|function\s+|#include|SELECT\s+|CREATE\s+TABLE|<!DOCTYPE|<html|<?php)/m,
        // 检查代码块标记
        /```[\s\S]*?```/,
        // 检查括号和分号的组合
        /[{};]\s*\n.*[{};]/,
        // 检查常见的编程语言关键字组合
        /(if|for|while|switch)\s*\([^)]*\)\s*{/,
        // 检查函数定义
        /\b(function|def|public|private|protected)\s+\w+\s*\([^)]*\)/,
        // 检查变量声明
        /(var|let|const|int|string|boolean|float|double)\s+\w+\s*=/,
        // 检查类定义
        /class\s+\w+(\s+extends\s+\w+)?(\s+implements\s+\w+)?\s*{/,
        // 检查SQL语句
        /SELECT\s+.*\s+FROM\s+\w+/i,
        // 检查HTML标签
        /<\/?[a-z][\s\S]*>/i
      ];
      
      return codePatterns.some(pattern => pattern.test(text));
    },
    formatCode(code) {
      // 如果代码被```包围，去除它
      code = code.replace(/^```[\w]*\n([\s\S]*?)```$/m, '$1');
      return code.trim();
    },
    detectLanguage(code) {
      if (!code) return 'plaintext';
      
      // 检查是否被```language包围
      const langMatch = code.match(/^```(\w+)/);
      if (langMatch) {
        return langMatch[1];
      }
      
      // 通过代码特征检测语言
      if (code.includes('public class') || code.includes('private class') || /\w+\s+\w+\([^)]*\)\s*{/.test(code)) {
        return 'java';
      } else if (code.includes('def ') || code.includes('print(') || /^\s*import\s+\w+/.test(code)) {
        return 'python';
      } else if (code.includes('function') || /const|let|var/.test(code)) {
        return 'javascript';
      } else if (code.includes('#include') || /int\s+main\s*\(/.test(code)) {
        return 'cpp';
      } else if (code.match(/<?php/)) {
        return 'php';
      } else if (code.match(/<html|<!DOCTYPE/i)) {
        return 'html';
      } else if (/SELECT|INSERT|UPDATE|DELETE|CREATE\s+TABLE/i.test(code)) {
        return 'sql';
      }
      
      return 'plaintext';
    }
  },
};
</script>

<style scoped>
.topic {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.number {
  display: inline-block;
  width: 25px;
  height: 25px;
  line-height: 25px;
  text-align: center;
  background-color: #409eff;
  color: white;
  border-radius: 50%;
  margin-right: 10px;
}

.text-content {
  padding: 16px;
  background-color: #f6f8fa;
  border-radius: 8px;
  border: 1px solid #e1e4e8;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.toolbar {
  margin-bottom: 10px;
}

.code-input-wrapper {
  position: relative;
}

.code-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  z-index: 1;
}

.code-input :deep(.el-textarea__inner) {
  height: 100% !important;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.answer-section {
  margin-bottom: 20px;
}

.title-code {
  margin-top: 10px;
  margin-bottom: 20px;
}

.title-code :deep(.hljs) {
  background-color: #f6f8fa;
  padding: 16px;
  border-radius: 8px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}
</style>