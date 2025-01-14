import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import qs from 'qs'
import elementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as icons from '@element-plus/icons-vue'
import Vue3Storage from 'vue3-storage'
import * as echarts from 'echarts'
import http from './services/loading.js'
import autocounter from 'vue3-autocounter'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const app = createApp(App)
app.config.globalProperties.$qs = qs
// 融合了loading
// headers: { 'showLoading': false }
// headers: { 'loadingTarget': 元素名 }
app.config.globalProperties.$axios = http


// locale保证ElementUI为中文（默认英文）
app.use(elementPlus, { locale: zhCn })
Object.keys(icons).forEach((key) => {
  app.component(key, icons[key])
})
app.config.globalProperties.$echarts = echarts
app.component('vue3-autocounter', autocounter)

app.use(Vue3Storage)
app.use(router)
app.mount('#app')
