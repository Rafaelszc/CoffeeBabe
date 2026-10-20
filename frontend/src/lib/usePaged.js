import { useCallback, useEffect, useState } from "react"

// Carrega uma página de uma listagem da API; `fetchPage(page)` precisa ser estável (função de módulo ou useCallback).
// reload() refaz a busca depois de uma ação (cancelar, excluir...).
export const usePaged = (fetchPage) => {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  const [version, setVersion] = useState(0)

  useEffect(() => {
    let cancelled = false
    fetchPage(page)
      .then((data) => {
        if (cancelled) return
        setResult(data)
        setFailed(false)
      })
      .catch(() => !cancelled && setFailed(true))
      .finally(() => !cancelled && setLoading(false))
    return () => { cancelled = true }
  }, [fetchPage, page, version])

  const reload = useCallback(() => setVersion((v) => v + 1), [])

  return { page, setPage, items: result?.items ?? [], totalPages: result?.totalPages ?? 0, loading, failed, reload }
}
