import React from 'react'
import axios from 'axios'
import logo from 'Logos/mainLogo.png'

export default function RequestPasswordReset() {
  const FormState = {
    Inital: 0,
    Success: 1,
    Error: 2
  }

  const [formState, setFormState] = React.useState(FormState.Inital)
  const [email, setEmail] = React.useState("")
  async function sendResetPassword(event) {
    event.preventDefault()
    await axios
      .post(
        `${process.env.REACT_APP_API_URL}/api/reset_password/send`,
        {
          email: email,
        },
        {withCredentials: true}
      )
      .then((_) => setFormState(FormState.Success))
      .catch((_) => setFormState(FormState.Error))
  }

  const resetState = () => {
    setEmail("")
    setFormState(FormState.Inital)
  }

  const handleEmailChange = (evt) => {
    const value = evt.target.value
    setEmail(value)
  }

  let content;
  switch (formState) {
    case FormState.Inital:
      content = (
        <form
          className="space-y-4 md:space-y-6"
          action="#"
          onSubmit={sendResetPassword}
        >
          <div>
            <label className="block mb-2 text-sm font-medium text-gray-900 text-left">
              Your verified email
            </label>
            <input
              type="email"
              name="email"
              id="email"
              onChange={handleEmailChange}
              value={email}
              className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
              placeholder="Email"
              required
            />
          </div>
          <button
            type="submit"
            disabled={email === ''}
            className={`${
              email != ''
                ? 'text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800'
                : 'cursor-not-allowed w-full bg-gray-200 hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800 text-white'
            }`}
          >
            Send password reset email
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
            Check your email for a link to reset your password. If it doesn't appear within a few minutes, check your spam folder.
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
    case FormState.Error:
      content = (
        <form
          className="space-y-4 md:space-y-6"
          action="#"
          onSubmit={resetState}
        >
          <h1 className='block mb-2 text-sm font-medium text-red-900 text-left'>
            Failed to send password reset email. Please try again in a few minutes.
          </h1>
          <button
            type='submit'
            className="text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
          >
            Try again
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