import { describe, expect, it } from 'vitest'
import { mount, RouterLinkStub } from '@vue/test-utils'
import TaskItem from '../TaskItem.vue'
import type { Task } from '@/types/api'

function buildTask(overrides: Partial<Task> = {}): Task {
  return {
    id: 'task-1',
    title: 'Préparer la démo',
    description: null,
    priority: 'HIGH',
    dueDate: '2026-10-10',
    completed: false,
    overdue: false,
    ownerId: 'alice-id',
    ownerUsername: 'alice',
    createdAt: '2026-10-01T10:00:00Z',
    updatedAt: '2026-10-01T10:00:00Z',
    ...overrides,
  }
}

function mountItem(task: Task) {
  return mount(TaskItem, {
    props: { task },
    global: { stubs: { RouterLink: RouterLinkStub } },
  })
}

describe('TaskItem', () => {
  it('met en évidence une tâche en retard', () => {
    const wrapper = mountItem(buildTask({ overdue: true }))

    expect(wrapper.get('[data-test="task-item"]').classes()).toContain('task--overdue')
    expect(wrapper.find('[data-test="overdue-badge"]').exists()).toBe(true)
  })

  it("n'affiche pas d'indicateur de retard pour une tâche à jour", () => {
    const wrapper = mountItem(buildTask({ overdue: false }))

    expect(wrapper.get('[data-test="task-item"]').classes()).not.toContain('task--overdue')
    expect(wrapper.find('[data-test="overdue-badge"]').exists()).toBe(false)
  })

  it('affiche le titre, la priorité en français et la date au format français', () => {
    const wrapper = mountItem(buildTask())

    expect(wrapper.text()).toContain('Préparer la démo')
    expect(wrapper.text()).toContain('Haute')
    expect(wrapper.text()).toContain('10/10/2026')
  })

  it('coche la case et barre le titre pour une tâche terminée', () => {
    const wrapper = mountItem(buildTask({ completed: true }))

    expect((wrapper.get('[data-test="toggle"]').element as HTMLInputElement).checked).toBe(true)
    expect(wrapper.get('[data-test="task-item"]').classes()).toContain('task--done')
  })

  it("émet 'toggle' avec l'identifiant au changement de statut", async () => {
    const wrapper = mountItem(buildTask())

    await wrapper.get('[data-test="toggle"]').trigger('change')

    expect(wrapper.emitted('toggle')).toEqual([['task-1']])
  })

  it("émet 'delete' avec la tâche au clic sur Supprimer", async () => {
    const task = buildTask()
    const wrapper = mountItem(task)

    await wrapper.get('[data-test="delete"]').trigger('click')

    expect(wrapper.emitted('delete')).toEqual([[task]])
  })
})