import { ref } from 'vue'

export function useApi(apiFunc) {
    const data = ref(null)
    const loading = ref(false)
    const error = ref(null)

    const execute = async (...args) => {
        loading.value = true
        error.value = null
        try {
            const result = await apiFunc(...args)
            data.value = result
            return result
        } catch (err) {
            error.value = err.message
            throw err
        } finally {
            loading.value = false
        }
    }

    const reset = () => {
        data.value = null
        error.value = null
        loading.value = false
    }

    return { data, loading, error, execute, reset }
}