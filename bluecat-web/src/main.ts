import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'
import { permission, role } from './directives/permission'
import 'ant-design-vue/dist/reset.css'
import './styles/index.less'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

// 注册权限指令
app.directive('permission', permission)
app.directive('role', role)

app.mount('#app')
