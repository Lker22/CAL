import { createRouter, createWebHistory } from 'vue-router'

// 用户与学习画像模块
const LoginView = () => import('@/views/user/LoginView.vue')
const RegisterView = () => import('@/views/user/RegisterView.vue')
const ProfileBuildView = () => import('@/views/user/ProfileBuildView.vue')
const ProfileDisplayView = () => import('@/views/user/ProfileDisplayView.vue')
const ProfileQueryView = () => import('@/views/user/ProfileQueryView.vue')
const UserInfoView = () => import('@/views/user/UserInfoView.vue')

// AI多智能体资源生成模块
const AgentListView = () => import('@/views/ai-resource/AgentListView.vue')
const ResourceGenerateView = () => import('@/views/ai-resource/ResourceGenerateView.vue')
const GenerationProgressView = () => import('@/views/ai-resource/GenerationProgressView.vue')
const ResourceListView = () => import('@/views/ai-resource/ResourceListView.vue')
const ResourceDetailView = () => import('@/views/ai-resource/ResourceDetailView.vue')

// 学习路径规划与智能推送模块
const PathGenerateView = () => import('@/views/learning-path/PathGenerateView.vue')
const PathListView = () => import('@/views/learning-path/PathListView.vue')
const PathStepView = () => import('@/views/learning-path/PathStepView.vue')
const ResourceRecommendView = () => import('@/views/learning-path/ResourceRecommendView.vue')
const PathAdjustView = () => import('@/views/learning-path/PathAdjustView.vue')

// 智能辅导模块
const TutorQuestionView = () => import('@/views/intelligent-tutor/TutorQuestionView.vue')
const TutorAnswerView = () => import('@/views/intelligent-tutor/TutorAnswerView.vue')
const TutorHistoryView = () => import('@/views/intelligent-tutor/TutorHistoryView.vue')

// 学习效果评估模块
const AssessmentReportView = () => import('@/views/learning-assessment/AssessmentReportView.vue')
const AssessmentResultView = () => import('@/views/learning-assessment/AssessmentResultView.vue')
const LearningStatsView = () => import('@/views/learning-assessment/LearningStatsView.vue')

// 布局组件
const Layout = () => import('@/components/layout/Layout.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/layout',
      name: 'layout',
      component: Layout,
      children: [
        // 用户与学习画像模块
        {
          path: '/profile/build',
          name: 'profile-build',
          component: ProfileBuildView
        },
        {
          path: '/profile/display',
          name: 'profile-display',
          component: ProfileDisplayView
        },
        {
          path: '/profile/query',
          name: 'profile-query',
          component: ProfileQueryView
        },
        {
          path: '/user/info',
          name: 'user-info',
          component: UserInfoView
        },

        // AI多智能体资源生成模块
        {
          path: '/agent/list',
          name: 'agent-list',
          component: AgentListView
        },
        {
          path: '/resource/generate',
          name: 'resource-generate',
          component: ResourceGenerateView
        },
        {
          path: '/generation/progress',
          name: 'generation-progress',
          component: GenerationProgressView
        },
        {
          path: '/resource/list',
          name: 'resource-list',
          component: ResourceListView
        },
        {
          path: '/resource/detail/:id',
          name: 'resource-detail',
          component: ResourceDetailView,
          props: true
        },

        // 学习路径规划与智能推送模块
        {
          path: '/path/generate',
          name: 'path-generate',
          component: PathGenerateView
        },
        {
          path: '/path/list',
          name: 'path-list',
          component: PathListView
        },
        {
          path: '/path/step/:id',
          name: 'path-step',
          component: PathStepView,
          props: true
        },
        {
          path: '/recommend/resource',
          name: 'resource-recommend',
          component: ResourceRecommendView
        },
        {
          path: '/path/adjust',
          name: 'path-adjust',
          component: PathAdjustView
        },

        // 智能辅导模块
        {
          path: '/tutor/question',
          name: 'tutor-question',
          component: TutorQuestionView
        },
        {
          path: '/tutor/answer/:recordId?',
          name: 'tutor-answer',
          component: TutorAnswerView
        },
        {
          path: '/tutor/history',
          name: 'tutor-history',
          component: TutorHistoryView
        },

        // 学习效果评估模块
        {
          path: '/assessment/report',
          name: 'assessment-report',
          component: AssessmentReportView
        },
        {
          path: '/assessment/result',
          name: 'assessment-result',
          component: AssessmentResultView
        },
        {
          path: '/learning/stats',
          name: 'learning-stats',
          component: LearningStatsView
        }
      ]
    }
  ]
})

// 抑制导航过程中的parentNode错误
const originalPush = router.push
router.push = function (...args) {
  return originalPush.apply(this, args).catch((err) => {
    if (err && err.message && err.message.includes('parentNode')) {
      return
    }
    throw err
  })
}

export default router