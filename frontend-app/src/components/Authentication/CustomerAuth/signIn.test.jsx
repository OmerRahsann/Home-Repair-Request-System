import { render, screen, fireEvent, act } from '@testing-library/react'
import SignInForm from './SignIn.jsx';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '../../../AuthContext.js';
import userEvent from "@testing-library/user-event"
import axios from 'axios';

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

  it('should submit the form when the "Sign In" button is clicked', () => {
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
    fireEvent.change(emailInput, { target: { value: 'test1@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    userEvent.click(signInButton)
    });