import { userData } from "@/data/mockData"

export const signInUser = async (user) => {
    alert("SignIn "+JSON.stringify(user))
    document.cookie = `userData=${JSON.stringify(userData)}; path=/; SameSite=Strict; max-age=3600`
    return {ok: true}
}

export const signUpUser = async (data) => {
    alert("SignUp "+JSON.stringify(data))
    return {ok: true}
}

export const getCookieData = (name) => {
  const data =  document.cookie.split('; ').reduce((r, v) => {
    const parts = v.split('=')
    return parts[0] === name ? decodeURIComponent(parts[1]) : r
  }, '')
  
  if (data.length === 0 || data === undefined || data === null) return null

  return data
}

const clearCookieData = (name) => {
  document.cookie = `${name}=''; path=/; SameSite=Strict; max-age=0`
}

export const logoutUser = () => {
  clearCookieData("userData")
}

export const checkUser = async () => {
  if (getCookieData("userData") !== null) {
    return {ok: true}
  } 
  else {
    clearCookieData("userData")
    return {ok: false}
  }
}