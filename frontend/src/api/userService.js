import { api } from "./apiClient"

export const signInUser = ({ email, password }) =>
  api("/api/auth/signin", { method: "POST", body: { email, password } })

export const signUpUser = ({ username, email, password }) =>
  api("/api/auth/signup", { method: "POST", body: { username, email, password } })

export const signOutUser = () => api("/api/auth/signout", { method: "POST" })

export const fetchMe = () => api("/api/auth/me")
