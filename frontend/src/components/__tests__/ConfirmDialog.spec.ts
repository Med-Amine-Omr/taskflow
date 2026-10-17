import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ConfirmDialog from '../ConfirmDialog.vue'

const baseProps = { title: 'Supprimer la tâche', message: 'Voulez-vous vraiment supprimer ?' }

describe('ConfirmDialog', () => {
  it("n'affiche rien quand la boîte est fermée", () => {
    const wrapper = mount(ConfirmDialog, { props: { ...baseProps, open: false } })

    expect(wrapper.find('[data-test="confirm-dialog"]').exists()).toBe(false)
  })

  it('affiche le titre et le message quand elle est ouverte', () => {
    const wrapper = mount(ConfirmDialog, { props: { ...baseProps, open: true } })

    expect(wrapper.text()).toContain('Supprimer la tâche')
    expect(wrapper.text()).toContain('Voulez-vous vraiment supprimer ?')
  })

  it("émet 'confirm' au clic sur le bouton de confirmation", async () => {
    const wrapper = mount(ConfirmDialog, { props: { ...baseProps, open: true } })

    await wrapper.get('[data-test="confirm"]').trigger('click')

    expect(wrapper.emitted('confirm')).toHaveLength(1)
    expect(wrapper.emitted('cancel')).toBeUndefined()
  })

  it("émet 'cancel' au clic sur Annuler", async () => {
    const wrapper = mount(ConfirmDialog, { props: { ...baseProps, open: true } })

    await wrapper.get('[data-test="cancel"]').trigger('click')

    expect(wrapper.emitted('cancel')).toHaveLength(1)
    expect(wrapper.emitted('confirm')).toBeUndefined()
  })
})