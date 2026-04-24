import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/template',
      name: 'template',
      component: () => import('../views/TemplateView.vue')
    },
    {
      path: '/task',
      name: 'task',
      component: () => import('../views/TaskView.vue')
    },
    {
      path: '/designer',
      name: 'designer',
      component: () => import('../views/DesignerView.vue')
    }
  ]
})

export default router