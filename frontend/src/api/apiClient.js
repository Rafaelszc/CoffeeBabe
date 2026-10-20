// Único ponto de acesso ao backend: URL vinda de VITE_API_URL e cookie de sessão HttpOnly (credentials: "include").
const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080"

export class ApiError extends Error {
  constructor(message, status, errors = []) {
    super(message)
    this.status = status
    this.errors = errors
  }
}

export const apiUrl = (path) => `${API_URL}${path}`

// Monta a query string ignorando valores vazios.
export const query = (params) => {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") search.set(key, value)
  })
  return search.toString()
}

// Devolve o campo `data` do envelope ApiResponseEntity; lança ApiError quando a resposta não é 2xx.
export const api = async (path, { method = "GET", body, form } = {}) => {
  const res = await fetch(apiUrl(path), {
    method,
    credentials: "include",
    headers: body === undefined ? undefined : { "Content-Type": "application/json" },
    body: form ?? (body === undefined ? undefined : JSON.stringify(body)),
  })

  const isJson = res.headers.get("content-type")?.includes("application/json")
  const payload = isJson ? await res.json() : null

  if (!res.ok) throw new ApiError(payload?.message ?? res.statusText, res.status, payload?.errors ?? [])
  return payload?.data
}
