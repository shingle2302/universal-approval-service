// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import HomeView from '../views/HomeView.vue'
import axios from 'axios'

vi.mock('axios', () => ({
  default: {
    get: vi.fn()
  }
}))

const flushPromises = () => new Promise((resolve) => setTimeout(resolve, 0))

describe('HomeView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads dashboard status and renders counters', async () => {
    axios.get
      .mockResolvedValueOnce({ data: { code: 200, data: [{ status: 'DEPLOYED' }, { status: 'CREATED' }] } })
      .mockResolvedValueOnce({ data: { code: 200, data: [{ id: 't1' }, { id: 't2' }] } })
      .mockResolvedValueOnce({ data: { code: 200, data: [{ id: 'p1' }] } })

    const wrapper = mount(HomeView, {
      global: {
        stubs: {
          RouterLink: {
            template: '<a><slot /></a>'
          }
        }
      }
    })

    await flushPromises()
    const values = wrapper.findAll('.status-value').map((node) => node.text())

    expect(values).toContain('1')
    expect(values).toContain('2')
    expect(values).toContain('1')
    expect(axios.get).toHaveBeenCalledTimes(3)
  })

  it('shows error message when request fails', async () => {
    axios.get.mockRejectedValue(new Error('network'))

    const wrapper = mount(HomeView, {
      global: {
        stubs: {
          RouterLink: {
            template: '<a><slot /></a>'
          }
        }
      }
    })

    await flushPromises()
    expect(wrapper.text()).toContain('获取系统状态失败，请稍后重试。')
  })
})
