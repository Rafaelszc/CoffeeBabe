import { afterEach, describe, expect, it, vi } from "vitest"
import { ApiError, api, query } from "./apiClient"

const jsonResponse = (status, payload) => new Response(JSON.stringify(payload), {
  status,
  headers: { "Content-Type": "application/json" },
})

describe("api", () => {
  afterEach(() => vi.unstubAllGlobals())

  it("sends the session cookie and JSON body, and unwraps the envelope", async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(200, { success: true, data: { id: 1 }, errors: [] }))
    vi.stubGlobal("fetch", fetchMock)

    const data = await api("/api/cart/add", { method: "POST", body: { productId: "p1" } })

    expect(data).toEqual({ id: 1 })
    const [url, options] = fetchMock.mock.calls[0]
    expect(url).toBe("http://localhost:8080/api/cart/add")
    expect(options.credentials).toBe("include")
    expect(options.headers).toEqual({ "Content-Type": "application/json" })
    expect(options.body).toBe(JSON.stringify({ productId: "p1" }))
  })

  it("throws ApiError with status and field errors", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(
      jsonResponse(400, { success: false, message: "Invalid data!", errors: ["email: invalid"] })))

    const error = await api("/api/auth/signup", { method: "POST", body: {} }).catch((e) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(400)
    expect(error.errors).toEqual(["email: invalid"])
  })

  it("handles responses without a JSON body", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(null, { status: 401, statusText: "Unauthorized" })))

    const error = await api("/api/auth/me").catch((e) => e)

    expect(error.status).toBe(401)
    expect(error.message).toBe("Unauthorized")
  })

  it("builds query strings without empty values", () => {
    expect(query({ q: "", category: "SNACK", page: 0, size: undefined })).toBe("category=SNACK&page=0")
  })
})
