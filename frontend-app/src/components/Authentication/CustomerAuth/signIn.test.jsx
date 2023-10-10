import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import SignInForm from './SignIn.jsx';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '../../../AuthContext.js';

test("SignIn Form renders successfully", () => {
    render(
    <BrowserRouter>
        <AuthProvider>
            <SignInForm/>
        </AuthProvider>
    </BrowserRouter>
    );
    expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
})

it('should update the state when input values change', () => {
    render(
        <BrowserRouter>
            <AuthProvider>
                <SignInForm/>
            </AuthProvider>
        </BrowserRouter>
        );
    const emailInput = screen.getByPlaceholderText('Email');
    const passwordInput = screen.getByPlaceholderText('Password');

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });

    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('password123');
  });

  it('should submit the form when the "Sign In" button is clicked', async () => {
    const accessAcount = jest.fn();
    const navigate = jest.fn();
    render(
        <BrowserRouter>
            <AuthProvider>
                <SignInForm/>
            </AuthProvider>
        </BrowserRouter>
        );
    const emailInput = screen.getByPlaceholderText('Email');
    const passwordInput = screen.getByPlaceholderText('Password');
    const signInButton = screen.getByText('Sign In');

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(signInButton);

    // You can add assertions here to check if the expected functions were called.
    // For example:
    await waitFor(() => expect(accessAcount).toHaveBeenCalledTimes(1));
    await waitFor(() => expect(navigate).toHaveBeenCalledWith('/'));
  });