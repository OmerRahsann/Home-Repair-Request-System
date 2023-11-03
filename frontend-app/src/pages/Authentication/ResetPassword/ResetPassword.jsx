import React from 'react'
import axios from 'axios'
import { Navigate, useSearchParams } from 'react-router-dom'
import logo from 'Logos/mainLogo.png'
import PasswordStrength from 'components/PasswordStrength'

export default function ResetPassword() {
  const FormState = {
    Inital: 0,
    Success: 1,
    Expired: 2
  }

  const [formState, setFormState] = React.useState(FormState.Inital)
  const [state, setState] = React.useState({
    password: '',
    confirmPassword: ''
  })
  const [canSubmit, setCanSubmit] = React.useState(false)

  const [searchParams, _] = useSearchParams();
  const token = searchParams.get("token")
  if (!token || token === "") {
    // No token?? Go to the request password reset form
    return <Navigate to="/reset_password" />
  }
  
  const expireAt = parseInt(searchParams.get("expire_at"))
  if (formState == FormState.Inital && expireAt) {
    const currentTimestamp = Math.ceil(new Date().getTime() / 1000);
    if (currentTimestamp >= expireAt) {
      // The link + token has expired
      setFormState(FormState.Expired)
    }
  }

  async function resetPassword(event) {
    event.preventDefault()
    const { password } = state
    await axios
      .post(
        'http://localhost:8080/api/reset_password',
        {
          password,
          token
        },
        {withCredentials: true}
      )
      .then((_) => setFormState(FormState.Success))
      .catch((error) => {
        if (error.response && error.response.status == 400) { // Received bad request
          if (error.response.data.type === "validation_error") {
            // Shouldn't happen? We have input validation with PasswordStrength
            console.log(error.response.data)
            alert("Unknown error occured!")
            return
          }
        }
        // Expired token or some other error, tell the user to try again
        setFormState(FormState.Expired)
      })
  }

  const handleChange = (evt) => {
    const value = evt.target.value
    setState({
      ...state,
      [evt.target.name]: value,
    })
  }

  let content;
  switch (formState) {
    case FormState.Inital:
      content = (
        <form
          className="space-y-4 md:space-y-6"
          action="#"
          onSubmit={resetPassword}
        >
          <div>
            <input
              type="password"
              name="password"
              value={state.password}
              onChange={handleChange}
              placeholder="Password"
              required
              className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
            />
          </div>
          <div>
            <input
              type="password"
              name="confirmPassword"
              value={state.confirmPassword}
              onChange={handleChange}
              placeholder="Confirm Password"
              required
              className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
            />
          </div>
          <PasswordStrength password={state.password} confirmPassword={state.confirmPassword} updateSatisfied={setCanSubmit}/>
          <button
            type="submit"
            disabled={!canSubmit}
            className={`${
              canSubmit
                ? 'text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800'
                : 'cursor-not-allowed w-full bg-gray-200 hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800 text-white'
            }`}
          >
            Reset password
          </button>
        </form>
      )
      break
    case FormState.Success:
      content = (
        <form
          className="space-y-4 md:space-y-6"
          action="/customer/login"
        >
          <h1 className='block mb-2 text-sm font-medium text-gray-900 text-left'>
            Password has been reset. Please log in with your email and new password.
          </h1>
          <button
            type='submit'
            className="text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
          >
            Return to log in
          </button>
        </form>
      )
      break
    case FormState.Expired:
      content = (
        <form
          className="space-y-4 md:space-y-6"
          action="/reset_password"
        >
          <h1 className='block mb-2 text-sm font-medium text-red-900 text-left'>
            Password reset link has expired. Please request a new password reset email.
          </h1>
          <button
            type='submit'
            className="text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
          >
            Request a new reset email
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
              Reset your password
            </h1>
            {content}
          </div>
        </div>
      </div>
    </div>
  )
}
