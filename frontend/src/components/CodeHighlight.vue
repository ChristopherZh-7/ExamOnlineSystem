<template>
  <div class="code-highlight">
    <div class="code-header">
      <span class="language-tag">{{ language }}</span>
      <div class="code-actions">
        <el-button size="small" type="text" @click="copyCode">
          <i class="el-icon-document-copy"></i>
          复制代码
        </el-button>
      </div>
    </div>
    <div class="code-container">
      <div class="line-numbers">
        <span v-for="line in lineCount" :key="line">{{ line }}</span>
      </div>
      <pre><code :class="language" ref="code">{{ code }}</code></pre>
    </div>
  </div>
</template>

<script>
import hljs from 'highlight.js';
import 'highlight.js/styles/github-dark.css';

export default {
  name: 'CodeHighlight',
  props: {
    code: {
      type: String,
      required: true
    },
    language: {
      type: String,
      default: 'javascript'
    }
  },
  computed: {
    lineCount() {
      return this.code.split('\n').length;
    }
  },
  mounted() {
    this.highlightCode();
  },
  watch: {
    code() {
      this.highlightCode();
    },
    language() {
      this.highlightCode();
    }
  },
  methods: {
    highlightCode() {
      const code = this.$refs.code;
      if (code) {
        hljs.highlightElement(code);
      }
    },
    copyCode() {
      navigator.clipboard.writeText(this.code).then(() => {
        this.$message({
          message: '代码已复制到剪贴板',
          type: 'success',
          duration: 2000
        });
      });
    }
  }
};
</script>

<style scoped>
.code-highlight {
  margin: 10px 0;
  border-radius: 8px;
  overflow: hidden;
  background-color: #0d1117;
  border: 1px solid #30363d;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background-color: #161b22;
  border-bottom: 1px solid #30363d;
}

.language-tag {
  color: #8b949e;
  font-size: 12px;
  text-transform: uppercase;
  font-family: 'Monaco', monospace;
}

.code-actions {
  opacity: 0.7;
}

.code-actions:hover {
  opacity: 1;
}

.code-container {
  display: flex;
  overflow: auto;
}

.line-numbers {
  padding: 16px 12px;
  border-right: 1px solid #30363d;
  background-color: #0d1117;
  user-select: none;
  text-align: right;
  color: #8b949e;
}

.line-numbers span {
  display: block;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
}

pre {
  margin: 0;
  padding: 16px;
  flex-grow: 1;
  overflow: visible;
  background-color: #0d1117;
}

code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
  color: #c9d1d9;
}

:deep(.el-button--text) {
  color: #8b949e;
}

:deep(.el-button--text:hover) {
  color: #58a6ff;
}
</style> 