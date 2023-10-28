import { render, screen, fireEvent, act, waitFor } from '@testing-library/react'

import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from '../AuthContext.js'
import userEvent from '@testing-library/user-event'
import axios from 'axios'
import CustomerSignIn from '../pages/Authentication/CustomerSignIn.jsx'

test('SignIn Form renders successfully', () => {
  render(
    <BrowserRouter>
      <AuthProvider>
        <CustomerSignIn />
      </AuthProvider>
    </BrowserRouter>,
  )
  expect(screen.getByPlaceholderText('Email')).toBeInTheDocument()
  expect(screen.getByPlaceholderText('Password')).toBeInTheDocument()
})

it('should update the state when input values change', () => {
  render(
    <BrowserRouter>
      <AuthProvider>
        <CustomerSignIn />
      </AuthProvider>
    </BrowserRouter>,
  )
  const emailInput = screen.getByPlaceholderText('Email')
  const passwordInput = screen.getByPlaceholderText('Password')

  fireEvent.change(emailInput, { target: { value: 'example@test.com' } })
  fireEvent.change(passwordInput, { target: { value: 'password123' } })

  expect(emailInput).toHaveValue('example@test.com')
  expect(passwordInput).toHaveValue('password123')
})

it('should submit the form when the "Sign In" button is clicked', async () => {
  const navigate = jest.fn()
  const accessAcount = jest.fn()
  // Render your component with the mocked functions as props
  const { getByText, getByLabelText } = render(
    <BrowserRouter>
      <AuthProvider>
        <CustomerSignIn />
      </AuthProvider>
    </BrowserRouter>,
  )

  // Find and interact with the form inputs and button
  const emailInput = screen.getByPlaceholderText('Email')
  const passwordInput = screen.getByPlaceholderText('Password')
  const signInButton = screen.getByRole('button')

  // Set values in the form inputs
  fireEvent.change(emailInput, { target: { value: 'example@test.com' } })
  fireEvent.change(passwordInput, { target: { value: 'password123' } })

  // Click the "Sign In" button to trigger the form submission
  fireEvent.click(signInButton)

  // Wait for the expected asynchronous operations to complete
})
