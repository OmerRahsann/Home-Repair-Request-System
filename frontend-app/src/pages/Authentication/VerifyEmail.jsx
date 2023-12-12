import React from 'react'
import axios from 'axios'
import { Navigate, useSearchParams } from 'react-router-dom'
import logo from 'Logos/mainLogo.png'

export default function VerifyEmail() {
  const FormState = {
    Inital: 0,
    Success: 1,
    Expired: 2,
  }

  const [formState, setFormState] = React.useState(FormState.Inital)

  const [searchParams, _] = useSearchParams()
  const token = searchParams.get('token')
  React.useEffect(() => {
    if (!token || formState != FormState.Inital) {
      return
    }
    const controller = new AbortController()
    axios
      .post('/api/verify', token, {
        signal: controller.signal,
        withCredentials: true,
      })
      .then((_) => setFormState(FormState.Success))
      .catch((_) => setFormState(FormState.Expired))
    // Abort the request if the component is unmounted to prevent double requests
    return () => controller.abort()
  }, [])

  if (!token || token === '') {
    // No token?? Go to the homepage
    return <Navigate to="/" />
  }

  let content
  switch (formState) {
    case FormState.Success:
      content = (
        <form className="space-y-4 md:space-y-6" action="/customer/login">
          <h1 className="block mb-2 text-sm font-medium text-gray-900 text-left">
            Email verified. Please log in.
          </h1>
          <button
            type="submit"
            className="text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
          >
            Return to log in
          </button>
        </form>
      )
      break
    case FormState.Expired:
      content = (
        <form className="space-y-4 md:space-y-6" action="/customer/login">
          <h1 className="block mb-2 text-sm font-medium text-red-900 text-left">
            Email verification link is invalid.
          </h1>
          <button
            type="submit"
            className="text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
          >
            Return to log in
          </button>
        </form>
      )
      break
  }

  return (
    <div className="bg-gradient-to-r from-[#b9a290] via-[#76323f] to-[#b9a290]">
      <a href="/">
        <img className="inset-y-0 h-28" src={logo} alt="logo" />
      </a>
      <div className="flex flex-col items-center px-6 py-8 mx-auto md:h-screen lg:py-0">
        <div className="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0  ">
          <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
            <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl text-center">
              Verify email
            </h1>
            {content}
          </div>
        </div>
      </div>
    </div>
  )
}
