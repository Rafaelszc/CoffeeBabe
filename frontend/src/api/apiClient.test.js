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
    vi.stubGlobal("fetch", vi.fn().mockImplementation(() =>
      Promise.resolve(new Response(null, { status: 401, statusText: "Unauthorized" }))))

    const error = await api("/api/auth/me").catch((e) => e)

    expect(error.status).toBe(401)
    expect(error.message).toBe("Unauthorized")
  })

  it("refreshes the session on 401 and retries the request once", async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(null, { status: 401 }))
      .mockResolvedValueOnce(jsonResponse(200, { success: true, data: null }))
      .mockResolvedValueOnce(jsonResponse(200, { success: true, data: { email: "ana@mail.com" } }))
    vi.stubGlobal("fetch", fetchMock)

    expect(await api("/api/auth/me")).toEqual({ email: "ana@mail.com" })
    expect(fetchMock.mock.calls.map(([url]) => url)).toEqual([
      "http://localhost:8080/api/auth/me",
      "http://localhost:8080/api/auth/refresh",
      "http://localhost:8080/api/auth/me",
    ])
    expect(fetchMock.mock.calls[1][1]).toMatchObject({ method: "POST", credentials: "include" })
  })

  it("shares one refresh between concurrent 401s", async () => {
    const seen = new Set()
    const fetchMock = vi.fn().mockImplementation((url) => {
      if (url.endsWith("/api/auth/refresh")) return Promise.resolve(jsonResponse(200, { success: true }))
      // first call per URL is 401, the retry succeeds
      if (!seen.has(url)) {
        seen.add(url)
        return Promise.resolve(new Response(null, { status: 401 }))
      }
      return Promise.resolve(jsonResponse(200, { success: true, data: url }))
    })
    vi.stubGlobal("fetch", fetchMock)

    await Promise.all([api("/api/cart/get"), api("/api/orders"), api("/api/auth/me")])

    expect(fetchMock.mock.calls.filter(([url]) => url.endsWith("/api/auth/refresh"))).toHaveLength(1)
  })

  it("never refreshes on auth routes, so a wrong password stays a 401", async () => {
    const fetchMock = vi.fn().mockImplementation(() =>
      Promise.resolve(jsonResponse(401, { success: false, message: "Invalid email or password!" })))
    vi.stubGlobal("fetch", fetchMock)

    const error = await api("/api/auth/signin", { method: "POST", body: {} }).catch((e) => e)

    expect(error.status).toBe(401)
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it("builds query strings without empty values", () => {
    expect(query({ q: "", category: "SNACK", page: 0, size: undefined })).toBe("category=SNACK&page=0")
  })
})
