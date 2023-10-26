import React, { useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../AuthContext'
import logo from '../../Logos/mainLogo.png'

function ProviderSignIn() {
  const [state, setState] = React.useState({
    email: '',
    password: '',
  })
  const { accessServiceProviderAccount } = useAuth()
  const navigate = useNavigate()
  async function login(event) {
    event.preventDefault()
    try {
      const { email, password } = state
      await axios
        .post(
          'http://localhost:8080/api/login',
          {
            email: email,
            password: password,
          },
          { withCredentials: true },
        )
        .then(
          (res) => {
            console.log(res.data)
            navigate('/provider/home')
          },
          (fail) => {
            alert('Unrecognized email or password')
            console.error(fail) // Error!
          },
        )
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
    <div className="bg-gradient-to-r from-[#999999] via-[#565656] to-[#565656]]">
      <a href="/">
        <img className="inset-y-0 h-28" src={logo} alt="logo" />
      </a>
      <div class="flex flex-col items-center px-6 py-8 mx-auto md:h-screen lg:py-0">
        <div class="w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0  ">
          <div class="p-6 space-y-4 md:space-y-6 sm:p-8">
            <h1 class="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl text-center">
              Sign in to Your Service Provider Account
            </h1>
            <form class="space-y-4 md:space-y-6" action="#" onSubmit={login}>
              <div>
                <label
                  for="email"
                  class="block mb-2 text-sm font-medium text-gray-900 text-left"
                >
                  Your email
                </label>
                <input
                  type="email"
                  name="email"
                  id="email"
                  onChange={handleChange}
                  value={state.email}
                  class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  placeholder="name@company.com"
                  required=""
                />
              </div>
              <div>
                <label
                  for="password"
                  class="block mb-2 text-sm font-medium text-gray-900 text-left"
                >
                  Password
                </label>
                <input
                  type="password"
                  name="password"
                  id="password"
                  placeholder="••••••••"
                  onChange={handleChange}
                  value={state.password}
                  class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5 dark:bg-gray-200 dark:border-gray-600 dark:placeholder-gray-400 "
                  required=""
                />
              </div>
              <div class="flex items-center justify-between">
                {/* <div class="flex items-start">
                          <div class="flex items-center h-5">
                            <input id="remember" aria-describedby="remember" type="checkbox" class="w-4 h-4 border border-gray-300 rounded bg-gray-50 focus:ring-3 focus:ring-primary-300 dark:bg-gray-700 dark:border-gray-600 dark:focus:ring-primary-600 dark:ring-offset-gray-800" required=""/>
                          </div>
                          <div class="ml-3 text-sm">
                            <label for="remember" class="text-gray-500 dark:text-gray-300">Remember me</label>
                          </div>
                      </div> */}
                <a
                  href="#"
                  class="text-sm font-medium text-primary-600 hover:underline dark:text-primary-500"
                >
                  Forgot password?
                </a>
              </div>
              <button class=" text-white w-full bg-custom-maroon hover:bg-primary-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800">
                Sign in
              </button>
              <p class="text-sm font-light  ">
                Want to provide your Service?{' '}
                <a
                  href="/provider/signup"
                  class="font-bold text-primary-600 hover:underline dark:text-primary-500"
                >
                  Sign up here!
                </a>
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

export default ProviderSignIn
