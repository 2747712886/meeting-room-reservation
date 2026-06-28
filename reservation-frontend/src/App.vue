<template>
  <main class="app-shell">
    <section v-if="!token" class="login-layout">
      <div class="login-copy">
        <p class="eyebrow">Meeting Room Reservation</p>
        <h1>会议室预约工作台</h1>
        <p>登录后可创建预约、跟踪审批状态、查看通知。管理员可以处理待审批申请。</p>
      </div>

      <el-form class="login-panel" label-position="top" @submit.prevent="login">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            autocomplete="current-password"
            show-password
          />
        </el-form-item>
        <el-button type="primary" class="full-button" :loading="loading.login" @click="login">
          登录
        </el-button>
      </el-form>
    </section>

    <template v-else>
      <header class="topbar">
        <div>
          <p class="eyebrow">Reservation Console</p>
          <h1>会议室预约工作台</h1>
        </div>
        <div class="user-actions">
          <el-tag type="info">{{ currentUser?.username }}</el-tag>
          <el-tag v-if="isAdmin" type="success">ADMIN</el-tag>
          <el-button @click="logout">退出</el-button>
        </div>
      </header>

      <section class="metrics">
        <div class="metric">
          <span>会议室</span>
          <strong>{{ rooms.length }}</strong>
        </div>
        <div class="metric">
          <span>我的预约</span>
          <strong>{{ myAppointments.length }}</strong>
        </div>
        <div class="metric">
          <span>待审批</span>
          <strong>{{ pendingAppointments.length }}</strong>
        </div>
        <div class="metric">
          <span>未读通知</span>
          <strong>{{ unreadCount }}</strong>
        </div>
      </section>

      <el-tabs v-model="activeTab" class="workspace-tabs">
        <el-tab-pane label="会议室" name="rooms">
          <section class="content-grid">
            <el-card class="tool-panel" shadow="never">
              <template #header>
                <span>创建预约</span>
              </template>
              <el-form label-position="top">
                <el-form-item label="会议室">
                  <el-select v-model="appointmentForm.roomId" class="full-width" placeholder="选择会议室">
                    <el-option
                      v-for="room in enabledRooms"
                      :key="room.id"
                      :label="`${room.name} / ${room.floor} / ${room.capacity}人`"
                      :value="room.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="主题">
                  <el-input v-model="appointmentForm.subject" maxlength="100" />
                </el-form-item>
                <el-form-item label="开始时间">
                  <el-date-picker
                    v-model="appointmentForm.startTime"
                    class="full-width"
                    type="datetime"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                  />
                </el-form-item>
                <el-form-item label="结束时间">
                  <el-date-picker
                    v-model="appointmentForm.endTime"
                    class="full-width"
                    type="datetime"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                  />
                </el-form-item>
                <el-button type="primary" :loading="loading.createAppointment" @click="createAppointment">
                  提交预约
                </el-button>
              </el-form>
            </el-card>

            <el-card shadow="never">
              <template #header>
                <div class="card-header">
                  <span>会议室列表</span>
                  <el-button size="small" @click="loadRooms">刷新</el-button>
                </div>
              </template>
              <el-table :data="rooms" height="430">
                <el-table-column prop="name" label="名称" min-width="140" />
                <el-table-column prop="floor" label="楼层" width="110" />
                <el-table-column prop="capacity" label="容量" width="90" />
                <el-table-column label="设备" min-width="160">
                  <template #default="{ row }">
                    <el-tag v-if="row.hasProjector" size="small">投影</el-tag>
                    <el-tag v-if="row.hasWhiteboard" size="small" class="tag-gap">白板</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="90">
                  <template #default="{ row }">
                    <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                      {{ row.enabled ? '启用' : '停用' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </section>
        </el-tab-pane>

        <el-tab-pane label="我的预约" name="appointments">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>我的预约</span>
                <el-button size="small" @click="loadMyAppointments">刷新</el-button>
              </div>
            </template>
            <el-table :data="myAppointments" height="500">
              <el-table-column prop="subject" label="主题" min-width="160" />
              <el-table-column label="会议室" width="130">
                <template #default="{ row }">{{ roomName(row.roomId) }}</template>
              </el-table-column>
              <el-table-column label="时间" min-width="260">
                <template #default="{ row }">{{ formatDate(row.startTime) }} - {{ formatDate(row.endTime) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="原因" min-width="160">
                <template #default="{ row }">{{ row.rejectReason || row.cancelReason || '-' }}</template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status === 'PENDING' || row.status === 'APPROVED'"
                    size="small"
                    type="danger"
                    plain
                    @click="cancelAppointment(row.id)"
                  >
                    取消
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <el-tab-pane v-if="isAdmin" label="审批" name="approval">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>待审批预约</span>
                <el-button size="small" @click="loadPendingAppointments">刷新</el-button>
              </div>
            </template>
            <el-table :data="pendingAppointments" height="500">
              <el-table-column prop="subject" label="主题" min-width="160" />
              <el-table-column prop="userId" label="用户" width="100" />
              <el-table-column label="会议室" width="130">
                <template #default="{ row }">{{ roomName(row.roomId) }}</template>
              </el-table-column>
              <el-table-column label="时间" min-width="260">
                <template #default="{ row }">{{ formatDate(row.startTime) }} - {{ formatDate(row.endTime) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="190">
                <template #default="{ row }">
                  <el-button size="small" type="success" @click="approveAppointment(row.id)">
                    通过
                  </el-button>
                  <el-button size="small" type="danger" plain @click="rejectAppointment(row.id)">
                    拒绝
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="通知" name="notifications">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>通知中心</span>
                <div>
                  <el-button size="small" @click="markAllNotificationsRead">全部已读</el-button>
                  <el-button size="small" @click="loadNotifications">刷新</el-button>
                </div>
              </div>
            </template>
            <el-table :data="notifications" height="500">
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.readFlag ? 'info' : 'warning'" size="small">
                    {{ row.readFlag ? '已读' : '未读' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="160" />
              <el-table-column prop="content" label="内容" min-width="260" />
              <el-table-column label="时间" width="180">
                <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="110">
                <template #default="{ row }">
                  <el-button v-if="!row.readFlag" size="small" @click="markNotificationRead(row.id)">
                    已读
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </template>
  </main>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'

type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

type PageResponse<T> = {
  total: number
  page: number
  size: number
  records: T[]
}

type CurrentUser = {
  userId: number
  username: string
  roles: string[]
}

type MeetingRoom = {
  id: number
  name: string
  floor: string
  capacity: number
  hasProjector: boolean
  hasWhiteboard: boolean
  enabled: boolean
}

type Appointment = {
  id: number
  userId: number
  roomId: number
  subject: string
  startTime: string
  endTime: string
  status: string
  rejectReason?: string
  cancelReason?: string
}

type NotificationItem = {
  id: number
  appointmentId?: number
  eventType: string
  title: string
  content: string
  readFlag: boolean
  createdAt: string
}

const token = ref(localStorage.getItem('reservation_token') || '')
const currentUser = ref<CurrentUser | null>(null)
const activeTab = ref('rooms')
const rooms = ref<MeetingRoom[]>([])
const myAppointments = ref<Appointment[]>([])
const pendingAppointments = ref<Appointment[]>([])
const notifications = ref<NotificationItem[]>([])

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const appointmentForm = reactive({
  roomId: undefined as number | undefined,
  subject: '',
  startTime: '',
  endTime: ''
})

const loading = reactive({
  login: false,
  createAppointment: false
})

const api = axios.create()

api.interceptors.request.use((config) => {
  if (token.value) {
    config.headers.Authorization = `Bearer ${token.value}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

const isAdmin = computed(() => currentUser.value?.roles.includes('ADMIN') ?? false)
const enabledRooms = computed(() => rooms.value.filter((room) => room.enabled))
const unreadCount = computed(() => notifications.value.filter((item) => !item.readFlag).length)

async function request<T>(promise: Promise<{ data: ApiResponse<T> }>) {
  const response = await promise
  return response.data.data
}

async function login() {
  loading.login = true
  try {
    const data = await request<{
      accessToken: string
      username: string
      userId: number
      roles: string[]
    }>(api.post('/api/auth/login', loginForm))
    token.value = data.accessToken
    localStorage.setItem('reservation_token', data.accessToken)
    currentUser.value = {
      userId: data.userId,
      username: data.username,
      roles: data.roles
    }
    await loadDashboard()
  } finally {
    loading.login = false
  }
}

function logout() {
  token.value = ''
  currentUser.value = null
  localStorage.removeItem('reservation_token')
}

async function loadCurrentUser() {
  currentUser.value = await request<CurrentUser>(api.get('/api/auth/me'))
}

async function loadDashboard() {
  await loadCurrentUser()
  await Promise.all([loadRooms(), loadMyAppointments(), loadNotifications()])
  if (isAdmin.value) {
    await loadPendingAppointments()
  }
}

async function loadRooms() {
  const data = await request<PageResponse<MeetingRoom>>(api.get('/api/meeting-rooms', { params: { size: 100 } }))
  rooms.value = data.records
  if (!appointmentForm.roomId && enabledRooms.value.length > 0) {
    appointmentForm.roomId = enabledRooms.value[0].id
  }
}

async function loadMyAppointments() {
  const data = await request<PageResponse<Appointment>>(api.get('/api/appointments', { params: { size: 100 } }))
  myAppointments.value = data.records
}

async function loadPendingAppointments() {
  const data = await request<PageResponse<Appointment>>(
    api.get('/api/appointments', { params: { status: 'PENDING', size: 100 } })
  )
  pendingAppointments.value = data.records
}

async function loadNotifications() {
  const data = await request<PageResponse<NotificationItem>>(api.get('/api/notifications', { params: { size: 100 } }))
  notifications.value = data.records
}

async function createAppointment() {
  if (!appointmentForm.roomId || !appointmentForm.subject || !appointmentForm.startTime || !appointmentForm.endTime) {
    ElMessage.warning('请填写完整预约信息')
    return
  }
  loading.createAppointment = true
  try {
    await request<Appointment>(api.post('/api/appointments', appointmentForm))
    appointmentForm.subject = ''
    appointmentForm.startTime = ''
    appointmentForm.endTime = ''
    ElMessage.success('预约已提交')
    await Promise.all([loadMyAppointments(), isAdmin.value ? loadPendingAppointments() : Promise.resolve()])
  } finally {
    loading.createAppointment = false
  }
}

async function cancelAppointment(id: number) {
  const { value } = await ElMessageBox.prompt('请输入取消原因', '取消预约', {
    inputPlaceholder: '例如：会议改期',
    confirmButtonText: '确认取消',
    cancelButtonText: '返回'
  })
  await request<void>(api.post(`/api/appointments/${id}/cancel`, { cancelReason: value }))
  ElMessage.success('预约已取消')
  await Promise.all([loadMyAppointments(), loadNotifications()])
}

async function approveAppointment(id: number) {
  await request<Appointment>(api.post(`/api/appointments/${id}/approve`))
  ElMessage.success('已审批通过')
  await Promise.all([loadPendingAppointments(), loadMyAppointments(), loadNotifications()])
}

async function rejectAppointment(id: number) {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝预约', {
    inputPlaceholder: '例如：时间段不可用',
    confirmButtonText: '确认拒绝',
    cancelButtonText: '返回'
  })
  await request<Appointment>(api.post(`/api/appointments/${id}/reject`, { rejectReason: value }))
  ElMessage.success('已拒绝')
  await Promise.all([loadPendingAppointments(), loadMyAppointments(), loadNotifications()])
}

async function markNotificationRead(id: number) {
  await request<void>(api.post(`/api/notifications/${id}/read`))
  await loadNotifications()
}

async function markAllNotificationsRead() {
  await request<void>(api.post('/api/notifications/read-all'))
  await loadNotifications()
}

function roomName(roomId: number) {
  return rooms.value.find((room) => room.id === roomId)?.name || `#${roomId}`
}

function formatDate(value: string) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}

function statusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELLED: '已取消',
    COMPLETED: '已完成',
    EXPIRED: '已过期'
  }
  return map[status] || status
}

function statusType(status: string) {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info',
    COMPLETED: 'success',
    EXPIRED: 'info'
  }
  return map[status] || 'info'
}

onMounted(async () => {
  if (token.value) {
    try {
      await loadDashboard()
    } catch {
      logout()
    }
  }
})
</script>
