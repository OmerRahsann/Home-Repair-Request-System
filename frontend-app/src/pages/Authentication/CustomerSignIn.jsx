import React from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { checkIsCustomerLoggedIn } from '../../AuthContext'
import logo from '../../Logos/mainLogo.png'

function CustomerSignIn() {
  const [state, setState] = React.useState({
    email: '',
    password: '',
  })
  const navigate = useNavigate()
  async function login(event) {
    event.preventDefault()
    try {
      const { email, password } = state
      await axios
        .post(
          '/api/login',
          {
            email: email,
            password: password,
          },
          { withCredentials: true },
        )
        .then(checkIsCustomerLoggedIn)
        .then((result) => {
          if (result) {
            navigate('/')
          } else {
            window.alert(
              'This is a provider account. Navigating you to the Provider Home Page.',
            )
            navigate('/provider/viewrequests')
          }
        })
        .catch((fail) => {
          console.error(fail) // Error!
          if (fail.response && fail.response.status == 403) {
            alert('Invalid credentials. Please try again.')
            return
          }
          alert('Oops...an error occurred. Please try again.')
        })
    } catch (err) {
      alert(err)
    }
  }
  const handleChange = (evt) => {
    const value = evt.target.value
    setState({
      ...state,
      [evt.target.name]: value,
    })
  }

  return (
    <div className="bg-gradient-to-r from-[#b9a290] via-[#76323f] to-[#b9a290]">
      <a href="/">
        <img className="inset-y-0 h-28" src={logo} alt="logo" />
      </a>
      <a
        href="/provider/login"
        className="text-blue-700 hover:underlin absolute top-0 right-0 pr-2 font-bold"
      >
        Are you a service provider?
      </a>
      <div className="flex flex-col items-center px-6 py-8 mx-auto md:h-screen lg:py-0">
        <div className="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0  ">
          <div className="p-6 space-y-4 md:space-y-6 sm:p-8">
            <h1 className="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl text-center">
              Sign in to Your Customer Account
            </h1>
            <form
              className="space-y-4 md:space-y-6"
              action="#"
              onSubmit={login}
            >
              <div>
                <label className="block mb-2 text-sm font-medium text-gray-900 text-left">
                  Your email
                </label>
                <input
                  type="email"
                  name="email"
                  id="email"
                  onChange={handleChange}
                  value={state.email}
                  className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  placeholder="Email"
                  required=""
                />
              </div>
              <div>
                <label className="block mb-2 text-sm font-medium text-gray-900 text-left">
                  Password
                </label>
                <input
                  type="password"
                  name="password"
                  id="password"
                  placeholder="Password"
                  onChange={handleChange}
                  value={state.password}
                  className="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  pattern=".{8,}"
                  title="Password must be at least 8 characters long."
                  required=""
                />
              </div>
              <div className="flex items-center justify-between">
                {/* <div className="flex items-start">
                          <div className="flex items-center h-5">
                            <input id="remember" aria-describedby="remember" type="checkbox" className="w-4 h-4 border border-gray-300 rounded bg-gray-50 focus:ring-3 focus:ring-primary-300 dark:bg-gray-700 dark:border-gray-600 dark:focus:ring-primary-600 dark:ring-offset-gray-800" required=""/>
                          </div>
                          <div className="ml-3 text-sm">
                            <label for="remember" className="text-gray-500 dark:text-gray-300">Remember me</label>
                          </div>
                      </div> */}
                <a
                  href="/reset_password"
                  className="text-sm font-medium text-primary-600 hover:underline dark:text-primary-500"
                >
                  Forgot password?
                </a>
              </div>
              <button
                type="submit"
                className=" text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
              >
                Sign in
              </button>
              <p className="text-sm font-light  ">
                Don’t have an account yet?{' '}
                <a
                  href="/customer/signup"
                  className="font-bold text-primary-600 hover:underline dark:text-primary-500"
                >
                  Sign up
                </a>
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CustomerSignIn
