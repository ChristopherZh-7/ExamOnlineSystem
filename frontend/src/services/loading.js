// according to https://www.hangge.com/blog/cache/detail_2554.html

import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import _ from 'lodash'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000, // 10s
})

let loading
let needLoadingRequestCount = 0

function showLoading(target) {
  if (needLoadingRequestCount === 0 && !loading) {
    loading = ElLoading.service({
      lock: true,
      text: '加载中...',
      background: 'rgba(255, 255, 255, 0.5)',
      target: target || 'main',
    })
  }
  needLoadingRequestCount++
}

function hideLoading() {
  needLoadingRequestCount = Math.max(--needLoadingRequestCount, 0) // 做个保护
  if (needLoadingRequestCount === 0) {
    toHideLoading()
  }
}

// 防抖：将 300ms 间隔内的关闭 loading 便合并为一次
var toHideLoading = _.debounce(() => {
  if (loading) {
    loading.close()
    loading = null
  }
}, 300)

http.interceptors.request.use(
  (request) => {
    if (request.headers.showLoading !== false) {
      showLoading(request.headers.loadingTarget)
    }
    return request
  },
  (err) => {
    if (request.headers.showLoading !== false) {
      hideLoading()
    }
    ElMessage.error('请求超时，请重试！')
    return Promise.resolve(err)
  },
)

http.interceptors.response.use(
  (response) => {
    if (response.config.headers.showLoading !== false) {
      hideLoading()
    }
    return response
  },
  (error) => {
    if (error.config.headers.showLoading !== false) {
      hideLoading()
    }
    let errorMessage = '发生未知错误'
    if (error.response && error.response.data) {
      try {
        if (typeof error.response.data === 'string') {
          const jsonObj = JSON.parse(error.response.data)
          errorMessage = jsonObj.message || '服务器返回了错误'
        } else if (typeof error.response.data === 'object') {
          errorMessage = error.response.data.message || '服务器返回了错误'
        }
      } catch (e) {
        console.error('解析错误响应失败:', e)
        errorMessage = error.response.data || '服务器返回了无效的错误信息'
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    ElMessage.error(errorMessage)
    return Promise.reject(error)
  },
)

export default http
