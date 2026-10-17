import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import TaskForm from '../TaskForm.vue'

describe('TaskForm', () => {
  it("refuse un titre vide et n'émet rien", async () => {
    const wrapper = mount(TaskForm)

    await wrapper.get('[data-test="title"]').setValue('   ')
    await wrapper.get('form').trigger('submit')

    expect(wrapper.get('[data-test="error-title"]').text()).toBe('Le titre est obligatoire')
    expect(wrapper.emitted('submit')).toBeUndefined()
  })

  it('émet les données nettoyées pour un formulaire valide', async () => {
    const wrapper = mount(TaskForm)

    await wrapper.get('[data-test="title"]').setValue('  Préparer la démo  ')
    await wrapper.get('[data-test="description"]').setValue('   ')
    await wrapper.get('[data-test="priority"]').setValue('HIGH')
    await wrapper.get('[data-test="dueDate"]').setValue('2026-10-20')
    await wrapper.get('form').trigger('submit')

    expect(wrapper.emitted('submit')).toEqual([
      [{ title: 'Préparer la démo', description: null, priority: 'HIGH', dueDate: '2026-10-20' }],
    ])
  })

  it("affiche les erreurs renvoyées par l'API sous le champ concerné", () => {
    const wrapper = mount(TaskForm, {
      props: {
        serverErrors: {
          title: 'Le titre ne doit pas dépasser 100 caractères',
          dueDate: 'Valeur invalide',
        },
      },
    })

    expect(wrapper.get('[data-test="error-title"]').text()).toBe('Le titre ne doit pas dépasser 100 caractères')
    expect(wrapper.get('[data-test="error-dueDate"]').text()).toBe('Valeur invalide')
    expect(wrapper.get('[data-test="title"]').attributes('aria-invalid')).toBe('true')
  })

  it('pré-remplit le formulaire en modification', () => {
    const wrapper = mount(TaskForm, {
      props: {
        initial: { title: 'Tâche existante', description: 'Détails', priority: 'LOW', dueDate: '2026-11-01' },
      },
    })

    expect((wrapper.get('[data-test="title"]').element as HTMLInputElement).value).toBe('Tâche existante')
    expect((wrapper.get('[data-test="priority"]').element as HTMLSelectElement).value).toBe('LOW')
    expect((wrapper.get('[data-test="dueDate"]').element as HTMLInputElement).value).toBe('2026-11-01')
  })

  it('désactive le bouton pendant l\'enregistrement', () => {
    const wrapper = mount(TaskForm, { props: { submitting: true } })

    expect(wrapper.get('[data-test="submit"]').attributes('disabled')).toBeDefined()
  })
})