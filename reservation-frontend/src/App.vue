<template>
  <main class="app-shell">
    <section class="topbar">
      <div>
        <h1>企业会议室预约系统</h1>
        <p>会议室管理、排班配置、预约审核和并发冲突控制</p>
      </div>
      <el-button type="primary" @click="checkHealth">检查后端</el-button>
    </section>

    <section class="grid">
      <el-card v-for="item in modules" :key="item.title" shadow="never">
        <h2>{{ item.title }}</h2>
        <p>{{ item.description }}</p>
      </el-card>
    </section>

    <el-alert
      v-if="healthMessage"
      :title="healthMessage"
      type="success"
      show-icon
      :closable="false"
    />
  </main>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ref } from 'vue'

const healthMessage = ref('')

const modules = [
  {
    title: '会议室管理',
    description: '维护会议室容量、位置、设备标签和启用状态。'
  },
  {
    title: '预约申请',
    description: '按会议室和时间段创建预约，后端负责冲突检测。'
  },
  {
    title: '权限控制',
    description: '普通员工、审核员、管理员拥有不同操作权限。'
  },
  {
    title: '异步通知',
    description: '通过 RabbitMQ 解耦预约流程和通知流程。'
  }
]

async function checkHealth() {
  const response = await axios.get('/api/health')
  healthMessage.value = `后端状态：${response.data.status}`
}
</script>

